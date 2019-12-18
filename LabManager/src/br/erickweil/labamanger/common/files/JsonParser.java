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
package br.erickweil.labamanger.common.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe para carregar/salvar arquivos json a partir de um mapping HashMap<String,String>
 * @author Usuario
 */
public class JsonParser {
    public static HashMap<String,String> load(File file)
    {
        try {
            if(!file.exists())
            {
                System.err.println("Erro ao carregar json mapping, caminho não encontrado: '"+file.getAbsolutePath()+"'");
                return null; 
            }
        
        
            byte[] file_contents = Files.readAllBytes(file.toPath());
            String file_text = new String(file_contents,Charset.forName("UTF-8"));
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(file_text);
            HashMap<String,String> mapping = new HashMap<>();
            
            for(Object key : json.keySet())
            {
                String k = (String) key;
                mapping.put(k, (String)json.get(key));
            }
            return mapping;
        } catch (IOException ex) {
            System.err.println("Erro ao carregar json mapping, erro de leiura:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        } catch (ParseException ex) {
            System.err.println("Erro ao carregar json mapping, erro no json:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        }  catch (Exception ex) {
            System.err.println("Erro ao carregar json mapping, erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        }
    }
    public static void save(HashMap<String,String> mapping,File file)
    {
        try {
            JSONObject json = new JSONObject(mapping); 
            
            String file_text = json.toJSONString();
            byte[] file_contents = file_text.getBytes(Charset.forName("UTF-8"));
            if(!file.exists())
            {
                file.getParentFile().mkdirs();
            }
            Files.write(file.toPath(), file_contents);
        } catch (IOException ex) {
            System.err.println("Erro ao salvar json mapping, erro na escrita:"+ex.getMessage());
            ex.printStackTrace();
        }  catch (Exception ex) {
            System.err.println("Erro ao salvar json mapping, erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
        }
    }

}
