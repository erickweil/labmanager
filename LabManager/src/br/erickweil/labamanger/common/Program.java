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
package br.erickweil.labamanger.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *<strong> Classe que abstrai a idéia de um programa.</strong>
 * dependendo do StartType irá indicar se irá:
 * <ul>
 * <li>manual: pode ser aberto e fechado manualmente</li>
 * <li>logon: irá ser aberto em todos os clientes, assim que conectarem</li>
 * <li>blacklist: proíbe a execução do programa em todos os clientes, até os que ainda vão se conectar</li>
 * </ul>
 * a indicação do path permite o programa ser aberto.</br>
 * a indicação do proccess permite o programa ser fechado.</br>
 */
public class Program {
    
    public String path;
    public String name;
    public String process;
    public String window;
    public StartType start;
    public BlockMode mode;
    public boolean block_offline;
    public Condition[] stopConditions;
        
    public static class Condition{
        public ConditionVar var;
        public ConditionMatch match;
        public String value;
        public boolean inverse;
        
        public Condition(ConditionVar var,
        ConditionMatch match,
        String value,
        boolean inverse)
        {
            this.var = var;
            this.match = match;
            this.value = value;
            this.inverse = inverse;
        }
        
        public Object[] toArray()
        {
            return new Object[]{var.toString(),match.toString(),value,inverse};
        }
        
        public static JSONObject toJSON(Condition c)
        {
            JSONObject ret = new JSONObject();
            ret.put("var", c.var.toString());
            ret.put("match", c.match.toString());
            ret.put("value", c.value);
            ret.put("inverse", ""+c.inverse);
            
            return ret;
            //return var+"\t"+match+"\t"+value+"\t"+inverse;
        }
        
        public static Condition fromJSON(JSONObject o)
        {
            //String[] args = str.split("\t");
            return new Condition(
                ConditionVar.valueOf(o.get("var").toString()), 
                ConditionMatch.valueOf(o.get("match").toString()), 
                o.get("value").toString(), 
                Boolean.parseBoolean(o.get("inverse").toString()));
        }
        
        public String toString()
        {
            return Condition.toJSON(this).toJSONString();
        }
    }
    public static enum StartType{
    logon,
    manual,
    blacklist
    };
    public static enum BlockMode{
    processo,
    janela,
    caminho
    };
    
    public static enum ConditionVar{
    processo,
    janela,
    caminho,
    pai
    };
    
    public static enum ConditionMatch{
    exatamente,
    inicia,
    termina,
    contem,
    regex
    };

    public Program(String name,String path,String process,StartType start,boolean block_offline)
    {
        this.path = path;
        this.name = name;
        this.process = process;
        this.start = start;
        this.block_offline = block_offline;
        this.mode = BlockMode.processo;
    }
    public Program(String name,String path,String process,StartType start,boolean block_offline,BlockMode mode)
    {
        this.path = path;
        this.name = name;
        this.process = process;
        this.start = start;
        this.block_offline = block_offline;
        this.mode = mode;
    }
    public Program(String name,String path,String process,String window,StartType start,boolean block_offline,BlockMode mode)
    {
        this.path = path;
        this.name = name;
        this.process = process;
        this.window = window;
        this.start = start;
        this.block_offline = block_offline;
        this.mode = mode;
    }
    public static Program fromJSON(JSONObject o)
    {
        Condition[] conds = null;
        if(o.get("conditions") != null)
        {
            JSONArray jconditions = (JSONArray)o.get("conditions");
            conds = new Condition[jconditions.size()];
            for(int i=0;i<conds.length;i++)
            {
                conds[i] = Condition.fromJSON((JSONObject)jconditions.get(i));
            }
        }
        
        //String name = (String)node.get("name");
        //String path = (String)node.get("path");
        //String process = (String)node.get("process");
        //String window = node.get("window") == null ? "" : (String)node.get("window");
        //String start = (String)node.get("start");
        //String block_offline = node.get("block_offline") == null ? "false" : (String)node.get("block_offline");
        //String mode = node.get("mode") == null ? "processo" : (String)node.get("mode");
                
        //Program p = new Program(name, path, process, window, Program.StartType.valueOf(start),Boolean.valueOf(block_offline),Program.BlockMode.valueOf(mode));
        
        Program p = new Program(
            o.get("name").toString(), // name
            o.get("path").toString(), // path
            o.get("process").toString(), // process
            o.get("window") == null ? "" : o.get("window").toString(), // window
            StartType.valueOf(o.get("start").toString()), // start
            Boolean.valueOf(o.get("block_offline") == null ? "false" : o.get("block_offline").toString()), // block offline
            BlockMode.valueOf(o.get("mode") == null ? "processo" : o.get("mode").toString()) // block mode
        );
        if(conds != null && conds.length > 0) p.stopConditions = conds;
        
        return p;
        
    }
    public static JSONObject toJSON(Program p)
    {
        JSONObject ret = new JSONObject();
        
        ret.put("name", p.name);
        ret.put("path", p.path);
        ret.put("process", p.process);
        ret.put("window", p.window);
        ret.put("start", p.start.toString());
        ret.put("block_offline", ""+p.block_offline);
        ret.put("mode", p.mode.toString());
        
        if(p.stopConditions != null && p.stopConditions.length > 0)
        {
            JSONArray conds = new JSONArray();
            for(int i=0;i<p.stopConditions.length;i++)
            {
                conds.add(Condition.toJSON(p.stopConditions[i]));
            }
            ret.put("conditions", conds);
        }
        
        return ret;
        
    }
    public static Program fromStr(String str) throws ParseException
    {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(str);
        return fromJSON(json);
    }
    public static String toStr(Program p) throws ParseException
    {
        return toJSON(p).toJSONString();
    }
    public static Program old_fromStr(String str) throws ParseException
    {
        System.out.println("'"+str+"'");
        String[] args = str.split("\t");
        return new Program(
            args[0], // name
            args[1], // path
            args[2], // process
            args[3], // window
            StartType.valueOf(args[4]), // start
            Boolean.valueOf(args[5]), // block offline
            BlockMode.valueOf(args[6]) // block mode
        );
    }
    public static String old_toStr(Program p) throws ParseException
    {
        return
            p.name + "\t" +
            p.path + "\t" +
            p.process + "\t" +
            p.window + "\t" +
            p.start.toString() + "\t" +
            p.block_offline + "\t" +
            p.mode.toString();
    }
}
