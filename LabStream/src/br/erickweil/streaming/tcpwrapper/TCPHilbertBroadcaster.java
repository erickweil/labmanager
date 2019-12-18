/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.tcpwrapper;

import br.erickweil.test.HilbertImage;
import br.erickweil.test.HilbertStream;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Usuario
 */
public class TCPHilbertBroadcaster extends TCPStreamBroadcaster{

    private int fullFrameInterval;
    private int threshold;
    private HilbertStream hilbimg;
    private long before_nframes;
    private ByteArrayOutputStream stream;

    public TCPHilbertBroadcaster(int port, float framerate, int transmission_width,int fullFrameInterval, int threshold) {
        super(port,framerate,transmission_width);
        this.fullFrameInterval = fullFrameInterval;
        this.threshold = threshold;
    }

    @Override
    public void init_stream(int w, int h) throws InterruptedException, IOException {
        System.out.println("BROADCASTING TCP HILBERT");
        threshold = threshold == -1 ? 100 : threshold;
        if(transmission_width < 0)
        {
            hilbimg = new HilbertStream(w,h, threshold);
        }
        else
        {
            hilbimg = new HilbertStream(transmission_width,transmission_height, threshold);
        }
    }

    private int count;
    @Override
    public long encode(BufferedImage low_screen) throws Exception {
                    count++;
            if(count == 0 || count % fullFrameInterval == 0)
            {
                hilbimg.resetLastData();
            }
            long starttime = System.currentTimeMillis();
            hilbimg.loadData(low_screen);
            test_elapsed_load = (int)(System.currentTimeMillis() - starttime);
            starttime = System.currentTimeMillis();
                    
            //encode_bytes_sent = 0;
            if(max_bandwith > 0)
            {
                if(last_bandwith > 0 && (System.currentTimeMillis() - before_nframes) > 1000)
                {
                    if(last_bandwith > (max_bandwith))
                    {
                        hilbimg.factor = (int)(hilbimg.factor * 2.0f);
                    }
                    else if(last_bandwith > (max_bandwith)*0.8)
                    {
                        hilbimg.factor = (int)(hilbimg.factor * 1.5f);
                    }
                    else if(last_bandwith > (max_bandwith)*0.6)
                    {
                        hilbimg.factor = (int)(hilbimg.factor * 0.9f);
                    }
                    else if(last_bandwith > (max_bandwith)*0.4)
                    {
                        hilbimg.factor = (int)(hilbimg.factor * 0.75f);
                    }
                    else
                    {
                        hilbimg.factor = (int)(hilbimg.factor * 0.5f);
                    }
                    hilbimg.factor = Math.max(threshold/2, hilbimg.factor);
                    System.out.println(last_bandwith+":"+hilbimg.factor);

                    before_nframes = System.currentTimeMillis();
                }
            }
            
            
            /*if(instance.max_bandwith > 0)
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
            }*/
            if(stream == null)
            {
                stream = new ByteArrayOutputStream();
            }
            else
            {
                stream.reset();
            }
            int writes = hilbimg.encode(null,stream);
            
            byte[] encoded_img = stream.toByteArray();
            sendImg(encoded_img);
            
            test_elapsed_encode = (int)(System.currentTimeMillis() - starttime);
            //test_elapsed_encode -=1;
            return encoded_img.length;
    }


    
}
