package utils;

import exception.ParseException;
import http.ContentType;

public final class ContentTypeUtils {

	public static void ensureContentType(final ContentType expected, final ContentType found) throws ParseException {
		ensureContentType(expected, null, found);
	}

	public static void ensureContentType(final ContentType expected, final String subTypeSuffix,
			final ContentType found) throws ParseException {

		if (found == null)
			throw new ParseException("Missing HTTP Content-Type header");

		if (expected.matches(found))
			return;

		if (expected.getBaseType().equals(found.getBaseType()) && found.hasSubTypeSuffix(subTypeSuffix))
			return;

		if (subTypeSuffix == null) {
			throw new ParseException(
					"The HTTP Content-Type header must be " + expected.getType() + ", recieved " + found.getType());
		} else {
			throw new ParseException("The HTTP Content-Type header must be " + expected.getType() + " or have the +"
					+ subTypeSuffix + " suffix, " + "received " + found.getType());
		}
	}

	private ContentTypeUtils() {
	}
}
