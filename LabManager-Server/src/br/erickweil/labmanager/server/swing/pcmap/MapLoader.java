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
package br.erickweil.labmanager.server.swing.pcmap;

import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.ClientStatusManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Usuario
 */
public class MapLoader {
    public static List<MapNode> loadMap(ClientStatusManager statusManager,String room)
    {
        try {
            File file = new File(ServerMain._preferencesdir+room+".pcmap.json");
            if(!file.exists())
            {
                System.err.println("Erro ao carregar mapa '"+room+"', caminho não encontrado: '"+file.getAbsolutePath()+"'");
                return null; 
            }
        
        
            byte[] file_contents = Files.readAllBytes(file.toPath());
            String file_text = new String(file_contents,Charset.forName("UTF-8"));
            JSONParser parser = new JSONParser();
            JSONArray json = (JSONArray) parser.parse(file_text);
            List<MapNode> nodelist = new ArrayList<MapNode>();
            for(int i=0;i<json.size();i++)
            {
                JSONObject node = (JSONObject) json.get(i);
                if(node.get("type").equals("PC"))
                {
                    String uuid = (String)node.get("uuid");
                    double px = Double.parseDouble((String)node.get("px"));
                    double py = Double.parseDouble((String)node.get("py"));
                    //double px = Integer.parseInt((String)node.get("px"))/32.0;
                    //double py = Integer.parseInt((String)node.get("py"))/32.0;
                    PC pcnode = new PC(statusManager,uuid,px,py);
                    nodelist.add(pcnode);
                }
                else System.err.println("UNKNOW PC TYPE '"+node.get("type")+"'");
            }
            return nodelist;
        } catch (IOException ex) {
            System.err.println("Erro ao carregar mapa '"+room+"', erro de leiura:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        } catch (ParseException ex) {
            System.err.println("Erro ao carregar mapa '"+room+"', erro no json:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        }  catch (Exception ex) {
            System.err.println("Erro ao carregar mapa '"+room+"', erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
            return null; 
        }
    }
    public static void saveMap(List<MapNode> nodelist,String room)
    {
        //if(true)
        //return;
        try {
            JSONArray json = new JSONArray();
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#0.00",otherSymbols); 
            for(int i=0;i<nodelist.size();i++)
            {
                JSONObject j_node = new JSONObject();
                MapNode node = nodelist.get(i);
                if(node instanceof PC)
                {
                    PC pcnode = (PC) node;
                    j_node.put("type", "PC");
                    j_node.put("uuid", pcnode.uuid);
                    j_node.put("px", df.format(pcnode.px));
                    j_node.put("py", df.format(pcnode.py));
                }
                else
                {
                    System.err.println("UNKNOW PC TYPE '"+node.getClass().getCanonicalName()+"'");
                    continue;
                }
                
                
                json.add(j_node);
            }
            
            String file_text = json.toJSONString();
            byte[] file_contents = file_text.getBytes(Charset.forName("UTF-8"));
            File file = new File(ServerMain._preferencesdir+room+".pcmap.json");
            if(!file.exists())
            {
                file.getParentFile().mkdirs();
            }
            Files.write(file.toPath(), file_contents);
        } catch (IOException ex) {
            System.err.println("Erro ao salvar mapa '"+room+"', erro na escrita:"+ex.getMessage());
            ex.printStackTrace();
        }  catch (Exception ex) {
            System.err.println("Erro ao salvar mapa '"+room+"', erro inesperado:"+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
