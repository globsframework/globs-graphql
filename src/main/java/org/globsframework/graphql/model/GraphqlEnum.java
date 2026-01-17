package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;
import org.globsframework.core.model.MutableGlob;

public class GraphqlEnum {
    public static final GlobType TYPE;

    public static final StringField name;

    public static final StringArrayField values;

    @InitUniqueKey
    public static final Key UNIQUE_KEY;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GraphqlEnum");
        name = typeBuilder.declareStringField("name");
        values = typeBuilder.declareStringArrayField("values");
        typeBuilder.register(GlobCreateFromAnnotation.class,
                annotation -> getMutableGlob((GraphqlEnum_) annotation));
        TYPE = typeBuilder.build();
        UNIQUE_KEY = KeyBuilder.newEmptyKey(TYPE);
    }

    private static MutableGlob getMutableGlob(GraphqlEnum_ annotation) {
        return GraphqlEnum.TYPE.instantiate()
                .set(name, annotation.name())
                .set(values, annotation.values());
    }

}
