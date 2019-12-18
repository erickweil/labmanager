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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Scanner;

import br.erickweil.webserver.ProtocolFactory;
import br.erickweil.webserver.ServerHttp;
import br.erickweil.webserver.ServerPageManager;
import br.erickweil.webserver.ServerProtocol;
import br.erickweil.webserver.WebServer;
import br.erickweil.webserver.pages.Index;
import br.erickweil.webserver.pages.PageError;

public class PortugolServer {
	public static final String file_input = "D:\\Documentos - D\\pasta do erick\\projetos\\portugol\\input.txt";
	public static final String file_output = "D:\\Documentos - D\\pasta do erick\\projetos\\portugol\\output.txt";
	/*public static void main2(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		int num_linhas = 0;
		while(true)
		{
			String msg = sc.nextLine();
			String text = num_linhas+"\n"+msg+"\nFIM";
			Writetxt(file_input,text);
			Thread.sleep(100);
			String resposta = Readtxt(file_output);
			System.out.println(resposta);
			num_linhas++;
		}
	}*/
	static int num_linhas = 0;
	public static void main(String[] args) {
			// TODO Auto-generated method stub
			System.out.println("Iniciando Server Kcire...");
			
			WebServer server = new WebServer(8080,new ProtocolFactory() {
				@Override
				public ServerProtocol get() {
					// TODO Auto-generated method stub
					return new ServerHttp( new ServerPageManager() {
						
						@Override
						public void getPage(ServerHttp httpserver, String path) throws IOException {
							managePage(httpserver, path);
						}
					});
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
	
	public static void managePage(ServerHttp httpserver, String path) throws IOException
	{
		try {
			
			
			switch(path)
			{
				case "/":
				case "/index.java":
					httpserver.echoStatus(200);
					httpserver.echo(sendRequest("GET PAGINA"));
					break;
				//case "/proxy.java":
					//httpserver.echoStatus(200);
				//	new Proxy(httpserver).get();
				//	break;
				default:
					httpserver.echoStatus(404);
					new PageError(httpserver,404).get();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	public static String sendRequest(String request) throws IOException, InterruptedException
	{
		String text = num_linhas+"\n"+request+"\nFIM";
		Writetxt(file_input,text);
		Thread.sleep(100);
		String resposta = Readtxt(file_output);
		resposta = resposta.substring(resposta.indexOf('\n'),resposta.lastIndexOf('\n')-4);
		System.out.println(resposta);
		num_linhas++;
		return resposta;

	}

	
	public static void Writetxt(String path,String content)
	{

		try {
			//content = new String(Files.readAllBytes(Paths.get(path)));
			Files.write(new File(path).toPath(), content.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String Readtxt(String path)
	{
		String content=null;

		try {
			//content = new String(Files.readAllBytes(Paths.get(path)));
			File arquivo = new File(path);
			if(arquivo.exists())
			{
				content = new String(Files.readAllBytes(arquivo.toPath()),Charset.forName("Cp1252"));
			}
			else
			{
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content+" ";
	}
}
