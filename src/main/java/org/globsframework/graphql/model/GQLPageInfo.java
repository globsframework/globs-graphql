package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.StringField;

public class GQLPageInfo {
    public static GlobType TYPE;

    public static StringField startCursor;

    public static StringField endCursor;

    @GQLMandatory_
    public static BooleanField hasNextPage;

    @GQLMandatory_
    public static BooleanField hasPreviousPage;

    static {
        GlobTypeLoaderFactory.create(GQLPageInfo.class).load();
    }
}
