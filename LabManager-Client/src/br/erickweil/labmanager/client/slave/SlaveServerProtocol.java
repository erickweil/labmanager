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
package br.erickweil.labmanager.client.slave;

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labmanager.client.ClientApp;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler.DataLink;
import br.erickweil.labmanager.client.ClientCmdProtocol;
import br.erickweil.webserver.ReaderWriter;
import br.erickweil.webserver.ServerProtocol;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * <strong>Classe que implementa o protocolo de comunicação da parte servidor</strong>
 * 
 * 
 *
 */
public class SlaveServerProtocol extends ServerProtocol {
   private final ThreadSafeListener listener;
   private final ThreadSafeHandler<BroadcasterMessage> handler;

    public SlaveServerProtocol(final ThreadSafeHandler<BroadcasterMessage> handler, final ThreadSafeListener listener)
    {
        this.handler = handler;
        this.listener = listener;
    }
    
    // debgu only;
    private void print(String txt, int uuid)
    {
    	//long ns = System.nanoTime() - nano;
    	//String time = String.format("%,d", ns); 
        //if(LOG_msgs)
        //{
        //    System.out.println("Arrived:"+uuid+"("+txt+")");    
        //}
        //else
    	System.out.println(txt);
    }
  
    
    /**
     * Aqui que é processado a comunicação de um cliente.
     * 
     * é lido a primeira linha, que indica a versão.<br/>
     * atualmente a versão aceita é 21.01.18.15.43<br/>
     * 
     * <p>de acordo com a versão é processado as requisições e 
     * esse método não retorna até que aconteça algum erro na
     * comunicação ou o cliente desconecte.<p>
     * 
     * 
     * @throws IOException
     */
    @Override
    public void processRequest() throws IOException {    
    	
        
    	try
    	{
            System.out.print("Checando Cliente");
            String client_ip = socket.getInetAddress().getHostAddress();
            int client_port = socket.getPort();
    		

			
            String client_hostname;
            
            System.out.print(".");
            client_hostname = socket.getInetAddress().getCanonicalHostName();

            System.out.print(".");
            
            System.out.println("IP:"+client_ip);
            System.out.println("HOSTNAME:"+client_hostname);
            System.out.println("LOOPBACK:"+socket.getInetAddress().isLoopbackAddress());
            if(!socket.getInetAddress().isLoopbackAddress())
            {
                System.err.println("cliente tentando conectar de fora");
                return;
            }
            
            process();

	}
        catch (SocketException | EOFException e){
    		System.out.println("Cliente Desconectou:"+e.getMessage());
    	}
        catch (IOException e){
    		System.out.println("Erro ao Desconectar:"+e.getMessage());
                e.printStackTrace();
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    	finally {
            
        }
    }
    
    
    
    private void process() throws IOException, ParseException {
        String txt;
        String status; 
        System.out.print(" .");
        
        String gen_uuid = readln_url();
        File arquivo_verificador = new File(gen_uuid+".txt");
        Files.write(arquivo_verificador.toPath(), "aaaa a a a, bbbbb b b bebe cai e levanta, vamos emboora...".getBytes());
        
        writeln_url("OK");
        
        status = readln_url();
        if(!status.equals("OK"))
        {
            System.err.println("Resposta '"+status+"' na validação da comunicação");
        }
        arquivo_verificador.delete();
        if(!ClientApp.slaveConnectedOnce )
        {
            List<Program> blacklist = ClientApp._getBlackList();
            for (int i = 0; i < blacklist.size(); i++) {
            if(blacklist.get(i).start == Program.StartType.logon)
            {
                writeln_url("start 2");
                writeln_url(blacklist.get(i).path);
                writeln_url(blacklist.get(i).process);
                status = readln_url();
                if(!status.equals("OK"))
                    print(status,-1);
            }
            }
        }
        
        ClientApp.slaveConnectedOnce = true;
        
        handler.waitMessage();
	DataLink<BroadcasterMessage> link = handler.getMessage();
        while (true) {
            
            BroadcasterMessage msg = link.getData();
            if (msg == null) {
                break;
            }
            
            if (msg.arguments == null || msg.arguments.length == 0) {
                    writeln_url(msg.cmd.toString());
                } else {
                    writeln_url(msg.cmd.toString() + " " + msg.arguments.length);
                    for (int i = 0; i < msg.arguments.length; i++) {
                        writeln_url(msg.arguments[i]);
                    }
                }
            if(msg.binary_data != null)
                output.write(msg.binary_data);
            
            status = readln_url();
            if(!status.equals("OK"))
                print(status,msg.msg_uuid);
                
            if(status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.binary)
            {
                int msg_length = Integer.parseInt(readln_url());
                byte[] binary_response = new byte[msg_length];
                input.readFully(binary_response);
                if(listener != null)
                {
                    listener.sendEvent(SlaveApp.CLIENT_RESPONSE, msg.msg_uuid, status,new LabProtocol.Response(msg.msg_uuid,status,binary_response));
                }
            }
            else if(status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.text)
            {
                int msg_length = Integer.parseInt(readln_url());
                String[] text_response = new String[msg_length];
                for(int i=0;i<msg_length;i++)
                {
                    text_response[i] = readln_url();
                }
                if(listener != null)
                {
                    listener.sendEvent(SlaveApp.CLIENT_RESPONSE, msg.msg_uuid, status,new LabProtocol.Response(msg.msg_uuid,status,text_response));
                }
            }
            else if(listener != null) // isso pode dar problema, tudo culpa da Splash Screen
                // depois tem q achar jeito melhor de saber se o slave ta on
            //if(msg.cmd.responseType() != BroadcasterMessage.MessageResponse.none)
            {
                listener.sendEvent(SlaveApp.CLIENT_RESPONSE, msg.msg_uuid, status, null);
            }
            
            if(!status.equals("OK") && !status.equals("pong"))
            {
                System.err.println("Resposta '"+status+"' para o comando '"+msg.cmd+"' ");
            }
            
            //String threadname = txt.substring(0,txt.indexOf(' '));
            //String msg = txt.substring(txt.indexOf(' ')+1);
            /*if (msg.threadname.equals(name) || msg.threadname.equals(BroadcasterMessage.All)) {
                if (listener != null && send_connected) {
                    listener.sendEvent(ServerApp.CLIENT_PROCESSING, name, "Processando:");
                }
                if (msg.arguments.length == 0) {
                    write(msg.cmd.toString());
                } else {
                    write(msg.cmd.toString() + " " + msg.arguments.length);
                    for (int i = 0; i < msg.arguments.length; i++) {
                        write(msg.arguments[i]);
                    }
                }

                if (msg.binary_data != null) {
                    output.write(msg.binary_data);
                }

                response = read();
                print(response);
                
                if(response.equals("OK") && msg.binary_response)
                {
                    int msg_length = Integer.parseInt(read());
                    byte[] binary_response = new byte[msg_length];
                    input.read(binary_response);
                }
            }*/

            
            DataLink<BroadcasterMessage> n = link.next();
            if (n == null) {
                handler.waitMessage();
                n = link.next();
            }
            link = n;
        }

        print("Terminou",0);
    }
    
    @Override
    public int getTimeout()
    {
        return 0;
    }
    
}

