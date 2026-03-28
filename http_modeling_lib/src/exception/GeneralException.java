package exception;

import java.net.URI;

import id.ClientID;
import id.State;
import oauth2.error.ErrorObject;

/*
 * The base class for checked exceptions.
 */
public class GeneralException extends Exception {

	private static final long serialVersionUID = 1641787397301043615L;

	private final ErrorObject error;
	private final ClientID clientID;
	private final URI redirectURI;
	private final State state;

	public GeneralException(final String message) {
		this(message, null, null, null, null, null);
	}
	
	public GeneralException(final String message, final ErrorObject error) {
		this(message, error, null, null, null, null);
	}

	public GeneralException(final String message, final Throwable cause) {
		this(message, null, null, null, null, cause);
	}

	public GeneralException(final String message, final ErrorObject error, final ClientID clientID,
			final URI redirectURI, final State state, final Throwable cause) {
		super(message, cause);

		this.error = error;
		this.clientID = clientID;
		this.redirectURI = redirectURI;
		this.state = state;
	}

	public ErrorObject getError() {
		return error;
	}

	public ClientID getClientID() {
		return clientID;
	}

	public URI getRedirectURI() {
		return redirectURI;
	}

	public State getState() {
		return state;
	}

}
