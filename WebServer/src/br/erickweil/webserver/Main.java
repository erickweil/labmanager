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

import java.io.IOException;
import java.net.Socket;

import br.erickweil.redirectport.RedirectTCP;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        int porta = 2020;
		System.out.println("Iniciando Server Kcire... porta:"+porta);
		
		WebServer server = new WebServer(porta,new ProtocolFactory() {
			@Override
			public ServerProtocol get() {
				// TODO Auto-generated method stub
				return new ServerHttpProxy("151.101.194.33","scratch.mit.edu",443);
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
	
	public static boolean mandouParar()
	{
		return false;
	}

}
