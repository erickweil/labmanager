/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.test;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class TestImage {

    public static void main(String[] args) throws IOException {
        BufferedImage test = ImageIO.read(new File("test.png"));

        int width = test.getWidth();
        int height = test.getHeight();

        Raster raster = test.getData();

        int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);

        OutputStream outputWriter = null;
        outputWriter = new BufferedOutputStream(new FileOutputStream(new File("test.weil")));
        for(int i=0;i<pixels.length;i++)
            outputWriter.write(pixels[i]);
        outputWriter.close();
        
        int[] read_pixels = new int[pixels.length];
        
        BufferedInputStream outputreader = null;
        outputreader = new BufferedInputStream(new FileInputStream(new File("test.weil")));
         for(int i=0;i<read_pixels.length;i++)
            read_pixels[i] = outputreader.read()&0xFF;
        outputreader.close();
        
        System.out.println("number of pixels:" + width * height + " array:" + pixels.length);
        System.out.println("elements per pixel:" + ((float) pixels.length / (float) (width * height)));

        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster writable_raster = newimage.getRaster();
        writable_raster.setPixels(0, 0, width, height, read_pixels);

        ImageIO.write(newimage, "png", new File("test_encoded.png"));
    }
}
