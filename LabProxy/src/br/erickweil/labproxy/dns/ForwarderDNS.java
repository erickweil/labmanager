/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.xbill.DNS.Address;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 *
 * @author Usuario
 */
public class ForwarderDNS {
    public static void main(String[] args) throws UnknownHostException, TextParseException
    {
        // http://www.xbill.org/dnsjava/dnsjava-current/examples.html
        String site = "jw.org";
        String dns_server = "8.8.8.8";
        
        for(int i=0;i<10;i++)
        {
            try{
            System.out.println("");
            InetAddress addr = Address.getByName(site);
            System.out.println(addr.getHostAddress());
            } catch(Exception e){e.printStackTrace();}

            try{
            Lookup l = new Lookup(site);//, Type.TXT, DClass.CH);
            l.setResolver(new SimpleResolver(dns_server));
            Record[] records = l.run();
            if (records != null && records.length > 0)
                    System.out.println(l.getAnswers()[0].rdataToString());
            else
                System.out.println(l.getErrorString());
            } catch(Exception e){e.printStackTrace();}
        }
    }
}
