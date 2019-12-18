/*
 * Copyright (C) 2018 Erick Leonardo Weil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.erickweil.labmanager.client.swing;

import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.client.GaussianFilter;
import br.erickweil.labmanager.client.Sepia;
import br.erickweil.labmanager.tests.ScreenLockerTest;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

/**
 *
 * @author Usuario
 */
public class ScreenLocker extends JFrame {
    private GraphicsDevice device;
    private boolean isFullScreen = false;

    Thread alttab_thread;
    private final JLabel label;
    private BufferedImage logo;
    private AltTabStopper alttab;
    
    private boolean Locking = false;
    
    public static final boolean BLOCKALTTAB = true;
    
    public synchronized boolean isLocking()
    {
        return Locking;
    }
    
    public void startBroadcasting()
    {
        if(Locking) return;
        Locking = true;
        focus();
        if(BLOCKALTTAB)
        this.alttab = AltTabStopper.create(this);
    }
    
    public void changeBroadcastImg(BufferedImage hd_screen)
    {
        int transmission_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int transmission_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        int screen_width = hd_screen.getWidth();
        int screen_height = hd_screen.getHeight();
        float aspect_ratio = (float)screen_width / (float)screen_height;
        
        transmission_height = (int)((float)transmission_width/aspect_ratio);
        
        BufferedImage low_screen = new BufferedImage(transmission_width,transmission_height, BufferedImage.TYPE_INT_RGB);
            Graphics g = low_screen.createGraphics();
            g.drawImage( hd_screen, 0, 0, transmission_width, transmission_height, null);
            g.dispose();
        
        ImageIcon icon = new ImageIcon(low_screen);
        this.label.setIcon(icon);
    }
    
    public void startLocking(boolean splash)
    {
        if(Locking) return;
        Locking = true;
        BufferedImage screenShot = takeScreenShot(splash);
        if(screenShot != null)
        {
            ImageIcon icon = new ImageIcon(screenShot);
            this.label.setIcon(icon);
        }
        focus();
        if(BLOCKALTTAB)
        this.alttab = AltTabStopper.create(this);
    }
    
    public void stopLocking() throws InterruptedException
    {
        if(!Locking) return;
        Locking = false;
        
        try
        {
            Thread.sleep(10);
            boolean parou = false;
            if(BLOCKALTTAB)
            {
                synchronized(alttab)
                {
                    parou = alttab.hasStopped();
                }


                if(!parou)
                {
                    System.out.println("Teve que dar o interrupt no ScreenLocker");
                    alttab.thread.interrupt();
                    Thread.sleep(10);
                    if(!alttab.hasStopped())
                    {
                        System.out.println("Teve que dar o stop no ScreenLocker");
                        alttab.thread.stop();
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        this.dispose();
        //this.setVisible(false);
        this.device.setFullScreenWindow(null);
    }
    
    public ScreenLocker()
    {
        //GraphicsEnvironment env = GraphicsEnvironment.
        //getLocalGraphicsEnvironment();
        //GraphicsDevice[] devices = env.getScreenDevices();
        this(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0]);
    }
    
    private ScreenLocker(GraphicsDevice device) {
        super(device.getDefaultConfiguration());
        this.device = device;
        this.setTitle("Screen Locker");
        this.label = new JLabel("Atenção");
        try {
            this.logo = ImageIO.read(new File("splash.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //BufferedImage screenShot = takeScreenShot(true);
        //if(screenShot != null)
        //{
        //    ImageIcon icon = new ImageIcon(screenShot);
        //    this.label.setIcon(icon);
        //}
        this.add(label);
        
        if(!ClientMain._testing){
            this.setAlwaysOnTop(true);
            this.setAutoRequestFocus(true);
        }
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        
        //isFullScreen = device.isFullScreenSupported();
        //setUndecorated(isFullScreen);
        //setResizable(!isFullScreen);
        setUndecorated(true);
        setResizable(false);
        
        setContentPane(getContentPane());
        
        Locking = false;
        this.setVisible(false);
    }
    
    public void focus() {
        
        this.setVisible(true);
        
        this.requestFocus();
        this.setExtendedState(getState() | MAXIMIZED_BOTH);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screen_width = screenSize.width;
        int screen_height = screenSize.height;
        this.setLocation(0, 0);
        this.setSize(screen_width, screen_height);
        
        this.validate();
        //this.device.setFullScreenWindow(this);
    }
    
    // TODO: find the memory leak
    private final BufferedImage takeScreenShot(boolean splash)
    {
        try {

            int py = Toolkit.getDefaultToolkit().getScreenSize().width;
            int px = Toolkit.getDefaultToolkit().getScreenSize().height;
            Robot robot = new Robot();
            BufferedImage im = robot.createScreenCapture(new Rectangle(py, px));
            //GaussianFilter f = new GaussianFilter(10.0f);
            //BufferedImage dst = deepCopy(im);
            
            Sepia.filterSepia(im);

            //BufferedImage result = f.filter(im, dst);
            BufferedImage result = im;
            BufferedImage myImage;
            if(splash && logo != null)
            {
                BufferedImage c = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_ARGB);

                int corner_x = (result.getWidth()/2) - logo.getWidth()/2;
                int corner_y = (result.getHeight()/2) - logo.getHeight()/2;

                Graphics g = c.getGraphics();
                
                g.drawImage(result, 0, 0, null);
                g.drawImage(logo, corner_x, corner_y, null);
                
                g.dispose();
                myImage = c;
            }
            else
            {
                myImage = result;
            }

            return myImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    
    public static void splash(final long time)
    {
        new Thread( new Runnable()
        {
        @Override
        public void run()
        {
            ScreenLocker test = null;
            try {
                test = new ScreenLocker();
                test.startLocking(true);
                Thread.sleep(time);
            }
             catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            finally{
                if(test!= null)
                {
                    try {
                        test.stopLocking();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        }).start();
    }
    
}
