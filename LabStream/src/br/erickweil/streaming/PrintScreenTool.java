/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import br.erickweil.labmanager.cmd.ProgramOpener;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 *
 * @author Usuario
 */
public class PrintScreenTool {

    public final int screen_width;
    public final int screen_height;
    public final float aspect_ratio;
    private final Robot robot;
    public final Rectangle screen_rectangle;
    
    public PrintScreenTool() throws AWTException
    {
        screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        aspect_ratio = (float)screen_width / (float)screen_height;
        robot = new Robot();
        screen_rectangle = new Rectangle(screen_width, screen_height);
    }
    
    public BufferedImage printScreen()
    {
        long time_start = System.currentTimeMillis();
        BufferedImage hd_screen = robot.createScreenCapture(screen_rectangle);
        return hd_screen;
    }
    
    public byte[] jpeg_printScreen(float factor, int width)
    {
        try {
            //https://stackoverflow.com/questions/37713773/java-bufferedimage-jpg-compression-without-writing-to-file
            BufferedImage image = printScreen();
            if(width > 0 && image.getWidth() != width)
            {
                image = ImageEdit.resize(image, width, -1);
            }
            // The important part: Create in-memory stream
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            
            /*
            PS: By default, ImageIO will use disk caching when creating your ImageOutputStream.
            This may slow down your in-memory stream writing. To disable it, use ImageIO.setCache(false)
            (disables disk caching globally) or explicitly create an MemoryCacheImageOutputStream (local),
            like this:
            */
            ImageOutputStream outputStream = new MemoryCacheImageOutputStream(compressed);
            //ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
            
            // NOTE: The rest of the code is just a cleaned up version of your code
            
            // Obtain writer for JPEG format
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            
            // Configure JPEG compression: 70% quality
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(factor);
            
            // Set your in-memory stream as the output
            jpgWriter.setOutput(outputStream);
            
            // Write image as JPEG w/configured settings to the in-memory stream
            // (the IIOImage is just an aggregator object, allowing you to associate
            // thumbnails and metadata to the image, it "does" nothing)
            jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
            
            // Dispose the writer to free resources
            jpgWriter.dispose();
            
            // Get data for further processing...
            byte[] jpegData = compressed.toByteArray();
            return jpegData;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) throws AWTException, IOException, InterruptedException
    {
        for(int i =0; i<1000;i++)
        {
            byte[] jpeg_printScreen = new PrintScreenTool().jpeg_printScreen((float) 0.7, 256);
            Thread.sleep(10);
        }
        //Files.write(new File("ok.jpg").toPath(), jpeg_printScreen);
        //ProgramOpener.start(new File("ok.jpg"), null);
    }
}
