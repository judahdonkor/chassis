package org.judahdonkor.chassis;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Jotter
 * 
 */
public interface Configuration {

    void remove(String key, Object context);

    default void remove(String key) {
        remove(key, null);
    }

    default void put(String key, String value) {
        put(key, value, null);
    }

    void put(String key, String value, Object context);

    default void put(String key, Object value) {
        put(key, value, null);
    }

    void put(String key, Object value, Object context);

    default Optional<String> get(String key) {
        return get(key, null);
    }

    Optional<String> get(String key, Object context);

    default <T> Optional<T> get(String key, Type cls) {
        return get(key, cls, null);
    }

    <T> Optional<T> get(String key, Type cls, Object context);

    default <T> Optional<T> get(String key, Class<T> cls) {
        return get(key, cls, null);
    }

    <T> Optional<T> get(String key, Class<T> cls, Object context);

    default boolean contains(String key) {
        return get(key, null).isPresent();
    }

    default boolean contains(String key, Object context) {
        return get(key, context).isPresent();
    }

}