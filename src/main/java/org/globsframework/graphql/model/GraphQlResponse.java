package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.json.annottations.IsJsonContent;
import org.globsframework.json.annottations.IsJsonContent_;

public class GraphQlResponse {
    public static final GlobType TYPE;

    @IsJsonContent_
    public static final StringField data;

    public static final StringField errorMessage;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GraphQlResponse");
        data = typeBuilder.declareStringField("data", IsJsonContent.UNIQUE_GLOB);
        errorMessage = typeBuilder.declareStringField("errorMessage");
        TYPE = typeBuilder.build();
    }
}
