package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class HumanQuery {
    public static GlobType TYPE;

    public static StringField id;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Humain");
        id = builder.declareStringField("id");
        TYPE = builder.build();
    }
}
