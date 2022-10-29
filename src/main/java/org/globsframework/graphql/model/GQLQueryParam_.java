package org.globsframework.graphql.model;

import org.globsframework.metamodel.GlobType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.FIELD})
public @interface GQLQueryParam_ {
    Class value();

    GlobType TYPE = GQLQueryParam.TYPE;
}
