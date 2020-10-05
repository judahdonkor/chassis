package org.judahdonkor.chassis;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Jotter
 * 
 */
public interface Configuration {

    <T> void remove(String key, T context);

    default void remove(String key) {
        remove(key, null);
    }

    default void put(String key, String value) {
        put(key, value, null);
    }

    <T> void put(String key, String value, T context);

    default void put(String key, Object value) {
        put(key, value, null);
    }

    <T> void put(String key, Object value, T context);

    default Optional<String> get(String key) {
        return get(key, null);
    }

    <T> Optional<String> get(String key, T context);

    default <T> Optional<T> get(String key, Type cls) {
        return get(key, cls, null);
    }

    <T> Optional<T> get(String key, Type cls, T context);

    default <T> Optional<T> get(String key, Class<T> cls) {
        return get(key, cls, null);
    }

    <T> Optional<T> get(String key, Class<T> cls, T context);

    default boolean contains(String key) {
        return get(key, null).isPresent();
    }

}