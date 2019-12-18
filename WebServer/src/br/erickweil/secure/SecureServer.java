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
package br.erickweil.secure;

import br.erickweil.webserver.ClientCountListener;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerConnection;
import br.erickweil.webserver.ServerProtocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class SecureServer extends Thread implements ClientCountListener
{

	protected int serverPort = 9999;
	protected SSLServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected ProtocolFactory protocolfactory;
	public int client_count = 0;
    private final SSLContext sslContext;

	public SecureServer(int port, ProtocolFactory protocolfactory,String keystore,String password) 
	{
		this.serverPort = port;
		this.protocolfactory = protocolfactory;
        this.sslContext = null;
		// https://www.sslshopper.com/article-how-to-create-a-self-signed-certificate-using-java-keytool.html
		System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStorePassword", password);
	}
    
    public SecureServer(int port, ProtocolFactory protocolfactory,SSLContext sslContext) 
	{
		this.serverPort = port;
		this.protocolfactory = protocolfactory;
        this.sslContext = sslContext;
	}

	@Override
	public void run() 
	{
		openServerSocket();
		while (!isStopped()) 
		{
			if (client_count > 1000) 
			{
				try 
				{
					while (client_count > 1000) 
					{
						System.out.println("Ainda Tem muitos clientes(" + client_count + "), esperando um pouco");
						Thread.sleep(1000);
					}
				}
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Socket clientSocket = null;
			try 
			{
				clientSocket = this.serverSocket.accept();
				client_count++;
				// System.out.println("NClientes:"+client_count);
				//System.out.println(clientSocket.getInetAddress().getHostName() + "("
				//		+ clientSocket.getInetAddress().getHostAddress() + ") port:" + clientSocket.getPort() + " -> "
				//		+ clientSocket.getInetAddress().getLocalHost() + "("
				//		+ clientSocket.getInetAddress().getLoopbackAddress() + ") port:" + serverSocket.getLocalPort());
			}
			catch (IOException e)
			{
				if (isStopped()) 
				{
					System.out.println("Conexão de cliente não pôde ser iniciada porque Server Parou.");
					return;
				}
				else
				{
					e.printStackTrace();
				}
			}
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
		System.out.println("Server Parou.");
	}

	public synchronized boolean isStopped() 
	{
		return this.isStopped;
	}

	public synchronized void trystop() 
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
			// jeito complicado de carregar o certificado, que dá na mesma
            /*            
			KeyStore ks = KeyStore.getInstance("JKS");
			char[] pass = "1a2b3c4d".toCharArray();
			ks.load(new FileInputStream("kcire.jks"), pass);
			//ks.load(null, "1a2b3c4d".toCharArray());
			
		    Enumeration<String> aliases = ks.aliases();
		    while (aliases.hasMoreElements()) {
		       String alias = aliases.nextElement();
		        //System.out.println("alias certificates :"+alias);
		       if (ks.isKeyEntry(alias)) {
		            ks.getKey(alias, pass);
		        }
		    }

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, pass);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); 
			tmf.init(ks);

			SSLContext sc = SSLContext.getInstance("TLS"); 
			TrustManager[] trustManagers = tmf.getTrustManagers(); 
			sc.init(kmf.getKeyManagers(), trustManagers, null);
			*/
            SSLServerSocketFactory sslserversocketfactory;
            if(sslContext == null)
            {
                sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            }
            else
            {
                sslserversocketfactory = sslContext.getServerSocketFactory();
            }
			this.serverSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(this.serverPort);
			// this.serverSocket = new ServerSocket(this.serverPort);
		} 
		catch (IOException e)
		{
			throw new RuntimeException("Não pôde abrir o server na porta " + serverPort, e);
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