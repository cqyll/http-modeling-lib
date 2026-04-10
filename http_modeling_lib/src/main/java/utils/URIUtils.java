package utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class URIUtils {

	public static URI getBaseURI(final URI uri) {
		if (uri == null)
			return null;

		try {
			return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	private URIUtils() {
	}
}
