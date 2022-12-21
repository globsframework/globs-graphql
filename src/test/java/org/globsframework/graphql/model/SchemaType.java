package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;

public class SchemaType {
    public static GlobType TYPE;

    @Target(QueryType.class)
    public static GlobField query;

    @Target(QueryMutation.class)
    public static GlobField mutation;

    static {
        GlobTypeLoaderFactory.create(SchemaType.class).load();
    }
}
