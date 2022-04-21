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
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.UUIDable;
import br.erickweil.labamanger.common.files.ConfigsLoader;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.client.swing.ScreenLocker;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanager.taskkiller.Taskkiller;
import br.erickweil.labmanager.threadsafeness.BlockingListenerHelper;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.secure.SecureClient;
import br.erickweil.streaming.StreamWatcher;
import br.erickweil.streaming.tcpwrapper.TCPHilbertWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import br.erickweil.webserver.HttpClientProtocol;
import br.erickweil.webserver.HttpResponse;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebClient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe que trabalha com o cliente, ela que:
 * controla a blacklist
 * tem o loop de re-conexão, com troca automatica da rede wi-fi
 * tem o tratamento das exceções, escrevendo no diretório log
 * 
 * @author Usuario
 */
public class ClientApp implements Runnable, ProtocolFactory, Taskkiller.KillerHandler{

	public static String serverAddress;
	private final int serverPort;
	
        
	private static List<Program> blacklist;
	public static ScreenLocker locker;
        public static TCPHilbertWatcher watcher;
        public static String UUID;
        // handler de comunicação com o Slave, o programa em conta padrão de usuário
        public final ThreadSafeHandler<BroadcasterMessage> IPC_handler;
    // variavel que indica se conseguiu conectar ao menos uma vez nessa sessao
    public static boolean ConnectedOnce;
    public boolean slaveWasOnline;
    public static boolean slaveConnectedOnce;
    
    // para controlar se mostrou a splash screen para o usuario
    public boolean ShowedSplash;
	public static synchronized void setBlackList(List<Program> programs)
	{
		blacklist = programs;
                SaveConfigs();
	}
        
	public static synchronized List<Program> _getBlackList()
	{
		return blacklist;
	}
        
	@Override
	public synchronized List<Program> getBlackList()
	{
            return _getBlackList();
	}
    private int conn_index;
    public final ThreadSafeListener<BroadcasterListener> IPC_status;
    

	public ClientApp(String _uuid,String _serverAddress,int serverPort,
                ThreadSafeHandler<BroadcasterMessage> IPC_handler,
                ThreadSafeListener<BroadcasterListener> IPC_status
        )
	{
                UUID = _uuid;
		serverAddress = _serverAddress;
		this.serverPort = serverPort;
                this.IPC_handler = IPC_handler;
                this.IPC_status = IPC_status;
                blacklist = new ArrayList<>();
		LoadConfigs();
	}
        
        public void IPC_stopLocking()
        {
            IPC_message(BroadcasterMessage.Messages.unlockscreen,new String[]{},null);
        }
        
        public void IPC_ping()
        {
            IPC_message(BroadcasterMessage.Messages.ping,new String[]{},null);
        }
        
        public void IPC_message(BroadcasterMessage.Messages m,String[] args,byte[] binary_msg)
        {
            BroadcasterMessage msg = new BroadcasterMessage("thread",m,args,binary_msg);
            try {
                IPC_handler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	
	public void run()
	{
            System.out.println("ClientApp inicando...");
            long startup_time = System.currentTimeMillis();
            Taskkiller taskkiller = null;
            //if(!ClientMain._testing)
            //{
                if(ClientMain._master_slave)
                    taskkiller = new Taskkiller(false,this); // só o slave pode matar janelas
                else
                    taskkiller = new Taskkiller(true,this);
                Thread taskkiller_thread = new Thread(taskkiller);
                taskkiller_thread.start();
            //}
            
            if(!ClientMain._master_slave)
            {
                locker = new ScreenLocker();
                watcher = new TCPHilbertWatcher(ClientMain._endereco,ClientMain._STREAM_port, new StreamWatcher.ImageListener() {
                    @Override
                    public void setImage(BufferedImage img) {
                        if(ClientApp.locker != null)
                            ClientApp.locker.changeBroadcastImg(img);
                    }
                });
            }
            try
            {
                try
                {
                    int tries = 0;
                    while(true)
                    {
                        // obter endereço local ao qual se deve conectar
                        if(ClientMain._synchronize_remote && tries % 5 == 0)
                        {
                            synchronizeWithRemote();
                        }
                        
                        connect();
                        
                        long runing_time = System.currentTimeMillis() - startup_time;
                        System.out.println("Reconectando...");

                        // Para de bloquear a tela. Porque se está desconectado não há como
                        // desbloquear mais a tela pelo comando.
                        if(!ClientMain._master_slave)
                        {
                            locker.stopLocking();
                            watcher.stopTask();
                        }
                        else
                        {
                            IPC_stopLocking();
                        }
                        
                        if(ClientMain._master_slave)
                        {
                            // envia uma solicitação ping para o slaveApp, para nao desconectar por Timeout
                            IPC_ping();
                        }


                        // depois de dois minutos sem sucesso,
                        // tenta se conectar de minuto em minuto para diminuir processamento
                        // mas se tiver conectado pelo menos uma vez,
                        // sempre tenta se conectar de dois em dois segundos

                        // dois segundos para evitar entrar em loop de conexões recusadas
                        // e sobrecarregar o computador
                        if(runing_time < ClientMain._timeout_noserver || ConnectedOnce)
                        {                        
                            if(ClientMain._wifi_autoconnect)
                            {
                                checkWifiNetwork();
                                if(ClientMain._delay_fastcheck < ClientMain._delay_wifiswitch)
                                Thread.sleep(ClientMain._delay_wifiswitch - ClientMain._delay_fastcheck); // espera a troca da rede tomar efeito
                            }
                            Thread.sleep(ClientMain._delay_fastcheck);
                        }
                        else
                        {
                            Thread.sleep(ClientMain._delay_slowcheck);
                        }

                        tries++;
                    }
                }
                catch (ExitAppException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new ExitAppException("Erro inesperado:", e, false);
                }
            }
            catch (ExitAppException e) {
                e.printStackTrace();
                if(e.getCause() != null)
                e.getCause().printStackTrace();
                // se nao saiu normalmente
                if(!e.normal)
                {
                    if(!ClientMain._testing)
                    {
                        try{                
                            long runing_time = System.currentTimeMillis() - startup_time;

                            LocalDateTime now = LocalDateTime.now();
                            String format3 = now.format(DateTimeFormatter.ofPattern("HH-mm-ss dd-MM-yyyy", Locale.getDefault()));
                            //File errfile = new File("log "+format3+".txt");
                            File errfile = Inicio.errFile;
                            PrintWriter writer = new PrintWriter(new FileOutputStream(errfile));
                            writer.write("CLIENTAPP CRASHED! \r\n\r\n");
                            writer.write("date: "+format3+" \r\n\r\n");
                            writer.write("execution time: "+runing_time+" ms\r\n\r\n");
                            String user = CmdExec.getUser();
                            writer.write("usuario:"+user+" \r\n\r\n");
                            e.printStackTrace(writer);
                            writer.flush();
                            writer.close();
                        } 
                        catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                else
                {
                    // parando serviço para nao reiniciar
                    CmdExec.stopService("labmanager_master");
                }
            }
            finally
            {
                //setBlackList(new ArrayList<>());
                //if(!ClientMain._testing)
                //{
                    taskkiller.stop();

                    //System.out.println("terminado, startando explorer...");
                    //ProgramOpener.start("C:/Windows/explorer\\.exe","explorer.exe");
                    System.exit(0);
                //}
            }

	}
	
	private void connect()
	{        
        SecureClient client = null;
        try
        {
            client = new SecureClient(serverAddress,serverPort, this, "client_trustore.jks", "1a2b3c4d");
            client.LOG = true;
        }
        catch(SecurityException | NullPointerException | IllegalArgumentException e)
        {
            throw new ExitAppException("Erro ao Criar socket seguro:", e, false);
        }
        
        try
        {
            client.run();
        }
        catch(SecurityException  | NullPointerException | IllegalArgumentException e)
        {
            throw new ExitAppException("Erro ao conectar com socket seguro:", e, false);
        }
	}
        
        private boolean synchronizeWithRemote()
        {
            try
            {
            SecureClient client = null;
            HashMap<String,String> cookies = new HashMap<>();
            
            final HttpClientProtocol httpprotocol = new HttpClientProtocol(
            ClientMain._synchronize_remote_host,
            ClientMain._synchronize_remote_uri,
            cookies,
            "GET",
                    "version=1"
                    +"&action=synchronize"
                    +"&uuid="+URLEncoder.encode(ClientMain._uuid,"UTF-8")
                    +"&extra="+URLEncoder.encode(ClientMain._extra,"UTF-8"));
            
            try
            {
                client = new SecureClient(ClientMain._synchronize_remote_host,443, 
                new ProtocolFactory() {
                    @Override
                    public ServerProtocol get() {
                        return httpprotocol;
                    }
                }
                , null, null);
                client.LOG = false;
            }
            catch(SecurityException | NullPointerException | IllegalArgumentException e)
            {
                throw new ExitAppException("Erro ao Criar socket seguro:", e, false);
            }

            try
            {
                client.run();
            }
            catch(SecurityException  | NullPointerException | IllegalArgumentException e)
            {
                throw new ExitAppException("Erro ao conectar com socket seguro:", e, false);
            }
            
            try
            {
                HttpResponse response = httpprotocol.response;
                if(!response.status_code.equals("200"))
                    return false;

                String responseText = response.getResponseAsText();
                if(responseText == null || responseText.trim().isEmpty())
                    return false;
                
                System.out.println("Resposta do servidor de sincronização:\n"+responseText);
                
                int indexOfLn =  responseText.indexOf("\n");
                String responseStatus = responseText.substring(0, indexOfLn);            
                
                if(!responseStatus.equals("OK"))
                    return false;
                
                String responseBody = responseText.substring(indexOfLn + 1, responseText.length());
                
                JSONObject json = (JSONObject) new JSONParser().parse(responseBody);
                
                boolean changedConf = false;
                if(json.containsKey("endereco"))
                {
                    String endereco = (String) json.get("endereco");
                    if(endereco != null && !endereco.trim().isEmpty())
                    {
                        changedConf = !ClientMain._endereco.equals(endereco);
                        ClientMain._endereco = endereco;
                    }
                }
                
                if(json.containsKey("server_port"))
                {
                    int port = (int) json.get("server_port");
                    changedConf = ClientMain._server_port != port;
                    ClientMain._server_port = port;
                }
                
                if(changedConf) ClientMain.conf.Save();
                
                return true;
            }
            catch(IOException | ParseException e)
            {
                e.printStackTrace();
            }
            
            
            }
            catch(UnsupportedEncodingException e)
            {
                throw new ExitAppException("Erro ao Montar requisição:", e, false);
            }
            
            return false;
        }
        
        
    public void checkWifiNetwork()
    {

        
        // mudar para a rede correta
        try{
            CmdExec.changeToWifi(ClientMain._wifi_network);
        }
        catch(Exception e)
        {
            System.out.println("Erro ao tentar mudar a rede:");
            e.printStackTrace(System.out);
        }
        
    }

    @Override
    public ServerProtocol get() {
        // TODO Auto-generated method stub
        //if(ClientMain._testing)
        //{
        //    return new FakeClientCmdProtocol( conn_index++);
        //}
        //else
        //{
            return new ClientCmdProtocol( conn_index++, this);
        //}
    }
    
    public static void SaveConfigs()
    {
        File file = ProgramOpener.parsePath("<conf>/programs.json");
        ConfigsLoader.SaveConfigs(blacklist, file);
    }
    
    public static void LoadConfigs()
    {
        File file = ProgramOpener.parsePath("<conf>/programs.json");
        ConfigsLoader.LoadConfigs(blacklist, file);
    }

    @Override
    public boolean connectedOnce() {
        return ConnectedOnce;
    }

    @Override
    public List<CmdProcess> getWindowList() {
            final List<CmdProcess> retList = new ArrayList<>();
            
            if(!slaveWasOnline) return retList;
            try {
                BlockingListenerHelper listener = new BlockingListenerHelper();
                
                UUIDable response = listener.sendAndWaitResponse(
                        new BroadcasterMessage("thread", BroadcasterMessage.Messages.windowlist, new String[]{}),
                        1,
                        IPC_handler,
                        IPC_status,
                        100
                );
                
                if(response != null)
                {
                    LabProtocol.Response resp = (LabProtocol.Response)response;
                    if(resp.status.equals("OK"))
                    {
                        //System.out.println("got "+resp.args.length);
                        for(int i=0;i<resp.args.length;i++)
                        {
                            String line = resp.args[i];
                            //System.out.println(i+":"+line);
                            try {
                                JSONParser parser = new JSONParser();
                                JSONObject json = (JSONObject) parser.parse(line);
                                retList.add(CmdProcess.fromJSON(json));
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                /*WebClient client = null;
                final List<CmdProcess> retList = new ArrayList<>();
                try
                {
                client = new WebClient(ClientMain.IPC_host,ClientMain.ASYNC_port, new ProtocolFactory() {
                @Override
                public ServerProtocol get() {
                return new ServerProtocol() {
                @Override
                public void processRequest() throws IOException {
                writeln_url("windowlist");
                int count = Integer.parseInt(readln_url());
                System.out.println("got "+count);
                for(int i=0;i<count;i++)
                {
                String line = readln_url();
                System.out.println(i+":"+line);
                try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(line);
                retList.add(CmdProcess.fromJSON(json));
                } catch (ParseException ex) {
                Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
                readln_url(); // END
                }
                @Override
                public int getTimeout()
                {
                return 100;
                }
                };
                }
                });
                }
                catch(SecurityException | NullPointerException | IllegalArgumentException e)
                {
                throw new ExitAppException("Erro ao Criar socket Async:", e, false);
                }
                
                try
                {
                client.run();
                }
                catch(SecurityException  | NullPointerException | IllegalArgumentException e)
                {
                throw new ExitAppException("Erro ao conectar com socket Async:", e, false);
                }
                
                
                return retList;*/
            } catch (IOException ex) {
                Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return retList;
    }
}
