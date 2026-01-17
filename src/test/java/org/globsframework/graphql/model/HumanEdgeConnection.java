package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;

public class HumanEdgeConnection {
    public static GlobType TYPE;

    @Target(Human.class)
    public static GlobField node;

    public static StringField cursor;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("HumanEdge");
        node = builder.declareGlobField("node", () -> Human.TYPE);
        cursor = builder.declareStringField("cursor");
        TYPE = builder.build();
    }

}
