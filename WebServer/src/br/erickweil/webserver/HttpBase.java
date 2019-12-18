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

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public abstract class HttpBase {
	public static final String CRLF = "\r\n";
	private static final long TIMEOUT = 15000;
	protected ByteArrayOutputStream writecontent;
	private InsensitiveMap headers;
	public HashMap<String,String> cookies;
	protected byte[] content;
	public String http_version;
	
	public HttpBase()
	{
		headers = new InsensitiveMap();
		http_version = "HTTP/1.1";
	}
	
	private boolean fiminput = false;
    private String readHeaderLine(DataInputStream reader) throws IOException
    {
    	if(fiminput) return null;
    	String line = ReaderWriter.readASCIILine(reader);
    	if(line != null)
    	{
    		if(line.length()==0)
    		{
	    		//input.close();
    			fiminput = true;
	    		return null;
    		}
    	}
    	else
    	{
    		//input.close();
    		fiminput = true;
    	}
    	return line;
    }
    
    
    protected String Transfer_Encoding;
	private void readIdentityContent(DataInputStream reader,int Content_length) throws IOException
    {
		if(Content_length > 0)
		{
			content = new byte[Content_length];
			try {
				reader.readFully(content);
			} catch (EOFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
	// não se sabe o comprimento do conteúdo, vai ler até a conexão fechar ou não ter mais oq ler
    protected void readBlindContent(DataInputStream reader) throws IOException
    {
    	System.out.println("Reading Blind Content!!!");
		try 
		{
	    	int Chunk_Length = -1;
	    	long last_read = System.currentTimeMillis();
	    	DataOutputStream stream = getcontentOutputStream();
	    	
	    	do
	    	{
		    	Chunk_Length = reader.available();
		    	if(Chunk_Length > 0)
		    	{
			    	System.out.print("Available->"+Chunk_Length);
			    	byte[] chunk = new byte[Chunk_Length];
					reader.readFully(chunk);
					stream.write(chunk);
					last_read = System.currentTimeMillis();
					int b = reader.read();
					if(b == -1){ 
						System.out.println(" -> END");
						return;
					}
					System.out.println(" last byte -> ("+b+") : "+(char)b);
					stream.write(b);
		    	}
				Thread.sleep(10);
				if(System.currentTimeMillis() - last_read> TIMEOUT) throw new TimeoutException();
	    	}
	    	while(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println(new String(content,Charset.forName("UTF-8")));
		}
    }
	
    private void readChunkedContent(DataInputStream reader) throws IOException
    {
		/*
		HTTP/1.1 200 OK 
		Content-Type: text/plain 
		Transfer-Encoding: chunked
		
		7\r\n
		Mozilla\r\n 
		9\r\n
		Developer\r\n
		7\r\n
		Network\r\n
		0\r\n 
		\r\n
		 */
		try 
		{
	    	int Chunk_Length = -1;
	    	DataOutputStream stream = getcontentOutputStream();
	    	do
	    	{
		    	Chunk_Length = Integer.parseInt(ReaderWriter.readASCIILine(reader).trim(),16);
		    	System.out.println("Chunk->"+Chunk_Length);
		    	//ReaderWriter.writeASCII(Integer.toHexString(Chunk_Length)+CRLF, stream);
		    	byte[] chunk = new byte[Chunk_Length];
				reader.readFully(chunk);
				stream.write(chunk);
				int r;
				if((r=reader.read()) != ReaderWriter.CR) throw new IOException("CRLF incomplete!"+r);
				if((r=reader.read()) != ReaderWriter.LF) throw new IOException("CRLF incomplete!"+r);
				//ReaderWriter.writeASCII(CRLF, stream);
	    	}
	    	while(Chunk_Length > 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println(new String(content,Charset.forName("UTF-8")));
		}
    }
    
    protected void readHeadersandContent(DataInputStream reader) throws IOException
    {
    	String linha = "";
		while((linha = readHeaderLine(reader)) != null)
		{
			System.out.println(linha);
			String key = linha.substring(0,linha.indexOf(':')).trim();
			String value = linha.substring(linha.indexOf(':')+1,linha.length()).trim();
			addHeader(key, value);
		}
		
		int Content_length = intHeader("Content-Length");
		//Transfer_Encoding = getHeader("Transfer-Encoding") != null ? getHeader("Transfer-Encoding") : "identity";
		if(getHeader("Transfer-Encoding") != null 
				&& getHeader("Transfer-Encoding").contains("chunked"))
		{
			readChunkedContent(reader);
		}
		else
		{
			readIdentityContent(reader,Content_length);
		}
    }
    
    protected void writeHeadersandContent(DataOutputStream writer) throws IOException
    {
		setHeader("Connection","close"); // pq vai fechar a conexão
		
		int Content_length = 0;
		if(writecontent != null)
		{
			Content_length = writecontent.size();
			delHeader("Transfer-Encoding"); // não passa o negócio chunked
		}
		else if(content != null && content.length > 0)
		{
			Content_length = content.length;
		}
		
		if(Content_length >0)
		{
			setHeader("Content-Length", ""+Content_length);
		}
		
		String[] keys = getHeadersKeys();
		for(int i=0;i<keys.length;i++)
		{
			String key = keys[i];
			List<String> values = getHeaderValues(key);
			
			for(String v : values)
			{
				ReaderWriter.writeASCII(key+": "+v+CRLF,writer);
				System.out.println(key+": "+v);
			}
		}
		ReaderWriter.writeASCII(""+CRLF,writer);
		
		if(writecontent != null)
		{
			writecontent.writeTo(writer);
		}
		else if(content != null && content.length > 0)
		{
			writer.write(content);
		}
    }
	
	
    public int intHeader(String name){
		return getHeader(name) != null ? Integer.parseInt(getHeader(name)) : 0;
	}

	public DataOutputStream getcontentOutputStream()
	{
		if(writecontent== null)
		{
			writecontent = new ByteArrayOutputStream(); 
		}
		return new DataOutputStream(writecontent);
	}
	
	public void addHeader(String key,String value)
	{
		headers.addHeader(key, value);
	}
	
	public void setHeader(String key,List<String> value)
	{
		headers.setHeader(key, value);
	}
	
	public void setHeader(String key,String value)
	{
		headers.setHeader(key, value);
	}
	
	public List<String> getHeaderValues(String key)
	{
		return headers.getHeaderValues(key);
	}
	
	public String getHeader(String key)
	{
		return headers.getHeader(key);
	}
	
	public void delHeader(String key)
	{
		headers.delHeader(key);
	}
	
	public int headersLength()
	{
		return headers.header_keys.size();
	}
	
	public String[] getHeadersKeys()
	{
		return headers.getHeadersKeys();
	}
}
