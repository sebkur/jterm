package de.topobyte.jterm.themes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Themes
{

	public static Palette LIGHT = new Palette(
	// @formatter:off
				new Color[] { // default
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
				}
	// @formatter:on
	);

	public static Palette DARK = new Palette(
	// @formatter:off
				new Color[] { // default
							  new Color(0x000000),
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
							  new Color(0xFFFFFF),

							  new Color(0xFFFFFF),
							  new Color(0x000000),
				}
	// @formatter:on
	);

	public static List<Palette> THEMES = new ArrayList<>();
	static {
		THEMES.add(LIGHT);
		THEMES.add(DARK);
	}

}
