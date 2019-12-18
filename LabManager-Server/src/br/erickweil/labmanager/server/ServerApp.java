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
import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.files.JpegHelper;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.files.ConfigsLoader;
import br.erickweil.labamanger.common.files.TestEncrypt;

import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labmanger.filetransmission.FileUploaderTask;
import br.erickweil.secure.SecureServer;
import br.erickweil.streaming.StreamBroadcaster;
import br.erickweil.streaming.StreamBroadcaster.Broadcast_Mode;
import br.erickweil.streaming.broadcasters.HilbertBroadcaster;
import br.erickweil.streaming.tcpwrapper.TCPHilbertBroadcaster;
import br.erickweil.streaming.tcpwrapper.TCPStreamBroadcaster;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe que serve de interface entre o servidor e a interface gráfica <br/>
 * Sem contudo, ter alguma dependência com a interface gráfica <br/>
 * Sim, poderiam se ter duas interfaces gráficas, e cada uma usar essa classe, e as
 * duas enviariam os mesmos comandos.
 * @author Usuario
 */
public class ServerApp implements ProtocolFactory {

    public static final int CLIENT_CONECTED = 1;
    public static final int CLIENT_DISCONECTED = 2;
    public static final int CLIENT_WAITING = 3;
    public static final int CLIENT_PROCESSING = 4;
    public static final int CLIENT_RESPONSE = 5;

    private final int serverPort;
    private String serverPassword;
    public final List<Program> programs;
    public final ThreadSafeHandler<BroadcasterMessage> handler;
    public ThreadSafeListener<BroadcasterListener> listener;
    public SecureServer server;
    public TCPStreamBroadcaster stream_broadcaster;
    int count;

    public ServerApp(int serverPort, String serverPassword) {
        this.serverPort = serverPort;
        this.serverPassword = serverPassword;
        handler = new ThreadSafeHandler();
        this.programs = new ArrayList<>();
        this.stream_broadcaster = new TCPHilbertBroadcaster(ServerMain._STREAM_port, (float)ServerMain._stream_framerate,ServerMain._stream_width,ServerMain._stream_fullframeinterval,ServerMain._stream_threshold);
        this.stream_broadcaster.max_bandwith = ServerMain._stream_maxbandwidth;
        LoadConfigs();
    }

    @Override
    public ServerProtocol get() {
        CmdBroadcasterProtocol prot = new CmdBroadcasterProtocol("Conn" + (count++), handler, listener, programs, ServerMain._solve_hostname);
        //prot.address_allowed = ServerMain._auth_clients_by_address ? Arrays.asList(ServerMain._client_address_allowed) : null;
        //prot.username_allowed = ServerMain._auth_clients_by_username ? Arrays.asList(ServerMain._client_username_allowed) : null;
        return prot;
    }

    public boolean isStopped() {
        return server.isStopped();
    }

    /**
     * A chave privada e o certificado do servidor são decriptados de acordo com
     * a senha do servidor, e o servidor é iniciado.
     * 
     * <strong> dependendo da configuração, pode levar até 3 segundos para descriptar!</strong>
     * @throws Exception 
     */
    public void startServer() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");

        File origem_enc = new File("server_keystore.jks.aes");
        byte[] decryptedText = null;
        if(origem_enc.exists() && serverPassword != null)
        {
            byte[] cipherText = Files.readAllBytes(origem_enc.toPath());

            System.out.println("Decrypting...");
            
            try {
                decryptedText = TestEncrypt.decryptWithPassw(cipherText, serverPassword);
            } catch (Exception ex) {
                System.out.println("Erro ao decryptar o keystore, exceçao:" + ex.toString());
                throw new RuntimeException("Erro ao decryptar o keystore, exceçao:" + ex.toString(), ex);
            }

            // sei la, vai que... eu nao sei.
            serverPassword = null;
        }
        else
        {
            File origem = new File("server_keystore.jks");
            decryptedText = Files.readAllBytes(origem.toPath());
        }

        SSLContext sc = null;
        try {
            InputStream keyStream = new ByteArrayInputStream(decryptedText);
            char[] keystore_pass = "1a2b3c4d".toCharArray();
            ks.load(keyStream, keystore_pass);

            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                //System.out.println("alias certificates :"+alias);
                if (ks.isKeyEntry(alias)) {
                    ks.getKey(alias, keystore_pass);
                }
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keystore_pass);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);
        } catch (Exception ex) {
            System.out.println("Erro ao carregar o keystore a partir do byte[], exceçao:" + ex.toString());
            throw new RuntimeException("Erro ao carregar o keystore a partir do byte[], exceçao:" + ex.toString(), ex);
        }
        

        //server = new SecureServer(serverPort,this,"server_keystore.jks", "1a2b3c4d");
        server = new SecureServer(serverPort, this, sc);
        server.start();

    }

    /**
     * Encerra as Threads e fecha o servidor.
     */
    public void stopServer() {
        try {
            // fechar os programas em todos os pcs
            //handler.sendMessage("all EXIT");
            handler.sendMessage(null);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.trystop();
    }

    /**
     * Envia uma mensagem.
     * 
     * <p>irá adicionar a mensagem no ThreadSafeHandler. ela será lida por 
     * todas as Threads, mesmo que elas estejam ocupadas agora.</p>
     * 
     * <p>as mensagens serão enviadas 
     * assim que as Threads clientes receberem o sinal para tal.
     * pode ser que um cliente lento receba esta mensagem em um tempo conderável no futuro.</p>
     * 
     * <p> basicamente, está construindo o resto da mensagem, e enviando para o handler como um BroadcasterMessage </p>
     * @param threadName o nome da thread cliente que deve escutar, ou BroadcasterMessage.All para todos
     * @param cmd a mensagem a ser enviada
     * @param args mais informações específicas ( caminhos de arquivos, site, programa, etc...)
     */
    public void sendMessage(BroadcasterMessage msg, BroadcasterListener onResponse) throws InterruptedException {
        if(onResponse != null && msg.cmd.responseType() != BroadcasterMessage.MessageResponse.none)
        {
            listener.registerResponseListener(msg.msg_uuid,msg.threadname.equals(BroadcasterMessage.All) ? -1 : 1,onResponse);
        }
        handler.sendMessage(msg);
    }
    
    private void _sendMessage(String[] threadName,Messages cmd, BroadcasterListener onResponse, String[] args) throws InterruptedException {
        for(String thread : threadName)
        {
            BroadcasterMessage msg = new BroadcasterMessage(thread, cmd, args);
            sendMessage(msg,onResponse);
        }
    }
    
    public void sendMessage(String[] threadName, Messages cmd, BroadcasterListener onResponse, String... args) {
        try {
            switch (cmd) {
                case start:
                    for (Program p : programs) {
                        if (p.name.equalsIgnoreCase(args[0]) && !p.path.isEmpty()) {
                            String[] new_args = new String[]{p.path, p.process};
                            _sendMessage(threadName,cmd,onResponse,new_args);
                        }
                    }
                    break;

                case stop:
                    for (Program p : programs) {
                        if (p.name.equalsIgnoreCase(args[0]) && !p.process.isEmpty()) {
                            String[] new_args = new String[]{p.process};
                            _sendMessage(threadName,cmd,onResponse,new_args);
                        }
                    }
                    break;
                case blacklist: {
                    List<String> blacklist = new ArrayList<>();
                    for (Program p : programs) {
                        blacklist.add(Program.toStr(p));
                    }
                    String[] new_args = new String[blacklist.size()];
                    new_args = blacklist.toArray(new_args);
                    _sendMessage(threadName,cmd,onResponse,new_args);
                }
                break;
                case admin_download:
                case download: {
                    // needs a open Uploader
                    String download_action = args[0];
                    String filename = args[1];
                    int port = Integer.parseInt(args[2]);
                    String filehash = args[3];
                    String filehashProtocol = args[4];
                    //String file_path = args[0];
                    //File file = new File(file_path);
                    //if (!file.exists()) {
                    //    System.err.println("Arquivo não existe!");
                    //    return;
                    //}
                    //byte[] dados_arquivo = Files.readAllBytes(file.toPath());
                    String[] new_args = new String[]{download_action, filename ,""+port, filehash, filehashProtocol};
                    _sendMessage(threadName,cmd,onResponse,new_args);
                }
                break;
                case browse: {
                    // deveria checar se o url é válido, colocar o http antes etc...
                    _sendMessage(threadName,cmd,onResponse,args);
                }
                break;
                case broadcast: {
                    
                    //int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
                    //int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
                    //float aspect_ratio = (float) screen_width / (float) screen_height;
                    //Robot robot = new Robot();
                    //Rectangle screen_rectangle = new Rectangle(screen_width, screen_height);

                    //long time_start = System.currentTimeMillis();
                    //BufferedImage hd_screen = robot.createScreenCapture(screen_rectangle);
                    //System.out.println("Screenshot: " + (System.currentTimeMillis() - time_start));
                    //time_start = System.currentTimeMillis();

                    //ByteArrayOutputStream image_out = new ByteArrayOutputStream();
                    //ImageIO.write(hd_screen, "jpg", image_out);

                    //byte[] image_data = image_out.toByteArray();
                    //byte[] image_data = JpegHelper.compress_jpeg(hd_screen, 0.71f);
                    //System.out.println("Jpg compress: " + (System.currentTimeMillis() - time_start));
                    //String[] arguments = new String[]{"" + image_data.length};
                    //BroadcasterMessage msg = new BroadcasterMessage(threadName, cmd, arguments, image_data);
                    //sendMessage(msg,onResponse);
                    String[] new_args = new String[]{};
                    _sendMessage(threadName,cmd,onResponse,new_args);
                    Thread.sleep(10);
                    //this.stream_broadcaster.updateAddresses(args);
                    this.stream_broadcaster.startTask();
                }
                break;
                case unlockscreen: {
                    this.stream_broadcaster.stopTask();
                    _sendMessage(threadName,cmd,onResponse,args);
                }
                break;
                default: {
                    _sendMessage(threadName,cmd,onResponse,args);
                }
                break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
        //} catch (IOException e) {
        //    e.printStackTrace();
       // }
        //} catch (AWTException ex) {
        //    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
        //}
    }
    
    public void sendMessage(String threadName, Messages cmd, String... args) {
        sendMessage(new String[]{threadName}, cmd, null, args);
    }
    
    public void sendMessageToAll(String[] threadName, Messages cmd, String... args) {
        sendMessage(threadName, cmd, null, args);
    }

    /**
     * modo linha de comando. apenas manda mensagens. como um chat broadcaster
     * @throws Exception 
     * @deprecated 
     */
    @Deprecated
    public void run() throws Exception {
        // TODO Auto-generated method stub
        try {
            startServer();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while (!(line = inputReader.readLine()).equals("EXIT")) {
                handler.sendMessage(new BroadcasterMessage(BroadcasterMessage.All, Messages.msg, new String[]{line}));
            }
            handler.sendMessage(new BroadcasterMessage(BroadcasterMessage.All, Messages.exit, new String[]{}));
            handler.sendMessage(null);

            stopServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void SaveConfigs()
    {
        File file = new File(ServerMain._preferencesdir+"programs.json");
        ConfigsLoader.SaveConfigs(programs, file);
    }
    
    public void LoadConfigs()
    {
        File file = new File(ServerMain._preferencesdir+"programs.json");
        ConfigsLoader.LoadConfigs(programs, file);
    }
}
