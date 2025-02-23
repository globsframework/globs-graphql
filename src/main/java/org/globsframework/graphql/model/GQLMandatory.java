package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueGlob;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class GQLMandatory {
    public static final GlobType TYPE;

    @InitUniqueKey
    public static final Key KEY;

    @InitUniqueGlob
    public static final Glob UNIQUE;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("GQLMandatory");
        TYPE = typeBuilder.unCompleteType();
        typeBuilder.complete();
        KEY = KeyBuilder.newEmptyKey(TYPE);
        UNIQUE = TYPE.instantiate();
        typeBuilder.register(GlobCreateFromAnnotation.class, annotation -> UNIQUE);
//        GlobTypeLoaderFactory.create(GQLMandatory.class, "GQLMandatory")
//                .register(GlobCreateFromAnnotation.class, annotation -> UNIQUE)
//                .load();
    }
}
