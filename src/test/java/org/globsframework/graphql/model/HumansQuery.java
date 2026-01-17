package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.KeyField;
import org.globsframework.core.metamodel.annotations.KeyField_;
import org.globsframework.core.metamodel.fields.DateTimeField;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringField;

public class HumansQuery {
    public static GlobType TYPE;

    @KeyField_
    public static IntegerField first;

    public static StringField after;

    public static StringField orderBy;

    public static StringField order;

    public static DateTimeField startedAt;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("HumansQuery");
        first = builder.declareIntegerField("first", KeyField.ZERO);
        after = builder.declareStringField("after");
        orderBy = builder.declareStringField("orderBy");
        order = builder.declareStringField("order");
        startedAt = builder.declareDateTimeField("startedAt");
        TYPE = builder.build();
    }
}
