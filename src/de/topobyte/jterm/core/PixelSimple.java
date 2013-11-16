package de.topobyte.jterm.core;

public class PixelSimple implements Pixel
{

	private char c;

	private int fg = 16;
	private int bg = 17;
	private boolean highlighted = false;
	private boolean reverse = false;
	private boolean fgBright = false;
	private boolean bgBright = false;

	public PixelSimple(char c)
	{
		this.c = c;
	}

	@Override
	public char getChar()
	{
		return c;
	}

	@Override
	public void setChar(char c)
	{
		this.c = c;
	}

	@Override
	public int getFg()
	{
		return fg;
	}

	@Override
	public void setFg(int fg)
	{
		this.fg = fg;
	}

	@Override
	public int getBg()
	{
		return bg;
	}

	@Override
	public void setBg(int bg)
	{
		this.bg = bg;
	}

	@Override
	public boolean isHighlighted()
	{
		return highlighted;
	}

	@Override
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}

	@Override
	public boolean isReverse()
	{
		return reverse;
	}

	@Override
	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	@Override
	public boolean isFgBright()
	{
		return fgBright;
	}

	@Override
	public void setFgBright(boolean fgBright)
	{
		this.fgBright = fgBright;
	}

	@Override
	public boolean isBgBright()
	{
		return bgBright;
	}

	@Override
	public void setBgBright(boolean bgBright)
	{
		this.bgBright = bgBright;
	}

	@Override
	public int getIndexFG()
	{
		int v = reverse ? getIndexBackground() : getIndexForeground();
		if (highlighted && v <= 7) {
			v += 8;
		}
		return v;
	}

	@Override
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

	@Override
	public int hashCode()
	{
		return c + fg * 32 + bg * 16 + (highlighted ? 8 : 0)
				+ (reverse ? 4 : 0) + (fgBright ? 2 : 0) + (bgBright ? 1 : 0);
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof PixelSimple)) {
			return false;
		}
		PixelSimple o = (PixelSimple) other;
		return o.c == c && o.fg == fg && o.bg == bg
				&& o.highlighted == highlighted
				&& o.reverse == reverse && o.fgBright == fgBright
				&& o.bgBright == bgBright;
	}
}
