/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.streaming.broadcasters.HilbertBroadcaster;
import br.erickweil.test.HilbertImage;
import br.erickweil.test.HilbertStream;
import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public abstract class StreamBroadcaster extends StoppableTask{

    private DatagramSocket clientSocket;
    private InetAddress[] clientIPAddresses;
    private InetAddress clientIPAddress;

    public static enum Broadcast_Mode{
        network_broadcast,
        multicast,
        multiple_unicast,
        client_propagation
    };
    
    protected final Broadcast_Mode mode;
    protected String[] addresses;
    protected final int port;
    protected final float framerate;
    protected final int transmission_width;
    protected int transmission_height;
    public int max_bandwith = 0; // b/s
    public int last_bandwith; // b/s
    public static int test_elapsed_load;
    public static int test_elapsed_encode;
    public StreamBroadcaster(String[] addresses,int port,float framerate,int transmission_width,Broadcast_Mode mode)
    {
        this.addresses = addresses;
        this.port = port;
        this.framerate = framerate;
        this.transmission_width = transmission_width;
        this.mode = mode;
        
    }

    @Override
    public void runTask() {
        try {
            PrintScreenTool print = new PrintScreenTool();
            transmission_height = (int)((float)transmission_width/print.aspect_ratio);

            //for(int i=0;i<addresses.length;i++)
            //{
            //    clientIPAddresses[i] = InetAddress.getByName(addresses[i]);
            //}
            //clientIPAddress = clientIPAddresses[0];
            updateAddresses();
            
            switch(mode)
            {
                case multicast: 
                    clientSocket = new MulticastSocket();
                    ((MulticastSocket)clientSocket).joinGroup(clientIPAddress);
                break;
                default:
                    clientSocket = new DatagramSocket();
                break;
            
            }
            
            
            init_stream(print.screen_width,print.screen_height);

            long last_measured = System.currentTimeMillis();
            long bytes_sent = 0;
            int sleep_time = (int)(1000.0f/framerate);
            System.out.println("sleep time:"+sleep_time);
            while (!Thread.currentThread().isInterrupted() && isRunning()) {
                long time_start = System.currentTimeMillis();
                long time_measure = time_start;
                BufferedImage trasmission_screen;
                BufferedImage hd_screen = print.printScreen();
                
                int elapsed_print = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                if(transmission_width > 0)
                    trasmission_screen = ImageEdit.resize(hd_screen, transmission_width, transmission_height);
                else
                    trasmission_screen = hd_screen;
                
                int elapsed_resize = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                
                bytes_sent += encode(trasmission_screen);
                
                int elapsed_encode = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                //hilbert_stream(clientSocket, IPAddress, low_screen, last_screen);
                //last_screen = low_screen;

                
                        
                //if(sleep_time > 0)
                //{
                    long elapsed = System.currentTimeMillis() - time_start;
                    if(sleep_time - elapsed > 0)
                    {
                        //System.out.println("sleeped:"+(sleep_time - elapsed));
                        Thread.sleep(sleep_time - elapsed);
                    }
                    else
                    {
                        System.out.println("no sleep!");
                    }
                //}
                
                

                //bytes_sent = HilbertBroadcaster.encode_bytes_sent;
                if((time_start - last_measured) > 1000)
                {
                    System.out.println(
                        "print img:"+elapsed_print
                        +" resize img:"+elapsed_resize
                        +" load img:"+test_elapsed_load
                        +" encode:"+test_elapsed_encode);

                    last_bandwith = (int)(HilbertBroadcaster.encode_bytes_sent / ((time_start - last_measured)/1000));
                    System.out.println(last_bandwith/1024 + " Kb/s");
                    last_measured = System.currentTimeMillis();
                    HilbertBroadcaster.encode_bytes_sent = 0;
                    
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            clientSocket.close();
        }
    }
    
    public synchronized void sendPacket(final byte[] data) throws IOException
    {
        switch(mode)
        {
            case network_broadcast: 
            {
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, clientIPAddress, port);
                clientSocket.setBroadcast(true);
                clientSocket.send(sendPacket);
            }
            break;
            case multicast: 
            {
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, clientIPAddress, port);
                clientSocket.send(sendPacket);
            }
            break;
            case multiple_unicast: 
            {
                for(int i=0;i<clientIPAddresses.length;i++)
                {
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, clientIPAddresses[i], port);
                    clientSocket.send(sendPacket);
                }
            }
            break;
        }
        
    }
    
    private void updateAddresses() throws UnknownHostException
    {
        for(int i=0;i<addresses.length;i++)
        {
            clientIPAddresses[i] = InetAddress.getByName(addresses[i]);
        }
        
        if(addresses.length > 0)
        clientIPAddress = clientIPAddresses[0];
    }
    
    public synchronized void updateAddresses(String[] addresses)
    {
        try {
            this.addresses = addresses;
            clientIPAddresses = new InetAddress[addresses.length];
            updateAddresses();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract void init_stream(int w,int h) throws InterruptedException, IOException;
    
    public abstract long encode(BufferedImage img) throws Exception;

    
    
    
}
