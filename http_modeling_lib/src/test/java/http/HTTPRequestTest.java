package http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exception.ParseException;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.*;

import static net.jadler.Jadler.*;
import static org.junit.Assert.*;

public class HTTPRequestTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Before
	public void setUp() {
		initJadler();
	}

	@After
	public void tearDown() {
		closeJadler();
	}

	@Test
	public void testDefaultHostnameVerifier() {
		assertEquals(HttpsURLConnection.getDefaultHostnameVerifier(), HTTPRequest.getDefaultHostnameVerifier());
	}

	@Test
	public void testDefaultSSLSocketFactory() {
		assertNotNull(HTTPRequest.getDefaultSSLSocketFactory());
	}

	@Test
	public void testConstructorPOSTAndAccessors() throws Exception {
		URL url = new URL("https://localhost/login");

		HTTPRequest request = new HTTPRequest(HTTPRequest.Method.POST, url);

		assertEquals(HTTPRequest.Method.POST, request.getMethod());
		assertEquals(url, request.getURL());
		assertEquals(url.toURI(), request.getURI());

		request.ensureMethod(HTTPRequest.Method.POST);

		try {
			request.ensureMethod(HTTPRequest.Method.GET);
			fail();
		} catch (ParseException e) {
			assertEquals("The HTTP request method must be GET", e.getMessage());
		}

		assertNull(request.getEntityContentType());
		request.setEntityContentType(ContentType.APPLICATION_JSON);
		assertEquals(ContentType.APPLICATION_JSON.toString(), request.getEntityContentType().toString());

		assertNull(request.getAuthorization());
		request.setAuthorization("Bearer 123");
		assertEquals("Bearer 123", request.getAuthorization());

		assertNull(request.getAccept());
		request.setAccept("text/plain");
		assertEquals("text/plain", request.getAccept());

		request.appendQueryString("x=123&y=456");
		assertEquals("x=123&y=456", request.getURL().getQuery());
		assertEquals("x=123&y=456", request.getURI().getQuery());

		Map<String, List<String>> params = request.getQueryStringParameters();
		assertEquals(Collections.singletonList("123"), params.get("x"));
		assertEquals(Collections.singletonList("456"), params.get("y"));

		request.setBody("{\"apples\":\"123\"}");
		JSONObject jsonObject = request.getBodyAsJSONObject();
		assertEquals("123", jsonObject.get("apples"));

		request.setFragment("fragment");
		assertEquals("fragment", request.getURL().getRef());

		assertEquals(0, request.getConnectTimeout());
		request.setConnectTimeout(250);
		assertEquals(250, request.getConnectTimeout());

		assertTrue(request.getFollowRedirects());
		request.setFollowRedirects(false);
		assertFalse(request.getFollowRedirects());
	}

	@Test
	public void testWithQueryStringAndBody() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST, new URL("https://localhost/token"));
		String queryString = "q-param-1=a&q-param-2=b";
		httpRequest.appendQueryString(queryString);
		String body = "f-param-3=d&f-param-4=e";
		httpRequest.setBody(body);

		assertEquals(queryString, httpRequest.getURL().getQuery());
		assertEquals(queryString, httpRequest.getURI().getQuery());
		assertEquals(body, httpRequest.getBody());
	}

	@Test
	public void testAppendQueryParameters() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST, new URL("http://localhost/login"));
		Map<String, List<String>> params = new LinkedHashMap<>();
		params.put("client_id", Collections.singletonList("123"));
		params.put("redirect_uri", Collections.singletonList("https://example.com/cb"));
		params.put("x-param", Arrays.asList("one", "two"));

		// "http://localhost/login?tenant=abc&client_id=123&redirect_uri=https://example.com/cb&x-param=one&x-param=two"
		httpRequest.appendQueryParameters(params);

		assertEquals(
				"http://localhost/login?client_id=123&redirect_uri=https%3A%2F%2Fexample.com%2Fcb&x-param=one&x-param=two",
				httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryParameters_toExistingQuery() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST,
				new URL("http://localhost/login?tenant=abc"));
		Map<String, List<String>> params = new LinkedHashMap<>();
		params.put("client_id", Collections.singletonList("123"));
		params.put("redirect_uri", Collections.singletonList("https://example.com/cb"));
		params.put("x-param", Arrays.asList("one", "two"));

		httpRequest.appendQueryParameters(params);

		assertEquals(
				"http://localhost/login?tenant=abc&client_id=123&redirect_uri=https%3A%2F%2Fexample.com%2Fcb&x-param=one&x-param=two",
				httpRequest.getURL().toString());

	}

	@Test
	public void testAppendQueryParameters_null() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST,
				new URL("http://localhost/login?tenant=abc"));

		httpRequest.appendQueryParameters(null);

		assertEquals("http://localhost/login?tenant=abc", httpRequest.getURL().toString());
	}

	@Test
	public void testAppentQueryParameters_empty() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST,
				new URL("http://localhost/login?tenant=abc"));
		Map<String, List<String>> params = Collections.emptyMap();

		httpRequest.appendQueryParameters(params);

		assertEquals("http://localhost/login?tenant=abc", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST, new URL("http://localhost/login"));
		httpRequest.appendQueryString("apples=10&some%20pears=20");
		assertEquals("http://localhost/login?apples=10&some%20pears=20", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString_toExistingQuery() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.POST, new URL("http://localhost/login?oranges=0"));
		httpRequest.appendQueryString("apples=10&some%20pears=20");
		assertEquals("http://localhost/login?oranges=0&apples=10&some%20pears=20", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString_null() throws MalformedURLException {

		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, new URL("http://localhost/login"));
		httpRequest.appendQueryString(null);
		assertEquals("http://localhost/login", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString_empty() throws MalformedURLException {

		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, new URL("http://localhost/login"));
		httpRequest.appendQueryString("");
		assertEquals("http://localhost/login", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString_blank() throws MalformedURLException {

		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, new URL("http://localhost/login"));
		httpRequest.appendQueryString(" ");
		assertEquals("http://localhost/login", httpRequest.getURL().toString());
	}

	@Test
	public void testAppendQueryString_startsWithQuestionMark() throws MalformedURLException {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, new URL("http://localhost/login"));

		try {
			httpRequest.appendQueryString("?a=1&b=2");
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The query string must not start with ?", e.getMessage());
		}

	}
	
	@Test
	public void testParseJSONObject() throws Exception {
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, new URL("http://localhost"));
		
		httpRequest.setEntityContentType(ContentType.APPLICATION_JSON);
		httpRequest.setBody("{\"apples\":30, \"pears\":\"green\"}");
		
		JSONObject jsonObject = httpRequest.getBodyAsJSONObject();
	}

}
