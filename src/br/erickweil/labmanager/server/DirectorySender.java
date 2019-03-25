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
package br.erickweil.labmanager.server;

import br.erickweil.webserver.ReaderWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLASSE INUTIL
 * PROSSIGA
 *
 */
public class DirectorySender {

    static void sendDir(File file, DataOutputStream output) {
        if(!file.exists())
        {
            System.err.println("Erro ao enviar versão mais nova: Arquivo "+file.getAbsolutePath()+" não existe!");
            return;
        }
        if(!file.isDirectory())
        {
            System.err.println("Erro ao enviar versão mais nova: Arquivo "+file.getAbsolutePath()+" não é um diretório!");
            return;
        }
        try {
            Files.walkFileTree(file.toPath(), new CopyFileVisitor(file.toPath(),output));
        } catch (IOException ex) {
            Logger.getLogger(DirectorySender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
     private final DataOutputStream output;
     private Path sourcePath = null;
     private Path initialPath = null;
     public CopyFileVisitor(Path initialPath, DataOutputStream output) {
         this.initialPath = initialPath;
         this.output = output;
     }
     
    public void write(String msg) throws UnsupportedEncodingException, IOException
    {
    	ReaderWriter.writeASCII(URLEncoder.encode(msg,"UTF-8")+"\n", output);
    }

     @Override
     public FileVisitResult preVisitDirectory(final Path dir,
     final BasicFileAttributes attrs) throws IOException {
         if (sourcePath == null) {
             sourcePath = dir;
         } else {
         }
         return FileVisitResult.CONTINUE;
     }

     @Override
     public FileVisitResult visitFile(final Path file,
     final BasicFileAttributes attrs) throws IOException {
     //Files.copy(file,targetPath.resolve(sourcePath.relativize(file)));
     Path relativedestination = sourcePath.relativize(file);
     write("FILE");
     write(relativedestination.toString());
     byte[] dados_arquivo = Files.readAllBytes(file);
     write(""+dados_arquivo.length);
     output.write(dados_arquivo);
     return FileVisitResult.CONTINUE;
     }
 }
}
