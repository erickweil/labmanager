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
package br.erickweil.labmanager.cmd;

import java.io.File;
import org.json.simple.JSONObject;

/**
 *  Classe que abstrai a ideia de um processo
 *  deve ser utilizada para representar um programa em execução ou
 * um programa a ser aberto
 * 
 */
public class CmdProcess {
    public String name;
    public String windowTitle;
    public String description;
    
    public File executable;

    public long pid;
    public long parent_pid;
    
    public int usage_cpu;
    public long usage_memory;
    public long usage_network;
    
    public String username;
    
    public CmdProcess(String name,String windowTitle,long pid)
    {
        if(name != null)
        this.name = name.replace(".exe", "");
        this.windowTitle = windowTitle;
        this.pid = pid;
    }
    
    public CmdProcess(String name,long pid, long parent_pid)
    {
        if(name != null)
        this.name = name.replace(".exe", "");
        this.pid = pid;
        this.parent_pid = parent_pid;
    }
    
    public static CmdProcess fromJSON(JSONObject obj)
    {
        CmdProcess ret = new CmdProcess(null,0,0);
        
        if(obj.containsKey("name"))ret.name = obj.get("name").toString();
        if(obj.containsKey("windowTitle"))ret.windowTitle = obj.get("windowTitle").toString();
        if(obj.containsKey("description"))ret.description = obj.get("description").toString();
        if(obj.containsKey("executable"))ret.executable = new File(obj.get("executable").toString());
        if(obj.containsKey("pid"))ret.pid = Long.parseLong(obj.get("pid").toString());
        if(obj.containsKey("parent_pid"))ret.parent_pid = Long.parseLong(obj.get("parent_pid").toString());
        
        return ret;
    }
    
    public static JSONObject toJSON(CmdProcess p)
    {
        JSONObject obj = new JSONObject();
        if(p.name!=null)obj.put("name", ""+p.name);
        if(p.windowTitle!=null)obj.put("windowTitle", ""+p.windowTitle);
        if(p.description!=null)obj.put("description", ""+p.description);
        if(p.executable!=null)obj.put("executable", p.executable.getAbsolutePath());
        obj.put("pid", ""+p.pid);
        obj.put("parent_pid", ""+p.parent_pid);
        
        return obj;
    }
    
    @Override
    public String toString()
    {
        return 
            (name == null ? "" : " process:"+name)+
            (windowTitle == null ? "" : " window:"+windowTitle)+
            (description == null ? "" : " desc:"+description)+
            (executable == null ? "" : " file:"+executable.getAbsolutePath())+
            (pid <= 0 ? "" : " pid:"+pid)+
            (parent_pid <= 0 ? "" : " ppid:"+parent_pid)
            ;
        
    }
}
