/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.watchers;

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.streaming.StreamWatcher;
import br.erickweil.test.HilbertStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class HilbertWatcher extends StreamWatcher{
    static final int packet_size = 1024;
    static final int header_size = 8;
    static final int data_size = packet_size-header_size;
    private HilbertStream img;
    private AsyncStreamDecoder decoder;
    public static long allowed_delay = 500;
    
    static class StreamPacket{
        final byte[] data;
        final long time_received;
        public StreamPacket(final byte[] data,final long time_received){
            this.time_received = time_received;
            this.data = data;
        }
    }
    
    public ThreadSafeHandler<StreamPacket> handler;
    
    public HilbertWatcher(String address, int port, ImageListener listener) {
        super(address, port, listener);
        handler = new ThreadSafeHandler<>();
        // para n esperar 1 segundo
        handler.instant_send = true;
    }
    
       @Override
    public void on_stopTask() {
        try {
            handler.sendMessage(null);
        } catch (InterruptedException e) {
            
        }
        decoder.stopTask();
    }
    
    

    @Override
    public void init() throws IOException {
        byte[] receiveData = new byte[packet_size];
        transmission_width = 0;
        while (transmission_width == 0) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
            if(index == -1)
            {
                transmission_width = data[4] & 0xFF | (data[5] << 8) & 0xFF00;
                transmission_height = data[6] & 0xFF | (data[7] << 8) & 0xFF00;
            }
        }
        System.out.println("w:"+transmission_width+" h:"+transmission_height);
        img = new HilbertStream(transmission_width, transmission_height, 50);
        decoder = new AsyncStreamDecoder(img, handler, listener);
        new Thread(decoder).start();
    }
    
    @Override
    public void decode() throws IOException {
        byte[] receiveData = new byte[packet_size];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        //System.out.println(receivePacket.getOffset());
        final byte[] data = receivePacket.getData();
        try {
            handler.sendMessage(new StreamPacket(data,System.currentTimeMillis()));
            //receive_hilbert(data);
        } catch (Exception ex) {
            Logger.getLogger(HilbertWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static class AsyncStreamDecoder extends StoppableTask{
        ThreadSafeHandler<StreamPacket> handler;
        private final ImageListener listener;
        private final HilbertStream img;
       
        public AsyncStreamDecoder(HilbertStream img, ThreadSafeHandler<StreamPacket> handler, ImageListener listener)
        {
            this.img = img;
            this.listener = listener;
            this.handler = handler;
        }

        @Override
        public void runTask() {
            handler.waitMessage();
            ThreadSafeHandler.DataLink<StreamPacket> link = handler.getMessage();
            while (true) {

                StreamPacket packet = link.getData();
                if (packet == null) {
                    break;
                }
                if((System.currentTimeMillis() - packet.time_received) < allowed_delay)
                {
                    final byte[] data = packet.data;
                    receive_hilbert(data);
                }

                ThreadSafeHandler.DataLink<StreamPacket> n = link.next();
                while (n == null) {
                    handler.waitMessage();
                    n = link.next();
                }
                link = n;
            }
        }
        
        static int last_index = -1;
        int decode_number = 0;
        int decode_elapsed = 0;
        int array_elapsed = 0;
        long last_frame = 0;
        BufferedImage gen_img;
        public void receive_hilbert(byte[] data)
        {
            //printPacket(data,img.hilbert_indexes.length);
            int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
            if ((index == -1 || last_index > (index+packet_size*10)) && img != null && listener != null)
            {
                long starttime = System.currentTimeMillis();
                gen_img = img.getImage(gen_img);
                int getimage_elapsed = (int)(System.currentTimeMillis() - starttime);
                starttime = System.currentTimeMillis();
                listener.setImage(gen_img);
                int setimage_elapsed = (int)(System.currentTimeMillis() - starttime);
                int frame_elapsed = (int)(System.currentTimeMillis() - last_frame);
                float frame_rate = (1000.0f/(float)frame_elapsed);
                System.out.println(
                        "FPS:"+frame_rate
                        +" gen img:"+getimage_elapsed
                        +" set img:"+setimage_elapsed
                        +" decode:"+((float)decode_elapsed/(float)decode_number)
                        +" array:"+((float)array_elapsed/(float)decode_number));
                last_frame = System.currentTimeMillis();
            } 
            else if (index != -1) 
            {
                int max_reads = data[4] & 0xFF | (data[5] << 8) & 0xFF00;
                //System.out.println("DECODING: " + index);
                long starttime = System.currentTimeMillis();
                byte[] pixels = new byte[data_size];
                System.arraycopy(data, header_size, pixels, 0, data_size);
                array_elapsed += (System.currentTimeMillis() - starttime);
                starttime = System.currentTimeMillis();
                try 
                {
                    img.decode(new ByteArrayInputStream(pixels), index, max_reads);
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
                decode_elapsed += (System.currentTimeMillis() - starttime);
                decode_number++;
            }
            last_index = index;
        }
        
        private void printPacket(byte[] data,int max)
        {
            
            System.out.println("--------------- PACKET ----------------");
            int index = data[0] & 0xFF | (data[1] << 8) & 0xFF00 | (data[2] << 16) & 0xFF0000 | (data[3] << 24) & 0xFF000000;
            System.out.println("index:"+index);
            if(index == -1) return;
            int t = index;
            for(int i=header_size;i<data.length && t < max;i+=2)
            {
                System.out.print(data[i]&0xFF);
                System.out.println(":"+(data[i+1]&0xFF));
                t+= data[i]&0xFF;
            }
        }

        @Override
        protected void on_stopTask() {
            
        }
    }
    


}
