/*
 * Copyright (C) 2019 Usuario
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
package br.erickweil.labmanager.client.slave;

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labmanager.client.ClientApp;
import br.erickweil.labmanager.client.ClientCmdProtocol;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import br.erickweil.labmanager.threadsafeness.ThreadSafeHandler;
import br.erickweil.webserver.ServerProtocol;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Usuario
 */
public class SlaveAsyncProtocol extends ServerProtocol {

       /**
     * Aqui que é processado a comunicação de um cliente.
     * 
     * é lido a primeira linha, que indica a versão.<br/>
     * atualmente a versão aceita é 21.01.18.15.43<br/>
     * 
     * <p>de acordo com a versão é processado as requisições e 
     * esse método não retorna até que aconteça algum erro na
     * comunicação ou o cliente desconecte.<p>
     * 
     * 
     * @throws IOException
     */
    @Override
    public void processRequest() throws IOException {    
    	try
    	{
            if(!socket.getInetAddress().isLoopbackAddress())
            {
                System.err.println("cliente tentando conectar de fora");
                return;
            }
            
            process();

	}
        catch (SocketException | EOFException e){
    		System.out.println("Cliente Desconectou:"+e.getMessage());
    	}
        catch (IOException e){
    		System.out.println("Erro ao Desconectar:"+e.getMessage());
                e.printStackTrace();
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    	finally {
            
        }
    }
    
    
    
    private void process() throws IOException, ParseException {
        String action = readln_url();
        switch(action)
        {
            case "windowlist":
                List<CmdProcess> windows = CmdExec.running_windows();
                writeln_url(""+windows.size());
                for(CmdProcess c : windows)
                {
                    writeln_url(CmdProcess.toJSON(c).toJSONString());
                }
                break;
        }
        
        writeln_url("Bye");
    } 
}
