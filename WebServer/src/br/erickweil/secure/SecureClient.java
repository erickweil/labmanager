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

import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SecureClient implements Runnable {
    
    int port;
    String host;
    public SSLSocket socket;
    protected ProtocolFactory protocolfactory;
    public boolean LOG = true;
    public boolean disableCert = false;
    public SecureClient(String host,int port, ProtocolFactory protocolfactory, String keystore,String password) 
    {
        this.port = port;
        this.host = host;
        this.protocolfactory = protocolfactory;
        
        if(keystore != null)
        {
            // https://www.sslshopper.com/article-how-to-create-a-self-signed-certificate-using-java-keytool.html
            System.setProperty("javax.net.ssl.trustStore", keystore);
            System.setProperty("javax.net.ssl.trustStorePassword", password);
        }
    }
    
    public void disableCertValidation()
    {
        this.disableCert = true;    
    }
    
    @Override
    public void run()
    {
        try {
            connect();
            
            new ServerConnection(null,protocolfactory.get(), socket, "Kcire Server").run();
            
            close();
        } catch (SocketException | UnknownHostException e) {
            if(LOG)
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void connect() throws IOException {    
        SSLSocketFactory sslsocketfactory;
        if(disableCert) 
        {
            SSLContext sc;
            try {
                sc = SSLContext.getInstance("SSL");
            
    
            TrustManager[] trustAllCerts = new TrustManager[] {
               new X509TrustManager() {
                  public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                  }

                  public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                  public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

               }
            };
            
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            sslsocketfactory = sc.getSocketFactory();
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException(ex.getMessage(),ex);
            } catch (KeyManagementException ex) {
                throw new IOException(ex.getMessage(),ex);
            }
        }
        else
        {
            sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            
        }   
        
        socket = (SSLSocket) sslsocketfactory.createSocket(host, port);
    }
    
    private void close() throws IOException
    {
        if(socket != null && !socket.isClosed())
        socket.close();
    }
}
