package org.globsframework.graphql.parser;

import org.globsframework.core.model.MutableGlob;

public interface GqlGlobBuilder {

    void complete();

    MutableGlob getArguments();

    GqlGlobBuilder getSubBuilder();

    GqlGlobBuilder addSub(String fieldName, String alias);
}
