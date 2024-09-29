package org.globsframework.graphql.parser;

import org.globsframework.core.metamodel.GlobModel;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.graphql.GQLGlobType;
import org.globsframework.graphql.QGLFieldWithParameter;
import org.globsframework.graphql.model.GQLQueryParam;

import java.util.Optional;
import java.util.function.Consumer;

public class GQLGlobFieldBuilder extends GqlGlobBuilderWithError {
    private final GlobModel model;
    private final Field field;
    private final Consumer<GqlField> consumer;
    private Glob parameters;
    private GQLGlobType gqlGlobType;

    public GQLGlobFieldBuilder(GlobModel model, Field field, Consumer<GqlField> consumer) {
        this.model = model;
        this.field = field;
        this.consumer = consumer;
    }

    public MutableGlob getArguments() {
        if (parameters != null) {
            throw new RuntimeException("Bug : parameters already set");
        }
        Glob annotation = field.getAnnotation(GQLQueryParam.KEY);
        GlobType type = model.getType(annotation.get(GQLQueryParam.name));
        MutableGlob instantiate = type.instantiate();
        parameters = instantiate;
        return instantiate;
    }

    public GqlGlobBuilder getSubBuilder() {
        if (field instanceof GlobArrayField) {
            return new GQLGlobSelection(((GlobArrayField) field).getTargetType(), model, (gqlGlobType) -> {
                this.gqlGlobType = gqlGlobType;
            });
        } else if (field instanceof GlobField) {
            return new GQLGlobSelection(((GlobField) field).getTargetType(), model, (gqlGlobType) -> {
                this.gqlGlobType = gqlGlobType;
            });
        }
        throw new RuntimeException("selection not expected on " + field.getName());
    }

    public void complete() {
        consumer.accept(new GqlField(new QGLFieldWithParameter(field, Optional.ofNullable(parameters)), gqlGlobType));
    }
}
