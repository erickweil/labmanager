/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.test;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class ImageMixer {
      // Implementing Fisher–Yates shuffle
  /*static void shuffleArray(int[][] ar)
  {
    // If running on Java 6 or older, use `new Random()` on RHS here
    Random rnd = ThreadLocalRandom.current();
    for (int i = ar.length - 1; i > 0; i--)
    {
      int index = rnd.nextInt(i + 1);
      // Simple swap
      int[] a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    }
  }*/
private static void shuffleArray(int[][] array)
{
    int index;
    int[] temp;
    Random random = new Random();
    for (int i = array.length - 1; i > 0; i--)
    {
        index = random.nextInt(i + 1);
        temp = array[index];
        array[index] = array[i];
        array[i] = temp;
    }
}
    public static void main(String[] args) throws IOException
    {
        BufferedImage test = ImageIO.read(new File("test.png"));

        Raster raster = test.getData();

        int width = raster.getWidth();
        int height = raster.getHeight();
        
        
        int[] pixels = raster.getPixels(0, 0, width, height, (int[]) null);
        
        int channels = (int)((float) pixels.length / (float) (width * height));
        
        int[] pixels_mixed = new int[pixels.length];
        
        System.out.println(width+","+height+" channels:"+channels);
        
        int[][] pixels_positions = new int[pixels.length/channels][];
        for(int y=0;y<height;y++)
        {
            for(int x=0;x<width;x++)
            {
                pixels_positions[y*width + x] = new int[2];
                pixels_positions[y*width + x][0] = x;
                pixels_positions[y*width + x][1] = y;
            }
        }
        shuffleArray(pixels_positions);
        //Collections.shuffle(Arrays.asList(pixels_positions)); 
        for(int y=0;y<height;y++)
        {
            for(int x=0;x<width;x++)
            {
                int nx = pixels_positions[y*width + x][0];
                int ny = pixels_positions[y*width + x][1];
                for(int c=0;c<channels;c++)
                {
                    pixels_mixed[(ny*width + nx)*channels + c] = pixels[(y*width + x)*channels + c];
                }
            }
        }
        
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster writable_raster = newimage.getRaster();
        writable_raster.setPixels(0, 0, width, height, pixels_mixed);

        ImageIO.write(newimage, "png", new File("test_mixed.png"));
    }
}
