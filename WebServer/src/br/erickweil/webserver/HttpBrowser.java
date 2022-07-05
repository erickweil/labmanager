/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.erickweil.webserver;

import br.erickweil.secure.SecureClient;
import java.net.URI;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 *
 * @author Usuario
 */
public class HttpBrowser {
    public static boolean LOG = true;
    public int retry;
    HashMap<String,String> cookies;
    public boolean ignoreCert;
    public HttpBrowser()
    {
        this.cookies = new HashMap<>();
        this.ignoreCert = false;
        this.retry = 0;
    }
    
    public void setRetryCount(int retry)
    {
        this.retry = retry;
    }
    
    public void disableCertValidation()
    {
        this.ignoreCert = true;
    }
    
    public static void main(String[] args) throws URISyntaxException, IOException
    {        
        //URI uri = new URI("a");
        /*parseURI("https://example.com/path/resource.txt#fragment");
        parseURI("//example.com/path/resource.txt");
        parseURI("/path/resource.txt");
        parseURI("path/resource.txt");
        parseURI("../resource.txt");
        parseURI("./resource.txt");
        parseURI("resource.txt");
        parseURI("#fragment");
        */
        
        HttpBase.LOG = true;
        HttpBrowser browser = new HttpBrowser();
        browser.disableCertValidation();
        
        System.out.println(browser.GET(new URI("https://virtual.ifro.edu.br/guajara/")));
    
    }
    
    
    public HttpResponse makeRequest(URI uri,String method,String postData,String contentType) 
            throws IOException
    {
        if(LOG)System.out.println(method+" "+uri.toString());
        
        final HttpRequest req = new HttpRequest();
        req.method = method;
        
        String txturi = uri.getPath();
        if(uri.getQuery() != null)  txturi += "?"+uri.getQuery();
        if(uri.getFragment()!= null)  txturi += "#"+uri.getFragment();
        
        if(txturi == null || txturi.isEmpty()) txturi = "/";
        
        req.uri = txturi;
        
        
        //GET / HTTP/1.1
        //Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
        //Accept-Encoding: gzip, deflate, br
        //Accept-Language: pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7
        //Cache-Control: no-cache
        //Connection: keep-alive
        //Cookie: _ga=GA1.3.1681645507.1612920096; _pk_id.3.5e32=54ce52f71e3f9f66.1630606232.; _pk_id.17.5e32=b95019a2c1d7af1c.1630606257.; _pk_ses.3.5e32=1
        //Host: virtual.ifro.edu.br
        //Pragma: no-cache
        //Sec-Fetch-Dest: document
        //Sec-Fetch-Mode: navigate
        //Sec-Fetch-Site: none
        //Sec-Fetch-User: ?1
        //Upgrade-Insecure-Requests: 1
        //User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36
        //sec-ch-ua: " Not A;Brand";v="99", "Chromium";v="101", "Google Chrome";v="101"
        //sec-ch-ua-mobile: ?0
        //sec-ch-ua-platform: "Windows"
        
        
        req.addHeader("Host", uri.getHost());
        req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        req.addHeader("Accept-Encoding","raw");
        req.addHeader("Accept-Language","pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
        req.addHeader("Cache-Control", "no-cache");
        req.addHeader("Pragma", "no-cache");
        
        //req.addHeader("Connection", "close");
        if(method.equals("POST"))
        {
            req.content = postData.getBytes(Charset.forName("UTF-8"));
            req.addHeader("Content-Length", ""+req.content.length);
            req.addHeader("Content-Type", contentType);
        }
        req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36");
        //req.addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"");
        //req.addHeader("sec-ch-ua-mobile", "?0");
        //req.addHeader("sec-ch-ua-platform", "Windows");
        
        // Cookies handled on the protocol...
        final HttpClientProtocol httpprotocol = new HttpClientProtocol(req,cookies);
        //httpprotocol.timeout = 10000; // 10s timeout
        
        String scheme = uri.getScheme();

        int port = uri.getPort();
        
        Runnable clientToRun;
        if(scheme.equals("https"))
        {
            if(port == -1) port = 443;
            SecureClient client = new SecureClient(uri.getHost(),port, 
            new ProtocolFactory() {
                @Override
                public ServerProtocol get() {
                    return httpprotocol;
                }
            }
            , null, null);
            client.LOG = true;
            if(ignoreCert)
            client.disableCertValidation();

            clientToRun = client;
        }
        else
        {
            if(port == -1) port = 80;
            WebClient client = new WebClient(uri.getHost(),port, 
            new ProtocolFactory() {
                @Override
                public ServerProtocol get() {
                    return httpprotocol;
                }
            }
            );
            
            clientToRun = client;
        }
        
        if(retry <= 0)
            clientToRun.run();
        else for(int i=0;i<retry;i++)
        {
            clientToRun.run();
            if(httpprotocol.response != null) break;
            
            if(LOG)System.out.println("Retrying...");
        }
        
        HttpResponse response = httpprotocol.response;
        
        return response;
    }
    
    public String GET(URI uri) throws IOException
    {
        HttpResponse response = makeRequest(uri, "GET",null,null);
        
        if(response == null)
        {
            throw new IOException("Erro Resposta nula");
        }
        
        String responseText = response.getResponseAsText();
        if(responseText == null || responseText.trim().isEmpty())
            return "";
        
        return responseText;
    }
    
    public String POST(URI uri,String postData,String contentType) throws IOException
    {
        HttpResponse response = makeRequest(uri, "POST",postData,contentType);
        
        if(response == null)
        {
            throw new IOException("Erro Resposta nula");
        }
        
        String responseText = response.getResponseAsText();
        if(responseText == null || responseText.trim().isEmpty())
            return "";
        
        return responseText;
    }
    
    
    public static URI parseURI(String site)
    {
        /*// URI = scheme ":" ["//" authority] path ["?" query] ["#" fragment]
        // http://www.google.com/?q=333
        String scheme = null;
        String authority = null;
        String path = null;
        String query = null;
        String fragment = null;
        
        int iendScheme = site.indexOf(':');
        int istartAuthority;
        if(iendScheme != -1)
        {
            scheme = site.substring(0, iendScheme);
            
        }
        
        istartAuthority = iendScheme+3;
        
        int iendAuthority;
        // contains authority
        if(istartAuthority <= site.length() && site.substring(iendScheme+1, istartAuthority).equals("//"))
        {
            iendAuthority = site.indexOf('/',istartAuthority);
            if(iendAuthority == -1) iendAuthority = site.indexOf('?',istartAuthority);
            if(iendAuthority == -1) iendAuthority = site.indexOf('#',istartAuthority);
            if(iendAuthority == -1) iendAuthority = site.length();
        }
        else
        {
            iendAuthority = iendScheme+1;
        }
        
        if(iendAuthority > istartAuthority)
            authority = site.substring(istartAuthority, iendAuthority);
        
        int iendPath = site.indexOf('?',iendAuthority);
        if(iendPath == -1) iendPath = site.indexOf('#',iendAuthority);
        if(iendPath == -1) iendPath = site.length();
        
        if(iendPath > iendAuthority)
            path = site.substring(iendAuthority,iendPath);
        
        int iendQuery = site.indexOf('#',iendPath);
        if(iendQuery == -1) iendQuery = site.length();
        
        if(iendQuery > iendPath+1)
            query = site.substring(iendPath+1,iendQuery);
        
        if(site.length() > iendQuery+1)
            fragment = site.substring(iendQuery+1,site.length());
        
        */
        
        
        URI uri =  URI.create(site);
        
        System.out.println(site);
        
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        String path = uri.getPath();
        String query = uri.getQuery();
        String fragment = uri.getFragment();
        
        if(scheme != null) System.out.print(" Scheme:"+scheme);
        if(authority != null) System.out.print(" Authority:"+authority);
        if(path != null) System.out.print(" Path:"+path);
        if(query != null) System.out.print(" Query:"+query);
        if(fragment != null) System.out.print(" Fragment:"+fragment);
        System.out.println("\n");
        
         return uri;
    }
}
