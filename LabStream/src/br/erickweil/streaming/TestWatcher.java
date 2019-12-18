/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import br.erickweil.streaming.broadcasters.HilbertBroadcaster;
import br.erickweil.streaming.tcpwrapper.TCPHilbertWatcher;
import br.erickweil.streaming.tcpwrapper.TCPStreamWatcher;
import br.erickweil.streaming.watchers.HilbertWatcher;
import br.erickweil.test.StreamingWindow;
import java.awt.image.BufferedImage;

/**
 *
 * @author Usuario
 */
public class TestWatcher {
    public static void main(String[] args)
    {
        StreamingWindow window = new StreamingWindow();
        window.setVisible(true);
        //StreamWatcher streamer = new HilbertWatcher("230.0.0.0", 9876, window);
        //streamer.startTask();
        TCPStreamWatcher streamer = new TCPHilbertWatcher("192.168.2.201", 9876, window);
        streamer.startTask();
    } 
}
