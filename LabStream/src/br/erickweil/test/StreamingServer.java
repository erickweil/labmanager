/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.test;

import br.erickweil.streaming.StreamWatcher.ImageListener;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class StreamingServer {

    
    static final int packet_size = 1024;
    static final int header_size = 8;
    static final int data_size = packet_size - header_size;
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        imagelistener = new StreamingWindow();
        imagelistener.run();
        System.out.println("??");
        //StreamingServer instance = new StreamingServer();
        //StreamingWindow.run(instance);
        

        int transmission_width = 0;
        int transmission_height = 0;
        //myImg = new BufferedImage(transmission_width, (int)(transmission_width*0.8f), BufferedImage.TYPE_INT_RGB);
        byte[] receiveData = new byte[packet_size];
        while (transmission_width == 0) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
            
            
            System.out.println("index: "+index);
            if(index == -1)
            {
                transmission_width = data[4] & 0xFF | (data[5] << 8) & 0xFF00;
                transmission_height = data[6] & 0xFF | (data[7] << 8) & 0xFF00;
                
                
                System.out.println("w: "+transmission_width+" h:"+transmission_height);
            }
        }
        System.out.println("w:"+transmission_width+" h:"+transmission_height);
        img = new HilbertStream(transmission_width, transmission_height, 50);
        //List<byte[]> list_packets = new ArrayList<>();
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            //System.out.println(receivePacket.getOffset());
            final byte[] data = receivePacket.getData();
            //receive_jpeg(data);
            receive_hilbert(data);
                  //byte[] store = new byte[packet_size];
            //System.arraycopy(data, 0, store, 0, packet_size);
            //list_packets.add(store);

                  //String sentence = new String( receivePacket.getData());
            //System.out.println(index+" l:"+(data[header_size+0]&0xFF)+" p:"+(data[header_size+1]&0xFF));
            //System.out.println(data.length);

        }
               //   InetAddress IPAddress = receivePacket.getAddress();
        //   int port = receivePacket.getPort();
        //for(int i=0;i<list_packets.size();i++)
        //{

                  //String capitalizedSentence = sentence.toUpperCase();
        //sendData = capitalizedSentence.getBytes();
        //DatagramPacket sendPacket =
        //new DatagramPacket(sendData, sendData.length, IPAddress, port);
        //serverSocket.send(sendPacket);
        //File outputfile = new File("image_received.bmp");
        //ImageIO.write(img.getImage(), "bmp", outputfile);
    }
    
    public static void receive_jpeg(byte[] data) throws Exception
    {
        // x
        // y
        // w
        int x = data[0]&0xFF | (data[1]<<8)&0xFF00;
        int y = data[2]&0xFF | (data[3]<<8)&0xFF00;
        int tile_width = data[4]&0xFF | (data[5]<<8)&0xFF00;
        int length = data[6]&0xFF | (data[7]<<8)&0xFF00;
        
        if(length == 0xFFFF)
        {
            imagelistener.setImage(myImg);
        }
        else
        {
            //System.out.println(x+","+y+" w"+tile_width+" l:"+length);
            byte[] jpeg_tile_data = new byte[length];
            System.arraycopy(data, header_size, jpeg_tile_data, 0, length);
            BufferedImage im = JpegHelper.decompress_jpeg(jpeg_tile_data);
            Graphics g = myImg.getGraphics();
            g.drawImage(im, x*tile_width, y*tile_width, null);
            g.dispose();
        }
        
    }
    static BufferedImage myImg;
    static int last_index = -1;
    public static void receive_hilbert(byte[] data) throws Exception
    {
        int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
        if ((index == -1 || last_index > (index+packet_size*10)) && img != null && imagelistener != null) {
            BufferedImage resimage = img.getImage();

            imagelistener.setImage(resimage);
        } else if (index != -1) {
        // System.out.println("DECODING: " + index);
        byte[] pixels = new byte[data_size];
        System.arraycopy(data, header_size, pixels, 0, data_size);
        try {
        img.decode(new ByteArrayInputStream(pixels), index, data_size / 2);
        } catch (EOFException e) {
        }

        }
        last_index = index;
    }
    
    
    static HilbertStream img;
    static StreamingWindow imagelistener;
}
