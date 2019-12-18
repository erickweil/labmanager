/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.erickweil.simplehttp;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class PrintScreenHttp {
     public static void main(String[] args) throws IOException, AWTException
    {
        ServerSocket server = new ServerSocket(80);
        while(true)
        {
            Socket client = server.accept();
            System.out.println("conectou:"+client.getInetAddress().getHostAddress());

            PrintStream writer = new PrintStream(client.getOutputStream());

            writer.println("HTTP/1.1 200 OK");
            writer.println("");
            
            int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
            float aspect_ratio = (float)screen_width / (float)screen_height;
            Robot robot = new Robot();
            Rectangle screen_rectangle = new Rectangle(screen_width, screen_height);
            BufferedImage screen = robot.createScreenCapture(screen_rectangle);
            
            ImageIO.write(screen, "bmp", writer);
            //muito importante
            writer.flush();

            writer.close();
            client.close();
        }
    }
}
