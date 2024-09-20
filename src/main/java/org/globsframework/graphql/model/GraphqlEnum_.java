package org.globsframework.graphql.model;

import org.globsframework.core.metamodel.GlobType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface GraphqlEnum_ {
    GlobType TYPE = GraphqlEnum.TYPE;

    String name();

    String[] values() default {};

}
