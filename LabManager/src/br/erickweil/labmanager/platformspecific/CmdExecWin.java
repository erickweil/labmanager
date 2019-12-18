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

import br.erickweil.labmanager.cmd.CSVUtils;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.CmdProcess;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class CmdExecWin extends CmdExec {
    
    public static void main(String[] args)
    {
        CmdExecWin t = new CmdExecWin();
        long[] i = t._memory_info();
        float c = t._cpu_info();
        System.out.println("mem:"+i[0]+","+i[1]+"("+((float)(i[0]-i[1])/(float)i[0])+"%)"+" cpu:"+c+"%");
        
        //List<CmdProcess> _running_windows = t._running_windows();
        //for(String w : _running_windows)
        //{
        //    System.out.println(w);
        //}
    }

    
    /** pega o usuario logado agora
     * 
     */
    @Override
    public String _getUser()
    {
    	List<String> result = CmdExec.readCmd("cmd","/c","echo %username%");
		if(result == null || result.isEmpty())
		{
			System.out.println("comand getuser retornou nulo");
			return null;
		}
		return result.get(0);
    }
    
	/**
     * Para a execução do programa, sem checar se está listado como processo aberto
	* <pre>
	* pode ser um problema, dependendo da implementação do ProcessBuilder 
	* pois recebe o program como entrada do server, e pode ser malicioso 
	* por exemplo o programa: "cmd.exe && start iexplore.exe http://i.imgur.com/UIcZyA4.png"
	* ficaria: 
	* taskkill /F /IM cmd.exe && start iexplore.exe http://i.imgur.com/UIcZyA4.png 
	* mas da forma que está, só é chamado pela Taskkiller 
	* que só chama esse comando se o nome a imagem do processo 'program' aparecer 
	* na saída do tasklist, ou seja, é seguro da forma que está 
	* </pre>
	* @param program o programa a ser parado
	*/
    @Override
    public void _lowlevel_stop(String process,boolean force,boolean killTree)
    {
        if(force)
        {
            if(killTree)
            {
                CmdExec.execCmd("taskkill","/F","/T","/IM",process);
            }
            else
            {
                CmdExec.execCmd("taskkill","/F","/IM",process);
            }
                
        }
        else
        {
            CmdExec.execCmd("taskkill","/IM",process);
        }
    }
    
    @Override
    protected void _lowlevel_stop(long pid, boolean force,boolean killTree) {
        if(force)
        {
            if(killTree)
            {
                CmdExec.execCmd("taskkill","/PID",""+pid,"/F","/T");
            }
            else
            {
                CmdExec.execCmd("taskkill","/PID",""+pid,"/F");
            }
        }
        else
        {
            CmdExec.execCmd("taskkill","/PID",""+pid);
        }
    }
    
    /**
     * Desliga o Computador após 30 segundos
     */
    @Override
    public void _shutdown()
    {
    	CmdExec.execCmd("shutdown","/s","/t","30","/c","seu computador desligará em 30 segundos");
    }
    
    /**
     * Reinicia o Computador após 30 segundos
     */
    @Override
    public void _restart()
    {
    	CmdExec.execCmd("shutdown","/r","/t","30","/c","seu computador reiniciará em 30 segundos");
    }
    
    /**
     * Faz logoff do Computador Imediatamente
     */
    @Override
    public void _logoff()
    {
    	CmdExec.execCmd("shutdown","/L");
    }
    
    /**
     * Cancela o desligamento
     */
    @Override
    public void _cancelshutdown()
    {
    	CmdExec.execCmd("shutdown","/a");
    }
    
    /**
     * Lista todos os programas rodando na máquina,
     * deve ser chamado no máximo a cada segundo, ou pode sobrecarregar o sistema,
     * ele chama com o comando "tasklist /NH /FO CSV"
     * @return a lista de programas rodando
     */
    public List<String> tasklist_running_programs()
    {
    	List<String> program_list = new ArrayList<>();
    	
		List<String> result = CmdExec.readCmd("tasklist","/NH","/FO","CSV");
		if(result == null || result.isEmpty())
		{
			System.out.println("comando tasklist retornou nulo.");
			return program_list;
		}
		for (String line : result)
		{
			List<String> csv = CSVUtils.parseLine(line);
			program_list.add(csv.get(0));
		}
    	return program_list;
    }
    
        
    /**
     * Lista todos os programas rodando na máquina,
     * alternativa ao tasklist,
     * ele chama com o comando "wmic process list status"
     * @return a lista de programas rodando
     */
    public List<String> wmic_running_programs()
    {
    	List<String> program_list = new ArrayList<>();
    	
		List<String> result = CmdExec.readCmd("wmic","process","list","status");
		if(result == null || result.isEmpty())
		{
			System.out.println("comando tasklist retornou nulo.");
			return program_list;
		}
		for (String line : result)
		{
            if(line != null && !line.isEmpty() && line.contains(" "))
            {
                String name = line.substring(0, line.indexOf(' '));
                program_list.add(name);
            }
		}
    	return program_list;
    }
    
     /**
     * Lista todos os programas rodando na máquina, usando JNA
     * a vantagem sobre tasklist e wmic é que demora de 3 a 5 ms, contra 100-120 ms
     *  //https://stackoverflow.com/questions/9877993/jna-query-windows-processes
     * @return a lista de programas rodando
     */
   
    public List<CmdProcess> jna_running_programs()
    {
        List<CmdProcess> result = new ArrayList<>();
        Kernel32 kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();          

        WinNT.HANDLE snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        try  {
            while (kernel32.Process32Next(snapshot, processEntry)) {             
                //System.out.println(processEntry.th32ProcessID + "\t" + Native.toString(processEntry.szExeFile));
                String name = Native.toString(processEntry.szExeFile);
                long pid = processEntry.th32ProcessID.longValue();
                long parent_pid = processEntry.th32ParentProcessID.longValue();
                result.add(new CmdProcess(name,pid,parent_pid));
                
            }
        }
        finally {
            kernel32.CloseHandle(snapshot);
        }
        return result;
    }
    /**
     * DEVERIA LISTAR SE É UMA JANELA, E O TÍTULO DA JANELA TAMBÉM
     * Lista todos os programas rodando na máquina,
     * ele chama com JNA
     * @return a lista de programas rodando
     */
    @Override
    public List<CmdProcess> _running_programs()
    {
    	return jna_running_programs();
    }
    
    @Override
    public long[] _memory_info()
    {
        Kernel32 kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
        WinBase.MEMORYSTATUSEX lpBuffer = new WinBase.MEMORYSTATUSEX();
        kernel32.GlobalMemoryStatusEx(lpBuffer);

        long[] info = new long[2];
        
        info[0] = lpBuffer.ullTotalPhys.longValue();
        info[1] = lpBuffer.ullAvailPhys.longValue();
        
        return info;
    }

    @Override
    protected float _cpu_info() {
        // wmic cpu get loadpercentage /format:value
        List<String> readc = readCmd("wmic","cpu","get","loadpercentage","/format:value");
        for(String s : readc)
        {
            if(!s.trim().isEmpty() && s.matches("LoadPercentage=\\d+"))
            {
                String percent = s.split("=")[1];
                return Float.parseFloat(percent);
            }
        }
        return 0.0f;
    }

    @Override
    protected List<CmdProcess> _running_executables() {
        List<String> readc = readCmd("wmic","process","get","ProcessID",",","ExecutablePath","/FORMAT:CSV");
        
        List<CmdProcess> result = new ArrayList<>();
        for(int i=0;i<readc.size();i++)
        {
            try
            {
                String s = readc.get(i);
                //System.out.println(s);
                if(s == null || s.isEmpty()) continue;
                List<String> line = CSVUtils.parseLine(s);
                if(line.size() == 3 && !line.get(1).isEmpty() && line.get(2).matches("\\d+"))
                {
                    File path = new File(line.get(1));
                    String name = path.getName();
                    long pid = Long.parseLong(line.get(2));
                    CmdProcess cmP = new CmdProcess(name, null, pid);
                    cmP.executable = path;
                    result.add(cmP);
                    
                }
            }
            catch(Exception e){
                System.err.println("erro na linha: '"+readc.get(i)+"'");
                e.printStackTrace();}
        }
        return result;
    }

    @Override
    protected void _stopService(String service) {
        CmdExec.execCmd("nssm.exe","stop",service);
    }

    @Override
    protected void _startService(String service) {
        CmdExec.execCmd("nssm.exe","start",service);
    }

 
    
    static interface User32 extends StdCallLibrary {

        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        interface WNDENUMPROC extends StdCallLibrary.StdCallCallback {

            boolean callback(Pointer hWnd, Pointer arg);
        }

        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
        
        boolean IsWindow(Pointer hWnd);
        
        boolean IsWindowVisible(Pointer hWnd);
        
        long GetWindowThreadProcessId(Pointer hWnd, Pointer lpdwProcessId);

        Pointer GetWindow(Pointer hWnd, int uCmd);
    }

    @Override
    public List<CmdProcess> _running_windows() {
        final List<CmdProcess> result = new ArrayList<CmdProcess>();
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new User32.WNDENUMPROC() {
            @Override
            public boolean callback(Pointer hWnd, Pointer arg) {
                
                boolean is_window = user32.IsWindow(hWnd);
                if(!is_window) return true;
                
                boolean visible = user32.IsWindowVisible(hWnd);
                if(!visible) return true;
                
                Memory pDev = new Memory(Pointer.SIZE); // allocate space to hold a pointer value
                pDev.clear(Pointer.SIZE);
                user32.GetWindowThreadProcessId(hWnd, pDev);
                long pid = pDev.getLong(0);
                
                
                byte[] windowText = new byte[512];
                
                user32.GetWindowTextA(hWnd, windowText, 512);
                String wText = Native.toString(windowText).trim();
                if (!wText.isEmpty()) {
                    //String running_window = wText.replace("\t\t", "")+"\t\t"+pid;
                    //System.out.println(running_window);
                    result.add(new CmdProcess(null,wText,pid));
                    
                }
                
                return true;
            }
        }, null);

        return result;
    }

    @Override
    public void _changeToWifi(String SSID) {
        // https://technet.microsoft.com/pt-br/library/dd744890(v=ws.10).aspx#bkmk_wlanConn
        // https://www.hanselman.com/blog/HowToConnectToAWirelessWIFINetworkFromTheCommandLineInWindows7.aspx
        // netsh wlan connect name=HANSELMAN-N
        String profile_name = getProfileName(SSID);
        if(profile_name != null)
        {
            CmdExec.execCmd("netsh","wlan","connect","name=\""+profile_name+"\"");
        }
        else
        {
            System.out.println("Não pode se conectar a rede wi-fi '"+SSID+"': não encontrou o Profile.");
        }
    }
    
    private String getProfileName(String SSID)
    {
        List<String> cmd_profiles = CmdExec.readCmd("netsh","wlan","show","profiles");
        String ProfileName = null;
        for(String s : cmd_profiles)
        {
            if(s.matches("^.*\\:.*$"))
            {
                String profile_name = s.substring(s.indexOf(":")+1,s.length());
                profile_name = profile_name.trim();
                List<String> cmd_details = CmdExec.readCmd("netsh","wlan","show","profiles",profile_name);
                for(String s1 : cmd_details)
                {
                    System.out.print(s1);
                    if(s1.contains("Nome SSID"))
                    {
                        
                         String profile_ssid = s1.substring(s1.indexOf(":")+1,s1.length());
                         profile_ssid = profile_ssid.trim();
                         
                         if(profile_ssid.startsWith("\""))
                         {
                            profile_ssid = profile_ssid.replace("\"","");
                         }
                         
                         System.out.println(" --> '"+profile_ssid+"'");
                         if(SSID.equals(profile_ssid))
                         {
                             return profile_name;
                         }
                    }
                    else
                    System.out.println("");
                }
            }
        }
        return null;
    }
}
