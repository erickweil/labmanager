/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.broadcasters;

import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.streaming.StreamBroadcaster;
import br.erickweil.test.HilbertImage;
import br.erickweil.test.HilbertStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Time;

/**
 *
 * @author Usuario
 */
public class HilbertBroadcaster extends StreamBroadcaster{
    static final int packet_size = 1024;
    static final int header_size = 8;
    static final int data_size = packet_size-header_size;
    
    private HilbertStream hilbimg;
    private final int fullFrameInterval;
    public static long encode_bytes_sent = 0;
    
    private AsyncStreamEncoder encoder;
    private int threshold;
    public static long allowed_delay = 100;


    static class StreamPacket{
        final BufferedImage data;
        final long time_received;
        public StreamPacket(final BufferedImage data,final long time_received){
            this.time_received = time_received;
            this.data = data;
        }
    }
    
      
    @Override
    protected void on_stopTask() {
        try{
        handler.sendMessage(null);
        }catch(Exception e){}
        encoder.stopTask();
    }
    
    public ThreadSafeHandler<StreamPacket> handler;
    public HilbertBroadcaster(String[] addresses, int port, float framerate, int transmission_width,Broadcast_Mode mode,int fullFrameInterval, int threshold) {
        super(addresses, port, framerate, transmission_width,mode);
        this.fullFrameInterval = fullFrameInterval;
        handler = new ThreadSafeHandler<>();
        handler.instant_send = true;
        this.threshold = threshold;
    }
        
    
    public HilbertBroadcaster(String[] addresses, int port, float framerate, int transmission_width,Broadcast_Mode mode,int fullFrameInterval) {
        this(addresses, port, framerate, transmission_width,mode,fullFrameInterval,50);
    }
    
    @Override
    public void init_stream(int sw,int sh) throws InterruptedException, IOException {
        System.out.println("BROADCASTING HILBERT");
        threshold = threshold == -1 ? 100 : threshold;
        if(transmission_width < 0)
        {
            hilbimg = new HilbertStream(sw,sh, threshold);
        }
        else
        {
            hilbimg = new HilbertStream(transmission_width,transmission_height, threshold);
        }
        
        // init everyone
        for(int i=0;i<10;i++)
        {
            byte[] sendData = new byte[packet_size];
            sendData[0] = (byte)(-1&0xFF);
            sendData[1] = (byte)((-1>>8)&0xFF);
            sendData[2] = (byte)((-1>>16)&0xFF);
            sendData[3] = (byte)((-1>>24)&0xFF);
            sendData[4] = (byte)(hilbimg.width&0xFF);
            sendData[5] = (byte)((hilbimg.width>>8)&0xFF);
            sendData[6] = (byte)(hilbimg.height&0xFF);
            sendData[7] = (byte)((hilbimg.height>>8)&0xFF);
            //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            //clientSocket.send(sendPacket);
            sendPacket(sendData);
            //encode_bytes_sent += packet_size;
            Thread.sleep(5);
        }
        encoder = new AsyncStreamEncoder(hilbimg, this, handler);
        new Thread(encoder).start();
    }
    
    @Override
    public long encode(BufferedImage low_screen) throws Exception
    {
        
        handler.sendMessage(new StreamPacket(low_screen,System.currentTimeMillis()));
        return 0;
    }
    
    static class AsyncStreamEncoder extends StoppableTask{
        ThreadSafeHandler<StreamPacket> handler;
        private final HilbertBroadcaster instance;
        //private final InetAddress IPAddress;
        //private final int port;
        //private final DatagramSocket clientSocket;
        private final HilbertStream img;
        public AsyncStreamEncoder(HilbertStream img,HilbertBroadcaster instance, ThreadSafeHandler<StreamPacket> handler)
        {
            this.img = img;
            this.instance = instance;
            //this.IPAddress = IPAddress;
            //this.port = port;
            //this.clientSocket = clientSocket;
            
            this.handler = handler;
        }

        @Override
        public void runTask() {
            handler.waitMessage();
            ThreadSafeHandler.DataLink<StreamPacket> link = handler.getMessage();
            while (!Thread.currentThread().isInterrupted() && isRunning()) {

                StreamPacket packet = link.getData();
                if (packet == null) {
                    break;
                }
                
                if((System.currentTimeMillis() - packet.time_received) < allowed_delay)
                {
                    try{
                        final BufferedImage data = packet.data;
                        
                        encode(data);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }

                ThreadSafeHandler.DataLink<StreamPacket> n = link.next();
                while(n == null) {
                    handler.waitMessage();
                    n = link.next();
                }
                link = n;
            }
        }
        
        
        private int count = 0;
        private int max_reads = 0;
        private long before_nframes = 0; 
        public long encode(BufferedImage low_screen) throws Exception
        {
            
            count++;
            if(count == 0 || count % instance.fullFrameInterval == 0)
            {
                img.resetLastData();
            }
            long starttime = System.currentTimeMillis();
            img.loadData(low_screen);
            test_elapsed_load = (int)(System.currentTimeMillis() - starttime);
            starttime = System.currentTimeMillis();
                    
            //encode_bytes_sent = 0;
            if(instance.max_bandwith > 0)
            {
            if(instance.last_bandwith > 0 && (System.currentTimeMillis() - before_nframes) > 1000)
            {
                if(instance.last_bandwith > (instance.max_bandwith))
                {
                    img.factor = (int)(img.factor * 2.0f);
                }
                else if(instance.last_bandwith > (instance.max_bandwith)*0.8)
                {
                    img.factor = (int)(img.factor * 1.5f);
                }
                else if(instance.last_bandwith > (instance.max_bandwith)*0.6)
                {
                    //img.factor = (int)(img.factor * 1.5f);
                }
                else if(instance.last_bandwith > (instance.max_bandwith)*0.4)
                {
                    img.factor = (int)(img.factor * 0.75f);
                }
                else
                {
                    img.factor = (int)(img.factor * 0.5f);
                }
                img.factor = Math.max(instance.threshold/2, img.factor);
                System.out.println(instance.last_bandwith+":"+img.factor);
                
                before_nframes = System.currentTimeMillis();
            }
            }
            

            final float packets_per_s;
            final float nanos_to_each_packet; 
            final int time_to_n_packets;
            
            if(instance.max_bandwith > 0)
            {
                packets_per_s = (float)instance.max_bandwith / (float)packet_size;
                nanos_to_each_packet = 1000000000.0f / packets_per_s;
                time_to_n_packets = (int)(nanos_to_each_packet * 20.0f) / 10;
            }
            else
            {
                packets_per_s = 100000000.0f / (float)packet_size;
                nanos_to_each_packet = 1000000000.0f / packets_per_s;
                time_to_n_packets = (int)(nanos_to_each_packet * 20.0f) / 10;
            }
            
            img.encode(new HilbertImage.HilbertEncoderListener() {

            int packets_sent = 0;
            //int last_index = 0;
            int data_off = header_size;
            final byte[] sendData = new byte[packet_size];
            long last_n_packet_sent = 0;
            //long last_10_packet_sent = 0;
            @Override
            public void visit(int index,int length,int pixel) throws IOException, InterruptedException {
            //System.out.println("index:"+index+" dataoff:"+data_off);
            if(data_off == header_size)
            {
                if(pixel == 0) return;
                sendData[0] = (byte)(index&0xFF);
                sendData[1] = (byte)((index>>8)&0xFF);
                sendData[2] = (byte)((index>>16)&0xFF);
                sendData[3] = (byte)((index>>24)&0xFF);
            }

            if(length < 255)
            {
                sendData[data_off++] = (byte)length;
            }
            else
            {
                sendData[data_off++] = (byte)255;
                sendData[data_off++] = (byte)(length&0xFF);
                sendData[data_off++] = (byte)((length>>8)&0xFF);
            }
            sendData[data_off++] = (byte)(pixel&0xFF);
            sendData[data_off++] = (byte)((pixel>>8)&0xFF);
            
            max_reads++;

            //System.out.println(index+":"+length+":"+pixel);

            if(data_off >= (packet_size-4) || (index+length) >= img.hilbert_indexes.length -2)
            {
                //int data_index = sendData[0]&0xFF | (sendData[1]<<8)&0xFF00 | (sendData[2]<<16)&0xFF0000 | (sendData[3]<<24)&0xFF000000;
                //System.out.println(data_index+" l:"+(sendData[header_size+0]&0xFF)+" p:"+(sendData[header_size+1]&0xFF));
                sendData[4] = (byte)(max_reads&0xFF);
                sendData[5] = (byte)((max_reads>>8)&0xFF);
                
                max_reads = 0;
                
                //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                //clientSocket.send(sendPacket);
                instance.sendPacket(sendData);
                
                //printPacket(sendData,data_off);
                //System.out.println(data_off);
                data_off = header_size;
                packets_sent++;
                encode_bytes_sent += packet_size;

                // em media leva 1/10 de um milisegundo para processar um pacote
                // e ainda tem a folga do tempo do print screen
                // isso controla o bandwidth
                if(packets_sent % 20 == 0)
                {
                    //if((last_n_packet_sent - System.nanoTime()) < 1500000)
                    //{
                    //Thread.sleep(1);  
                    //}
                    int diff =(int)(time_to_n_packets - (last_n_packet_sent - System.nanoTime()));
                    if(diff > 0)
                    {
                        if(instance.max_bandwith > 0)Thread.sleep(diff / 1000000, diff % 1000000);
                    }
                    last_n_packet_sent = System.nanoTime();
                }
                //Thread.sleep(1);
                
                
                // to nem ai
                    // nem ai mesmo
                    //sendData = new byte[packet_size];
            }
            //last_index = index;
            }
            });

            Thread.sleep(1);
            byte[] sendData = new byte[packet_size];
            sendData[0] = (byte)(-1&0xFF);
            sendData[1] = (byte)((-1>>8)&0xFF);
            sendData[2] = (byte)((-1>>16)&0xFF);
            sendData[3] = (byte)((-1>>24)&0xFF);
            sendData[4] = (byte)(img.width&0xFF);
            sendData[5] = (byte)((img.width>>8)&0xFF);
            sendData[6] = (byte)(img.height&0xFF);
            sendData[7] = (byte)((img.height>>8)&0xFF);
            //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            //clientSocket.send(sendPacket);
            instance.sendPacket(sendData);
            
            encode_bytes_sent += packet_size;
            // leva 5 milisegundos para processar a imagem
            //Thread.sleep(10);
            
            test_elapsed_encode = (int)(System.currentTimeMillis() - starttime);
            //test_elapsed_encode -=1;
            return encode_bytes_sent;
        }
        
        private void printPacket(byte[] data,int length)
        {
            System.out.println("--------------- PACKET ----------------");
            int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
            System.out.println("index:"+index);
            for(int i=header_size;i<length;i+=2)
            {
                System.out.print(data[i]&0xFF);
                System.out.println(":"+(data[i+1]&0xFF));
            }
        }
        
        @Override
        protected void on_stopTask() {
            
        }
    }

}
