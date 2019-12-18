/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.test;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author Usuario
 */
public class SurfaceBlur {

    BufferedImage imp;

    int[] pixels;
    int[] temp;

    int width, height, imagesize;

    int RADIUS;

    int DROP;

    int STRENGTH = 1;

    int THRESHOLD;

    int ITERATIONS;

    public SurfaceBlur(BufferedImage imp, int radius, int proximity, int threshold,int iterations) {
        this.imp = imp;
        this.RADIUS = radius;
        this.DROP = proximity;
        this.THRESHOLD = threshold;
        this.ITERATIONS = iterations;
    }

    public BufferedImage run() {
        //GenericDialog gd = new GenericDialog("Surface Blur");
        //gd.addNumericField("Radius", 5, 0);
        //gd.addNumericField("Proximity Weighting", 5, 0);
        //gd.addNumericField("Edge Threshhold", 100, 0);
        //gd.addNumericField("Iterations", 1, 0);
        //gd.showDialog();
        //if (gd.wasCanceled()) {
        //    IJ.error("PlugIn canceled!");
        //    return;
        //}
        //RADIUS = (int) gd.getNextNumber();
        //DROP = (int) gd.getNextNumber();
        //THRESHOLD = (int) gd.getNextNumber();
        //ITERATIONS = (int) gd.getNextNumber();
        //ColorProcessor cp = (ColorProcessor) ip;
        //if (imp == null) {
        //    IJ.error("No image!");
        //    return;
       // }
        width = imp.getWidth();
        height = imp.getHeight();
        imagesize = width * height;
        Raster raster = imp.getData();
        pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), pixels);
        //pixels = (int[]) cp.getPixels();
        temp = new int[pixels.length];
        for (int j = 0; j < ITERATIONS; j++) {
            //IJ.showStatus("Iteration " + (j + 1));
            for (int i = 0; i < pixels.length; i++) {
                discriminateBlur(i);
            //    IJ.showProgress(i, pixels.length);
            }
            System.arraycopy(temp, 0, pixels, 0, temp.length);
            //imp.updateAndRepaintWindow();
        }
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster writable_raster = newimage.getRaster();
        writable_raster.setPixels(0, 0, width, height, pixels);
        return newimage;
    }

    private void discriminateBlur(int i) {
        int R1, G1, B1, R2, G2, B2, Rd, Gd, Bd, dist, diff, pos = 0, offset = 0;
        R1 = (pixels[i] & 0xff0000) >> 16;
        G1 = (pixels[i] & 0x00ff00) >> 8;
        B1 = (pixels[i] & 0x0000ff);
        //scany:
        for (int y = -RADIUS; y <= RADIUS; y++) {
            offset = i + (y * width);
            if (offset < 0 || offset + RADIUS >= pixels.length) {
                continue;// scany;
            }
            //scanx:
            for (int x = -RADIUS; x <= RADIUS; x++) {
                pos = offset + x;
                if (pos < (((pos - x) / width)) * width
                        || pos > (((pos - x) / width) + 1) * width
                        || pos == i) {
                    continue;// scanx;
                }
                dist = Math.max(Math.abs(x), Math.abs(y));
                dist = dist * DROP;
                R2 = (pixels[pos] & 0xff0000) >> 16;
                G2 = (pixels[pos] & 0x00ff00) >> 8;
                B2 = (pixels[pos] & 0x0000ff);
                Rd = Math.abs(R1 - R2);
                Gd = Math.abs(G1 - G2);
                Bd = Math.abs(B1 - B2);
                diff = Rd * Rd + Gd * Gd + Bd * Bd;
                if (diff > THRESHOLD) {
                    continue;// scanx;
                }
                R1 = (R1 * dist + R2 * STRENGTH) / (dist + STRENGTH);
                G1 = (G1 * dist + G2 * STRENGTH) / (dist + STRENGTH);
                B1 = (B1 * dist + B2 * STRENGTH) / (dist + STRENGTH);
            }
        }
        temp[i] = ((R1 & 0xff) << 16) + ((G1 & 0xff) << 8) + (B1 & 0xff);
    }
}
