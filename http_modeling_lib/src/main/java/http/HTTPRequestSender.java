package http;

import java.io.IOException;

public interface HTTPRequestSender {
	ReadOnlyHTTPResponse send(final ReadOnlyHTTPRequest httpRequest) throws IOException;
}
