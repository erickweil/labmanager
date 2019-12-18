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
package br.erickweil.labmanager.tests;

import br.erickweil.labmanager.client.GaussianFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ScreenLockerTest extends JFrame {

    private GraphicsDevice device;
    private boolean isFullScreen = false;
    AltTabStopper alttab;
    Thread alttab_thread;
    private BufferedImage myImage;
    private final JLabel label;

    public static void splash(final long time)
    {
        new Thread( new Runnable()
        {
        @Override
        public void run()
        {
            ScreenLockerTest test = null;
            try {
                test = ScreenLockerTest.create(true);
                test.startLocking();
                Thread.sleep(time);
            }
             catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            finally{
                if(test!= null)
                {
                    test.stop();
                }
            }
        }
        }).start();
    }
    
    public static ScreenLockerTest create(boolean splash)
    {
        GraphicsEnvironment env = GraphicsEnvironment.
        getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        return new ScreenLockerTest(devices[0],splash);
    }
    
    public ScreenLockerTest(GraphicsDevice device,boolean splash) {
        super(device.getDefaultConfiguration());
        this.device = device;
        setTitle("Screen Locaker");
        label = new JLabel("Atenção");
        try {

            int py = Toolkit.getDefaultToolkit().getScreenSize().width;
            int px = Toolkit.getDefaultToolkit().getScreenSize().height;
            Robot robot = new Robot();
            BufferedImage im = robot.createScreenCapture(new Rectangle(py, px));
            GaussianFilter f = new GaussianFilter(10.0f);
            BufferedImage dst = deepCopy(im);
            
            
            BufferedImage result = f.filter(im, dst);
            if(splash)
            {
                BufferedImage logo = ImageIO.read(new File("splash.png"));
                BufferedImage c = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_ARGB);

                int corner_x = (result.getWidth()/2) - logo.getWidth()/2;
                int corner_y = (result.getHeight()/2) - logo.getHeight()/2;

                Graphics g = c.getGraphics();
                g.drawImage(result, 0, 0, null);
                g.drawImage(logo, corner_x, corner_y, null);

                myImage = c;
            }
            else
            {
                myImage = result;
            }
            //myImage =ImageIO.read(new File("icone.png"));
            //} catch (IOException ex) {
            //    Logger.getLogger(ScreenLocker.class.getName()).log(Level.SEVERE, null, ex);

            ImageIcon icon = new ImageIcon(myImage);
            label.setIcon(icon);
            this.add(label);
            //setContentPane(new ImagePanel(myImage));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //label = new JLabel("Atenção");
        //Font labelFont = label.getFont();
        //label.setFont(new Font(labelFont.getName(), Font.PLAIN, 50));
        //label.setHorizontalAlignment(JLabel.CENTER);
        //int gap = 5;
        //setLayout(new BorderLayout(gap, gap));
        //add(label, BorderLayout.CENTER);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setAutoRequestFocus(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        alttab = AltTabStopper.create(this);
    }
    
    public void startLocking()
    {
        try {
            begin();
            initComponents(getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    private void initComponents(Container c) {
        setContentPane(c);
    }

    public void begin() {
        isFullScreen = device.isFullScreenSupported();
        setUndecorated(isFullScreen);
        setResizable(!isFullScreen);
        focus();
    }

    public void stop() {
        try
        {
            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            //dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            synchronized(alttab)
            {
                alttab.stop();
            }
            Thread.sleep(10);
            boolean parou = false;
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

            dispose();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void focus() {

        //setState(MAXIMIZED_BOTH);
        //isFullScreen = device.isFullScreenSupported();
        //if (isFullScreen) {
        // Full-screen mode
        setVisible(true);
        device.setFullScreenWindow(this);
        this.requestFocus();
        //this.requestFocusInWindow();
        setExtendedState(getState() | MAXIMIZED_BOTH);

        validate();
        //} else {
        // Windowed mode
        //   pack();
        //    setVisible(true);
        //}
    }
    
    static BufferedImage deepCopy(BufferedImage bi) {
 ColorModel cm = bi.getColorModel();
 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
 WritableRaster raster = bi.copyData(null);
 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
}

    public static void main(String[] args) throws InterruptedException {
        splash(15000);
    }
    /*static class ImagePanel extends JComponent {
     private Image image;
     public ImagePanel(Image image) {
     super();
     this.image = image;
     }
     @Override
     protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     g.drawImage(image, 0, 0, this);
     }
     }*/
    static class AltTabStopper implements Runnable {

        private volatile boolean working = true;
        private volatile boolean hastopped = false;
        public Thread thread;
        private ScreenLockerTest frame;

        public AltTabStopper(ScreenLockerTest frame) {
            this.frame = frame;
        }

        public synchronized void stop() {
            working = false;
        }

        public synchronized boolean hasStopped() {
            return hastopped;
        }
        
        public static AltTabStopper create(ScreenLockerTest frame) {
            AltTabStopper stopper = new AltTabStopper(frame);
            stopper.thread = new Thread(stopper, "Alt-Tab Stopper");
            stopper.thread.start();
            return stopper;
        }

        @Override
        public void run() {
            try {
                Robot robot = new Robot();
                while (working && !Thread.currentThread().isInterrupted()) {
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_DELETE);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_WINDOWS);
                    frame.focus();
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally
            {
                hastopped = true;
            }
        }
    }
}
