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

import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

import java.io.IOException;
import java.net.InetAddress;

public class WebServer implements Runnable, ClientCountListener{

    protected int          serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected ProtocolFactory protocolfactory;
    public int client_count = 0;
    public InetAddress _bind_address;
    public WebServer(int port,ProtocolFactory protocolfactory){
        this.serverPort = port;
        this.protocolfactory = protocolfactory;
    }

    public void run()
    {
        openServerSocket();
        while(! isStopped())
        {
        	if(client_count > 5000) 
        	{
        		try {
					while(client_count > 5000) 
		        	{
						System.out.println("Ainda Tem muitos clientes("+client_count+"), esperando um pouco");
						Thread.sleep(1000);
		        	}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
        	}
            Socket clientSocket = null;
            try 
            {
                clientSocket = this.serverSocket.accept();
                onConnected();
                //System.out.println("NClientes:"+client_count);
                //System.out.println(
                //		clientSocket.getInetAddress().getHostName()+"("+clientSocket.getInetAddress().getHostAddress()+") port:"+clientSocket.getPort()
                //+" -> "+clientSocket.getInetAddress().getLocalHost()+"("+clientSocket.getInetAddress().getLoopbackAddress()+") port:"+serverSocket.getLocalPort());
            }
            catch (IOException e) 
            {
                if(isStopped())
                {
                    System.out.println("Conexão de cliente não pôde ser iniciada porque Server Parou.") ;
                    return;
                }
                else
                {
                	e.printStackTrace();
                }
            }
            //new Thread(new ServerConnection(protocolfactory.get(),clientSocket, "Kcire Server")).start();
            System.out.print("creating");
            ServerProtocol protocol = protocolfactory.get();
            System.out.print(".");
            ServerConnection serverconn = new ServerConnection(this,protocol, clientSocket, "Kcire Server");
            System.out.print("..");
			Thread th = new Thread(serverconn);
            System.out.print("...");
            th.start();
            System.out.println(" done!");
        }
        client_count = 0;
        System.out.println("Server Parou.");
    }


    protected synchronized boolean isStopped() 
    {
        return this.isStopped;
    }
    
    public synchronized int getPort()
    {
        return this.serverSocket.getLocalPort();
    }

    public synchronized void stop()
    {
        this.isStopped = true;
        try 
        {
            this.serverSocket.close();
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Erro ao parar o servidor", e);
        }
    }

    protected void openServerSocket() 
    {
        try 
        {
            if(_bind_address != null)
            {
                //https://stackoverflow.com/questions/14976867/how-can-i-bind-serversocket-to-specific-ip?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                this.serverSocket = new ServerSocket(this.serverPort,0,_bind_address);
            }
            else
            {
                this.serverSocket = new ServerSocket(this.serverPort);
            
            }
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Não pôde abrir o server na porta "+serverPort, e);
        }
    }

    @Override
    public synchronized void onDisconnected() {
        client_count--;
    }

    @Override
    public synchronized void onConnected() {
        client_count++;
    }
    
    @Override
    public synchronized int getClientCount() {
        return client_count;
    }

}
