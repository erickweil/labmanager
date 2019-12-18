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

import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import br.erickweil.secure.SecureClient;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ReaderWriter;
import br.erickweil.webserver.ServerProtocol;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author Usuario
 */
public class Inicializador implements ProtocolFactory{
	public static String _endereco = "localhost";
	public static final int server_port = 22133;
	// 123456 hash
	public static String _serverpasshash = "$s0$100801$wpvi6RatmHTufOf0J11XFg==$nUUrwbIYPjQdC6pT/GCpSPtB9LIHhN+a5FYm2g80YbU=";
    public static void main(String[] args) throws InterruptedException
    {
        try {
            new Configurable(new SimpleLogger(),Inicializador.class, "config_inicializador");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        new Inicializador().connect();
    }
    
    private void connect() throws InterruptedException
	{
        while(true)
        {
            SecureClient client;
            client = new SecureClient(_endereco,server_port, this, "kcire.jks", "1a2b3c4d");
            client.run();
            
            Thread.sleep(10000);
        }
	}
    

	@Override
	public ServerProtocol get() {
		// TODO Auto-generated method stub
        return new ServerProtocol() {
            
            public void write(String msg) throws UnsupportedEncodingException, IOException
            {
                ReaderWriter.writeASCII(URLEncoder.encode(msg,"UTF-8")+"\n", output);
            }

            public String read() throws UnsupportedEncodingException, IOException
            {
                String txt = ReaderWriter.readASCIILine(input);
                if(txt == null || txt.isEmpty()) return txt;
                return URLDecoder.decode(txt,"UTF-8");
            }
            @Override
            public void processRequest() throws IOException {
                write("WAHAT??");
                write("INIT");
                String path = read();
                File labmanager = ProgramOpener.parsePath(path);
                if(labmanager.exists())
                {
                    write("OK");
                    ProgramOpener.start(path, null);
                    throw new RuntimeException("Saiu Normalmente do Inicializador");
                }
                else
                {
                    write("DOWNLOAD");
                }
            }
        };
	}
}
