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
package br.erickweil.reverseserver;

import br.erickweil.redirectport.RedirectTCP;
import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;

public class Teste {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Iniciando Server Kcire...");
		
		WebServer server = new WebServer(8080,new ProtocolFactory() {
			@Override
			public ServerProtocol get() {
				// TODO Auto-generated method stub
				return new ReverseServerProxy(8081,false,true,60000);
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

}
