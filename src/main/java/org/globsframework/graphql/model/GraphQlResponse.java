package org.globsframework.graphql.model;

import org.globsframework.json.annottations.IsJsonContentAnnotation;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.StringField;

public class GraphQlResponse {
    public static GlobType TYPE;

    @IsJsonContentAnnotation
    public static StringField data;

    public static StringField errorMessage;

    static {
        GlobTypeLoaderFactory.create(GraphQlResponse.class).load();
    }
}
