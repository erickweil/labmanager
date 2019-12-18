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
package br.erickweil.redirectport;

import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;

public class Teste extends Thread{
    
    public int server_port;
    public String dest_host;
    public int dest_port;
    private final Runnable onClientChanged;
    public Teste(int server_port, String dest_host, int dest_port, Runnable onClientChanged)
    {
        this.server_port = server_port;
        this.dest_host = dest_host;
        this.dest_port = dest_port;
        this.onClientChanged = onClientChanged;
    }
    
	public static void main(String[] args) {
        asyncRun(25565, "portquiz.net", 80, null);
	}
    
    public static Teste asyncRun(int server_port, String dest_host, int dest_port, Runnable r)
    {
        Teste t = new Teste(server_port,dest_host,dest_port,r);
        t.start();
        
        return t;
    }

    public WebServer server;
    @Override
    public void run() {
        // TODO Auto-generated method stub
		System.out.println("Iniciando Server Kcire...");
		
		server = new WebServer(server_port,new ProtocolFactory() {
			@Override
			public ServerProtocol get() {
				// TODO Auto-generated method stub
				return new RedirectTCP(dest_host,dest_port,onClientChanged);
			}
		});
		try {
			server.run();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Parando Server Kcire...");
			server.stop();
		}
    }
    
    public void onStop()
    {
        if(server != null)
        server.stop();
    }
    
}
