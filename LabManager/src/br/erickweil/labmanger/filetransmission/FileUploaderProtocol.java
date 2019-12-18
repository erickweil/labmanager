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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class FileUploaderProtocol extends ServerProtocol {
    File file;
    FileUploaderTask task;
    double net_maxspeed;
    long net_started;
    public FileUploaderProtocol(File file,FileUploaderTask task, double net_maxspeed)    
    {
       this.file = file;
       this.task = task;
       this.net_maxspeed = net_maxspeed;
    }
    
    @Override
    public void processRequest() throws IOException {
        
        
        
        
        if(!file.exists())
        {
            System.err.println("tentou fazer upload de arquivo que não existe");
            return;
        }
        
        if(!file.isFile())
        {
            System.err.println("tentou fazer upload de arquivo que não é arquivo");
            return;
        }
        
        // deveria ter alguma autenticacao nessa comunicacao sem ssl? o que isso impediria?
        
        String client_uuid = readln_url();
        if(!task.uuidPresent(client_uuid))
        {
            System.err.println("cliente inválido tentou fazer download:"+client_uuid);
            return;
        }

        try
        {
            int package_size = (int)Math.pow(2.0, 18.0);
            long file_len = file.length();
            writeln_url(""+package_size);
            writeln_url(""+file_len);

            long off = Long.parseLong(readln_url());


            if(off == 0)
            {
                task.setDownloadStart(client_uuid);
                net_started = System.currentTimeMillis();
            }
            else
            {
                net_started = task.getDownloadStart(client_uuid);
            }


            byte[] p = new byte[package_size];
            int last_read = 0;
            long total_read = 0;
            //String response = null;
            FileInputStream fin = new FileInputStream(file);
            try(BufferedInputStream in = new BufferedInputStream(fin)) 
            {
                if(off != 0)
                {
                    long skipped = in.skip(off);
                    System.out.println("skipped "+skipped+"/"+off);
                }
                do
                {
                    if(net_maxspeed > 0.001) // se é para controlar a velocidade
                    {
                        double downloaded = (total_read+off)/1024.0; // kb
                        double timediff = System.currentTimeMillis() - net_started;
                        double mean_speed = downloaded/(timediff/1000.0); // kb/s

                        if(mean_speed > net_maxspeed)
                        {
                            double packets_sent = (total_read+off)/package_size;
                            try {
                                Thread.sleep((long)(timediff/packets_sent) * (new Random().nextInt(10) + 2)); // dorme o tempo de envio de +- 10 pacotes;
                            } catch (InterruptedException ex) {
                                Logger.getLogger(FileUploaderProtocol.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            continue;
                        }
                    }

                    last_read = in.read(p);
                    if(last_read == -1) break;
                    total_read += last_read;
                    //System.out.println("writing:"+last_read);
                    //writeln_url(""+last_read);
                    output.write(p,0,last_read);

                    // calcular progresso
                    //int progress = (int)((total_read*100L)/file_len);
                    //if(progress != last_progress && progress != 100)
                    //{
                    task.setTotalDownloaded(client_uuid, total_read+off);
                    //}
                }
                while(last_read == package_size);

                System.out.println("EOF!");
                task.setTotalDownloaded(client_uuid,file_len);
                //writeln_url(""+(-1));
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FileUploaderProtocol.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    task.stopIfAllDownloaded();
                }
            }).start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            task.notifyDownloadDisconnected(client_uuid);
        }
    }
    
    
}
