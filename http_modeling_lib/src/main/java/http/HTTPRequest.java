package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import exception.ParseException;
import exception.SerializeException;
import utils.MapUtils;
import utils.StringUtils;
import utils.URLUtils;

public class HTTPRequest extends HTTPMessage implements ReadOnlyHTTPRequest {
	public enum Method {
		GET, POST, PUT, DELETE
	}

	private final Method method;

	private URL url;

	// transport layer config
	private int connectTimeout = 0;
	private int readTimeout = 0;

	private boolean followRedirects = true;

	private Proxy proxy = null; // no connection proxy by default

	// hostname verifier and SSL socket factory to use for outgoing HTTPS requests
	private HostnameVerifier hostnameVerifier = null;
	private SSLSocketFactory sslSocketFactory = null;

	private static HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
	private static SSLSocketFactory defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

	/**
	 * received validated client X.509 cert for a received HTTPS request.
	 */
	private X509Certificate clientX509Certificate = null;

	/**
	 * subject DN of a received client X.509 certificate for a received HTTPS
	 * request.
	 */
	private String clientX509CertificateSubjectDN = null;

	/**
	 * root issuer of DN of a received client X.509 certificate for a received HTTPS
	 * request.
	 */
	private String clientX509CertificateRootDN = null;

	/**
	 * if {@code true} disables swallowing of {@link IOException}s when the HTTP
	 * connection streams are closed.
	 */
	private boolean debugCloseStreams = false;

	public HTTPRequest(final Method method, final URL url) {
		if (method == null)
			throw new IllegalArgumentException("The HTTP method must not be null");

		this.method = method;

		if (url == null)
			throw new IllegalArgumentException("The HTTP URL must not be null");

		this.url = url;
	}

	public HTTPRequest(final Method method, final URI uri) {
		this(method, toURLWithUncheckedException(uri));
	}

	private static URL toURLWithUncheckedException(final URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException | IllegalArgumentException e) {
			throw new SerializeException(e.getMessage(), e);
		}
	}

	public void ensureMethod(final Method expectedMethod) throws ParseException {
		if (method != expectedMethod) {
			throw new ParseException("The HTTP request method must be " + expectedMethod);
		}
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public URI getURI() {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public int getReadTimeout() {
		return readTimeout;
	}

	public void setConnectTimeout(final int connectTimeout) {
		if (connectTimeout < 0) {
			throw new IllegalArgumentException("The HTTP connect timeout must be non-negative");
		}
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(final int readTimeout) {
		if (readTimeout < 0) {
			throw new IllegalArgumentException("The HTTP response read timeout must be non-negative");
		}
		this.readTimeout = readTimeout;
	}

	public String getAuthorization() {
		return getHeaderValue("Authorization");
	}

	public void setAuthorization(final String authz) {
		setHeader("Authorization", authz);
	}

	/**
	 * Returns the proxy to use for this HTTP request.
	 * 
	 * @return The connection specific proxy for this request, {@code null} for
	 *         default proxy.
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * Tunnels this http request via the specified proxy. Configures the proxy on
	 * the {@link java.net.URLConnection}. The proxy is only used for this instance
	 * and bypasses any other proxy settings (such as those set via System
	 * properties or {@link java.net.ProxySelector}).
	 * 
	 * Supplying {@code null} (default) reverts to default proxy strategy of
	 * {@link java.net.URLConnection}. To avoid using a proxy, supply
	 * {@link Proxy#NO_PROXY}.
	 * 
	 * @see URL#openConnection(Proxy)
	 */
	public void setProxy(final Proxy proxy) {
		this.proxy = proxy;
	}

	public String getAccept() {
		return getHeaderValue("Accept");
	}

	public void setAccept(final String accept) {
		setHeader("Accept", accept);
	}

	/**
	 * Gets boolean flag to determine whether HTTP redirects (3xx) should be
	 * automatically followed.
	 * 
	 * @return
	 */
	public boolean getFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(final boolean follow) {
		followRedirects = follow;
	}

	public void setFragment(final String fragment) {
		url = URLUtils.setEncodedFragment(url, fragment);
	}

	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}

	public X509Certificate getClienntX509Certificate() {
		return clientX509Certificate;
	}

	public void setClientX509Certificate(final X509Certificate clientX509Certificate) {
		this.clientX509Certificate = clientX509Certificate;
	}

	public String getClientX509CertificateSubjectDN() {
		return clientX509CertificateSubjectDN;
	}

	public void setClientX509CertificateSubjectDN(final String subjectDN) {
		this.clientX509CertificateSubjectDN = subjectDN;
	}

	public String getClientX509CertificateRootDN() {
		return clientX509CertificateRootDN;
	}

	public void setClientX509CertificateRootDN(final String rootDN) {
		this.clientX509CertificateRootDN = rootDN;
	}

	public void setSSLSocketFactory(final SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}
	
	
	public static SSLSocketFactory getDefaultSSLSocketFactory() {
		return defaultSSLSocketFactory;
	}
	
	public static void setDefaultSSLSocketFactory(final SSLSocketFactory sslSocketFactory) {
		if (sslSocketFactory == null) {
			throw new IllegalArgumentException("The SSL socket factory must not be null");
		}
		
		HTTPRequest.defaultSSLSocketFactory = sslSocketFactory;
	}

	public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}
	
	public static HostnameVerifier getDefaultHostnameVerifier() {
		return defaultHostnameVerifier;
	}
	
	public static void setDefaultHostnameVerifier(final HostnameVerifier defaultHostnameVerifier) {
		if (defaultHostnameVerifier == null) {
			throw new IllegalArgumentException("The hostname verifier must not be null");
		}
		
		HTTPRequest.defaultHostnameVerifier = defaultHostnameVerifier;
	}

	/**
	 * Enables debugging of the closing of the HTTP connection streams.
	 * 
	 * @param debugCloseStreams If {@code true} disables swallowing of
	 *                          {@link IOException}s when the HTTP connection
	 *                          streams are closed.
	 */
	void setDebugCloseStreams(final boolean debugCloseStreams) {
		this.debugCloseStreams = debugCloseStreams;
	}

	/**
	 * Appends the specified raw (encoded) query string to the current HTTP request
	 * {@link #getURL() URL} query.
	 * 
	 * <p>
	 * If the current URL has a query string the new query is appended with `&amp;`
	 * in front.
	 * 
	 * <p>
	 * The '?' character preceding the query string must not be included.
	 * 
	 * <p>
	 * Example query string to append:
	 * 
	 * <pre>
	 * client_id=123&amp;logout_hint=eepaeph8siot&amp;state=shah2key
	 * </pre>
	 * 
	 * @param queryString The query string to append, blank or {@code null} if
	 *                    nothing to append.
	 * 
	 * @throws IllegalArgumentException If the URL composition failed.
	 */
	public void appendQueryString(final String queryString) {
		if (StringUtils.isBlank(queryString))
			return;

		if (StringUtils.isNotBlank(queryString) && queryString.startsWith("?")) {
			throw new IllegalArgumentException("The query string must not start with ?");
		}

		StringBuilder sb = new StringBuilder();

		if (StringUtils.isNotBlank(url.getQuery())) {
			sb.append(url.getQuery());
			sb.append('&');
		}
		sb.append(queryString);

		url = URLUtils.setEncodedQuery(url, sb.toString());
	}

	/**
	 * Appends the specified query parameters to the current HTTP request
	 * {@link #getURI() URL} query.
	 * 
	 * <p>
	 * If the current URL has a query string the new query is appended with `&amp;`
	 * in front.
	 * 
	 * @param queryParams The query parameters to append, empty or {@code null} if
	 *                    nothing to append.
	 * 
	 * @throws IllegalArgumentException If the URL composition failed.
	 */
	public void appendQueryParameters(final Map<String, List<String>> queryParams) {
		if (MapUtils.isEmpty(queryParams)) {
			return;
		}

		appendQueryString(URLUtils.serializeParameters(queryParams));
	}

	/**
	 * Gets the query string as a parameter map. The parameters are decoded
	 * according to {@code application/x-www-form-urlencoded}
	 * 
	 * @return The query parameters to, decoded. If none the map will be empty.
	 */
	public Map<String, List<String>> getQueryStringParameters() {
		return URLUtils.parseParameters(url.getQuery());
	}

	/**
	 * Returns an established HTTP URL connection for this HTTP request.
	 * 
	 * @return The HTTP URL connection, with the request sent and ready to read the
	 *         response.
	 * @throws IOException If the HTTP request couldn't be made, due to a network or
	 *                     other error.
	 */
	public HttpURLConnection toHttpURLConnection() throws IOException {
		final URL finalURL = getURL();

		HttpURLConnection conn = (HttpURLConnection) (proxy == null ? finalURL.openConnection()
				: finalURL.openConnection(proxy));

		if (conn instanceof HttpsURLConnection) {
			HttpsURLConnection sslConn = (HttpsURLConnection) conn;
			sslConn.setHostnameVerifier(hostnameVerifier != null ? hostnameVerifier : getDefaultHostnameVerifier());
			sslConn.setSSLSocketFactory(sslSocketFactory != null ? sslSocketFactory: getDefaultSSLSocketFactory());
		}

		for (Map.Entry<String, List<String>> header : getHeaderMap().entrySet()) {
			for (String headerValue : header.getValue()) {
				conn.addRequestProperty(header.getKey(), headerValue);
			}
		}

		conn.setRequestMethod(getMethod().name());
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		conn.setInstanceFollowRedirects(followRedirects);

		if (method.equals(Method.POST) || method.equals(Method.PUT)) {
			conn.setDoOutput(true);

			if (getEntityContentType() != null) {
				conn.setRequestProperty("Content-Type", getEntityContentType().toString());
			}

			if (getBody() != null) {
				OutputStream outputStream = null;
				try {
					outputStream = conn.getOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(outputStream);
					writer.write(getBody());
					writer.close();
				} catch (IOException e) {
					closeStreams(conn.getInputStream(), outputStream, conn.getErrorStream(), debugCloseStreams);
					throw e;
				}
			}
		}

		return conn;
	}

	public HTTPResponse send() throws IOException {

		HttpURLConnection conn = toHttpURLConnection();

		int statusCode;

		BufferedReader reader;

		InputStream inputStream = null;
		InputStream errStream = null;
		OutputStream outputStream = null;

		try {
			if (conn.getDoOutput()) {
				outputStream = conn.getOutputStream();
			}
			inputStream = conn.getInputStream();

			reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			statusCode = conn.getResponseCode();
		} catch (IOException e) {
			// HttpURLConnection thorws IOException if any 4XX response is sent. Must set it
			// again.
			statusCode = conn.getResponseCode();

			if (statusCode == -1) {
				throw e; // rethrow IOException
			} else {
				errStream = conn.getErrorStream();

				if (errStream != null) {
					reader = new BufferedReader(new InputStreamReader(errStream, StandardCharsets.UTF_8));
				} else {
					reader = new BufferedReader(new StringReader(""));
				}
			}
		}

		StringBuilder body = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			body.append(line);
			body.append(System.getProperty("line.separator"));
		}
		reader.close();

		HTTPResponse response = new HTTPResponse(statusCode);

		response.setStatusMessage(conn.getResponseMessage());

		// headers
		for (Map.Entry<String, List<String>> responseHeader : conn.getHeaderFields().entrySet()) {
			if (responseHeader.getKey() == null) {
				continue; // skip header
			}

			List<String> values = responseHeader.getValue();
			if (values == null || values.isEmpty() || values.get(0) == null) {
				continue; // skip header
			}

			response.setHeader(responseHeader.getKey(), values.toArray(new String[] {}));
		}

		closeStreams(inputStream, outputStream, errStream, debugCloseStreams);

		final String bodyContent = body.toString();
		if (!bodyContent.isEmpty()) {
			response.setBody(bodyContent);
		}

		return response;
	}

	public HTTPResponse send(final HTTPRequestSender httpRequestSender) throws IOException {

		ReadOnlyHTTPResponse roResponse = httpRequestSender.send(this);

		HTTPResponse response = new HTTPResponse(roResponse.getStatusCode());
		response.setStatusMessage(roResponse.getStatusMessage());

		for (Map.Entry<String, List<String>> en : roResponse.getHeaderMap().entrySet()) {
			if (en.getKey() != null && en.getValue() != null && !en.getValue().isEmpty()) {
				response.setHeader(en.getKey(), en.getValue().toArray(new String[0]));
			}
		}
		response.setBody(roResponse.getBody());
		return response;
	}

	private static void closeStreams(final InputStream inputStream, final OutputStream outputStream,
			final InputStream errStream, final boolean debugCloseStreams) throws IOException {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			if (debugCloseStreams) {
				throw e;
			}
		} catch (Exception e) {
			// ignore
		}

		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			if (debugCloseStreams) {
				throw e;
			}
		} catch (Exception e) {
			// ignore
		}

		try {
			if (errStream != null) {
				errStream.close();
			}
		} catch (IOException e) {
			if (debugCloseStreams) {
				throw e;
			}
		} catch (Exception e) {
			// ignore
		}

	}
}
