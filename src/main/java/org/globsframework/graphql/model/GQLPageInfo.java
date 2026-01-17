package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;

public class GQLPageInfo {
    public static final GlobType TYPE;

    public static final StringField startCursor;

    public static final StringField endCursor;

    @GQLMandatory_
    public static final BooleanField hasNextPage;

    @GQLMandatory_
    public static final BooleanField hasPreviousPage;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GQLPageInfo");
        startCursor = typeBuilder.declareStringField("startCursor");
        endCursor = typeBuilder.declareStringField("endCursor");
        hasNextPage = typeBuilder.declareBooleanField("hasNextPage", GQLMandatory.UNIQUE);
        hasPreviousPage = typeBuilder.declareBooleanField("hasPreviousPage", GQLMandatory.UNIQUE);
        TYPE = typeBuilder.build();
    }
}
