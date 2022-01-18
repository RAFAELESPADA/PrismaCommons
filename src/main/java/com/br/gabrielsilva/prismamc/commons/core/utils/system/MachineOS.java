package com.br.gabrielsilva.prismamc.commons.core.utils.system;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import com.br.gabrielsilva.prismamc.commons.core.Core;

public class MachineOS {

	public static boolean isLinux;
	
	public static String OS = System.getProperty("os.name").toLowerCase(),
			machineIP = "erro";
	
	public MachineOS() {
		if (isWindows()) {
			isLinux = false;
		} else if (isUnix()) {
			isLinux = true;
		}
		machineIP = getRealIP();
		
		Core.console("§a[IP DA MAQUINA ATUAL] -> " + machineIP);
	}
	
	public static String getMachineIP() {
		return machineIP;
	}
	
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
	    return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
	    return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}
	    
	public static String getOS() {
		if (isWindows()) {
	        return "win";
		} else if (isMac()) {
	        return "osx";
		} else if (isUnix()) {
	       return "uni";
		} else if (isSolaris()) {
	       return "sol";
		} else {
	       return "err";
		}
	}
	
	public static String getDiretorio() {
		if (isLinux) {
			return "/root/plugins/PrismaConfig";
		}
		return "C:\\plugins\\PrismaConfig";
	}
	
	public static String getSeparador() {
		if (isLinux) {
			return "/";
		}
		return "\\";
	}
	
	private static String getRealIP() {
		try {
			URLConnection connect = new URL("http://checkip.amazonaws.com/").openConnection();
			connect.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			Scanner scan = new Scanner(connect.getInputStream());
			StringBuilder sb = new StringBuilder();
			while (scan.hasNext()) {
				sb.append(scan.next());
			}
			scan.close();
			return sb.toString();
		} catch (Exception ex) {
			return "Erro";
		}
	}
}