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
package br.erickweil.labamanger.common.files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
public class Unzip{
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void unzip(InputStream zipFileStream, File destDir) throws IOException {
        //File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try ( //ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipInputStream zipIn = new ZipInputStream(zipFileStream)) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
    
    public boolean check_unzip(InputStream zipFileStream, File destDir) throws IOException {
        //File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            return false;
        }
        try ( //ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipInputStream zipIn = new ZipInputStream(zipFileStream)) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    //extractFile(zipIn, filePath);
                    File entryfile = new File(filePath);
                    if(!(entryfile.exists() && entryfile.isFile())) return false;
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    if(!(dir.exists() && dir.isDirectory())) return false;
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        
        return true;
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        
        // deleta primeiro se é exe (SÓ FUNCIONA NO WINDOWS!!!)
        if(new File(filePath).exists() && filePath.endsWith("exe"))
        {
            FilesHelper.delExeFile(new File(filePath));
        }
        
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}