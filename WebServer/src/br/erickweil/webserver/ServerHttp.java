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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import br.erickweil.webserver.pages.PageError;
import br.erickweil.webserver.pages.Proxy;

public class ServerHttp extends ServerProtocol {

	public HttpRequest Request;
	public HttpResponse Response;
	public DataOutputStream response_output;
	public ServerPageManager pagemanager;
	public ServerHttp()
	{
		
	}
	
	public ServerHttp(ServerPageManager pagemanager)
	{
		this.pagemanager = pagemanager;
	}
	
	public void echo(String txt) throws IOException
	{
		System.out.print(txt.replace("\n","\n--> "));
		ReaderWriter.write(txt, response_output,charset);
	}
	
    public void echoStatus(int status) throws IOException
    {
    	String statusString = "";
    	switch(status)
    	{
    		case 200: statusString= "OK"; break;
    		case 404: statusString= "Not Found"; break;
    	}
    	Response.status_code = ""+status;
    	Response.reason_frase = statusString;
    }

	@Override
	public void processRequest() throws IOException {
		// TODO Auto-generated method stub

		Request = new HttpRequest();
		boolean sucesso = Request.buildfromInputStream(input);
		if(!sucesso) return;
	
		System.out.print("->Processando Resposta...\n--> ");
		
		Response = new HttpResponse();
		response_output = Response.getcontentOutputStream();
		if(pagemanager == null)
		ServerPage.getPage(this,Request.uri);
		else
		pagemanager.getPage(this,Request.uri);
		
		System.out.println("->Enviando Resposta...");
		
		Response.writeIntoOutputStream(output);
		//new Proxy(this).get();
	}

}
