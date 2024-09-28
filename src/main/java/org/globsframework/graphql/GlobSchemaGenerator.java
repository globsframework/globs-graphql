package org.globsframework.graphql;

import org.globsframework.core.metamodel.GlobModel;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.annotations.FieldName;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.globsframework.graphql.model.GQLMandatory;
import org.globsframework.graphql.model.GQLQueryParam;
import org.globsframework.graphql.model.GraphqlEnum;

import java.util.*;

public class GlobSchemaGenerator {
    private final GlobType schemaType;
    private final GlobModel parameters;
    private final Set<GlobType> types = new LinkedHashSet<>();
    private final Set<GlobType> input = new LinkedHashSet<>();
    private final Map<String, String[]> enums = new HashMap<>();

    public GlobSchemaGenerator(GlobType schemaType, GlobModel parameters) {
        this.schemaType = schemaType;
        this.parameters = parameters;
        for (GlobType parameter : parameters) {
            addChildInput(parameter);
        }
        loopType(schemaType);
    }

//    private void check() {
//        String schema = generateAll();
//
//        SchemaParser schemaParser = new SchemaParser();
//        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
//        RuntimeWiring runtimeWiring = newRuntimeWiring()
//                .scalar(GraphQLScalarType.newScalar().name("Date")
//                        .coercing(new GraphqlStringCoercing())
//                        .build())
//                .scalar(GraphQLScalarType.newScalar().name("DateTime")
//                        .coercing(new GraphqlStringCoercing())
//                        .build())
//                .scalar(GraphQLScalarType.newScalar().name("Long")
//                        .coercing(new Coercing<Long, Long>() {
//                            @Override
//                            public Long serialize(Object dataFetcherResult) throws CoercingSerializeException {
//                                return null;
//                            }
//
//                            @Override
//                            public Long parseValue(Object input) throws CoercingParseValueException {
//                                return null;
//                            }
//
//                            @Override
//                            public Long parseLiteral(Object input) throws CoercingParseLiteralException {
//                                return null;
//                            }
//                        })
//                        .build())
//                    .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world")))
//                .build();
//
//        SchemaGenerator schemaGenerator = new SchemaGenerator();
//        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
//
//        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
//        ExecutionResult executionResult = build.execute("{}");
//
//        System.out.println(executionResult.getData().toString());
//    }

    public String generateAll() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("scalar Date\n");
        stringBuilder.append("scalar DateTime\n");
        stringBuilder.append("scalar Long\n");
        for (Map.Entry<String, String[]> stringEntry : enums.entrySet()) {
            stringBuilder.append("enum ").append(stringEntry.getKey()).append("{").append("\n");
            for (String v : stringEntry.getValue()) {
                stringBuilder.append(v).append("\n");
            }
            stringBuilder.append("}\n");
        }
        stringBuilder.append(generate(schemaType, "schema ", false));
        stringBuilder.append("\n\n");
        for (GlobType type : types) {
            stringBuilder.append(generate(type, "type ", true));
            stringBuilder.append("\n\n");
        }
        for (GlobType type : input) {
            stringBuilder.append(generate(type, "input ", true));
            stringBuilder.append("\n\n");
        }
        return stringBuilder.toString();
    }

    public void add(GlobType type) {
        if (types.add(type)) {
            loopType(type);
        }
    }

    private void loopType(GlobType type) {
        for (Field field : type.getFields()) {
            if (field instanceof GlobField) {
                add(((GlobField) field).getTargetType());
            } else if (field instanceof GlobArrayField) {
                add(((GlobArrayField) field).getTargetType());
            }
            if (field.hasAnnotation(GraphqlEnum.UNIQUE_KEY)) {
                final Glob annotation = field.getAnnotation(GraphqlEnum.UNIQUE_KEY);
                enums.put(annotation.get(GraphqlEnum.name), annotation.get(GraphqlEnum.values));
            }
        }
    }

    public void addInput(GlobType type) {
        if (input.add(type)) {
            addChildInput(type);
        }
    }

    private void addChildInput(GlobType type) {
        for (Field field : type.getFields()) {
            if (field instanceof GlobField) {
                addInput(((GlobField) field).getTargetType());
            } else if (field instanceof GlobArrayField) {
                addInput(((GlobArrayField) field).getTargetType());
            }
            if (field.hasAnnotation(GraphqlEnum.UNIQUE_KEY)) {
                final Glob annotation = field.getAnnotation(GraphqlEnum.UNIQUE_KEY);
                enums.put(annotation.get(GraphqlEnum.name), annotation.get(GraphqlEnum.values));
            }
        }
    }

    String generate(GlobType type, String graphqlType, boolean outputType) {
        StringBuilder desc = new StringBuilder();
        desc.append(graphqlType);
        if (outputType) {
            desc.append(type.getName());
        }
        desc.append(" {\n");
        for (Field field : type.getFields()) {
            desc.append(FieldName.getName(field));
            Optional<Glob> typeParameters = field.findOptAnnotation(GQLQueryParam.KEY);
            typeParameters.ifPresent(glob -> {
                desc.append("(");
                GlobType parametersType = parameters.getType(glob.get(GQLQueryParam.name));
                for (Field paramField : parametersType.getFields()) {
                    desc.append(FieldName.getName(paramField))
                            .append(":");
                    paramField.safeAccept(new ToGQLTypeVisitor(desc));
                    if (paramField.hasAnnotation(GQLMandatory.KEY)) {
                        desc.append("!");
                    }
                    desc.append(", ");
                }
                desc.replace(desc.length() - 2, desc.length(), ")");
            });
            desc.append(" : ");
            field.safeAccept(new ToGQLTypeVisitor(desc));
            if (field.hasAnnotation(GQLMandatory.KEY)) {
                desc.append("!");
            }
            desc.append("\n");
        }
        desc.append("}\n\n");
        return desc.toString();
    }

    private static class ToGQLTypeVisitor extends FieldVisitor.AbstractWithErrorVisitor {
        private final StringBuilder desc;

        public ToGQLTypeVisitor(StringBuilder desc) {
            this.desc = desc;
        }

        @Override
        public void visitInteger(IntegerField field) throws Exception {
            desc.append("Int");
        }

        @Override
        public void visitStringArray(StringArrayField field) throws Exception {
            if (field.hasAnnotation(GraphqlEnum.UNIQUE_KEY)) {
                desc.append("[");
                desc.append(field.getAnnotation(GraphqlEnum.UNIQUE_KEY).get(GraphqlEnum.name));
                desc.append("]");
            } else {
                desc.append("[String]");
            }
        }

        @Override
        public void visitDouble(DoubleField field) throws Exception {
            desc.append("Float");
        }

        @Override
        public void visitString(StringField field) throws Exception {
            if (field.isKeyField()) {
                desc.append("ID");
            } else {
                if (field.hasAnnotation(GraphqlEnum.UNIQUE_KEY)) {
                    desc.append(field.getAnnotation(GraphqlEnum.UNIQUE_KEY).get(GraphqlEnum.name));
                } else {
                    desc.append("String");
                }
            }
        }

        @Override
        public void visitBoolean(BooleanField field) throws Exception {
            desc.append("Boolean");
        }

        @Override
        public void visitLong(LongField field) throws Exception {
            desc.append("Long");
        }

        @Override
        public void visitDate(DateField field) throws Exception {
            desc.append("Date");
        }

        @Override
        public void visitDateTime(DateTimeField field) throws Exception {
            desc.append("DateTime");
        }

        @Override
        public void visitGlob(GlobField field) throws Exception {
            desc.append(field.getTargetType().getName());
        }

        @Override
        public void visitGlobArray(GlobArrayField field) throws Exception {
            desc.append("[")
                    .append(field.getTargetType().getName())
                    .append("]");
        }
    }
}
