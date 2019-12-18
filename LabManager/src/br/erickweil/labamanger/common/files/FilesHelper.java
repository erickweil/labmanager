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
package br.erickweil.labamanger.common.files;

import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Usuario
 */
public class FilesHelper {
    
   public static boolean isInside(File f,File dir)
    {
        int count = 64;
        File absoluteF = f.getAbsoluteFile();
        File absoluteDir = dir.getAbsoluteFile();
        while(absoluteF != null && absoluteF.getParentFile() != null && count > 0)
        {
            absoluteF = absoluteF.getParentFile();
            if(absoluteF.getAbsolutePath().equals(absoluteDir.getAbsolutePath()))
            {
                return true;
            }// else System.out.println(absoluteF.getAbsolutePath()+" != "+absoluteDir.getAbsolutePath());
            count--;
        }
        return false;
    }
    
    public static void delExeFile(File exe)
    {
        try
        {
            if(exe.delete())
            {
                return;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Deletando arquivo sendo usado?");

        CmdProcess p = new CmdProcess(null, null, 0);
        p.executable = exe;
        CmdExec.stop(p, true); // parar pelo caminho do executável
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(FilesHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(!exe.delete())
        {
            System.out.println("ainda nao deletou?");
        }
    }
    
    public static void unzip(File zipFilePath, File destDir) throws FileNotFoundException, IOException {
        Unzip unziper = new Unzip();
        unziper.unzip(new FileInputStream(zipFilePath), destDir);
    }
    
    public static boolean check_unzip(File zipFilePath, File destDir) throws FileNotFoundException, IOException {
        Unzip unziper = new Unzip();
        return unziper.check_unzip(new FileInputStream(zipFilePath), destDir);
    }
    
    public static void __unzip(File zipFilePath, File destDir) {
        // create output directory if it doesn't exist
        if(!destDir.exists()) destDir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry ze = zis.getNextEntry();
                while(ze != null){
                    String fileName = ze.getName();
                    File newFile = new File(destDir, fileName);
                    if(newFile.exists() && newFile.getName().endsWith(".exe"))
                    {
                        delExeFile(newFile);
                    }
                    System.out.println("Unzipping to "+newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    //close this ZipEntry
                    zis.closeEntry();
                    ze = zis.getNextEntry();
                }
                //close last ZipEntry
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static void zipFile(File fileToZip, File destination) throws FileNotFoundException, IOException
    {
        destination.getAbsoluteFile().getParentFile().mkdirs();
        try (ZipOutputStream zipOut = new ZipOutputStream( new FileOutputStream(destination))) {
            if (fileToZip.isDirectory()) {
                File[] children = fileToZip.listFiles();
                for (File childFile : children) {
                    _zipFile(childFile, childFile.getName(), zipOut);
                }
            }
            else
            {
                _zipFile(fileToZip, fileToZip.getName(), zipOut);
            }
        }
    }
    
    private static void _zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                _zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
    
    public static boolean checkDirectory(StatusReceiver g,File directory,File destination) throws IOException
    {
        CheckFileVisitor visitor = new CheckFileVisitor(g,destination.toPath());
        Files.walkFileTree(directory.toPath(), visitor);
        return visitor.equals;
    }
    
    public static void copyDirectory(StatusReceiver g,File directory,File destination) throws IOException
    {
        Files.walkFileTree(directory.toPath(), new CopyFileVisitor(g,destination.toPath()));
    }
    
    public static void deleteDirectory(StatusReceiver g,File directory) throws IOException
    {
        Files.walkFileTree(directory.toPath(), new DeleteFileVisitor(g));
    }

        public static class CheckFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;
    StatusReceiver g;
    private boolean equals = true;
    public CheckFileVisitor(StatusReceiver g,Path targetPath) {
        this.g = g;
        this.targetPath = targetPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
    final BasicFileAttributes attrs) throws IOException {
        if (sourcePath == null) {
            sourcePath = dir;
        } else {
            if(!targetPath.resolve(sourcePath.relativize(dir)).toFile().exists())
            {
                equals = false;
            }
        //    g.sendMessage("Creating dir "+dir.toString());
        //Files.createDirectories(targetPath.resolve(sourcePath
        //            .relativize(dir)));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
        //g.sendMessage("Copying "+file.toString());
        //Files.copy(file,
        //targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
        Path resolved = targetPath.resolve(sourcePath.relativize(file));
        if(!resolved.toFile().exists()) equals = false;
        if(file.toFile().length() != resolved.toFile().length()) equals = false;
    return FileVisitResult.CONTINUE;
    }
}
    
        public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;
    StatusReceiver g;
    public CopyFileVisitor(StatusReceiver g,Path targetPath) {
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
    
    StatusReceiver g;
    public DeleteFileVisitor(StatusReceiver g) {
        this.g = g;
    }


    @Override
    public FileVisitResult visitFile(final Path file,
    final BasicFileAttributes attrs) throws IOException {
    //Files.copy(file,
    //    targetPath.resolve(sourcePath.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
        g.sendMessage("Deleting "+file.toString());
        File pathfile = file.toFile();
        delExeFile(pathfile);
        //if(pathfile.exists() && pathfile.getName().endsWith(".exe"))
        //{
        //    pathfile);
        //}
        //else
        //{
        //    Files.delete(file);
        //}
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        g.sendMessage("Deleted Directory "+dir.toString());
        //Files.delete(dir);
        File pathfile = dir.toFile();
        delExeFile(pathfile);
        return FileVisitResult.CONTINUE;
    }
    
    
}
}
