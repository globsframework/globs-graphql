package org.globsframework.graphql;

import org.globsframework.functional.FunctionalKey;

public record OnKey(FunctionalKey key, OnNewData onNew) {
}
