/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.erickweil.test;

import br.erickweil.test.HilbertImage.HilbertEncoderListener;
import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class StreamingTest {
    //https://stackoverflow.com/questions/24668407/how-to-take-a-screenshot-of-desktop-fast-with-java-in-windows-ffmpeg-etc
    
    public static HilbertStream hilbimg;
    
    static final int tile_width = 32;
    static int transmission_width = 1024;
    static int transmission_height = 512;
    
    static final int packet_size = 1024;
    static final int header_size = 8;
    static final int data_size = packet_size-header_size;
    
    static private long last_measured = 0;
    static private long bytes_sent = 0;
    
    public static void main(String[] args) throws AWTException, InterruptedException, IOException, Exception
    {
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        float aspect_ratio = (float)screen_width / (float)screen_height;
        Robot robot = new Robot();
        Rectangle screen_rectangle = new Rectangle(screen_width, screen_height);
        transmission_height = (int)((float)transmission_width/aspect_ratio);
        
        DatagramSocket clientSocket = new DatagramSocket();
        //InetAddress IPAddress = InetAddress.getByName("192.168.2.99");
        InetAddress IPAddress = InetAddress.getByName("localhost");
        //int transmission_height = 720;
        BufferedImage last_screen = null;
        hilbimg = new HilbertStream(transmission_width,transmission_height, 50);
        
        last_measured = System.currentTimeMillis();
        while(true)
        {
            long time_start = System.currentTimeMillis();
            BufferedImage hd_screen = robot.createScreenCapture(screen_rectangle);
            //System.out.println("Screenshot: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
            
            
            BufferedImage low_screen = new BufferedImage(transmission_width,transmission_height, BufferedImage.TYPE_INT_RGB);
            Graphics g = low_screen.createGraphics();
            g.drawImage( hd_screen, 0, 0, transmission_width, transmission_height, null);
            g.dispose();
            //System.out.println("Resize: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
            
            hilbert_stream(clientSocket, IPAddress, low_screen, last_screen);
            //jpeg_stream(clientSocket, IPAddress, low_screen, last_screen);

            //System.out.println("transmission: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
            
            last_screen = low_screen;
            //File outputfile = new File("image_sent.bmp");
            //ImageIO.write(low_screen, "bmp", outputfile);
            //System.out.println("saving: "+(System.currentTimeMillis()-time_start));
            //time_start = System.currentTimeMillis();
            Thread.sleep(50);
            //break;
            
            if((time_start - last_measured) > 5000)
            {
                
                System.out.println((bytes_sent / ((time_start - last_measured)/1000))/1024 + " Kb/s");
                last_measured = System.currentTimeMillis();
                bytes_sent = 0;
            }
        }
    }
    
    
    
    public static void jpeg_stream(DatagramSocket clientSocket,InetAddress IPAddress ,BufferedImage low_screen,BufferedImage last_screen) throws Exception
    {
        int xTiles = transmission_width/tile_width;
        int yTiles = transmission_height/tile_width;
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
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                clientSocket.send(sendPacket);
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
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
        packets_sent++;
        
        
    }
    
    public static void hilbert_stream(DatagramSocket clientSocket,InetAddress IPAddress ,BufferedImage low_screen,BufferedImage last_screen) throws Exception
    {
        hilbimg.loadData(low_screen);

        hilbimg.encode(new HilbertEncoderListener() {

        int packets_sent = 0;
        int last_index = 0;
        int data_off = header_size;
        byte[] sendData = new byte[packet_size];
        @Override
        public void visit(int index,int length,int pixel) throws IOException, InterruptedException {
        //System.out.println("index:"+index+" dataoff:"+data_off);
        if(data_off == header_size)
        {
            sendData[0] = (byte)(last_index&0xFF);
            sendData[1] = (byte)((last_index>>8)&0xFF);
            sendData[2] = (byte)((last_index>>16)&0xFF);
            sendData[3] = (byte)((last_index>>24)&0xFF);
        }

        sendData[data_off++] = (byte)length;

        sendData[data_off++] = (byte)pixel;



        if(data_off >= packet_size)
        {
            int data_index = sendData[0]&0xFF | (sendData[1]<<8)&0xFF00 | (sendData[2]<<16)&0xFF0000 | (sendData[3]<<24)&0xFF000000;
            //System.out.println(data_index+" l:"+(sendData[header_size+0]&0xFF)+" p:"+(sendData[header_size+1]&0xFF));
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);
            data_off = header_size;
            packets_sent++;
            bytes_sent += packet_size;
            
            if(packets_sent % 10 == 0)
            Thread.sleep(1);
            sendData = new byte[packet_size];
        }
        last_index = index;
        }
        });

        Thread.sleep(20);
        byte[] sendData = new byte[packet_size];
        sendData[0] = (byte)(-1&0xFF);
        sendData[1] = (byte)((-1>>8)&0xFF);
        sendData[2] = (byte)((-1>>16)&0xFF);
        sendData[3] = (byte)((-1>>24)&0xFF);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);

        
    }
}
