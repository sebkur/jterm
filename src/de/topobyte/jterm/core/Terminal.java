package de.topobyte.jterm.core;

public class Terminal
{

	private int mfd = 0;

	private int cols = 80;
	private int rows = 24;

	public native void test();

	public native String testStringCreation();

	public native void write(String message);

	public native void start();

	public native byte[] read();

	public native void setSize(int width, int height);

	public void printInfo()
	{
		System.out.println("MFD: " + mfd);
	}

	public int getNumberOfCols()
	{
		return cols;
	}

	public int getNumberOfRows()
	{
		return rows;
	}

	public void setNumberOfCols(int cols)
	{
		this.cols = cols;
	}

	public void setNumberOfRows(int rows)
	{
		this.rows = rows;
	}
}
