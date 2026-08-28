package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.GlobField;

public class QueryType {
    public static GlobType TYPE;

    public static GlobField<Human> humain;

    public static GlobField<HumanConnection> humains;


    public static GlobField<HumanConnection> complexHumains;


    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Query");
        humain = builder.declareGlobField("humain", () -> Human.TYPE, GQLQueryParam.create(HumanQuery.TYPE));
        humains = builder.declareGlobField("humains", () -> HumanConnection.TYPE, GQLQueryParam.create(HumansQuery.TYPE));
        complexHumains = builder.declareGlobField("complexHumains", () -> HumanConnection.TYPE, GQLQueryParam.create(ComplexHumansQuery.TYPE));
        TYPE = builder.build();
    }
}
