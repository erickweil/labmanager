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

package br.erickweil.labmanager.client;

import br.erickweil.labamanger.common.Program;
import static br.erickweil.labmanager.client.ClientCmdProtocol.VERSAO;

import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanager.tests.ScreenLockerTest;
import br.erickweil.webserver.ReaderWriter;
import br.erickweil.webserver.ServerProtocol;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



/**
 *
 * @author Aluno
 */
public class FakeClientCmdProtocol extends ServerProtocol{
    public static final String VERSAO = "21.01.18.15.43";
    private final int conn_index;
    public FakeClientCmdProtocol(int conn_index)
    {
        this.conn_index = conn_index;
    }
    
    
    public void write(String msg) throws UnsupportedEncodingException, IOException
    {
    	ReaderWriter.writeASCII(URLEncoder.encode(msg,"UTF-8")+"\n", output);
    }
    
    public String read() throws UnsupportedEncodingException, IOException
    {
    	String txt = ReaderWriter.readASCIILine(input);
    	if(txt == null || txt.isEmpty()) return txt;
    	return URLDecoder.decode(txt,"UTF-8");
    }
    
    public int tryReadInt(int defaultValue)
    {
        try{
            return Integer.parseInt(read());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return defaultValue;
        }
    }
    
    @Override
    public void processRequest() throws IOException {
        
        write(VERSAO);
         
        String userName = CmdExec.getUser();
        if(userName == null || userName.isEmpty())
            userName = System.getProperty("user.name");
        String user_name = userName+Thread.currentThread().getName();
        
        JSONObject client_info = new JSONObject();
        // valores obrigatorios
        client_info.put("user_name", user_name);
        client_info.put("uuid", ClientApp.UUID+Thread.currentThread().getName());
        
        // client info
        write(client_info.toJSONString());
        
        ClientApp.ConnectedOnce = true;
        //opcoes
        //opcoes
        int splash_delay = ClientMain._delay_defaultsplash;
        
        try
        {
            String server_info = read();
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(server_info);
            
            if(json.containsKey("splash_delay"))
            splash_delay = Integer.parseInt((String)json.get("splash_delay"));
            
        }
        catch(Exception e)
        {
            System.out.println("Erro ao carregar informações do servidor:");
            e.printStackTrace(System.out);
        }
        
        // splash screen
        if(conn_index == 0 && splash_delay > 0)
        {
            //ScreenLocker.splash(15000);
        }
        
        boolean locking_screen = false;
        ScreenLockerTest locker = null;
        try
        {
        // espera por pedidos de execucao de comandos
        String linha;
        int insternal_state = 0;
        while(!(linha = read()).isEmpty())
        {
        	linha = linha.trim();
        	boolean hasArgs = linha.contains(" ");
        	final String req;
        	final String[] arguments;
        	if(!hasArgs)
        	{
        		req = linha.toLowerCase();
        		arguments = null;	
        	}
        	else
        	{
        		req = linha.substring(0,linha.indexOf(' ')).toLowerCase();
                int n_args = Integer.parseInt(linha.substring(linha.indexOf(' ')+1)); 
        		arguments = new String[n_args];
                for(int i=0;i<n_args;i++)
                {
                    arguments[i] = read();
                }
        	}
            System.out.println("Requisicao '"+linha+"'");
            
            switch(req)
            {
                case "stop":
                	write("OK");
                break;
                case "start":
                    String program_path = arguments[0];
                    String program_process = arguments[1];
                    write("OK");
                break;
                case "exit":
                	write("OK");
                	throw new RuntimeException("Saiu normalmente.");
                case "shutdown":
                	write("OK");
                break;
                case "restart":
                	write("OK");
                break;
                case "logoff":
                	write("OK");
                break;
                case "cancelshutdown":
                	write("OK");
                break;
                case "blacklist":
                    if(arguments == null)
                    {
                       ClientApp.setBlackList(new ArrayList<Program>()); 
                    }
                    else
                    {
                       //List<Program> p = new ArrayList<>();
                       //for(String s : arguments)
                       //{
                       //    p.add(new Program("","",s,Program.StartType.blacklist));
                       //}
                       //ClientApp.setBlackList(p);
                    }
                	write("OK");
                break;
                case "lockscreen":
                    insternal_state++;
                    Thread.sleep(new Random().nextInt(5000) + 100);
                    write("OK:"+insternal_state);
                    
                break;
                case "broadcast":
                {
                    //ClientApp.locker.startBroadcasting();
                    
                    int size = Integer.parseInt(arguments[0]);
                    byte[] file = new byte[size];
                    input.readFully(file);
                    //InputStream inImg = new ByteArrayInputStream(file);
                    //BufferedImage image = ImageIO.read(inImg);
                    //ClientApp.locker.changeBroadcastImg(image);
                    write("OK");
                }
                break;
                case "download":
                {
                    String action = arguments[0];
                    String dest_path = ProgramOpener.parsePath(Inicio._down_folder+arguments[1]).getAbsolutePath();
                    int size = Integer.parseInt(arguments[2]);
                    byte[] file = new byte[size];
                    input.readFully(file);
                    //Files.write(new File(dest_path).toPath(), file);
                    if(action.equals("run"))
                    {
                        //if(ProgramOpener.start(dest_path,null))
                        //{
                            write("OK");
                        //}
                        //else
                        //{
                        //    write("Nao Pode abrir o programa: '"+dest_path+"'");
                        //}
                    }
                    else
                    {
                        write("OK");
                    }
                }
                break;
                case "unlockscreen":
                    ClientApp.locker.stopLocking();
                    insternal_state--;
                    Thread.sleep(new Random().nextInt(5000) + 100);
                    write("OK:"+insternal_state);
                    
                break;
                case "msg":
                	//final String txt = linha.substring(linha.indexOf(' ')+1);
                    final String txt = arguments[0];
                    if(txt.equals("hang") && new Random().nextFloat() > 0.9)
                    {
                        throw new RuntimeException("Saiu normalmente.");
                    }
                    if(txt.equals("timeout") && new Random().nextFloat() > 0.5)
                    {
                        Thread.sleep(new Random().nextInt(5000) + 100);
                        throw new IOException("ERRO TIMEOUT");
                    }
                	write("OK");
                break;
                case "ping":
                   write("pong:"+insternal_state);
                break;
                default:
                	write("Comando '"+req+"' não implementado");
                break;
            }
        }
        
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally
        {
            if(locker != null)
            {
                locker.stop();
                locker = null;
            }
            System.out.println("Acabou!");
        }
    }
    
}
