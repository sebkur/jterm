package de.topobyte.jterm.core;

public class Terminal
{

	private int pid = 0;
	private int mfd = 0;

	private int cols = 80;
	private int rows = 24;

	public native void test();

	public native String testStringCreation();

	public native void write(String message);

	public native void start(String pwd);

	public native byte[] read();

	public native void setSize(int width, int height);

	public native byte getEraseCharacter();

	public native String getPwd();

	public void printInfo()
	{
		System.out.println("PTY PID: " + pid + ", MFD: " + mfd);
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
