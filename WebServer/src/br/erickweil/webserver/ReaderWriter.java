/*
 * Copyright (C) 2018 Erick Leonardo Weil
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
package br.erickweil.webserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;

public class ReaderWriter {
	public static final char CR = '\r';
	public static final char LF = '\n';
	public static String readASCIILine(DataInputStream reader) throws IOException
	{
		int b = 0;
		StringBuilder sb = new StringBuilder();
		//while((b = reader.read()) != -1 && b != CR)
		//{
		//	sb.append((char)b);
		//}
        while((b = reader.read()) != -1 && !(b == CR || b == LF))
		{
			sb.append((char)b);
		}
		if(b == -1) throw new EOFException();
		if(b == CR && reader.read() != LF) throw new IOException("CRLF incomplete!");
		return sb.toString();
	}
	public static void writeASCII(String txt,DataOutputStream writer) throws IOException
	{
		byte[] to_write = txt.getBytes(Charset.forName("US-ASCII"));
		writer.write(to_write);
	}
	
	public static void write(String txt, DataOutputStream writer, Charset charset) throws IOException {
		byte[] to_write = txt.getBytes(charset);
		writer.write(to_write);
	}
	
}
