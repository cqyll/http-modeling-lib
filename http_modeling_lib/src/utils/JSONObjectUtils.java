package utils;

import java.net.URI;
import java.net.URISyntaxException;

import exception.ParseException;
import net.minidev.json.JSONObject;

public final class JSONObjectUtils {

	/*
	 * boolean returns true if JSON object is defined and contains the specified
	 * key.
	 */
	public static boolean containsKey(final JSONObject jsonObject, final String key) {
		return jsonObject != null && jsonObject.containsKey(key);
	}

	public static JSONObject parse(final String s) throws ParseException {
		Object o = JSONUtils.parseJSON(s);

		if (o instanceof JSONObject) {
			return (JSONObject) o;
		} else {
			throw new ParseException("The JSON entity is not an object");
		}
	}

	public static <T> T getGeneric(final JSONObject o, final String key, final Class<T> clazz) throws ParseException {
		if (!o.containsKey(key))
			throw new ParseException("Missing JSON object member with key " + key);

		Object value = o.get(key);

		if (value == null)
			throw new ParseException("JSON object member with key " + key + " has null value");

		try {
			return JSONUtils.to(value, clazz);
		} catch (ParseException e) {
			throw new ParseException("Unexpected type of JSON object member with key " + key, e);
		}
	}

	public static String getString(final JSONObject o, final String key) throws ParseException {
		return getGeneric(o, key, String.class);
	}

	/**
	 * Gets a string member of a JSON object.
	 *
	 * @param o   The JSON object. Must not be {@code null}.
	 * @param key The JSON object member key. Must not be {@code null}.
	 * @param def The default value to return if the key is not present or the value
	 *            is {@code null}. May be {@code null}.
	 *
	 * @return The member value.
	 *
	 * @throws ParseException If the value is not of the expected type.
	 */
	public static String getString(final JSONObject o, final String key, final String def) throws ParseException {

		if (o.get(key) != null) {
			return getString(o, key);
		}

		return def;
	}

	public static String getNonBlankString(final JSONObject o, final String key) throws ParseException {

		String value = getString(o, key);
		if (StringUtils.isBlank(value))
			throw new ParseException("Empty or blank JSON object member with key " + key);
		return value;
	}

	public static <T extends Enum<T>> T getEnum(final JSONObject o, final String key, final Class<T> enumClass)
			throws ParseException {

		String value = getString(o, key);

		for (T en : enumClass.getEnumConstants()) {
			if (en.toString().equalsIgnoreCase(value))
				return en;
		}

		throw new ParseException("Unexpected value of JSON object member with key " + key);
	}

	public static <T extends Enum<T>> T getEnum(final JSONObject o, final String key, final Class<T> enumClass,
			final T def) throws ParseException {

		if (o.get(key) != null) {
			return getEnum(o, key, enumClass);
		}
		return def;
	}

	public static URI getURI(final JSONObject o, final String key) throws ParseException {

		try {
			return new URI(getGeneric(o, key, String.class));
		} catch (URISyntaxException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}

	public static URI getURI(final JSONObject o, final String key, final URI def) throws ParseException {

		if (o.get(key) != null) {
			return getURI(o, key);
		}

		return def;
	}
	
	private JSONObjectUtils() {}
}
