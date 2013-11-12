package de.topobyte.jterm;

import java.util.ArrayList;
import java.util.List;

public class Screen
{

	// Current position
	private int ccol = 1;
	private int crow = 1;

	private List<Row> rows = new ArrayList<Row>();

	public Screen()
	{
	}

	public List<Row> getRows()
	{
		return rows;
	}

	public int getCurrentColumn()
	{
		return ccol;
	}

	public int getCurrentRow()
	{
		return crow;
	}

	public void setCurrentColumn(int ccol)
	{
		this.ccol = ccol;
	}

	public void setCurrentRow(int crow)
	{
		this.crow = crow;
	}

}
