package http;

import java.util.List;
import java.util.Map;

public interface ReadOnlyHTTPMessage {
	String getBody();

	Map<String, List<String>> getHeaderMap();
}
