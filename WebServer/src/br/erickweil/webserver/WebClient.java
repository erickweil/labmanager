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
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class WebClient implements Runnable {
    
    int port;
    String host;
    public Socket socket;
    protected ProtocolFactory protocolfactory;
    public WebClient(String host,int port, ProtocolFactory protocolfactory) 
    {
        this.port = port;
        this.host = host;
        this.protocolfactory = protocolfactory;
        
    }
    
    @Override
    public void run()
    {
        try {
            connect();
            
            new ServerConnection(null,protocolfactory.get(), socket, "Kcire Server").run();
            
            close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private void connect() throws IOException {
            //SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SocketFactory socketFactory = SocketFactory.getDefault();
            socket = socketFactory.createSocket(host, port);
    }
    
    private void close() throws IOException
    {
        if(socket != null && !socket.isClosed())
        socket.close();
    }
}
