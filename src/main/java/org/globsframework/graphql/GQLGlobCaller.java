package org.globsframework.graphql;

import org.globsframework.core.model.Glob;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface GQLGlobCaller<C extends GQLGlobCaller.GQLContext> {
    CompletableFuture<Glob> query(String query, Map<String, String> variables, C gqlContext);

    interface GQLContext {
    }

}
