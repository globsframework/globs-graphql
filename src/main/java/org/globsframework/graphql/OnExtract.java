package org.globsframework.graphql;

import org.globsframework.model.Glob;

public record OnExtract(Glob parent, OnNewKey onNew) {
}
