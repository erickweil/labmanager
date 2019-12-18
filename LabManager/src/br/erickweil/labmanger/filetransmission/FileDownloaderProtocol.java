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

import br.erickweil.webserver.ServerProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Usuario
 */
public class FileDownloaderProtocol extends ServerProtocol{
    
    final String uuid;
    final File file;
    long off;
    FileDownloaderTask task;
    
    public FileDownloaderProtocol(String uuid,File file,long off)
    {
        this.uuid = uuid;
        this.file = file;
        this.off = off;
    }
        
    public FileDownloaderProtocol(String uuid,File file,long off,FileDownloaderTask task)
    {
        this.uuid = uuid;
        this.file = file;
        this.off = off;
        this.task = task;
    }
    
    
    @Override
    public void processRequest() throws IOException {
        
        try
        {
            long millis_start = System.currentTimeMillis();
            
            // identificacao
            writeln_url(uuid);
            
            int package_size = Integer.parseInt(readln_url());
            long file_size = Long.parseLong(readln_url());
            
            System.out.println("Starting Donwload with off:"+off);
            writeln_url(""+off);
            byte[] p = new byte[package_size];
            long total_read = 0;
            FileOutputStream fout = new FileOutputStream(file,off > 0);
            try(BufferedOutputStream out = new BufferedOutputStream(fout)) 
            {
                
                while(true)
                {
                    int next_amount_to_read = off+package_size > file_size ? (int)(file_size-off) : package_size;
                    //if(package_size == -1)
                    //{
                    //System.out.println("reading: EOF!");
                    //off = -1;
                    //break;
                    //}
                    //System.out.println("reading:"+next_amount_to_read);
                    input.readFully(p,0,next_amount_to_read);
                    out.write(p, 0, next_amount_to_read);
                    //writeln_url("OK");
                    total_read += next_amount_to_read;
                    off += next_amount_to_read;
                    
                    if(off >= file_size)
                    {
                        System.out.println("EOF!");
                        task.setDownloadComplete();
                        off = -1;
                        break;
                    }
                    
                    
                    //if(new Random().nextInt(800) == 1) return;
                }
            }
            int elapsed = (int)(System.currentTimeMillis()-millis_start);
            System.out.println("Elapsed:"+elapsed);
            System.out.println("Speed:"+(((double)total_read/1024.0)/((double)elapsed/1000.0))+ " KB/s");
        }
        finally{
            if(task != null)
            {
                System.out.println("Ending download with off:"+off);
                task.setOff(off);
            }
        }
    }
}
