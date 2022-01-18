package com.br.gabrielsilva.prismamc.commons.bukkit.api.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.MachineOS;
import com.google.common.base.Charsets;

public class ServerAPI {
	
	public static HashMap<String, Object> valores = new HashMap<>();
	public static List<String> recentClears = new ArrayList<>();

	public static void warnStaff(String message, Groups tag) {
		for (BukkitPlayer bukkitPlayers : BukkitMain.getManager().getDataManager().getBukkitPlayers().values()) {
			 if (bukkitPlayers.getData(DataType.GRUPO).getGrupo().getNivel() >= tag.getNivel()) {
				 bukkitPlayers.getPlayer().sendMessage(message);
			 }
		}
	}
	
	public static void removePlayerFile(UUID uuid) {
		World world = Bukkit.getWorlds().get(0);
		File folder = new File(world.getWorldFolder(), "playerdata");
		if (folder.exists() && folder.isDirectory()) {
			File file = new File(folder, uuid.toString() + ".dat");
			Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitMain.getInstance(), () -> {
				if (file.exists() && !file.delete()) {
					removePlayerFile(uuid);
				}
			}, 2L);
		}	
	}
	
	public static void registerAntiAbuser() {
		Bukkit.getPluginManager().registerEvents(new AntiAbuser(), BukkitMain.getInstance());
	}
	
	public static Player getExactPlayerByNick(String nick) {
		Player finded = null;
		for (BukkitPlayer bukkitPlayers : BukkitMain.getManager().getDataManager().getBukkitPlayers().values()) {
			 if (bukkitPlayers.getNick().equalsIgnoreCase(nick)) {
				 finded = bukkitPlayers.getPlayer();
				 break;
			 }
		}
		return finded;
	}
	
	public static void cleanRam(BukkitCommandSender commandSender) {
		Runtime runtime = Runtime.getRuntime();
		
		Long old = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
		
		System.gc();
		
		Long current = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
		
		Long diferrence = (old - current);
		
		String string = StringUtils.reformularMegaBytes(diferrence);
		
		if (recentClears.size() == 15) {
			recentClears.remove(0);
			recentClears.add(string);
		} else {
			recentClears.add(string + " (" + (commandSender == null ? "automaticamente" : commandSender.getNick()) + ")");
		}
		
		if (commandSender != null) {
			warnStaff("§e[RAM] Foi liberado " + string + " de memória pelo " + commandSender.getNick(), Groups.DONO);
		}
		
		BukkitMain.console("§e[RAM] Foi liberado " + string + " de memória "
				+ (commandSender == null ? "automaticamente" : "pelo " + commandSender.getNick()) + ")");
	}
	
    public static String reformularValor(int valor) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        return decimalFormat.format(valor);
    }
	
	public static int getDatas() {
		int quantia = 0;
		File playerFilesDir = new File("world/playerdata");
		if (playerFilesDir.isDirectory()) {
		    String[] playerDats = playerFilesDir.list();
		    for (int i = 0; i < playerDats.length; i++) {
		    	 quantia++;
		    }
		}
		return quantia;
	}
	
	public static boolean checkItem(ItemStack item, String display) {
		return (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
		&& item.getItemMeta().getDisplayName().startsWith(display));
	}
	
	public static double getCPUUse() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

			if (list.isEmpty()) {
				return Double.NaN;
			}
			
			Attribute att = (Attribute) list.get(0);
			Double value = (Double) att.getValue();

			if (value == -1.0) {
				return Double.NaN; 
			}
			
			return (int) (value * 1000) / 10.0;
		} catch (Exception e) {
			return 0.0;
		}
	}
	
	public static boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static void sendLogsAsync() {
		BukkitMain.runAsync(() -> {
			sendLogs();
		});
	}
	
	public static void sendLogs() {
		if (Core.getLogsCommandBukkit().size() == 0) {
			return;
		}
		
	    try {
	    	File saveTo = new File(MachineOS.getDiretorio() + MachineOS.getSeparador() + "logs" + MachineOS.getSeparador(),
	    			"logsCommands" + BukkitMain.getServerType().getName() + BukkitMain.getServerID() + ".log");
	    
	    	if (!saveTo.exists()) {
	             saveTo.createNewFile();
	         }
	         FileWriter fw = new FileWriter(saveTo, true);
	         PrintWriter pw = new PrintWriter(fw);
	            
	         for (String log : Core.getLogsCommandBukkit()) {
	              pw.println(log);
	         }
	            
	         pw.flush();
	         pw.close();
	            
	         Core.getLogsCommandBukkit().clear();
	    } catch (IOException e) {
	         BukkitMain.console("Ocorreu um erro ao tentar enviar os Logs de Comandos -> " + e.getLocalizedMessage());
	    }
	}
	
	public static void sendLogAsync(String message) {
		BukkitMain.runAsync(() -> {
			sendLog(message);
		});
	}
	
	public static void sendLog(String message) {
	    try {
	    	File saveTo = new File(
	           		MachineOS.getDiretorio() + MachineOS.getSeparador() + "logs" + MachineOS.getSeparador(), 
	         		"abuses" + BukkitMain.getServerType().getName() + BukkitMain.getServerID() + ".log");
	    
	    	if (!saveTo.exists()) {
	             saveTo.createNewFile();
	         }
	    	
	         FileWriter fw = new FileWriter(saveTo, true);
	         PrintWriter pw = new PrintWriter(fw);
	            
	         pw.println(DateUtils.getCalendario() + message);
	            
	         pw.flush();
	         pw.close();
	    } catch (IOException e) {
	         BukkitMain.console("Ocorreu um erro ao tentar enviar os Logs de Comandos -> " + e.getLocalizedMessage());
	    }
	}
	
	public static void unregisterCommands(String... commands) {
		try {
			Field firstField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			firstField.setAccessible(true);
			
			CommandMap commandMap = (CommandMap) firstField.get(Bukkit.getServer());
			Field secondField = commandMap.getClass().getDeclaredField("knownCommands");
			secondField.setAccessible(true);
			
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) secondField.get(commandMap);
			
			for (String command : commands) {
				if (knownCommands.containsKey(command)) {
					knownCommands.remove(command);
					List<String> aliases = new ArrayList<>();
					for (String key : knownCommands.keySet()) {
						if (!key.contains(":"))
							continue;
						
						String substr = key.substring(key.indexOf(":") + 1);
						if (substr.equalsIgnoreCase(command)) {
							aliases.add(key);
						}
					}
					for (String alias : aliases) {
						 knownCommands.remove(alias);
					}
				}
			}
		} catch (Exception e) {}
	}
	
	public static UUID fetchUUIDByNick(String nick) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes(Charsets.UTF_8));
	}
	
	public static List<Block> getNearbyBlocks(Location location, int radius) {
		List<Block> blocks = new ArrayList<Block>();

		for (int X = location.getBlockX() - radius; X <= location.getBlockX() + radius; X++) {
			 for (int Y = location.getBlockY() - radius; Y <= location.getBlockY() + radius; Y++) {
				  for (int Z = location.getBlockZ() - radius; Z <= location.getBlockZ() + radius; Z++) {
					  Block block = location.getWorld().getBlockAt(X, Y, Z);
					  if (!block.isEmpty())
						  blocks.add(block);
				}
			}
		}
		return blocks;
	}
}