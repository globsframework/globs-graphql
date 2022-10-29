package org.globsframework.graphql;

import org.globsframework.graphql.parser.GqlField;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;

import java.util.Map;

public class GQLGlobType {
    public final GlobType type;
    public final GlobType outputType;
    public final Map<Field, GqlField> aliasToField;

    public GQLGlobType(GlobType type, GlobType outputType, Map<Field, GqlField> aliasToField) {
        this.type = type;
        this.outputType = outputType;
        this.aliasToField = aliasToField;
    }
}
