package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.KeyField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;

public class Human {
    public static GlobType TYPE;

    @KeyField
    public static StringField id;

    public static StringField surName;

    public static StringField firstName;

    public static StringField lastName;

    @Target(BirthDate.class)
    public static GlobField birthDate;

    @Target(Human.class)
    @GQLQueryParam_(FriendQueryParam.class)
    public static GlobArrayField friends;

    public static class FriendQueryParam {
        public static GlobType TYPE;

        public static StringField sort;

        static {
            GlobTypeLoaderFactory.create(FriendQueryParam.class).load();
        }
    }

    static {
        GlobTypeLoaderFactory.create(Human.class).load();
    }

    public static class BirthDate {
        public static GlobType TYPE;

        public static IntegerField day;

        public static IntegerField month;

        public static IntegerField year;

        static  {
            GlobTypeLoaderFactory.create(BirthDate.class).load();
        }
    }
}
