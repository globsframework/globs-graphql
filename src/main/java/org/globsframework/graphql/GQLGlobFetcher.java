package org.globsframework.graphql;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GQLGlobFetcher<C extends GQLGlobCaller.GQLContext> {
    CompletableFuture<Void> load(GQLGlobType gqlGlobType, C callContext, List<OnKey> parents);
}
