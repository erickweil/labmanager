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
package br.erickweil.labmanager.start;

import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import br.erickweil.labmanager.platformspecific.CheckOS;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Classe que trata de carregar a configurção e o logging para arquivos.
 * 
 * @author Aluno
 */
public class Inicio {
    
    public static boolean _logtofile = false;
    public static String _log_folder = "%appdata%/LabManager/logs/";
    public static String _down_folder = "%USERPROFILE%/Downloads/";//"%appdata%/LabManager/down/";
    public static String _conf_folder = "%appdata%/LabManager/conf/";
    public static String _temp_folder = "%Temp%";

    public static PrintStream old_out;
    public static File errFile;
    //static{ // gambiarra
        //out = System.out;
        //log_folder =  System.getenv("programdata")+ "\\LabManager\\logs\\";
        //down_folder = System.getenv("programdata")+ "\\LabManager\\down\\";
    //}
    /**
     * Metodo para iniciar as configurações do labmanager, tanto cliente como servidor
     * E redirecionar o output para um arquivo, se for o caso.
     * @throws IOException
     * @throws InterruptedException 
     */
    public static void start() throws IOException, InterruptedException
    {
        if(CheckOS.isWindows())
        {
            _log_folder = "%appdata%/LabManager/logs/";
            _down_folder = "%USERPROFILE%/Downloads/";
            _conf_folder = "%appdata%/LabManager/conf/";
            _temp_folder = "%Temp%";
        }
        else if(CheckOS.isUnix())
        {
            _log_folder = "logs/";
            _down_folder = "down/";
            _conf_folder = "conf/";
            _temp_folder = "temp/";
        }
        try {
               
				SimpleLogger logger = new SimpleLogger();
				new Configurable(logger,Inicio.class, "config_inicio");
                //throw new Exception("OLOCO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        
        try {
            if(_logtofile)
            {
                File log = ProgramOpener.parsePath(_log_folder);
                log.mkdirs();
                File down = ProgramOpener.parsePath(_down_folder);
                down.mkdirs();
                if(!log.isDirectory() || !log.exists())
                {            
                    System.err.println("não pode criar o diretório de log de erros");
                }
                if(!down.isDirectory() || !down.exists())
                {            
                    System.err.println("não pode criar o diretório de downloads");
                }

                LocalDateTime now = LocalDateTime.now();
                String format3 = now.format(DateTimeFormatter.ofPattern("HH-mm-ss dd-MM-yyyy", Locale.getDefault()));

                File logFile = new File(log,"log "+format3+".txt");
                old_out = System.out;
                PrintStream new_out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)),true,"UTF-8");
                System.setOut(new_out);
                System.setErr(new_out);

                errFile = new File(log,"err "+format3+".txt");
            }
        } 
        catch(Exception e1)
        {
            e1.printStackTrace();
            if(old_out != null)
            System.setOut(old_out);
        }
        
        //if(_init_type.equalsIgnoreCase("server"))
        //{
        //    ServerMain.main(args);
        //}
        //else
        //{
            //ClientMain.main(args);
        //}
    }
}

