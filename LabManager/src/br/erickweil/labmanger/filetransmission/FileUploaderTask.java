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
package br.erickweil.labmanger.filetransmission;

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class FileUploaderTask extends StoppableTask implements ProtocolFactory {
    final File file;
    private long file_length;
    
    final HashMap<String,Long> clients_downloadStart;
    final HashMap<String,Long> clients_downloaded;
    private WebServer server;
    private long time_start;
    private int port;
    
    private BroadcasterListener onDownloadListener;
    public void register_onDownloadListener(BroadcasterListener onDownloadListener)
    {
        this.onDownloadListener = onDownloadListener;
    }
    
    // velocidade maxima em kb/s, > 0 para ter efeito
    public double net_maxspeed = -1.0;
    
    public FileUploaderTask(File file, int port, List<String> clients_uuid)
    {
        this.port = port;
        this.file = file;
        this.file_length = file.length();
        clients_downloaded = new HashMap<>();
        clients_downloadStart = new HashMap<>();
        for(String uuid : clients_uuid)
        {
            clients_downloaded.put(uuid, 0L);
            
        }
    }
    
    public synchronized void setMaxSpeed(double maxspeed)
    {
        System.out.println("Download Max Speed:"+maxspeed+" KB/s");
        this.net_maxspeed = maxspeed;
    }
    
    public synchronized double getMaxSpeed()
    {
        return this.net_maxspeed;
    }
    
    
    @Override
    public void runTask()
    {
        time_start = System.currentTimeMillis();
        server = new WebServer(port, this);
        server.run();
    }
    
    @Override
    protected void on_stopTask() {
        server.stop();
    }
    
    public synchronized int getPort()
    {
        if(port == 0)
        {
            port = server.getPort();
        }
        return port;
    }
    
    @Override
    public ServerProtocol get() {
        return new FileUploaderProtocol(file,this,net_maxspeed);
    }
    
    public synchronized boolean uuidPresent(String uuid)
    {
        return clients_downloaded.containsKey(uuid);
    }
    
    public synchronized void setDownloadStart(String uuid)
    {
        clients_downloadStart.put(uuid, System.currentTimeMillis());
    }
    
    public synchronized long getDownloadStart(String uuid)
    {
        // unboxing pode dar nullpointer
        if(clients_downloadStart.containsKey(uuid) && clients_downloaded.get(uuid) != null)
        return clients_downloadStart.get(uuid);
        else return 0;
    }
    
    public synchronized void setTotalDownloaded(String uuid,long downloaded)
    {
        //clients_downloaded.put(uuid, Boolean.TRUE);
        if(downloaded == file_length && onDownloadListener != null && clients_downloaded.get(uuid) != file_length)
        {
            onDownloadListener.onResponse(uuid, "OK", null);
        }
        clients_downloaded.put(uuid, downloaded);
        
    }
    
    public synchronized void notifyDownloadDisconnected(String uuid)
    {
        if(onDownloadListener != null && clients_downloaded.get(uuid) != file_length)
        {
            onDownloadListener.onResponse(uuid, "DISCONNECTED", null);
        }
    }
    
    public synchronized int getProgress(String uuid)
    {
        if(clients_downloaded.containsKey(uuid) && clients_downloaded.get(uuid) != null)
        return (int)((clients_downloaded.get(uuid)*100L)/file_length);
        else return 0;
    }
    
    public synchronized double getMeanSpeed(String uuid)
    {
        if(clients_downloaded.containsKey(uuid) && clients_downloaded.get(uuid) != null
            && clients_downloadStart.get(uuid) != null)
        {
            double downloaded = clients_downloaded.get(uuid)/1024.0;
            double timediff = (System.currentTimeMillis() - clients_downloadStart.get(uuid))/1000.0;

            return downloaded/timediff;
        } else return 0;
    }
    
    public synchronized void stopIfAllDownloaded()
    {
        
        for(String uuid : clients_downloaded.keySet())
        {
            if(clients_downloaded.get(uuid) != file_length) return;
        }
        //if(!clients_downloaded.containsValue(Boolean.FALSE))
        System.out.println("Todos os clientes terminaram o download, fechando servidor de transmissão.");
        server.stop();
    }


}
