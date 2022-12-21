package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobField;

public class QueryMutation {
    public static GlobType TYPE;


    @Target(Human.class)
    @GQLQueryParam_(CreateParam.class)
    public static GlobField createHumain;

    static {
        GlobTypeLoaderFactory.create(QueryMutation.class, "Mutation").load();
    }
}
