package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobModel;
import org.globsframework.core.metamodel.GlobModelBuilder;

public class AllGraphQLAnnotations {
    public static GlobModel INSTANCE = GlobModelBuilder.create(GQLQueryParam.TYPE, GQLMandatory.TYPE, GraphqlEnum.TYPE).get();
}
