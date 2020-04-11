package de.topobyte.jterm.core;

public class PixelDense implements Pixel
{

	private char c;
	private int flags; // saves colours and switches

	/* @formatter:off

      flags bits (from the right):
             5-0 : bg-colour
            11-6 : fg-colour
              13 : bg_bright
              14 : fg_bright
              15 : reverse
              16 : highlighted
    */

	private final static int PIXEL_FLAG_HIGHLIGHTED = 0x8000;
	private final static int PIXEL_FLAG_REVERSE =     0x4000;
	private final static int PIXEL_FLAG_FG_BRIGHT =   0x2000;
	private final static int PIXEL_FLAG_BG_BRIGHT =   0x1000;

	// @formatter:on

	public PixelDense(char c)
	{
		this.c = c;
		setFg(16);
		setBg(17);
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

	private void setFlag(int BITMASK, boolean value)
	{
		if (value) {
			flags |= BITMASK;
		} else {
			flags &= ~BITMASK;
		}
	}

	@Override
	public int getFg()
	{
		return (flags & 0x03E0) >> 5;
	}

	@Override
	public void setFg(int fg)
	{
		flags &= ~0x03E0;
		flags |= (fg << 5);
	}

	@Override
	public int getBg()
	{
		return flags & 0x001F;
	}

	@Override
	public void setBg(int bg)
	{
		flags &= ~0x001F;
		flags |= bg;
	}

	@Override
	public boolean isHighlighted()
	{
		return (flags & PIXEL_FLAG_HIGHLIGHTED) != 0;
	}

	@Override
	public void setHighlighted(boolean highlighted)
	{
		setFlag(PIXEL_FLAG_HIGHLIGHTED, highlighted);
	}

	@Override
	public boolean isReverse()
	{
		return (flags & PIXEL_FLAG_REVERSE) != 0;
	}

	@Override
	public void setReverse(boolean reverse)
	{
		setFlag(PIXEL_FLAG_REVERSE, reverse);
	}

	@Override
	public boolean isFgBright()
	{
		return (flags & PIXEL_FLAG_FG_BRIGHT) != 0;
	}

	@Override
	public void setFgBright(boolean fgBright)
	{
		setFlag(PIXEL_FLAG_FG_BRIGHT, fgBright);
	}

	@Override
	public boolean isBgBright()
	{
		return (flags & PIXEL_FLAG_BG_BRIGHT) != 0;
	}

	@Override
	public void setBgBright(boolean bgBright)
	{
		setFlag(PIXEL_FLAG_BG_BRIGHT, bgBright);
	}

	@Override
	public int getIndexFG()
	{
		int v = isReverse() ? getIndexBackground() : getIndexForeground();
		if (isHighlighted() && v <= 7) {
			v += 8;
		}
		return v;
	}

	@Override
	public int getIndexBG()
	{
		int v = isReverse() ? getIndexForeground() : getIndexBackground();
		return v;
	}

	private int getIndexForeground()
	{
		int v = getFg();
		if (v != 16 && isFgBright()) {
			v += 8;
		}
		return v;
	}

	private int getIndexBackground()
	{
		int v = getBg();
		if (v != 17 && isBgBright()) {
			v += 8;
		}
		return v;
	}

	@Override
	public int hashCode()
	{
		return c + flags * 1000;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof PixelDense)) {
			return false;
		}
		PixelDense o = (PixelDense) other;
		return o.c == c && o.flags == flags;
	}

}
