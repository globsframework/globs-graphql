package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.json.annottations.IsJsonContent_;

public class GraphQlRequest {
    public static GlobType TYPE;

    public static StringField query;

    @IsJsonContent_
    public static StringField variables;

    static {
        GlobTypeLoaderFactory.create(GraphQlRequest.class).load();
    }
}

