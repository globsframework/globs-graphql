package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.GlobField;

public class SchemaType {
    public static GlobType TYPE;

    public static GlobField<QueryType> query;

    public static GlobField<QueryMutation> mutation;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Schema");
        query = builder.declareGlobField("query", () -> QueryType.TYPE);
        mutation = builder.declareGlobField("mutation", () -> QueryMutation.TYPE);
        TYPE = builder.build();
    }
}
