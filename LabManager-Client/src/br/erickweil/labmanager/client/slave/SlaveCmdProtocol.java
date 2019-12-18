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
package br.erickweil.labmanager.client.slave;

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.files.JpegHelper;
import br.erickweil.labmanager.client.ClientApp;
import br.erickweil.labmanager.client.ClientCmdProtocol;
import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.client.ExitAppException;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.start.Inicio;
import br.erickweil.labmanger.filetransmission.FileDownloaderTask;
import br.erickweil.streaming.StreamWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import br.erickweil.webserver.ReaderWriter;
import br.erickweil.webserver.ServerProtocol;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Usuario
 */
public class SlaveCmdProtocol extends ServerProtocol{
    
        public static String genUuid() {
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
        StringBuilder sb = new StringBuilder();
        //Random random = new Random();
        SecureRandom rdn = new SecureRandom();
        for (int i = 0; i < 30; i++) {
            
            sb.append(candidateChars.charAt(rdn.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }
    
     @Override
    public void processRequest() throws IOException {
        
        // deveria checar se conectou com o programa correto,
        // com uma prova de que está no mesmo computador
        // e de que ele tem controle administrador
        System.out.print("Checando Servidor");

        // garante pela rede que está no mesmo pc
        if(!socket.getInetAddress().isLoopbackAddress())
        {
            throw new ExitAppException("conexão IPC não validada.",null,true);
        }
        System.out.print(".");
        // garante que o outro programa tem acesso ao computador, pelo menos como usuário padrão
        // e que está executando na mesma pasta, e se for Arquivos de programas tem q ser Admin
        // se o outro programa tem acesso ao computador, e usa esse para causar danos,
        // ele mesmo poderia causar danos, n faz nem sentido ter mais uma etapa aqui
        String gen_uuid = genUuid();
        writeln_url(gen_uuid);
        readln_url();
        File arquivo_verificador = new File(gen_uuid+".txt");
        if(!(arquivo_verificador).exists())
        {
            throw new ExitAppException("conexão IPC não validada..",null,true);
        }
        System.out.print(".");
        writeln_url("OK");
        System.out.println(" TUDO OK!");
        SlaveApp.ConnectedOnce = true;
        
        /*StreamWatcher watcher = new HilbertWatcher(ClientMain._endereco,ClientMain._STREAM_port, new StreamWatcher.ImageListener() {
            @Override
            public void setImage(BufferedImage img) {
                if(SlaveApp.locker != null)
                    SlaveApp.locker.changeBroadcastImg(img);
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
            System.out.println("Requisicao '"+linha+"'");
            
            BroadcasterMessage msg = new BroadcasterMessage("", BroadcasterMessage.Messages.valueOf(req), arguments);
            if(msg.cmd == BroadcasterMessage.Messages.exit)
            {
                writeln_url("OK");
                throw new ExitAppException("saiu normalmente.",null,true);
            }
            else
            {
                if(!msg.cmd.executeAsMaster())
                {
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
                else
                {
                    System.err.println("não pode executar como slave");
                    writeln_url("Não pode executar como slave");
                }
            }
            /*
            switch(req)
            {
                case "browse":
                {
                    String site_url = arguments[0];
                    ProgramOpener.browse(site_url);
                    writeln_url("OK");
                }
                break;
                case "start": // -- inseguro e não portável
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
                break;
                case "exit":
                    writeln_url("OK");
                    throw new ExitAppException("saiu normalmente.",null,true);
                case "lockscreen":
                    SlaveApp.locker.startLocking(false);
                    writeln_url("OK");
                break;
                case "broadcast": // rídículo, sem palavras.
                {
                    //SlaveApp.locker.startBroadcasting();
                    //
                    //int size = Integer.parseInt(arguments[0]);
                    //byte[] file = new byte[size];
                    //input.readFully(file);
                    //InputStream inImg = new ByteArrayInputStream(file);
                    //BufferedImage image = ImageIO.read(inImg);
                    //BufferedImage image = JpegHelper.decompress_jpeg(file);
                    //SlaveApp.locker.changeBroadcastImg(image);
                    SlaveApp.locker.startBroadcasting();
                    watcher.startTask();
                    writeln_url("OK");
                }
                break;
                case "unlockscreen":
                    watcher.stopTask();
                    SlaveApp.locker.stopLocking();
                    writeln_url("OK");
                break;
                case "download":// -- inseguro.
                    // nao deve passar arquivos grandes, maiores que 5 Mb
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
                            SlaveApp.UUID, 
                            dest_pathfile, 
                            SlaveApp.serverAddress, 
                            fileserver_port, 
                            action.equals("run"),
                            hash.equals("none") ? null : hash,
                            hash.equals("none") ? null : hashProtocol
                        );
                        downloader.startTask();
                        writeln_url("OK");
                    }
                    /*String action = arguments[0];
                    File dest_pathfile = ProgramOpener.parsePath("<downloads>/"+arguments[1]);
                    if(dest_pathfile == null)
                    {
                        writeln_url("Caminho Nao Aceito: '"+arguments[1]+"'");
                    }
                    else
                    {
                        //int size = Integer.parseInt(arguments[2]);
                        //byte[] file = new byte[size];
                        //input.readFully(file);
                        //Files.write(dest_pathfile.toPath(), file);
                        if(action.equals("run"))
                        {
                            if(ProgramOpener.start("<downloads>/"+arguments[1],null))
                            {
                                writeln_url("OK");
                            }
                            else
                            {
                                writeln_url("Nao Pode abrir o programa: '"+dest_pathfile.getAbsolutePath()+"'");
                            }
                        }
                        else
                        {
                            writeln_url("OK");
                        }
                    }/*<<
                }
                break;
                case "exec-user": //-- inseguro e não portável
                    // retirar o 'exec'
                    for(String cmd : arguments)
                    {
                        CmdExec.execCmd(cmd);
                    }
                    writeln_url("OK");
                break;
                case "msg":
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
                break;
                //case "question": // muito mal feito, se o usuario nao responder, trava o inteiro programa
                //{
                //	//final String txt = linha.substring(linha.indexOf(' ')+1);
                //    final String txt = arguments[0];
                //	
                //    int dialogButton = JOptionPane.YES_NO_OPTION;
                //    int dialogResult = JOptionPane.showConfirmDialog(null, txt, "Por favor, Responda", dialogButton, JOptionPane.QUESTION_MESSAGE);
                //    if (dialogResult == JOptionPane.YES_OPTION) {
                //        write("YES");
                //    }
                //    else
                //    {
                //        write("NO");
                //    }
                //}
                //break;
                case "ping":
                    writeln_url("pong");
                break;
                default:
                    writeln_url("Comando '"+req+"' nao implementado");
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
            if(SlaveApp.locker != null)
            {
                try {
                    SlaveApp.locker.stopLocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //if(kiosk_code != -1)
                //{
                //    ProgramOpener.kiosk_close(kiosk_code);
                //    kiosk_code = -1;
                //} precisa mesmo?
            }
            if(SlaveApp.watcher != null)
            {
                try{
                    SlaveApp.watcher.stopTask();
                }
                catch( Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("Acabou!");
        }
    }
}
