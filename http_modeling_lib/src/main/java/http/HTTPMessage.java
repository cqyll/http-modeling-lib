package http;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import exception.ParseException;
import net.minidev.json.JSONObject;
import utils.ContentTypeUtils;
import utils.JSONObjectUtils;
import utils.MapUtils;
import utils.StringUtils;
import utils.URLUtils;

/**
 * The base abstract class for HTTP requests and responses.
 */
public abstract class HTTPMessage implements ReadOnlyHTTPMessage {

	/**
	 * The HTTP request/response headers.
	 */
	private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * The HTTP message body.
	 */
	private String body;

	/**
	 * The client IP address.
	 */
	private String clientIPAddress;

	/**
	 * Gets the {@code Content-Type} header value.
	 * 
	 * @return The {@code Content-Type} header value, {@code null} if not specified
	 *         or parsing failed
	 */
	public ContentType getEntityContentType() {
		final String value = getHeaderValue("Content-Type");

		if (value == null) {
			return null;
		}

		try {
			return ContentType.parse(value);
		} catch (java.text.ParseException e) {
			return null;
		}
	}

	public void setEntityContentType(final ContentType ct) {
		setHeader("Content-Type", ct != null ? ct.toString() : null);
	}

	public void setContentType(final String ct) throws ParseException {

		try {
			setHeader("Content-Type", ct != null ? ContentType.parse(ct).toString() : null);
		} catch (java.text.ParseException e) {
			throw new ParseException("Invalid Content-Type value: " + e.getMessage());
		}
	}

	public void setHeader(final String name, final String... values) {

		if (values != null && values.length > 0) {
			headers.put(name, Arrays.asList(values));
		} else {
			headers.remove(name);
		}
	}

	public void ensureEntityContentType() throws ParseException {

		if (getEntityContentType() == null) {
			throw new ParseException("Missing HTTP Content-Type header");
		}
	}

	public void ensureEntityContentType(final ContentType contentType) throws ParseException {
		ContentTypeUtils.ensureContentType(contentType, getEntityContentType());
	}

	public void ensureEntityContentType(final ContentType contentType, final String subTypeSuffix)
			throws ParseException {
		ContentTypeUtils.ensureContentType(contentType, subTypeSuffix, getEntityContentType());
	}

	// getters

	/**
	 * Get HTTP header's value.
	 * 
	 * @param name The header name. Must not be {@code null}
	 * 
	 * @return The first header value, {@code null} if not specified.
	 */
	public String getHeaderValue(final String name) {
		return MapUtils.getFirstValue(headers, name);
	}

	/**
	 * Get an HTTP header's value(s)
	 * 
	 * @param name The header name. Must not be {@code null}.
	 * 
	 * @return The header value(s), {@code null} if not specified.
	 */
	public List<String> getHeaderValues(final String name) {
		return headers.get(name);
	}

	@Override
	public Map<String, List<String>> getHeaderMap() {
		return headers;
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	private void ensureBody() throws ParseException {
		if (getBody() == null || getBody().isEmpty()) {
			throw new ParseException("Missing or empty HTTP message body");
		}
	}

	public Map<String, List<String>> getBodyAsFormParameters() throws ParseException {
		ensureEntityContentType(ContentType.APPLICATION_URLENCODED);

		if (StringUtils.isBlank(getBody())) {
			return Collections.emptyMap();
		}

		return URLUtils.parseParameters(getBody());
	}

	public JSONObject getBodyAsJSONObject() throws ParseException {
		ensureEntityContentType(ContentType.APPLICATION_JSON, "json");

		// json parsing can't proceed without an actual body to parse
		// unlike form parameters, a missing body is not a valid json object.
		ensureBody();

		return JSONObjectUtils.parse(getBody());
	}

	public String getClientIPAddress() {
		return clientIPAddress;
	}

	public void setClientIPAddress(final String clientIPAddress) {
		this.clientIPAddress = clientIPAddress;
	}

}
