package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoader;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.metamodel.annotations.InitUniqueKey;
import org.globsframework.metamodel.fields.StringArrayField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Key;

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
