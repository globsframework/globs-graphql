package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.IntegerField;

public class HumanConnection {
    public static GlobType TYPE;

    public static IntegerField totalCount;

    @Target(HumanEdgeConnection.class)
    public static GlobArrayField edges;

    @Target(GQLPageInfo.class)
    public static GlobField pageInfo;

    static {
        GlobTypeLoaderFactory.create(HumanConnection.class, "HumanConnection").load();
    }
}
