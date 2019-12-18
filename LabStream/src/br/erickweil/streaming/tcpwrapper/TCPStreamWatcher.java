/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.tcpwrapper;

import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.streaming.StreamWatcher.ImageListener;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebClient;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public abstract class TCPStreamWatcher  extends StoppableTask implements ProtocolFactory{
    protected final String address;
    protected final int port;
    protected int transmission_width;
    protected int transmission_height;
    protected ImageListener listener;
    public static final boolean LOG = true;
    private WebClient client;
    public static class WatcherProtocol extends ServerProtocol{

        final TCPStreamWatcher instance;

        public WatcherProtocol(TCPStreamWatcher instance) {
            this.instance = instance;
        }
        
        @Override
        public void processRequest() throws IOException {
            
            int width = Integer.parseInt(readln_url());
            int height = Integer.parseInt(readln_url());
            instance.init(width,height);
            while(true)
            {
                int imgLength = Integer.parseInt(readln_url());
                if(imgLength == 0) return;
                byte[] imgIncoming = new byte[imgLength];
                input.readFully(imgIncoming);
                instance.decode(imgIncoming);
            }
        }
        
    }
    
    @Override
    public ServerProtocol get() {
            // TODO Auto-generated method stub
        return new WatcherProtocol(this);
    }
    
    public TCPStreamWatcher(String address,int port,ImageListener listener)
    {
        this.address = address;
        this.port = port;
        this.listener = listener;
    }
    
    @Override
    public void runTask()
    {
        if(LOG)System.out.println("Iniciando TCP Watcher.");
        try {
            while (!Thread.currentThread().isInterrupted() && isRunning()) {
                connect();
            }
        } catch (Exception ex) {
            Logger.getLogger(TCPStreamWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void connect() throws Exception
    {        
        client = null;
        try
        {
            client = new WebClient(address,port, this);
        }
        catch(SecurityException | NullPointerException | IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new Exception("Erro ao Criar socket:",e);
        }

        try
        {
            client.run();
        }
        catch(SecurityException  | NullPointerException | IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new Exception("Erro ao Criar socket:",e);
        }
    }
    
    public abstract void init(int width,int height) throws IOException;
    
    public abstract void decode(byte[] data) throws IOException;
}
