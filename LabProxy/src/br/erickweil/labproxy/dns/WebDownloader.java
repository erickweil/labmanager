/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import br.erickweil.webserver.HttpRequest;
import java.io.File;

/**
 *
 * @author Usuario
 */
public class WebDownloader {
    public static void download(String host,String url,File dest)
    {
        HttpRequest request = new HttpRequest();
        request.method = "GET";
        request.uri = url;
        request.http_version = "1.1";
        
        /*request.addHeader("Host", host);
        request.addHeader("Connection"," keep-alive
        request.addHeader("Cache-Control"," max-age=0
        request.addHeader("Upgrade-Insecure-Requests"," 1
        request.addHeader("User-Agent"," Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36
        request.addHeader("Accept"," text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,/;q=0.8
        request.addHeader("Accept-Encoding"," gzip, deflate
        request.addHeader("Accept-Language"," pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7*/
    }
}
