/**
 * 
 */
package org.judahdonkor.chassis;

import java.util.Objects;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

/**
 * @author Jotter
 *
 */
public interface Beef {
	Error error();

	default Response.Status httpStatus() {
		return Response.Status.INTERNAL_SERVER_ERROR;
	}

	default Response.ResponseBuilder response() {
		return Response.status(httpStatus()).entity(error());
	}

	public static <T extends UncheckedException> Builder<T> of(Class<T> cls) {
		return new Builder<>(cls);
	}

	public static Builder<UncheckedException> of() {
		return new Builder<>(UncheckedException.class);
	}

	public static Builder<UncheckedException> of(Throwable cause) {
		Objects.requireNonNull(cause, "Cause cannot be null");
		return Beef.of().following(cause);
	}

	public static RuntimeException uncheck(Exception cause) {
		Objects.requireNonNull(cause, "Cause cannot be null");
		if (RuntimeException.class.isAssignableFrom(cause.getClass()))
			return (RuntimeException) cause;
		return of(cause).build();
	}

	public static Builder<ConfigException> config() {
		return new Builder<>(ConfigException.class);
	}

	public static Builder<InternalException> internal() {
		return new Builder<>(InternalException.class);
	}

	public static Builder<UserException> user() {
		return new Builder<>(UserException.class);
	}

	public static Builder<ValidationException> validation() {
		return new Builder<>(ValidationException.class);
	}

	public static Beef wrap(Throwable throwable) {
		Objects.requireNonNull(throwable, "Throwable cannot be null");
		if (Beef.class.isAssignableFrom(throwable.getClass()))
			return Beef.class.cast(throwable);
		return new Beef() {
			@Override
			public Error error() {
				var error = Error.builder().title(throwable.getClass().getName())
						.detail(throwable.getLocalizedMessage());
				return error.build(throwable.getCause() != null ? Beef.wrap(throwable.getCause()).error() : null);
			}
		};
	}

	public static class UncheckedException extends RuntimeException implements Beef {
		private static final long serialVersionUID = -8576854518162184976L;
		private final Error.Builder error;

		public UncheckedException(Error.Builder error, Throwable cause) {
			super(cause);
			this.error = error != null ? error : Error.builder();
		}

		public UncheckedException(Error.Builder error) {
			this(error, null);
		}

		public UncheckedException(Throwable cause) {
			this(Error.builder(), cause);
		}

		public UncheckedException() {
			this(null, null);
		}

		@Override
		public final Error error() {
			return error.build(getCause() != null ? Beef.wrap(getCause()).error() : null);
		}

		@Override
		public String toString() {
			return error().toString();
		}
	}

	public static class Builder<T extends UncheckedException> {
		private final Class<T> cls;
		private Error.Builder error;
		private Throwable cause;

		private Builder(Class<T> cls) {
			Objects.requireNonNull(cls, "Class cannot be null");
			this.cls = cls;
			this.error = Error.builder();
		}

		public Builder<T> following(Throwable cause) {
			if (this.cause != null)
				throw Beef.of().as(e -> e.title("Cause of error already known")).build();
			this.cause = cause;
			return this;
		}

		public Builder<T> as(Consumer<Error.Builder> description) {
			description.accept(error);
			return this;
		}

		public T build() {
			if (cls == UncheckedException.class)
				return cls.cast(new UncheckedException(error, cause));
			if (cls == UserException.class)
				return cls.cast(new UserException(error, cause));
			if (cls == ValidationException.class)
				return cls.cast(new ValidationException(error, cause));
			if (cls == InternalException.class)
				return cls.cast(new InternalException(error, cause));
			if (cls == ConfigException.class)
				return cls.cast(new ConfigException(error, cause));
			try {
				return cls.getDeclaredConstructor(Error.Builder.class, Throwable.class).newInstance(error, cause);
			} catch (Exception e) {
				throw Beef.uncheck(e);
			}
		}
	}
}
