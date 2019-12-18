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
package br.erickweil.labmanager.client;
/**
 * File: Sepia.java
 *
 * Description:
 * Convert color image to sepia image.
 *
 * @author Yusuf Shakeel
 * Date: 27-01-2014 mon
 *
 * www.github.com/yusufshakeel/Java-Image-Processing-Project
 */
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
public class Sepia{
    // lento que uma desgraça
  public static void filterSepia(BufferedImage img) {
    //get width and height of the image
    int width = img.getWidth();
    int height = img.getHeight();

    //convert to sepia
    for(int y = 0; y < height; y++){
      for(int x = 0; x < width; x++){
        int p = img.getRGB(x,y);

        int a = (p>>24)&0xff;
        int r = (p>>16)&0xff;
        int g = (p>>8)&0xff;
        int b = p&0xff;

        //calculate tr, tg, tb
        int tr = (int)(0.393*r + 0.769*g + 0.189*b);
        int tg = (int)(0.349*r + 0.686*g + 0.168*b);
        int tb = (int)(0.272*r + 0.534*g + 0.131*b);

        //check condition
        if(tr > 255){
          r = 255;
        }else{
          r = tr;
        }

        if(tg > 255){
          g = 255;
        }else{
          g = tg;
        }

        if(tb > 255){
          b = 255;
        }else{
          b = tb;
        }

        //set new RGB value
        p = (a<<24) | (r<<16) | (g<<8) | b;

        img.setRGB(x, y, p);
      }
    }
    
  }//main() ends here
}//class ends here
