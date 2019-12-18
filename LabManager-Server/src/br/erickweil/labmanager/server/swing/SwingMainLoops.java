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
import br.erickweil.labamanger.common.files.JpegHelper;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import br.erickweil.labmanager.server.ServerApp;
import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanager.server.swing.pcmap.MapLoader;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONObject;

/**
 *
 * @author Usuario
 */
public class SwingMainLoops {
    private final SwingServer sw;
    private final ClientStatusManager clients_manager;
    private int last_error_count = 0;
    private BroadcasterListener showScreenListener;
    public SwingMainLoops(SwingServer serverWindow)
    {
        this.sw = serverWindow;
        this.clients_manager = serverWindow.clients_manager;
        this.showScreenListener = new BroadcasterListener() {
            @Override
            public void onResponse(String threadname, String status, Object data) {
                try 
                {
                    if(!sw.checkShowScreen.isSelected())
                    {
                        return;
                    }
                    
                    if(data == null){
                        System.err.println("print do '"+threadname+"' == null");
                        return;
                    }
                    byte[] img_print = (byte[]) data;
                    BufferedImage decompress_jpeg = JpegHelper.decompress_jpeg(img_print);
                    ClientData client = sw.clients_manager.getClientbyThreadName(threadname);
                    if(client != null)
                    {
                        sw.mapa_canvas.setScreenPC(decompress_jpeg, client.uuid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
          
    public void screenShowLoop()
    {
        boolean updateTable = sw.TabSelected == 0;
        boolean updateMap = sw.TabSelected == 1;
        
        if(updateMap)
        {
            if(sw.checkShowScreen.isSelected())
            {
                int connClients = sw.clients_manager.connectedClients();
                int showNumber = sw.sliderScreenAmount.getValue();
                int max = 
                        connClients > showNumber ?
                        connClients / showNumber :
                        1;
                if(mapShowScreenCounter >= max)
                {
                    mapShowScreenCounter = 0;
                }
                //System.out.println("("+mapShowScreenCounter+"/"+max+") would print:");
                int count = 0;
                // tirar print e enviar já na resolução que as telas serão exibidas, para economizar
                int showScreenWidth = sw.mapa_canvas.getShowScreenWidth();
                for(ClientData client : sw.clients_manager.clients)
                {
                    if(client == null || client.status == ClientStatusManager.ClientStatus.Desconectado)
                        continue;
                    
                    // so tirar print de alguns pc's por vez
                    if(count % max != mapShowScreenCounter)
                    {
                        //System.out.println(count+" != "+mapShowScreenCounter);
                        count++;
                        continue;
                    }
                    else
                    {
                        //System.out.print(count+" ");
                        //System.out.println(count+" == "+mapShowScreenCounter);
                        count++;
                    }
                    
                    //nao tirar print de pc's que estejam ocupados processando
                    if(client.status != ClientStatusManager.ClientStatus.Aguardando)
                        continue;
                    
                    JSONObject ping_info = client.ping_info;
                    if(ping_info == null) continue;
                    
                    // só tirar print de pc's que estão com o slave Online
                    boolean slaveOnline = ping_info.containsKey("slaveOnline") ? (boolean)ping_info.get("slaveOnline") : false;
                    if(!slaveOnline) continue;
                    
                    
                    // só tirar print de pc's que estão visíveis no canvas
                    if(sw.mapa_canvas.isPCVisible(client.uuid))
                    {
                        
                        sw.server.sendMessage(new String[]{client.thread_name},Messages.printscreen,showScreenListener,""+showScreenWidth);
                    }
                }
                //System.out.println(".");
                mapShowScreenCounter++;
                //if(mapShowScreenCounter == sw.clients_manager.clients.size()) mapShowScreenCounter = 0;
            }
        }
    }
    
    public void veryfastLoop()
    {
        if(ServerMain.err_receiver != null && last_error_count != ServerMain.err_receiver.lines.size())
        {
            
            sw.label_erros.setText("Erros no console!");
            if(sw.janelaConsole != null)
            {
                for(int i=last_error_count;i<ServerMain.err_receiver.lines.size();i++)
                {
                    String str = ServerMain.err_receiver.lines.get(i);
                    sw.janelaConsole.textarea_err.append(str);
                    sw.janelaConsole.textarea_err.append("\n");
                }
            }
            last_error_count = ServerMain.err_receiver.lines.size();
        }
        updateClientStatus();
    }
    
    int mapShowScreenCounter = 0;
    public void fastLoop()
    {
        boolean updateTable = sw.TabSelected == 0;
        boolean updateMap = sw.TabSelected == 1;
        
        if(updateMap)
        {
            
        }
    }
    
    public void slowLoop()
    {
        if(sw.lockingAll)
        {
            //clients_manager.setGlobalLocking(true);
            //server.sendMessage(BroadcasterMessage.All, Messages.lockscreen);
            //se está bloquando todos, continua mandando a mensagem para bloquear novos clientes
            sw.ToolbarAction(Messages.lockscreen, new String[]{BroadcasterMessage.All});
        }
        else
        {
            if(sw.broadcast)
            {

                //clients_manager.setGlobalLocking(true);
                //server.sendMessage(BroadcasterMessage.All, Messages.broadcast);
                sw.ToolbarAction(Messages.broadcast, new String[]{BroadcasterMessage.All});
            }
            else
            {
                //envia um ping para todos os clientes, para que a conexão não exceda o timeout
                //sw.ToolbarAction(Messages.ping, new String[]{BroadcasterMessage.All});
                sw.pingUpdate();
            }
        }
        
        if(sw.mapa_canvas.changed)
            sw.mapa_canvas.save();
        
        if(sw.prefs.changed)
            sw.prefs.save();
        
        if(ServerMain.configs_changed)
        {
            ServerMain.SaveConfigs();
        }
    }
    
        
    public void updateClientStatus()
    {
        boolean updateTable = sw.TabSelected == 0;
        boolean updateMap = sw.TabSelected == 1;
        
        boolean fullupdate = sw.clients_fullupdate || sw.TabChanged;
        //System.out.println(clients_fullupdate);
        if(sw.clients_changed || sw.TabChanged)
        {
            int waiting_clients = 0;
            int conected_clients = 0;


            DefaultTableModel model = (DefaultTableModel) sw.table_clientes.getModel();
            // deleta todas as linhas
            if(fullupdate && updateTable)
            {
                model.setRowCount(0);
            }

            for(int i=0;i<clients_manager.clients.size();i++)
            {
                ClientStatusManager.ClientData client = clients_manager.clients.get(i);
                //System.out.println(clients_status.get(s));
                if(client.status == ClientStatusManager.ClientStatus.Aguardando)
                {
                    waiting_clients++;
                }
                if(client.status != ClientStatusManager.ClientStatus.Desconectado)
                {
                    conected_clients++;
                }

                if(updateTable)
                {
                    if(fullupdate)
                    {
                        model.addRow(new Object[]{client.ip+":"+client.port, client.hostname, client.user, client.status.toString()});
                    }
                    else
                    {
                        model.setValueAt(client.status.toString(), i, 3);
                    }
                }
            }

            sw.label_nclientes.setText(""+conected_clients);
            sw.label_aguardando.setText(""+waiting_clients);

            if(waiting_clients == conected_clients)
            {
                sw.progress_enviar.setValue(0);
            }
            else if( conected_clients == 0)
            {
                sw.progress_enviar.setValue(0);
            }
            else
            {
                double percentage = (double)waiting_clients/(double)conected_clients;
                percentage = Math.max(0.05,percentage);
                //System.out.println(percentage);
                sw.progress_enviar.setValue((int)(percentage*100.0));
            }
            
            
        }
        
        if(updateMap)
            sw.mapa_canvas.update();
        
        if(sw.clients_changed)
            updateToolbar();
        
        sw.clients_changed = false;
        sw.clients_fullupdate = false;
        sw.TabChanged = false;
    }
    
    public void updateToolbar()
    {
        boolean enable_lock;
        boolean enable_unlock;
        boolean enable_broadcast;
        if(sw.broadcast)
        {
            enable_lock = false;
            enable_unlock = true;
            enable_broadcast = false;
        }
        else
        {
            if(sw.lockingAll)
            {
                enable_lock = false;
                enable_unlock = true;
                enable_broadcast = false;
            }
            else
            {
                enable_broadcast = true;
                //enable_lock = true;
                //enable_unlock = true;
                int n_locked = 0;
                int n_active = 0;
                for(int i=0;i<clients_manager.clients.size();i++)
                {
                    ClientStatusManager.ClientData c = clients_manager.clients.get(i);
                    if(c != null && ((c.status == ClientStatusManager.ClientStatus.Aguardando) || (c.status == ClientStatusManager.ClientStatus.Processando)))
                    {
                        n_locked += c.locked ? 1 : 0;
                        n_active++;
                    }
                }
                if(n_locked == 0)
                {
                    enable_lock = true;
                    enable_unlock = false;
                }
                else if(n_locked >= n_active)
                {
                    enable_lock = false;
                    enable_unlock = true;
                }
                else
                {
                    enable_lock = true;
                    enable_unlock = true;
                }
            }
        
        }
        
        
        sw.TopToolbarButtons.get("lockscreen").setEnabled(enable_lock);
        sw.TopToolbarButtons.get("unlockscreen").setEnabled(enable_unlock);
        sw.TopToolbarButtons.get("broadcast").setEnabled(enable_broadcast);
    }
}
