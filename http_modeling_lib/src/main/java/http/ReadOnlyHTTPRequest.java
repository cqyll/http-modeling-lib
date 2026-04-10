package http;

import java.net.URI;
import java.net.URL;

public interface ReadOnlyHTTPRequest extends ReadOnlyHTTPMessage {
	int getConnectTimeout();
	HTTPRequest.Method getMethod();
	int getReadTimeout();
	URI getURI();
	URL getURL();
}
