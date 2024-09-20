package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;

public class QueryMutation {
    public static GlobType TYPE;


    @Target(Human.class)
    @GQLQueryParam_(CreateParam.class)
    public static GlobField createHumain;

    static {
        GlobTypeLoaderFactory.create(QueryMutation.class, "Mutation").load();
    }
}
