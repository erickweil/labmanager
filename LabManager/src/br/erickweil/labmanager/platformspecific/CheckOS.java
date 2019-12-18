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

/**
 * Classe que checa o sistema operacional que está sendo utilizado
 * @author Usuario
 */
public class CheckOS {
  	public static final String OS = System.getProperty("os.name").toLowerCase();
    
	public static void main(String[] args) {

        //Operating system name
        System.out.println(System.getProperty("os.name"));

        //Operating system version
        System.out.println(System.getProperty("os.version"));

        //Path separator character used in java.class.path
        System.out.println(System.getProperty("path.separator"));

        //User working directory
        System.out.println(System.getProperty("user.dir"));

        //User home directory
        System.out.println(System.getProperty("user.home"));

        //User account name
        System.out.println(System.getProperty("user.name"));

        //Operating system architecture
        System.out.println(System.getProperty("os.arch"));

        //Sequence used by operating system to separate lines in text files
        System.out.println(System.getProperty("line.separator").replace("\r","CR").replace("\n","LF"));

        System.out.println(System.getProperty("java.version")); //JRE version number

        System.out.println(System.getProperty("java.vendor.url")); //JRE vendor URL

        System.out.println(System.getProperty("java.vendor")); //JRE vendor name

        System.out.println(System.getProperty("java.home")); //Installation directory for Java Runtime Environment (JRE)

        System.out.println(System.getProperty("java.class.path"));

        System.out.println(System.getProperty("file.separator"));
        
		if (isWindows()) {
			System.out.println("This is Windows");
		} else if (isMac()) {
			System.out.println("This is Mac");
		} else if (isUnix()) {
			System.out.println("This is Unix or Linux");
		} else if (isSolaris()) {
			System.out.println("This is Solaris");
		} else {
			System.out.println("Your OS is not support!!");
		}
	}
    
    public static String getFamily()
    {
        if(isWindows()) return "windows";
        if(isMac()) return "mac";
        if(isUnix()) return "unix";
        if(isSolaris()) return "solaris";
        else return "unknown";
    }

	public static boolean isWindows() {
		return (OS.contains("win"));
	}

	public static boolean isMac() {
		return (OS.contains("mac"));
	}

	public static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") );
	}

	public static boolean isSolaris() {
		return (OS.contains("sunos"));
	}

}
