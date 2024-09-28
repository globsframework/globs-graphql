package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Key;

public class GQLQueryParam {
    public static GlobType TYPE;

    public static StringField name;

    @InitUniqueKey
    public static Key KEY;

    static {
        GlobTypeLoaderFactory.create(GQLQueryParam.class, "GQLQueryParam")
                .register(GlobCreateFromAnnotation.class, annotation -> {
                    try {
                        return TYPE.instantiate()
                                .set(name, ((GlobType) ((GQLQueryParam_) annotation).value().getField("TYPE").get(null)).getName());
                    } catch (Exception e) {
                        throw new RuntimeException("Fail to extract TYPE");
                    }
                })
                .load();
    }
}
