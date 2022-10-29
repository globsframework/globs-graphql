package org.globsframework.graphql;

import org.globsframework.model.Glob;

public record OnLoad(Glob parent, OnNewData onNew) {
}
