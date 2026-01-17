package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.json.annottations.IsJsonContent;
import org.globsframework.json.annottations.IsJsonContent_;

public class GraphQlRequest {
    public static final GlobType TYPE;

    public static final StringField query;

    @IsJsonContent_
    public static final StringField variables;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GraphQlRequest");
        query = typeBuilder.declareStringField("query");
        variables = typeBuilder.declareStringField("variables", IsJsonContent.UNIQUE_GLOB);
        TYPE = typeBuilder.build();
    }
}

