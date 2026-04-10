package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapUtils {

	public static boolean isEmpty(final Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(final Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}

	// Multi-valued Maps

	/**
	 * Converts specified multi-valued map to a single-valued map by taking the
	 * first value in the list.
	 * 
	 * @param map The multi-valued map, {@code null} if not specified.
	 * 
	 * @return The single-valued map, {@code null} if no map was specified.
	 */
	public static <K, V> Map<K, V> toSingleValuedMap(final Map<K, List<V>> map) {

		if (map == null)
			return null;

		Map<K, V> out = new HashMap<>();

		for (Map.Entry<K, List<V>> en : map.entrySet()) {

			if (en.getValue() == null || en.getValue().isEmpty()) {
				out.put(en.getKey(), null);
			} else {
				out.put(en.getKey(), en.getValue().get(0));
			}
		}
		return out;
	}

	/**
	 * Gets the first value for the specified key.
	 */
	public static <K, V> V getFirstValue(final Map<K, List<V>> map, final K key) {
		List<V> valueList = map.get(key);
		if (valueList == null || valueList.isEmpty()) {
			return null;
		}
		return valueList.get(0);
	}

	/**
	 * Returns the keys with more than one distinct value. Keys that map to two or
	 * more identical values are treated as single-valued.
	 */
	public static <K, V> Set<K> getKeysWithMoreThanOneValue(final Map<K, List<V>> map, final Set<K> excepted) {
		if (isEmpty(map)) {
			return Collections.emptySet();
		}

		Set<K> found = new HashSet<>();
		for (Map.Entry<K, List<V>> en : map.entrySet()) {
			if (CollectionUtils.contains(excepted, en.getKey())) {
				continue;
			}

			Set<V> entryValues = new HashSet<>(en.getValue());

			if (CollectionUtils.isNotEmpty(entryValues) && entryValues.size() > 1) {
				found.add(en.getKey());
			}
		}
		return found;

	}

	private MapUtils() {
	}
}
