package exception;

import java.net.URI;

import id.ClientID;
import id.State;
import oauth2.error.ErrorObject;

public class ParseException extends GeneralException {
	
	private static final long serialVersionUID = 5717029305138222869L;
	
	public ParseException(final String message) {
		this(message, null, null, null, null, null);
	}
	
	public ParseException(final String message, final ErrorObject error) {
		this(message, error, null, null, null, null);
	}
	
	public ParseException(final String message, final Throwable cause) {
		this(message, null, null, null, null, cause);
	}
	
	public ParseException(
			final String message,
			final ErrorObject error,
			final Throwable cause) {
		this(message, error, null, null, null, cause);
	}
	
	public ParseException(
			final String message,
			final ErrorObject error,
			final ClientID clientID,
			final URI redirectURI,
			final State state,
			final Throwable cause) {
		super(message, error, clientID, redirectURI, state, cause);
	}
	
}
