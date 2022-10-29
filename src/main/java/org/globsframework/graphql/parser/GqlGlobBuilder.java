package org.globsframework.graphql.parser;

import org.globsframework.model.MutableGlob;

public interface GqlGlobBuilder {

    void complete();

    MutableGlob getArguments();

    GqlGlobBuilder getSubBuilder();

    GqlGlobBuilder addSub(String fieldName, String alias);
}
