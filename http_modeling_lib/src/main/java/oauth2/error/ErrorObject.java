package oauth2.error;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ParseException;
import net.minidev.json.JSONObject;
import utils.JSONObjectUtils;
import utils.MapUtils;

//Error object, used to encapsulate OAuth 2.0 and other errors. Supports custom parameters.
//Example error object as HTTP response:
//
// HTTP/1.1 400 Bad Request
// Content-Type: application/json;charset=UTF-8
// Cache-Control: no-store
// Pragma: no-cache
//
// {
//   "error" : "invalid_request"
// }
// 

public final class ErrorObject implements Serializable {

	@Serial
	private static final long serialVersionUID = 361808781364656206L;

	/**
	 * error code, may not always be defined.
	 */
	private final String code;

	/**
	 * Optional error description
	 */
	private final String description;

	/**
	 * Optional HTTP status code, 0 if not defined.
	 */
	private final int httpStatusCode;

	/**
	 * Optional URI of a web page that includes additional information about the
	 * error.
	 */
	private final URI uri;

	/**
	 * Optional custom parameters, empty or {@code null} if none.
	 */
	private final Map<String, String> customParams;

	public ErrorObject(final String code, final String description, final int httpStatusCode, final URI uri,
			final Map<String, String> customParams) {
		if (!isLegal(code)) {
			throw new IllegalArgumentException("Illegal char(s) in code, see RFC 6749, section 5.2");
		}
		this.code = code;

		if (!isLegal(description)) {
			throw new IllegalArgumentException("Illegal char(s) in description, see RFC 6749, section 5.2");
		}
		this.description = description;

		this.httpStatusCode = httpStatusCode;
		this.uri = uri;

		this.customParams = customParams;
	}

	public ErrorObject(final String code, final String description, final int httpStatusCode, final URI uri) {
		this(code, description, httpStatusCode, uri, null);
	}

	public ErrorObject(final String code, final String description, final int httpStatusCode) {
		this(code, description, httpStatusCode, null);
	}

	public ErrorObject(final String code, final String description) {
		this(code, description, 0, null);
	}

	public ErrorObject(final String code) {
		this(code, null, 0, null);
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public int getHTTPStatusCode() {
		return httpStatusCode;
	}

	public URI getURI() {
		return uri;
	}

	public Map<String, String> getCustomParams() {
		if (MapUtils.isNotEmpty(customParams)) {
			return Collections.unmodifiableMap(customParams);
		} else {
			return Collections.emptyMap();
		}
	}

	public ErrorObject setDescription(final String description) {
		return new ErrorObject(getCode(), description, getHTTPStatusCode(), getURI(), getCustomParams());
	}

	public ErrorObject appendDescription(final String text) {
		String newDescription;

		if (getDescription() != null) {
			newDescription = getDescription() + text;
		} else {
			newDescription = text;
		}

		return new ErrorObject(getCode(), newDescription, getHTTPStatusCode(), getURI(), getCustomParams());
	}

	public ErrorObject setHTTPStatusCode(final int httpStatusCode) {
		return new ErrorObject(getCode(), getDescription(), httpStatusCode, getURI(), getCustomParams());
	}

	public ErrorObject setURI(final URI uri) {
		return new ErrorObject(getCode(), getDescription(), getHTTPStatusCode(), uri, getCustomParams());
	}

	public ErrorObject setCustomParams(final Map<String, String> customParams) {
		return new ErrorObject(getCode(), getDescription(), getHTTPStatusCode(), getURI(), customParams);
	}

	// helpers

	/*
	 * legal range as per rfc 6749, section 5.2. values for the error/error_description parameter must
	 * not include characters outside the set %x20-21 / %x23-5B / %x5D-7E
	 */
	public static boolean isLegal(final char c) {
		if (c > 0x7f) {
			// not ASCII
			return false;
		}
		
		return (c >= 0x20 && c <= 0x21) || (c >= 0x23 && c <= 0x58) || (c >= 0x5D && c <= 0x7E);
	}

	public static boolean isLegal(final String s) {
		if (s == null)
			return true;

		for (char c : s.toCharArray()) {
			if (!isLegal(c)) {
				return false;
			}
		}

		return true;
	}

	public static String removeIllegalChars(final String s) {
		if (s == null)
			return null;

		final StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (isLegal(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns JSON object representation of this error object.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * {
	 * 		"error" 			: "invalid_grant"
	 * 		"error_description" : "Invalid resource owner credentials"
	 * }
	 * </pre>
	 * 
	 * @return The JSON object.
	 */
	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();

		if (getCode() != null) {
			o.put("error", getCode());
		}

		if (getDescription() != null) {
			o.put("error_description", getDescription());
		}

		if (getURI() != null) {
			o.put("error_uri", getURI().toString());
		}

		if (!getCustomParams().isEmpty()) {
			o.putAll(getCustomParams());
		}
		return o;
	}

	/**
	 * Returns a parameters representation of this error object. Suitable for
	 * URL-encoded error responses
	 * 
	 * @return The parameters.
	 */
	public Map<String, List<String>> toParameters() {
		Map<String, List<String>> params = new HashMap<>();

		if (getCode() != null)
			params.put("error", Collections.singletonList(getCode()));
		if (getDescription() != null)
			params.put("error_descripition", Collections.singletonList(getDescription()));
		if (getURI() != null)
			params.put("error_uri", Collections.singletonList(getURI().toString()));

		if (!getCustomParams().isEmpty()) {
			for (Map.Entry<String, String> en : getCustomParams().entrySet()) {
				params.put(en.getKey(), Collections.singletonList(en.getValue()));
			}
		}

		return params;
	}
	
	/**
	 * @see #getCode()
	 */
	@Override
	public String toString() {
		return code != null ? code : null;
	}
	
	@Override
	public int hashCode() {
		return code != null ? code.hashCode() : "null".hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof ErrorObject &&
				this.toString().equals(obj.toString());
	}

	// parsers

	/*
	 * RFC 6749, section 5.2 defines error responses to include parameters in the
	 * entity-body using application/json media type. The parameters are serialized
	 * into a JSON structure by adding each parameter at the highest structure
	 * level. Order of parameters does not matter and can vary. Parameter names and
	 * string values are included as JSON strings, Numerical values are included as
	 * JSON numbers.
	 * 
	 * RFC 6749, section 4.1.2.1 states that parameters are added to the query
	 * component of the redirection URI using application/x-www-form-urlencoded
	 * 
	 */
	public static ErrorObject parse(final JSONObject jsonObject) {
		String code = null;
		try {
			code = JSONObjectUtils.getString(jsonObject, "error", null);
		} catch (ParseException e) {
			// ignore and continue
		}
		
		if (!isLegal(code)) {
			code = null;
		}
		
		String description = null;
		try {
			description = JSONObjectUtils.getString(jsonObject, "error_description", null);
		} catch (ParseException e) {
			// ignore and continue
		}
		
		URI uri = null;
		try {
			uri = JSONObjectUtils.getURI(jsonObject, "error_uri", null);
		} catch (ParseException e) {
			// ignore and continue
		}
		
		
		Map<String, String> customParams = null;
		for (Map.Entry<String, Object> en: jsonObject.entrySet()) {
			if (!"error".equals(en.getKey()) && !"error_description".equals(en.getKey()) && !"error_uri".equals(en.getKey())) {
				if (en.getValue() == null || en.getValue() instanceof String) {
					if (customParams == null) {
						customParams = new HashMap<>();
					}
					customParams.put(en.getKey(), (String) en.getValue());
				}
			}
		}
		
		return new ErrorObject(code, removeIllegalChars(description), 0, uri, customParams);
	}
	
	
	public static ErrorObject parse(final Map<String, List<String>> params) {
		String code = MapUtils.getFirstValue(params, "error");
		String description = MapUtils.getFirstValue(params, "error_description");
		String uriString = MapUtils.getFirstValue(params, "error_uri");
		
		if (!isLegal(code)) {
			code = null;
		}
		
		URI uri = null;
		if (uriString != null) {
			try {
				uri = new URI(uriString);
			} catch (URISyntaxException e) {
				// ignore
			}
		}
		
		Map<String, String> customParams = null;
		for (Map.Entry<String, List<String>> en: params.entrySet()) {
			if(!"error".equals(en.getKey()) && !"error_description".equals(en.getKey()) && !"error_uri".equals(en.getKey())) {
				if (customParams == null) {
					customParams = new HashMap<>();
				}
				
				if (en.getValue() == null) {
					customParams.put(en.getKey(), null); 
				} else if (!en.getValue().isEmpty()) {
					customParams.put(en.getKey(), en.getValue().get(0));
				}
			}
		}
		
		return new ErrorObject(code, removeIllegalChars(description), 0, uri, customParams);
		
	}

//	public static ErrorObject parse(HTTPResponse httpResponse) {
//		Objects.requireNonNull(httpResponse, "httpResponse must not be null");
//		
//		final String body = httpResponse.getBody();
//		
//		if (body == null || body.isBlank()) {
//			return new ErrorObject(null, null, httpResponse.getStatusCode(), null, null);
//		}
//	}
//	
//	// parse util for json bodies
//	private static Map<String, Object> parseFlatJsonObject(final String json) {
//		final String trimmed = json == null ? "" : json.trim();
//		if (trimmed.isEmpty()) return Collections.emptyMap();
//		if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
//			throw new IllegalArgumentException("Body is not a JSON object");
//		}
//		final String inner = trimmed.substring(1, trimmed.length() - 1).trim();
//		if (inner.isEmpty()) {
//			return Collections.emptyMap();
//		}
//		
//		final List<String> pairs;
//	}
}
