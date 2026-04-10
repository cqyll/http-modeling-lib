package http;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Helper class that models an HTTP content type (instead of treating them as
 * plain strings)
 */
public final class ContentType {

	/**
	 * Optional content type parameter, e.g. {@code charset=UTF-8}.
	 */
	public static final class Parameter {

		/**
		 * A {@code charset=UTF-8} parameter.
		 */
		public static final Parameter CHARSET_UTF_8 = new Parameter("charset", "UTF-8");

		/**
		 * parameter name.
		 */
		private final String name;

		/**
		 * parameter value.
		 */
		private final String value;

		/**
		 * Creates a new content type parameter.
		 * 
		 * @param name  The name. Must not be {@code null} or empty.
		 * @param value The value. Must not be {@code null} or empty.
		 */
		public Parameter(final String name, final String value) {
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("The parameter name must be specified");
			}

			this.name = name;

			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException("The parameter value must be specified");
			}
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name + "=" + value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Parameter))
				return false;
			Parameter other = (Parameter) obj;
			return getName().equalsIgnoreCase(other.getName()) && getValue().equalsIgnoreCase(other.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getName().toLowerCase(), getValue().toLowerCase());
		}
	}

	/**
	 * Content type {@code application/json; charset=UTF-8}.
	 */
	public static final ContentType APPLICATION_JSON = new ContentType("application", "json", Parameter.CHARSET_UTF_8);

	/**
	 * Content type {@code application/jose; charset=UTF-8}.
	 */
	public static final ContentType APPLICATION_JOSE = new ContentType("application", "jose", Parameter.CHARSET_UTF_8);

	/**
	 * Content type {@code application/jwt; charset=UTF-8}.
	 */
	public static final ContentType APPLICATION_JWT = new ContentType("application", "jwt", Parameter.CHARSET_UTF_8);

	/**
	 * Content type {@code application/x-www-form-urlencoded; charset=UTF-8}.
	 */
	public static final ContentType APPLICATION_URLENCODED = new ContentType("application", "x-www-form-urlencoded",
			Parameter.CHARSET_UTF_8);

	/**
	 * Content type {@code text/plain; charset=UTF-8}.
	 */
	public static final ContentType TEXT_PLAIN = new ContentType("text", "plain", Parameter.CHARSET_UTF_8);

	/**
	 * Content type {@code image/apng}.
	 */
	public static final ContentType IMAGE_APNG = new ContentType("image", "apng");

	/**
	 * Content type {@code image/avif}.
	 */
	public static final ContentType IMAGE_AVIF = new ContentType("image", "avif");

	/**
	 * Content type {@code image/gif}.
	 */
	public static final ContentType IMAGE_GIF = new ContentType("image", "gif");

	/**
	 * Content type {@code image/jpeg}.
	 */
	public static final ContentType IMAGE_JPEG = new ContentType("image", "jpeg");

	/**
	 * Content type {@code image/png}.
	 */
	public static final ContentType IMAGE_PNG = new ContentType("image", "png");

	/**
	 * Content type {@code image/svg+xml}.
	 */
	public static final ContentType IMAGE_SVG_XML = new ContentType("image", "svg+xml");

	/**
	 * Content type {@code image/webp}.
	 */
	public static final ContentType IMAGE_WEBP = new ContentType("image", "webp");

	/**
	 * Content type {@code application/pdf}.
	 */
	public static final ContentType APPLICATION_PDF = new ContentType("application", "pdf");

	private final String baseType;
	private final String subType;
	private final List<Parameter> params;

	/**
	 * Creates a new content type.
	 *
	 * @param baseType The type. E.g. "application" from "application/json".Must not
	 *                 be {@code null} or empty.
	 * @param subType  The subtype. E.g. "json" from "application/json". Must not be
	 *                 {@code null} or empty.
	 * @param param    Optional parameters.
	 */
	public ContentType(final String baseType, final String subType, final Parameter... param) {

		if (baseType == null || baseType.trim().isEmpty()) {
			throw new IllegalArgumentException("The base type must be specified");
		}
		this.baseType = baseType;

		if (subType == null || subType.trim().isEmpty()) {
			throw new IllegalArgumentException("The subtype must be specified");
		}
		this.subType = subType;

		if (param != null && param.length > 0) {
			params = Collections.unmodifiableList(Arrays.asList(param));
		} else {
			params = Collections.emptyList();
		}
	}

	/**
	 * Creates a new content type with the specified character set.
	 *
	 * @param baseType The base type. E.g. "application" from
	 *                 "application/json".Must not be {@code null} or empty.
	 * @param subType  The subtype. E.g. "json" from "application/json". Must not be
	 *                 {@code null} or empty.
	 * @param charset  The character set to use for the {@code charset} parameter.
	 *                 Must not be {@code null}.
	 */
	public ContentType(final String baseType, final String subtype, final Charset charset) {
		this(baseType, subtype, new Parameter("charset", charset.toString()));
	}

	public String getBaseType() {
		return baseType;
	}

	public String getSubType() {
		return subType;
	}

	/**
	 * Returns the base sub type. e.g. "entity-statement" from
	 * "application/entity-statement+jwt"
	 * 
	 * @return The base sub type or the sub type if a suffix is not present.
	 */
	public String getBaseSubType() {

		Map.Entry<String, String> subtypeEn = splitSubtype();
		if (subtypeEn != null) {
			return subtypeEn.getKey();
		}
		return getSubType();
	}

	/**
	 * Returns the sub type suffix. e.g. "jwt" from
	 * "application/entity-statement+jwt"
	 * 
	 * @return The sub type suffix, {@code null} none.
	 */
	public String getSubTypeSuffix() {

		Map.Entry<String, String> subtypeEn = splitSubtype();
		if (subtypeEn != null) {
			return subtypeEn.getValue();
		}
		return null;
	}

	/**
	 * Returns {@code true} if this content type has the specified sub type suffix.
	 * 
	 * @param suffix The sub type suffix, {@code null} if not specified.
	 * 
	 * @return {@code true} if the sub type has the specified suffix, else
	 *         {@code false}.
	 */
	public boolean hasSubTypeSuffix(final String suffix) {
		return suffix != null && suffix.equals(getSubTypeSuffix());
	}

	public List<Parameter> getParameters() {
		return params;
	}

	/**
	 * Returns the type. e.g. "application/json"
	 */
	public String getType() {

		StringBuilder sb = new StringBuilder();
		sb.append(getBaseType());
		sb.append("/");
		sb.append(getSubType());
		return sb.toString();
	}
	

	public boolean matches(final ContentType other) {
		return other != null
				&& getBaseType().equalsIgnoreCase(other.getBaseType())
				&& getSubType().equalsIgnoreCase(other.getSubType());
		
	}
	
	// helpers

	private Map.Entry<String, String> splitSubtype() {

		String[] split = getSubType().split("\\+");
		if (split.length == 2) {
			return new AbstractMap.SimpleEntry<>(split[0], split[1]);
		}
		return null;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ContentType))
			return false;
		ContentType other = (ContentType) obj;
		return getBaseType().equalsIgnoreCase(other.getBaseType()) && getSubType().equalsIgnoreCase(other.getSubType())
				&& params.equals(other.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBaseType().toLowerCase(), getSubType().toLowerCase(), params);
	}

	/**
	 * Parses a content type from the specified string.
	 * 
	 * @param s The string to parse.
	 * 
 	 * @return The content type.
 	 * 
 	 * @throws ParseException If parsing failed or the string is {@code null} or empty.
	 */
	public static ContentType parse(final String s) throws ParseException {
		if (s == null || s.trim().isEmpty()) {
			throw new ParseException("Null or empty content type string", 0);
		}
		
		StringTokenizer st = new StringTokenizer(s, "/");
		
		if (!st.hasMoreTokens()) {
			throw new ParseException("Invalid content type string", 0);
		}
		
		String type = st.nextToken().trim();
		
		if (type.trim().isEmpty()) {
			throw new ParseException("Invalid content type string", 0);
		}
		
		if (!st.hasMoreTokens()) {
			throw new ParseException("Invalid content type string", 0);
		}
		
		String subtypeWithOptParams = st.nextToken().trim();
		
		st = new StringTokenizer(subtypeWithOptParams, ";");
		
		if (!st.hasMoreTokens()) {
			// no params
			return new ContentType(type, subtypeWithOptParams.trim());				
		}
		
		String subtype = st.nextToken().trim();
		
		if (!st.hasMoreTokens()) {
			// no params
			return new ContentType(type, subtype);
		}
		
		List<Parameter> params = new LinkedList<>();
		
		while (st.hasMoreTokens()) {
			String paramToken = st.nextToken().trim();
			
			StringTokenizer paramTokenizer = new StringTokenizer(paramToken, "=");
			
			if (!paramTokenizer.hasMoreTokens()) {
				throw new ParseException("Invalid parameter", 0);
			}
			
			String paramName = paramTokenizer.nextToken().trim();
			
			if (!paramTokenizer.hasMoreTokens()) {
				throw new ParseException("Invalid parameter", 0);
			}
			
			String paramValue = paramTokenizer.nextToken().trim();
			
			try {
				params.add(new Parameter(paramName, paramValue));
			} catch (IllegalArgumentException e) {
				throw new ParseException("Invalid parameter: " + e.getMessage(), 0);
			}
		}
		
		return new ContentType(type, subtype, params.toArray(new Parameter[0]));
	}

}
