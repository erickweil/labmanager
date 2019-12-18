/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming.tcpwrapper;

import br.erickweil.labamanger.common.StoppableTask;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.streaming.ImageEdit;
import br.erickweil.streaming.PrintScreenTool;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public abstract class TCPStreamBroadcaster extends StoppableTask{
    
    
    public ThreadSafeHandler<String> handler;
    public ThreadSafeHandler<String> encoder_handler;
    private WebServer server;
    private StoppableTask serverTask;
    private PrintScreenListener encoderTask;
    private byte[] encoded_img;
    protected final int port;
    protected final float framerate;
    protected final int transmission_width;
    protected int transmission_height;
    
    protected int img_width;
    protected int img_height;
    
    protected int test_elapsed_load;
    protected int test_elapsed_encode;
    protected int last_bandwith;
    
    public int max_bandwith = 0; // b/s
    private BufferedImage trasmission_screen;
    
    protected synchronized BufferedImage getPrint()
    {
        return trasmission_screen;
    }
    
    protected synchronized void setPrint(BufferedImage img)
    {
        trasmission_screen = img;
    }
    
    protected synchronized byte[] getImg()
    {
        return encoded_img;
    }
    
    protected synchronized void sendImg(byte[] img) throws InterruptedException
    {
        encoded_img = img;

        handler.sendMessage("SEND");
    }
    
    public static class BroadcasterWrapper extends ServerProtocol {
        //private final ThreadSafeListener listener;
        private final ThreadSafeHandler<String> handler;
        private final TCPStreamBroadcaster instance;
        private final int transmission_width;
        private final int transmission_height;

        public BroadcasterWrapper(ThreadSafeHandler<String> handler
                ,TCPStreamBroadcaster instance
                , int transmission_width, int transmission_height) {
            //this.listener = listener;
            this.handler = handler;
            this.instance = instance;
            this.transmission_width  = transmission_width;
            this.transmission_height  = transmission_height;
        }

        @Override
        public void processRequest() throws IOException {
            handler.waitMessage();
            ThreadSafeHandler.DataLink<String> link = handler.getMessage();
            writeln_url(""+transmission_width);
            writeln_url(""+transmission_height);
            System.out.println("written:"+transmission_width+","+transmission_height);
            while (true) {

                String msg = link.getData();
                if (msg == null) {
                    break;
                }
                
                //if(msg.equals("SEND"))
                //{
                
                    byte[] encoded_img = instance.getImg();
                    if(encoded_img == null)
                    {
                        writeln_url("0");
                        break;
                    }

                    writeln_url(""+encoded_img.length);
                    output.write(encoded_img);
                
                //}
                
                ThreadSafeHandler.DataLink<String> n = link.next();
                if (n == null) {
                    handler.waitMessage();
                    n = link.next();
                }
                else
                {
                    ThreadSafeHandler.DataLink<String> outdated;
                    while((outdated = n.next()) != null)
                    {
                        n = outdated;
                        System.err.println("off sync");
                    }
                }
                link = n;
            }
        }
    
    }
    
    public static class PrintScreenListener extends StoppableTask{
        //private final ThreadSafeListener listener;
        private final ThreadSafeHandler<String> handler;
        private final TCPStreamBroadcaster instance;
        private long time_measure;

        public PrintScreenListener(ThreadSafeHandler<String> handler
                ,TCPStreamBroadcaster instance) {
            //this.listener = listener;
            this.handler = handler;
            this.instance = instance;
        }

        public void processRequest() throws IOException, Exception {
            
            //System.out.println("waiting...");
            handler.waitMessage();
            ThreadSafeHandler.DataLink<String> link = handler.getMessage();
            while (true) {

                //System.out.println("geting...");
                String msg = link.getData();
                if (msg == null) {
                    break;
                }
                
                //if(msg.equals("PRINT"))
                //{
                    time_measure = System.currentTimeMillis();
                    BufferedImage print = instance.getPrint();
                    instance.img_width = print.getWidth();
                    instance.img_height = print.getHeight();
                    
                    //System.out.println("encoding...");
                    instance.encode(print);
                
                    instance.test_elapsed_encode = (int)(System.currentTimeMillis() - time_measure);
                    //System.out.println("AFF:"+instance.img_width);
                    
                //}
                ThreadSafeHandler.DataLink<String> n = link.next();
                if (n == null) {
                    handler.waitMessage();
                    n = link.next();
                }
                else
                {
                    ThreadSafeHandler.DataLink<String> outdated;
                    while((outdated = n.next()) != null)
                    {
                        n = outdated;
                        System.err.println("off sync");
                    }
                }
                link = n;
            }
        }

        @Override
        protected void on_stopTask() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void runTask() {
            try {
                System.out.println("STARTED ENCODER");
                processRequest();
            } catch (Exception ex) {
                Logger.getLogger(TCPStreamBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
    
    public TCPStreamBroadcaster(int port, float framerate, int transmission_width) {
        handler = new ThreadSafeHandler<>(false);
        handler.instant_send = true;
        encoder_handler = new ThreadSafeHandler<>(false);
        encoder_handler.instant_send = true;
        this.encoderTask = new PrintScreenListener(encoder_handler, this);
        this.port = port;
        this.framerate = framerate;
        this.transmission_width = transmission_width;
   }

    @Override
    protected void on_stopTask() {
        encoded_img = null;
        try {
            //super.on_stopTask(); //To change body of generated methods, choose Tools | Templates.
            handler.sendMessage(null);
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(TCPStreamBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        server.stop();
        serverTask.stopTask();
        encoderTask.stopTask();
    }
    
    private void startServer()
    {
        if(server != null)
        {
            server.stop();
        }
        if(serverTask != null)
        {
            serverTask.stopTask();
        }
        TCPStreamBroadcaster This = this;
        serverTask = new StoppableTask() {
           
            @Override
            protected void on_stopTask() {
                server.stop();
            }

            @Override
            public void runTask() {
                server = new WebServer(port,() -> {
                    return new BroadcasterWrapper(handler,This,img_width,img_height); //To change body of generated lambdas, choose Tools | Templates.
                });
                server.run();
            }
        };
        serverTask.startTask();
    }
    
    
    @Override
    public void runTask() {
        try {
            PrintScreenTool print = new PrintScreenTool();
            transmission_height = (int)((float)transmission_width/print.aspect_ratio);
            startServer();
            
            encoderTask.startTask();
            
            init_stream(print.screen_width,print.screen_height);

            long last_measured = System.currentTimeMillis();
            long bytes_sent = 0;
            int sleep_time = (int)(1000.0f/framerate);
            System.out.println("sleep time:"+sleep_time);
            while (!Thread.currentThread().isInterrupted() && isRunning()) {
                long time_start = System.currentTimeMillis();
                long time_measure = time_start;
                
                BufferedImage hd_screen = print.printScreen();
                
                int elapsed_print = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                if(transmission_width > 0)
                    setPrint(ImageEdit.resize(hd_screen, transmission_width, transmission_height));
                else
                    setPrint(hd_screen);
                
                int elapsed_resize = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                
                // imagem é transformada e enviada para threads
                encoder_handler.sendMessage("PRINT");
                /*this.img_width = trasmission_screen.getWidth();
                this.img_height = trasmission_screen.getHeight();
                bytes_sent += encode(trasmission_screen);
                
                int elapsed_encode = (int)(System.currentTimeMillis() - time_measure);
                time_measure = System.currentTimeMillis();
                */
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
                
                if((time_start - last_measured) > 1000)
                {
                    System.out.println(
                        "print img:"+elapsed_print
                        +" resize img:"+elapsed_resize
                        +" load img:"+test_elapsed_load
                        +" encode:"+test_elapsed_encode);

                    //last_bandwith = (int)(TCPHilbertBroadcaster.encode_bytes_sent / ((time_start - last_measured)/1000));
                    //System.out.println(last_bandwith/1024 + " Kb/s");
                    last_measured = System.currentTimeMillis();
                    //HilbertBroadcaster.encode_bytes_sent = 0;
                    
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            serverTask.stopTask();
        }
    }
    
    public abstract void init_stream(int w,int h) throws InterruptedException, IOException;
    
    public abstract long encode(BufferedImage img) throws Exception;
}
