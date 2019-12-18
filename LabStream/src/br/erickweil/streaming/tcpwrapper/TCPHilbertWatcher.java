/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.tcpwrapper;

import br.erickweil.streaming.StreamWatcher.ImageListener;
import br.erickweil.test.HilbertStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * @author Usuario
 */
public class TCPHilbertWatcher extends TCPStreamWatcher{

    private HilbertStream img;
    private BufferedImage gen_img;
    private long last_frame;

    public TCPHilbertWatcher(String address, int port, ImageListener listener) {
        super(address, port, listener);
    }

    @Override
    public void init(int width, int height) throws IOException {
        System.out.println("w:"+width+" h:"+height);
        img = new HilbertStream(width, height, 50);
    }

    @Override
    public void decode(byte[] data) throws IOException {
        try 
        {
           //System.out.println("decoding...");
           img.decode(new ByteArrayInputStream(data), 0, -1);
           
           long starttime = System.currentTimeMillis();
           //System.out.println("generating...");
           gen_img = img.getImage(gen_img);
            int getimage_elapsed = (int)(System.currentTimeMillis() - starttime);
            starttime = System.currentTimeMillis();
           //System.out.println("displaying...");
            listener.setImage(gen_img);
            int setimage_elapsed = (int)(System.currentTimeMillis() - starttime);
            int frame_elapsed = (int)(System.currentTimeMillis() - last_frame);
            float frame_rate = (1000.0f/(float)frame_elapsed);
            System.out.println(
                    "FPS:"+frame_rate
                    +" gen img:"+getimage_elapsed
                    +" set img:"+setimage_elapsed
            );
                    //+" decode:"+((float)decode_elapsed/(float)decode_number)
                    //+" array:"+((float)array_elapsed/(float)decode_number));
            last_frame = System.currentTimeMillis();
            //System.out.println("finished!\n");
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void on_stopTask() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
