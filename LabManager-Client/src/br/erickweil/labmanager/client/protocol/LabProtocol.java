/*
 * Copyright (C) 2018 Usuario
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
package br.erickweil.labmanager.client.protocol;

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.UUIDable;
import br.erickweil.labmanager.client.ClientApp;
import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.client.ExitAppException;
import br.erickweil.labmanager.client.slave.SlaveApp;
import br.erickweil.labmanager.client.swing.ScreenLocker;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.platformspecific.CheckOS;
import br.erickweil.labmanger.filetransmission.FileDownloaderTask;
import br.erickweil.streaming.PrintScreenTool;
import br.erickweil.streaming.tcpwrapper.TCPHilbertWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import java.awt.AWTException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Usuario
 */
public class LabProtocol {
    
    public static class Response implements UUIDable{
        public final int msg_uuid;
        public String status;
        public String[] args;
        public byte[] binary_response;
        
        public Response(int msg_uuid,String status)
        {
            this.msg_uuid = msg_uuid;
            this.status = status;
        }
        
        public Response(int msg_uuid,String status,String[] args)
        {
            this.msg_uuid = msg_uuid;
            this.status = status;
            this.args = args;
        }
        public Response(int msg_uuid,String status,byte[] binary_response)
        {
            this.msg_uuid = msg_uuid;
            this.status = status;
            this.binary_response = binary_response;
        }

        @Override
        public int getUUID() {
            return msg_uuid;
        }
    }
    
    //public static Response OK = new Response("OK");
    public static Response OK(BroadcasterMessage msg)
    {
        return new Response(msg.msg_uuid,"OK");
    }
    
    public static Response execute(BroadcasterMessage msg)
    {
        ScreenLocker locker = ClientMain.isMaster? ClientApp.locker : SlaveApp.locker;
        TCPHilbertWatcher watcher = ClientMain.isMaster? ClientApp.watcher : SlaveApp.watcher;
        String UUID = ClientMain.isMaster ? ClientApp.UUID : SlaveApp.UUID;
        String serverAddress = ClientMain.isMaster ? ClientApp.serverAddress : SlaveApp.serverAddress;
        switch(msg.cmd)
        {
            case stop:
                CmdExec.stop(msg.arguments[0],true);
            return OK(msg);
            case shutdown:
                CmdExec.shutdown();
            return OK(msg);
            case restart:
                CmdExec.restart();
            return OK(msg);
            case logoff:
                CmdExec.logoff();
            return OK(msg);
            case cancelshutdown:
                CmdExec.cancelshutdown();
            return OK(msg);
            case blacklist:
                
                if(!ClientMain.isMaster)
                {
                    
                    return OK(msg);
                }
                
                if(msg.arguments == null)
                {
                    ClientApp.setBlackList(new ArrayList<>()); 
                }
                else
                {
                    List<Program> p = new ArrayList<>();
                    for(String s : msg.arguments)
                    {
                        try
                        {
                            p.add(Program.fromStr(s));    
                        }
                        catch(ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    
                    ClientApp.setBlackList(p);
                }
            return OK(msg);
            case admin_exec:
            case exec: 
                // implementar limite de tempo de execução do comando
                // para não travar o pc executando
                //-- inseguro e não portável
                // retirar o 'exec'
                try
                {
                    List<String> output;
                    if(CheckOS.isWindows()) output = CmdExec.readCmd("cmd","/c",msg.arguments[0]);
                    else if(CheckOS.isMac()) output = CmdExec.readCmd(msg.arguments[0]);
                    else if(CheckOS.isUnix()) output = CmdExec.readCmd(msg.arguments[0]);
                    else output = CmdExec.readCmd(msg.arguments[0]);
                    //else output = Arrays.asList(new String[]{CheckOS.OS,"Não é suportado execução neste Sistema"});
                    
                    String[] ret = new String[output.size()];
                    for(int i = 0; i < output.size(); i++)
                    {
                        ret[i] = output.get(i);
                    }
                    return new Response(msg.msg_uuid,"OK",ret);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    List<String> output = new ArrayList<>();
                    output.add(e.getClass().getCanonicalName()+": "+e.getMessage());
                    if(e.getCause() != null)
                    {
                        output.add("\t"+e.getCause().getClass().getCanonicalName()+": "+e.getCause().getMessage());
                    }
                    else
                    {
                        output.add("no cause");
                    }
                    String[] ret = new String[output.size()];
                    ret = output.toArray(ret);
                    return new Response(msg.msg_uuid,"OK",ret);
                }
            case browse:
            {
                String site_url = msg.arguments[0];
                ProgramOpener.browse(site_url);
                return OK(msg);
            }
            case start: // -- inseguro e não portável
            {
                String program_path = msg.arguments[0];
                String program_process = msg.arguments[1];
                if(ProgramOpener.start(program_path,program_process))
                {
                    return OK(msg);
                }
                else
                {
                    return new Response(msg.msg_uuid,"Nao Pode abrir o programa: '"+program_path+"'");
                }
            }
            case lockscreen:
                if(msg.arguments != null && msg.arguments.length >= 1)
                {
                    long splash_delay = Long.parseLong(msg.arguments[0]);
                    ScreenLocker.splash(splash_delay);
                }
                else
                {
                    if(locker != null)locker.startLocking(false);
                }
                return OK(msg);
            case broadcast: // rídículo, sem palavras. nem funciona
                if(locker != null)locker.startBroadcasting();
                if(watcher != null)watcher.startTask();
                return OK(msg);
            case printscreen:
            {
                try {
                    PrintScreenTool tool = new PrintScreenTool();
                    int print_width = msg.arguments != null && msg.arguments.length >= 1 ? Integer.parseInt(msg.arguments[0]) : -1;
                    byte[] jpeg_img = tool.jpeg_printScreen(0.7f,print_width);
                    return new Response(msg.msg_uuid,"OK",jpeg_img);
                } catch (AWTException ex) {
                    ex.printStackTrace();
                    return new Response(msg.msg_uuid,"nao pode tirar print:"+ex.getMessage(),new byte[0]);
                }
            }
            case unlockscreen:
            {
                try
                {
                    if(watcher != null)watcher.stopTask();
                    if(locker != null)locker.stopLocking();
                    return OK(msg);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                    return new Response(msg.msg_uuid,"Erro ao desbloquear tela:"+e.getMessage());
                }
            }
            case admin_download:
            case download:// se usar hash, +/- seguro.
                // agora consegue passar arquivos grandes
            {
                    String action = msg.arguments[0];
                    String filename = msg.arguments[1];
                    int fileserver_port = Integer.parseInt(msg.arguments[2]);
                    String hash = msg.arguments[3];
                    String hashProtocol = msg.arguments[4];
                    File dest_pathfile = ProgramOpener.parsePath("<downloads>/"+filename);
                    if(dest_pathfile == null)
                    {
                        return new Response(msg.msg_uuid,"Caminho Nao Aceito: '"+msg.arguments[1]+"'");
                    }
                    else
                    {
                        if(!dest_pathfile.getParentFile().exists())
                        {
                            dest_pathfile.getParentFile().mkdirs(); // cria a pasta down
                        }
                        FileDownloaderTask downloader = new FileDownloaderTask(
                                UUID, 
                                dest_pathfile, 
                                serverAddress, 
                                fileserver_port, 
                                action,
                                hash.equals("none") ? null : hash,
                                hash.equals("none") ? null : hashProtocol
                        );
                        downloader.startTask();
                        return OK(msg);
                    }
            }
            /*case execuser: //-- inseguro e não portável
                // retirar o 'exec'
                for(String cmd : arguments)
                {
                    CmdExec.execCmd(cmd);
                }
                return OK;*/
            //case "bringwindow":
            //https://stackoverflow.com/questions/557166/bring-to-front-for-windows-xp-command-shell
            //break;
            case msg:
            {
                final String txt = msg.arguments[0];
                new Thread(new Runnable(){public void run(){
                        //JOptionPane.showMessageDialog(null, txt);
                JDialog dialog = new JOptionPane(txt,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION).createDialog("Mensagem Recebida"); 
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                dialog.dispose();
                }}).start();
                return OK(msg);
            }
            case ping:
            {
                //long[] mem = CmdExec.memory_info();
                //double cpu = CmdExec.cpu_info();                
                JSONObject client_info = new JSONObject();
                
                client_info.put("slaveOnline", !ClientMain.isMaster);
                //client_info.put("memTotal", mem[0]);
                //client_info.put("memFree", mem[1]);
                //client_info.put("cpuUsage", cpu);
                
                return new Response(msg.msg_uuid,"OK",new String[]{client_info.toJSONString()});
            }
            case windowlist:
            {
                //long[] mem = CmdExec.memory_info();
                //double cpu = CmdExec.cpu_info();                
                
                
                List<CmdProcess> windows = CmdExec.running_windows();
                String[] args = new String[windows.size()];

                for(int i = 0;i<args.length;i++)
                {
                    args[i] = CmdProcess.toJSON(windows.get(i)).toJSONString();
                }
                return new Response(msg.msg_uuid,"OK",args);
            }
            default:
                return new Response(msg.msg_uuid,"Comando '"+msg.cmd+"' nao implementado");
        }

    }
}
