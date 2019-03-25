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
package br.erickweil.labmanager.server;

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.BroadcasterMessage.MessageResponse;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler.DataLink;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
public class CmdBroadcasterProtocol extends ServerProtocol {
   public static final String ULTIMA_VERSAO = "22.09.18.16.32b";
   private final ThreadSafeListener<BroadcasterListener> listener;
   private final ThreadSafeHandler<BroadcasterMessage> handler;
   private final List<Program> program_list;
   //private final List<String[]> init_list;

   private final boolean solve_hostame;
   // debug only;
   private final String name;
    protected CmdBroadcasterProtocol(String name,final ThreadSafeHandler<BroadcasterMessage> handler, final ThreadSafeListener<BroadcasterListener> listener, List<Program> program_list, boolean solve_hostname)
    {
    	this.name = name;
        this.handler = handler;
        this.listener = listener;
        this.solve_hostame = solve_hostname;
        //this.init_list = new ArrayList<>();
        this.program_list = program_list;
        //for(Program p : program_list)
        //{
        //    if(p.start == Program.StartType.logon)
        //    {
        //        String path = p.path;
        //        String process = p.process;
        //        init_list.add(new String[]{path,process});
        //    }
        //    else if(p.start == Program.StartType.blacklist)
        //    {
        //        black_list.add(p.process);
        //    }
        //}
    }
    
    // debgu only;
    private void print(String txt)
    {
    	//long ns = System.nanoTime() - nano;
    	//String time = String.format("%,d", ns); 
    	System.out.println(name+" -> "+txt);
    }
    
    private void write(String msg) throws UnsupportedEncodingException, IOException
    {
    	ReaderWriter.writeASCII(URLEncoder.encode(msg,"UTF-8")+"\n", output);
    }
    
    private String read() throws UnsupportedEncodingException, IOException
    {
    	String txt = ReaderWriter.readASCIILine(input);
    	if(txt == null || txt.isEmpty()) return txt;
    	return URLDecoder.decode(txt,"UTF-8");
    }
    boolean send_connected = false;
    
    private boolean AuthList(String auth_elem,String[] list,String auth_mode)
    {
        if (auth_mode.equals("allowlist") || auth_mode.equals("blocklist")) {
            
            boolean matched = false;
            for(String elem : list)
            {
                if(elem.equals(auth_elem))
                {
                   matched = true;
                   break;
                }
                try{
                    if(elem.matches(auth_elem))
                    {
                        matched = true;
                        break;
                    } 
                }
                catch(PatternSyntaxException e)
                {
                    // normal, mamãe passou açúcar em mim.
                }
            }
            if(
            (!matched && auth_mode.equals("allowlist"))
          ||( matched && auth_mode.equals("blacklist")))
            {
                
                return false;
            }
            else return true;
        }
        else return true;
    }
    
    /**
     * Aqui que é processado a comunicação de um cliente.
     * 
     * é lido a primeira linha, que indica a versão.<br/>
     * atualmente a versão aceita é 26.04.18.08.27<br/>
     * outras aceitas são:<br/>
     * 21.01.18.15.43<br/>
     * 12.11.17.10.07<br/>
     * e as não aceitas mais:<br/>
     * 06.10.17.12.22<br/>
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
            if(solve_hostame)
            {
                System.out.print(".");
                client_hostname = socket.getInetAddress().getCanonicalHostName();
            }
            else
            {
                System.out.print(".");
                client_hostname = socket.getInetAddress().getHostAddress();
            }
            
            if(!AuthList(client_ip, ServerMain._auth_address_list, ServerMain._auth_by_address))
            {
                if(!AuthList(client_hostname, ServerMain._auth_address_list, ServerMain._auth_by_address))
                {
                        print("Cliente não autorizado: hostname:"+client_hostname+" ip:"+client_ip);
                        return;
                }
            }

            System.out.print(".");
            
            String client_versao = read();
            // 22.09.18.16.32, 22.09.18.16.32a, 22.09.18.16.32b, etc.. tudo igual
            String protocol_versao = client_versao.replaceAll("[a-z]$", "");
            // deveria implementar um queue dos comandos, para não perder requests
            switch(protocol_versao)
            {
                case "16.05.18.10.19":
                case "22.09.18.16.32":
                    versao_22_09_18_16_32(client_versao,client_ip,client_port,client_hostname); 
                break;
                
                // VERSOES LEGADAS
                
                case "21.01.18.15.43":
                    versao_21_01_18_15_43(client_versao,client_ip,client_port,client_hostname); 
                break;
                
                case "12.11.17.10.07":
                    versao_12_11_17_10_07(client_versao,client_ip,client_port,client_hostname); 
                break;
                
                case "06.10.17.12.22": 
                    versao_06_10_17_12_22(client_versao,client_ip,client_port,client_hostname); 
                break;
                
                default:
                System.err.println("cliente com versão desconhecida: '"+client_versao+"'");
                break;
            }

	}
        catch (SocketException | EOFException e){
    		System.out.println("Cliente Desconectou:"+e.getMessage());
    	}
        catch (IOException e){
    		System.out.println("Erro ao Desconectar:"+e.getMessage());
                e.printStackTrace(System.out);
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    	finally {
            if(listener != null && send_connected)
            listener.sendEvent(ServerApp.CLIENT_DISCONECTED, name);
        }
    }
    
    private void versao_22_09_18_16_32(String versao,String client_ip, int client_port, String client_hostname) throws IOException, ParseException {
        String txt;
        String status; 
        System.out.print(" .");
        
        
        String client_info = read();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(client_info);
        
        String client_user = (String)json.get("user_name");

        if(!AuthList(client_user,ServerMain._auth_username_list,ServerMain._auth_by_username))
        {
            print("Cliente não autorizado: username:" + client_user);
            return;
        }

        System.out.print(".");
        
        String client_uuid = (String)json.get("uuid");
        
        if(!AuthList(client_uuid,ServerMain._auth_uuid_list,ServerMain._auth_by_uuid))
        {
            print("Cliente não autorizado: uuid:" + client_uuid);
            return;
        }
        
        // opcoes
        JSONObject server_info = new JSONObject();
        server_info.put("splash_delay", ""+ServerMain._delaySplash);
        
        write(server_info.toJSONString());

        System.out.println(" Conectado!");
        write("blacklist " + program_list.size());
        for (int i = 0; i < program_list.size(); i++) {
            write(Program.toStr(program_list.get(i)));
        }
        status = read();
        if (!status.equals("OK")) {
            print("Não foi possível conectar, cliente não aceitou a blacklist");
            return;
        }

        if (listener != null) {
            send_connected = true;
            listener.sendEvent(ServerApp.CLIENT_CONECTED, name, client_uuid, client_ip, client_hostname, client_port, client_user, json, versao);
        }

        for (int i = 0; i < program_list.size(); i++) {
            if(program_list.get(i).start == Program.StartType.logon)
            {
                write("start 2");
                write(program_list.get(i).path);
                write(program_list.get(i).process);
                status = read();
                print(status);
            }
        }

        if (listener != null && send_connected) {
            listener.sendEvent(ServerApp.CLIENT_WAITING, name, -1, "Aguardando:");
        }


        handler.waitMessage();
	DataLink<BroadcasterMessage> link = handler.getMessage();
        while (true) {
            
            BroadcasterMessage msg = link.getData();
            if (msg == null) {
                break;
            }
            //String threadname = txt.substring(0,txt.indexOf(' '));
            //String msg = txt.substring(txt.indexOf(' ')+1);
            if (msg.threadname.equals(name) || msg.threadname.equals(BroadcasterMessage.All)) {
                if (listener != null && send_connected) {
                    listener.sendEvent(ServerApp.CLIENT_PROCESSING, name, msg.msg_uuid, "Processando:");
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

                status = read();
                print(status);
                
                if(status.equals("OK") && msg.cmd.responseType() == MessageResponse.binary)
                {
                    int msg_length = Integer.parseInt(read());
                    byte[] binary_response = new byte[msg_length];
                    input.readFully(binary_response);
                    if(listener != null && send_connected)
                    listener.sendEvent(ServerApp.CLIENT_RESPONSE, name, msg.msg_uuid, status,binary_response);
                }
                else if(status.equals("OK") && msg.cmd.responseType() == MessageResponse.text)
                {
                    int msg_length = Integer.parseInt(read());
                    String[] text_response = new String[msg_length];
                    for(int i=0;i<msg_length;i++)
                    {
                        text_response[i] = read();
                    }
                    if(listener != null && send_connected)
                    listener.sendEvent(ServerApp.CLIENT_RESPONSE, name, msg.msg_uuid, status,text_response);
                }
                else if(msg.cmd.responseType() != MessageResponse.none)
                {
                    if(listener != null && send_connected)
                    listener.sendEvent(ServerApp.CLIENT_RESPONSE, name, msg.msg_uuid, status, null);
                }
            
                if (listener != null && send_connected) {
                    listener.sendEvent(ServerApp.CLIENT_WAITING, name, msg.msg_uuid, "Aguardando:");
                }
                
            }
            else
            {
                if (listener != null && send_connected) {
                    listener.sendEvent(ServerApp.CLIENT_WAITING, name, -1, "Aguardando:");
                }
            }
                

            
            
            DataLink<BroadcasterMessage> n = link.next();
            if (n == null) {
                handler.waitMessage();
                n = link.next();
            }
            link = n;
        }

        print("Terminou");
    }
    
    
    
    @Override
    public int getTimeout()
    {
        return ServerMain._readTimeout;
    }
    
    /*
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    
    VERSOES LEGADAS DO PROTOCOLO, COLOCADO AQUI PARA POSSIVEL COMPATIBILIDADE
    
    NAO SERAO MAIS SUPORTADAS, SENDO APENAS PARCIALMENTE COMPATIVEIS.
    
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    ############################################################################
    
     */
    
    private void versao_21_01_18_15_43(String versao,String client_ip, int client_port, String client_hostname) throws IOException, ParseException 
    {
        String txt;
        String response; 
        System.out.print(" .");
        
        
        String client_info = read();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(client_info);
        
        String client_user = (String)json.get("user_name");

        if(!AuthList(client_user,ServerMain._auth_username_list,ServerMain._auth_by_username))
        {
            print("Cliente não autorizado: username:" + client_user);
            return;
        }

        System.out.print(".");
        
        String client_uuid = (String)json.get("uuid");
        
        if(!AuthList(client_uuid,ServerMain._auth_uuid_list,ServerMain._auth_by_uuid))
        {
            print("Cliente não autorizado: uuid:" + client_uuid);
            return;
        }
        
        // opcoes
        JSONObject server_info = new JSONObject();
        server_info.put("splash_delay", ""+ServerMain._delaySplash);
        
        write(server_info.toJSONString());

        System.out.println(" Conectado!");
        write("blacklist " + program_list.size());
        for (int i = 0; i < program_list.size(); i++) {
            write(program_list.get(i).process);
        }
        response = read();
        if (!response.equals("OK")) {
            print("Não foi possível conectar, cliente não aceitou a blacklist");
            return;
        }

        if (listener != null) {
            send_connected = true;
            listener.sendEvent(ServerApp.CLIENT_CONECTED, name, client_uuid, client_ip, client_hostname, client_port, client_user,json,versao);
        }

        for (int i = 0; i < program_list.size(); i++) {
            if(program_list.get(i).start == Program.StartType.logon)
            {
                write("start 2");
                write(program_list.get(i).path);
                write(program_list.get(i).process);
                response = read();
                print(response);
            }
        }

        if (listener != null && send_connected) {
            listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Aguardando:");
        }


        handler.waitMessage();
	DataLink<BroadcasterMessage> link = handler.getMessage();
        while (true) {
            
            BroadcasterMessage msg = link.getData();
            if (msg == null) {
                break;
            }
            //String threadname = txt.substring(0,txt.indexOf(' '));
            //String msg = txt.substring(txt.indexOf(' ')+1);
            if (msg.threadname.equals(name) || msg.threadname.equals(BroadcasterMessage.All)) {
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
                
                if(response.equals("OK") && msg.cmd.responseType() == MessageResponse.binary)
                {
                    int msg_length = Integer.parseInt(read());
                    byte[] binary_response = new byte[msg_length];
                    input.read(binary_response);
                }
                else if(response.equals("OK") && msg.cmd.responseType() == MessageResponse.text)
                {
                    int msg_length = Integer.parseInt(read());
                    String[] text_response = new String[msg_length];
                    for(int i=0;i<msg_length;i++)
                    {
                        text_response[i] = read();
                    }
                }
                
            }

            if (listener != null && send_connected) {
                listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Aguardando:");
            }
            
            DataLink<BroadcasterMessage> n = link.next();
            if (n == null) {
                handler.waitMessage();
                n = link.next();
            }
            link = n;
        }

        print("Terminou");
    }
    
    private void versao_12_11_17_10_07(String versao,String client_ip, int client_port, String client_hostname) throws IOException {
        
        print("CLIENTE COM VERSAO LEGADA. IP:"+client_ip+" HOST:"+client_hostname);
        String txt;
        String response; 
        System.out.print(" .");
        String client_user = read();

        if(!AuthList(client_user,ServerMain._auth_username_list,ServerMain._auth_by_username))
        {
            print("Cliente não autorizado: username:" + client_user);
            return;
        }

        System.out.print(".");
        
        // screen splash delay
        write(""+ServerMain._delaySplash);

        System.out.println(" Conectado!");
        write("blacklist " + program_list.size());
        for (int i = 0; i < program_list.size(); i++) {
            write(program_list.get(i).process);
        }
        response = read();
        if (!response.equals("OK")) {
            print("Não foi possível conectar, cliente não aceitou a blacklist");
            return;
        }

        if (listener != null) {
            send_connected = true;
            listener.sendEvent(ServerApp.CLIENT_CONECTED, name, client_ip, client_hostname, client_port, client_user, null,versao);
        }

        for (int i = 0; i < program_list.size(); i++) {
            if(program_list.get(i).start == Program.StartType.logon)
            {
                write("start 2");
                write(program_list.get(i).path);
                write(program_list.get(i).process);
                response = read();
                print(response);
            }
        }

        if (listener != null && send_connected) {
            listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Aguardando:");
        }


        handler.waitMessage();
	DataLink<BroadcasterMessage> link = handler.getMessage();
        while (true) {
            
            BroadcasterMessage msg = link.getData();
            if (msg == null) {
                break;
            }
            //String threadname = txt.substring(0,txt.indexOf(' '));
            //String msg = txt.substring(txt.indexOf(' ')+1);
            if (msg.threadname.equals(name) || msg.threadname.equals(BroadcasterMessage.All)) {
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
            }

            if (listener != null && send_connected) {
                listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Aguardando:");
            }
            
            DataLink<BroadcasterMessage> n = link.next();
            if (n == null) {
                handler.waitMessage();
                n = link.next();
            }
            link = n;
        }

        print("Terminou");
    }
    
    // versao nao suportada mais, por causa que nao e mais passada a senha
    private void versao_06_10_17_12_22(String versao,String client_ip, int client_port, String client_hostname) throws IOException
    {
        print("CLIENTE COM VERSAO LEGADA. IP:"+client_ip+" HOST:"+client_hostname);
        String txt;
        String response; 
        System.out.print(" .");
        String client_user = read();

        /*if (username_allowed != null) {
            
            boolean auth = false;
            for(String username : username_allowed)
            {
                if(client_user.equals(username))
                {
                   auth = true; 
                   break;
                }
                try{
                    if(client_user.matches(username))
                    {
                        auth = true; 
                        break;
                    } 
                }
                catch(PatternSyntaxException e)
                {
                    // normal, mamãe passou açúcar em mim.
                }
            }
            
            //if (!username_allowed.contains(client_user)) {
            if(!auth) {
                print("Cliente não autorizado: username:" + client_user);
                return;
            }
        }*/

        System.out.print(".");
        //write(pass);
        // senha perdida. nunca mais utilizar essa senha
        write("ifroabc2017");
        response = read();
        if (!response.equals("OK")) {
            print("Não foi possível conectar, cliente não aceitou a senha");
            return;
        }
        
        // screen splash delay
        write(""+ServerMain._delaySplash);

        System.out.println(" Conectado!");
        write("blacklist " + program_list.size());
        for (int i = 0; i < program_list.size(); i++) {
            write(program_list.get(i).process);
        }
        response = read();
        if (!response.equals("OK")) {
            print("Não foi possível conectar, cliente não aceitou a blacklist");
            return;
        }

        String uuid=  "UUID"+(new Random().nextLong());
        if (listener != null) {
            send_connected = true;
            // listener.sendEvent(ServerApp.CLIENT_CONECTED, name, client_uuid, client_ip, client_hostname, client_port, client_user);
            listener.sendEvent(ServerApp.CLIENT_CONECTED, name, uuid,client_ip + " LEGADO", client_hostname, client_port, client_user,null, versao);
        }

        for (int i = 0; i < program_list.size(); i++) {
            if(program_list.get(i).start == Program.StartType.logon)
            {
                write("start 2");
                write(program_list.get(i).path);
                write(program_list.get(i).process);
                response = read();
                print(response);
            }
        }

        listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Processando:");
            DataLink<BroadcasterMessage> link = null;
            handler.waitMessage();
            
            while (true) {
                
                if(link == null)
                {
                    link = handler.getMessage();
                }
                else
                {
                    DataLink<BroadcasterMessage> n = link.next();
                    if(n == null)
                    {
                        handler.waitMessage();
                        n = link.next();
                    }
                    link = n;
                }
                
                BroadcasterMessage msg = link.getData();
                if(msg == null) break;
            if (msg.threadname.equals(name) || msg.threadname.equals(BroadcasterMessage.All)) {
                listener.sendEvent(ServerApp.CLIENT_PROCESSING, name, "Processando:");
                if (msg.arguments.length == 0) {
                    write(msg.cmd.toString());
                } else {
                    write(msg.cmd.toString() + " " + msg.arguments.length);
                    for (int i = 0; i < msg.arguments.length; i++) {
                        write(msg.arguments[i]);
                    }
                }
                
                if(msg.binary_data != null)
                {
                    output.write(msg.binary_data);
                }
                
                response = read();
                print(response);
                listener.sendEvent(ServerApp.CLIENT_WAITING, name, "Aguardando. "+response);
            }
        }

        print("Terminou");
    }
}
