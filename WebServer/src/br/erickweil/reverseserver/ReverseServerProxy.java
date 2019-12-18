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
package br.erickweil.reverseserver;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import br.erickweil.webserver.HttpRequest;
import br.erickweil.webserver.HttpResponse;
import br.erickweil.webserver.ReaderWriter;
import br.erickweil.webserver.ServerHttpProxy;
import br.erickweil.webserver.ServerProtocol;

public class ReverseServerProxy extends ServerProtocol{


	int port;
	public boolean secure;
	public long TIMEOUT = 60000;
	public boolean keepAlive;
	public ReverseServerProxy(int port,boolean secure,boolean keepAlive,long timeout) {
		// TODO Auto-generated constructor stub
		this.port = port;
		this.secure = secure;
		this.keepAlive = keepAlive;
		this.TIMEOUT = timeout;
	}
	
	@Override
	public void processRequest() throws IOException {
		// TODO Auto-generated method stub
		//System.out.print("->Processando redirecionamento...\n\n");
		
		ServerSocket serverSocket = new ServerSocket(this.port);
		
		
        Socket clientSocket = null;

        clientSocket = serverSocket.accept();
        System.out.println( " Cliente conectou!\n"+
            		clientSocket.getInetAddress().getHostName()+"("+clientSocket.getInetAddress().getHostAddress()+") port:"+clientSocket.getPort()
            +" -> "+clientSocket.getInetAddress().getLocalHost()+"("+clientSocket.getInetAddress().getLoopbackAddress()+") port:"+serverSocket.getLocalPort());
        
		
		
		DataInputStream socketinput  = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream socketoutput = new DataOutputStream(clientSocket.getOutputStream());

	
		System.out.print("->Aguardando Dados...\n\n");		

		long last_write = -1;
		long last_read = -1;
		long last_readwrite = -1;
		long traffic_count = 0;
		long download_bytes = 0;
		long upload_bytes = 0;
		long last_traffic_count = 0;
		boolean waiting = true;
		try {
			while(true)
			{
				waiting = true;
				

				int inputavailable = input.available();
				//System.out.println("input:"+inputavailable);
				if(inputavailable > 0)
				{
					byte[] read = new byte[input.available()];
					
					traffic_count += read.length;
					download_bytes += read.length;
					input.readFully(read);
					System.out.print(new String(read,Charset.forName("UTF-8")));
					socketoutput.write(read);
					socketoutput.flush();

					last_readwrite = System.currentTimeMillis(); 
					last_read = last_readwrite;
					waiting = false;
				}
				int socketinputavailable = socketinput.available();
				//System.out.println("socketinput:"+socketinputavailable);
				if(socketinput.available() > 0)
				{
					byte[] read = new byte[socketinput.available()];
					
					traffic_count += read.length;
					upload_bytes += read.length;
					socketinput.readFully(read);
					System.out.print(new String(read,Charset.forName("UTF-8")));
					output.write(read);
					output.flush();

					last_readwrite = System.currentTimeMillis();
					last_write = last_readwrite;
					waiting = false;
				}
				//if(!waiting)
				//{
					//System.out.println((System.currentTimeMillis() - last_read)+":"+(System.currentTimeMillis() - last_write));
				//}
				//if(last_traffic_count < traffic_count + 1024)
				//{
				//	System.out.println("KBytes trafegados:"+(int)(traffic_count/1024));
				//	last_traffic_count = traffic_count;
				//}
				//System.out.println("LEU ALGO!");
				Thread.sleep(1);
				//if(last_readwrite > 0 && System.currentTimeMillis() - last_readwrite> TIMEOUT) throw new TimeoutException();
			}
		} 
		//catch(TimeoutException e)
		//{
		//	System.out.println("TIMEOUT");
		//}
		catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println(e.getClass().getName());
		}
		finally {
			clientSocket.close();
			System.out.println(" Download:"+(download_bytes/1024.0)+" Kb   Upload:"+(upload_bytes/1024.0)+" Kb");
			//loginput.flush();
			//loginput.close();
			//logoutput.flush();
			//logoutput.close();
		}
		
		
		//new Proxy(this).get();
	}

}
