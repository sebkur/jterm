package de.topobyte.jterm.core;

public class History extends RingBuffer<Row>
{

	public History(int maximumSize)
	{
		super(Row.class, maximumSize);
	}

	int rowsDismissed = 0;
	int gcCounter = 0;

	public void push(Row row)
	{
		Row replaced = append(row);
		if (replaced != null) {
			if (rowsDismissed++ >= 5000) {
				Runtime.getRuntime().gc();
				rowsDismissed = 0;
				gcCounter++;
			}
		}
	}

	public Row pop()
	{
		return removeFirst();
	}

}
