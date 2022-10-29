package org.globsframework.graphql;

import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;

import java.util.Optional;

public record QGLFieldWithParameter(Field field, Optional<Glob> parameters) {
}
