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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URLEncoder;

import javax.net.ssl.SSLSocketFactory;

public class CheckSSL {
      
   public static final String TARGET_HTTPS_SERVER = "www.jw.org"; 
   public static final int    TARGET_HTTPS_PORT   = 443; 
      
   public static void main(String[] args) throws Exception {
      
     Socket socket = SSLSocketFactory.getDefault().
       createSocket(TARGET_HTTPS_SERVER, TARGET_HTTPS_PORT);
     try {
       Writer out = new OutputStreamWriter(
          socket.getOutputStream(), "ISO-8859-1");
       out.write("GET /pt/publicacoes/biblia/nwt/livros/"+URLEncoder.encode("Gênesis", "UTF-8").toLowerCase()+"/1/ HTTP/1.1\r\n");  
       out.write("Host: " + TARGET_HTTPS_SERVER + ":" + 
           TARGET_HTTPS_PORT + "\r\n");  
       out.write("Agent: SSL-TEST\r\n");  
       out.write("\r\n");  
       out.flush();  
       BufferedReader in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), "UTF-8"));
       String line = null;
       //while ((line = in.readLine()) != null) {
       for(int i=0;i<100;i++)
       {
    	   line = in.readLine();
          System.out.println(line);
       }
     } finally {
       socket.close(); 
     }
   }
}
