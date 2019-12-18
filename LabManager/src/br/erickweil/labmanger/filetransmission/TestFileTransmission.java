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
package br.erickweil.labmanger.filetransmission;

import br.erickweil.labamanger.common.WeilUtils;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebClient;
import br.erickweil.webserver.WebServer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Usuario
 */
public class TestFileTransmission {
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException
    {
        final File filesrc = new File("D:\\utorrent\\Windows_7_todas.as.versoes_x86_ou_x64_pt-BR.iso");
        //final File filesrc = new File("D:\\utorrent\\gimp-2.8.22-setup.exe");
        
        long millis_start = System.currentTimeMillis();
        System.out.println(WeilUtils.hashFile(filesrc,"MD5"));
        
        int elapsed = (int)(System.currentTimeMillis()-millis_start);
        System.out.println("Elapsed:"+elapsed);
        System.out.println("Speed:"+(((double)filesrc.length()/1024.0)/((double)elapsed/1000.0))+ " KB/s");
        
        if("a".equals("a"))
        return;
        //final File filesrc = new File("D:\\utorrent\\Windows_7_todas.as.versoes_x86_ou_x64_pt-BR.iso");
        //final File filedst = new File("D:\\utorrent\\Windows_7_todas.as.versoes_x86_ou_x64_pt-BR - Cópia.iso");
        //long millis_start = System.currentTimeMillis();
        /*WebServer server = new WebServer(553, new ProtocolFactory() {
            @Override
            public ServerProtocol get() {
                return new FileUploaderProtocol(filesrc,null);
            }
        });
        new Thread(server).start();*/
        String[] clients = new String[]{"a0","a1","a2","a3","a4"};
        FileUploaderTask Uploader = new FileUploaderTask(filesrc,555,Arrays.asList(clients));
        Uploader.startTask();
        
        for(int i=0;i<5;i++)
        {
            /*final int fi = i;
            WebClient client = new WebClient("127.0.0.1",553, new ProtocolFactory() {
                @Override
                public ServerProtocol get() {
                    File filedst = new File("D:\\utorrent\\gimp-2.8.22-setup - Cópia("+fi+").exe");
                    return new FileDownloaderProtocol(filedst, 0);
                }
            });
            new Thread(client).start();*/
            FileDownloaderTask t = new FileDownloaderTask("a"+i,new File("D:\\utorrent\\gimp-2.8.22-setup - Cópia("+i+").exe"), "127.0.0.1", 553,"none");
            t.startTask();
        }
        
        //inputstream(8192,0);
        //int elapsed = (int)(System.currentTimeMillis()-millis_start);
        //System.out.println("Elapsed:"+elapsed);
        //System.out.println("Speed:"+(((double)filesrc.length()/1024.0)/((double)elapsed/1000.0))+ " KB/s");
    }
    
    public static void randomaccess(long off) throws FileNotFoundException, IOException, InterruptedException
    {
        File file = new File("D:\\utorrent\\Windows_7_todas.as.versoes_x86_ou_x64_pt-BR.iso");
        long package_size = 1024*100;
        byte[] p = new byte[(int)package_size];
        int last_read = 0;
        long total_read = 0;
        
        

        try(RandomAccessFile rf = new RandomAccessFile(file,"r")) 
        {
            long file_len = rf.length();
            System.out.println(file_len);
            if(off < 0 || off*package_size >= file_len)
            {
                System.err.println("Offset '"+off+"' inválido");
                return;
            }
            
            rf.seek(off);
            do
            {
                last_read = rf.read(p);
                total_read += last_read;
                System.out.println(last_read);
                //Thread.sleep(1);
            }while(last_read == package_size);
        }
        System.out.println("Total size:\n"+total_read+" bytes \n"+(total_read/1024)+" Kb\n"+((total_read/1024)/1024)+" Mb");
    }
            
    // reading sequentially
    public static void inputstream(int package_size,long off) throws FileNotFoundException, IOException, InterruptedException
    {
        //int package_size = 1024*100;
        byte[] p = new byte[package_size];
        int last_read = 0;
        long total_read = 0;
        File file = new File("D:\\utorrent\\Windows_7_todas.as.versoes_x86_ou_x64_pt-BR.iso");
        FileInputStream file_in = new FileInputStream(file);
        long size = file.length();
        int percent = 0;
        try(BufferedInputStream in = new BufferedInputStream(file_in)) 
        {
            if(off != 0)
            in.skip(off);
            do
            {
                last_read = in.read(p);
                total_read += last_read;
                if((int)(((float)total_read/(float)size)*100.0f) > percent)
                {
                    percent = (int)(((float)total_read/(float)size)*100.0f);
                    System.out.println((int)percent);
                }
                //Thread.sleep(1);
            }while(last_read == package_size);
        
        }
        System.out.println("Total size:\n"+total_read+" bytes \n"+(total_read/1024)+" Kb\n"+((total_read/1024)/1024)+" Mb");
    }
}
