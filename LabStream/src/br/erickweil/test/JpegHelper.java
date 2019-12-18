/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.erickweil.test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author Usuario
 */
public class JpegHelper {
    public static byte[] compress_jpeg(BufferedImage image,float quality) throws IOException
    {
        // The important part: Create in-memory stream
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);

        // NOTE: The rest of the code is just a cleaned up version of your code

        // Obtain writer for JPEG format
        ImageIO.setUseCache(false);
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();

        // Configure JPEG compression: 70% quality
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

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
    }
    
    public static BufferedImage decompress_jpeg(byte[] data) throws IOException
    {
        // The important part: Create in-memory stream
        ByteArrayInputStream compressed = new ByteArrayInputStream(data);
        ImageInputStream inputStream = ImageIO.createImageInputStream(compressed);

        // NOTE: The rest of the code is just a cleaned up version of your code

        // Obtain writer for JPEG format
        ImageIO.setUseCache(false);
        ImageReader jpgReader = ImageIO.getImageReadersByFormatName("jpg").next();

        // Configure JPEG compression: 70% quality
        ImageReadParam jpgWriteParam = jpgReader.getDefaultReadParam();
        //jpgWriteParam.setDestination(data);
        
        // Set your in-memory stream as the output
        // Set your in-memory stream as the output
        jpgReader.setInput(inputStream);        

        // Write image as JPEG w/configured settings to the in-memory stream
        // (the IIOImage is just an aggregator object, allowing you to associate
        // thumbnails and metadata to the image, it "does" nothing)
        //jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
        BufferedImage result = jpgReader.read(0, jpgWriteParam);

        // Dispose the writer to free resources
        jpgReader.dispose();

        return result;
    }
}
