package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;

public class HumanEdgeConnection {
    public static GlobType TYPE;

    @Target(Human.class)
    public static GlobField node;

    public static StringField cursor;

    static {
        GlobTypeLoaderFactory.create(HumanEdgeConnection.class, "HumanEdge").load();
    }

}
