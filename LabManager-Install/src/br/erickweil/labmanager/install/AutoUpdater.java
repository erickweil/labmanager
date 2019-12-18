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

import br.erickweil.labamanger.common.files.StatusReceiver;
import br.erickweil.labamanger.common.files.Unzip;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import static br.erickweil.labmanager.install.UniversalInstaller.exec;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Usuario
 */
public class AutoUpdater implements StatusReceiver{
    public static final String install_dest = "C:\\Program Files\\labmanager\\";
    public static final String conf_path = "C:\\Program Files\\labmanager\\configs\\";
    
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
    {
        Thread.sleep(1000);
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
        
        new AutoUpdater().run();
    }
    
    public void run() throws FileNotFoundException, IOException, InterruptedException
    {
        /*InputStream in = Install.class.getResourceAsStream("labmanager.zip");
        if( in == null)
        {
            //g.sendMessage("Resource labmanager não existe.");
            throw new FileNotFoundException("Não encontrou os arquivos para copiar");
        }
        
        Unzip unziper = new Unzip();
        File tempDir = ProgramOpener.parsePath("%appdata%/LabManager/temp/");
        tempDir.mkdirs();
        unziper.unzip(in, tempDir);   
        */
        
        
        
        /*if(destination.exists())
        {
            System.out.println("Desinstalando versão anterior");
            for(File f : destination.listFiles())
            {
                if(f.isDirectory())
                {
                    if(!f.getName().equals("configs"))
                        Files.walkFileTree(f.toPath(), new DeleteFileVisitor());
                }
                else
                {
                    if(!f.getName().equals("client_trustore.jks"))
                        f.delete();
                }
            }
            
            //System.out.println("Removendo possíveis entradas no registro");
            //CmdExec.execCmd("reg","delete",registry_normal_path,"/v",registry_normal_key,"/f");
            //CmdExec.execCmd("reg","add",registry_userinit_path,"/v",registry_userinit_key,"/t",registry_userinit_type,"/d",registry_userinit_data,"/f");
            
            System.out.println("Desinstalado com sucesso!");
        }*/
        
        //System.out.println("Criando diretórios");
        //destination.mkdirs();
        
        //System.out.println("Copiando arquivos");
        //Files.walkFileTree(tempDir.toPath(), new CopyFileVisitor(destination.toPath()));
        
        //System.out.println("Adicionando entrada no registro");
        //CmdExec.execCmd("reg","add",registry_path,"/v",registry_key,"/t",registry_type,"/d",registry_data,"/f");
        
        //System.out.println("Deletando pasta temporária");
        //Files.walkFileTree(tempDir.toPath(), new DeleteFileVisitor());
        
        exec(this,install_dest+"nssm.exe","stop","labmanager_master");
        System.out.println("Aguardando parada do serviço...");
        Thread.sleep(2500);
        
        System.out.println("Extraindo arquivos");
        UniversalInstaller.extractFilesFromResources(this, "labmanager.zip", install_dest);
        
        if( UniversalInstaller.checkextractFilesFromResources("labmanager.zip", install_dest))
        {
            System.out.println("Instalado com sucesso!");
            CmdExec.restart();
        }
        else
        {
            System.out.println("Erro na instalação.");
            JOptionPane.showMessageDialog(null,
            "Erro na instalação verifique os logs.",
            "Inane error",
            JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void sendMessage(String msg) {
        System.out.println("msg:"+msg);
    }
/*
        public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;
    public CopyFileVisitor(Path targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
    final BasicFileAttributes attrs) throws IOException {
        if (sourcePath == null) {
            sourcePath = dir;
        } else {
            System.out.println("Creating dir "+dir.toString());
        Files.createDirectories(targetPath.resolve(sourcePath
                    .relativize(dir)));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
        System.out.println("Copying "+file.toString());
    Files.copy(file,
        targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
    return FileVisitResult.CONTINUE;
    }
}
    
public static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
    
    public DeleteFileVisitor() {
    }


    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
    //Files.copy(file,
    //    targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Deleting "+file.toString());
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("Deleted Directory "+dir.toString());
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }
    
    
}
*/
}
