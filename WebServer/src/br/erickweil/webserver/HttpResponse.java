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


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class HttpResponse extends HttpBase{
	public String status_code;
	public String reason_frase;

	public HttpResponse()
	{
		status_code = "200";
		reason_frase = "OK";
	}
	
	public boolean buildfromInputStream(DataInputStream reader) throws IOException
	{
		String Status_line = ReaderWriter.readASCIILine(reader);
		if(LOG)System.out.println(Status_line);
		String[] Status_split = Status_line.split(" ");
		if(Status_split == null || Status_split.length==0)
		{
			throw new IOException("Bad Status line:"+Status_line);
		}
		http_version =  Status_split[0];
		status_code = Status_split.length > 1 ? Status_split[1] : null;
		reason_frase = Status_split.length > 2 ? Status_split[2] : null;
		
		readHeadersandContent(reader);
		
		boolean contains_content_length = getHeader("Content-Length") != null;
		if( (content == null || content.length == 0) && (writecontent == null || writecontent.size() == 0) &&
			!contains_content_length && status_code.equals("200") && getHeader("Content-Type") != null
			&& !(getHeader("Transfer-Encoding") != null && getHeader("Transfer-Encoding").equalsIgnoreCase("chunked"))
		)
		{
			readBlindContent(reader);
		}
		
		return true;
	}

	public void writeIntoOutputStream(DataOutputStream writer) throws IOException
	{
		String Response_status = http_version+" "+status_code+" "+reason_frase; 
		if(LOG)System.out.println(Response_status);
		ReaderWriter.writeASCII(Response_status+CRLF,writer);
		
		if(headersLength() == 0)
		{
			//Sun, 18 Oct 2012 10:36:20 GMT	
			SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date();
			String currentDate = format.format(date);
			setHeader("Date",currentDate+" GMT");
			setHeader("Server","Kcire/1.0 (Win64)");
		}
		
		writeHeadersandContent(writer);
	}
	
	public void writeOnlyContentIntoOutputStream(DataOutputStream writer) throws IOException
	{
		if(writecontent != null)
		{
			writecontent.writeTo(writer);
		}
		else if(content != null && content.length > 0)
		{
			writer.write(content);
		}
	}
	
    public String getResponseAsText() throws IOException
    {
        ByteArrayOutputStream respBytes = new ByteArrayOutputStream();
        writeOnlyContentIntoOutputStream(new DataOutputStream(respBytes));
        return new String(respBytes.toByteArray(),Charset.forName("UTF-8"));
    }
}
