package utils;

import java.util.Collection;

public class CollectionUtils {
	public static boolean isEmpty(final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNotEmpty(final Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}

	public static <T> boolean contains(final Collection<T> collection, final T item) {
		return isNotEmpty(collection) && collection.contains(item);
	}

	public static <T> boolean intersect(final Collection<T> a, final Collection<T> b) {
		if (isEmpty(a) || isEmpty(b)) {
			return false;
		}

		for (T item : a) {
			if (b.contains(item))
				return true;
		}

		return false;
	}

	private CollectionUtils() {
	}
}
