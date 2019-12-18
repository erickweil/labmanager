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
package br.erickweil.labmanager.install;

import br.erickweil.labamanger.common.files.Unzip;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class Install extends Thread{

    InstallGrafico g;
    public Install(InstallGrafico g)
    {
        this.g = g;
    }
    
    @Override
    public void run()
    {
        File logFile = new File("log.txt");
        File errFile = new File("err.txt");
        PrintStream old_out = System.out;
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)),true,"UTF-8"));
            System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFile)),true,"UTF-8"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Install.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Install.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            uninstall();
            install();
            g.suscessfull();
        } catch (Exception ex) {
            
            g.sendMessage(ex.getMessage());
           StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            g.sendMessage(errors.toString());
            
            g.failed("Erro na instalação, veja acima");
        }
    }
    
    public void install() throws FileNotFoundException, IOException
    {
        String install_dest = "C:\\Program Files\\labmanager\\";
        //String install_orig = "labmanager";
        String registry_path = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run";
        String registry_key = "LABMANAGER";
        String registry_type = "REG_SZ";
        String registry_data = "C:\\Program Files\\labmanager\\svchost.exe";
        
        g.sendMessage("Instalando");
        
        //File source = new File(install_orig);
        //if(!source.exists()){
        //    g.sendMessage("Diretório "+source.getAbsolutePath()+" não existe.");
        //    throw new FileNotFoundException("Não encontrou os arquivos para copiar em '"+install_orig+"'");
        //}
        
        InputStream in = getClass().getResourceAsStream("labmanager.zip");
        if( in == null)
        {
            g.sendMessage("Resource labmanager não existe.");
            throw new FileNotFoundException("Não encontrou os arquivos para copiar");
        }
        Unzip unziper = new Unzip();
        File tempDir = ProgramOpener.parsePath("%appdata%/LabManager/temp/");
        tempDir.mkdirs();
        unziper.unzip(in, tempDir);
        
        File destination = new File(install_dest);
        
        g.sendMessage("Criando diretórios");
        destination.mkdirs();
        
        g.sendMessage("Copiando arquivos");
        Files.walkFileTree(tempDir.toPath(), new CopyFileVisitor(g,destination.toPath()));
        
        g.sendMessage("Adicionando entrada no registro");
        CmdExec.execCmd("reg","add",registry_path,"/v",registry_key,"/t",registry_type,"/d",registry_data,"/f");
        
        g.sendMessage("Deletando pasta temporária");
        Files.walkFileTree(tempDir.toPath(), new DeleteFileVisitor(g));
        
        g.sendMessage("Instalado com sucesso!");
    }
    
    public void uninstall() throws IOException
    {        
        String install_dest = "C:\\Program Files\\labmanager\\";
        String registry_normal_path = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run";
        String registry_normal_key = "LABMANAGER";
        
        //String registry_userinit_path = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Winlogon";
        //String registry_userinit_key = "Userinit";
        //String registry_userinit_type = "REG_SZ";
        //String registry_userinit_data = "C:\\Windows\\system32\\userinit.exe,";
        
        File destination = new File(install_dest);
        if(destination.exists())
        {
            g.sendMessage("Desinstalando versão anterior");
            Files.walkFileTree(destination.toPath(), new DeleteFileVisitor(g));
            
            g.sendMessage("Removendo possíveis entradas no registro");
            CmdExec.execCmd("reg","delete",registry_normal_path,"/v",registry_normal_key,"/f");
            //CmdExec.execCmd("reg","add",registry_userinit_path,"/v",registry_userinit_key,"/t",registry_userinit_type,"/d",registry_userinit_data,"/f");
            
            g.sendMessage("Desinstalado com sucesso!");
        }
        else return;
    }
    
    public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;
    InstallGrafico g;
    public CopyFileVisitor(InstallGrafico g,Path targetPath) {
        this.g = g;
        this.targetPath = targetPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
    final BasicFileAttributes attrs) throws IOException {
        if (sourcePath == null) {
            sourcePath = dir;
        } else {
            g.sendMessage("Creating dir "+dir.toString());
        Files.createDirectories(targetPath.resolve(sourcePath
                    .relativize(dir)));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
        g.sendMessage("Copying "+file.toString());
    Files.copy(file,
        targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
    return FileVisitResult.CONTINUE;
    }
}
    
public static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
    
    InstallGrafico g;
    public DeleteFileVisitor(InstallGrafico g) {
        this.g = g;
    }


    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
    //Files.copy(file,
    //    targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
        g.sendMessage("Deleting "+file.toString());
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        g.sendMessage("Deleted Directory "+dir.toString());
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }
    
    
}
}
