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

import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.labamanger.common.WeilUtils;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebClient;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class FileDownloaderTask extends StoppableTask implements ProtocolFactory{
    String uuid;
    File file;
    String hostname;
    int port;
    long off;
    public int MAX_TIMEOUT = 120000;
    String action;
    String hash;
    String hashProtocol;
    private boolean downloadComplete;
    
     public FileDownloaderTask(String uuid, File file,String hostname,int port, String action, String hash, String hashProtocol)
    {
        this.uuid = uuid;
        this.file = file;
        this.hostname = hostname;
        this.port = port;
        this.action = action;
        this.hash = hash;
        this.hashProtocol = hashProtocol;
    }
    
    
    public FileDownloaderTask(String uuid, File file,String hostname,int port, String action)
    {
        this(uuid, file, hostname, port, action, null, null);
    }
    
    protected synchronized void setOff(long off)
    {
        this.off = off;
    }
    
    @Override
    public void runTask()
    {
        try
        {
            int time_withoutServer = 0;
            long lastoff = off;
            while(!Thread.currentThread().isInterrupted() && isRunning())
            {
                lastoff = off;
                connect();
                if(off == -1) break;
                System.out.println("File Downloader: Reconectando... off:"+off);
                Thread.sleep(1000);
                if(lastoff == off)
                {
                    time_withoutServer += 1000;
                }
                else
                {
                    time_withoutServer = 0;
                }
                
                if(time_withoutServer > MAX_TIMEOUT)
                {
                    System.err.println("Demorou muito para conectar ao servidor, cancelando download.");
                    System.err.println("deletando arquivo...");
                    file.delete();
                    break;
                }
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        boolean valid;
        if(hash != null)
        {
            valid = false;
            try {
                String downloaded_hash = WeilUtils.hashFile(file, hashProtocol);
                if(downloaded_hash.equals(hash))
                {
                    valid = true;
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }
            if(!valid)
            {
                System.err.println("Hash do arquivo baixado não bateu! deletando arquivo...");
                file.delete();
                return;
            }
        } else valid = true;
        
        if(valid && off == -1 && file.exists())
        {
            setDownloadComplete();
            
            // se terminou o download e deve realmente abrir o programa
            if(action.equals("run") || action.equals("run_after_exit"))
            {
                ProgramOpener.start(file,null);
            }
            
            if(action.equals("run_after_exit"))
            {
                System.out.println("saindo do programa... para abrir arquivo");
                System.exit(0);
            }
        }
    }
    
    @Override
    protected void on_stopTask() {
        
    }
    
    private void connect()
	{        
        WebClient client = null;
        client = new WebClient(hostname,port, this);
        client.run();
	}
    
        @Override
    public ServerProtocol get() {
        return new FileDownloaderProtocol(uuid,file,off, this);
    }
    
    public synchronized void setDownloadComplete()
    {
        this.downloadComplete = true;
    }
    
    public synchronized boolean isDownloadComplete()
    {
        return downloadComplete;
    }
        


}
