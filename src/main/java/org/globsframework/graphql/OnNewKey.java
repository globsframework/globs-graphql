package org.globsframework.graphql;

import org.globsframework.functional.FunctionalKey;

public interface OnNewKey {
    void push(FunctionalKey key);
}
