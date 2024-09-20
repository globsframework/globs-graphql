package org.globsframework.graphql;

import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;

import java.util.Optional;

public record QGLFieldWithParameter(Field field, Optional<Glob> parameters) {
}
