package org.globsframework.graphql;

import org.globsframework.core.model.FieldSetter;
import org.globsframework.core.model.Glob;

public interface GQLGlobFieldMapper {
    void update(Glob source, FieldSetter<?> target);
}
