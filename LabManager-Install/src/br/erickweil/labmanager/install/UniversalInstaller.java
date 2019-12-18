/*
 * Copyright (C) 2018 Usuario
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
package br.erickweil.labmanager.install;

import br.erickweil.labamanger.common.files.FilesHelper;
import br.erickweil.labamanger.common.files.StatusReceiver;
import br.erickweil.labamanger.common.files.Unzip;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

/**
 *
 * @author Usuario
 */
public class UniversalInstaller {
    
    public static void exec(StatusReceiver g,String ... args)
    {
        if(g != null)g.sendMessage(String.join(" ", args));
        List<String> cmdResults = new ArrayList<>();
            
        cmdResults = CmdExec.readCmd(args);
        if(cmdResults == null || g == null) return;
        for(String line : cmdResults)
        {
            g.sendMessage("\t"+line);
        }
    }
    
    public static boolean checkDirectories(StatusReceiver g,String[] directories) throws FileNotFoundException, IOException
    {
        for(int i =0;i<directories.length;i++)
        {
            try
            {
                String[] split = directories[i].split(",");
                File source = new File(split[0]);
                File destination = new File(split[1]);
                
                if(!destination.exists()) return false;
                
                if(source.exists())
                {
                    if(source.isFile())
                    {
                        if(source.getName().endsWith(".zip"))
                        {
                            if(!FilesHelper.check_unzip(source,destination)) return false;
                        }    
                        else
                        {
                            
                            //Files.copy(source.toPath(), new File(destination,source.getName()).toPath());
                            if(!(destination.exists() && destination.isFile())) return false;
                            if(source.length() != destination.length()) return false;
                        }
                    }
                    else
                    {
                        if(!FilesHelper.checkDirectory(g, source, destination)) return false;
                    }
                }
            }
            catch (IOException e) 
            {
                e.printStackTrace();
                g.sendMessage("Erro ao checar '"+directories[i]+"': "+e.getClass().toString()+" -> "+e.getMessage());
            }
            
        }
        return true;
    }
    
    public static void copyDirectories(StatusReceiver g,String[] directories) throws FileNotFoundException, IOException
    {
        for(int i =0;i<directories.length;i++)
        {
            try 
            {
                String[] split = directories[i].split(",");
                File source = new File(split[0]);
                File destination = new File(split[1]);
                destination.mkdirs();
                
                if(source.exists())
                {
                    if(source.isFile())
                    {
                        if(source.getName().endsWith(".zip"))
                        {
                            FilesHelper.unzip(source,destination);
                        }    
                        else
                        {
                            g.sendMessage("Copiando arquivo:"+destination.getAbsolutePath());
                            Files.copy(source.toPath(), new File(destination,source.getName()).toPath());
                        }
                    }
                    else
                    {   
                        g.sendMessage("Copiando diretório:"+destination.getAbsolutePath());
                        FilesHelper.copyDirectory(g, source, destination);
                    }
                }
            }
            catch (IOException e) 
            {
                e.printStackTrace();
                g.sendMessage("Erro ao deletar '"+directories[i]+"': "+e.getClass().toString()+" -> "+e.getMessage());
            }
            
        }
    }
    
    public static void extractFilesFromResources(StatusReceiver g,String resource,String destination) throws FileNotFoundException, IOException
    {
        InputStream in;
        
        File dest = new File(destination);
        g.sendMessage("Criando diretórios");
        dest.mkdirs();
        
        
        in = Install.class.getResourceAsStream(resource);
        if( in == null)
        {
            g.sendMessage("Resource '"+resource+"' não existe.");
            throw new FileNotFoundException("Não encontrou os arquivos para copiar");
        }
        Unzip unziper = new Unzip();
        g.sendMessage("Extraindo arquivos...");
        unziper.unzip(in, dest);
    }
    
    public static boolean checkextractFilesFromResources(String resource,String destination) throws FileNotFoundException, IOException
    {
        InputStream in;
        
        File dest = new File(destination);

        in = Install.class.getResourceAsStream(resource);
        if( in == null)
        {
            throw new FileNotFoundException("Não encontrou os arquivos para checar");
        }
        Unzip unziper = new Unzip();
        return unziper.check_unzip(in, dest);
    }
    
    public static void registerService(StatusReceiver g, String[] services)
    {
        //nssm install <servicename> <program>
        for(int i=0;i<services.length;i++)
        {
            String[] split = services[i].split(",");
            String nssmPath = split[0];
            String servicename = split[1];
            String program = split[2];
            
            g.sendMessage("Instalando serviço:"+services[i]);
            exec(g,nssmPath,"install",servicename,program);
        }
    }
    
    public static void unregisterService(StatusReceiver g, String[] services) throws InterruptedException
    {
        //nssm remove <servicename> confirm
        for(int i=0;i<services.length;i++)
        {
            String[] split = services[i].split(",");
            String nssmPath = split[0];
            String servicename = split[1];
            
            g.sendMessage("parando serviço:"+services[i]);
            exec(g,nssmPath,"stop",servicename);
            
            Thread.sleep(500);
            
            g.sendMessage("removendo serviço:"+services[i]);
            exec(g,nssmPath,"remove",servicename,"confirm");
        }
    }
    
    public static boolean checkService(StatusReceiver g, String[] services)
    {
        //nssm remove <servicename> confirm
        for(int i=0;i<services.length;i++)
        {
            String[] split = services[i].split(",");
            String nssmPath = split[0];
            String servicename = split[1];
            String program = split[2];
            
            exec(g,nssmPath,"get",servicename,"Application");
            List<String> readCmd = CmdExec.readCmd(nssmPath,"get",servicename,"Application");
            //boolean matches = false;
            for(String s: readCmd)
            {
                System.out.println(s);
                //if(s.trim().replaceAll("\\s+", " ").contains(program.trim().replaceAll("\\s+", " "))) matches = true;
            }
            //if(!matches) return false;
        }
        return true;
    }
        
    public static void deleteRegistry(StatusReceiver g,String[] keys)
    {
        for(int i=0;i<keys.length;i++)
        {
            int ind = keys[i].lastIndexOf('\\');
            String path = keys[i].substring(0,ind);
            String key = keys[i].substring(ind+1);
            g.sendMessage("Deletando chave do registro:"+keys[i]);
            exec(g,"reg","delete",path,"/v",key,"/f");
        }
    }
    public static void addRegistry(StatusReceiver g,String[] keys)
    {
        for(int i=0;i<keys.length;i++)
        {
            String[] infos = keys[i].split(",");
            int ind = infos[0].lastIndexOf('\\');
            String path = infos[0].substring(0,ind);
            String key = infos[0].substring(ind+1);
            String type = infos[1];
            String data = infos[2];
            g.sendMessage("Adicionando entrada no registro:"+keys[i]);
            exec(g,"reg","add",path,"/v",key,"/t",type,"/d",data,"/f");
        }
    }
    
    public static boolean checkRegistry(StatusReceiver g,String[] keys)
    {
        //REG QUERY HKLM\Software\Microsoft\ResKit /v Version
        for(int i=0;i<keys.length;i++)
        {
            String[] infos = keys[i].split(",");
            int ind = infos[0].lastIndexOf('\\');
            String path = infos[0].substring(0,ind);
            String key = infos[0].substring(ind+1);
            String type = infos[1];
            String data = infos[2];
            //g.sendMessage("checando entrada no registro:"+keys[i]);
            //exec(g,"reg","query",path,"/v",key);
            List<String> readCmd = CmdExec.readCmd("reg","query",path,"/v",key);
            boolean matches = false;
            for(String s: readCmd)
            {
                System.out.println(s);
                if(s.trim().contains(data.trim())) matches = true;
            }
            if(!matches) return false;
        }
        return true;
    }
    
    public static void deleteFiles(StatusReceiver g,String[] directories)
    {
        for(int i =0;i<directories.length;i++)
        {
            try 
            {
                File destination = new File(directories[i]);
                if(destination.exists())
                {
                    if(destination.isFile())
                    {
                        g.sendMessage("Deletando arquivo:"+destination.getAbsolutePath());
                        destination.delete();
                    }
                    else
                    {   
                        g.sendMessage("Deletando diretório:"+destination.getAbsolutePath());
                        FilesHelper.deleteDirectory(g, destination);
                    }
                }
            }
            catch (IOException e) 
            {
                e.printStackTrace();
                g.sendMessage("Erro ao deletar '"+directories[i]+"': "+e.getClass().toString()+" -> "+e.getMessage());
            }
        }
    }
    

}
