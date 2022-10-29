package org.globsframework.graphql;

import org.globsframework.model.FieldSetter;
import org.globsframework.model.Glob;

public interface GQLGlobFieldMapper {
    void update(Glob source, FieldSetter<?> target);
}
