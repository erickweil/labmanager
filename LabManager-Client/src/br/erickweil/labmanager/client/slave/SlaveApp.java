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

import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.files.ConfigsLoader;
import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.client.ExitAppException;
import br.erickweil.labmanager.client.swing.ScreenLocker;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanager.taskkiller.Taskkiller;
import br.erickweil.streaming.StreamWatcher;
import br.erickweil.streaming.tcpwrapper.TCPHilbertWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebClient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Usuario
 */
public class SlaveApp implements Runnable, ProtocolFactory{
    public static final int CLIENT_CONECTED = 1;
    public static final int CLIENT_DISCONECTED = 2;
    public static final int CLIENT_WAITING = 3;
    public static final int CLIENT_PROCESSING = 4;
    public static final int CLIENT_RESPONSE = 5;
    
        public static ScreenLocker locker;
        public static TCPHilbertWatcher watcher;
        // variavel que indica se conseguiu conectar ao menos uma vez nessa sessao
    public static boolean ConnectedOnce;
    public static String serverAddress;
    public static String UUID;
        
	//@Override
	//public synchronized List<Program> getBlackList()
	//{
        //    return _getBlackList();
	//}
    
    //@Override
    //public boolean connectedOnce() {
    //    return ConnectedOnce;
    //}
    
    public SlaveApp(String _uuid,String _serverAdderss)
    {
        UUID = _uuid;
        serverAddress = _serverAdderss;
        //blacklist = new ArrayList<>();
        //LoadConfigs();
    }
    	@Override
	public ServerProtocol get() {
		// TODO Auto-generated method stub
            return new SlaveCmdProtocol();
	}

    @Override
    public void run() {
        long startup_time = System.currentTimeMillis();
        //Taskkiller taskkiller = null;
            //if(!ClientMain._testing)
            //{
            //    taskkiller = new Taskkiller(false,true,this); // o master já está matando os processos, aqui só pra matar janela
            //    Thread taskkiller_thread = new Thread(taskkiller);
            //    taskkiller_thread.start();
        
        locker = new ScreenLocker();
        watcher = new TCPHilbertWatcher(ClientMain._endereco,ClientMain._STREAM_port, new StreamWatcher.ImageListener() {
            @Override
            public void setImage(BufferedImage img) {
                if(SlaveApp.locker != null)
                    SlaveApp.locker.changeBroadcastImg(img);
            }
        });
        System.out.println("SlaveApp inicando...");
        try
            {
                try
                {
                    while(true)
                    {
                        connect();
                        long runing_time = System.currentTimeMillis() - startup_time;
                        System.out.println("Reconectando...");

                        // Para de bloquear a tela. Porque se está desconectado não há como
                        // desbloquear mais a tela pelo comando.
                        locker.stopLocking();
                        watcher.stopTask();


                        // depois de dois minutos sem sucesso,
                        // tenta se conectar de minuto em minuto para diminuir processamento
                        // mas se tiver conectado pelo menos uma vez,
                        // sempre tenta se conectar de dois em dois segundos

                        // dois segundos para evitar entrar em loop de conexões recusadas
                        // e sobrecarregar o computador
                        if(runing_time < ClientMain._timeout_noserver || ConnectedOnce)
                        {
                            Thread.sleep(ClientMain._delay_fastcheck);
                        }
                        else
                        {
                            Thread.sleep(ClientMain._delay_slowcheck);
                        }
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
                            writer.write("SLAVEAPP CRASHED! \r\n\r\n");
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
            }
            finally
            {
                //taskkiller.stop();
                //if(!ClientMain._testing)
                //{
                    //System.out.println("terminado, startando explorer...");
                    //ProgramOpener.start("C:/Windows/explorer\\.exe","explorer.exe");
                    System.exit(0);
                //}
            }
    }
    
    private void connect()
    {        
        WebClient client = null;
        try
        {
            client = new WebClient(ClientMain.IPC_host,ClientMain.IPC_port, this);
        }
        catch(SecurityException | NullPointerException | IllegalArgumentException e)
        {
            throw new ExitAppException("Erro ao Criar socket IPC:", e, false);
        }

        try
        {
            client.run();
        }
        catch(SecurityException  | NullPointerException | IllegalArgumentException e)
        {
            throw new ExitAppException("Erro ao conectar com socket IPC:", e, false);
        }
    }
     /*public static void SaveConfigs()
    {
        File file = ProgramOpener.parsePath("<conf>/programs.json");
        ConfigsLoader.SaveConfigs(blacklist, file);
    }
    
    public static void LoadConfigs()
    {
        File file = ProgramOpener.parsePath("<conf>/programs.json");
        ConfigsLoader.LoadConfigs(blacklist, file);
    }*/
    
}
