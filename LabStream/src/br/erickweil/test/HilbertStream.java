/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.test;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author Usuario
 */
public class HilbertStream extends HilbertImage {

    public HilbertStream(BufferedImage img, int factor) {
        super(img, factor);
    }
    
    

    private static int nextPowerOfTwo(int x)
    {
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }
    
    public HilbertStream(int width, int height, int factor) {
        super(nextPowerOfTwo(width), height, factor);
        this.width = width;
        hilbert_indexes = new int[width*height];
        int count = 0;
        System.out.println("x:"+width+" y:"+height);
        for (int index = 0; index < hilbert_x.length; index++) {
            int x = hilbert_x[index];
            int y = hilbert_y[index];

            if(y >= 0 && y < height && x < width && x >= 0)
            {
                
                hilbert_indexes[count] = y * width + x;
                count++;
                if(count >= hilbert_indexes.length) break;
            }
        }
    }
    //public int repeat_size = 4;
    protected int[] last_data;
    public int[] hilbert_indexes;
    
    public void resetLastData()
    {
        data = null;
        last_data = null;
    }    
    
    public void loadData(BufferedImage img)
    {
        last_data = data;
        Raster raster = img.getData();
        data = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[])null);
    }
    
    @Override
    public void encode(final HilbertEncoderListener listener) throws Exception {
        encode(listener, null);
    }

    public int encode(final HilbertEncoderListener listener,OutputStream output) throws Exception {
        //if (last_data == null) {
        //    super.encode(listener);
        //    return;
        //}
        //HilbertIterator it = new HilbertIterator(new AcessListener() { 
        long mean_r = -1;
        long mean_g = -1;
        long mean_b = -1;

        int last_count = 0;
        int start_index = 0;
        int writes = 0;
        //boolean was_stream_equal = false;
        //    @Override
        //    public void visit(int index,int x,int y) throws IOException, Exception {
        //for (int index = 0; index < hilbert_x.length; index++) {
        //    int x = hilbert_x[index];
        //    int y = hilbert_y[index];

        //    int pi = y * width + x;
          for(int index =0; index < hilbert_indexes.length;index++)
          {
            // data
            //int real_pr = 0;
            //int real_pg = 0;
            //int real_pb = 0;
            
            // new
            int npr = 0;
            int npg = 0;
            int npb = 0;

            // old
            //int opr = 0;
            //int opg = 0;
            //int opb = 0;

            // diff
            //int drm = 0;
            //int dpr = 0;
            //int dpg = 0;
            //int dpb = 0;
            //int stream_diff = 0;
            //boolean stream_equal = false;
            int pi3 = hilbert_indexes[index]*3;
            
            npr = data[pi3 + 0];
            npg = data[pi3 + 1];
            npb = data[pi3 + 2];
            //last_data = null;   
            if (last_data != null) {
                
                int opr = last_data[pi3 + 0];
                int opg = last_data[pi3 + 1];
                int opb = last_data[pi3 + 2];

                //int drm = (npr + opr) / 2;
                int dpr = npr - opr;
                int dpg = npg - opg;
                int dpb = npb - opb;
                int stream_diff = dpr * dpr + dpg * dpg + dpb * dpb;
                //int stream_diff = (((512+drm)*dpr*dpr)>>8) + 4*dpg*dpg + (((767-drm)*dpb*dpb)>>8);
                //stream_diff = stream_diff * stream_diff;
                //stream_diff = (Math.abs(dpr) + Math.abs(dpg) + Math.abs(dpb)) / 3;
                //stream_diff = stream_diff * stream_diff;
                //After which you can just find the average color difference in percentage.
                //System.out.println(stream_diff);
                if(stream_diff < factor)
                {
                    npr = -255;
                    npg = -255;
                    npb = -255;
                //    stream_equal = true;
                //    was_stream_equal = true;
                }
                /*else if(npr <= 5 && npg <= 2 && npb <= 5) // alguma coisa a ver com preto
                {
                    npr+= 3;
                    npg+= 2;
                    npb+= 3;
                    if(npr <= 5 && npg <= 2 && npb <= 5) // alguma coisa a ver com preto
                    {
                        npr+= 4;
                        npg+= 2;
                        npb+= 4;
                    }
                }*/
                
                
            }
            else
            {
               /*if(npr <= 5 && npg <= 2 && npb <= 5) // alguma coisa a ver com preto
               {
                    npr+= 3;
                    npg+= 2;
                    npb+= 3;
                    if(npr <= 5 && npg <= 2 && npb <= 5) // alguma coisa a ver com preto
                    {
                        npr+= 4;
                        npg+= 2;
                        npb+= 4;
                    }
               }*/
            }
            
            /*if(stream_equal || was_stream_equal)
            {
                
                if (!stream_equal || last_count == 255 || (index >= hilbert_x.length - 1)) {
                    listener.visit(start_index, last_count, 0);
                    mean_r = npr;
                    mean_g = npg;
                    mean_b = npb;
                    last_count = 0;
                    start_index = index;
                }
                else
                {
                    last_count++;
                }
                
                was_stream_equal = stream_equal;
                continue;
            }
            was_stream_equal = stream_equal;
            */

            if (mean_r == -1) { // se é o primeiro loop, pega o pixel
                mean_r = 0;
                mean_g = 0;
                mean_b = 0;
                
                mean_r += npr;
                mean_g += npg;
                mean_b += npb;
                last_count = 0;
                //System.out.print("FIRST: "+index+": \n");
            } else {

                int lpr = (int) (mean_r / (last_count + 1));
                int lpg = (int) (mean_g / (last_count + 1));
                int lpb = (int) (mean_b / (last_count + 1));

                //int mr = (npr + lpr)/2;
                int dist_r = npr - lpr;
                int dist_g = npg - lpg;
                int dist_b = npb - lpb;
                int dist = dist_r * dist_r + dist_g * dist_g + dist_b * dist_b;
                //int dist = (((512+mr)*dist_r*dist_r)>>8) + 4*dist_g*dist_g + (((767-mr)*dist_b*dist_b)>>8);
                //dist =dist*dist;
                if (dist >= factor || last_count == 65535 || (index >= hilbert_indexes.length - 1)) {
                            //out.write(last_count);

                            // 8-bit truecolor
                    //Bit    7  6  5  4  3  2  1  0
                    //Data   R  R  R  G  G  G  B  B
                    
                            // 16-bit truecolor
                    //Bit    15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 
                    //Data   R  R  R  R  R  G  G  G  G  G  G  B  B  B  B  B
                    
                    int pixel;
                    //int pixel = Math.round((float) lpr / 36.0f) << 5 | Math.round((float) lpg / 36.0f) << 2 | Math.round((float) lpb / 85.0f);
                    if(lpr < 0 || lpg < 0 || lpb < 0)
                    {
                        pixel = 0;
                    }
                    else
                    {
                        pixel = Math.round((float) lpr / 8.225f) << 11 | Math.round((float) lpg / 4.04f) << 5 | Math.round((float) lpb / 8.225f);
                        if( pixel == 0)
                        {
                            pixel = 0b0000000000100000;
                        }
                    }
                    //if(pixel != 0)
                    //{
                    //    lpr = (int)(((float)index / (float)hilbert_indexes.length)*255.0f);
                    //    lpg = (int)(((float)index / (float)hilbert_indexes.length)*255.0f);
                    //    lpb = (int)(((float)index / (float)hilbert_indexes.length)*255.0f);
                    //    pixel = Math.round((float) lpr / 36.0f) << 5 | Math.round((float) lpg / 36.0f) << 2 | Math.round(lpb / 85.0f);
                       //pixel = new Random().nextInt(65535);
                    //}
                    //if(pixel == 0)
                    //{
                    //    pixel = 0b00100000;
                    //}
                    //out.write(pixel);
                    
                    
                    //System.out.print(index+": ");
                    //if(repeat_size > 1)
                    //{
                    //    int rep_last_count = last_count / repeat_size;
                    //    if(last_count % repeat_size > 0)
                    //    {
                    //        rep_last_count++;
                        //    index += last_count % repeat_size -1;
                    //    }
                    //    index = start_index + rep_last_count*2 + 1;
                    //    listener.visit(start_index, rep_last_count, pixel);
                    //}
                    //else
                    if(listener != null)
                    {
                        listener.visit(start_index, last_count, pixel);
                        writes++;
                    }
                    else
                    {
                        if(last_count < 255)
                        {
                            output.write(last_count);
                        }
                        else
                        {
                            output.write(255);
                            output.write(last_count&0xFF);
                            output.write((last_count>>8)&0xFF);
                        }
                        output.write(pixel&0xFF);
                        output.write((pixel>>8)&0xFF);
                        
                        writes++;
                    }
                        

                    mean_r = npr;
                    mean_g = npg;
                    mean_b = npb;

                    last_count = 0;
                    start_index = index;

                    //if (index >= hilbert_x.length - 1) {
                        //System.out.println("mean lenght:" + (float) mlenght / (float) mlength_count);
                    //}
                } else {
                    //System.out.print(index+": \n");
                    mean_r += npr;
                    mean_g += npg;
                    mean_b += npb;
                    last_count++;
                }

            }
        }
       // });

        //it.A(depth);
        return writes;
    }

    @Override
    public int decode(final InputStream in, int start_index, int max_reads) throws Exception {
        int n_reads = 0;
        int index = start_index;
        while( index < hilbert_indexes.length) {

            if (max_reads != -1 && n_reads + 1 > max_reads) {
                return index;
            }
            final int p_length = in.read();
            if(p_length == -1) return index;
            final int length;
            if(p_length == 255)
            {
                length = (in.read() & 0xFF | (in.read() << 8) & 0xFF00) + 1;
            }
            else
            {
                length = p_length + 1;
            }
                //if(length == -1) throw new EOFException();

            n_reads++;

            
            int pixel = in.read() & 0xFF | (in.read() << 8) & 0xFF00;
            
            //System.out.println(index+":"+(length-1)+":"+pixel);
            if(pixel != 0)
            {
                // 8-bit truecolor
                //Bit    7  6  5  4  3  2  1  0
                //Data   R  R  R  G  G  G  B  B
                //int pixel = in.read();

                //if(pixel == 0b00100000)
                //    pixel = 0;
                //final int lpr = ((pixel & 0b11100000) >> 5) * 36;
                //final int lpg = ((pixel & 0b00011100) >> 2) * 36;
                //final int lpb = (pixel & 0b00000011) * 85;
                
                    // 16-bit truecolor
                //Bit    15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 
                //Data   R  R  R  R  R  G  G  G  G  G  G  B  B  B  B  B
                
                
                
                final int lpr = (int)(((pixel & 0b1111100000000000) >> 11) * 8.225f);
                final int lpg = (int)(((pixel & 0b0000011111100000) >> 5) * 4.04f);
                final int lpb = (int)((pixel &  0b0000000000011111) * 8.225f);
                
                //final int api = hilbert_y[index == 0? 0 : index-1] * width + hilbert_x[index == 0? 0 : index-1];
                //final int apr = data[api * 3 + 0];
                //final int apg = data[api * 3 + 1];
                //final int apb = data[api * 3 + 2];

                for(int i=0;i<length && index+i < hilbert_indexes.length;i++)
                {
                    //final int x = hilbert_x[index+i];
                    //final int y = hilbert_y[index+i];
                    //System.out.println(x+":"+y);
                    //final int pi = y * width + x;
                    final int pi3 = hilbert_indexes[index+i]*3;

                    //if (y < height) {
                    data[pi3 + 0] = lpr;
                    data[pi3 + 1] = lpg;
                    data[pi3 + 2] = lpb;
                    //}

                }

            }
            index += length;
            
            
                
        }
        
        //System.out.println("nreads:"+n_reads);
        return -1;
        //it.A(depth);

        //return getImageFromArray(result, width, height);
    }
    
    public int decode2(final InputStream in, int start_index, int max_reads) throws Exception {
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

        int n_reads = 0;
        for (int index = start_index; index < hilbert_x.length; index++) {
            //@Override
            //public void visit(int index,int x,int y) throws IOException {
            //if(index < start_index-1) return;
            int x = hilbert_x[index];
            int y = hilbert_y[index];
            //System.out.println(x+":"+y);
            int pi = y * width + x;
            if (last_count > length || length == -1) {
                if (max_reads != -1 && n_reads + 1 > max_reads) {
                    return index;
                }
                length = in.read();
                    //if(length == -1) throw new EOFException();
                // 8-bit truecolor
                //Bit    7  6  5  4  3  2  1  0
                //Data   R  R  R  G  G  G  B  B
                int pixel = in.read();
                n_reads++;

                
                lpr = ((pixel & 0b11100000) >> 5) * 36;
                lpg = ((pixel & 0b00011100) >> 2) * 36;
                lpb = (pixel & 0b00000011) * 85;
                
                

                last_count = 0;
            }

            
            if (y < height) {
                if(!(lpr == 0 && lpg == 0 && lpb == 0))
                {
                    data[pi * 3 + 0] = lpr;
                    data[pi * 3 + 1] = lpg;
                    data[pi * 3 + 2] = lpb;
                }
            }
            last_count++;
            //}
            //});
        }
        
        System.out.println("nreads:"+n_reads);
        return -1;
        //it.A(depth);

        //return getImageFromArray(result, width, height);
    }
    
    public static void main(String[] args) {
    for ( int i = 1 ; i < 10000 ; i+=100 ) {
        System.out.println(i+":"+nextPowerOfTwo(i));
    }
}
     /*public static void main(String[] args) throws Exception
    {
        BufferedImage test = ImageIO.read(new File("test.png"));
        long time_start = System.currentTimeMillis();
        HilbertImage img = new HilbertStream(test,100);
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
            
        BufferedImage test2 = ImageIO.read(new File("test2.png"));
        img.loadData(test2);
        File encoded_file2 = new File("test_encoded2.hilb");
        try (BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream(encoded_file2))) {
            img.encode(new HilbertEncoderListener() {

                @Override
                public void visit(int pixelIndex, int length, int pixel) throws Exception {
                    out.write(length);
                    out.write(pixel);
                }
            });
        }
        System.out.println("encoding2: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
            
            
        BufferedImage resimg = null;
        HilbertStream newhilb = new HilbertStream(test.getWidth(),test.getHeight(),400);
        try (BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file))) {
            newhilb.decode(in,0,-1);
        }
        try (BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file2))) {
            newhilb.decode(in,0,-1);
        }
        
        /*try (BufferedInputStream in = new BufferedInputStream( new FileInputStream(encoded_file))) {
            newhilb.decode(in,10000);
        }
        
        resimg = newhilb.getImage();
        
        System.out.println("decoding: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
        File outputfile = new File("test_decoded.png");
            ImageIO.write(resimg, "png", outputfile);
        System.out.println("saving: "+(System.currentTimeMillis()-time_start));
            time_start = System.currentTimeMillis();
        
    }*/

}
