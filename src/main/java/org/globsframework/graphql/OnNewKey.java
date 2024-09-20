package org.globsframework.graphql;

import org.globsframework.core.functional.FunctionalKey;

public interface OnNewKey {
    void push(FunctionalKey key);
}
