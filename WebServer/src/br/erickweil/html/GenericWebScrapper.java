/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.erickweil.html;

import br.erickweil.webserver.HttpBase;
import br.erickweil.webserver.HttpBrowser;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

/**
 * Classe que lê páginas HTML e processa o seu conteúdo de forma a encontrar elementos específicos, seguindo
 * regras pre-definidas de forma genérica.
  
 CLASSE COMPLETAMENTE INÚTIL PQ O SELECT DO JSOUP JÁ FAZ TUDO ISSO
 */
public class GenericWebScrapper {
    
    /*
    Tag:
    z --> indica qualquer
    
    Atributo:
    z --> indica qualquer
    dataz --> todos que começam com data
    
    attributos especiais
    z-index --> valor indica qual deve ser o indice do elemento na lista do parente
                Se começar com + o indice deve ser maior que
                Se começar com - o indice deve ser menor que
    
    
    Valor do atributo:
    class=".*topo.*" --> regex no valor do atributo
    */
    
    
    public static void main(String[] args) throws URISyntaxException, IOException
    {
        String testHTML = 
"<!DOCTYPE html>\n" +
"<html>\n" +
"<head>\n" +
"				<meta charset=\"utf-8\">\n" +
"		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
"		\n" +
"		<link rel=\"stylesheet\" href=\"/marmitaria/css/layout.css\">\n" +
"		\n" +
"				<link rel=\"stylesheet\" href=\"/marmitaria/css/modal.css\">\n" +
"				\n" +
"		<title>Casa Das Marmitas</title>\n" +
"		<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n" +
"		<!--[if lt IE 9]>\n" +
"			<script src=\"http://html5shiv.googlecode.com/svn/trunk/html5.js\"></script>\n" +
"		<![endif]-->\n" +
"	</head>\n" +
"<body>\n" +
"	<ul class=\"topo\" id=\"barratopo\">\n" +
"	\n" +
"					<li class=\"col right\">\n" +
"				<a href=\"/marmitaria/login/fazerlogin.php\">LOGIN</a>\n" +
"			</li>\n" +
"				</ul>\n" +
"	<div align=\"center\">\n" +
"	<img src=\"img/logotipo.png\"></img>\n" +
"	<h1><a href=\"/marmitaria/login/fazerlogin.php\">Você Precisa se Autenticar para continuar</a></h1>\n" +
"	<br/>\n" +
"	<br/>\n" +
"	<br/>\n" +
"	<h3> Produto desenvolvido pela empresa Kcire LTDA® <br/></h3>\n" +
"	<h3> Obrigado pela sua escolha! </h3><br/>\n" +
"	<br/>\n" +
"	\n" +
"	\n" +
"</div>\n" +
"\n" +
"\n" +
            "<a href=\"aaa\">aaa</a>\n"+
            "	<br/>\n" +
            "	<br/>\n" +
"	        <!-- BLOCO RODAPE -->\n" +
"			<img class=\"right\" src=\"/marmitaria/img/logo_pequeno.png\"></img> \n" +
"			<script src=\"/marmitaria/js/layout.js\"></script>\n" +
"			<script src=\"/marmitaria/js/util.js\"></script>\n" +
"	</body>\n" +
"</html>";
        //String testModel = "<table><tr class=\".*discussion.*\" > "
        //    + "<th class=\".*topic.*\"><a>TITULO</a></th>"
        //    + "<td class=\".*author.*\"><div class=\".*author-info.*\"><div class=\".*text\\-truncate.*\">AUTOR</div></div></td> "
        //    + "</tr></table>";
        
        String testModel = "<z z-index=\"0\"><z z-index=\"0\" /></z>";
        
        //testHTML = new String(
        //    Files.readAllBytes(new File("C:\\Users\\Usuario\\Downloads\\forum.html").toPath()),
        //    "UTF-8");
        

        //HttpBrowser browser = new HttpBrowser();
        
        
        //HttpBase.LOG = true;
        //testHTML = browser.GET(new URI("https://scratch.mit.edu/"));
        //testHTML = "";
        
        
        //System.out.println(testHTML);
        
        Document doc = Jsoup.parse(testHTML);
        //Document doc  = Jsoup.connect("https://www.reddit.com/").get();
        
        Element model = Jsoup.parse(testModel).body();
        
        
        printChildNodes(model);
        
        long timeStart = System.currentTimeMillis();
        HashMap<String,Elements> resultMap = new HashMap<>();
        Elements elems = scrap(doc,model.children(),resultMap);
        
        System.out.println("Millis:"+(System.currentTimeMillis() - timeStart));
        
        
        //Elements titulos = resultMap.getOrDefault("TITULO",null);
        //Elements autores = resultMap.getOrDefault("AUTOR",null);
        
        //if(titulos != null && autores != null)
        //for(int i=0;i<titulos.size();i++)
        //{
        //    System.out.println(titulos.get(i).ownText()+" --> "+autores.get(i).ownText());
        //}
        
        if(elems != null)
        {
            System.out.println("Matched "+elems.size());
            for(Element e : elems)
            {
                System.out.println(e.tagName() + " --> "+ e.ownText());
            }
        }
        else
        {
            System.out.println("null...");
        }
    }
    
    public static void printChildNodes(Element e)
    {   
        Elements children = e.children();
        if(children == null) return;
        
        for(Element c : children)
        {
            if(c == null) continue;
            
            System.out.print("<"+c.tagName()+" ");
            
            Attributes attrs = c.attributes();
            for(Attribute attr : attrs)
            {
                System.out.print(attr.getKey()+"=\""+attr.getValue()+"\" ");
            }
            
            System.out.println(">");
            
            printChildNodes(c);
            
            System.out.println("</"+c.tagName()+">");
        }
    }
    
    public static Elements scrap(Element toScrap,Elements models,HashMap<String,Elements> resultMap)
    {
        Elements sublist = new Elements();
        for(int i=0;i<models.size();i++)
        {
            Element model = models.get(i);
            Elements filtered = scrapSingle(toScrap, model);
            
            if(filtered == null || filtered.isEmpty()) continue;
            
            if(model.children() != null && !model.children().isEmpty())
            {
                for(Element efiltered : filtered)
                {
                    Elements result = scrap(efiltered, model.children(),resultMap);
                    if(result != null)
                    {
                        addAllUnique(sublist, result);
                    }
                }
            }
            else
            {
                if(resultMap != null)
                {
                    String mapKey = model.ownText();
                    if(mapKey == null || mapKey.trim().isEmpty())
                        mapKey = model.tagName();

                    if(filtered.size() == 1)
                        System.out.println(mapKey+" --> "+filtered.first().ownText());
                    else
                        System.out.println(mapKey+" --> "+filtered.size());
                    
                    if(!resultMap.containsKey(mapKey))
                    {
                        resultMap.put(mapKey, filtered);
                    }
                    else
                    {
                        addAllUnique(resultMap.get(mapKey),filtered);
                    }
                }
                
                addAllUnique(sublist, filtered);
            }
        }
        
        return sublist;
    }
    
    public static Elements scrapSingle(Element toScrap,Element model)
    {
        // 0. Find Elements that match the topmost model element.
        
        
        Elements doc = toScrap.children();
        
        Elements sublist = new Elements();
        
        // 0.1 -> find by tag name if present
        if(!isAny(model)) {
            for(Element e : doc)
            {
                Elements list = e.getElementsByTag(model.tagName());
                if(list != null)addAllUnique(sublist,list);
            }
        }
        else for(Element e : doc)
        {
            Elements list = e.getAllElements();
            if(list != null)addAllUnique(sublist,list);
        }
        
        
        // 0.2 -> find by attr name/value if present
        Attributes attrs = model.attributes();
        if(sublist.isEmpty() && attrs != null && attrs.size() > 0)
        {
            Attribute firstExact = null;
            for(Attribute attr : attrs)
            {
                if(isExact(attr)) firstExact = attr;
            }

            if(firstExact != null) {
                String fnaKey = firstExact.getKey();
                for(Element e : doc)
                {
                    Elements list;
                    if(fnaKey.endsWith("z"))
                    list = e.getElementsByAttributeStarting(fnaKey.substring(0, fnaKey.length()-1));
                    else if(firstExact.getValue() != null)
                    list = e.getElementsByAttributeValueMatching(fnaKey,firstExact.getValue());
                    else
                    list = e.getElementsByAttribute(fnaKey);
                    
                    if(list != null)addAllUnique(sublist,list);
                }
            }
            else
            {
                for(Element e : doc)
                {
                    Elements list = e.getAllElements();
                    if(list != null)addAllUnique(sublist,list);
                }
            }
            
        }

        // no result? return...
        if(sublist.isEmpty()) return null;
        
        // filter the list to match the model
        sublist = filterTag(sublist,model);
        
        // no result? return...
        if(sublist.isEmpty()) return null;
        
        //doc = sublist;
        //if(model.children() == null || model.children().isEmpty()) return doc;
        
        // continue on the children of the doc list
        /*sublist = new Elements();
        for(Element e : doc)
        {
            Elements list = e.children();
            if(list != null)addAllUnique(sublist,list);
        }
        doc = sublist;
        */
        
        //model = model.children().first();        
        
        return sublist;
    }
    
    public static boolean isAny(Element model)
    {
        return model.tagName().equalsIgnoreCase("z");
    }
    
    public static boolean isExact(Attribute model)
    {
        String attrKey = model.getKey();
        return !attrKey.equalsIgnoreCase("z")
            && !attrKey.startsWith("z-");
    }
    
    
    
    public static Elements filterTag(Elements list, Element rules)
    {
        Elements ret = new Elements();
        
        String tagName = rules.tagName();
        Attributes attrs = rules.attributes();
        for(Element e : list)
        {
            // Se o modelo especifica um nome de tag mas a tag não tem esse nome
            if(!isAny(rules) && !e.tagName().equalsIgnoreCase(tagName))
            {
                //System.out.println("tag: "+e.tagName()+" != "+tagName);
                continue;
            }
            boolean matched = true;
            if(attrs != null && attrs.size() > 0)
            for(Attribute attr : attrs)
            { 
                String attrKey = attr.getKey();
                if(attrKey.startsWith("z-"))
                {
                    // checagens especiais
                    String attrValue = attr.getValue();
                    switch(attrKey.substring(2))
                    {
                        case "index":
                        {
                            int siblingIndex = e.elementSiblingIndex();
                            
                            int index = Math.abs(Integer.parseInt(attrValue));
                            if(attrValue.startsWith("-"))
                            {
                                if(!(siblingIndex < index))
                                matched = false;
                            }
                            else if(attrValue.startsWith("+"))
                            {
                                if(!(siblingIndex > index))
                                matched = false;
                            }
                            else if(siblingIndex != index)
                            {
                                matched = false;
                            }
                        }
                        break;
                    }
                    
                    
                    if(!matched) break;
                }
                else
                {
                    // Se o modelo especifica um nome de atributo mas a tag não tem ele
                    Attributes filteredAttrs = filterAttrs(e.attributes(), attr);

                    if(filteredAttrs.size() == 0){
                        matched = false;
                        break;
                    }
                }
            }
            
            if(!matched) continue;
            
            
            ret.add(e);
        }
        
        return ret;
    }
    
    public static Attributes filterAttrs(Attributes attrs, Attribute model)
    {
        String attrKey = model.getKey();
        String attrValue = model.getValue();
        
        
        
        
        Attributes toCheck = new Attributes();
        
        if(isExact(model))
        {
            toCheck = attrs;
        }
        else if(attrKey.endsWith("z"))
        {
            attrKey = attrKey.substring(0,attrKey.length()-1);
            for(Attribute a : attrs)
            {
                if(a.getKey().startsWith(attrKey))
                toCheck.put(a);
            }
        }
        else
        {
            toCheck.put(attrKey,attrs.get(attrKey));
        }
        
        
        
        
        if(attrValue != null)
        {    
            Attributes ret = new Attributes();
            if(toCheck.size() > 0) for(Attribute eattr : toCheck)
            {
                String eattrValue = eattr.getValue();
                if(eattrValue == null) {
                    continue;
                }
                if(eattrValue.matches(attrValue) )
                {
                    ret.put(eattr);
                }
            }
            
            return ret;
        }
        else
        return toCheck;
    }
    
    public static void addAllUnique(Elements sublist, Elements toAdd)
    {
        for(Element e : toAdd)
        {
            if(!sublist.contains(e)) sublist.add(e);
        }
    }
}
