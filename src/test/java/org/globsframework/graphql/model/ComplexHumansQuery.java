package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;

public class ComplexHumansQuery {
    public static GlobType TYPE;

    public static StringField who;

    @Target(Subinfo.class)
    public static GlobField subInfo;

    static {
        GlobTypeLoaderFactory.create(ComplexHumansQuery.class, "ComplexHumansQuery").load();
    }

    public static class Subinfo {
        public static GlobType TYPE;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeLoaderFactory.create(Subinfo.class).load();
        }
    }
}
