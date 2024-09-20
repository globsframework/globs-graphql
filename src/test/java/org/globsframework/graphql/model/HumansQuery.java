package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.KeyField;
import org.globsframework.core.metamodel.fields.DateTimeField;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringField;

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
