package utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public final class URLUtils {

	public static final String CHARSET = "utf-8";

	/**
	 * Gets the base part (protocol, host, port and path) of the specified URL.
	 * 
	 * @param url The URL. May be {@code null}
	 * 
	 * @return The base part of the URL, {@code null} if the original URL is
	 *         {@code null} or doesn't specify a protocol.
	 */
	public static URL getBaseURL(final URL url) {
		if (url == null)
			return null;

		try {
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Sets the encoded query of the specified URL.
	 * 
	 * @param url   The URL. May be {@code null}.
	 * @param query The encoded query, {@code null} if not specified.
	 * 
	 * @return The new URL.
	 */
	public static URL setEncodedQuery(final URL url, final String query) {
		if (url == null)
			return null;

		try {
			URI uri = url.toURI();
			StringBuilder sb = new StringBuilder(URIUtils.getBaseURI(uri).toString());
			if (query != null && !query.isEmpty()) {
				sb.append('?');
				sb.append(query);
			}
			if (uri.getRawFragment() != null) {
				sb.append('#');
				sb.append(uri.getRawFragment());
			}
			return new URL(sb.toString());
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Sets the encoded fragment of the specified URL.
	 * 
	 * @param url      The URL. May be {@code null}.
	 * @param fragment The encoded fragment, {@code null} if not specified.
	 * 
	 * @return The new URL.
	 */
	public static URL setEncodedFragment(final URL url, final String fragment) {
		if (url == null)
			return null;

		try {
			URI uri = url.toURI();
			StringBuilder sb = new StringBuilder(URIUtils.getBaseURI(uri).toString());
			if (uri.getRawQuery() != null) {
				sb.append('?');
				sb.append(uri.getRawQuery());
			}

			if (fragment != null && !fragment.isEmpty()) {
				sb.append('#');
				sb.append(fragment);
			}
			return new URL(sb.toString());
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Performs {@code application/x-www-form-urlencoded} encoding on the specified
	 * parameter keys and values.
	 * 
	 * @param params A map of the parameters. May be empty or {@code null}.
	 * 
	 * @return The encoded parameters, {@code null} if not specified.
	 */
	public static Map<String, List<String>> urlEncodeParameters(final Map<String, List<String>> params) {

		if (MapUtils.isEmpty(params))
			return params;

		Map<String, List<String>> out = new LinkedHashMap<>();

		for (Map.Entry<String, List<String>> entry : params.entrySet()) {

			try {
				String newKey = entry.getKey() != null ? URLEncoder.encode(entry.getKey(), CHARSET) : null;

				List<String> newValues;

				if (entry.getValue() != null) {
					newValues = new LinkedList<>();

					for (String value : entry.getValue()) {
						if (value != null) {
							newValues.add(URLEncoder.encode(value, CHARSET));
						} else {
							newValues.add(null);
						}
					}
				} else {
					newValues = null;
				}

				out.put(newKey, newValues);
			} catch (UnsupportedEncodingException e) {
				// utf-8 must always be supported
				throw new RuntimeException(e);
			}
		}
		return out;
	}

	/**
	 * Serialises the specified map of paramters into a URL query string. The
	 * parameter keys and values are {@code application/x-www-form-urlencoded}
	 * encoded.
	 * 
	 * <p>
	 * Parameters with {@code null} keys or values are ignored and not serialised.
	 * 
	 * <p>
	 * Not that the '?' character preceding the query string in GET requests is not
	 * included in the returned string.
	 * 
	 * <p>
	 * Example query string:
	 * 
	 * <pre>
	 * response_type=code
	 * &amp;client_id=s6BhdRkqt3
	 * &amp;state=xyz
	 * &amp;redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
	 * </pre>
	 * 
	 * <p>
	 * The ooposite method is {@link #parseParameters}.
	 * 
	 * @param params A map of the URL query parameters. May be empty or
	 *               {@code null}.
	 * 
	 * @return The serialised URL query string, empty if not parameters.
	 */
	public static String serializeParameters(final Map<String, List<String>> params) {

		if (params == null || params.isEmpty())
			return "";

		Map<String, List<String>> encodedParams = urlEncodeParameters(params);

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, List<String>> entry : encodedParams.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				continue;
			}

			for (String value : entry.getValue()) {
				if (value == null) {
					value = "";
				}
				if (sb.length() > 0) {
					sb.append('&');
				}

				sb.append(entry.getKey());
				sb.append('=');
				sb.append(value);
			}
		}
		return sb.toString();
	}

	/**
	 * Parses the specified URL query string into a parameter map. If a parameter
	 * has multiple values only the first one will be saved. The parameter keys and
	 * values are {@code application/x-www-form-urlencoded} decoded.
	 * 
	 * <p>
	 * Note that the '?' character preceding the query string in GET requests must
	 * not be included.
	 * 
	 * <p>
	 * Example query string:
	 * 
	 * <pre>
	 * response_type=code
	 * &amp;client_id=s6BhdRkqt3
	 * &amp;state=xyz
	 * &amp;redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
	 * </pre>
	 *
	 * <p>
	 * The opposite method {@link #serializeParameters}.
	 *
	 * @param query The URL query string to parse. May be {@code null}.
	 *
	 * @return A map of the URL query parameters, empty if none are found.
	 */
	public static Map<String, List<String>> parseParameters(final String query) {
		Map<String, List<String>> params = new LinkedHashMap<>();

		if (StringUtils.isBlank(query))
			return params; // empty map

		StringTokenizer st = new StringTokenizer(query.trim(), "&");

		while (st.hasMoreTokens()) {
			String param = st.nextToken();

			String[] pair = param.split("=", 2); // split around first '='

			String key, value;

			try {
				key = URLDecoder.decode(pair[0], CHARSET);
				value = pair.length > 1 ? URLDecoder.decode(pair[1], CHARSET) : "";
			} catch (UnsupportedEncodingException e) {
				continue;
			} catch (Exception e) {
				continue;
			}

			if (params.containsKey(key)) {
				List<String> updatedValueList = new LinkedList<>(params.get(key));
				updatedValueList.add(value);
				params.put(key, Collections.unmodifiableList(updatedValueList));
			} else {
				params.put(key, Collections.singletonList(value));
			}
		}
		return params;
	}

	private URLUtils() {
	}
}
