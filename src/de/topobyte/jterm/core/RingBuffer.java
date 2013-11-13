package de.topobyte.jterm.core;

import java.lang.reflect.Array;

public class RingBuffer<T>
{

	private T[] buffer;
	private int start = 0;
	private int length = 0;

	public RingBuffer(Class<T> c, int maximumSize)
	{
		buffer = (T[]) Array.newInstance(c, maximumSize);
	}

	public int getLength()
	{
		return length;
	}

	public T get(int i)
	{
		int pos = (start + i) % buffer.length;
		return (T) buffer[pos];
	}

	public T prepend(T data)
	{
		int pos = start == 0 ? buffer.length - 1 : start - 1;
		T old = null;
		if (length == buffer.length) {
			old = (T) buffer[pos];
		} else {
			length += 1;
		}
		buffer[pos] = data;
		start = pos;
		return old;
	}

	public T append(T data)
	{
		int pos = (start + length) % buffer.length;
		T old = null;
		if (length == buffer.length) {
			old = (T) buffer[pos];
			start = (start + 1) % buffer.length;
		} else {
			length += 1;
		}
		buffer[pos] = data;
		return old;
	}

	public T removeFirst()
	{
		T data = (T) buffer[start];
		start = (start + 1) % buffer.length;
		length -= 1;
		return data;
	}

	public T removeLast()
	{
		int pos = (start + length - 1) % buffer.length;
		T data = (T) buffer[pos];
		length -= 1;
		return data;
	}
}
