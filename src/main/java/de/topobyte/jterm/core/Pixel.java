package de.topobyte.jterm.core;

public interface Pixel
{

	public abstract char getChar();

	public abstract void setChar(char c);

	public abstract int getFg();

	public abstract void setFg(int fg);

	public abstract int getBg();

	public abstract void setBg(int bg);

	public abstract boolean isHighlighted();

	public abstract void setHighlighted(boolean highlighted);

	public abstract boolean isReverse();

	public abstract void setReverse(boolean reverse);

	public abstract boolean isFgBright();

	public abstract void setFgBright(boolean fgBright);

	public abstract boolean isBgBright();

	public abstract void setBgBright(boolean bgBright);

	public abstract int getIndexFG();

	public abstract int getIndexBG();

}