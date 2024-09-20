package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoader;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Key;

public class GraphqlEnum {
    public static GlobType TYPE;

    public static StringField name;

    public static StringArrayField values;

    @InitUniqueKey
    public static Key UNIQUE_KEY;

    static {
        GlobTypeLoader loader = GlobTypeLoaderFactory.create(GraphqlEnum.class);
        loader.register(GlobCreateFromAnnotation.class, annotation ->
                TYPE.instantiate()
                        .set(name, ((GraphqlEnum_) annotation).name())
                        .set(values, ((GraphqlEnum_) annotation).values())
        ).load();
    }

}
