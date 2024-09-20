package org.globsframework.graphql;

import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CallContext {
    public Map<Key, CompletableFuture<Glob>> queriesData = new ConcurrentHashMap<>();
}
