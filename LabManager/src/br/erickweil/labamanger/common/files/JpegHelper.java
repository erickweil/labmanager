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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Helper de compressão do JPEG
 * @author Usuario
 */
public class JpegHelper {
    public static byte[] compress_jpeg(BufferedImage image,float quality) throws IOException
    {
        // The important part: Create in-memory stream
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);

        // NOTE: The rest of the code is just a cleaned up version of your code

        // Obtain writer for JPEG format
        ImageIO.setUseCache(false);
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();

        // Configure JPEG compression: 70% quality
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        // Set your in-memory stream as the output
        jpgWriter.setOutput(outputStream);

        // Write image as JPEG w/configured settings to the in-memory stream
        // (the IIOImage is just an aggregator object, allowing you to associate
        // thumbnails and metadata to the image, it "does" nothing)
        jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);

        // Dispose the writer to free resources
        jpgWriter.dispose();

        // Get data for further processing...
        byte[] jpegData = compressed.toByteArray();
        return jpegData;
    }
    
    public static BufferedImage decompress_jpeg(byte[] data) throws IOException
    {
        // The important part: Create in-memory stream
        ByteArrayInputStream compressed = new ByteArrayInputStream(data);
        ImageInputStream inputStream = ImageIO.createImageInputStream(compressed);

        // NOTE: The rest of the code is just a cleaned up version of your code

        // Obtain writer for JPEG format
        ImageIO.setUseCache(false);
        ImageReader jpgReader = ImageIO.getImageReadersByFormatName("jpg").next();

        // Configure JPEG compression: 70% quality
        ImageReadParam jpgWriteParam = jpgReader.getDefaultReadParam();
        //jpgWriteParam.setDestination(data);
        
        // Set your in-memory stream as the output
        // Set your in-memory stream as the output
        jpgReader.setInput(inputStream);        

        // Write image as JPEG w/configured settings to the in-memory stream
        // (the IIOImage is just an aggregator object, allowing you to associate
        // thumbnails and metadata to the image, it "does" nothing)
        //jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
        BufferedImage result = jpgReader.read(0, jpgWriteParam);

        // Dispose the writer to free resources
        jpgReader.dispose();

        return result;
    }
}

