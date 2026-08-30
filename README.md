# Globs GraphQL

A GraphQL execution engine where every value is a [Glob](https://globsframework.org) and every type is a
`GlobType`. There is no code generation and no `graphql-java`: the schema *is* a tree of `GlobType`s, and the
response *is* a Glob whose `GlobType` is built at runtime to match the query's selection set.

Resolvers are called **once per level, not once per node** — every parent at a level is handed to the loader
in one list — so the N+1 problem is designed out rather than patched with a data loader.

One distinction to keep in mind throughout: **the GraphQL type is not the data type**. A node in the graph
holds a source Glob (a DB row, a repository glob — whatever the loader pushed), while the query selects from
a GraphQL `GlobType` (`Human`, `HumanConnection`, …). The mapping between the two is explicit, though it is
automatic when the names match.

## Requirements

Java 21, `org.globsframework:globs`, `globs-gson`; `globs-sql` for the database-backed connections.

## Installation

```xml
<dependency>
    <groupId>org.globsframework</groupId>
    <artifactId>globs-graphql</artifactId>
    <version>5.0.0</version>
</dependency>
```

## Declaring the schema

The schema is a `GlobType` with a `query` and a `mutation` field:

```java
public class SchemaType {
    public static GlobType TYPE;

    public static GlobField<QueryType> query;

    public static GlobField<QueryMutation> mutation;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Schema");
        query = builder.declareGlobField("query", () -> QueryType.TYPE);
        mutation = builder.declareGlobField("mutation", () -> QueryMutation.TYPE);
        TYPE = builder.build();
    }
}
```

the query type, each field carrying the `GlobType` of its arguments through `GQLQueryParam`:

```java
public class QueryType {
    public static GlobType TYPE;

    public static GlobField<Human> humain;

    public static GlobField<HumanConnection> humains;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Query");
        humain = builder.declareGlobField("humain", () -> Human.TYPE, GQLQueryParam.create(HumanQuery.TYPE));
        humains = builder.declareGlobField("humains", () -> HumanConnection.TYPE, GQLQueryParam.create(HumansQuery.TYPE));
        TYPE = builder.build();
    }
}
```

and a connection, which must follow the Relay names — `totalCount`, `edges`, `edges.node`, `edges.cursor`,
`pageInfo` — because that is how the engine recognizes one:

```java
public class HumanConnection {
    public static GlobType TYPE;

    public static IntegerField totalCount;

    public static GlobArrayField<HumanEdgeConnection> edges;

    public static GlobField<GQLPageInfo> pageInfo;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("HumanConnection");
        totalCount = builder.declareIntegerField("totalCount");
        edges = builder.declareGlobArrayField("edges", () -> HumanEdgeConnection.TYPE);
        pageInfo = builder.declareGlobField("pageInfo", () -> GQLPageInfo.TYPE);
        TYPE = builder.build();
    }
}
```

Only the fields the query actually selects are filled, so a connection type may leave out what it does not
offer. `src/test/java/org/globsframework/graphql/model/` holds the full set of reference declarations.

## Resolvers

Next we register the functions that fetch the globs and map the fields. A node in the graph is a Glob pushed
by its parent; its type is the *data* type, not the GraphQL one, hence the field mapping.

Everything is registered on a builder:

```java
GQLGlobCallerBuilder gqlGlobCallerBuilder = new GQLGlobCallerBuilder();
```

There are four kinds of registration — a **loader** (parents in, children out), a **connection** (a Relay
page), the **functional-key** pair extractor/fetcher (for an entity reachable from several points in the
schema), and **field mappings**. Each is keyed on the *schema* field.

A field mapping is one line:

```java
gqlGlobCallerBuilder.registerSimpleField(Humain.firstName, DbHumain.firstName);
```

and is not even needed when the two names match — the engine falls back to the data type's field of the same
name, and caches that.

A computed mapping:

```
gqlGlobCallerBuilder.registerField(Human.BirthDate.day, birthDate.getGlobType(), (source, target) -> target.set(Human.BirthDate.day, source.get(birthDate).getDayOfMonth()));
```

It reads: for the field `Human.BirthDate.day`, when the node's GlobType is `birthDate.getGlobType()`, apply
this mapping to extract the day.

Now to fetch one Humain:

```
gqlGlobCallerBuilder.registerLoader(QueryType.humain, new GQLGlobLoad<>() {
    @Override
    public CompletableFuture<Object> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnLoad> parents) {
        Glob parameters = gqlField.field().parameters().orElseThrow();
        if (parameters.isSet(HumanQuery.id)) {
            String id = parameters.get(HumanQuery.id);
            for (OnLoad parent : parents) {
                parent.onNew().push(globRepository.get(KeyBuilder.create(humainType, id)));
            }
        }
        return CompletableFuture.completedFuture(null);
    }
});
```

The engine groups every parent of a level, which is why `parents` is a list: there is one call per level,
not one per node.
The humain has a mandatory parameter, we retrieve it, retrieve the associated glob (from the globRepository or from the
db)
then the glob is push to each parent.

It is possible to retrieve glob using functionnalKey, it is usefull if the schema allow the access to the same object
from different point.

After creating a functionalKeyBuilder, we register a FKeyFetcher that given a functionnalKey return the associated
Glob (a cache can be used using the context)
The code bellow is to access a Humain using it's id

```
        FunctionalKeyBuilder functionalKeyBuilder = new DefaultFunctionalKeyBuilderFactory(humainType)
                .add(id).create();
        gqlGlobCallerBuilder.registerFKeyFetcher(Human.TYPE, functionalKeyBuilder, new GQLGlobFetcher<>() {
            @Override
            public CompletableFuture<Void> load(GQLGlobType gqlGlobType, GQLGlobCaller.GQLContext callContext, List<OnKey> onKeys) {
                for (OnKey parent : onKeys) {
                    parent.onNew().push(globRepository.get(KeyBuilder.create(humainType, parent.key().getValue(id))));
                }
                return CompletableFuture.completedFuture(null);
            }
        });
```

And a KeyExtractor, we want the friends for each humain.
So for each parent we retrieve the id of it's friend and push the functionnalKey using parent.onNew()

```
        gqlGlobCallerBuilder.registerFKeyExtractor(Human.friends, id.getGlobType(), new GQLKeyExtractor<>() {
            @Override
            public CompletableFuture<Void> extract(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnExtract> parents) {
                for (OnExtract parent : parents) {
                    List<String> value = new ArrayList<>(friends.get(parent.parent().get(id)));
                    value.sort(String::compareTo);
                    for (String s : value) {
                        parent.onNew().push(functionalKeyBuilder.create()
                                .setValue(id, s).create());
                    }
                }
                return CompletableFuture.completedFuture(null);
            }
        });
```

The underlying code call the keyFetcher to associate each humain to their friends.

The last register it to managed connection with cursor and order.

```
        gqlGlobCallerBuilder.registerConnection(QueryType.humains, new GQLGlobConnectionLoad<>() {
            @Override
            public CompletableFuture<Void> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnConnectionLoad> parents) {
                final OnConnectionLoad onConnectionLoad = parents.get(0);
                List<Glob> all = globRepository.getAll(humainType);
                final Optional<String> order = gqlField.field().parameters().map(HumansQuery.orderBy);
                if (order.isPresent()) {
                    List<Glob> l = new ArrayList<Glob>(all);
                    l.sort(Comparator.comparing(g -> g.get(humainType.getField(order.get()).asStringField())));
                    all = l;
                }
                for (Glob glob : all) {
                    onConnectionLoad.onNew().push(glob);
                }
                onConnectionLoad.onCursor().push(new CursorInfo(false, false, all.size()));
                return CompletableFuture.completedFuture(null);
            }
        }, id, HumansQuery.orderBy);

```

By declaring id and orderBy we allow the library to automatically create the next and previous field (in base64)
The cursor give the information for hasNext/hasPrevious.

A more realist query using db :

```
        gqlGlobCallerBuilder.registerConnection(HumainQuery.humains, new GQLGlobConnectionLoad<GQLGlobCaller.GQLContext>() {
            @Override
            public CompletableFuture<Void> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnConnectionLoad> parents) {
                return
                        ConnectionBuilder.withDbKey(DbHumain.uuid)
                                .withParam(HumainQuery.Parameter.EMPTY, HumainQuery.Parameter.after,
                                        HumainQuery.Parameter.first, HumainQuery.Parameter.before,
                                        HumainQuery.Parameter.last, HumainQuery.Parameter.skip)
                                .withOrder(HumainQuery.Parameter.orderBy, HumainQuery.Parameter.order)
                                .scanAll(gqlField, parents.get(0), null, db);
            }
        }, DbHumain.uuid, HumainQuery.Parameter.orderBy);
```

Using a ConnectionBuilder it will fetch the wanted size, the first cursor, order, etc.
It do all the job!
The cursor are encoded int base64 :
```eyJfa2luZCI6ImN1cnNvclR5cGUiLCJsYXN0SWQiOiI4NjQiLCJsYXN0T3JkZXJWYWx1ZSI6ImZpcnN0TmFtZSA2NSJ9``` ->
```{"_kind":"cursorType","lastId":"864","lastOrderValue":"firstName 65"}```
And used in the db query.

After the declaration of the functor we build a GQLGlobCaller

```
        final GQLGlobCaller<GQLGlobCaller.GQLContext> build =
                gqlGlobCallerBuilder.build(SchemaType.TYPE, new DefaultGlobModel(HumainQuery.Parameter.TYPE));
```

Then The query :

```
            final CompletableFuture<Glob> id1 = gqlGlobCaller.query("""
                    query myFriends {
                       humain(id: $ID) {
                            firstName     
                            lastName     
                            birthDate {  
                                   year     
                                 }     
                            friends(sort: "lastName", name: ["AA", "BB"]) {
                                   firstName        
                                   friends(sort: "lastName") {
                                      firstName           
                                      lastName        
                                   }     
                            }   
                      }
                    }""",
                    Map.of("ID", "\"AZE\""), null);
```

Last, it is possible to generate the schema using

```
        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(SchemaType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
        final String s = globSchemaGenerator.generateAll();
```

That SDL can be handed to graphql-java to publish the schema on an introspection endpoint.

## Query variables

Variables arrive as a `Map<String, String>` of **raw JSON literals**, so a string variable carries its own
quotes: `Map.of("ID", "\"AZE\"")`. A variable with no value is dropped from the arguments and logged at
warn rather than failing the query.


## Building

```bash
mvn -o package
mvn -o test
mvn -o test -Dtest='GQLQueryParserTest#testFragment'
```

The GraphQL parser is generated from `src/main/resources/Graphql.g4` by the ANTLR plugin.
`DefaultDbGraphqlQueryTest` runs against an in-memory HSQLDB; nothing else needs an external service.

## License

Apache License 2.0 — see <https://www.apache.org/licenses/LICENSE-2.0.txt>.

## Links

- [Globs Framework](https://globsframework.org)
- [GitHub repository](https://github.com/globsframework/globs-graphql)
- [globs-examples](https://github.com/globsframework/globs-examples) — a server exposing REST, OpenAPI and GraphQL over the same types
