package de.topobyte.jterm.core;

import java.util.HashMap;
import java.util.Map;

public class Charmap
{

	/*
	 * DEC Special Character and Line Drawing Set. VT100 and higher (per XTerm
	 * docs).
	 */
	private static char[][] iso2022_map = new char[][] { //
			{ 96, 0x25c6 }, /* diamond */
			{ 'a', 0x2592 }, /* checkerboard */
			{ 'b', 0x2409 }, /* HT symbol */
			{ 'c', 0x240c }, /* FF symbol */
			{ 'd', 0x240d }, /* CR symbol */
			{ 'e', 0x240a }, /* LF symbol */
			{ 'f', 0x00b0 }, /* degree */
			{ 'g', 0x00b1 }, /* plus/minus */
			{ 'h', 0x2424 }, /* NL symbol */
			{ 'i', 0x240b }, /* VT symbol */
			{ 'j', 0x2518 }, /* downright corner */
			{ 'k', 0x2510 }, /* upright corner */
			{ 'l', 0x250c }, /* upleft corner */
			{ 'm', 0x2514 }, /* downleft corner */
			{ 'n', 0x253c }, /* cross */
			{ 'o', 0x23ba }, /* scan line 1/9 */
			{ 'p', 0x23bb }, /* scan line 3/9 */
			{ 'q', 0x2500 }, /* horizontal line (also scan line 5/9) */
			{ 'r', 0x23bc }, /* scan line 7/9 */
			{ 's', 0x23bd }, /* scan line 9/9 */
			{ 't', 0x251c }, /* left t */
			{ 'u', 0x2524 }, /* right t */
			{ 'v', 0x2534 }, /* bottom t */
			{ 'w', 0x252c }, /* top t */
			{ 'x', 0x2502 }, /* vertical line */
			{ 'y', 0x2264 }, /* <= */
			{ 'z', 0x2265 }, /* >= */
			{ '{', 0x03c0 }, /* pi */
			{ '|', 0x2260 }, /* not equal */
			{ '}', 0x00a3 }, /* pound currency sign */
			{ '~', 0x00b7 }, /* bullet */
	};

	private static Map<Character, Character> lookup = new HashMap<>();

	static {
		for (char[] mapping : iso2022_map) {
			lookup.put(mapping[0], mapping[1]);
		}
	}

	public static Character get(char c)
	{
		return lookup.get(c);
	}

}
