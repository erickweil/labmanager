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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe para receber as mensagens de erro, que serão exibidas no console de erros do servidor
 * @author Usuario
 */
public class ErrStreamReceiver extends OutputStream{
    
    byte[] buffer;
    int bc;
    public static final int MAX_BUFFER_SIZE = 134_217_727; // 134 mb
    private final PrintStream old_stream;
    public final List<String> lines;
    public ErrStreamReceiver(PrintStream oldStream) {
        old_stream = oldStream;
        buffer = new byte[2048];
        lines = new ArrayList<>();
    }
    
    public void doubleBuffer() throws IOException
    {
        if(buffer.length >= MAX_BUFFER_SIZE)
            throw new IOException("IMPEDINDO A DUPLICAÇAO DO BUFFER, CHEGOU AO TAMANHO MAXIMO");
        byte[] old_buffer = buffer; 
        buffer = new byte[old_buffer.length*2];
        System.arraycopy(old_buffer, 0, buffer, 0, old_buffer.length);
        System.out.println("DOBROU PARA "+buffer.length);
    }

    @Override
    public void flush() throws IOException {
        if(bc == 0 || (bc == 2 && (buffer[0] == '\r' && buffer[1] == '\n'))){
            bc = 0;
        }
        else
        {
            byte[] flushed = new byte[bc];
            System.arraycopy(buffer, 0, flushed, 0, bc);
            String str = new String(flushed);
            lines.add(str);
            old_stream.println(str);
            //System.out.println(lines.toString());
            bc=0;
        }
    }
    
    

    @Override
    public void write(byte[] b) throws IOException {
        try
        {
            while(buffer.length <= (bc + b.length))
            {
                doubleBuffer();
            }
            System.arraycopy(b, 0, buffer, bc, b.length);
            bc += b.length;
            //old_stream.print(new String(b));
        }
        catch(Exception e)
        {
            e.printStackTrace(old_stream);
        }
    }

    
    @Override
    public void write(int b) throws IOException {
        try
        {
            if(buffer.length <= bc)
            {
                doubleBuffer();
            }
            buffer[bc] = (byte)b;
            bc++;
            //old_stream.print((char)b);
        }
        catch(Exception e)
        {
            e.printStackTrace(old_stream);
        }
    }
    
    
}
