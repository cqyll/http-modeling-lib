package exception;

public class SerializeException extends RuntimeException {

	private static final long serialVersionUID = -1441994426154259304L;

	public SerializeException(final String message) {
		this(message, null);
	}

	public SerializeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
