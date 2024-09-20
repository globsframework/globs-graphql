package org.globsframework.graphql.parser;

import org.globsframework.core.model.MutableGlob;

public class GqlGlobBuilderWithError implements GqlGlobBuilder {

    public void complete() {
        throw new RuntimeException("NI " + this.getClass());
    }

    public MutableGlob getArguments() {
        throw new RuntimeException("NI " + this.getClass());
    }


    public GqlGlobBuilder getSubBuilder() {
        throw new RuntimeException("NI " + this.getClass());
    }

    public GqlGlobBuilder addSub(String fieldName, String alias) {
        throw new RuntimeException("NI " + fieldName + " " + alias + this.getClass());
    }
}
