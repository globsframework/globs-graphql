package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.StringField;

public class HumanEdgeConnection {
    public static GlobType TYPE;

    @Target(Human.class)
    public static GlobField node;

    public static StringField cursor;

    static {
        GlobTypeLoaderFactory.create(HumanEdgeConnection.class, "HumanEdge").load();
    }

}
