package http;

import java.util.Arrays;

import exception.ParseException;

public class HTTPResponse extends HTTPMessage implements ReadOnlyHTTPResponse {

	public static final int SC_OK = 200;
	public static final int SC_CREATED = 201;
	public static final int SC_FOUND = 302;
	public static final int SC_BAD_REQUEST = 400;
	public static final int SC_UNAUTHORIZED = 401;
	public static final int SC_FORBIDDEN = 403;
	public static final int SC_NOT_FOUND = 404;
	public static final int SC_SERVER_ERROR = 500;
	public static final int SC_SERVICE_UNAVAILABLE = 503;

	private final int statusCode;

	private String statusMessage;

	public HTTPResponse(final int statusCode) {
		this.statusCode = statusCode;
	}

	public boolean indicatesSuccess() {
		return statusCode >= 200 && statusCode < 300;
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(final String message) {
		this.statusMessage = message;
	}

	public void ensureStatusCode(final int... expectedStatusCode) throws ParseException {
		for (int c : expectedStatusCode) {
			if (this.statusCode == c)
				return;
		}

		throw new ParseException(
				"Unexpected HTTP status code " 
		+ this.statusCode + " , must be "
						+ Arrays.toString(expectedStatusCode));
	}

}
