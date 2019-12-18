/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.streaming;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author Usuario
 */
public class ImageEdit {
    public static BufferedImage resize(BufferedImage img,int new_width,int new_height)
    {
        if(new_height < 0)
        {
            double aspect = (double)img.getWidth()/(double)img.getHeight();
            new_height = (int)(new_width / aspect);
        }
        BufferedImage new_img = new BufferedImage(new_width,new_height, img.getType());
        

            
        Graphics2D g = new_img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        
        g.drawImage( img, 0, 0, new_width, new_height, null);
        g.dispose();
        //BufferedImage new_img = (BufferedImage)img.getScaledInstance(new_width, new_height, Image.SCALE_SMOOTH);
        return new_img;
    }
}
