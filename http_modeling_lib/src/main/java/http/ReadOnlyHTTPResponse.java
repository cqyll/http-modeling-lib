package http;

public interface ReadOnlyHTTPResponse extends ReadOnlyHTTPMessage {
	int getStatusCode();
	String getStatusMessage();
}
