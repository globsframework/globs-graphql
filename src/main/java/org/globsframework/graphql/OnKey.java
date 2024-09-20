package org.globsframework.graphql;

import org.globsframework.core.functional.FunctionalKey;

public record OnKey(FunctionalKey key, OnNewData onNew) {
}
