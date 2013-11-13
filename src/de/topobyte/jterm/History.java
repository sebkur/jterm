package de.topobyte.jterm;

import java.util.ArrayList;
import java.util.List;

public class History
{

	private List<Row> rows = new ArrayList<Row>();

	public int getLength()
	{
		return rows.size();
	}

	public Row get(int i)
	{
		return rows.get(i);
	}

	public void push(Row row)
	{
		rows.add(row);
	}

	public Row pop()
	{
		return rows.remove(rows.size() - 1);
	}

}
