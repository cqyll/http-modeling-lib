package utils;



import java.util.LinkedList;
import java.util.List;

import exception.ParseException;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.writer.JsonReader;

public final class JSONUtils {

	public static Object parseJSON(final String s) throws ParseException {
		try {

			// created bit mask
			return new JSONParser(
					JSONParser.USE_HI_PRECISION_FLOAT | JSONParser.ACCEPT_TAILLING_SPACE | JSONParser.LIMIT_JSON_DEPTH)
					.parse(s);
		} catch (net.minidev.json.parser.ParseException e) {
			throw new ParseException("Invalid JSON", e);
		} catch (NullPointerException e) {
			throw new ParseException("The JSON string must not be null", e);
		} catch (Exception e) {
			throw new ParseException("Unexpected exception: " + e.getMessage(), e);
		} catch (Error e) {
			// guard against java.lang.Error instances
			throw new ParseException("Unexpected error: " + e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T to(final Object o, final Class<T> clazz) throws ParseException {
		if (!clazz.isAssignableFrom(o.getClass()))
			throw new ParseException("Unexpected type: " + o.getClass());
		
		return (T) o;
	}
	
	public static boolean toBoolean(final Object o) throws ParseException {
		return to(o, Boolean.class);
	}
	
	public static Number toNumber(final Object o) throws ParseException {
		return to(o, Number.class);
	}
	
	public static String toString(final Object o) throws ParseException {
		return to(o, String.class);
	}
	
	public static List<?> toList(final Object o) throws ParseException {
		return to(o, List.class);
	}
	
	
	/**
	 * Casts an object to a list then returns a string list copy of it
	 * casting each item to a string.
	 * 
	 * @param o The object. Must not be {@code null}.
	 * 
	 * @return The string list.
	 * 
	 * @throws ParseException If the object is not of the expected type.
	 */
	public static List<String> toStringList(final Object o) throws ParseException {
		List<String> stringList = new LinkedList<>();
		try {
			for (Object item : toList(o)) {
				stringList.add((String) item);
			}
		} catch (ClassCastException e) {
			throw new ParseException("Item not a string");
		}
		return stringList;
	}

	private JSONUtils() {}
}
