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
package br.erickweil.labamanger.common;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JComponent;

/**
 * Classe de utilidades aleatórias
 * @author Usuario
 */
public class WeilUtils {
    public static String tempoDecorrido(long millis)
    {
        long s = millis/1000;
        long segundos = s % 60;
        long minutos = (s / 60) % 60;
        long horas = (s / 3600) % 24;
        long dias = (s / 86400);
        
        return (dias > 0 ? dias+" dias " : "")+
            (horas > 0 ? horas+", horas " : "")+
            (minutos > 0 ? minutos+", minutos " : "")+
            (segundos > 0 ? segundos+", segundos " : "");
    }
    
    public static String velocidadeDownload(double kbs)
    {
        NumberFormat formatter = new DecimalFormat("#0.0");
        if(kbs < 10.0)
        {
           return formatter.format(kbs)+" KB/s";
        }
        else if(kbs < 1000.0)
        {
            return ""+((int)kbs)+" KB/s";
        }
        else if(kbs < 1000000.0)
        {
            kbs = kbs/1000.0;
            return formatter.format(kbs)+" MB/s";
        }
        else
        {
            kbs = kbs/1000000.0;
            return formatter.format(kbs)+" GB/s";
        }
        
    }
    
    public static String hashFile(File file, String algorithm)
        throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        }
    }
    
    public static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
    
    public static void disableAll(boolean children,Component ... components)
    {
        _setEnabled(false,children,components);
    }
    
    public static void enableAll(boolean children,Component ... components)
    {
        _setEnabled(true,children,components);
    }
        
    public static void _setEnabled(boolean state,boolean children,Component[] components)
    {
        for(Component comp : components)
        {
            comp.setEnabled(state);
            if(children)
            {
                if(comp instanceof Container)
                {
                    _setEnabled(state,true,((Container) comp).getComponents());
                }
            }
        }
    }
}
