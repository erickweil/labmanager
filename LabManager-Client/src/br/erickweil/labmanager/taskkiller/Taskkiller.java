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
package br.erickweil.labmanager.taskkiller;


import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.Program.Condition;
import br.erickweil.labamanger.common.Program.ConditionVar;
import br.erickweil.labamanger.common.files.FilesHelper;
import br.erickweil.labmanager.client.ClientMain;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.platformspecific.CheckOS;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import org.json.simple.parser.ParseException;

/**
 * Exterminador de Processos.
 * @author Usuario
 */
public class Taskkiller implements Runnable{
	
    public static interface KillerHandler {
        public boolean connectedOnce();
        public List<Program> getBlackList();
        public List<CmdProcess> getWindowList();
    }
    
    // TEMP
    private static final boolean LOGBLOCKS = true;
    private String blocklog = "";
    
    
    // mapeamentos dinâmicos
    private List<CmdProcess> processList;
    private HashMap<Long,CmdProcess> pid_map;
    
    // baseado na blacklist
    private boolean doProcessCheck;
    private boolean doWindowCheck;
    private boolean doExecCheck;
    
    private boolean ableToCheckWindows;
    public KillerHandler handler;
	public static void main(String[] args) throws IOException
	{
		new Taskkiller(true,null).run();
	}
	
	public Taskkiller(boolean ableToCheckWindows,KillerHandler handler) {
		// TODO Auto-generated constructor stub
                this.handler = handler;
                this.ableToCheckWindows = ableToCheckWindows;
	}
    
    public boolean running = true;
    
    public synchronized void stop()
    {
        this.running = false;
    }   
	
	@Override
	public void run() {
        long taskkiler_started = System.currentTimeMillis();
		try {
			while(running)
			{
				long start = System.currentTimeMillis();
				try {
                    
                    if((start - taskkiler_started) < 35000 && !handler.connectedOnce())
                    {
                        // lista de programas que irão ser proibidos pelos
                        // primeiros 35 segundos da execução, caso ainda não tenha conectado com o servidor
                        // para dar uma chance de carregar a nova lista e evitar um ciclo infinito
                        // quando se tem um bloqueio que fecha o programa
                        
                        // por causa disso deve-se ter um delay de 15000 no locker inicial
                        //List<Program> blacklist = new ArrayList<>();
                        //blacklist.add(new Program("","","regedit.exe",Program.StartType.blacklist,false));
                        //blacklist.add(new Program("","","taskmgr.exe",Program.StartType.blacklist,false));
                        //blacklist.add(new Program("","","msconfig.exe",Program.StartType.blacklist,false));
                        //blacklist.add(new Program("","","cmd.exe",Program.StartType.blacklist,false));
                        //updateProcessList(null,null,null);
                        //check(blacklist);
                    }
                    else
                    {
                        List<Program> blacklist = handler.getBlackList();
                        
                        List<CmdProcess> program_list = null;// CmdExec.running_programs();
                        List<CmdProcess> window_list= null;// CmdExec.running_windows();
                        List<CmdProcess> exec_list= null;// CmdExec.running_windows();
                        
                        if(doProcessCheck || doWindowCheck || doExecCheck) program_list = CmdExec.running_programs(); // PID, parent PID, name
                        if(doWindowCheck)
                        {
                            if(ableToCheckWindows)
                            {
                                window_list = CmdExec.running_windows();
                            } // PID, windowtitle
                            else
                            {
                                window_list = handler.getWindowList();
                            }
                            
                            //if(window_list == null) System.out.println("window_list=null");
                            //else if(window_list.isEmpty()) System.out.println("window_list=[]");
                            //else System.out.println("window_list=["+window_list.size()+"]");
                        }
                        if(doExecCheck) exec_list = CmdExec.running_executables(); // PID, executable path
                        
                        updateProcessList(program_list,window_list,exec_list);
                        check(blacklist);
                    }
                    
                    
                    
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long elapsed = System.currentTimeMillis() - start;
                //System.out.println(elapsed);
				if(elapsed < ClientMain._delay_taskkiller)
				Thread.sleep(ClientMain._delay_taskkiller - elapsed);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
        
        public void updateProcessList(List<CmdProcess> program_list,List<CmdProcess> window_list,List<CmdProcess> exec_list)
        {
            processList = new ArrayList<>();
            pid_map = new HashMap<Long,CmdProcess>();
            if(doProcessCheck || doWindowCheck || doExecCheck && program_list != null) for(CmdProcess c : program_list)
            {
                if(c.name.endsWith(".exe"))
                {
                    c.name = c.name.replace(".exe", "");
                }
                processList.add(c); 
                pid_map.put(c.pid, c);
            }
            if(doWindowCheck && window_list != null) for(CmdProcess c : window_list)
            {
                if(!pid_map.containsKey(c.pid))
                {
                    processList.add(c);
                }
                else
                {
                    pid_map.get(c.pid).windowTitle = c.windowTitle;
                }
            }
            if(doExecCheck && exec_list != null) for(CmdProcess c : exec_list)
            {
                if(!pid_map.containsKey(c.pid))
                {
                    processList.add(c);
                }
                else
                {
                    pid_map.get(c.pid).executable = c.executable;
                }
            }
            
            doProcessCheck = false;
            doWindowCheck = false;
            doExecCheck = false;
        }
        
        public boolean checkstr(Program.ConditionMatch match,String name, String process)
        {
            if(name == null || process == null) return false;
            String lname = name.toLowerCase();
            String lprocess = process.toLowerCase();
            switch(match)
            {
                case inicia:
                    return lname.startsWith(lprocess);
                case termina:
                    return lname.endsWith(lprocess);
                case contem:
                    return lname.contains(lprocess);
                case regex:
                    return name.matches(process);
                default:
                case exatamente:
                return lname.equals(lprocess);
            }
        }
        
        public List<CmdProcess> checkCondition(Program.Condition cond, List<CmdProcess> candidates)
        {
            List<CmdProcess> ret = new ArrayList<>();
            if(LOGBLOCKS)
                blocklog += "\t"+Condition.toJSON(cond).toJSONString()+"\n";
            
            if(cond.value == null || cond.value.isEmpty())
            {
                return ret;
            }
            
            switch(cond.var)
            {
                case processo:
                    doProcessCheck = true;
                    String process = cond.value;
                    if(process.endsWith(".exe"))
                    {
                        process = process.replace(".exe", "");
                    }
                    for (CmdProcess c : processList) {
                        try {
                            if (c.name != null)
                            {
                                //boolean condEval = (c.name.equalsIgnoreCase(process) || c.name.matches(process));
                                boolean condEval = checkstr(cond.match, c.name, process);
                                
                                if((cond.inverse && !condEval) || (!cond.inverse && condEval))
                                {
                                    if(candidates == null || candidates.contains(c))
                                    {
                                        if(LOGBLOCKS)
                                            blocklog += "\t\t"+c.name+" == "+process;
                                        ret.add(c);
                                    }
                                }
                            }
                        } catch (PatternSyntaxException ex) {
                            /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */
                        }
                    }    
                    break;
                case janela:
                    //System.out.println("CHECKING WINDOW!:"+cond);
                    doWindowCheck = true; // na proxima vez vai checar
                    for (CmdProcess c : processList) {
                        try {
                            if (c.windowTitle != null)
                            {
                                //boolean condEval = (c.windowTitle.equalsIgnoreCase(cond.value) || c.windowTitle.matches(cond.value));
                                boolean condEval = checkstr(cond.match, c.windowTitle, cond.value);
                                
                                if((cond.inverse && !condEval) || (!cond.inverse && condEval))
                                {
                                    if(candidates == null || candidates.contains(c))
                                    {
                                        if(LOGBLOCKS)
                                            blocklog += "\t\t"+c.windowTitle+" == "+cond.value;
                                        ret.add(c);
                                    }
                                }
                            }
                        } catch (PatternSyntaxException ex) {
                            /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */
                        }
                    }
                    break;
                case caminho:
                    doExecCheck = true; // na proxima vez vai checar
                    File pathfile = ProgramOpener.parsePath(cond.value);
                    String absoluteExe = pathfile.getAbsolutePath();
                    boolean directory = pathfile.isDirectory();
                    for (CmdProcess c : processList) {
                        if (c.executable != null)
                        try {
                            //System.out.println("\t"+w.windowTitle+"\t"+w.name+"\t"+w.pid);
                            boolean condEval;
                            if(directory)
                            {
                                condEval = FilesHelper.isInside(c.executable,pathfile);
                            }
                            else
                            {
                                condEval = c.executable.getAbsolutePath().equals(absoluteExe);
                            }
                            
                            if((cond.inverse && !condEval) || (!cond.inverse && condEval))
                            {
                                if(candidates == null || candidates.contains(c))
                                    ret.add(c);
                            }
                        } catch (PatternSyntaxException ex) {
                            /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */
                        }
                    }
                break;
                case pai: break;
            }
            if(LOGBLOCKS)
            {
                for(CmdProcess cmdP : ret)
                {
                    blocklog += "\tRemained:\n\t\t"+cmdP.toString()+"\n";
                }
            }
            return ret;
        }
        
        public List<CmdProcess> checkConditions(Program p,List<CmdProcess> match)
        {
            if(LOGBLOCKS)
            try {
                blocklog = Program.toStr(p)+"\n";
            } catch (ParseException ex) {
                Logger.getLogger(Taskkiller.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(p.stopConditions == null || p.stopConditions.length <= 0) return match;
            
            for(int i=0;i<p.stopConditions.length;i++)
            {
                Program.Condition cond = p.stopConditions[i];
                match = checkCondition(cond, match);
                if(match.isEmpty())
                {
                    return match;
                }   
            }
            return match;
        }
	public void check(List<Program> blacklist) throws IOException
	{
            if(blacklist == null || blacklist.isEmpty())
            {
                return;
            }

            for(Program p : blacklist)
            {
                // só bloqueia o que for para ser bloqueado
                if(p.start != Program.StartType.blacklist) continue;
                // não bloquear programas marcados para serem bloqueados apenas com o servidor ativo.
                // obs: pelo menos esteve uma vez conectado ao servidor.
                if(!p.block_offline && !handler.connectedOnce()) continue;
                
                if(p.stopConditions == null || p.stopConditions.length == 0)
                {
                    p.stopConditions = new Program.Condition[1];
                    p.stopConditions[0] = new Program.Condition(
                            p.mode == Program.BlockMode.processo ? ConditionVar.processo
                           :(p.mode == Program.BlockMode.janela ? ConditionVar.janela : ConditionVar.processo), 
                            Program.ConditionMatch.regex,
                            p.mode == Program.BlockMode.processo ? p.process
                           :(p.mode == Program.BlockMode.janela ? p.window :  p.process), 
                            false);
                }
                
                ///*List<CmdProcess> toStop = checkCondition(p,p.stopConditions[0],null);
                //if(toStop != null)
                //{
                    List<CmdProcess> toStop = checkConditions(p, null);
                    if(toStop != null) for(CmdProcess c : toStop)
                    {
                        CmdExec.stop(c, true);
                        if(LOGBLOCKS)
                            System.out.println(blocklog);
                    }
                //}
                
            }
	}
        
	public void _check_old(List<Program> blacklist) throws IOException
	{
		if(blacklist == null || blacklist.size() == 0)
                {
                    return;
                }
                List<CmdProcess> program_list = null;// CmdExec.running_programs();
                List<CmdProcess> window_list= null;// CmdExec.running_windows();
                                
                for(Program p : blacklist)
                {
                    // só bloqueia o que for para ser bloqueado
                    if(p.start != Program.StartType.blacklist) continue;
                    // não bloquear programas marcados para serem bloqueados apenas com o servidor ativo.
                    // obs: pelo menos esteve uma vez conectado ao servidor.
                    if(!p.block_offline && !handler.connectedOnce()) continue;
                    
                    String process = p.process;
                    if(process.endsWith(".exe"))
                    {
                        process = process.replace(".exe", "");
                    }
                    
                    if(/*checkProcess &&*/ p.mode == Program.BlockMode.processo)
                    {
                        if(program_list == null) program_list = CmdExec.running_programs();
                        for (CmdProcess c : program_list) {
                            try {
                                if (c.name.equalsIgnoreCase(process) || c.name.matches(process)) 
                                {
                                    //if(checkConditions(p,program_list,window_list))
                                    //{
                                        CmdExec.lowlevel_stop(p.process,true);
                                        System.out.println("Stopped "+p);
                                    //}
                                }
                            } catch (PatternSyntaxException ex) {
                                /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */
                            }
                        }
                    }
                    else if(/*checkWindow && */p.mode == Program.BlockMode.janela)
                    {
                        if(window_list == null) window_list = CmdExec.running_windows();
                        //System.out.println("WINDOW LIST:");
                        for (CmdProcess w : window_list) {
                            try {
                                //System.out.println("\t"+w.windowTitle+"\t"+w.name+"\t"+w.pid);
                                if (w.windowTitle.equalsIgnoreCase(p.window) || w.windowTitle.matches(p.window)) {
                                    //if(checkConditions(p,program_list,window_list))
                                    //{
                                        CmdExec.lowlevel_stop(w.pid,true);
                                        System.out.println("Stopped "+p+" pid:"+w.pid);
                                    //}
                                }
                            } catch (PatternSyntaxException ex) {
                                /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */
                            }
                        }
                    }
                }
	}

}
