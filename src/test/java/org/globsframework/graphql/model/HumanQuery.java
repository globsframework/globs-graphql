package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class HumanQuery {
    public static GlobType TYPE;

    public static StringField id;

    static {
        GlobTypeLoaderFactory.create(HumanQuery.class, "Humain").load();
    }
}
