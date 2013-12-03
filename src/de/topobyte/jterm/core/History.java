package de.topobyte.jterm.core;

public class History extends RingBuffer<Row>
{

	public History(int maximumSize)
	{
		super(Row.class, maximumSize);
	}

	int rowsDismissed = 0;
	int gcCounter = 0;

	private int pos = 0;

	public void push(Row row)
	{
		Row replaced = append(row);
		if (replaced == null) {
			pos += 1;
		} else {
			if (rowsDismissed++ >= 5000) {
				Runtime.getRuntime().gc();
				rowsDismissed = 0;
				gcCounter++;
			}
		}
	}

	public Row pop()
	{
		pos -= 1;
		return removeLast();
	}

	public int getPos()
	{
		return pos;
	}

	public void setPos(int pos)
	{
		this.pos = pos;
	}

}
