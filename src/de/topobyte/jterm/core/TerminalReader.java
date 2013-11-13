package de.topobyte.jterm.core;

import java.nio.charset.Charset;

public abstract class TerminalReader implements Runnable
{
	private Terminal terminal;

	public TerminalReader(Terminal terminal)
	{
		this.terminal = terminal;
	}

	public abstract void chunkHandled();

	public abstract void handleAscii(byte c);

	public abstract void handleUtf8(char c);

	@Override
	public void run()
	{
		while (true) {
			byte[] bytes = terminal.read();
			if (bytes == null) {
				break;
			}
			parseBytes(bytes);
			chunkHandled();
		}
	}

	byte[] unhandled = null;

	protected void parseBytes(byte[] bytes)
	{
		/* if we got some data left, prepend it. */
		byte[] available = bytes;
		if (unhandled != null) {
			available = new byte[unhandled.length + bytes.length];
			System.arraycopy(unhandled, 0, available, 0, unhandled.length);
			System.arraycopy(bytes, 0, available, unhandled.length,
					bytes.length);
			unhandled = null;
		}

		/* now parse it, unichar by unichar */
		int p = 0;
		while (p < available.length) {
			int used = parseChar(available, p);
			if (used != 0) { // a char has been parsed successfully
				p += used;
			} else { // end of buffer reached, no valid char
				int leftover = available.length - p;
				unhandled = new byte[leftover];
				System.arraycopy(available, p, unhandled, 0, leftover);
				break;
			}
		}
	}

	private int parseChar(byte[] bytes, int p)
	{
		byte b = bytes[p];
		boolean ascii = (b & 0x80) == 0x00;
		if (ascii) {
			parseAscii(b);
			return 1;
		}

		int followers = 0;
		boolean bytes2 = (b & 0xE0) == 0xC0;
		boolean bytes3 = (b & 0xF0) == 0xE0;
		boolean bytes4 = (b & 0xF8) == 0xF0;

		if (bytes2) {
			followers = 1;
		} else if (bytes3) {
			followers = 2;
		} else if (bytes4) {
			followers = 3;
		} else {
			return 1;
		}

		int checked = checkSanity(bytes, p, followers);
		if (checked == -1) {
			parseUtf8(bytes, p, followers + 1);
			return followers + 1;
		}
		if (checked == 0) {
			return 0;
		}
		return checked;
	}

	/*
	 * UTF8-helper
	 * 
	 * Work on the byte sequence buffer, starting at position buffer[offset].
	 * Check the next 'check' bytes for UTF8-fitness.
	 */
	private int checkSanity(byte[] buffer, int offset, int check)
	{
		// System.out.println("buffer length: " + buffer.length
		// + ", offset: " + offset
		// + ", check: " + check);
		if (offset + check + 1 > buffer.length) {
			return 0;
		}
		for (int i = 0; i < check; i++) {
			byte x = buffer[offset + i + 1];
			if ((x & 0xC0) != 0x80) {
				return check;
			}
		}
		return -1;
	}

	private void parseAscii(byte b)
	{
		handleAscii(b);
	}

	private void parseUtf8(byte[] bytes, int p, int numBytes)
	{
		String string = new String(bytes, p, numBytes, Charset.forName("utf8"));
		if (string.length() < 0) {
			return;
		}
		char c = string.toCharArray()[0];
		handleUtf8(c);
	}

}
