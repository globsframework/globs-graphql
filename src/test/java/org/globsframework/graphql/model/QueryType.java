package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;

public class QueryType {
    public static GlobType TYPE;

    @GQLQueryParam_(HumanQuery.class)
    @Target(Human.class)
    public static GlobField humain;

    @GQLQueryParam_(HumansQuery.class)
    @Target(HumanConnection.class)
    public static GlobField humains;

    @Target(Human.class)
    @GQLQueryParam_(CreateParam.class)
    public static GlobField createHumain;

    static {
        GlobTypeLoaderFactory.create(QueryType.class, "Query").load();
    }
}
