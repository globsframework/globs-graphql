package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.KeyField_;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.*;

public class Human {
    public static GlobType TYPE;

    @KeyField_
    public static StringField id;

    public static StringField surName;

    public static StringField firstName;

    public static StringField lastName;

    // try to add a loader that can execute a query only for this attribut
//    public static BooleanField hasFriend;

    @Target(BirthDate.class)
    public static GlobField birthDate;

    @Target(Human.class)
    @GQLQueryParam_(FriendQueryParam.class)
    public static GlobArrayField friends;

    public static class FriendQueryParam {
        public static GlobType TYPE;

        public static StringField sort;

        public static StringArrayField name;

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

        static {
            GlobTypeLoaderFactory.create(BirthDate.class).load();
        }
    }
}
