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
package br.erickweil.labmanager.server.swing;

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.Program.StartType;
import br.erickweil.labamanger.common.WeilUtils;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.server.swing.pcmap.MapaCanvas;
import br.erickweil.labmanager.server.swing.icons.IconSetter;


import br.erickweil.labmanager.server.ServerApp;
import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientStatus;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import br.erickweil.labmanger.filetransmission.FileUploaderTask;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Aluno
 */
public class SwingServer extends javax.swing.JFrame
{

    /**
     * Creates new form SwingServer
     */
    protected ServerApp server;
    //public String broadcast_selected = broadcast_all;
    
    public boolean broadcast = false;
    public boolean lockingAll = false;
    public ClientStatusManager clients_manager;
    public JanelaConfiguracoes janelaConfiguracoes;
    public HashMap<String,JButton> TopToolbarButtons;
    public final MapaCanvas mapa_canvas;
    public boolean clients_changed=true;
    public boolean clients_fullupdate=true;
    public boolean TabChanged=true;
    public int TabSelected;
    
    public SwingMainLoops mainLoops;
    protected Console janelaConsole;
    private final JPanel bottomToolbar;
    public final JCheckBox checkAlignOnGrid;
    public final JCheckBox checkShowGrid;
    public final JSlider sliderGridSize;
    public final Prefs prefs;
    
    private JanelaTeste janelaTeste;
    //public FileUploaderTask uploaderTask;
    public FileUploaderHelper uploaderHelper;
    private JanelaTransferencias janelaTransferencias;
    public JCheckBox checkShowScreen;
    public final JSlider sliderScreenAmount;
    
    public SwingServer(ServerApp server)
    {
        initComponents();
        
        prefs = new Prefs(this);
        this.server = server;
        
        clients_manager = new ClientStatusManager(this);//,table_clientes,mapa_canvas,label_aguardando,label_nclientes,progress_enviar);
        this.server.listener = clients_manager;
        
        mapa_canvas = new MapaCanvas(this);
        mapa_canvas.setBorder(null);
        
        PanelMapa.setLayout(new BoxLayout(PanelMapa, BoxLayout.Y_AXIS));
        PanelMapa.add(mapa_canvas);
        
        bottomToolbar = new JPanel();
        bottomToolbar.setLayout(new BoxLayout(bottomToolbar, BoxLayout.X_AXIS));
        
        checkAlignOnGrid = new JCheckBox("Alinhar na grade");
        checkAlignOnGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setChanged();
                mainLoops.updateClientStatus();
            }
        });
        bottomToolbar.add(checkAlignOnGrid);
        
        checkShowGrid = new JCheckBox("Mostrar grade");
        checkShowGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setChanged();
                mainLoops.updateClientStatus();
            }
        });
        bottomToolbar.add(checkShowGrid);
        
        sliderGridSize = new JSlider(JSlider.HORIZONTAL, 1, 1500, 32);

        sliderGridSize.setSnapToTicks(true);
        sliderGridSize.setMinorTickSpacing(1);
        
        //sliderGridSize.setPaintTicks(true);
        //sliderGridSize.setPaintLabels(true);
        sliderGridSize.setMinimumSize(new Dimension(300, 20));
        sliderGridSize.setPreferredSize(new Dimension(300, 20));
        sliderGridSize.setMaximumSize(new Dimension(300, 20));
        sliderGridSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs.setChanged();
                mainLoops.updateClientStatus();
            }
        });
        bottomToolbar.add(sliderGridSize);
        
        JButton centralizar = new JButton("Centralizar");
        centralizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapa_canvas.centerCamera();
                mainLoops.updateClientStatus();
            }
        });
        bottomToolbar.add(centralizar);
        
        checkShowScreen = new JCheckBox("Exibir Telas");
        checkShowScreen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setChanged();
                mainLoops.updateClientStatus();
                if(!checkShowScreen.isSelected()) mapa_canvas.removeScreens();
            }
        });
        bottomToolbar.add(checkShowScreen);
        
        sliderScreenAmount = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);

        //sliderScreenAmount.setSnapToTicks(true);
        sliderScreenAmount.setMinorTickSpacing(5);
        
        sliderScreenAmount.setPaintTicks(true);
        sliderScreenAmount.setPaintLabels(true);
        sliderScreenAmount.setMinimumSize(new Dimension(150, 20));
        sliderScreenAmount.setPreferredSize(new Dimension(150, 20));
        sliderScreenAmount.setMaximumSize(new Dimension(150, 20));
        sliderScreenAmount.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs.setChanged();
                mainLoops.updateClientStatus();
            }
        });
        bottomToolbar.add(sliderScreenAmount);
        
        //JButton excluir = new JButton("Excluir");
        //excluir.addActionListener(new ActionListener() {
        //    @Override
        //    public void actionPerformed(ActionEvent e) {
        //        mapa_canvas.deleting = !mapa_canvas.deleting;
        //    }
        //});
        //bottomToolbar.add(excluir);
        
        bottomToolbar.setMinimumSize(new Dimension(100, 32));
        bottomToolbar.setPreferredSize(new Dimension(10000, 32));
        bottomToolbar.setMaximumSize(new Dimension(10000, 32));
        PanelMapa.add(bottomToolbar);
        
        TopToolbarButtons = new HashMap<>();
        for(int i=0;i<IconSetter.ButtonNames.length;i++)
        {
            final String buttonName = IconSetter.ButtonNames[i];
            JButton button = IconSetter.createButton(
                    buttonName, 
                    IconSetter.ButtonTooltips[i],
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ToolbarAction(Messages.valueOf(buttonName),new String[]{BroadcasterMessage.All});
                        }
                    }
            );
            TopToolbarButtons.put(buttonName, button);
            TopToolbarPanel.add(button);
            if(i==2 || i == 6 || i == 11)
            {
                TopToolbarPanel.add(Box.createHorizontalStrut(5));
                TopToolbarPanel.addSeparator();
                TopToolbarPanel.add(Box.createHorizontalStrut(5));
            }
            else
            {
                TopToolbarPanel.add(Box.createHorizontalStrut(5));
            }
        }


        
        try {
            this.setIconImage(ImageIO.read(new File("icone.png")));
        } catch (IOException e) {
            System.err.println("Não pôde abrir o ícone do programa");
            e.printStackTrace();
        }
        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupAcaoCliente.PopupClickListener(this);
        //output.addMouseListener(popupListener);
        
        DefaultTableModel model = (DefaultTableModel) table_clientes.getModel();
        while(model.getRowCount()>0) model.removeRow(0);
        table_clientes.addMouseListener(popupListener);
        
        // TIMERS
        
        
        
        mainLoops = new SwingMainLoops(this);

        new Timer(ServerMain._showScreenLoop, new ActionListener() {
            public void actionPerformed(ActionEvent event) { mainLoops.screenShowLoop();}}).start();
        new Timer(ServerMain._veryfastLoop, new ActionListener() {
            public void actionPerformed(ActionEvent event) { mainLoops.veryfastLoop(); }}).start();
        new Timer(ServerMain._fastLoop, new ActionListener() {
            public void actionPerformed(ActionEvent event) { mainLoops.fastLoop(); }}).start();
        new Timer(ServerMain._slowLoop, new ActionListener() {
            public void actionPerformed(ActionEvent event) { mainLoops.slowLoop(); }}).start();

        
        this.TabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Tab: " + TabbedPane.getSelectedIndex());
                TabSelected = TabbedPane.getSelectedIndex();
                prefs.setChanged();
                TabChanged = true;
                mainLoops.updateClientStatus();
            }
        });
        
       
        prefs.load();
        mapa_canvas.updateBackground();
        //mapa_canvas.centerCamera();
        getContentPane().addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                prefs.setChanged();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                prefs.setChanged();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                prefs.setChanged();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                prefs.setChanged();
            }
        });
        
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent we)
        { 
            //int dialogButton = JOptionPane.YES_NO_OPTION;
            //int dialogResult = JOptionPane.showConfirmDialog (null, "Ao sair sem desbloquear os computadores,\n eles ficarão mais 5 minutos tentando reconectar \n deseja Desbloquear os computadores antes de sair?","Atenção",dialogButton);
            //if(dialogResult == JOptionPane.YES_OPTION || dialogResult == JOptionPane.NO_OPTION)
            //{
                try
                {
                    //if(dialogResult == JOptionPane.YES_OPTION)
                    //{
                    //    server.sendMessage(BroadcasterMessage.All,Messages.exit);
                    //}
                    server.stopServer();
                }
                finally
                {
                    System.exit(0);
                }
            //}
            
        }
        });
        
        myPingListener = new BroadcasterListener() {
            @Override
            public void onResponse(String threadname, String status, Object data) {
                
                if(data == null){
                                System.err.println("ping do '"+threadname+"' == null");
                                return;
                }
                String[] resp = (String[]) data;
                
                if(resp[0] == null || resp[0].isEmpty()){
                                System.err.println("ping do '"+threadname+"' == '"+resp[0]+"'");
                                return;
                }
                //System.out.println("PING RESPONSE!:"+threadname+","+status+","+Arrays.toString(resp));
                ClientData client = clients_manager.getClientbyThreadName(threadname);
                if(client != null)
                {
                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(resp[0]);
                        client.ping_info = json;
                        //System.out.println("info put!:"+json.toJSONString());
                    } catch (ParseException ex) {
                        Logger.getLogger(SwingServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        };
    }
    
    private BroadcasterListener myPingListener;
    public void pingUpdate()
    {
        this.server.sendMessage(new String[]{BroadcasterMessage.All},Messages.ping, myPingListener);
    }

    public void ToolbarAction(BroadcasterMessage.Messages action,String[] broadcast_selected)
    {
        
        boolean toAll = broadcast_selected.length == 1 && broadcast_selected[0].equals(BroadcasterMessage.All);
        //BroadcasterMessage.Messages action = Messages.blacklist;
        switch(action)
        {
            case shutdown:
            {
                int dialogResult = JOptionPane.showConfirmDialog (null, toAll ? "Deseja Desligar Todos os Computadores?" : "Deseja Desligar este Computador?","Atenção",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION)
                {
                    this.server.sendMessageToAll(broadcast_selected,Messages.shutdown);
                }
            }
            break;
            case restart:
            {
                int dialogResult = JOptionPane.showConfirmDialog (null, toAll ? "Deseja Reiniciar Todos os Computadores?" : "Deseja Reiniciar este Computador?","Atenção",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION)
                {
                    this.server.sendMessageToAll(broadcast_selected,Messages.restart);
                }
            }
            break;
            case logoff:
            {
                int dialogResult = JOptionPane.showConfirmDialog (null, toAll ? "Deseja Fazer Logoff em Todos os Computadores?" : "Deseja Fazer Logoff Neste Computador?","Atenção",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION)
                {
                    this.server.sendMessageToAll(broadcast_selected,Messages.logoff);
                }
            }
            break;
            case unlockscreen:
            {
                if(toAll)
                {
                    if(broadcast)
                    {
                       broadcast = false; 
                    }
                    clients_manager.setGlobalLocking(false);
                    lockingAll = false;
                    //TopToolbarButtons.get("broadcast").setEnabled(true);
                    //TopToolbarButtons.get("lockscreen").setEnabled(true);
                    //TopToolbarButtons.get("unlockscreen").setEnabled(false);
                }
                else
                {
                    this.clients_manager.setLocking(broadcast_selected, false);
                    //TopToolbarButtons.get("lockscreen").setEnabled(true);
                }
                this.server.sendMessageToAll(broadcast_selected,Messages.unlockscreen);
                mainLoops.updateToolbar();
            }
            break;
            case lockscreen:
            {
                if(!toAll)
                {
                    this.clients_manager.setLocking(broadcast_selected, true);
                    //TopToolbarButtons.get("unlockscreen").setEnabled(true);
                }
                else
                {
                    if(broadcast)
                    {
                        System.err.println("estado inconsistente");
                    }
                    clients_manager.setGlobalLocking(true);
                    lockingAll = true;
                    //TopToolbarButtons.get("broadcast").setEnabled(false);
                    //TopToolbarButtons.get("lockscreen").setEnabled(false);
                    //TopToolbarButtons.get("unlockscreen").setEnabled(true); 
                }
                this.server.sendMessageToAll(broadcast_selected,Messages.lockscreen);
                mainLoops.updateToolbar();
            }
            break;
            case broadcast:
            {
                if(!toAll)
                {
                    System.err.println("não implementada a transmissão a apenas 1 cliente.");
                    return;
                }
                if(!lockingAll)
                {
                    clients_manager.setGlobalLocking(true);
                    broadcast = true;
                    String[] client_ips;
                    if(toAll)
                    {
                        client_ips = clients_manager.getConnectedIPs();
                    }
                    else
                    {
                        List<String> ips = new ArrayList<>();
                        for(String thread : broadcast_selected)
                        {
                            ips.add(clients_manager.clients_threadnamemap.get(thread).ip);
                        }
                        client_ips = new String[ips.size()];
                        client_ips = ips.toArray(client_ips);
                    }
                    
                    this.server.sendMessageToAll(broadcast_selected,Messages.broadcast,client_ips);
                    //TopToolbarButtons.get("broadcast").setEnabled(false);
                    //TopToolbarButtons.get("lockscreen").setEnabled(false);
                    //TopToolbarButtons.get("unlockscreen").setEnabled(true);
                }
                else System.err.println("estado inconsistente");
                mainLoops.updateToolbar();
            }
            break;
            case printscreen:
            {
                // abre o diretorio das print
                LocalDateTime now = LocalDateTime.now();
                String format4 = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()));
                final File print_dir = ProgramOpener.parsePath(ServerMain._printsdir+format4+"/");
                if(!print_dir.exists())
                    print_dir.mkdirs();
                ProgramOpener.start(print_dir,null);
                this.server.sendMessage(broadcast_selected,Messages.printscreen, new BroadcasterListener() {
                    @Override
                    public void onResponse(String threadname, String status, Object data) {
                        try {
                            if(data == null){
                                System.err.println("print do '"+threadname+"' == null");
                                return;
                            }
                            byte[] img_print = (byte[]) data;
                            LocalDateTime now = LocalDateTime.now();
                            String format3 = now.format(DateTimeFormatter.ofPattern("HH-mm-ss", Locale.getDefault()));
                            File print_img;
                            ClientData client = clients_manager.getClientbyThreadName(threadname);
                            String uuid = client.uuid.replace('/', '-').replace('+', '_');
                            print_img = new File(print_dir,uuid+" "+format3+".jpg");
                            Files.write(print_img.toPath(), img_print);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }
            break;
            case browse:
            {
                String txt = null;
                txt = JOptionPane.showInputDialog("Site: http://www.exemplo.com/index.php");
                if (txt == null || txt.equals("")) {
                    JOptionPane.showMessageDialog(null,"deve inserir um url válido!.");
                    return;
                }
                if(!txt.contains("://"))
                {
                    txt = "http://"+txt;
                }
                this.server.sendMessageToAll(broadcast_selected,Messages.browse, txt);
            }
            break;
            case msg:
            {
                String txt = null;
                txt = JOptionPane.showInputDialog("escreva sua mensagem:");
                if (txt == null || txt.equals("")) {
                    JOptionPane.showMessageDialog(null,"deve inserir uma mensagem!.");
                    return;
                }
                
                this.server.sendMessageToAll(broadcast_selected,Messages.msg, txt); 
            }
            break;
            case admin_download:
            case download:
            {
                JFileChooser jfc;
                jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int returnValue = jfc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    List<String> uuids;
                    if(toAll)
                    {
                        uuids = clients_manager.getConnectedUUIDs();
                    }
                    else
                    {
                        uuids = new ArrayList<>();
                        for(String thread : broadcast_selected)
                        {
                            uuids.add(clients_manager.clients_threadnamemap.get(thread).uuid);
                        }
                    }
                    
                    if(uploaderHelper == null)
                    {
                        uploaderHelper = new FileUploaderHelper(this);
                    }
                    
                    uploaderHelper.upload(selectedFile, action, uuids, broadcast_selected);
                   
                    /*if(uploaderTask != null && uploaderTask.isRunning())
                    {
                        if(ServerMain._uploader_port == 0)
                        {
                            int dialogResult = JOptionPane.showConfirmDialog (null, "Já há um upload em progresso, deseja cancelar o upload anterior antes de começar o upload atual?","Atenção",JOptionPane.YES_NO_OPTION);
                            if(dialogResult == JOptionPane.YES_OPTION)
                            {
                                uploaderTask.stopTask();
                            }
                        }
                        else
                        {
                            uploaderTask.stopTask();
                        }
                    }
                    
                    String download_action = "run";
                    if(action == Messages.admin_download)
                    {
                        int dialogResult = JOptionPane.showConfirmDialog (null, "O arquivo a ser enviado é uma Atualização do Programa?","Atenção",JOptionPane.YES_NO_OPTION);
                        if(dialogResult == JOptionPane.YES_OPTION)
                        {
                            download_action = "run_after_exit";
                        }
                    }
                    
                                        
                    String filehash = "none";
                    if(ServerMain._uploader_use_hash)
                    {
                        try {
                            System.out.println("calculando hash do arquivo a ser enviado...");
                            filehash = WeilUtils.hashFile(selectedFile, ServerMain._uploader_hash_protocol);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (NoSuchAlgorithmException ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                    uploaderTask = new FileUploaderTask(selectedFile,ServerMain._uploader_port,uuids);
                    uploaderTask.setMaxSpeed(ServerMain._uploader_maxspeed);
                    uploaderTask.startTask();
                    
                    int port = ServerMain._uploader_port;
                    if(ServerMain._uploader_port == 0)
                    {
                        for(int i=0;i<50 && port == 0;i++)
                        {
                            try
                            {
                                Thread.sleep(50);
                                port = uploaderTask.getPort();
                            }
                            catch(Exception e){if(i == 0)e.printStackTrace();}
                        }
                    }

                    server.sendMessageToAll(broadcast_selected, action, download_action, selectedFile.getName() ,""+port, filehash, ServerMain._uploader_hash_protocol);
                    */
                    
                }
            }
            break;
            case start:
            {
               JComboBox<String> combo_abrir = new JComboBox<>();
                for(Program p : server.programs)
                {
                    if(p.start != Program.StartType.blacklist)
                    {
                        if(!p.path.isEmpty())
                        {
                            combo_abrir.addItem(p.name);
                        }
                    }    
                }
                int okCxl = JOptionPane.showConfirmDialog(null, combo_abrir, "Selecione o Programa:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (okCxl == JOptionPane.OK_OPTION) {
                    int index = combo_abrir.getSelectedIndex();
                    if(index != -1)
                    {
                        String selected = combo_abrir.getItemAt(index);
                        if(selected != null && !selected.isEmpty())
                        this.server.sendMessageToAll(broadcast_selected,Messages.start, selected.toLowerCase());
                    }
                }
            }
            break;
            case stop:
            {
                JPanel msg_panel = new JPanel();
                JComboBox<String> combo_fechar = new JComboBox<>();
                for(Program p : server.programs)
                {
                    if(p.start != Program.StartType.blacklist)
                    {
                        if(!p.process.isEmpty() || !p.window.isEmpty())
                        {
                            combo_fechar.addItem(p.name);
                        }
                    }    
                }
                msg_panel.add(combo_fechar);
                
                JCheckBox check_blacklist = new JCheckBox("Adicionar à Blacklist");
                check_blacklist.setToolTipText("A Blacklist é a lista de programas que são proibidos de estarem abertos, e se forem abertos em menos de 1 segundo são fechados");
                if(toAll)
                    msg_panel.add(check_blacklist);
                int okCxl = JOptionPane.showConfirmDialog(null, msg_panel, "Selecione o Programa:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (okCxl == JOptionPane.OK_OPTION) {
                    int index = combo_fechar.getSelectedIndex();
                    if(index != -1)
                    {
                        String selected = combo_fechar.getItemAt(index);
                        if(selected != null && !selected.isEmpty())
                        {
                            this.server.sendMessageToAll(broadcast_selected,Messages.stop, selected.toLowerCase());
                            if(toAll && check_blacklist.isSelected())
                            {
                                Program p = null;
                                for(int i=0;i<server.programs.size();i++)
                                {
                                    if(server.programs.get(i).name.equals(selected))
                                    {
                                        p = server.programs.get(i);
                                        p.start = StartType.blacklist;
                                    }      
                                }
                                this.server.sendMessage(BroadcasterMessage.All, Messages.blacklist);
                                this.server.SaveConfigs();
                            }
                        }
                    }
                }
            }
            break;
            case exit:
            {
                int dialogResult = JOptionPane.showConfirmDialog (null, toAll ? "Deseja Desconectar Todos os Computadores? (não terá como controlá-los)" : "Deseja Desconectar este Computador? (não terá como controlá-lo)","Atenção",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION)
                {
                    server.sendMessageToAll(broadcast_selected,Messages.exit);
                }
                mainLoops.updateToolbar();
            }
            break;
            case ping:
            {
                server.sendMessageToAll(broadcast_selected,Messages.ping);
            }
            break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        TopToolbarPanel = new javax.swing.JToolBar();
        jPanel3 = new javax.swing.JPanel();
        progress_enviar = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        label_nclientes = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        label_aguardando = new javax.swing.JLabel();
        label_erros = new javax.swing.JLabel();
        TabbedPane = new javax.swing.JTabbedPane();
        PanelLista = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        table_clientes = new javax.swing.JTable();
        PanelMapa = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menu_arquivo_sair = new javax.swing.JMenuItem();
        menu_arquivo_sairedesbloquear = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        menu_executar_janelateste = new javax.swing.JMenuItem();
        menu_uploadadmin = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        menu_progs = new javax.swing.JMenuItem();
        menu_transferencias = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LabManager");

        jScrollPane3.setBorder(null);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane3.setFocusable(false);

        TopToolbarPanel.setBackground(new java.awt.Color(255, 255, 255));
        TopToolbarPanel.setFloatable(false);
        TopToolbarPanel.setRollover(true);
        TopToolbarPanel.setToolTipText("");
        TopToolbarPanel.setFocusable(false);
        jScrollPane3.setViewportView(TopToolbarPanel);

        jLabel3.setText("Clientes Conectados:");

        label_nclientes.setText("0");

        jLabel2.setText("Clientes Aguardando:");

        label_aguardando.setText("0");

        label_erros.setText(" ");
        label_erros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_errosMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_errosMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_nclientes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_aguardando)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_erros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progress_enviar, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(label_nclientes)
                        .addComponent(jLabel2)
                        .addComponent(label_aguardando)
                        .addComponent(label_erros))
                    .addComponent(progress_enviar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        TabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jScrollPane2.setBorder(null);

        table_clientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "IP", "Hostname", "Usuario", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table_clientes.setRowSelectionAllowed(false);
        table_clientes.setShowVerticalLines(false);
        table_clientes.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(table_clientes);
        if (table_clientes.getColumnModel().getColumnCount() > 0) {
            table_clientes.getColumnModel().getColumn(0).setResizable(false);
            table_clientes.getColumnModel().getColumn(1).setResizable(false);
            table_clientes.getColumnModel().getColumn(3).setResizable(false);
        }

        javax.swing.GroupLayout PanelListaLayout = new javax.swing.GroupLayout(PanelLista);
        PanelLista.setLayout(PanelListaLayout);
        PanelListaLayout.setHorizontalGroup(
            PanelListaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
        );
        PanelListaLayout.setVerticalGroup(
            PanelListaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
        );

        TabbedPane.addTab("Lista", PanelLista);

        javax.swing.GroupLayout PanelMapaLayout = new javax.swing.GroupLayout(PanelMapa);
        PanelMapa.setLayout(PanelMapaLayout);
        PanelMapaLayout.setHorizontalGroup(
            PanelMapaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 695, Short.MAX_VALUE)
        );
        PanelMapaLayout.setVerticalGroup(
            PanelMapaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 409, Short.MAX_VALUE)
        );

        TabbedPane.addTab("Mapa", PanelMapa);

        jMenu1.setText("Arquivo");

        menu_arquivo_sair.setText("Sair");
        menu_arquivo_sair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_arquivo_sairActionPerformed(evt);
            }
        });
        jMenu1.add(menu_arquivo_sair);

        menu_arquivo_sairedesbloquear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/erickweil/labmanager/server/swing/icons/exit-default.png"))); // NOI18N
        menu_arquivo_sairedesbloquear.setText("Sair");
        menu_arquivo_sairedesbloquear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_arquivo_sairedesbloquearActionPerformed(evt);
            }
        });
        jMenu1.add(menu_arquivo_sairedesbloquear);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Executar...");

        menu_executar_janelateste.setText("CMD");
        menu_executar_janelateste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_executar_janelatesteActionPerformed(evt);
            }
        });
        jMenu3.add(menu_executar_janelateste);

        menu_uploadadmin.setText("Upload as Admin");
        menu_uploadadmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_uploadadminActionPerformed(evt);
            }
        });
        jMenu3.add(menu_uploadadmin);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Ferramentas");

        menu_progs.setText("Configurar Programas");
        menu_progs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_progsActionPerformed(evt);
            }
        });
        jMenu5.add(menu_progs);

        menu_transferencias.setText("Transferências pela Rede");
        menu_transferencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_transferenciasActionPerformed(evt);
            }
        });
        jMenu5.add(menu_transferencias);

        jMenuBar1.add(jMenu5);

        jMenu2.setText("Ajuda");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(TabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(TabbedPane)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menu_arquivo_sairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_arquivo_sairActionPerformed
        // TODO add your handling code here:
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_menu_arquivo_sairActionPerformed

    private void menu_arquivo_sairedesbloquearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_arquivo_sairedesbloquearActionPerformed
        // TODO add your handling code here:
        try
        {
            /*for(int i=0;i< 10 && clients_manager.connectedClients() > 0;i++)
            {
                try {
                    this.server.sendMessage(BroadcasterMessage.All,Messages.exit);
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SwingServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
            this.server.stopServer();
        }
        finally
        {
            System.exit(0);
        }
    }//GEN-LAST:event_menu_arquivo_sairedesbloquearActionPerformed

    private void menu_progsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_progsActionPerformed
        if(janelaConfiguracoes == null)
        {
            janelaConfiguracoes = new JanelaConfiguracoes(server);
            janelaConfiguracoes.setVisible(true);
        }
        else
        {
            janelaConfiguracoes.updateValues();
            janelaConfiguracoes.setVisible(true);
            janelaConfiguracoes.requestFocus();
        }
    }//GEN-LAST:event_menu_progsActionPerformed

    private void label_errosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_errosMouseEntered
        
    }//GEN-LAST:event_label_errosMouseEntered

    private void label_errosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_errosMouseClicked
        if(janelaConsole == null)
        {
            janelaConsole = new Console();
            janelaConsole.setVisible(true);
        }
        else
        {
            janelaConsole.setVisible(true);
            janelaConsole.requestFocus();
        }
        label_erros.setText(" ");
    }//GEN-LAST:event_label_errosMouseClicked

    private void menu_executar_janelatesteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_executar_janelatesteActionPerformed
        if(janelaTeste == null)
        {
            janelaTeste = new JanelaTeste(server);
            janelaTeste.setVisible(true);
        }
        else
        {
            janelaTeste.setVisible(true);
            janelaTeste.requestFocus();
        }
    }//GEN-LAST:event_menu_executar_janelatesteActionPerformed

    private void menu_transferenciasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_transferenciasActionPerformed
        if(janelaTransferencias == null)
        {
            janelaTransferencias = new JanelaTransferencias(server);
            janelaTransferencias.setVisible(true);
        }
        else
        {
            janelaTransferencias.updateValues();
            janelaTransferencias.setVisible(true);
            janelaTransferencias.requestFocus();
        }
    }//GEN-LAST:event_menu_transferenciasActionPerformed

    private void menu_uploadadminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_uploadadminActionPerformed
        ToolbarAction(Messages.admin_download, new String[]{BroadcasterMessage.All});
    }//GEN-LAST:event_menu_uploadadminActionPerformed

    /**
     * @param server     
     */
    public static void startSwing(final ServerApp server)
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Windows".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(SwingServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(SwingServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(SwingServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(SwingServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new SwingServer(server).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelLista;
    private javax.swing.JPanel PanelMapa;
    protected javax.swing.JTabbedPane TabbedPane;
    private javax.swing.JToolBar TopToolbarPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    protected javax.swing.JLabel label_aguardando;
    protected javax.swing.JLabel label_erros;
    protected javax.swing.JLabel label_nclientes;
    private javax.swing.JMenuItem menu_arquivo_sair;
    private javax.swing.JMenuItem menu_arquivo_sairedesbloquear;
    private javax.swing.JMenuItem menu_executar_janelateste;
    private javax.swing.JMenuItem menu_progs;
    private javax.swing.JMenuItem menu_transferencias;
    private javax.swing.JMenuItem menu_uploadadmin;
    protected javax.swing.JProgressBar progress_enviar;
    protected javax.swing.JTable table_clientes;
    // End of variables declaration//GEN-END:variables
}
