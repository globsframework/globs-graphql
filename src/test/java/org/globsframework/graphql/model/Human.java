package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.KeyField;
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
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("FriendQueryParam");
            sort = builder.declareStringField("sort");
            name = builder.declareStringArrayField("name");
            TYPE = builder.build();
        }
    }

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Human");
        id = builder.declareStringField("id", KeyField.ZERO);
        surName = builder.declareStringField("surName");
        firstName = builder.declareStringField("firstName");
        lastName = builder.declareStringField("lastName");
        birthDate = builder.declareGlobField("birthDate", () -> BirthDate.TYPE);
        friends = builder.declareGlobArrayField("friends", () -> Human.TYPE, GQLQueryParam.create(FriendQueryParam.TYPE));
        TYPE = builder.build();
    }

    public static class BirthDate {
        public static GlobType TYPE;

        public static IntegerField day;

        public static IntegerField month;

        public static IntegerField year;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("BirthDate");
            day = builder.declareIntegerField("day");
            month = builder.declareIntegerField("month");
            year = builder.declareIntegerField("year");
            TYPE = builder.build();
        }
    }
}
