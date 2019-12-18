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
import br.erickweil.labmanager.server.ServerApp;
import br.erickweil.labmanager.server.swing.pcmap.MapaCanvas;
import br.erickweil.labmanager.server.swing.pcmap.PC;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONObject;

/**
 *
 * @author Usuario
 */
public class ClientStatusManager extends ThreadSafeListener<BroadcasterListener>{



    public static enum ClientStatus{
        Iniciando,
        Aguardando,
        Processando,
        Desconectado
    };
    
    public static class ClientData{
        public String version;
        public String thread_name;
        public String uuid;
        public String ip;
        public String hostname;
        public int port;
        public String user;
        public ClientStatus status;
        public Date connected_since;
        public int times_disconected;
        public JSONObject client_info;
        public JSONObject ping_info;
        public boolean locked;

        private ClientData(String thread_name, String uuid, String client_ip, String client_hostname, int client_port, String client_user, ClientStatus clientStatus, JSONObject client_info,String version) {
            this.thread_name = thread_name;
            this.uuid = uuid;
            this.ip = client_ip;
            this.hostname = client_hostname;
            this.port = client_port;
            this.user = client_user;
            this.status = clientStatus;
            this.client_info = client_info;
            this.version = version;
            connected_since = new Date();
            times_disconected = 0;
            locked = false;
        }
        
        private void setDisconected()
        {
            //System.out.println("SETTED DISCONNECTED");
            status = ClientStatus.Desconectado;
            locked = false;
            times_disconected++;                
        }
    }    
    public List<ClientData> clients;
    public HashMap<String,ClientData> clients_threadnamemap;
    public HashMap<String,ClientData> clients_uuidmap;
    public HashMap<Integer,Integer> response_waitcount;
    public HashMap<Integer,BroadcasterListener> response_listeners;
    public SwingServer serverWindow;
    public ClientStatusManager(SwingServer server)
    {
        serverWindow = server;
        clients = new ArrayList<>();
        clients_threadnamemap = new HashMap<>();
        clients_uuidmap = new HashMap<>();
        response_listeners = new HashMap<>();
        response_waitcount = new HashMap<>();
    }
    
    public List<String> getConnectedUUIDs()
    {
        List<String> uuids = new ArrayList<>();
        for(ClientData client : clients)
        {
            if(client.status != ClientStatus.Desconectado)
            {
                uuids.add(client.uuid);
            }
        }
        return uuids;
    }
    
    public String[] getConnectedIPs()
    {
        List<String> ips = new ArrayList<>();
        for(ClientData client : clients)
        {
            if(client.status != ClientStatus.Desconectado)
            {
                ips.add(client.ip);
            }
        }
        String[] ips_arr = new String[ips.size()];
        ips_arr = ips.toArray(ips_arr);
        return ips_arr;
    }

    public int connectedClients()
    {
        int conected_clients = 0;
        for(ClientData client : clients)
        {
            if(client.status != ClientStatus.Desconectado)
            {
                conected_clients++;
            }
        }
        return conected_clients;
    }
    
    
    public ClientData getClientAt(int row)
    {
        return clients.get(row);
    }
    
    public ClientData getClientbyThreadName(String thread_name)
    {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).thread_name.equals(thread_name)) {
                return clients.get(i);
            }
        }
        return null;
    }
    
    public void setGlobalLocking(boolean locked)
    {
        for(int i=0;i<clients.size();i++)
        {
            if(clients.get(i).status != ClientStatus.Desconectado)
            clients.get(i).locked = locked;
        }
    }
    
    public void setLocking(String[] thread_name, boolean locked)
    {
        List<String> threads = Arrays.asList(thread_name);
        for (int i = 0; i < clients.size(); i++) {
            //if (clients.get(i).thread_name.equals(thread_name)) {
            if (threads.contains(clients.get(i).thread_name)) {
                clients.get(i).locked = locked;
            }
        }
    }
    
    @Override
    public void registerResponseListener(int msg_uuid, int nResponses, BroadcasterListener listener) {
        this.response_listeners.put(msg_uuid, listener);
        //System.err.println("responseListener registering... na msg:"+msg_uuid);
        if(nResponses > 0)
        {
            this.response_waitcount.put(msg_uuid, nResponses);
        }
        else
        {
            this.response_waitcount.put(msg_uuid, connectedClients());
        }
        
    }
    
    @Override
    protected void handleEvent(int event, Object... args) {
        String thread_name = (String) args[0];

        switch (event) {
            case ServerApp.CLIENT_CONECTED: {
                String client_uuid = (String) args[1];
                String client_ip = (String) args[2];
                String client_hostname = (String) args[3];
                int client_port = (int) args[4];
                String client_user = (String) args[5];
                JSONObject client_info = (JSONObject) args[6];
                String client_version = (String) args[7];
                
                ClientData newclient = null;
                ClientData client = clients_uuidmap.get(client_uuid);
                if (client != null) {
                    if (client.status != ClientStatus.Desconectado && client.status != ClientStatus.Processando) {
                        System.err.println("Novo Cliente conectado com uuid duplicado:" + Arrays.toString(args));
                    }
                    else
                    {
                        newclient = client;
                        newclient.thread_name = thread_name;
                        newclient.ip = client_ip;
                        newclient.hostname = client_hostname;
                        newclient.port = client_port;
                        newclient.user = client_user;
                        newclient.status = ClientStatus.Iniciando;
                        newclient.locked = false;
                        
                    }
                }

                if (newclient == null) {
                    newclient = new ClientData(thread_name, client_uuid, client_ip, client_hostname, client_port, client_user, ClientStatus.Iniciando, client_info,client_version);
                    clients.add(newclient);
                    clients_uuidmap.put(client_uuid, newclient);
                    
                    serverWindow.mapa_canvas.AddPC(client_uuid);
                }

                clients_threadnamemap.put(thread_name, newclient);
                
                serverWindow.clients_changed = true;
                serverWindow.clients_fullupdate = true;
            }
            break;
            case ServerApp.CLIENT_DISCONECTED: {
                
                ClientData client = clients_threadnamemap.remove(thread_name);
                client.setDisconected();
                /*for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).thread_name.equals(thread_name)) {
                        clients.get(i).setDisconected();
                    }
                }*/
                
                serverWindow.clients_changed = true;
                serverWindow.clients_fullupdate = true;
            }
            break;
            case ServerApp.CLIENT_WAITING:
            case ServerApp.CLIENT_PROCESSING: {
                int msg_uuid = (int) args[1];
                String statusMessage = (String) args[2];
                ClientData whichClient = clients_threadnamemap.get(thread_name);
                if (event == ServerApp.CLIENT_WAITING) {
                    whichClient.status = ClientStatus.Aguardando;
                    if(msg_uuid != -1 && response_waitcount.containsKey(msg_uuid) && response_waitcount.get(msg_uuid) != null)
                    {
                        response_waitcount.put(msg_uuid, response_waitcount.get(msg_uuid) - 1);
                        if(response_waitcount.get(msg_uuid) == 0)
                        {
                            //System.err.println("responseListener removing... thread:"+thread_name+" na msg:"+msg_uuid);
                            response_listeners.remove(msg_uuid);
                            response_waitcount.remove(msg_uuid);
                        }
                    }
                }
                if (event == ServerApp.CLIENT_PROCESSING) {
                    whichClient.status = ClientStatus.Processando;
                }

                serverWindow.clients_changed = true;
            }
            break;
            case ServerApp.CLIENT_RESPONSE:
            {
                int msg_uuid = (int) args[1];
                String status = (String) args[2];
                BroadcasterListener broadListener = response_listeners.get(msg_uuid);
                if(broadListener != null)
                {
                    broadListener.onResponse(thread_name,status,args[3]);
                }
                else System.err.println("responseListener null na thread:"+thread_name+" na msg:"+msg_uuid);
            }
            break;
        }
    }
    
}
