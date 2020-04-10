package de.topobyte.jterm.core;

public class Cursor
{

	private int col = 1;
	private int row = 1;

	public Cursor(int row, int col)
	{
		this.row = row;
		this.col = col;
	}

	public int getCol()
	{
		return col;
	}

	public void setCol(int col)
	{
		this.col = col;
	}

	public int getRow()
	{
		return row;
	}

	public void setRow(int row)
	{
		this.row = row;
	}

}
