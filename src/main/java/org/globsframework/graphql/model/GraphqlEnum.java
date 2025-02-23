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

public class GraphqlEnum {
    public static final GlobType TYPE;

    public static final StringField name;

    public static final StringArrayField values;

    @InitUniqueKey
    public static final Key UNIQUE_KEY;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GraphqlEnum");
        TYPE = typeBuilder.unCompleteType();
        name = typeBuilder.declareStringField("name");
        values = typeBuilder.declareStringArrayField("values");
        typeBuilder.register(GlobCreateFromAnnotation.class, annotation ->
                TYPE.instantiate()
                        .set(name, ((GraphqlEnum_) annotation).name())
                        .set(values, ((GraphqlEnum_) annotation).values())
        );
        typeBuilder.complete();
        UNIQUE_KEY = KeyBuilder.newEmptyKey(TYPE);
//        GlobTypeLoader loader = GlobTypeLoaderFactory.create(GraphqlEnum.class, "GraphqlEnum");
//        loader.register(GlobCreateFromAnnotation.class, annotation ->
//                TYPE.instantiate()
//                        .set(name, ((GraphqlEnum_) annotation).name())
//                        .set(values, ((GraphqlEnum_) annotation).values())
//        ).load();
    }

}
