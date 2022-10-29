package org.globsframework.graphql.parser;

import org.globsframework.graphql.GQLGlobType;
import org.globsframework.graphql.QGLFieldWithParameter;

public record GqlField(QGLFieldWithParameter field, GQLGlobType gqlGlobType) {
}
