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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import br.erickweil.redirectport.RedirectTCP;
import br.erickweil.webserver.pages.PageError;

public class ServerHttpProxy extends ServerHttp{
	private static final int TIMEOUT = 10000;
	public HttpRequest Request;
	public HttpResponse Response;
	public DataOutputStream response_output;
	public String localhost = "177.201.79.199";
	public int localport = 8080;
	public String hostip = "radiopovo87fm.com.br";
	public String hostname = "radiopovo87fm.com.br";
	public int port = 80;
	public ServerHttpProxy(String hostip,String hostname, int port) {
		// TODO Auto-generated constructor stub
		this.hostip = hostip;
		this.hostname = hostname;
		this.port = port;
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

		socket.setSoTimeout(TIMEOUT);
		
		Request = new HttpRequest();
		boolean sucesso = Request.buildfromInputStream(input);
		if(!sucesso) return;
	
		//System.out.println("->Processando Resposta...\n");

		String host_header = Request.getHeader("Host");
		String hostname_real = null;
		int hostport_real = 0;
		if(host_header != null)
		{
			if(host_header.contains("http://") || host_header.contains("https://"))
			{
				Request.uri = host_header;
			}
			else if(host_header.contains(":"))
			{
				hostname_real = host_header.split(":")[0]; 
				hostport_real = Integer.parseInt(host_header.split(":")[1]);		
			}
			else
			{
				hostname_real = host_header; 
				hostport_real = 80;	
			}
		}
		
		if(Request.method.equalsIgnoreCase("POST") && Request.content!= null && Request.content.length >0)
		{
			System.out.println("Post content->"+URLDecoder.decode(new String(Request.content,Charset.forName("US-ASCII")),"UTF-8"));
		}
		
		Socket proxysocket;
		boolean proxy_request; 
		
		if(host_header != null && host_header.equals("kcire.ddns.net:8080"))
		{
			proxy_request = false;
		}
		else
		{
			proxy_request = true;
		}
		
		if(Request.method.equals("CONNECT"))
		{
			if(Request.uri.contains(":"))
			{
				String proxydomain = Request.uri.split(":")[0];
				int proxyport = Integer.parseInt(Request.uri.split(":")[1]);
				System.out.println("CONNECT host:"+proxydomain+" port:"+proxyport);
				System.out.println("");
				ReaderWriter.writeASCII("HTTP/1.1 200 OK"+HttpBase.CRLF+HttpBase.CRLF, output);
				output.flush();
			    RedirectTCP redirectTCP = new RedirectTCP(proxydomain, proxyport,null);
			    redirectTCP.input = input;
			    redirectTCP.output = output;
				redirectTCP.socket = socket;
				redirectTCP.charset = charset;
				redirectTCP.secure = false;
			    redirectTCP.processRequest();
				return;
			}
			else{
				
				System.out.println("Não sei o que fazer com"+Request.method+" "+Request.uri+" "+Request.http_version);
				return;
			}
		}
		else if(Request.method.equals("GET") || Request.method.equals("POST"))
		{
			if(Request.uri.startsWith("http://") || Request.uri.startsWith("https://"))
			{
				int index_domainstart =Request.uri.indexOf('/')+2;
				int index_domainend = Request.uri.indexOf('/',index_domainstart);
				if(index_domainend == -1) index_domainend = Request.uri.length();
				int index_uristart = index_domainend;
				int index_uriend = Request.uri.length();
				
				String proxy_domain = Request.uri.substring(index_domainstart, index_domainend);
				int proxy_port = Request.uri.startsWith("https://") ? 443 : 80;
				if(proxy_domain.contains(":"))
				{
					String[] domain_port_split = proxy_domain.split(":");
					proxy_domain = domain_port_split[0];
					proxy_port = Integer.parseInt(domain_port_split[1]);
				}
				
				if(index_domainend != Request.uri.length())
				Request.uri =  Request.uri.substring(index_uristart, index_uriend);
				else
				Request.uri = "/";
				//HttpRequest ProxyRequest = new HttpRequest();
				Request.method = Request.method;
				//Request.uri = Request.uri;
				if(proxy_port == 80 || proxy_port == 443)
					Request.setHeader("Host",proxy_domain);
				else	
					Request.setHeader("Host",proxy_domain+":"+proxy_port);
				Request.setHeader("Connection", "close");
				//Request.setHeader("Accept", "*/*");
				//Request.setHeader("Accept-Encoding", "identity");
				//Request.setHeader("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
				Request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");

				
				/*if(Request.getHeader("Accept") != null && Request.getHeader("Accept").contains("image"))
				{
					//proxy_domain ="thecatapi.com";
					//proxy_port = 80;
					//Request.uri = "/api/images/get?format=src&type=gif";
					//https://www.petdrugsonline.co.uk/images/page-headers/cats-master-header
					proxy_domain = "www.petdrugsonline.co.uk";
					Request.uri = "/images/page-headers/cats-master-header";
					proxy_port = 443;
					Request.setHeader("Host",proxy_domain);
					if(Request.getHeader("Referer") != null) Request.setHeader("Referer", proxy_domain);
					if(Request.getHeader("Origin") != null)	Request.setHeader("Origin", proxy_domain);
					
				}*/
				
				
				if(proxy_port == 443)
				{
				    proxysocket = SSLSocketFactory.getDefault().createSocket(proxy_domain, proxy_port);
				}
				else
				{
					proxysocket = new Socket(proxy_domain, proxy_port);
				}
			}
			else if(proxy_request)
			{
				String proxydomain = host_header != null? hostname_real : null;
				if(proxydomain != null)
				{
					if(hostport_real == 443)
					{
					    proxysocket = SSLSocketFactory.getDefault().createSocket(proxydomain, hostport_real);
					}
					else
					{
						proxysocket = new Socket(proxydomain, hostport_real);
					}
				}
				else
				{
					System.out.println("Não sei o que fazer sem um HOST");
					return;
				}
			}
			else
			{
				
				//Request.headers.put("Accept","text/html");
				//Request.headers.put("Accept-Encoding","identity");
				
				String uri = Request.uri;
				String protocol_expected = (port == 443? "https" : "http");
				String host_expected = hostname+(port != 80 && port != 443 ? ":"+port : "");
				String url_expected = protocol_expected+"://"+host_expected+"/";
				
				//Request.setHeader("User-Agent", "Kcire Server");
				
				if(port == 80 || port == 443)
					Request.setHeader("Host",hostname);
				else	
					Request.setHeader("Host",hostname+":"+port);
				
				if(Request.getHeader("Referer") != null)
				{
					Request.setHeader("Referer", url_expected);
					//System.out.println("################## "+url_expected+" ####################");
				}
				if(Request.getHeader("Origin") != null)
					Request.setHeader("Origin", url_expected);
				
				Request.delHeader("Upgrade-Insecure-Requests");
				
				if(port == 443)
				{
				    proxysocket = SSLSocketFactory.getDefault().createSocket(hostip, port);
				}
				else
				{
					proxysocket = new Socket(hostip, port);
				}
			}
		}
		else
		{
			System.err.println("Request desconhecido:"+Request.method);
			return;
		}
		
		try {
			DataInputStream socketinput  = new DataInputStream(proxysocket.getInputStream());
			DataOutputStream socketoutput = new DataOutputStream(proxysocket.getOutputStream());
			
			Request.writeIntoOutputStream(socketoutput);
			
			System.out.println("->Recebendo Request...-->\n");
			Response = new HttpResponse();
			Response.buildfromInputStream(socketinput);
			
			
			if(!proxy_request)
			{	
				Response.delHeader("content-security-policy");
				Response.delHeader("X-Frame-Options");
				
				if(Response.status_code.equals("301") || Response.status_code.equals("302"))// não envia a resposta de redirecionamento
				{
					System.out.println("->Enviando Resposta de Erro...\n");
					
					HttpResponse newResponse = new HttpResponse();
					response_output = newResponse.getcontentOutputStream();
					new PageError(this,Integer.parseInt(Response.status_code)).get();
					newResponse.status_code = "500";
					newResponse.reason_frase = "Internal Server Error";
					newResponse.writeIntoOutputStream(output);
					return;
				}
				
	
				List<String> Set_Cookie_headers = Response.getHeaderValues("set-cookie");
				if(Set_Cookie_headers != null)
				{
					for(int i =0;i< Set_Cookie_headers.size();i++)
					{
						HashMap<String,String> param = Cookie.decodeCookies(Set_Cookie_headers.get(i));
						if(param.containsKey("domain")) 
						{
							param.put("domain", hostname_real);	
						}
						param.remove("secure");
						
						Set_Cookie_headers.set(i, Cookie.encodeCookies(param));
					}
					Response.setHeader("set-cookie", Set_Cookie_headers);
				}
			}
			System.out.println("->Enviando Resposta...\n");

			Response.writeIntoOutputStream(output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			proxysocket.close();
		}
		//new Proxy(this).get();
	}
}
