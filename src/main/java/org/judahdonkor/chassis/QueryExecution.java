package org.judahdonkor.chassis;

import javax.persistence.TypedQuery;

@FunctionalInterface
public interface QueryExecution<T> {
    void control(TypedQuery<T> typedQuery);
}
