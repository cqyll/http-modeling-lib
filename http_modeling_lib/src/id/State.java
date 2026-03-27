package id;

import utils.StringUtils;

/*
 * Opaque value used to maintain state between a request and a callback.
 */
public final class State extends Identifier {
	private static final long serialVersionUID = 5357710275974915335L;
	
	public State(final String value) {
		super(value);
	}
	
	public State(final int byteLength) {
		super(byteLength);
	}
	
	public State() {
		super();
	}
	
	@Override
	public boolean equals(final Object object) {
		return object instanceof State &&
				this.toString().equals(object.toString());
	}
	
	
	public static State parse(final String s) {
		if(StringUtils.isBlank(s))
			return null;
		
		return new State(s);
	}
	
}
