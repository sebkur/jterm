package de.topobyte.jterm;

public class Pixel {

	private int flags;
	private char c;

	public Pixel(int flags, char c) {
		this.flags = flags;
		this.c = c;
	}

	public int getFlags() {
		return flags;
	}

	public char getChar() {
		return c;
	}

}