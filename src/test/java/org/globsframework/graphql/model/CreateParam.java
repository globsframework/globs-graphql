package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.StringField;

public class CreateParam {
    public static GlobType TYPE;

    @Target(HumanInput.class)
    public static GlobField humain;

    static {
        GlobTypeLoaderFactory.create(CreateParam.class).load();
    }

    public static class HumanInput {
        public static GlobType TYPE;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeLoaderFactory.create(HumanInput.class).load();
        }
    }

}
