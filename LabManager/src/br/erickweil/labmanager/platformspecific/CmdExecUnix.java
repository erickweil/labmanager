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
package br.erickweil.labmanager.platformspecific;

import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class CmdExecUnix extends CmdExec{



    @Override
    protected String _getUser() {
        //https://www.cyberciti.biz/faq/appleosx-bsd-shell-script-get-current-user/
        //https://www.thegeekstuff.com/2009/03/4-ways-to-identify-who-is-logged-in-on-your-linux-system/
        /*List<String> result = CmdExec.readCmd("w","-h","-s");
        String first = result.get(0);
        String[] cols = first.replace('\t',' ').split("\\s+");
        return cols[0];*/
        List<String> result = CmdExec.readCmd("last","-n","1");
        String first = result.get(0);
        String[] cols = first.replace('\t',' ').split("\\s+");
        return cols[0];
    }

    @Override
    protected void _lowlevel_stop(String process,boolean force,boolean killTree) {
        //https://www.linux.com/learn/intro-to-linux/2017/5/how-kill-process-command-line
        //http://programmergamer.blogspot.com.br/2013/05/clarification-on-sigint-sigterm-sigkill.html
        if(force)
        {
            //CmdExec.execCmd("killall","-9",process);
            CmdExec.readCmd("killall","-9",process);
        }
        else
        {
            //CmdExec.execCmd("killall","-15",process);
            CmdExec.readCmd("killall","-15",process);
        }
    }

        @Override
    protected void _lowlevel_stop(long pid,boolean force,boolean killTree) {
        //https://www.linux.com/learn/intro-to-linux/2017/5/how-kill-process-command-line
        //http://programmergamer.blogspot.com.br/2013/05/clarification-on-sigint-sigterm-sigkill.html
        if(force)
        {
            CmdExec.readCmd("kill","-9",""+pid);
        }
        else
        {
            CmdExec.readCmd("kill","-15",""+pid);
        }
    }

    @Override
    protected void _shutdown() {
        CmdExec.readCmd("shutdown","-h","-t","1");
    }

    @Override
    protected void _restart() {
        CmdExec.readCmd("shutdown","-r","-t","1");
    }

    @Override
    protected void _logoff() {
        CmdExec.readCmd("exit");
    }

    @Override
    protected void _cancelshutdown() {
        CmdExec.readCmd("shutdown","-c");
    }

    @Override
    protected List<CmdProcess> _running_programs() {
        List<CmdProcess> program_list = new ArrayList<>();
        
        List<String> result = CmdExec.readCmd("ps","-A","--no-headers");
		if(result == null || result.isEmpty())
		{
			System.out.println("comando ps retornou nulo.");
			return program_list;
		}
		for (String line : result)
		{
            line = line.replace('\t',' ').replaceAll("\\s+"," ").trim();
            if(line.isEmpty() || !line.contains("\\s")) continue;
            
			String[] cols = line.split("\\s");
            if(cols.length <= 1) continue;
            
            if(!cols[0].trim().isEmpty() && cols[0].matches("^\\d+$"))
            {
                long pid = Long.parseLong(cols[0]);
                String name = cols[cols.length-1];
                program_list.add(new CmdProcess(name,pid,-1));
            }
		}
    	return program_list;
    }

    @Override
    protected long[] _memory_info() {
        //https://superuser.com/questions/521551/cat-proc-meminfo-what-do-all-those-numbers-mean
        try {
            
            long[] info = new long[2];
            List<String> lines = Files.readAllLines(new File("/proc/meminfo").toPath());
            for(String s : lines)
            {
                String[] cols = s.split("\\s+");
                if(cols[0].equalsIgnoreCase("MemTotal:"))
                {
                    info[0] = Long.parseLong(cols[1])*1000;
                }
                if(cols[0].equalsIgnoreCase("MemFree:"))
                {
                    info[1] = Long.parseLong(cols[1])*1000;
                }
            }
            return info;
        } catch (Exception ex) {
            System.out.println("Erro ao ler o uso de memória no Linux/Unix:"+ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    // não é assim tão importante
    @Override
    protected List<CmdProcess> _running_windows() {
        //https://superuser.com/questions/176754/get-a-list-of-open-windows-in-linux?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //https://superuser.com/questions/176754/get-a-list-of-open-windows-in-linux
        return new ArrayList<>();
    }

    @Override
    protected void _changeToWifi(String SSID) {
        // ubuntu-only: https://askubuntu.com/questions/461825/how-to-connect-to-wifi-from-the-command-line
    }
    
    @Override
    protected float _cpu_info() {
        // https://stackoverflow.com/questions/23367857/accurate-calculation-of-cpu-usage-given-in-percentage-in-linux
        //     user    nice   system  idle      iowait irq   softirq  steal  guest  guest_nice
        //cpu  74608   2520   24433   1117073   6176   4054  0        0      0      0
        List<String> prevresult = CmdExec.readCmd("grep","cpu","/proc/stat");
        String[] cpu = prevresult.get(0).replaceAll("[\\s\\t]+", " ").split(" ");
        
        int prevuser = Integer.parseInt(cpu[1]);
        int prevnice = Integer.parseInt(cpu[2]);
        int prevsystem = Integer.parseInt(cpu[3]);
        int previdle = Integer.parseInt(cpu[4]);
        int previowait = Integer.parseInt(cpu[5]);
        int previrq = Integer.parseInt(cpu[6]);
        int prevsoftirq = Integer.parseInt(cpu[7]);
        int prevsteal = Integer.parseInt(cpu[8]);
        int prevguest = Integer.parseInt(cpu[9]);
        int prevguest_nice = Integer.parseInt(cpu[10]);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(CmdExecUnix.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<String> result = CmdExec.readCmd("grep","cpu","/proc/stat");
        cpu = result.get(0).replaceAll("[\\s\\t]+", " ").split(" ");
        
        int user = Integer.parseInt(cpu[1]);
        int nice = Integer.parseInt(cpu[2]);
        int system = Integer.parseInt(cpu[3]);
        int idle = Integer.parseInt(cpu[4]);
        int iowait = Integer.parseInt(cpu[5]);
        int irq = Integer.parseInt(cpu[6]);
        int softirq = Integer.parseInt(cpu[7]);
        int steal = Integer.parseInt(cpu[8]);
        int guest = Integer.parseInt(cpu[9]);
        int guest_nice = Integer.parseInt(cpu[10]);
        
        
        //Algorithmically, we can calculate the CPU usage percentage like:

        //PrevIdle = previdle + previowait
        //Idle = idle + iowait
        int PrevIdle = previdle + previowait;
        int Idle = idle + iowait ;
        
        //PrevNonIdle = prevuser + prevnice + prevsystem + previrq + prevsoftirq + prevsteal
        //NonIdle = user + nice + system + irq + softirq + steal
        int PrevNonIdle = prevuser + prevnice + prevsystem + previrq + prevsoftirq + prevsteal;
        int NonIdle = user + nice + system + irq + softirq + steal;
        
        //PrevTotal = PrevIdle + PrevNonIdle
        //Total = Idle + NonIdle
        int PrevTotal = PrevIdle + PrevNonIdle;
        int Total = Idle + NonIdle;

        //# differentiate: actual value minus the previous one
        //totald = Total - PrevTotal
        //idled = Idle - PrevIdle
        int totald = Total - PrevTotal;
        int idled = Idle - PrevIdle;

        //CPU_Percentage = (totald - idled)/totald
        float CPU_Percentage = (float)(totald - idled)/(float)totald;
        
        return CPU_Percentage;
    }

    @Override
    protected List<CmdProcess> _running_executables() {
        return new ArrayList<>();
    }

    @Override
    protected void _stopService(String service) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void _startService(String service) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
