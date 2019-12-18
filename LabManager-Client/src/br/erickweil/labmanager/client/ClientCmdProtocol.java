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

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.UUIDable;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.client.swing.ScreenLocker;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.platformspecific.CheckOS;
import br.erickweil.labmanager.threadsafeness.BlockingListenerHelper;
import br.erickweil.labmanger.filetransmission.FileDownloaderTask;
import br.erickweil.streaming.StreamWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import br.erickweil.webserver.ServerProtocol;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Aqui acontece toda a parte da comunicação, a classe cmdBroadcasterProtocol deve
 * estar de acordo com essa.
 * @author Aluno
 */
public class ClientCmdProtocol extends ServerProtocol{
    public static final String VERSAO = "19.05.19.11.12";
    private final int conn_index;
    private final ClientApp _clientapp;
    public ClientCmdProtocol(int conn_index,ClientApp instance)
    {
        this.conn_index = conn_index;
        this._clientapp = instance;
    }
    /*
    private LabProtocol.Response response;
    private boolean responseArrived;
    private boolean waiting;
    private int uuidWaiting;
    public static final boolean LOG_msgs = false;
    private synchronized void setResponseArrived(LabProtocol.Response r)
    {
        if(waiting && r != null)
        {
            if(r.msg_uuid == uuidWaiting)
            {
                response = r;
                responseArrived = true;
            }
            else
            {
                System.err.println("DESCARTADA RESPOSTA '"+r.status+"' À MSG:"+r.msg_uuid);
            }
        }
    }
    private synchronized boolean hasresponseArrived()
    {
        return responseArrived;
    }
    private synchronized LabProtocol.Response getresponseArrived()
    {
        if(LOG_msgs)
        System.out.println("Taken:"+response.msg_uuid);
        LabProtocol.Response r = response;
        
        response = null;
        responseArrived = false;
        
        waiting = false;
        return r;
    }
    private synchronized void setWaiting(int uuid)
    {
        if(LOG_msgs)
        System.out.println("Waiting:"+uuid);
        waiting = true;
        uuidWaiting = uuid;
        
        response = null;
        responseArrived = false;
    }
    */
    // retorna verdadeiro para indicar que a mensagem foi respondida
    public boolean IPC_forward(String cmd,String[] args,byte[] binary_msg, Messages response) throws IOException
    {
        return IPC_exec(cmd, args, binary_msg, response, false, true);
    }
    
    
    public boolean IPC_exec(final String cmd,String[] args,byte[] binary_msg, final Messages response,boolean forceListener,boolean forward) throws IOException
    {
        final BroadcasterMessage msg = new BroadcasterMessage("thread",Messages.valueOf(cmd),args,binary_msg);
        
        if((response.responseType() != BroadcasterMessage.MessageResponse.none) || forceListener)
        {
            BlockingListenerHelper listener = new BlockingListenerHelper();

            UUIDable respOBJ = listener.sendAndWaitResponse(
                    msg,
                    1,
                    _clientapp.IPC_handler,
                    _clientapp.IPC_status,
                    1000
            );

            if(respOBJ != null)
            {
                LabProtocol.Response resp = (LabProtocol.Response)respOBJ;
                if(forward)
                {
                    if(resp.status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.binary)
                    {
                        if(resp.binary_response != null)
                        {
                            writeln_url(resp.status);
                            
                            writeln_url(""+resp.binary_response.length);
                            output.write(resp.binary_response);
                        }
                        else
                        {
                            writeln_url("SEM binary_response");
                        }
                        
                    }
                    else if(resp.status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.text)
                    {
                        if(resp.args != null)
                        {
                            writeln_url(resp.status);
                        
                            writeln_url(""+resp.args.length);
                            for(String s : resp.args) writeln_url(s);
                        }
                        else
                        {
                            writeln_url("SEM args");
                        }
                    }
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            try {
                _clientapp.IPC_handler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if(forward)
            {
                writeln_url("OK");
            }
            return true;
        }
        /*
        if((response.responseType() != BroadcasterMessage.MessageResponse.none) || forceListener)
        {
            // diz q ta esperando a resposta para essa msg
            setWaiting(msg.msg_uuid);
        }
        
        
        // envia a msg
        try {
            _clientapp.IPC_handler.sendMessage(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if((response.responseType() != BroadcasterMessage.MessageResponse.none) || forceListener)
        {
            try {
                long time_start = System.currentTimeMillis();
                while(!hasresponseArrived() && (System.currentTimeMillis() - time_start) < 1000)
                {
                    Thread.sleep(5);
                }
                
                if(!hasresponseArrived())
                {
                    System.err.println("msg to slave not asnwered:"+cmd);
                    return false;
                }
                
                // pega a resposta que chegou
                LabProtocol.Response resp = getresponseArrived();
                
                //System.out.println("forwarding asnwer:"+resp.status + " args:"+(resp.args != null ? resp.args.length : "null"));
                if(forward)
                {
                    
                    if(resp.status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.binary)
                    {
                        if(resp.binary_response != null)
                        {
                            writeln_url(resp.status);
                            
                            writeln_url(""+resp.binary_response.length);
                            output.write(resp.binary_response);
                        }
                        else
                        {
                            writeln_url("SEM binary_response");
                        }
                        
                    }
                    else if(resp.status.equals("OK") && msg.cmd.responseType() == BroadcasterMessage.MessageResponse.text)
                    {
                        if(resp.args != null)
                        {
                            writeln_url(resp.status);
                        
                            writeln_url(""+resp.args.length);
                            for(String s : resp.args) writeln_url(s);
                        }
                        else
                        {
                            writeln_url("SEM args");
                        }
                    }
                }
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        else
        {
            if(forward)
            {
                writeln_url("OK");
            }
            return true;
        }
        */
    }
    public void IPC_new(Messages m,String[] args,byte[] binary_msg) throws IOException
    {
        BroadcasterMessage msg = new BroadcasterMessage("thread",m,args,binary_msg);
        try {
            _clientapp.IPC_handler.sendMessage(msg);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void tryShowSplashScreen(int splash_delay) throws IOException
    {
        if(splash_delay <= 0)
        {
            _clientapp.ShowedSplash = true;
            return;
        }
        //IPC_new(Messages.lockscreen,new String[]{""+splash_delay},null);
        if(IPC_exec(Messages.lockscreen.toString(), new String[]{""+splash_delay},null, Messages.lockscreen, true, false))
        {
            _clientapp.ShowedSplash = true;
        }
    }
    
    @Override
    public void processRequest() throws IOException {

        
        
        writeln_url(VERSAO);
        //https://stackoverflow.com/questions/797549/get-login-username-in-java
        //https://stackoverflow.com/questions/19990038/how-to-get-windows-username-in-java
        String user_name = CmdExec.getUser();
        if(user_name == null || user_name.isEmpty())
            user_name = System.getProperty("user.name");
        
        JSONObject client_info = new JSONObject();
        // valores obrigatorios
        client_info.put("user_name", user_name);
        client_info.put("uuid", ClientApp.UUID);
        client_info.put("OS", CheckOS.OS);
        client_info.put("OS_family", CheckOS.getFamily());
        
        String[] ip_mac = CmdExec.getIPandMAC(null);
        client_info.put("IP", ip_mac[0]);
        client_info.put("MAC", ip_mac[1]);
        // client info
        writeln_url(client_info.toJSONString());
        
        
        // NAO PRECISA MAIS DA SENHA - ERA SO ALGUEM FINGIR SER O CLIENTE E ROUBAVA A SENHA
        // IRA SE CONECTAR USANDO SSL,
        // PELO FATO DE A CONEXAO SSL SER BEM SUCEDIDA, PROVA QUE:
        // 1. o servidor tem a chave privada do único certificado que esse cliente confia
        // 2. essa chave privada está encriptada com uma senha, o que prova que foi descriptada
        // 3. o que prova que é uma pessoa autorizada, pois conseguiu descriptar usando a senha.
        
        // o que traz um problema: Com uma senha se controla todos os computadores.
        // deveria ser adicionado uma etapa a mais.
        // OBRIGAR o atacante a ter acesso FÍSICO ao computador do professor ( O que faria da senha uma formalidade )
        // Não apenas fingir o IP.
        // isso se torna verdade caso a keystore do servidor sendo diferente em cada sala.
        // e tomando todas as precauções necessárias ao instalar o servidor.
        // mesmo com a senha e um instalador, só daria acesso a uma sala, nao todas.
        
        // Resumindo para o serviodor funcionar ( assumindo posse do programa ):
        // 1. Ser Administrador do computador e ter o java policy files instalado.
        // 2. Ter o Ip de sua máquina igual ao ip que os clientes tentam se conectar
        // 3. Estar na mesma rede que os clientes ( supondo um ip privado )
        // 4. Ter o keystore correspondente.
        // 5. Saber a senha para descriptografar o keystore.
         
        // medidas de segurança ( Em ordem de importância )
        // I   Senha forte para a keystore
        // II  impedir acesso ao computador com a keystore ( Remoto ou físico )
        // III impedir acesso ao programa servidor / código fonte do servidor
        // IV  impedir acesso não autorizado a rede local -> permissão administrador na rede local
        //     - se for rede Wi-Fi, manter a senha segura, já configurada nas máquinas
        //     - se for cabeada, impedir conectar notebooks e etc...
        
        // a etapa II deve ser tomada como muito importante, pois num vazamento da senha
        // essa etapa deve dar uma margem de tempo suficiente para atualizar os clientes com um novo certificado.
        // antes que um ataque seja feito
        
        // deveria haver um gatilho para desativar temporariamente os cliente no caso da senha vazar
        // algo que fosse rápido para checar e difícil para alguém usar como meio de desligar o programa
        // SOLUÇÃO: dns dinâmico ou local para indicar o servidor.
        
        // TODO: naive security by obscurity, se nao consigo proteger mais que isso,
        // tornar o protocolo complicado e difícil de reversamente-egenheirar
        // pra q se vai ser postado no github?
        
        /*
        // le a senha do servidor
        String senha = read();
		// Validating a hash
        Inicio.out.println("Checando senha...");
        try
        {
            if (!BCrypt.checkpw(senha, serverPasswHash))
            //if(!server_passw.equals(senha))
            {
                write("Senha incorreta!");
                Inicio.out.println("Servidor com senha incorreta.");
                throw new ExitAppException("servidor não foi autenticado.",null,false);
            }
            Inicio.out.println("Servidor Autenticado!");
            write("OK");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            write("Senha incorreta!");
            Inicio.out.println("erro ao processar senha incorreta.");
            throw new ExitAppException("servidor não foi autenticado.",e,false);
        }
        */
        
        // para controlar o delay de reconexao e mudar o wifi sempre
        ClientApp.ConnectedOnce = true;
        //opcoes
        int splash_delay = ClientMain._delay_defaultsplash;
        
        try
        {
            String server_info = readln_url();
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(server_info);
            
            if(json.containsKey("splash_delay"))
            splash_delay = Integer.parseInt((String)json.get("splash_delay"));
            
        }
        catch(Exception e)
        {
            System.out.println("Erro ao carregar informações do servidor:");
            e.printStackTrace();
        }
        //int splash_delay = tryReadInt(ClientMain._delay_defaultsplash);
        
        // splash screen
        if(conn_index == 0 && splash_delay > 0)
        {
            if(ClientMain._master_slave){
                if(!_clientapp.ShowedSplash)tryShowSplashScreen(splash_delay);
            }
            else
            ScreenLocker.splash(splash_delay);
        }
        
        
        // registra o listener do slave
        /*_clientapp.IPC_status.registerResponseListener(-1,-1,new BroadcasterListener() {
            @Override
            public void onResponse(String threadname, String status, Object data) {
                if(data == null)
                {
                    System.err.println("UNKNOW RESPONSE:"+status);
                }
                else if(status.equals("OK"))
                {
                    //System.out.println("Response Arrived: "+status);
                    setResponseArrived((LabProtocol.Response) data);
                }
                else
                {
                    System.err.println("SLAVE STATUS NAO OK:"+status);
                    
                    setResponseArrived((LabProtocol.Response) data);
                }
            }
        });*/
        

        try
        {
        // espera por pedidos de execucao de comandos
        String linha;
        while(!(linha = readln_url()).isEmpty())
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
                    arguments[i] = readln_url();
                }
            }
            //System.out.println("Requisicao '"+linha+"'");
            
            BroadcasterMessage msg = new BroadcasterMessage("", Messages.valueOf(req), arguments);
            if(msg.cmd == Messages.exit)
            {
                writeln_url("OK");
                if(ClientMain._master_slave) IPC_forward(req,arguments,null,msg.cmd);
                throw new ExitAppException("saiu normalmente.",null,true);
            }
            //else if(msg.cmd == Messages.ping)
            //{
            //    writeln_url("pong");
            //    if(ClientMain._master_slave) IPC_new(Messages.ping,null,null);
            //}
            else
            {
                if(msg.cmd == Messages.admin_download && msg.arguments[0].equals("run_after_exit"))
                {
                    if(ClientMain._master_slave) IPC_new(Messages.exit,new String[]{},null);
                    Thread.sleep(1000);
                }
                
                boolean executeAsMasterAnyway = msg.cmd.executeAsBoth();
                if( msg.cmd.executeAsBoth() && ClientMain._master_slave)
                {
                    System.out.println("S "+msg.cmd.toString());
                    IPC_new(msg.cmd,msg.arguments,null);
                }
                else if(!(msg.cmd.executeAsMaster() || !ClientMain._master_slave)) // condição nada a ver
                {
                    System.out.println("S "+msg.cmd.toString());
                    if(!IPC_forward(req,arguments,null,msg.cmd))
                    {
                        _clientapp.slaveWasOnline = false;
                        if(msg.cmd.executeAsMasterIfSlaveOffline())
                        {
                            executeAsMasterAnyway = true;
                        }
                        else
                        {
                            writeln_url("SLAVE TIMEOUT");
                        }
                    }
                    else // se conseguiu enviar uma mensagem pro slave, e é a primeira vez, tentar mostrar o splash
                    {
                        _clientapp.slaveWasOnline = true;
                        if(!_clientapp.ShowedSplash)tryShowSplashScreen(splash_delay);
                    }
                }
                
                if(msg.cmd.executeAsMaster() || !ClientMain._master_slave || executeAsMasterAnyway)
                {
                    System.out.println("M "+msg.cmd.toString());
                    //String[] asnwer = LabProtocol.execute(msg);
                    //for(String s : asnwer) writeln_url(s);
                    LabProtocol.Response asnwer = LabProtocol.execute(msg);
                    writeln_url(asnwer.status);
                    if(msg.cmd.responseType() == BroadcasterMessage.MessageResponse.text)
                    {
                        writeln_url(""+asnwer.args.length);
                        for(String s : asnwer.args) writeln_url(s);
                    }
                    if(msg.cmd.responseType() == BroadcasterMessage.MessageResponse.binary)
                    {
                        writeln_url(""+asnwer.binary_response.length);
                        output.write(asnwer.binary_response);
                    }
                }
                

            }
            /*
            switch(req)
            {
                case "stop":
                    CmdExec.stop(arguments[0],true);
                    writeln_url("OK");
                break;
                case "shutdown":
                    CmdExec.shutdown();
                    writeln_url("OK");
                break;
                case "restart":
                    CmdExec.restart();
                    writeln_url("OK");
                break;
                case "logoff":
                    CmdExec.logoff();
                    writeln_url("OK");
                break;
                case "cancelshutdown":
                    CmdExec.cancelshutdown();
                    writeln_url("OK");
                break;
                case "blacklist":
                    if(arguments == null)
                    {
                       ClientApp.setBlackList(new ArrayList<>()); 
                    }
                    else
                    {
                       List<Program> p = new ArrayList<>();
                       for(String s : arguments)
                       {
                           p.add(Program.fromStr(s));    
                       }
                       ClientApp.setBlackList(p);
                    }
                    writeln_url("OK");
                break;
                case "exec": //-- inseguro e não portável
                    // retirar o 'exec'
                    try
                    {
                        List<String> output = CmdExec.readCmd("cmd","/c",arguments[0]);
                        writeln_url("OK");
                        writeln_url(""+output.size());
                        for(String s : output)
                        {
                            writeln_url(s);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        writeln_url("OK");
                        writeln_url("2");
                        writeln_url(e.getClass().getCanonicalName()+": "+e.getMessage());
                        if(e.getCause() != null)
                        {
                            writeln_url("\t"+e.getCause().getClass().getCanonicalName()+": "+e.getCause().getMessage());
                        }
                        else
                        {
                            writeln_url("no cause");
                        }
                    }
                    
                break;
                case "browse":
                {
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        String site_url = arguments[0];
                        ProgramOpener.browse(site_url);
                        writeln_url("OK");
                    }
                }
                break;
                case "start": // -- inseguro e não portável
                {
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        String program_path = arguments[0];
                        String program_process = arguments[1];
                        if(ProgramOpener.start(program_path,program_process))
                        {
                            writeln_url("OK");
                        }
                        else
                        {
                            writeln_url("Nao Pode abrir o programa: '"+program_path+"'");
                        }
                    }
                }
                break;
                case "exit":
                    writeln_url("OK");
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    throw new ExitAppException("saiu normalmente.",null,true);
                case "lockscreen":
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        ClientApp.locker.startLocking(false);
                        writeln_url("OK");
                    }
                break;
                case "broadcast": // rídículo, sem palavras.
                {
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        ClientApp.locker.startBroadcasting();
                        ClientApp.watcher.startTask();
                        writeln_url("OK");
                    }
                }
                break;
                case "unlockscreen":
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        ClientApp.watcher.stopTask();
                        ClientApp.locker.stopLocking();
                        writeln_url("OK");
                    }
                break;
                case "download":// se usar hash, +/- seguro.
                    // agora consegue passar arquivos grandes
                {
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        String action = arguments[0];
                        String filename = arguments[1];
                        int fileserver_port = Integer.parseInt(arguments[2]);
                        String hash = arguments[3];
                        String hashProtocol = arguments[4];
                        File dest_pathfile = ProgramOpener.parsePath("<downloads>/"+filename);
                        if(dest_pathfile == null)
                        {
                            writeln_url("Caminho Nao Aceito: '"+arguments[1]+"'");
                        }
                        else
                        {
                            FileDownloaderTask downloader = new FileDownloaderTask(
                                    ClientApp.UUID, 
                                    dest_pathfile, 
                                    ClientApp.serverAddress, 
                                    fileserver_port, 
                                    action.equals("run"),
                                    hash.equals("none") ? null : hash,
                                    hash.equals("none") ? null : hashProtocol
                            );
                            downloader.startTask();
                            writeln_url("OK");
                        }
                    }
                }
                break;
                
                case "exec-user": //-- inseguro e não portável
                    // retirar o 'exec'
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                        for(String cmd : arguments)
                        {
                            CmdExec.execCmd(cmd);
                        }
                        writeln_url("OK");
                    }
                    
                break;
                //case "bringwindow":
                //https://stackoverflow.com/questions/557166/bring-to-front-for-windows-xp-command-shell
                //break;
                case "msg":
                {
                    if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                    else
                    {
                            //final String txt = linha.substring(linha.indexOf(' ')+1);
                        final String txt = arguments[0];
                            new Thread(new Runnable(){public void run(){
                                    //JOptionPane.showMessageDialog(null, txt);
                            JDialog dialog = new JOptionPane(txt,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION).createDialog("Mensagem Recebida"); 
                            dialog.setAlwaysOnTop(true);
                            dialog.setVisible(true);
                            dialog.dispose();
                            }}).start();
                            writeln_url("OK");
                    }
                }
                break;
                case "ping":
                    writeln_url("pong");
                    if(ClientMain._master_slave) IPC_new(Messages.ping,null,null);
                break;
                default:
                    writeln_url("Comando '"+req+"' nao implementado");
                    //if(ClientMain._master_slave) IPC_forward(req,arguments,null);
                break;
            }
            */
        	
        }
        }
        catch(ExitAppException e){
            throw e;
        }
        catch(Exception e){
            System.out.println("Erro no loop de processamento dos requests:");
            e.printStackTrace();
        }
        finally
        {
            if(ClientApp.locker != null)
            {
                try {
                    ClientApp.locker.stopLocking();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(ClientApp.watcher != null)
            {
                try{
                    ClientApp.watcher.stopTask();
                }
                catch( Exception e){
                    e.printStackTrace();
                }
            }
                //if(kiosk_code != -1)
                //{
                //    ProgramOpener.kiosk_close(kiosk_code);
                //    kiosk_code = -1;
                //} precisa mesmo?
            System.out.println("Acabou!");
        }
    }
    
}
