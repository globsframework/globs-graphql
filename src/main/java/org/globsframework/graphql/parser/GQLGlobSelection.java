package org.globsframework.graphql.parser;

import org.globsframework.graphql.GQLGlobType;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeBuilder;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.metamodel.type.DataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GQLGlobSelection extends GqlGlobBuilderWithError {
    private final GlobType type;
    private GlobTypeBuilder outputTypeBuilder;
    private final GlobModel globModel;
    private final GqlQuery consumer;
    private final Map<Field, GqlField> aliasToField = new HashMap<>();

    interface GqlQuery {
        void complete(GQLGlobType gqlGlobType);
    }

    GQLGlobSelection(GlobType type, GlobModel globModel, GqlQuery consumer) {
        this.type = type;
        this.globModel = globModel;
        this.consumer = consumer;
        outputTypeBuilder = new DefaultGlobTypeBuilder(type.getName());
    }

    public void complete() {
        consumer.complete(new GQLGlobType(type, outputTypeBuilder.get(), aliasToField));
    }

    public GqlGlobBuilder addSub(String fieldName, String alias) {
        Field field = fieldName.startsWith("__") ? null : type.getField(fieldName);
        GqlGlobBuilder gqlGlobBuilder = new GQLGlobFieldBuilder(globModel, field, gqlField -> {
            Field f;
            if (gqlField.field().field() == null) {
                f = outputTypeBuilder.declare(alias, DataType.String, List.of());
            } else {
                f = gqlField.field().field().safeVisit(new FieldVisitor.AbstractFieldVisitor() {
                    Field f;

                    public void notManaged(Field field) throws Exception {
                        f = outputTypeBuilder.declare(alias, field.getDataType(), field.streamAnnotations().collect(Collectors.toList()));
                    }

                    public void visitGlob(GlobField field) throws Exception {
                        f = outputTypeBuilder.declareGlobField(alias, gqlField.gqlGlobType().outputType, field.streamAnnotations().collect(Collectors.toList()));
                    }

                    public void visitGlobArray(GlobArrayField field) throws Exception {
                        f = outputTypeBuilder.declareGlobArrayField(alias, gqlField.gqlGlobType().outputType, field.streamAnnotations().collect(Collectors.toList()));
                    }

                    public void visitUnionGlob(GlobUnionField field) throws Exception {
                        throw new RuntimeException(fieldName + " union not managed");
                    }

                    public void visitUnionGlobArray(GlobArrayUnionField field) throws Exception {
                        throw new RuntimeException(fieldName + "union not managed");
                    }
                }).f;
            }
            aliasToField.put(f, gqlField);
        });
        return gqlGlobBuilder;
    }
}
