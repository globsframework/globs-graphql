package org.globsframework.graphql;

import org.globsframework.core.model.Glob;

public record OnExtract(Glob parent, OnNewKey onNew) {
}
