package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;

public class QueryType {
    public static GlobType TYPE;

    @GQLQueryParam_(HumanQuery.class)
    @Target(Human.class)
    public static GlobField humain;

    @GQLQueryParam_(HumansQuery.class)
    @Target(HumanConnection.class)
    public static GlobField humains;


    @GQLQueryParam_(ComplexHumansQuery.class)
    @Target(HumanConnection.class)
    public static GlobField complexHumains;


    static {
        GlobTypeLoaderFactory.create(QueryType.class, "Query").load();
    }
}
