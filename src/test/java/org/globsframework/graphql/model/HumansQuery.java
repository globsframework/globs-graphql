package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.KeyField;
import org.globsframework.metamodel.fields.DateTimeField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;

public class HumansQuery {
    public static GlobType TYPE;

    @KeyField
    public static IntegerField first;

    public static StringField after;

    public static StringField orderBy;

    public static StringField order;

    public static DateTimeField startedAt;

    static {
        GlobTypeLoaderFactory.create(HumansQuery.class).load();
    }
}
