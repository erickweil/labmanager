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
package br.erickweil.portugolserver;

import java.util.concurrent.TimeoutException;

public class PortugolSerialTest {
	public static final String file_input = "D:\\Documentos - D\\pasta do erick\\projetos\\portugol\\input.txt";
	public static final String file_output = "D:\\Documentos - D\\pasta do erick\\projetos\\portugol\\output.txt";
	public static void main(String[] args) throws TimeoutException {
		// TODO Auto-generated method stub
		PortugolConn conn = new PortugolConn(file_input, file_output);
		
		while(true)
		{
			String msg = conn.ler_mensagem();
	
			System.out.println(msg);
			conn.enviar_mensagem("PONG");
		}
	}

}
