package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class ErrorStreamManualTest {

	public static void main(String[] args) throws Exception {
		testErrorStreamViaToHttpURLConnection();
	}


	public static void testErrorStreamViaToHttpURLConnection() throws Exception {
		System.out.println("=== Testing getErrorStream() via toHttpURLConnection() ===\n");

		// Start embedded HTTP server that returns a 404 with a body
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/error", new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				String response = "{\"error\":\"not_found\",\"message\":\"The resource was not found\"}";
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(404, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
		});
		server.start();

		int port = server.getAddress().getPort();
		System.out.println("Server started on port: " + port);

		// Create HTTPRequest and get HttpURLConnection via toHttpURLConnection()
		HTTPRequest httpRequest = new HTTPRequest(
			HTTPRequest.Method.GET,
			new URL("http://localhost:" + port + "/error")
		);
		httpRequest.setAccept("application/json");

		// This returns the HttpURLConnection object
		HttpURLConnection conn = httpRequest.toHttpURLConnection();
		
		System.out.println("\nConnection object:");
		System.out.println("  Declared type: " + HttpURLConnection.class.getName());
		System.out.println("  Actual class:  " + conn.getClass().getName());
		System.out.println("  Actual class:  " + conn.getClass());

		// Now manually test the error stream just like HTTPRequest.send() does
		System.out.println("\n--- Testing getErrorStream() directly ---");
		
		try {
			System.out.println("Calling conn.getInputStream()...");
			InputStream inputStream = conn.getInputStream();
			System.out.println("  inputStream: " + inputStream);
		} catch (IOException e) {
			System.out.println("  Caught IOException: " + e.getMessage());
			
			int responseCode = conn.getResponseCode();
			System.out.println("\n  conn.getResponseCode(): " + responseCode);
			
			// THIS IS THE KEY LINE - getErrorStream() on the connection from toHttpURLConnection()
			InputStream errorStream = conn.getErrorStream();
			System.out.println("  conn.getErrorStream(): " + errorStream);
			System.out.println("  errorStream == null? " + (errorStream == null));
			
			if (errorStream != null) {
				System.out.println("\n  --- Error stream content ---");
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(errorStream, StandardCharsets.UTF_8));
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("  " + line);
				}
				reader.close();
				System.out.println("  --- End error stream ---");
			} else {
				System.out.println("\n  ✗ getErrorStream() returned null!");
			}
		}
		
		server.stop(0);
		System.out.println("\n=== Test complete ===");
	}
}