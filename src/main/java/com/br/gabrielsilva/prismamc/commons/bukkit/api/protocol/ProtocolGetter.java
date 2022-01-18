package com.br.gabrielsilva.prismamc.commons.bukkit.api.protocol;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

public class ProtocolGetter {

	//Protocol Support
	static Class<?> ProtocolSupportAPI;
	static Class<?> ProtocolVersion;
	static Method ProtocolSupportAPI_getProtocolVersion;
	static Method ProtocolVersion_getId;
	
	static {
		try {
			ProtocolSupportAPI = Class.forName("protocolsupport.api.ProtocolSupportAPI");
			ProtocolVersion = Class.forName("protocolsupport.api.ProtocolVersion");

			ProtocolSupportAPI_getProtocolVersion = getMethod(ProtocolSupportAPI, "getProtocolVersion", new Class[] { Player.class });
			ProtocolVersion_getId = getMethod(ProtocolVersion, "getId");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getVersion(Player p) {
		try {
			Object protocolVersion = ProtocolSupportAPI_getProtocolVersion.invoke(null, p);
			int id = (int) ProtocolVersion_getId.invoke(protocolVersion);
			return id;
		} catch (Exception ex) {
			return 0;
		}
	}
	
	public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
				m.setAccessible(true);
				return m;
			}
		}
		return null;
	}

	public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
		boolean equal = true;
		if (l1.length != l2.length) { return false; }
		for (int i = 0; i < l1.length; i++) {
			if (l1[i] != l2[i]) {
				equal = false;
				break;
			}
		}
		return equal;
	}
}