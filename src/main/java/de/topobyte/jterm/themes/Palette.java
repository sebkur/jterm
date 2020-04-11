package de.topobyte.jterm.themes;

import java.awt.Color;

public class Palette
{

	private Color[] colors;

	public Palette(Color[] colors)
	{
		this.colors = colors;
	}

	public Color getColor(int index)
	{
		return colors[index];
	}

}
