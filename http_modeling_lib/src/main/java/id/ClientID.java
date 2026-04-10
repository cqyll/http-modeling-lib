package id;

public final class ClientID extends Identifier {
	
	private static final long serialVersionUID = 8098426263125084877L;
	
	public ClientID(final String value) {
		super(value);
	}
	
	public ClientID(final Identifier value) {
		super(value.getValue());
	}
	
	public ClientID(final int byteLength) {
		super(byteLength);
	}
	
	public ClientID() {
		super();
	}
	
	@Override
	public boolean equals(final Object object) {
		return object instanceof ClientID &&
				this.toString().equals(object.toString());
	}
}
