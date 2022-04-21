/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import br.erickweil.streaming.StreamBroadcaster.Broadcast_Mode;
import br.erickweil.streaming.broadcasters.HilbertBroadcaster;
import br.erickweil.streaming.broadcasters.JpegBroadcaster;
import br.erickweil.streaming.tcpwrapper.TCPHilbertBroadcaster;
import br.erickweil.streaming.tcpwrapper.TCPStreamBroadcaster;
import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, AWTException, IOException
    {
        //PrintScreenTool tool = new PrintScreenTool();
        //BufferedImage img = tool.printScreen();
        //ImageIO.write(img, "png", new File("test.png"));
        TCPStreamBroadcaster streamer = new TCPHilbertBroadcaster(9876, 10, 1280,80,350);

        System.out.println(streamer.max_bandwith);
        //StreamBroadcaster streamer = new HilbertBroadcaster(args.length == 2 ? args[1] : "10.115.254.191", 9876, 10, -1,120,-1);
        streamer.startTask();
        //Thread.sleep(5000);
        //streamer.stopTask();
    }
}
