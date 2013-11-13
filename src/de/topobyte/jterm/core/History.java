package de.topobyte.jterm.core;

public class History extends RingBuffer<Row>
{

	public History(int maximumSize)
	{
		super(Row.class, maximumSize);
	}

	public void push(Row row)
	{
		append(row);
	}

	public Row pop()
	{
		return removeFirst();
	}

}
