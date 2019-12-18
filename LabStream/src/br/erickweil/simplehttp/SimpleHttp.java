/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.erickweil.simplehttp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Usuario
 */
public class SimpleHttp {
    public static void main(String[] args) throws IOException
    {
        ServerSocket server = new ServerSocket(80);
        List<String> historico = new ArrayList<>();
        while(true)
        {
            Socket client = server.accept();
            System.out.println("conectou:"+client.getInetAddress().getHostAddress());
            
            Scanner reader = new Scanner(client.getInputStream());
            
            String request = reader.nextLine();
            System.out.println(request);

            String msg = URLDecoder.decode(request.split(" ")[1],"UTF-8");
            historico.add(msg);
            
            PrintStream writer = new PrintStream(client.getOutputStream());

            writer.println("HTTP/1.1 200 OK");
            writer.println("");

            writer.println("<html>");
            writer.println("<head><title>TESTE</title></head>");
            writer.println("<h1>FUNCIONOU BELEZA</h1>");
            writer.println("<form action=\"\">");
            writer.println("Mensagem:<input name=\"msg\"/> <br/>");
            writer.println("<input type=\"submit\" action=\"POST\" />");
            writer.println("</form>");
            
            for(int i=0;i<historico.size();i++)
            {
                writer.println(historico.get(i)+"<br/>");
            }
            
            writer.println("</html>");
            //muito importante
            writer.flush();

            writer.close();
            client.close();
        }
    }
}
