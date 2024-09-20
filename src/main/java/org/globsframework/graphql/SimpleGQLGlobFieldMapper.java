package org.globsframework.graphql;

import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.FieldSetter;
import org.globsframework.core.model.Glob;

public class SimpleGQLGlobFieldMapper implements GQLGlobFieldMapper {
    final Field sourceField;
    final Field targetField;

    public SimpleGQLGlobFieldMapper(Field sourceField, Field targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    public void update(Glob source, FieldSetter<?> target) {
        target.setValue(targetField, source.getValue(sourceField));
    }
}
