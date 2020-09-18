package org.judahdonkor.chassis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class encapsulates the serial details of and unchecked exception
 * {@link org.judahdonkor.chassis.Beef}.
 * 
 * 
 * @author judahdonkor
 * @since beef-1.0.0
 * @version 1.0.0
 */
@Getter
@Setter
@ToString
public class Error {
	private String context, title, detail, suggestion, reference, about;
	private Error cause;

	/**
	 * Create a new Builder {@link Builder}
	 * 
	 * @return Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new Builder {@link Builder} with supplied titile
	 * 
	 * @param title
	 * @return Builder
	 */
	public static Builder builder(String title) {
		return new Builder().title(title);
	}

	/**
	 * A fluent error {@link Error} builder
	 * 
	 * @author judahdonkor
	 * @since beef-1.0.0
	 * @version 1.0.0
	 */
	public static class Builder {
		private String context, title, detail, suggestion, reference, about;

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder detail(String detail) {
			this.detail = detail;
			return this;
		}

		public Builder when(String context) {
			this.context = context;
			return this;
		}

		public Builder may(String suggestion) {
			this.suggestion = suggestion;
			return this;
		}

		public Builder reference(String reference) {
			this.reference = reference;
			return this;
		}

		public Builder about(String about) {
			this.about = about;
			return this;
		}

		public Error build() {
			var e = new Error();
			e.setTitle(title);
			e.setDetail(detail);
			e.setContext(context);
			e.setSuggestion(suggestion);
			e.setReference(reference);
			e.setAbout(about);
			return e;
		}

		public Error build(Error cause) {
			var e = build();
			e.setCause(cause);
			return e;
		}
	}
}
