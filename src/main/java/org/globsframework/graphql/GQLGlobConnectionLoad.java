package org.globsframework.graphql;

import org.globsframework.core.model.Glob;
import org.globsframework.graphql.parser.GqlField;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GQLGlobConnectionLoad<C extends GQLGlobCaller.GQLContext> {
    CompletableFuture<Void> load(GqlField gqlField, C callContext, List<OnConnectionLoad> parents);

    interface OnCursor {
        void push(CursorInfo cursorInfo);
    }

    record OnConnectionLoad(Glob parent, OnNewData onNew, OnCursor onCursor) {
    }

    record CursorInfo(boolean hasPrevious, boolean hasNext, int totalCount) {
    }
}
