package utils;

public class StringUtils {

	public static boolean isBlank(final CharSequence cs) {
		if (cs != null) {
			int strLen = cs.length();
			if (strLen != 0) {
				for (int i = 0; i < strLen; i++) {
					if (!Character.isWhitespace(cs.charAt(i)))
						return false;
				}
			}
		}
		return true;
	}

	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	/**
	 * Returns true if char sequence is all alphabetic letters
	 * 
	 * @return {@code true} if char sequence is all alphabetic letters, empty or
	 *         {@code null}, else {@code false}.
	 */
	public static boolean isAlpha(final CharSequence cs) {
		int strLen;
		if (cs != null && (strLen = cs.length()) != 0) {
			for (int i = 0; i < strLen; i++) {
				if (!Character.isAlphabetic(cs.charAt(i)))
					return false;

			}
		}
		return true;
	}

	public static boolean isNumeric(final CharSequence cs) {
		int strLen;
		if (cs != null && (strLen = cs.length()) != 0) {
			for (int i = 0; i < strLen; i++) {
				if (!Character.isDigit(cs.charAt(i)))
					return false;
			}
		}
		return true;
	}

	// prevent public instantiation
	private StringUtils() {
	};
}
