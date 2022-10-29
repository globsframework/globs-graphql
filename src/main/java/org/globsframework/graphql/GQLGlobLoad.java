package org.globsframework.graphql;

import org.globsframework.graphql.parser.GqlField;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GQLGlobLoad<C extends GQLGlobCaller.GQLContext> {
    GQLGlobLoad identity = (GQLGlobLoad<GQLGlobCaller.GQLContext>) (gqlField, callContext, parents) -> {
        parents.forEach(parent -> parent.onNew().push(parent.parent()));
        return CompletableFuture.completedFuture(null);
    };
    CompletableFuture<Object> load(GqlField gqlField, C callContext, List<OnLoad> parents);

    record CursorInfo(boolean hasPrevious, boolean hasNext) {
    }
}
