package de.topobyte.jterm.core;

import java.awt.Color;

public class Palette
{

	// @formatter:off
	private Color[] colors = new Color[] { // default
			new Color(0xFFFFFF),
			new Color(0xAA0000),
			new Color(0x00AA00),
			new Color(0xAA5500),
			new Color(0x0000AA),
			new Color(0xAA00AA),
			new Color(0x00AAAA),
			new Color(0xAAAAAA),

			new Color(0x555555),
			new Color(0xFF5555),
			new Color(0x55FF55),
			new Color(0xFFFF55),
			new Color(0x5555FF),
			new Color(0xFF55FF),
			new Color(0x55FFFF),
			new Color(0x000000),

			new Color(0x000000),
			new Color(0xFFFFFF),
		};
	// @formatter:on

	public Color getColor(int index)
	{
		return colors[index];
	}

}
