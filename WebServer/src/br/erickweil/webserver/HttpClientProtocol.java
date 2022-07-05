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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Usuario
 */
public class HttpClientProtocol extends ServerProtocol{

    HashMap<String,String> cookies;

    public HttpRequest request;
    public HttpResponse response;
    
    public HttpClientProtocol(HttpRequest request,HashMap<String,String> cookies)
    {
        this.request = request;
        this.cookies = cookies;
    }
    
    public HttpClientProtocol(String host,String uri,HashMap<String,String> cookies,String method,String getpostData,String contentType) 
    {
        this.cookies = cookies;   
        initReq(host,uri,method,getpostData,contentType);
    }
    
    public HttpClientProtocol(String host,String uri,HashMap<String,String> cookies,String method,String getpostData) 
    {
        this.cookies = cookies;
        initReq(host,uri,method,getpostData,"application/x-www-form-urlencoded");
    }
    
    private void initReq(String host, String uri, String method, String getpostData,String contentType)
    {
        request = new HttpRequest();
        HttpRequest req = request;
        
        req.http_version = "1.1";
        req.method = method;
        req.uri = uri;    
        
        String urlParameters = "";
        if(uri.contains("?"))
        {
            urlParameters = uri.split("\\?")[1];
            uri = uri.split("\\?")[0];
        }
        else if(method.equals("GET")) urlParameters = getpostData == null ? "" : getpostData;
        
        
        
        String postdata = getpostData;
        
        String reqString = method+" "+uri+(urlParameters.length() > 0 ? "?"+urlParameters : "")+" HTTP/1.1\r\n"+
        "Host: "+host+"\r\n"+
        "Connection: keep-alive\r\n"+
        (method.equals("POST") ? "Content-Length: "+postdata.length()+"\r\n" : "")+
        //"Origin: http://"+host+"\r\n"+
        (method.equals("POST") ? "Content-Type: "+contentType+"\r\n" : "")+
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36\r\n"+
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
        
        try {
            req.buildfromInputStream(inputRequest);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void processRequest() throws IOException {
        //response = httpReq(host,uri,cookies,method,getpostData,contentType);        
    
        if(!cookies.isEmpty())
        {
            request.delHeader("Cookie");
            request.addHeader("Cookie",Cookie.encodeCookies(cookies));
        }
        
        request.writeIntoOutputStream(output);
        output.flush();
        
        response = new HttpResponse();
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
                Cookie.decodeSetCookie(Set_Cookie_headers.get(i),cookies);
                //cookies.putAll(param);
                //System.out.println(Set_Cookie_headers.get(i));
            }
        }
        
        /*List<String> redirectHeader = response.getHeaderValues("Location");
        if(this.autoRedirect && redirectHeader != null && redirectHeader.size() > 0)
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
            if(HttpBase.LOG)System.out.println("site -> '"+host+"','"+uri+"','"+getpostData+"'");
            return httpReq(host, uri, cookies, "GET", getpostData,contentType);
        }
        else
        return response;*/
    }
}