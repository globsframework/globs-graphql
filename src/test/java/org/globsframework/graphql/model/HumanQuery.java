package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.StringField;

public class HumanQuery {
    public static GlobType TYPE;

    public static StringField id;

    static {
        GlobTypeLoaderFactory.create(HumanQuery.class, "Humain").load();
    }
}
