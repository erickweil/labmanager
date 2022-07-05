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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ServerConnection implements Runnable{

    protected final Socket clientSocket;
    protected String serverText   = null;
    private final ServerProtocol protocol;
    private DataInputStream input;
    private DataOutputStream output; 
    private final Charset charset;
    private final ClientCountListener countListener;
    
    public ServerConnection(ClientCountListener listener,ServerProtocol protocol,Socket clientSocket, String serverText) {
        this.countListener = listener;
    	this.protocol = protocol;
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
        this.charset = Charset.forName("UTF-8");
    }

    public void run() {
        try {
            clientSocket.setSoTimeout(protocol.getTimeout());
			input  = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			if(HttpBase.LOG)System.out.println("Cliente conectou");
			protocol.input = input;
			protocol.output = output;
			protocol.socket = clientSocket;
			protocol.charset = charset;
			
            do{
                protocol.processRequest();
            }
            while(protocol.repeat() && !clientSocket.isClosed() && !clientSocket.isInputShutdown() && !clientSocket.isOutputShutdown());
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
        	if(HttpBase.LOG)System.out.println("Cliente desconectou");
            
            try{
                input.close();
                output.close();
            }
            catch(Exception e){
                //ok
            }
            
            try{
                if(!clientSocket.isClosed())
                clientSocket.close();
            }
            catch(Exception e){
                //ok
            }
            
        	//WebServer.client_count--;
            if(countListener != null)
            countListener.onDisconnected();
		}
    }


}
