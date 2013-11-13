package de.topobyte.jterm.core;

public class Pixel
{

	private int flags;
	private char c;

	// Could all be encoded in 'flags'
	private int fg = 16;
	private int bg = 17;
	private boolean highlighted = false;
	private boolean reverse = false;
	private boolean fgBright = false;
	private boolean bgBright = false;

	public Pixel(int flags, char c)
	{
		this.flags = flags;
		this.c = c;
	}

	public int getFlags()
	{
		return flags;
	}

	public char getChar()
	{
		return c;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public void setChar(char c)
	{
		this.c = c;
	}

	public int getFg()
	{
		return fg;
	}

	public void setFg(int fg)
	{
		this.fg = fg;
	}

	public int getBg()
	{
		return bg;
	}

	public void setBg(int bg)
	{
		this.bg = bg;
	}

	public boolean isHighlighted()
	{
		return highlighted;
	}

	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	public boolean isFgBright()
	{
		return fgBright;
	}

	public void setFgBright(boolean fgBright)
	{
		this.fgBright = fgBright;
	}

	public boolean isBgBright()
	{
		return bgBright;
	}

	public void setBgBright(boolean bgBright)
	{
		this.bgBright = bgBright;
	}

	public int getIndexFG()
	{
		int v = reverse ? getIndexBackground() : getIndexForeground();
		if (highlighted && v <= 7) {
			v += 8;
		}
		return v;
	}

	public int getIndexBG()
	{
		int v = reverse ? getIndexForeground() : getIndexBackground();
		return v;
	}

	private int getIndexForeground()
	{
		int v = fg;
		if (v != 16 && fgBright) {
			v += 8;
		}
		return v;
	}

	private int getIndexBackground()
	{
		int v = bg;
		if (v != 17 && bgBright) {
			v += 8;
		}
		return v;
	}
}
