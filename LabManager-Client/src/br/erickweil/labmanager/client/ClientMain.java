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
import br.erickweil.labmanager.client.slave.SlaveApp;
import br.erickweil.labmanager.client.slave.SlaveAsyncProtocol;
import br.erickweil.labmanager.client.slave.SlaveServerProtocol;
import br.erickweil.labmanager.client.slave.SlaveStatusManager;
import br.erickweil.labmanager.cmd.ProgramOpener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.secure.SecureClient;
import br.erickweil.secure.SecureServer;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerHttp;
import br.erickweil.webserver.ServerHttpProxy;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain {
	
    public static String _testendereco = "localhost";
	public static String _endereco = "localhost";
    
    
    // mudar o wifi automaticamente para a rede correta
    public static boolean _wifi_autoconnect = false;
    public static String _wifi_network = "WIFI";
    public static String _service_name = "labmanager_master";
    public static int _server_port = 22133;
    
    public static boolean _master_slave = true;
    public static final String IPC_host = "127.0.0.1";
    public static final int IPC_port = 22134;
    public static final int ASYNC_port = 22136;
    public static int _STREAM_port = 22135;
    
    public static String _uuid = "none";
	// 123456 hash
	//public static String _serverpasshash = "$s0$100801$wpvi6RatmHTufOf0J11XFg==$nUUrwbIYPjQdC6pT/GCpSPtB9LIHhN+a5FYm2g80YbU=";
    public static boolean _testing = false;
    public static int _testsize = 40;
    // timeout para assumir que o servidor está offline. 
    // Diminui o uso do computador após esse tempo, para não sobrecarregar sem necessidade.
    public static int _timeout_noserver = 120000;
    // Tempo de checagem rápida e lenta. são os tempos de checagem:
    // se não conseguir conectar ao servidor antes do timeout acima
    // irá passar a checar se o servidor está ativo usando o slowcheck.
    // obs: se conectar ao menos uma vez, sempre irá usar o fastcheck
    public static int _delay_fastcheck = 2000;
    public static int _delay_slowcheck = 60000;
    // Tempo mínimo que deve aguardar após tentar trocar a rede wifi (_wifi_autoconnect == true)
    public static int _delay_wifiswitch = 4000;
    public static int _delay_defaultsplash = 15000;
    
    // Tempo de checagem dos programas
    public static int _delay_taskkiller = 1500;
    
    public static boolean isMaster = true;
    
    public static String genUuid() {
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890+/";
        StringBuilder sb = new StringBuilder();
        //Random random = new Random();
        SecureRandom rdn = new SecureRandom();
        for (int i = 0; i < 30; i++) {
            
            sb.append(candidateChars.charAt(rdn.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }
    
    public static void startMaster() throws IOException, InterruptedException {
        isMaster = true;
        start();
        final ThreadSafeHandler<BroadcasterMessage> IPC_handler;
        IPC_handler = new ThreadSafeHandler<>(false);
        
        final ThreadSafeListener<BroadcasterListener> IPC_status = new SlaveStatusManager();
        WebServer IPC_server = new WebServer(IPC_port, new ProtocolFactory() {
        @Override
        public ServerProtocol get() {
            return new SlaveServerProtocol(IPC_handler,IPC_status);
        }
        });
        // garante que só aceitará conecções vindas do mesmo computador
        IPC_server._bind_address = InetAddress.getLoopbackAddress();
        
        new Thread(IPC_server).start();
 
        ClientApp client = new ClientApp(_uuid,_endereco,_server_port,IPC_handler,IPC_status);
        new Thread(client).start();
    }
    
    public static void startSlave() throws IOException, InterruptedException {
        isMaster = false;
        start();
        
        WebServer Async_server = new WebServer(ASYNC_port, new ProtocolFactory() {
        @Override
        public ServerProtocol get() {
            return new SlaveAsyncProtocol();
        }
        });
        // garante que só aceitará conecções vindas do mesmo computador
        Async_server._bind_address = InetAddress.getLoopbackAddress();
        
        new Thread(Async_server).start();
        
        
        SlaveApp client = new SlaveApp(_uuid,_endereco);
        new Thread(client).start();
    }
    
	public static void start() throws IOException, InterruptedException {
           
            try {
			Configurable conf = new Configurable(new SimpleLogger(),ClientMain.class, "config_cliente");
                        // gera um uuid com uma chance baixa de colisao ( uma em 64^30 )
                        if(_uuid.equals("none"))
                        {
                            if(_testing)
                            {
                                _uuid = "HJEtpJYqh/g33qoYSuf2PrdftjdMT3";
                            }
                            else
                            {
                                _uuid = genUuid();
                                conf.Save(); // deve rodar como admin pra funcionar.
                                // instalador deve rodar pelo menos uma vez o programa como admin, ou criar o arquivo conf com a uuid
                            }
                        }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
                Inicio.start();
        if(_testing)
        {
            //for(int i=0;i<_testsize;i++)
            //{
                
            //    ClientApp client = new ClientApp(_uuid,_testendereco,server_port,null);
                //int random = (short)(System.nanoTime()^System.currentTimeMillis()^(new Random().nextInt()));
            //    new Thread(client,("test"+i)).start();   
            //}
        }
        else
        {
            //Inicio.start();
            //if(_userinit) // para iniciar o LabManager ANTES do explorer
            //{
            //    new Thread(new Runnable() {
            //        @Override
            //        public void run() {
            //            try {
            //                Thread.sleep(3000);
            //                ProgramOpener.start("%windir%/System32/userinit.exe","userinit.exe");
            //            } catch (InterruptedException ex) {
            //                Logger.getLogger(ClientCmdProtocol.class.getName()).log(Level.SEVERE, null, ex);
            //            }
            //        }
            //    }).start();
           // }
        }

	}
	


}
