package org.globsframework.graphql;

import org.globsframework.graphql.parser.GqlField;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GQLKeyExtractor<C extends GQLGlobCaller.GQLContext> {
    CompletableFuture<Void> extract(GqlField gqlField, C callContext, List<OnExtract> parents);
}
