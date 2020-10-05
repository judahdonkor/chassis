package org.judahdonkor.chassis;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface Creation {
    <T> void persist(T entity);

    <T> List<T> list(Class<T> cls, String... keys);

    <T> Optional<T> find(Class<T> cls, String key);

    default <T> T entity(Class<T> cls, String key, Supplier<T> supplier) {
        return find(cls, key).orElseGet(() -> {
            var entity = supplier.get();
            persist(entity);
            return entity;
        });
    }

    default <T> List<T> entities(Class<T> cls, String... keys) {
        return list(cls, keys);
    }

    Repository.Factory reposFcty();

    <T> Discrimination<T> of(T discriminator);

    public static interface Discrimination<T> {
        <U extends Entity<T>> void persist(U entity);

        <U extends Entity<T>> List<U> list(Class<U> cls, String... keys);

        <U extends Entity<T>> Optional<U> find(Class<U> cls, String key);

        default <U extends Entity<T>> U entity(Class<U> cls, String key, Supplier<U> supplier) {
            return find(cls, key).orElseGet(() -> {
                var entity = supplier.get();
                persist(entity);
                return entity;
            });
        }

        default <U extends Entity<T>> List<U> entities(Class<U> cls, String... keys) {
            return list(cls, keys);
        }

        T disc();

        Creation creation();

        public static interface Entity<T> {
            void discriminate(T entity);
        }
    }

}