package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobField;

public class QueryMutation {
    public static GlobType TYPE;


    @Target(Human.class)
    @GQLQueryParam_(CreateParam.class)
    public static GlobField createHumain;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Mutation");
        createHumain = builder.declareGlobField("createHumain", () -> Human.TYPE, GQLQueryParam.create(CreateParam.TYPE));
        TYPE = builder.build();
    }
}
