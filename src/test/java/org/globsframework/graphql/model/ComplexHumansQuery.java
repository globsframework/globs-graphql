package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;

public class ComplexHumansQuery {
    public static GlobType TYPE;

    public static StringField who;

    @Target(Subinfo.class)
    public static GlobField subInfo;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("ComplexHumansQuery");
        who = builder.declareStringField("who");
        subInfo = builder.declareGlobField("subInfo", () -> Subinfo.TYPE);
        TYPE = builder.build();
    }

    public static class Subinfo {
        public static GlobType TYPE;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Subinfo");
            firstName = builder.declareStringField("firstName");
            lastName = builder.declareStringField("lastName");
            TYPE = builder.build();
        }
    }
}
