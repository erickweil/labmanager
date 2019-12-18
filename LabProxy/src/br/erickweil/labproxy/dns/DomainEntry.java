/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 *
 * @author Usuario
 */
public class DomainEntry{
    HashMap<String,DomainEntry> left_child;
    boolean blockchilds;
    public DomainEntry(boolean blockchilds)
    {
        this.blockchilds = blockchilds;
        left_child = new HashMap<>();
        
    }
    public DomainEntry get(String name)
    {
        return blockchilds ? null : left_child.get(name);
    }
    public void put(String name,DomainEntry domain)
    {
        left_child.put(name,domain);
    }
    
    public void putAll(String[] name, int off, boolean blockchilds)
    {
        if(off == -1)
        {
            this.blockchilds = true;
            if(!isLeaf())
            {
                //System.out.println(this.left_child.keySet().size()+" redundant blocks, replaced by:"+Arrays.toString(name));
                //for(String s : this.left_child.keySet())
                //{
                //    System.out.println(s);
                //}
                
            }
            this.left_child.clear();
        }
        else
        {
            if(this.blockchilds)
            {
                //System.out.println("redundant block:"+Arrays.toString(name));
                return;
            }
            
            DomainEntry child;
            if((child = left_child.get(name[off])) == null)
            {
                child = new DomainEntry(false);
                left_child.put(name[off], child);
            }

            child.putAll(name,off-1,blockchilds); 
        }
    }
    public boolean isLeaf(){return left_child.isEmpty();}
    
    public boolean contains(String name)
    {
        String[] n = trimDomain(name).split("\\.");
        if(n.length > 0)
        return contains(n,n.length-1);
        else return false;
    }
    
    public boolean contains(String[] name,int off)
    {
        DomainEntry d;
        if((d = left_child.get(name[off])) != null) // se foi encontrado o filho
        {
            if(off == 0) // este dominio
            {
                if(d.isLeaf())
                {
                    return true;
                }
                // se tem filhos, é mais específico que esse
                // ex: google.com
                // mas tem registrado o www.google.com
                else
                {
                    return false;
                }
            }
            else // um filho
            {
                // analisa se esse ja bloqueia
                if(d.blockchilds)
                {
                    return true;
                }
                // senão procura no filho
                else
                {
                    return d.contains(name,off-1);
                }
            }
        }
        else
        {
            //System.out.println(name.getLabelString(off)+" not blocked");
            return false;
        }
    }
    
    public boolean printcontains(String[] name,int off)
    {
        System.out.println("searching["+off+"]:"+name[off]);
        System.out.println("\t"+left_child.keySet());
        DomainEntry d;
        if((d = left_child.get(name[off])) != null) // se foi encontrado o filho
        {
            System.out.println("\tfound!");
            if(off == 0) // este dominio
            {
                if(d.isLeaf())
                {
                    System.out.println("\t\tleaf! blocked.");
                    return true;
                }
                // se tem filhos, é mais específico que esse
                // ex: google.com
                // mas tem registrado o www.google.com
                else
                {
                    System.out.println("\t\ttoo deep! not blocked.");
                    return false;
                }
            }
            else // um filho
            {
                // analisa se esse ja bloqueia
                if(d.blockchilds)
                {
                    System.out.println("\t\tblocked all childs! blocked.");
                    return true;
                }
                // senão procura no filho
                else return d.printcontains(name,off-1);
            }
        }
        else
        {
            System.out.println("\tnot found.");
            //System.out.println(name.getLabelString(off)+" not blocked");
            return false;
        }
    }
    
    public List<String> getAllDomains(List<String> domains,String parent)
    {
        if(domains == null) domains = new ArrayList<>();
        
        if(blockchilds) domains.add("."+parent);
        else if(this.isLeaf()) domains.add(parent);
        else
        {
            for(String domain : left_child.keySet())
            {
                left_child.get(domain).getAllDomains(domains, domain+ ( parent == null ? "" : "."+parent));
            }
        }
        
        return domains;
    }
    
    public static String trimDomain(String entry)
    {
        entry = entry.trim();
        
        if(entry.startsWith("*."))
        {
            entry = entry.substring(2);
        }
        else if(entry.startsWith(".") || entry.startsWith("*"))
        {
            entry = entry.substring(1);
        }

        if(entry.endsWith(".") || entry.endsWith("*")) entry = entry.substring(0,entry.length()-1);
        else if(entry.endsWith(".*")) entry = entry.substring(0,entry.length()-2);
        
        return entry;
    }
}
