package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.json.annottations.IsJsonContentAnnotation;

public class GraphQlResponse {
    public static GlobType TYPE;

    @IsJsonContentAnnotation
    public static StringField data;

    public static StringField errorMessage;

    static {
        GlobTypeLoaderFactory.create(GraphQlResponse.class).load();
    }
}
