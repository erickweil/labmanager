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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public abstract class ServerProtocol {
	public DataInputStream input;
	public DataOutputStream output; 
	public Charset charset;
	public Socket socket;
    public int timeout = 60000;
	
	public abstract void processRequest() throws IOException;
    
    protected void writeln_ascii(String msg) throws UnsupportedEncodingException, IOException
    {
    	ReaderWriter.writeASCII(msg+"\n", output);
    }
    
    protected String readln_ascii() throws UnsupportedEncodingException, IOException
    {
     	return ReaderWriter.readASCIILine(input);
    }
    
    protected void writeln_url(String msg) throws UnsupportedEncodingException, IOException
    {
    	ReaderWriter.writeASCII(URLEncoder.encode(msg,"UTF-8")+"\n", output);
    }
    
    protected String readln_url() throws UnsupportedEncodingException, IOException
    {
    	String txt = ReaderWriter.readASCIILine(input);
    	if(txt == null || txt.isEmpty()) return txt;
    	return URLDecoder.decode(txt,"UTF-8");
    }
    
    public int getTimeout()
    {
        return timeout;
    }
    
    protected boolean repeat()
    {
        return false;
    }
}
