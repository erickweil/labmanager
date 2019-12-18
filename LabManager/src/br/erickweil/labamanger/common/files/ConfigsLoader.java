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

import br.erickweil.labamanger.common.Program;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Usuario
 */
public class ConfigsLoader {

    public static void SaveConfigs(final List<Program> programs,File file)
    {
        System.out.println("Salvando Configurações dos Programas");
        try {
            JSONArray json = new JSONArray();
            for(int i=0;i<programs.size();i++)
            {
                JSONObject j_node = Program.toJSON(programs.get(i));
                /*JSONObject j_node = new JSONObject();
                Program p = programs.get(i);
                
                j_node.put("name", p.name);
                j_node.put("path", p.path);
                j_node.put("process", p.process);
                j_node.put("window", p.window);
                j_node.put("start", p.start.toString());
                j_node.put("mode", p.mode.toString());
                j_node.put("block_offline", ""+p.block_offline);
                */
                json.add(j_node);
            }
            
            String file_text = json.toJSONString();
            byte[] file_contents = file_text.getBytes(Charset.forName("UTF-8"));
            
            if(!file.exists())
            {
                file.getParentFile().mkdirs();
            }
            Files.write(file.toPath(), file_contents);
        } catch (IOException ex) {
            System.err.println("Erro ao salvar programas, erro na escrita:"+ex.getMessage());
            ex.printStackTrace();
        }  catch (Exception ex) {
            System.err.println("Erro ao salvar programas, erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static void LoadConfigs(final List<Program> programs,File file)
    {
        System.out.println("Carregando Configurações dos Programas");
        try {
            if(!file.exists())
            {
                System.err.println("Erro ao carregar programas, caminho não encontrado: '"+file.getAbsolutePath()+"'");
                
            }

            byte[] file_contents = Files.readAllBytes(file.toPath());
            String file_text = new String(file_contents,Charset.forName("UTF-8"));
            JSONParser parser = new JSONParser();
            JSONArray json = (JSONArray) parser.parse(file_text);
            programs.clear();
            for(int i=0;i<json.size();i++)
            {
                JSONObject node = (JSONObject) json.get(i);
                //String name = (String)node.get("name");
                //String path = (String)node.get("path");
                //String process = (String)node.get("process");
                //String window = node.get("window") == null ? "" : (String)node.get("window");
                //String start = (String)node.get("start");
                //String block_offline = node.get("block_offline") == null ? "false" : (String)node.get("block_offline");
                //String mode = node.get("mode") == null ? "processo" : (String)node.get("mode");
                
                //Program p = new Program(name, path, process, window, Program.StartType.valueOf(start),Boolean.valueOf(block_offline),Program.BlockMode.valueOf(mode));
                Program p = Program.fromJSON(node);
                programs.add(p);
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar programas, erro de leiura:"+ex.getMessage());
            ex.printStackTrace();
        } catch (ParseException ex) {
            System.err.println("Erro ao carregar programas, erro no json:"+ex.getMessage());
            ex.printStackTrace();
        }  catch (Exception ex) {
            System.err.println("Erro ao carregar programas, erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
        }
        
    }
}
