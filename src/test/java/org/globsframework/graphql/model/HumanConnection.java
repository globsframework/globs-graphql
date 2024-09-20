package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.IntegerField;

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
