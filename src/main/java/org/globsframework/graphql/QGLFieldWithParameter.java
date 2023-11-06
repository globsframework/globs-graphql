package org.globsframework.graphql;

import org.globsframework.metamodel.fields.Field;
import org.globsframework.model.Glob;

import java.util.Optional;

public record QGLFieldWithParameter(Field field, Optional<Glob> parameters) {
}
