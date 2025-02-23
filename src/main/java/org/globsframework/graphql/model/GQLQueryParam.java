package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class GQLQueryParam {
    public static final GlobType TYPE;

    public static final StringField name;

    @InitUniqueKey
    public static final Key KEY;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GQLQueryParam");
        TYPE = typeBuilder.unCompleteType();
        name = typeBuilder.declareStringField("name");
        typeBuilder.complete();
        KEY = KeyBuilder.newEmptyKey(TYPE);
        typeBuilder.register(GlobCreateFromAnnotation.class, annotation -> {
            try {
                return TYPE.instantiate()
                        .set(name, ((GlobType) ((GQLQueryParam_) annotation).value().getField("TYPE").get(null)).getName());
            } catch (Exception e) {
                throw new RuntimeException("Fail to extract TYPE");
            }
        })
        ;
//        GlobTypeLoaderFactory.create(GQLQueryParam.class, "GQLQueryParam")
//                .register(GlobCreateFromAnnotation.class, annotation -> {
//                    try {
//                        return TYPE.instantiate()
//                                .set(name, ((GlobType) ((GQLQueryParam_) annotation).value().getField("TYPE").get(null)).getName());
//                    } catch (Exception e) {
//                        throw new RuntimeException("Fail to extract TYPE");
//                    }
//                })
//                .load();
    }

    public static Glob create(GlobType type) {
        return TYPE.instantiate().set(name, type.getName());
    }
}
