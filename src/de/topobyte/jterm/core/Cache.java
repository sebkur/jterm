package de.topobyte.jterm.core;

import java.util.HashMap;
import java.util.Map;

import de.topobyte.misc.adt.list.linked.UniqueLinkedList;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the value type.
 */
public class Cache<K, V>
{

	private int size;
	private UniqueLinkedList<K> keys = new UniqueLinkedList<K>();
	private Map<K, V> map = new HashMap<K, V>();

	/**
	 * Create a memory cache.
	 * 
	 * @param size
	 *            the number of elements to store.
	 */
	public Cache(int size)
	{
		this.size = size;
	}

	/**
	 * Put key, value into the cache.
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the value.
	 * @return the key removed or null
	 */
	public Entry put(K key, V value)
	{
		if (map.containsKey(key)) {
			map.put(key, value);
		} else {
			map.put(key, value);
			keys.addFirst(key);
			if (keys.size() > size) {
				K removedKey = keys.removeLast();
				V removedValue = map.remove(removedKey);
				return new Entry(removedKey, removedValue);
			}
		}
		return null;
	}

	/**
	 * Get the stored element.
	 * 
	 * @param key
	 *            the key to retrieve a value for.
	 * @return the value.
	 */
	public V get(K key)
	{
		if (map.containsKey(key))
			return map.get(key);
		return null;
	}

	/**
	 * Remove and get the stored element if any.
	 * 
	 * @param key
	 *            the key to remove the value for.
	 * @return the removed element or null.
	 */
	public V remove(K key)
	{
		if (map.containsKey(key)) {
			return map.remove(key);
		}
		return null;
	}

	/**
	 * Reorder the key within the replacement list.
	 * 
	 * @param key
	 *            the key to reorder.
	 */
	public void refresh(K key)
	{
		if (!map.containsKey(key)) {
			return;
		}
		keys.moveToFront(key);
	}

	/**
	 * Clear this cache. Removes all elements.
	 */
	public void clear()
	{
		keys.clear();
		map.clear();
	}

	/**
	 * Return the size of this cache.
	 * 
	 * @return the number of elements stored in this cache.
	 */
	public int size()
	{
		return keys.size();
	}

	public class Entry
	{
		private K key;
		private V value;

		public Entry(K key, V value)
		{
			this.key = key;
			this.value = value;
		}

		public K getKey()
		{
			return key;
		}

		public V getValue()
		{
			return value;
		}
	}
}
