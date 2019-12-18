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

import br.erickweil.labamanger.common.files.FilesHelper;
import br.erickweil.labmanager.platformspecific.CheckOS;
import br.erickweil.labmanager.platformspecific.CmdExecMac;
import br.erickweil.labmanager.platformspecific.CmdExecUnix;
import br.erickweil.labmanager.platformspecific.CmdExecWin;
import br.erickweil.labmanager.start.Inicio;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinBase.MEMORYSTATUSEX;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

/**
 * Classe que executa ações no computador, pela linha de comando.
 * os métodos expostos apenas executam as ações requisitadas, não permitindo
 * a execução de um comando remoto
 * 
 * idealmente funcionaria em qualquer sistema operacional, 
 * mas por enquanto esses métodos funcionam no windows (xp, Vista, 7, 8, 10)
 * e alguns comandos funcionam também em sistemas Unix.
 * @author Usuario
 */
public abstract class CmdExec {

    public static final CmdExec INSTANCE = getInstace();
    private static CmdExec getInstace()
    {
        if(CheckOS.isWindows()) return new CmdExecWin();
        else if(CheckOS.isMac()) return new CmdExecMac();
        else if(CheckOS.isUnix()) return new CmdExecUnix();
        //else if(CheckOS.isSolaris()) return new CmdExecSolaris();
        else return null;
    }
    public static void main(String[] args) throws IOException
    {
        /*BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line = reader.readLine()) != null)
        {
            //String[] cmd = new String[]{"cmd","/c",line};
            String[] cmd = line.split("&");
            execCmd(cmd);
        }*/
        //start("notepad");
        CmdExec instance = CmdExec.getInstace();
        if(instance == null)
        {
            System.out.println("Esse Sistema operacional não é suportado");
        }
        else
        {
            List<CmdProcess> process = instance.running_windows();
            for(CmdProcess p : process)
            {
                System.out.println(p);
            }
        }
        
        System.out.println(getIPandMAC(null)[0]+":"+getIPandMAC(null)[1]);
    }
    

	/**
	* inicia um processo com o process builder
	* path é o caminho do diretório que o processo irá enxergar
	* inheritIO:
	*   true: você não vai ler o output do processo
	*   false: você deve ler o output do processo ou ele pode travar
	*/
    private static Process startProcess(String[] cmd,String path,boolean inheritIO)
    {
    	try {
    		ProcessBuilder builder = new ProcessBuilder(cmd);
		    if(path != null)
		    	builder.directory(new File(path));
			//https://stackoverflow.com/questions/36559284/should-i-call-process-destroy-if-the-process-ends-with-exit-code-0
		    builder.redirectErrorStream(true);
			//https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/
			if(inheritIO)
			builder.inheritIO();
			return builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
	
	private static void execCmdPath(String[] cmd,String path)
    {
        String cmd_executed = String.join(" ", cmd);
        Process p = null;
        try
        {
            
            System.out.println("Executando o cmd '"+cmd_executed+"':");
            p = startProcess(cmd, path, false);
            if(p == null) return;
            try (BufferedReader reader = getReader(p))
            {
                String line;
                
                while ((line = reader.readLine()) != null)
                {
                    System.out.println("'"+line+"'");
                }
            }
            int exitcode = p.waitFor();
            if (exitcode != 0)
            {
                System.out.println("erro ao executar o comando:" + cmd_executed);
            }
           
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (p != null)
            {
                p.destroy();
                // limpa a memória usada pelo Process, talvez não precise disso, mas assim diminuiu uso de memória
                System.gc();
            }
            System.out.println("Terminou o cmd '"+cmd_executed+"':");
        }
	}
	
    /**
     * USE COM CUIDADO - System specific
     * por enquanto só Install.java usa!
     * e o clientcmdprotocol
     * @param cmd 
     */
    public static void execCmd(final String ... cmd)
    {
		execCmdPath(cmd,null);
    }
	
	
    
    private static BufferedReader getReader(Process p)
    {
	        //https://stackoverflow.com/questions/8398277/which-encoding-does-process-getinputstream-use
	        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(),Charset.forName("CP850")));
	        return r;
    }
    
    private static BufferedWriter getWriter(Process p)
    {
	        BufferedWriter r = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(),Charset.forName("CP850")));
	        return r;
    }
    
    public static List<String> readCmd(String ... cmd)
    {
    	List<String> lines = new ArrayList<String>();
		Process p =null;
    	 try {
            if(!Inicio._logtofile)System.out.println("readCmd: "+Arrays.toString(cmd)+"");
			p = startProcess(cmd,null,false);
            if(p == null) return null;
            try (BufferedReader reader = getReader(p))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    lines.add(line);
                    if(!Inicio._logtofile)
                    {
                        System.out.println("'"+line+"'");
                    }
                }
            }
			int exitcode = p.waitFor();
			if(exitcode != 0)
			{
				System.out.println("erro ao executar o comando:"+Arrays.toString(cmd));
			}
			return lines;
 		} catch (IOException | InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
			return null;
 		}
		finally
		{
			if(p != null)
            {
                p.destroy();
                // limpa a memória usada pelo Process, talvez não precise disso, mas assim diminuiu uso de memória
                System.gc();
            }
		}
    	
    }
    
    public static boolean validURL(String site)
    {
        try {
            URL t = new URL(site);
            return t != null;
        } catch (MalformedURLException ex) {
            Logger.getLogger(CmdExec.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    /*
     * Inicia a execução de um programa
	 * ***tem que ver se esse é o jeito certo de iniciar um programa, usando cmd /c 'start chrome.exe'
	 * ***ou dando o caminho completo
     * @param program -> programa a ser iniciado
     */
    /*
    public static void start(String program,String ... args)
    {
    	switch(program)
    	{
            case "windows explorer": execCmd("cmd","/c","start explorer.exe"); break;
			case "firefox": execCmd("cmd","/c","start firefox.exe"); break;
			case "chrome": execCmd("cmd","/c","start chrome.exe"); break;
			case "internet explorer": execCmd("cmd","/c","start iexplore.exe"); break;
			case "kiosk-chrome": if(validURL(args[0]))execCmd("cmd","/c","start chrome.exe --kiosk "+args[0]); break;
			case "kiosk-iexplore": if(validURL(args[0]))execCmd("cmd","/c","start iexplore.exe -k "+args[0]); break;
            case "kiosk-opera": if(validURL(args[0]))execCmd("cmd","/c","start opera.exe /KioskMode "+args[0]); break;
			case "portugol": 
			{
				String[] portugol_cmd = 
				new String[] {
						"C:\\ProgramData\\UNIVALI\\Portugol Studio\\java\\java-windows\\bin\\javaw.exe",
						"-jar",
						"inicializador-ps.jar"
				};
				String portugol_path = "C:/ProgramData/UNIVALI/Portugol Studio";
				execCmdPath(portugol_cmd,portugol_path);
			}
            break;
            case "netbeans": 
            {
				String[] cmd = 
				new String[] {
						"C:\\Program Files\\NetBeans 8.0\\bin\\netbeans64.exe"
				};
				String path = "C:\\Program Files\\NetBeans 8.0";
				execCmdPath(cmd,path);
			}
			break;
    	}
    }*/
    
       
    /**
     * Para a execução de um programa, apenas se ele estiver listado como processo aberto.
     * @param program -> programa a ser parado
     */
    public static void stop(String process,boolean force)
    {
        if(is_running_process(process))
        {
            lowlevel_stop(process,force);
        }        
    }
    
    public static void stop(CmdProcess process,boolean force)
    {
        if(process.pid > 0)
        {
            lowlevel_stop(process.pid, force);
        }
        else if(process.executable != null)
        {
            String absoluteExe = process.executable.getAbsolutePath();
            boolean directory = process.executable.isDirectory();
            List<CmdProcess> running_executables = INSTANCE._running_executables();
            for(CmdProcess p : running_executables)
            {
                if(p == null || p.executable == null) continue;
                if(directory)
                {
                    if(FilesHelper.isInside(p.executable,process.executable))
                    {
                        lowlevel_stop(p.pid,force,true);
                    }
                }
                else
                {
                    if(p.executable.getAbsolutePath().equals(absoluteExe))
                    {
                        lowlevel_stop(p.pid,force,true);
                    }
                }
            }
        }
        else if(process.name != null)
        {
            lowlevel_stop(process.name, force);
        }
        else if(process.windowTitle != null)
        {
            List<CmdProcess> windows = running_windows();
            for(CmdProcess w : windows)
            {
                if(w.windowTitle.trim().equalsIgnoreCase(process.windowTitle.trim()))
                {
                    lowlevel_stop(w.pid, force);
                    return;
                }
            }
            System.out.println("não pôde encerrar o processo '"+process+"', nenhuma janela com o nome '"+process.windowTitle+"'");
        }
        else{
            System.out.println("não pôde encerrar o processo '"+process+"', não há informações sufientes");
        }
            
    }
    
    public static String[] getIPandMAC(String IP)
    {
        String MAC;
        byte[] mac = new byte[6];
        
        try
        {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            if(IP != null)
            {
                while(networkInterfaces.hasMoreElements())
                {
                    NetworkInterface net = networkInterfaces.nextElement();
                    if(net.getInetAddresses().nextElement().getHostAddress().equals(IP))
                    {
                        mac = net.getHardwareAddress();
                    }
                }
            }
            else
            {
                networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while(networkInterfaces.hasMoreElements())
                {
                    NetworkInterface net = networkInterfaces.nextElement();
                    if(net.isVirtual() || !net.isUp() || net.isLoopback()) continue;
                    String disp = net.getDisplayName().toLowerCase();
                    if(disp.contains("hamachi") || disp.contains("virtualbox")) continue;
                    //System.out.println(net.getDisplayName()+":"+net.getInetAddresses().nextElement().getHostAddress());
                    
                    IP = net.getInetAddresses().nextElement().getHostAddress();
                    mac = net.getHardwareAddress();
                    break;
                }
                if(mac == null)
                {
                    while(networkInterfaces.hasMoreElements())
                    {
                        NetworkInterface net = networkInterfaces.nextElement();
                        if(net.isVirtual() || !net.isUp() || net.isLoopback()) continue;
                        
                        IP = net.getInetAddresses().nextElement().getHostAddress();
                        mac = net.getHardwareAddress();
                        break;
                    }
                }
            }
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }

        MAC = sb.toString();
        

        return new String[]{IP,MAC};
    }
    /** pega o usuario logado agora
     * 
     */
    public static String getUser()
    {
        return INSTANCE._getUser();
    }
    
	/**
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
    public static void lowlevel_stop(String program,boolean force)
    {
        INSTANCE._lowlevel_stop(program,force,false);
    }
    
    public static void lowlevel_stop(long pid,boolean force)
    {
        INSTANCE._lowlevel_stop(pid,force,false);
    }
    
    public static void lowlevel_stop(String program,boolean force,boolean killTree)
    {
        INSTANCE._lowlevel_stop(program,force,killTree);
    }
    
    public static void lowlevel_stop(long pid,boolean force,boolean killTree)
    {
        INSTANCE._lowlevel_stop(pid,force,killTree);
    }
    
    /**
     * Desliga o Computador após 30 segundos
     */
    public static void shutdown()
    {
        INSTANCE._shutdown();
    }
    
    /**
     * Reinicia o Computador após 30 segundos
     */
    public static void restart()
    {
        INSTANCE._restart();
    }
    
    /**
     * Faz logoff do Computador Imediatamente
     */
    public static void logoff()
    {
        INSTANCE._logoff();
    }
    
    /**
     * Cancela o desligamento
     */
    public static void cancelshutdown()
    {
        INSTANCE._cancelshutdown();
    }
    
    /**
     * DEVERIA LISTAR SE É UMA JANELA, E O TÍTULO DA JANELA TAMBÉM
     * Lista todos os programas rodando na máquina,
     * ele chama com JNA
     * @return a lista de programas rodando
     */
    public static List<CmdProcess> running_programs()
    {
        return INSTANCE._running_programs();
    }
   
     /**
     * memoria disponivel/memoria utilizada
     * @return memoria
     */
    public static long[] memory_info()
    {
        return INSTANCE._memory_info();
    }
    
     /**
     * cpu %
     * @return memoria
     */
    public static float cpu_info()
    {
        return INSTANCE._cpu_info();
    }

    public static List<CmdProcess> running_windows()
    {
        return INSTANCE._running_windows();
    }
    
    public static List<CmdProcess> running_executables()
    {
        return INSTANCE._running_executables();
    }
    
    
    public static boolean is_running_process(String process)
    {
        List<CmdProcess> programs = running_programs();
        for(CmdProcess c : programs)
        {
            if(c.name.equalsIgnoreCase(process))
            {
                return true;
            }
        } 
        return false;
    }
    
    public static boolean is_running_window(String windowTitle)
    {
        List<CmdProcess> programs = running_programs();
        for(CmdProcess c : programs)
        {
            try
            {
                if(c.windowTitle.equalsIgnoreCase(windowTitle) || c.windowTitle.matches(windowTitle))
                {
                    return true;
                }
            }
            catch(PatternSyntaxException ex){ /* quando o caminho não é um regex, pode dar erro, mas tudo bem. */}
        } 
        return false;
    }

    public static void changeToWifi(String SSID)
    {
        INSTANCE._changeToWifi(SSID);
    }
    
    public static void stopService(String service)
    {
        INSTANCE._stopService(service);
    }
    
    public static void startService(String service)
    {
        INSTANCE._startService(service);
    }



    protected abstract String _getUser();
    
    protected abstract void _lowlevel_stop(String program,boolean force, boolean killTree);
    
    protected abstract void _lowlevel_stop(long pid,boolean force, boolean killTree);
    
    protected abstract void _shutdown();
 
    protected abstract void _restart();
    
    protected abstract void _logoff();
  
    protected abstract void _cancelshutdown();

    protected abstract List<CmdProcess> _running_programs();
    
    protected abstract long[] _memory_info();
    
    protected abstract float _cpu_info();

    protected abstract List<CmdProcess> _running_windows();
    
    protected abstract List<CmdProcess> _running_executables();

    protected abstract void _changeToWifi(String SSID);

    protected abstract void _stopService(String service);
    
    protected abstract void _startService(String service);
}
