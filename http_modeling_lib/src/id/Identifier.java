package id;

import utils.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.security.SecureRandom;

import net.minidev.json.JSONAware;
import net.minidev.json.JSONValue;
import com.nimbusds.jose.util.Base64URL;


/*
 * The base class for representing identifiers. Provides constructors that generate Base64URL-encoded secure random identifier values.
 */
public class Identifier implements Serializable, Comparable<Identifier>, JSONAware {

	private static final long serialVersionUID = 365052911829193101L;

	/*
	 * default byte length of generated identifiers.
	 */
	public static final int DEFAULT_BYTE_LENGTH = 32;

	/**
	 * Returns a string list representation of the specified identifier collection.
	 * Omits nulls.
	 */
	public static List<String> toStringList(final Collection<? extends Identifier> ids) {
		if (ids == null) {
			return Collections.emptyList();
		}
		List<String> stringList = new ArrayList<>(ids.size());
		for (Identifier id : ids) {
			if (id != null) {
				stringList.add(id.getValue());
			}
		}
		return stringList;
	}

	/**
	 * secure random generator
	 */

	protected static final SecureRandom secureRandom = new SecureRandom();

	private final String value;

	public Identifier(final String value) {
		if (StringUtils.isBlank(value))
			throw new IllegalArgumentException("The value must not be null or empty string.");

		this.value = value.trim();
	}
	
	public Identifier(final int byteLength) {
		if (byteLength < 1)
			throw new IllegalArgumentException("The byte length must be a positive integer");
		
		byte[] n = new byte[byteLength];
		secureRandom.nextBytes(n);
		value = Base64URL.encode(n).toString();
	}
	
	/**
	 * Creates a new identifier with a randomly generated 32 byte value encoded in Base64
	 */
	public Identifier() {
		this(DEFAULT_BYTE_LENGTH);
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toJSONString() {
		return "\"" + JSONValue.escape(value) + '"';
	}
	
	@Override
	public String toString() {
		return getValue();
	}
	
	@Override
	public int compareTo(final Identifier other) {
		return getValue().compareTo(other.getValue());
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		
		Identifier other = (Identifier) obj;
		
		return getValue() != null ? getValue().equals(other.getValue()) : other.getValue() == null;
	}
	
	@Override
	public int hashCode() {
		return getValue() != null ? getValue().hashCode() : 0;
	}
	
}
