package de.topobyte.jterm;

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
