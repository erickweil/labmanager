/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import br.erickweil.labamanger.common.StoppableTask;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public abstract class StreamWatcher extends StoppableTask{
    protected final String address;
    protected final int port;
    protected int transmission_width;
    protected int transmission_height;
    protected DatagramSocket serverSocket;
    protected ImageListener listener;
    public static final boolean LOG = true;
    public static abstract interface ImageListener{
        public abstract void setImage(BufferedImage img);
    }
    
    public StreamWatcher(String address,int port,ImageListener listener)
    {
        this.address = address;
        this.port = port;
        this.listener = listener;
    }
    
    @Override
    public void runTask()
    {
        if(LOG)System.out.println("Iniciando Watcher.");
        try
        {
            if(InetAddress.getByName(address).isMulticastAddress())
            {
                MulticastSocket msocket = new MulticastSocket(port);
                InetAddress group = InetAddress.getByName(address);
                msocket.joinGroup(group);
                
                serverSocket = msocket;
            }
            else
            {
                serverSocket = new DatagramSocket(port);
            }
            init();
            while (!Thread.currentThread().isInterrupted() && isRunning()) {
                decode();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
                if(InetAddress.getByName(address).isMulticastAddress())
                {
                    try {
                        MulticastSocket msocket = (MulticastSocket)serverSocket;
                        InetAddress group = InetAddress.getByName(address);
                        msocket.leaveGroup(group);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                        e.printStackTrace();
            }
            serverSocket.close();
            serverSocket.disconnect();
        }
    }
    
    public abstract void init() throws IOException;
    
    public abstract void decode() throws IOException;
}
