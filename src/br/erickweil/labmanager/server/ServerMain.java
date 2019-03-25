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

import br.erickweil.labamanger.common.ErrStreamReceiver;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import br.erickweil.labmanager.server.swing.JanelaSetup;
import br.erickweil.labmanager.server.swing.SwingServer;
import java.io.File;
import java.io.PrintStream;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * Classe principal do Servidor
 * 
 * 
 */
public class ServerMain {

    //public static boolean _IniciarInterfaceAutomaticamente = true;
    //public static boolean _IniciarMaximizado = true;
    public static int _delaySplash = 15000;

    public static boolean _solve_hostname = true;
    public static String _auth_by_address = "none";
    public static String _auth_by_username = "none";
    public static String _auth_by_uuid = "none";

    public static String[] _auth_address_list
            = {
                "localhost",
                "127.0.0.1",
                "::1",
                "192.168.2.99"
            };

    public static String[] _auth_username_list
            = {
                "Usuario"
            };
    public static String[] _auth_uuid_list
            = {
                "qTFVO71islHs3rZGRCV6iKSO9ZpgS7Rb81QzPNsY"
            };


    //public static String _client_programdir = "C:/ProgramData/LabManager061017";
    //public static String _client_programexec = "C:/ProgramData/LabManager061017/LabManager.jar";
    //public static String _mapdir = "pcmaps/";
    public static String _preferencesdir = "prefs/";
    public static String _printsdir = "%USERPROFILE%/Documents/Lab Manager/";

    public static int _server_port = 22133;//8080;
    //public static String _stream_host = "192.168.2.6";
    public static int _stream_framerate = 5;
    public static int _stream_width = -1;
    public static int _stream_fullframeinterval = 18;
    public static int _stream_threshold = 70;
    public static int _stream_maxbandwidth = 0;
    public static int _STREAM_port = 22135;
    public static int _readTimeout = 60000;
    public static int _showScreenLoop = 1000;
    public static int _veryfastLoop = 100;
    public static int _fastLoop = 1000;
    public static int _slowLoop = 10000;
    public static PrintStream old_err;
    public static ErrStreamReceiver err_receiver;
    public static boolean _errconsole = true;
    public static int _uploader_port = 22136;
    public static int _uploader_package_size = 1024*100;
    public static boolean _uploader_use_hash = true;
    public static String _uploader_hash_protocol = "MD5";
    public static double _uploader_maxspeed = -1.0;
    public static boolean _uploader_onebyone = false;
    
    // n acredito que fiz isso
    public static volatile boolean configs_changed;
    
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            /**
             * ####################### CARREGANDO CONFIGURAÇÕES
             * ############################
             *
             */
            try {
                SimpleLogger logger = new SimpleLogger();
                new Configurable(logger, ServerMain.class, "config");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if(_errconsole)
            {
                old_err = System.err;
                err_receiver = new ErrStreamReceiver(old_err);
                System.setErr(new PrintStream(err_receiver,true));
            }
            
            if(!(new File("server_keystore.jks.aes").exists() || new File("server_keystore.jks").exists() ))
            {
                System.out.println("Rodando server desconfigurado");
                JanelaSetup.startSwing();
                return;
            }
            
            /*try{
                String a = null;
                throw new Exception("ErrStreamReceiver funcionando");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }*/
            /**
             * ####################### LIGANDO O SERVIDOR
             * ############################
             *
             */
            String serverPassword = null;// "Ex3.15Sal83.18";
            if(!new File("server_keystore.jks").exists())
            {
                while (serverPassword == null || serverPassword.equals("")) {
                    //serverPassword = JOptionPane.showInputDialog("insira a senha do servidor:");
                    JPasswordField pf = new JPasswordField();
                    int okCxl = JOptionPane.showConfirmDialog(null, pf, "Insira a senha do servidor:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (okCxl == JOptionPane.OK_OPTION) {
                        serverPassword = new String(pf.getPassword());
                    }
                    if (serverPassword == null || serverPassword.equals("")) {
                        JOptionPane.showMessageDialog(null,
                                "Você não inseriu uma senha válida.");
                    }
                }
            }
            startServer(serverPassword);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            new SimpleLogger().Erro(ServerMain.class, "Erro Extremamente Crítico:" + e.getMessage(), "erro totalmente inseperado, algo extremamente errado aconteceu e impediu o funcionamento mais básico possível do programa.", e);
            e.printStackTrace();
            System.exit(0);

        } finally {
            System.out.println("MainThread Parou");
        }
    }
    
    public static void startServer(String serverPassword) throws Exception
    {

        /*List<Program> programs = new ArrayList<>();
        for (int i = 0; i < _programs.length / 4; i++) {
            String Name = _programs[i * 4 + 0].trim();
            String Path = _programs[i * 4 + 1].trim();
            String Process = _programs[i * 4 + 2].trim();
            StartType start = StartType.valueOf(_programs[i * 4 + 3].toLowerCase().trim());

            programs.add(new Program(Name, Path, Process, start));
        }*/

        ServerApp server = new ServerApp(_server_port, serverPassword);

        // sei la... vai que... n sei.
        serverPassword = null;

        SwingServer.startSwing(server);

        server.startServer();
    }
    
    public static void SaveConfigs()
    {
        configs_changed = false;
        try {
            SimpleLogger logger = new SimpleLogger();
            Configurable conf = new Configurable(logger, ServerMain.class, null);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
