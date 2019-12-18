/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.erickweil.test;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class HilbertImage {
    
    public static abstract class AcessListener{
        public abstract void visit(int index,int x,int y)throws Exception;
    }
    
    public static abstract class HilbertEncoderListener{
        public abstract void visit(int pixelIndex,int length,int pixel)throws Exception;
    }
    
    public static class HilbertIterator{
        private int rot;
        private int dx;
        private int dy;
        private int x;
        private int y;
        private int inc;
        private final AcessListener listener;
        public HilbertIterator(AcessListener listener)
        {
            this.rot = 0;
            this.dx = 1;
            this.dy = 0;
            this.x = 0;
            this.y = 0;
            this.inc = 0;
            this.listener = listener;
        }
		
	public void A(int depth) throws Exception
        {
	//  -BF+AFA+FB-
            //if (depth < 0) return;
            final int newdepth = depth-1;
            final boolean keepgoing = newdepth >= 0;
            this.rm();
            if(keepgoing) this.B(newdepth);
            this.F();
            this.rp();
            if(keepgoing) this.A(newdepth);
            this.F();
            if(keepgoing) this.A(newdepth);
            this.rp();
            this.F();
            if(keepgoing) this.B(newdepth);
            this.rm();
        }
	private void B(int depth) throws Exception
        {
    //  +AF-BFB-FA+
            //if(depth < 0) return;
            final int newdepth = depth-1;
            final boolean keepgoing = newdepth >= 0;
            this.rp();
            if(keepgoing) this.A(newdepth);
            this.F();
            this.rm();
            if(keepgoing) this.B(newdepth);
            this.F();
            if(keepgoing) this.B(newdepth);
            this.rm();
            this.F();
            if(keepgoing) this.A(newdepth);
            this.rp();
        }
	
	private void rp()
        {
            int newx = this.dy;
            int newy = -this.dx;

            this.dx = newx;
            this.dy = newy;
        }
	private void rm()
        {
            int newx = -this.dy;
            int newy = this.dx;

            this.dx = newx;
            this.dy = newy;
        }
	private void F() throws Exception
        {
            //remap(self.inc,(self.x,self.y))
            //put_sample(self.inc,(self.x,self.y))
            listener.visit(this.inc,this.x,this.y);

            this.x += this.dx;
            this.y += this.dy;
            this.inc += 1;
        }
    }
    
    protected int[] data;
    protected int depth;
    public int width;
    public int height;
    public int factor;
    protected final int[] hilbert_x;
    protected final int[] hilbert_y;
    public HilbertImage(BufferedImage img,int factor)
    {
        this(img.getWidth(),img.getHeight(),factor);
        Raster raster = img.getData();
        data = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), data);
    }
    
    public void loadData(BufferedImage img)
    {
        Raster raster = img.getData();
        data = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), data);
    }
    
    public HilbertImage(int width, int height,int factor)
    {
        this.width = width;
        this.height = height;
        if(width <= 0 || !((width & (width - 1)) == 0))
        {
            throw new UnsupportedOperationException("Image must be Hilbert compatible! i.e: a square power of 2:"+width);
        }
        
        this.depth = (32 - Integer.numberOfLeadingZeros(width - 1))-1;
        
        data = new int[width*width*3];
        //data = img.getSampleModel().getPixels(0, 0, width, width, (int[])null, img.getData().getDataBuffer());

        //data = raster.getPixels(0,0,width,width,data);
        System.out.println("Depth:"+depth+" width:"+width+" pixels:"+data.length/3);
        this.factor = factor;
        
        
        hilbert_x = new int[width*width];
        hilbert_y = new int[width*width];
        HilbertIterator it = new HilbertIterator(new AcessListener() {
             @Override
            public void visit(int index,int x,int y) throws IOException {
                hilbert_x[index] = x;
                hilbert_y[index] = y;
            }
        });
        try {
            it.A(depth);
        } catch (Exception ex) {
            Logger.getLogger(HilbertImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void encode(final HilbertEncoderListener listener) throws Exception
    {
        //HilbertIterator it = new HilbertIterator(new AcessListener() { 
            long mpr = -1;
            long mpg = -1;
            long mpb = -1;
            int last_count = 0;
            int mlenght = 0;
            int mlength_count = 0;
        //    @Override
        //    public void visit(int index,int x,int y) throws IOException, Exception {
        for(int index=0;index<hilbert_x.length;index++)
        { 
                int x = hilbert_x[index];
                int y = hilbert_y[index];
                
                int pi = y*width + x;
                int pr = 0;
                int pg = 0;
                int pb = 0;
                if(y < height)
                {
                    pr = data[pi*3 + 0];///8388607;
                    pg = data[pi*3 + 1];///8388607;
                    pb = data[pi*3 + 2];///8388607;
                }
                
                if(mpr == -1)
                {
                    mpr = pr;
                    mpg = pg;
                    mpb = pb;
                    last_count = 0;
                }
                else
                {
                    int lpr = (int)(mpr/(last_count+1));
                    int lpg = (int)(mpg/(last_count+1));
                    int lpb = (int)(mpb/(last_count+1));
                    
                    int dist_r = pr - lpr;
                    int dist_g = pg - lpg;
                    int dist_b = pb - lpb;
                    int dist = dist_r*dist_r + dist_g*dist_g + dist_b*dist_b;
                    if(dist >= factor || last_count == 255 || (index >= hilbert_x.length-1))
                    {
                        //out.write(last_count);
                        
                        // 8-bit truecolor
                        //Bit    7  6  5  4  3  2  1  0
                        //Data   R  R  R  G  G  G  B  B
                        
                        int pixel = Math.round((float)lpr/36.0f)<<5 | Math.round((float)lpg/36.0f)<<2 | Math.round(lpb/85.0f);
                        //out.write(pixel);
                        
                        listener.visit(index,last_count,pixel);

                        mpr = pr;
                        mpg = pg;
                        mpb = pb;

                        mlenght += last_count;
                        mlength_count++;
                         
                        last_count = 0; 

                        //if(index >= hilbert_x.length-1)
                        //{
                        //    System.out.println("mean lenght:"+(float)mlenght/(float)mlength_count);
                        //} 
                    }
                    else
                    {
                        mpr += pr;
                        mpg += pg;
                        mpb += pb;
                        last_count++;
                    }
                }
        }
       // });
    
        //it.A(depth);
    }
    
    public int decode(final InputStream in,int start_index,int max_reads) throws Exception
    {
        //final int width = in.read()&0xFF | (in.read()<<8)&0xFF00;
        //final int height = in.read()&0xFF | (in.read()<<8)&0xFF00;
        //int depth = (32 - Integer.numberOfLeadingZeros(width - 1))-1;
        
        //System.out.println("Depth:"+depth+" width:"+width+" pixels:"+data.length/3);
        //HilbertIterator it = new HilbertIterator(new AcessListener() {
                    
        int lpr = -1;
        int lpg = -1;
        int lpb = -1;

        int last_count = 0;
        int length = -1;
        
        int n_reads= 0;
        for(int index=start_index;index<hilbert_x.length;index++)
        {            
            //@Override
            //public void visit(int index,int x,int y) throws IOException {
                //if(index < start_index-1) return;
                int x = hilbert_x[index];
                int y = hilbert_y[index];
                //System.out.println(x+":"+y);
                int pi = y*width + x;
                if(last_count > length || length == -1)
                {
                    if(max_reads != -1 && n_reads+1 > max_reads)
                    {
                        return index;
                    }
                    length = in.read();
                    //if(length == -1) throw new EOFException();
                    // 8-bit truecolor
                    //Bit    7  6  5  4  3  2  1  0
                    //Data   R  R  R  G  G  G  B  B
                    int pixel = in.read();
                    n_reads++;
                    lpr = ((pixel&0b11100000)>>5)*36;
                    lpg = ((pixel&0b00011100)>>2)*36;
                    lpb = (pixel&0b00000011)*85;

                    last_count = 0;
                }
                
                if(y<height)
                {
                    data[pi*3 + 0] = lpr;
                    data[pi*3 + 1] = lpg;
                    data[pi*3 + 2] = lpb;
                }
                last_count++;
            //}
        //});
        }
        return -1;
        //it.A(depth);
        
        //return getImageFromArray(result, width, height);
    }
    
    public BufferedImage getImage() {
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster writable_raster = newimage.getRaster();
        writable_raster.setPixels(0, 0, width, height, data);
        return newimage;
    }
    
    public BufferedImage getImage(BufferedImage old) {
        if(old == null) return getImage();
        WritableRaster writable_raster = old.getRaster();
        writable_raster.setPixels(0, 0, width, height, data);
        return old;
    }
    
    public static void main(String[] args) throws Exception
    {
        BufferedImage test = ImageIO.read(new File("test.png"));
        long time_start = System.currentTimeMillis();
        HilbertImage img = new HilbertImage(test,1000);
        File encoded_file = new File("test_encoded.hilb");
        try (BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream(encoded_file))) {
            img.encode(new HilbertEncoderListener() {

                @Override
                public void visit(int pixelIndex, int length, int pixel) throws Exception {
                    out.write(length);
                    out.write(pixel);
                }
            });
        }
        System.out.println("encoding: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
            
        BufferedImage resimg = null;
        HilbertImage newhilb = new HilbertImage(test.getWidth(),test.getHeight(),400);
        //BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file));
        //in.read(new byte[10000]);
        try (BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file))) {
             int i = newhilb.decode(in,0,500);
             i = newhilb.decode(in,i,500);
             newhilb.decode(in,i,-1);
        }
        
        /*try (BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file))) {
            newhilb.decode(in,10000);
        }*/
        
        resimg = newhilb.getImage();
        
        System.out.println("decoding: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
        File outputfile = new File("test_decoded.png");
            ImageIO.write(resimg, "png", outputfile);
        System.out.println("saving: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
        
    }
    
}
