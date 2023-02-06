package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.StringField;

public class ComplexHumansQuery {
    public static GlobType TYPE;

    @Target(Subinfo.class)
    public static GlobField subInfo;

    static {
        GlobTypeLoaderFactory.create(ComplexHumansQuery.class, "ComplexHumansQuery").load();
    }

    public static class Subinfo{
        public static GlobType TYPE;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeLoaderFactory.create(Subinfo.class).load();
        }
    }
}
