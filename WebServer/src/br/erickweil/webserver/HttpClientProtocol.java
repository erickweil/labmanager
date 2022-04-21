/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.erickweil.webserver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Usuario
 */
public class HttpClientProtocol extends ServerProtocol{

    String host;
    String uri;
    HashMap<String,String> cookies;
    String method;
    String getpostData;
    String contentType;
    
    public HttpResponse response;
    public HttpClientProtocol(String host,String uri,HashMap<String,String> cookies,String method,String getpostData,String contentType) 
    {
        this.host = host;
        this.uri = uri;
        this.cookies = cookies;
        this.method = method;
        this.getpostData = getpostData;
        this.contentType = contentType;
    }
    
    public HttpClientProtocol(String host,String uri,HashMap<String,String> cookies,String method,String getpostData) 
    {
        this.host = host;
        this.uri = uri;
        this.cookies = cookies;
        this.method = method;
        this.getpostData = getpostData;
        this.contentType = "application/x-www-form-urlencoded";
    }

    @Override
    public void processRequest() throws IOException {
        response = httpReq(host,uri,cookies,method,getpostData,contentType);        
    }
    
    public HttpResponse httpReq(String host,String uri,HashMap<String,String> cookies,String method,String getpostData,String contentType) throws IOException
    {
        HttpRequest req = new HttpRequest();
        req.http_version = "1.1";
        req.method = method;
        req.uri = uri;    
        
        String urlParameters = "";
        if(uri.contains("?"))
        {
            urlParameters = uri.split("\\?")[1];
            uri = uri.split("\\?")[0];
        }
        else if(method.equals("GET")) urlParameters = getpostData;
        
        String postdata = getpostData;
        
        String reqString = method+" "+uri+(urlParameters.length() > 0 ? "?"+urlParameters : "")+" HTTP/1.1\r\n"+
        "Host: "+host+"\r\n"+
        "Connection: keep-alive\r\n"+
        (method.equals("POST") ? "Content-Length: "+postdata.length()+"\r\n" : "")+
        //"Origin: http://"+host+"\r\n"+
        (method.equals("POST") ? "Content-Type: "+contentType+"\r\n" : "")+
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36\r\n"+
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\r\n"+
        "Accept-Encoding: raw\r\n"+
        "Accept-Language: pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7\r\n"+
        (cookies.isEmpty() ? "" : "Cookie: "+Cookie.encodeCookies(cookies)+"\r\n")+
        "\r\n"+
        (method.equals("POST") ? postdata : "")
        ;
        
        byte[] requestBytes = reqString.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream inputBytes = new ByteArrayInputStream(requestBytes);
        DataInputStream inputRequest= new DataInputStream(inputBytes);
        
        req.buildfromInputStream(inputRequest);
        
        
        req.writeIntoOutputStream(output);
        output.flush();
        
        HttpResponse response = new HttpResponse();
        response.buildfromInputStream(input);
                
        List<String> Set_Cookie_headers = response.getHeaderValues("Set-Cookie");
        if(Set_Cookie_headers == null)
        {
            //System.out.println("erro ao carrgear cookies");
        }
        else
        {
            for(int i =0;i< Set_Cookie_headers.size();i++)
            {
                HashMap<String,String> param = Cookie.decodeCookies(Set_Cookie_headers.get(i));
                cookies.putAll(param);
                //System.out.println(Set_Cookie_headers.get(i));
            }
        }
        
        List<String> redirectHeader = response.getHeaderValues("Location");
        if(redirectHeader != null && redirectHeader.size() > 0)
        {
            String site = redirectHeader.get(0).replace("https://", "");
            host = site.substring(0,site.indexOf("/"));
            uri = site.substring(site.indexOf("/"),site.length());
            getpostData = "";
            if(uri.contains("?"))
            {
                getpostData = uri.substring(uri.indexOf("?")+1,uri.length());
                uri = uri.substring(0,uri.indexOf("?"));
            }
            //System.out.println("site -> '"+host+"','"+uri+"','"+getpostData+"'");
            return httpReq(host, uri, cookies, "GET", getpostData,contentType);
        }
        else
        return response;
    }
}