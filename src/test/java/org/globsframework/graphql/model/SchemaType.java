package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;

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
