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
package br.erickweil.labmanager.cmd;

import br.erickweil.labamanger.common.files.FilesHelper;
import br.erickweil.labmanager.start.Inicio;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

/**
 * Classe que abstrai a idéia de abrir programas.
 * 
 * teoricamente funcionaria em qualquer sistema operacional,
 * desde que o caminho de arquivo fosse informado de forma compatível
 * já que o método ParsePath e o Desktop é cross-platform.
*/
public class ProgramOpener {
    public static void main(String[] args) throws InterruptedException
    {
        String[] test = new String[]
        {
            "%windir%/System32/notepad.exe",
            "C:/ProgramData/UNIVALI/Portugol Studio/inicializador-ps.jar",
            "C:/Program Files/NetBeans.*/bin/netbeans.*\\.exe",
            "%JAVA_HOME%/bin/javaw.exe"
            
        };
        for(String s : test)
        {
            File f = parsePath(s);
            if(f!= null)
            System.out.println(f.getAbsolutePath());
            else
            System.out.println("erro no:"+s);
        }
        //Thread.sleep(2000);
        //kiosk_browse("https://jw.org");
        
        LocalDateTime now = LocalDateTime.now();
        String format3 = now.format(DateTimeFormatter.ofPattern("HH-mm-ss dd-MM-yyyy", Locale.getDefault()));
        
        File fnow = ProgramOpener.parsePath("%USERPROFILE%/Documents/Lab Manager/"+"print"+"thread1"+format3+".jpg");
        System.out.println(fnow.getAbsolutePath());
        
        //ProgramOpener.parsePath("%USERPROFILE%/Documents/Lab Manager/").mkdirs();
        //ProgramOpener.start(ProgramOpener.parsePath("%USERPROFILE%/Documents/Lab Manager/"),null);
        
        System.out.println(FilesHelper.isInside(new File("a"),new File("D:\\")));
    }
    
    /**
     * Esse método pode conter um caminho no formato:
     * C:/Program Files/NetBeans.+/bin/netbeans.*
     * 
     * onde que cada parte do caminho pode ser ou não um regex
     * atenção: C:/.* /img.png
     * 
     * irá procurar na primeira pasta de C:/ que bater
     * o critério '.*', para então procurar 'img.png'
     * 
     * não irá procurar TODAS as subpastas por 'img.png'
     * ele para no primeiro resultado
    */
    public static File parsePath(String path)
    {
        //  C:/Program Files/NetBeans.*/bin/netbeans.*
        String[] dirs = path.split("/");
        String total_path = "";
        String PS = File.separator;
        for(int i=0;i<dirs.length;i++)
        {
            if(dirs[i].startsWith("<"))
            {
                String conf_var = dirs[i].substring(1, dirs[i].length()-1);
                if(conf_var == null || conf_var.isEmpty())
                {
                    System.out.println("conf_var: '"+conf_var+"' inválida");
                    return null;
                }
                switch(conf_var)
                {
                    case "downloads":
                        // ATENÇÃO: se na conf do down_folder tiver <downloads> no caminho, da stackoverflow
                        total_path += ProgramOpener.parsePath(Inicio._down_folder).getAbsolutePath();
                        if(i < dirs.length -1)
                        total_path += PS;
                    break;
                    case "conf":
                        // ATENÇÃO: se na conf do conf_folder tiver <conf> no caminho, da stackoverflow
                        total_path += ProgramOpener.parsePath(Inicio._conf_folder).getAbsolutePath();
                        if(i < dirs.length -1)
                        total_path += PS;
                    break;
                    case "log":
                        // ATENÇÃO: se na conf do log_folder tiver <log> no caminho, da stackoverflow
                        total_path += ProgramOpener.parsePath(Inicio._log_folder).getAbsolutePath();
                        if(i < dirs.length -1)
                        total_path += PS;
                    break;
                    default:
                        System.out.println("conf_var: '"+conf_var+"' não existe");
                        return null;
                }
            }
            else if(dirs[i].startsWith("%"))
            {
                if( i != 0)
                {
                    System.out.println("env_var: '"+path+"' deve aparecer em primeiro lugar no caminho.");
                    return null;
                }
                String env_var = System.getenv(dirs[i].substring(1, dirs[i].length()-1));
                if(env_var == null || env_var.isEmpty())
                {
                    System.out.println("env_var: '"+env_var+"' não encontrada");
                    return null;
                }
                File env_file = new File(env_var);
                if(env_file.exists())
                {
                    total_path += env_file.getAbsolutePath();
                    if(i < dirs.length -1)
                    total_path += PS;
                }
                else
                {
                    System.out.println("env_file: '"+env_file+"' não existe");
                    return null;
                }
            }
            else if( i > 0)
            {
                File f = new File(total_path);
                String[] list = f.list();
                //System.out.println(Arrays.toString(list));
                boolean matched = false;
                if(f.exists() && list != null)
                {
                    for(int k=0;k<list.length;k++)
                    {
                        try
                        {
                            // primeiro checa igualdade, depois o regex, pq o regex pode dar erro
                            if(list[k].equals(dirs[i]) || list[k].matches(dirs[i]))
                            {
                                //System.out.println(list[k]+" -> "+dirs[i]);
                                //System.out.println("match");
                                total_path += list[k];
                                if(i < dirs.length -1)
                                total_path += PS;
                                matched = true;
                                break;
                            }
                        }
                        catch(PatternSyntaxException ex){ /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */}
                    }
                }
                if(!matched)
                {
                    //Inicio.out.println("dir: '"+dirs[i]+"' não bate com nenhum diretório em '"+total_path+"' ");
                    //return null;
                    // quando o caminho é para um lugar que ainda não existe:
                    total_path += dirs[i];
                    if(i < dirs.length -1)
                    total_path += PS;
                }
            }
            else
            {
                total_path += dirs[i];
                total_path += PS;
            }
        }
        //System.out.println(total_path);
        return new File(total_path);
    }
    
    public static final int KIOSK_CHROME = 1;
    public static final int KIOSK_INTERNETEXPLORER = 2;
    public static final int KIOSK_NONE = 5;

    public static boolean anyExist(String ... paths)
    {
        for(String p : paths)
        {
            try{
                File path = parsePath(p);
                if(path.exists()) return true;
            }
            catch(Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
        return false;
    }
    
    
    public static int kiosk_browse(String site)
    {
        // modo kiosk. funciona como um navegador que exibe apenas um site, e nenhum outro.
        
        //https://peter.sh/experiments/chromium-command-line-switches/
        //https://productforums.google.com/forum/#!topic/chrome/vd3-DMq3orQ
        if(anyExist("%programfiles%/Google/Chrome/Application/chrome.exe",
        "%programfiles(x86)%/Google/Chrome/Application/chrome.exe",
        "%localappdata%/Google/Chrome/Application/chrome.exe"))
        {
            // todas as instâncias do chrome devem estar fechadas antes de
            // iniciar o modo kiosk
            CmdExec.stop("chrome.exe",false);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
            
            //CmdExec.start("kiosk-chrome", site);
            return KIOSK_CHROME;
        }
        
        //https://support.microsoft.com/en-us/help/154780/how-to-use-kiosk-mode-in-microsoft-internet-explorer
        //https://superuser.com/questions/102334/where-is-internet-explorer-located-on-windows-7
        if(anyExist("%programfiles%/Internet Explorer/iexplore.exe",
        "%programfiles(x86)%/Internet Explorer/iexplore.exe"))
        {
            //CmdExec.start("kiosk-iexplore", site);
            return KIOSK_INTERNETEXPLORER;
        }
        
        //https://superuser.com/questions/496555/launching-google-chrome-opera-in-kiosk-mode
        //if(anyExist("%programfiles%/Opera/opera.exe",
        //"%programfiles(x86)%/Opera/opera.exe"))
        //{
        //    CmdExec.start("kiosk-opera", site);
        //    return KIOSK_OPERA;
        //}
        
        // fallback (firefox only?)
        browse(site);
        return KIOSK_NONE;
    }
    
    public static void kiosk_close(int which)
    {
        if(which == KIOSK_CHROME) CmdExec.stop("chrome",true);
        if(which == KIOSK_INTERNETEXPLORER) CmdExec.stop("iexplore",true);
    }
    
    public static void browse(String site)
    {
        // before any Desktop APIs are used, first check whether the API is
        // supported by this particular VM on this particular host
        if (Desktop.isDesktopSupported()) {
            Desktop desk = Desktop.getDesktop();
            URI uri;
            try {
                uri = new URI(site);
                desk.browse(uri);
                System.out.println("started "+uri);
            } catch (URISyntaxException e) {
                System.out.println("erro ao fazer o parsing do URI do site '"+site+"' exceção:"+e.getMessage());
                e.printStackTrace(System.out);
            } catch (IOException e) {
                System.out.println("erro ao acessar o site '"+site+"' exceção:"+e.getMessage());
                e.printStackTrace(System.out);
            }
            
            
        }
        else
        {
            System.out.println("Não pôde acessar o site porque Classe Desktop não é suportada");
        }
    }
    public static boolean start(String path, String process)
    {
        if(path == null || path.isEmpty())
        {
            System.out.println("path: '"+path+"' null!");
            return false;
        }
        File file = parsePath(path);
        return start(file, process);
    }
    
    public static boolean start(File file, String process)
    {
        if(file != null && file.exists())
        {
            if(file.isFile() && process != null && !process.isEmpty())
            {
                if(CmdExec.is_running_process(process))
                {
                    System.out.println("process: '"+process+"' already running");
                    return true;
                }
            }
            try
            {
                // before any Desktop APIs are used, first check whether the API is
                // supported by this particular VM on this particular host
                if (Desktop.isDesktopSupported()) {
                    Desktop desk = Desktop.getDesktop();
                    desk.open(file);
                    System.out.println("started "+file.getAbsolutePath());
                    return true;
                }
                else
                {
                    System.out.println("Não pôde abrir o programa porque Classe Desktop não é suportada");
                    return false;
                }
                

            }
            catch(IOException e)
            {
                System.out.println("erro ao abrir o programa '"+file.getAbsolutePath()+"' exceção:"+e.getMessage());
                e.printStackTrace(System.out);
                return false;
            }
            
        }
        else
        {
           System.out.println("file "+(file != null ? file.getAbsolutePath() : "null")+" not found");
           return false;
        }
    }
}
