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
package br.erickweil.redirectport;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

public class RedirectTCP extends ServerProtocol{

	String host;
	int port;
	public boolean secure;
	public long TIMEOUT = 5000;
    private final Runnable onClientChanged;
	public RedirectTCP(String host,int port,Runnable onClientChanged) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.port = port;
        this.onClientChanged = onClientChanged;
	}
	
	@Override
	public void processRequest() throws IOException {
		// TODO Auto-generated method stub
		//System.out.print("->Processando redirecionamento...\n\n");
        if(onClientChanged!=null)onClientChanged.run();
		Socket proxysocket;
		
		if(secure)
		{
		    proxysocket = SSLSocketFactory.getDefault().createSocket(host, port);
		}
		else
		{
			proxysocket = new Socket(host, port);
		}
		
		String client_hostname = socket.getInetAddress().getHostName();
		String client_hostaddres = socket.getInetAddress().getHostAddress();
		int client_port = socket.getPort();
		//String filename = client_hostname+" p"+client_port+" h"+client_hostname+" n"+new Random(System.currentTimeMillis()-client_port).nextLong();
		//filename = filename.replace(':','.');
		//File login = new File("log/"+filename+".in");
		//File logout = new File("log/"+filename+".out");
		
		//if(!login.getParentFile().exists())
		//{
		//	login.mkdirs();
		//}
		
		//login.createNewFile();
		//logout.createNewFile();
		
		//OutputStream loginput = new FileOutputStream(login);
		//OutputStream logoutput = new FileOutputStream(logout);
		
		DataInputStream socketinput  = new DataInputStream(proxysocket.getInputStream());
		DataOutputStream socketoutput = new DataOutputStream(proxysocket.getOutputStream());

		
		//ReaderWriter.writeASCII("CONNECT kcire.ddns.net:25565 HTTP/1.1\n", socketoutput);
		//ReaderWriter.writeASCII("Host: kcire.ddns.net:25565\n", socketoutput);
		//ReaderWriter.writeASCII("\n", socketoutput);
		
		//socketoutput.flush();
		System.out.print("->Aguardando Dados...\n\n");		
		
		//System.out.println(ReaderWriter.readASCIILine(socketinput)); // ok
		//System.out.println(ReaderWriter.readASCIILine(socketinput)); // ln

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
				
				/*if(last_readwrite == -1)
				{
					int r = socketinput.read();
					if(r != -1)
					{
						output.write(r);
						logoutput.write(r);
					}
					else
					{
						throw new EOFException();
					}
					int r = input.read();
					if(r != -1)
					{
						socketoutput.write(r);
						loginput.write(r);
					}
					
					//last_readwrite = System.currentTimeMillis(); 
					//last_read = last_readwrite;
					//last_write = last_readwrite;
					//waiting = false;
					traffic_count += 1;
					System.out.println("PASSOU DO PRIMEIRO BYTE!");
				}*/
				int inputavailable = input.available();
				//System.out.println("input:"+inputavailable);
				if(inputavailable > 0)
				{
					//System.out.println("LENDO ALGO!");
					byte[] read = new byte[input.available()];
					traffic_count += read.length;
					download_bytes += read.length;
					input.readFully(read);
					socketoutput.write(read);
					socketoutput.flush();
					//loginput.write(read);
					//System.out.println(read.length);
					//System.out.println(new String(read));
					last_readwrite = System.currentTimeMillis(); 
					last_read = last_readwrite;
					waiting = false;
				}
				int socketinputavailable = socketinput.available();
				//System.out.println("socketinput:"+socketinputavailable);
				if(socketinput.available() > 0)
				{
					//System.out.println("ESCREVENDO ALGO!");
					byte[] read = new byte[socketinput.available()];
					traffic_count += read.length;
					upload_bytes += read.length;
					socketinput.readFully(read);
					output.write(read);
					output.flush();
					//logoutput.write(read);
					//System.out.println(read.length);
					//System.out.println(new String(read));
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
				Thread.sleep(10);
				if(last_readwrite > 0 && System.currentTimeMillis() - last_readwrite> TIMEOUT) throw new TimeoutException();
			}
		} 
		catch(TimeoutException e)
		{
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println(e.getClass().getName());
		}
		finally {
			proxysocket.close();
			System.out.println(host+" Download:"+(download_bytes/1024.0)+" Kb   Upload:"+(upload_bytes/1024.0)+" Kb");
			//loginput.flush();
			//loginput.close();
			//logoutput.flush();
			//logoutput.close();
            if(onClientChanged!=null)onClientChanged.run();
		}
		
		
		//new Proxy(this).get();
	}

}
