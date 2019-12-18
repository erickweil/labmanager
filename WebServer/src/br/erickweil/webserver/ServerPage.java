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
package br.erickweil.webserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import br.erickweil.webserver.pages.*;

public abstract class ServerPage {
	public ServerHttp server;
	public ServerPage(ServerHttp server)
	{
		this.server = server;
	}
	
    protected void echo(String message) throws IOException
    {
    	//System.out.println(message);
    	server.echo(message);
    }
	
	public abstract void get() throws IOException;
	public static void getPage(ServerHttp httpserver,String path) throws IOException
	{
		switch(path)
		{
			case "/":
			case "/index.java":
				httpserver.echoStatus(200);
				new Index(httpserver).get();
				break;
			//case "/proxy.java":
				//httpserver.echoStatus(200);
			//	new Proxy(httpserver).get();
			//	break;
			default:
				httpserver.echoStatus(404);
				new PageError(httpserver,404).get();
		}
	}
}
