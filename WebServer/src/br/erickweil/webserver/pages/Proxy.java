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
package br.erickweil.webserver.pages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;

import br.erickweil.webserver.ServerHttp;
import br.erickweil.webserver.ServerPage;

public class Proxy extends ServerPage{

	public Proxy(ServerHttp server) {
		super(server);
		// TODO Auto-generated constructor stub
	}

	public boolean fiminput = false;
    protected String readLine(BufferedReader input) throws IOException
    {
    	if(fiminput) return null;
    	String line = input.readLine();
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
    
    private int GetContentLength(BufferedReader input) throws NumberFormatException, IOException
    {
		String linha = "";
		int Content_length = 0;
		while((linha = readLine(input)) != null)
		{
			echo(linha+"\n");
			String key = linha.substring(0,linha.indexOf(':')).trim();
			String value = linha.substring(linha.indexOf(':')+1,linha.length()).trim();
			if(key.equals("Content-Length"))
				Content_length = Integer.parseInt(value);
		}
		echo("\n");
		return Content_length;
    }
    
    public static byte[] toByteArray(InputStream is,int length) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[length];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
                b = new byte[length-n];
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
	
	@Override
	public void get() throws IOException {
		// TODO Auto-generated method stub
		/*
		Socket proxysocket = null;
		try 
		{
			proxysocket = new Socket("marmitasads.hol.es", 80);
			BufferedReader input  = new BufferedReader(new InputStreamReader(proxysocket.getInputStream(),server.charset));
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(proxysocket.getOutputStream(),server.charset));
			
			System.out.println("escrevendo no proxy...");
			//output.write("GET /marmitaria/verpedido.php HTTP/1.1\n");
			
			String EntireRequest = server.EntireRequest_string.replace("Host: localhost:8080", "Host: marmitasads.hol.es");
			
			//EntireRequest = EntireRequest.replace("Accept: ^^/^^", "Accept: text/html");
			//EntireRequest = EntireRequest.replace("Accept-Encoding: gzip, deflate, sdch", "Accept-Encoding: identity");
			
			output.write(EntireRequest);
			String linha = "";
			output.flush();
			
			
			String Status_line = input.readLine();
			echo(Status_line);
			
			int Content_length = GetContentLength(input);
			
			System.out.println("esperando resposta...");
			//byte[] content = new byte[Content_length];
			
			//proxysocket.getInputStream().read(content);
			linha = "";
			
			//echo(input.readLine());
			
			char[] bytearray = new char[Content_length];
			//InputStream rawinput = proxysocket.getInputStream();
			for(int i=0;i<bytearray.length;i++)
			{
				int b = input.read();
				bytearray[i] = (char)(b);
			}
			
			//byte[] bytearray = toByteArray(proxysocket.getInputStream(),Content_length);
			
			String content = new String(bytearray);
			echo(content);
			//while((linha = input.readLine()) != null)
			//{
			//	echo(linha);
			//}
			
			//echo(new String(content,server.charset));
			System.out.println("Ok.");
			//int Content_Length = GetContentLength(input);
			
			proxysocket.close();
		}
		catch (IOException e) {
		    System.out.println(e);
		}
		*/

	}



}
