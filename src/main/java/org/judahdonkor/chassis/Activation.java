package org.judahdonkor.chassis;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Target({ METHOD, FIELD, PARAMETER, TYPE })
@Retention(RUNTIME)
public @interface Activation {
    Type value() default Type.ACTIVATE;

    public static enum Type {
        ACTIVATE, DEACTIVATE, ACTIVATED, DEACTIVATED;
    }

    public static class Literal extends AnnotationLiteral<Activation> implements Activation {
        /**
         *
         */
        private static final long serialVersionUID = 1897408086436213407L;

        private final Type type;

        public Literal(Type type) {
            this.type = type;
        }

        @Override
        public Type value() {
            return type;
        }

    }
}