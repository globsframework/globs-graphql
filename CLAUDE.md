# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & test

Maven, Java 21 (`source`/`target` 21 in the pom; CI still uses JDK 17). The ANTLR plugin generates the GraphQL parser from `src/main/resources/Graphql.g4` into `target/generated-sources/antlr4/org/globsframework/graphql/parser/antlr/` — only `Graphql.g4` is compiled; `GraphqlCommon.g4`, `GraphqlOperation.g4`, `GraphqlSDL.g4` are unused leftovers.

```bash
mvn -o package                                  # build (offline: deps are local snapshots)
mvn -o test                                     # all tests
mvn -o test -Dtest='GQLQueryParserTest#testFragment'   # single method (works despite JUnit3 TestCase style)
```

The `org.globsframework:globs` (5.3-SNAPSHOT) and `globs-sql` (5.1-SNAPSHOT) deps are sibling projects under `/home/guiot/dev/globs/`, resolved from `~/.m2`. If a change needs a newer core, build the sibling repo first. `settings.xml` + `GH_MAVEN_REGISTRY_USER`/`GH_MAVEN_REGISTRY_ACCESS_TOKEN` are only needed for online resolution from GitHub Packages.

`DefaultDbGraphqlQueryTest` runs against an in-memory hsqldb (test-scoped dep); the rest need no external service.

## What this library is

A GraphQL execution engine where every value is a `Glob` (globsframework's dynamic-typed record) and every type is a `GlobType`. There is no code generation and no `graphql-java`: the schema *is* a tree of `GlobType`s, and the response *is* a `Glob` whose `GlobType` is built at runtime to match the query's selection set.

Central distinction to keep in mind: **the GraphQL type is not the data type**. A node in the graph holds a source `Glob` (whatever the loader pushed — a DB row, a repository glob) while the query declares a GraphQL `GlobType` (`Human`, `HumanConnection`, …). Mapping between the two is explicit via field mappers.

## Execution pipeline

1. **Parse** — `GQLQueryParser` → ANTLR → `FragmentExtractor` (pre-pass collecting named fragments) → `AntlrGQLVisitor`. `GQLGlobSelection`/`GQLGlobFieldBuilder` walk the selection set against the schema `GlobType` and produce a `GQLGlobType` per level:
   - `type` — the schema GlobType being selected from
   - `outputType` — a `GlobType` built on the fly containing exactly the selected fields, **named by alias**
   - `aliasToField` — output field → `GqlField(QGLFieldWithParameter, GQLGlobType)`

   Arguments are rebuilt as JSON text by `AntlrGQLVisitor.JsonBuilder` and decoded with GSON into an instance of the field's parameter `GlobType` (declared by the `GQLQueryParam` annotation). Variables arrive as `Map<String, String>` of **raw JSON literals** — a string variable's value must include its quotes (`Map.of("ID", "\"AZE\"")`). A variable with no value throws `MissingVariable` internally and the argument is silently dropped (logged at warn).

2. **Resolve** — `GQLGlobCallerBuilder.DefaultGQLGlobCaller.deepScan` walks the `GQLGlobType` tree **level by level, not node by node**. For each field it looks up (in this order) a connection, a loader, or key extractors; the callback receives the *list* of all parents at that level, so a resolver issues one batched call per level (the anti-N+1 design). Results are attached with `Node.addChild`, building a parallel `Node` tree of source globs.

3. **Build response** — `Node.buildResponse` instantiates `outputType` and, per field, either recurses into children or calls the `GQLGlobFieldMapper` for `(graphql field, source GlobType)`. `__typename` is special-cased. A `GQLMandatory`-annotated array field with no children emits `[]` rather than null.

## Registering resolvers (`GQLGlobCallerBuilder`)

Four kinds, all keyed on the *schema* `Field`:

- `registerLoader(field, GQLGlobLoad)` — parent globs in, child globs out via `OnLoad.onNew().push(glob)`.
- `registerFKeyExtractor(field, sourceType, GQLKeyExtractor)` + `registerFKeyFetcher(targetType, functionalKeyBuilder, GQLGlobFetcher)` — the two-step form. The extractor turns each parent into `FunctionalKey`s; the engine groups identical keys across the whole level and calls the fetcher once per `FunctionalKeyBuilder`, fanning the result back to every parent that asked for that key. Use this when the same entity is reachable from several points in the schema. A missing fetcher/extractor for an encountered source `GlobType` throws.
- `registerConnection(globField, GQLGlobConnectionLoad, uuidField, orderByParamField)` — Relay connections (see below).
- `registerField` / `registerSimpleField` — field mapping. **Mapping is automatic when names match**: `get(field, type)` falls back to `type.getField(field.getName())` and caches a `SimpleGQLGlobFieldMapper`, so only differing names or computed values need registration.

The builder's `Executor` (constructor arg, default `Runnable::run` = caller thread) drives the `CompletableFuture` composition in `deepScan`. Note that `manageConnection` calls `completableFuture.join()` — connection loads are resolved synchronously regardless of the executor.

## Connections and cursors

`manageConnection` discovers the connection shape by **literal field names** on the GraphQL types: `totalCount`, `pageInfo`, `edges`, `edges.node`, `edges.cursor`, and on the page-info type the `GQLPageInfo` names (`startCursor`, `endCursor`, `hasNextPage`, `hasPreviousPage`). Each is optional; only what the query selects is filled. Types not following these names silently produce nothing.

A cursor is base64 of the JSON encoding of `GQLGlobCallerBuilder.CursorType` — `{"_kind":"Cursor","lastId":...,"lastOrderValue":...}`. `lastId` comes from the `uuidField` passed to `registerConnection`; `lastOrderValue` from the field named by the `orderByParamField` argument in the query parameters. That is why both fields are declaration arguments.

The `db` package closes the loop for SQL sources: `ConnectionBuilder` reads `after`/`before`/`first`/`last`/`skip`/`orderBy`/`order` from the parameter glob, `DbGQLQueryBuilder.afterB64` decodes the cursor back into a `Last(id, orderValue)`, and `DefaultDbGraphqlQuery` emits keyset-pagination constraints (`(order = v AND id > lastId) OR order > v`, reversed for `desc`). Page size defaults to 10 and is clamped to 100; `totalCount` triggers a separate count query.

## Schema declaration and generation

Types are declared with globs v5 `GlobTypeBuilder` (`GlobTypeBuilderFactory.create(name)` … `build()`) — the old `GlobTypeLoaderFactory`/introspection path was removed in v5. **The README's examples still show the pre-v5 `GlobTypeLoaderFactory` style and are out of date**; follow `src/test/java/org/globsframework/graphql/model/` instead. Those model classes keep the Java annotations (`@Target`, `@GQLQueryParam_`, `@KeyField_`) as documentation, but the effective declaration is the builder call (`builder.declareGlobField("humain", () -> Human.TYPE, GQLQueryParam.create(HumanQuery.TYPE))`).

The root schema type has `query` and `mutation` `GlobField`s; `AntlrGQLVisitor` picks the branch from the operation keyword.

Glob annotations understood by the engine (`org.globsframework.graphql.model`, aggregated in `AllGraphQLAnnotations`):
- `GQLQueryParam` — names the parameter `GlobType` for a field. Parameter types must be passed to `build(...)`/`GlobSchemaGenerator` in a `GlobModel`, since they are resolved by name at parse time.
- `GQLMandatory` — `!` in generated SDL, and `[]`-instead-of-null in responses.
- `GraphqlEnum` — emits an `enum` in SDL and types the field as that enum.

`GlobSchemaGenerator` walks the schema type and emits SDL text (scalars `Date`, `DateTime`, `Long`; key `StringField` → `ID`). It only generates — nothing validates the runtime resolvers against it.
