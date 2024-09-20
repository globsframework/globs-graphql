package org.globsframework.graphql;

import org.globsframework.core.model.Glob;

public record OnLoad(Glob parent, OnNewData onNew) {
}
