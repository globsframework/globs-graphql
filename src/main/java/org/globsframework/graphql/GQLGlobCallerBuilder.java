package org.globsframework.graphql;

import org.globsframework.functional.FunctionalKey;
import org.globsframework.functional.FunctionalKeyBuilder;
import org.globsframework.graphql.model.GQLPageInfo;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.fields.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValuesAccessor;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;
import org.globsframework.utils.Ref;
import org.globsframework.utils.collections.MapOfMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GQLGlobCallerBuilder<C extends GQLGlobCaller.GQLContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GQLGlobCallerBuilder.class);
    private Map<Field, GQLGlobLoad<C>> loaders = new HashMap<>();
    private Map<Field, ConnectionInfo<C>> connections = new HashMap<>();
    private MapOfMaps<Field, GlobType, GQLGlobFieldMapper> fieldMapper = new MapOfMaps<>();
    private MapOfMaps<GlobType, FunctionalKeyBuilder, GQLGlobFetcher<C>> fetchers = new MapOfMaps<>();
    private MapOfMaps<Field, GlobType, GQLKeyExtractor<C>> keyExtractors = new MapOfMaps<>();

    public void registerLoader(Field field, GQLGlobLoad<C> gqlGlobLoad) {
        loaders.put(field, gqlGlobLoad);
    }

    public GQLGlobFieldMapper get(Field field, GlobType type) {
        final GQLGlobFieldMapper gqlGlobFieldMapper = fieldMapper.get(field, type);
        if (gqlGlobFieldMapper == null) {
            Field fromField = type.getField(field.getName());
            final SimpleGQLGlobFieldMapper value = new SimpleGQLGlobFieldMapper(fromField, field);
            fieldMapper.put(field, type, value);
            return value;
        } else {
            return gqlGlobFieldMapper;
        }
    }

    public void registerField(Field field, GlobType sourceType, GQLGlobFieldMapper gqlGlobFieldMapper) {
        fieldMapper.put(field, sourceType, gqlGlobFieldMapper);
    }

    public void registerSimpleField(Field target, Field field) {
        fieldMapper.put(target, field.getGlobType(), new SimpleGQLGlobFieldMapper(field, target));
    }

    public GQLGlobCaller<C> build(GlobType rootQueryType, GlobModel parameters) {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(rootQueryType, parameters);
        return new DefaultGQLGlobCaller(gqlQueryParser);
    }

    public void registerFKeyFetcher(GlobType target, FunctionalKeyBuilder functionalKeyBuilder, GQLGlobFetcher<C> gqlGlobFetcher) {
        fetchers.put(target, functionalKeyBuilder, gqlGlobFetcher);
    }

    public void registerFKeyExtractor(Field field, GlobType from, GQLKeyExtractor<C> glindaExtractor) {
        keyExtractors.put(field, from, glindaExtractor);
    }

    public void registerConnection(GlobField globField, GQLGlobConnectionLoad<C> connection, StringField uuid, StringField paramForOrderBy) {
        connections.put(globField, new ConnectionInfo<>(connection, uuid, paramForOrderBy));
    }

    record ConnectionInfo<C extends GQLGlobCaller.GQLContext>(GQLGlobConnectionLoad<C> globLoad, StringField uuidField,
                                                              StringField paramOrderBy) {
    }

    public static class ToStringSerialiser {

        public static String toString(Field field, FieldValuesAccessor value) {
            if (value == null || value.isNull(field)) {
                return null;
            }
            final Ref<String> context = new Ref<>();
            field.safeVisitValue(Obj2StrVisitor.STR_VISITOR, value, context);
            return context.get();
        }

        public static Object toObject(Field field, String value) {
            if (value == null ) {
                return null;
            }
            final Ref<Object> ctx2 = new Ref<>();
            field.safeVisit(StrToObjectVisitor.STR_VISITOR, value, ctx2);
            return ctx2.get();
        }

        public static class StrToObjectVisitor extends FieldVisitorWithTwoContext.AbstractWithErrorVisitor<String, Ref<Object>> {
            private final static StrToObjectVisitor STR_VISITOR = new StrToObjectVisitor();

            @Override
            public void visitInteger(IntegerField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(Integer.parseInt(ctx1));
            }

            @Override
            public void visitDouble(DoubleField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(Double.parseDouble(ctx1));
            }

            @Override
            public void visitString(StringField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(ctx1);
            }

            @Override
            public void visitBoolean(BooleanField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(Boolean.parseBoolean(ctx1));
            }

            @Override
            public void visitLong(LongField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(Long.parseLong(ctx1));
            }

            @Override
            public void visitDate(DateField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(LocalDate.parse(ctx1));
            }

            @Override
            public void visitDateTime(DateTimeField field, String ctx1, Ref<Object> ctx2) throws Exception {
                ctx2.set(ZonedDateTime.parse(ctx1));
            }
        }

        public static class Obj2StrVisitor extends FieldValueVisitorWithContext.AbstractWithErrorVisitor<Ref<String>> {
            private final static Obj2StrVisitor STR_VISITOR = new Obj2StrVisitor();

            @Override
            public void visitInteger(IntegerField field, Integer value, Ref<String> stringRef) throws Exception {
                stringRef.set(Integer.toString(value));
            }

            @Override
            public void visitDouble(DoubleField field, Double value, Ref<String> stringRef) throws Exception {
                stringRef.set(Double.toString(value));
            }

            @Override
            public void visitString(StringField field, String value, Ref<String> stringRef) throws Exception {
                stringRef.set(value);
            }

            @Override
            public void visitBoolean(BooleanField field, Boolean value, Ref<String> stringRef) throws Exception {
                stringRef.set(Boolean.toString(value));
            }

            @Override
            public void visitLong(LongField field, Long value, Ref<String> stringRef) throws Exception {
                stringRef.set(Long.toString(value));
            }

            @Override
            public void visitDate(DateField field, LocalDate value, Ref<String> stringRef) throws Exception {
                stringRef.set(value.toString());
            }

            @Override
            public void visitDateTime(DateTimeField field, ZonedDateTime value, Ref<String> stringRef) throws Exception {
                stringRef.set(value.toString());
            }
        }

    }

    public static class CursorType {
        public static GlobType TYPE;

        public static StringField lastId;

        public static StringField lastOrderValue;

        static {
            GlobTypeLoaderFactory.create(CursorType.class).load();
        }
    }

    private class DefaultGQLGlobCaller implements GQLGlobCaller<C> {
        private final GQLQueryParser gqlQueryParser;

        public DefaultGQLGlobCaller(GQLQueryParser gqlQueryParser) {
            this.gqlQueryParser = gqlQueryParser;
        }

        public CompletableFuture<Glob> query(String query, Map<String, String> variables, C gqlContext) {
            GQLGlobType glQuery = gqlQueryParser.parse(query, variables);
            Node node = new Node(glQuery, glQuery.type.instantiate());
            CompletableFuture<Void> future = deepScan(glQuery, gqlContext, List.of(node));
            return future.thenApply(unused -> node.buildResponse(GQLGlobCallerBuilder.this));
        }

        private CompletableFuture<Void> deepScan(GQLGlobType glQuery,
                                                 C callContext, List<Node> current) {
            if (current.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            List<CompletableFuture<Void>> futurs = new ArrayList<>();
            for (Map.Entry<Field, GqlField> entry : glQuery.aliasToField.entrySet()) {
                final GqlField gqlField = entry.getValue();
                final Field outField = entry.getKey();
                if (gqlField.gqlGlobType() == null) {
                    continue;
                }
                Field field = gqlField.field().field();
                final ConnectionInfo<C> connection = connections.get(field);
                if (connection != null) {
                    List<Node> newNode = new ArrayList<>();
                    futurs.add(manageConnection(outField, gqlField, connection, current, callContext, newNode)
                            .thenCompose(gqlType -> deepScan(gqlType, callContext, newNode)));
                } else {
                    GQLGlobLoad<C> gqlGlobLoad = loaders.get(field);
                    if (gqlGlobLoad != null) {
                        List<Node> newNode = new ArrayList<>();
                        List<OnLoad> parents = current.stream().map(node -> new OnLoad(node.data, data -> {
                            newNode.add(node.addChild(outField, gqlField.gqlGlobType(), data));
                        })).collect(Collectors.toList());

                        CompletableFuture<Void> future = gqlGlobLoad.load(gqlField, callContext, parents)
                                .thenCompose(unused -> deepScan(gqlField.gqlGlobType(), callContext, newNode));
                        futurs.add(future);
                    } else {
                        Map<GlobType, GQLKeyExtractor<C>> gqlKeyExtractors = keyExtractors.get(field);
                        if (!gqlKeyExtractors.isEmpty()) {
                            final Set<GlobType> globTypes = current.stream().map(Node::getType).collect(Collectors.toSet());
                            for (GlobType globType : globTypes) {
                                final GQLKeyExtractor<C> gqlKeyExtractor = gqlKeyExtractors.get(globType);
                                if (gqlKeyExtractor == null) {
                                    final String msg = "No extractor found for " + globType.getName() + " got " + gqlKeyExtractors.keySet().stream().map(GlobType::getName).collect(Collectors.toSet());
                                    LOGGER.error(msg);
                                    throw new RuntimeException(msg);
                                }
                                MapOfMaps<FunctionalKeyBuilder, FunctionalKey, List<Node>> call = new MapOfMaps<>();
                                gqlKeyExtractor.extract(gqlField, callContext,
                                        current.stream().map(node -> new OnExtract(node.data, key -> {
                                            List<Node> list = call.get(key.getBuilder(), key);
                                            if (list == null) {
                                                call.put(key.getBuilder(), key, new ArrayList<Node>(List.of(node)));
                                            } else {
                                                list.add(node);
                                            }
                                        })).collect(Collectors.toList()));
                                for (Map.Entry<FunctionalKeyBuilder, Map<FunctionalKey, List<Node>>> functionalKeyBuilderMapEntry : call.entry()) {
                                    List<Node> newNode = new ArrayList<>();
                                    GQLGlobFetcher<C> gqlGlobFetcher = fetchers.get(gqlField.gqlGlobType().type, functionalKeyBuilderMapEntry.getKey());
                                    if (gqlGlobFetcher == null) {
                                        final String s = "Can not find fetcher for " + gqlField.gqlGlobType().type;
                                        LOGGER.error(s);
                                        throw new RuntimeException(s);
                                    }
                                    gqlGlobFetcher.load(gqlField.gqlGlobType(), callContext, functionalKeyBuilderMapEntry.getValue().entrySet()
                                                    .stream().map(functionalKeyListEntry -> new OnKey(functionalKeyListEntry.getKey(), data -> {
                                                        for (Node node : functionalKeyListEntry.getValue()) {
                                                            newNode.add(node.addChild(outField, gqlField.gqlGlobType(), data));
                                                        }
                                                    })).collect(Collectors.toList()))
                                            .thenCompose(unused -> deepScan(gqlField.gqlGlobType(), callContext, newNode));

                                }
                            }
                        } else {
                            LOGGER.warn("no loader for " + field);
                        }
                    }
                }
            }
            return CompletableFuture.allOf(futurs.toArray(CompletableFuture[]::new));
        }

        record PageInfoField(GlobType pageType, StringField startCursor, BooleanField hasNextPage,
                             StringField endCursor, BooleanField hasPreviousPage) {
        }

        static PageInfoField extract(GlobType pageType) {
            final StringField startCursor = pageType.findOptField(GQLPageInfo.startCursor.getName()).map(Field::asStringField).orElse(null);
            final StringField endCursor = pageType.findOptField(GQLPageInfo.endCursor.getName()).map(Field::asStringField).orElse(null);
            final BooleanField hasNextPage = pageType.findOptField(GQLPageInfo.hasNextPage.getName()).map(Field::asBooleanField).orElse(null);
            final BooleanField hasPreviousPage = pageType.findOptField(GQLPageInfo.hasPreviousPage.getName()).map(Field::asBooleanField).orElse(null);
            return new PageInfoField(pageType, startCursor, hasNextPage, endCursor, hasPreviousPage);
        }

        private CompletableFuture<GQLGlobType> manageConnection(Field outField, GqlField gqlField, ConnectionInfo<C> connectionInfo,
                                                                List<Node> current, C callContext, List<Node> newNode) {
            GQLGlobConnectionLoad<C> loader = connectionInfo.globLoad();
            GQLGlobType nodeType = null;
            for (Node node : current) {
                List<Glob> values = new ArrayList<>();
                Ref<GQLGlobConnectionLoad.CursorInfo> cursorInfoRef = new Ref<>();
                final CompletableFuture<Void> completableFuture = loader.load(gqlField, callContext,
                        List.of(new GQLGlobConnectionLoad.OnConnectionLoad(node.data, values::add, cursorInfoRef::set)));
                completableFuture.join();
                final MutableGlob connectionData = gqlField.gqlGlobType().outputType.instantiate();
                final Node connectionNode = node.addChild(outField, gqlField.gqlGlobType(), connectionData);
                final Optional<IntegerField> total = gqlField.gqlGlobType().outputType.findOptField("totalCount").map(Field::asIntegerField);
                final Optional<GlobField> pageInfoField = gqlField.gqlGlobType().outputType.findOptField("pageInfo").map(Field::asGlobField);
                final Optional<PageInfoField> pageInfoType = pageInfoField.map(t -> extract(t.getTargetType()));
                final Optional<GlobArrayField> edgesField = gqlField.gqlGlobType().outputType.findOptField("edges").map(Field::asGlobArrayField);
                final Optional<GlobType> edgesType = edgesField.map(GlobArrayField::getTargetType);
                final Optional<GlobField> edgeNode = edgesType.map(t -> t.findField("node")).map(Field::asGlobField);
                final Optional<StringField> edgeCursor = edgesType.map(t -> t.findField("cursor")).map(Field::asStringField);
                final Optional<GqlField> pageInfoGQLField = pageInfoField.map(pi -> gqlField.gqlGlobType().aliasToField.get(pi));
                final Optional<MutableGlob> pageInfo = pageInfoField.map(globField -> globField.getTargetType().instantiate());

                Glob first = null;
                Glob last = null;
                if (edgesField.isPresent() && edgesType.isPresent()) {
                    final GqlField edgeGQLField = gqlField.gqlGlobType().aliasToField.get(edgesField.get());
                    boolean withCursor = edgeCursor.isPresent();
                    Optional<GqlField> nodeGqlField = edgeNode.map(en -> edgeGQLField.gqlGlobType().aliasToField.get(en));
                    if (nodeGqlField.isPresent()) {
                        nodeType = nodeGqlField.get().gqlGlobType();
                        for (Glob value : values) {
                            if (first == null) {
                                first = value;
                            }
                            last = value;
                            final MutableGlob edge = edgesType.get().instantiate();
                            if (withCursor) {
                                final MutableGlob st = getCursor(gqlField, connectionInfo, value, value.getType())
                                        .set(CursorType.lastId, value.get(connectionInfo.uuidField()));
                                edge.set(edgeCursor.get(), Base64.getEncoder().encodeToString(GSonUtils.encode(st, true).getBytes(StandardCharsets.UTF_8)));
                            }
//                    edge.set(edgeNode, value);
                            newNode.add(
                                    connectionNode
                                            .addChild(edgesField.get(), edgeGQLField.gqlGlobType(), edge)
                                            .addChild(edgeNode.get(), nodeType, value));
                        }
                    }
                }
                if (first != null) {
                    GlobType innerType = first.getType();
                    if (pageInfoType.isPresent() && pageInfoType.get().startCursor != null) {
                        final MutableGlob st = getCursor(gqlField, connectionInfo, first, innerType)
                                .set(CursorType.lastId, first.get(connectionInfo.uuidField()));
                        pageInfo.get().set(pageInfoType.get().startCursor, Base64.getEncoder().encodeToString(GSonUtils.encode(st, true).getBytes(StandardCharsets.UTF_8)));
                    }
                }
                if (last != null) {
                    GlobType innerType = last.getType();
                    if (pageInfoType.isPresent() && pageInfoType.get().endCursor != null) {
                        final MutableGlob st = getCursor(gqlField, connectionInfo, last, innerType)
                                .set(CursorType.lastId, last.get(connectionInfo.uuidField()));
                        pageInfo.get().set(pageInfoType.get().endCursor, Base64.getEncoder().encodeToString(GSonUtils.encode(st, true).getBytes(StandardCharsets.UTF_8)));
                    }
                }

                if (cursorInfoRef.get() != null && pageInfoType.isPresent()) {
                    if (pageInfoType.get().hasNextPage != null) {
                        pageInfo.get().set(pageInfoType.get().hasNextPage, cursorInfoRef.get().hasNext());
                    }
                    if (pageInfoType.get().hasPreviousPage != null) {
                        pageInfo.get().set(pageInfoType.get().hasPreviousPage, cursorInfoRef.get().hasPrevious());
                    }
                }
                if (pageInfoGQLField.isPresent()) {
                    connectionNode.addChild(pageInfoField.get(), pageInfoGQLField.get().gqlGlobType(), pageInfo.get());
                }
                if (cursorInfoRef.get() != null && total.isPresent()) {
                    connectionData.setValue(total.get(), cursorInfoRef.get().totalCount());
                }
            }
            return CompletableFuture.completedFuture(nodeType);
        }

        private MutableGlob getCursor(GqlField gqlField, ConnectionInfo<C> connectionInfo, Glob data, GlobType innerType) {
            return gqlField.field().parameters().map(connectionInfo.paramOrderBy)
                    .map(innerType::getField)
                    .map(f -> CursorType.TYPE.instantiate()
                            .set(CursorType.lastOrderValue, ToStringSerialiser.toString(f, data)))
                    .orElse(CursorType.TYPE.instantiate());
        }
    }

}
