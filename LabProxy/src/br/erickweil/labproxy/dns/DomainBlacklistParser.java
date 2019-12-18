/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import static br.erickweil.labproxy.dns.DomainEntry.trimDomain;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.xbill.DNS.Name;

/**
 *
 * @author Usuario
 */
public class DomainBlacklistParser {
    public static DomainEntry getBlacklist(File file) throws IOException
    {
        String file_txt = new String(Files.readAllBytes(file.toPath()),Charset.forName("UTF-8"));
        String[] file_lines = file_txt.split("\n");
        
        DomainEntry root = new DomainEntry(false);
        
        for(int i=0;i<file_lines.length;i++)
        {
            try
            {
                String entry = file_lines[i].trim();
                boolean blockchilds = false;
                if(entry.startsWith("*."))
                {
                    blockchilds = true;
                    entry = entry.substring(2);
                }
                else if(entry.startsWith(".") || entry.startsWith("*"))
                {
                    blockchilds = true;
                    entry = entry.substring(1);
                }

                if(entry.endsWith(".") || entry.endsWith("*")) entry = entry.substring(0,entry.length()-1);
                else if(entry.endsWith(".*")) entry = entry.substring(0,entry.length()-2);

                String[] entry_part = entry.split("\\.");

                root.putAll(entry_part, entry_part.length-1, blockchilds);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
        try
        {
            List<String> domains = root.getAllDomains(null, null);
            System.out.println("loaded "+file_lines.length+" inputs, total domains list:"+domains.size());
            Files.write(new File("domainsoutput.txt").toPath(), domains);

            System.out.println("testing...");
            int passcount = 0;
            for(int i=0;i<file_lines.length;i++)
            {
                if(!root.contains(file_lines[i]))
                {
                    if(passcount < 5)
                    {
                        String[] n = trimDomain(file_lines[i]).split("\\.");
                        boolean b = root.printcontains(n, n.length-1);
                        System.out.println(b);
                    }
                    if(passcount < 100)
                        System.out.println(DomainEntry.trimDomain(file_lines[i])+" passou!");
                    passcount++;
                }
            }
            System.out.println(passcount+"/"+file_lines.length+" passaram!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return root;
    }
}
