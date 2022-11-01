package org.globsframework.graphql.model;

import org.globsframework.json.annottations.IsJsonContentAnnotation;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.StringField;

public class GraphQlRequest {
    public static GlobType TYPE;

    public static StringField query;

    @IsJsonContentAnnotation
    public static StringField variables;

    static {
        GlobTypeLoaderFactory.create(GraphQlRequest.class).load();
    }
}

