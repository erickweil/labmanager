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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class HttpRequest extends HttpBase{
	public String method;
	public String uri;


	public HttpRequest()
	{
		method = "GET";
		uri = "/";
	}
    
	public boolean buildfromInputStream(DataInputStream reader) throws IOException
	{
		String Request_line = null;
		try {
			Request_line = ReaderWriter.readASCIILine(reader);
		} catch (EOFException e1) {
			// TODO Auto-generated catch block
			System.out.println("EOF");
            
			return false;
		}
		if(LOG)System.out.println(Request_line);
		String[] Request_split = Request_line.split(" ");
		method = Request_split[0];
		uri = Request_split[1];
		http_version = Request_split[2];
		
		readHeadersandContent(reader);
		
		//if(getHeader("Cookie")!=null)
		//{
		//	cookies = Cookie.decodeCookies(getHeader("Cookie"));
		//}
		
		return true;
	}
	

	public void writeIntoOutputStream(DataOutputStream writer) throws IOException
	{
		ReaderWriter.writeASCII(method+" "+uri+" "+http_version+CRLF,writer);
		if(LOG)System.out.println(method+" "+uri+" "+http_version);
		//if(cookies!=null)
		//{
		//	setHeader("Cookie", Cookie.encodeCookies(cookies));
		//}
		
		writeHeadersandContent(writer);
	}

}
