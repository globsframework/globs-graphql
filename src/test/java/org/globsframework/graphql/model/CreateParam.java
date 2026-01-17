package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;

public class CreateParam {
    public static GlobType TYPE;

    @Target(HumanInput.class)
    public static GlobField humain;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("CreateParam");
        humain = builder.declareGlobField("humain", () -> HumanInput.TYPE);
        TYPE = builder.build();
    }

    public static class HumanInput {
        public static GlobType TYPE;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("HumanInput");
            firstName = builder.declareStringField("firstName");
            lastName = builder.declareStringField("lastName");
            TYPE = builder.build();
        }
    }
}
