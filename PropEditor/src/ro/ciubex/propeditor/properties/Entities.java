/**
 * This file is part of PropEditor application.
 * 
 * Copyright (C) 2013 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.propeditor.properties;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a replacement for the java.util.Properties class.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Entities implements Cloneable {

	/**
	 * On entities list will be stored all lines from properties file:
	 * properties, comments and empty lines
	 */
	List<Entity> entities;
	private boolean modified;
	int count;

	/**
	 * On the constructor are initialized the lists.
	 */
	public Entities() {
		entities = new ArrayList<Entity>();
		count = 0;
	}

	/**
	 * Removes all elements from both lists, leaving them empty.
	 */
	public void clear() {
		count = 0;
		entities.clear();
	}

	/**
	 * Get the properties list
	 * 
	 * @return The properties list
	 */
	public List<Entity> getProperties() {
		return entities;
	}

	/**
	 * Returns the number of elements in the properties list.
	 * 
	 * @return The number of elements in the properties list.
	 */
	public int size() {
		return count;
	}

	/**
	 * Returns whether the properties list contains no elements.
	 * 
	 * @return true if the properties list has no elements, false otherwise.
	 */
	public boolean isEmpty() {
		return count < 1;
	}

	/**
	 * Returns the entity at the specified location in the properties list.
	 * 
	 * @param location
	 *            The index of the entity to return.
	 * @return The entity at the specified location.
	 */
	public Entity getProperty(int location) {
		Entity entity = null;
		int i = 0, p;
		for (p = 0; p < entities.size(); p++) {
			entity = entities.get(p);
			if (entity.getType() == Type.PROPERTY) {
				if (i == location) {
					break;
				}
				i++;
			}
		}
		return entity;
	}

	/**
	 * Adds the specified entity at the end of the both lists.
	 * 
	 * @param entity
	 *            The entity to add.
	 */
	public void add(Entity entity) {
		if (entity != null) {
			entities.add(entity);
			if (Type.PROPERTY == entity.getType()) {
				count++;
			}
		}
	}

	/**
	 * Removes the first occurrence of the specified entity from the both lists.
	 * 
	 * @param entity
	 *            The entity to remove.
	 */
	public void remove(Entity entity) {
		if (entity != null) {
			entities.remove(entity);
			if (Type.PROPERTY == entity.getType()) {
				count--;
				if (count < 0) {
					count = 0;
				}
			}
		}
	}

	/**
	 * Removes the entity at the specified location from the both lists.
	 * 
	 * @param location
	 */
	public void remove(int location) {
		Entity entity = getProperty(location);
		remove(entity);
	}

	/**
	 * 
	 */
	@Override
	public synchronized Object clone() {
		Entities ent = new Entities();
		for (Entity entity : entities) {
			ent.add(entity);
		}
		return ent;
	}

	/**
	 * Loads properties from the specified InputStream.
	 * 
	 * @param inputStream
	 *            The specified InputStream.
	 * @throws IOException
	 */
	public void load(InputStream inputStream) throws IOException {
		clear();
		load0(new LineReader(inputStream));
	}

	private void load0(LineReader lr) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;
		boolean isCommentLine;

		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;

			precedingBackslash = false;
			isCommentLine = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				// need check if escaped.
				if (c == '#' || c == '!') {
					isCommentLine = true;
					valueStart = keyLen + 1;
				} else if ((c == '=' || c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' || c == '\f')
						&& !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' && c != '\f') {
					if (!hasSep && (c == '=' || c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit
					- valueStart, convtBuf);
			if (isCommentLine) {
				add(new Entity(Type.COMMENT, key, value));
			} else {
				add(new Entity(key, value));
			}
		}
	}

	/**
	 * Read in a "logical line" from an InputStream/Reader, skip all comment and
	 * blank lines and filter out those leading whitespace characters ( , and )
	 * from the beginning of a "natural line". Method returns the char length of
	 * the "logical line" and stores the line in "lineBuf".
	 */
	class LineReader {
		byte[] inByteBuf;
		char[] inCharBuf;
		char[] lineBuf = new char[1024];
		int inLimit = 0;
		int inOff = 0;
		InputStream inStream;
		Reader reader;

		public LineReader(InputStream inStream) {
			this.inStream = inStream;
			inByteBuf = new byte[8192];
		}

		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192];
		}

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = (inStream == null) ? reader.read(inCharBuf)
							: inStream.read(inByteBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0) {
							return -1;
						}
						return len;
					}
				}
				if (inStream != null) {
					// The line below is equivalent to calling a
					// ISO8859-1 decoder.
					c = (char) (0xff & inByteBuf[inOff++]);
				} else {
					c = inCharBuf[inOff++];
				}
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						add(new Entity(Type.EMPTY, "", ""));
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (len == 0) {
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = (inStream == null) ? reader.read(inCharBuf)
								: inStream.read(inByteBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}

	/**
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved
	 * chars to their original forms
	 */
	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding for character: \"" + aChar +
											"\", out: \"" + out + "\"");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}

	/**
	 * Writes the entities properties list (key and element pairs) to the output
	 * character stream. Every entry in the entities properties list is written
	 * out, one per line. For each entry the key string is written, then an
	 * ASCII <code>=</code>, then the associated element string.
	 * 
	 * @param writer
	 *            An output character stream writer.
	 * @throws IOException
	 *             If writing this entities properties list to the specified
	 *             output stream throws an <tt>IOException</tt>.
	 */
	public void store(Writer writer) throws IOException {
		store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer
				: new BufferedWriter(writer), false);
	}

	private void store0(BufferedWriter bw, boolean escUnicode)
			throws IOException {
		synchronized (this) {
			for (Entity entity : entities) {
				switch (entity.getType()) {
				case COMMENT:
					writeComments(bw,
							entity.getKey() + " " + entity.getContent());
					break;
				case EMPTY:
					bw.write(' ');
					bw.newLine();
					break;
				case PROPERTY:
					bw.write(saveConvert(entity.getKey(), true, escUnicode)
							+ "="
							+ saveConvert(entity.getContent(), false,
									escUnicode));
					bw.newLine();
					break;
				}
			}
		}
		bw.flush();
	}

	/**
	 * Write the comment to the file.
	 */
	private static void writeComments(BufferedWriter bw, String comments)
			throws IOException {
		int len = comments.length();
		int current = 0;
		int last = 0;
		char[] uu = new char[6];
		uu[0] = '\\';
		uu[1] = 'u';
		if (comments.charAt(0) != '#') {
			bw.write("#");
		}
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current)
					bw.write(comments.substring(last, current));
				if (c > '\u00ff') {
					uu[2] = toHex((c >> 12) & 0xf);
					uu[3] = toHex((c >> 8) & 0xf);
					uu[4] = toHex((c >> 4) & 0xf);
					uu[5] = toHex(c & 0xf);
					bw.write(new String(uu));
				} else {
					bw.newLine();
					if (c == '\r' && current != len - 1
							&& comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1
							|| (comments.charAt(current + 1) != '#' && comments
									.charAt(current + 1) != '!'))
						bw.write("#");
				}
				last = current + 1;
			}
			current++;
		}
		if (last != current)
			bw.write(comments.substring(last, current));
		bw.newLine();
	}

	/**
	 * Converts unicodes to encoded &#92;uxxxx and escapes special characters
	 * with a preceding slash
	 */
	private String saveConvert(String theString, boolean escapeSpace,
			boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				// outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Check if the entities are modified.
	 * 
	 * @return True if the entities are modified.
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Set the modified entities flag.
	 * 
	 * @param modified
	 *            The modified entities flag.
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
