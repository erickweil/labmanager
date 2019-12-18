/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.broadcasters;

import br.erickweil.streaming.StreamBroadcaster;
import br.erickweil.test.JpegHelper;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Usuario
 */
public class JpegBroadcaster extends StreamBroadcaster{

    static final int tile_width = 32;
    
    static final int packet_size = 1024;
    static final int header_size = 8;
    static final int data_size = packet_size-header_size;
    
    public JpegBroadcaster(String[] address, int port, float framerate, int transmission_width,Broadcast_Mode mode) {
        super(address, port, framerate, transmission_width,mode);
    }

    @Override
    public void init_stream(int sw,int sh) {
        
    }
    
       @Override
    public void on_stopTask() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long encode(BufferedImage low_screen) throws Exception {

        int xTiles = transmission_width/tile_width;
        int yTiles = transmission_height/tile_width;
        
        long bytes_sent = 0;
        Raster raster = low_screen.getData();
        BufferedImage tileimage = new BufferedImage(tile_width,tile_width,BufferedImage.TYPE_BYTE_INDEXED);
        int packets_sent = 0;
        byte[] sendData = new byte[packet_size];
        for(int x =0;x<xTiles;x++)
        {
            for(int y =0;y<yTiles;y++)
            {
                tileimage = low_screen.getSubimage(x*tile_width, y*tile_width, tile_width, tile_width);
                byte[] data = JpegHelper.compress_jpeg(tileimage, 0.15f);
                
                System.arraycopy(data, 0, sendData, header_size, data.length);
                sendData[0] = (byte)(x&0xFF);
                sendData[1] = (byte)((x>>8)&0xFF);
                sendData[2] = (byte)(y&0xFF);
                sendData[3] = (byte)((y>>8)&0xFF);
                sendData[4] = (byte)(tile_width&0xFF);
                sendData[5] = (byte)((tile_width>>8)&0xFF);
                sendData[6] = (byte)(data.length&0xFF);
                sendData[7] = (byte)((data.length>>8)&0xFF);
            
                //System.out.println(data_index+" l:"+(sendData[header_size+0]&0xFF)+" p:"+(sendData[header_size+1]&0xFF));
                //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                //clientSocket.send(sendPacket);
                sendPacket(sendData);
                bytes_sent += packet_size;
                packets_sent++;
                if(packets_sent % 10 == 0)
                Thread.sleep(1);
                
                //System.out.println("x:"+x+" y:"+y+" l:"+data.length);
            }    
        }
        
        Thread.sleep(10);
        sendData[0] = (byte)(-1&0xFF);
        sendData[1] = (byte)((-1>>8)&0xFF);
        sendData[2] = (byte)(-1&0xFF);
        sendData[3] = (byte)((-1>>8)&0xFF);
        sendData[4] = (byte)(-1&0xFF);
        sendData[5] = (byte)((-1>>8)&0xFF);
        sendData[6] = (byte)(-1&0xFF);
        sendData[7] = (byte)((-1>>8)&0xFF);

        //System.out.println(data_index+" l:"+(sendData[header_size+0]&0xFF)+" p:"+(sendData[header_size+1]&0xFF));
        //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        //clientSocket.send(sendPacket);
        sendPacket(sendData);
        packets_sent++;
        bytes_sent += packet_size;
        
        return bytes_sent;
    }
    
}
