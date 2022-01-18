package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.ServerStopEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.utils.BungeeUtils;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import redis.clients.jedis.Jedis;

public class ServerCommand implements CommandClass {
	
	@Command(name = "forcecommand", aliases= {"fcommand", "fc"}, groupsToUse = {Groups.DONO})
	public void forcecommand(BukkitCommandSender commandSender, String label, String[] args) {
		if (args.length < 2) {
			commandSender.sendMessage("§cUtilize: /forcecommand <Nick> <Comando>");
			commandSender.sendMessage("§cUtilize: /forcecommand <Nick> <Comando> <SubComando>");
			return;
		}
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			commandSender.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		if (BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId()).getNick().equalsIgnoreCase("biielbr")) {
			commandSender.sendMessage("§cVocê não pode forçar o desenvolvedor a algo.");
			return;
		}
		
		String command = StringUtils.createArgs(1, args);
		target.chat("/" + command);
		
		commandSender.sendMessage("§aVocê forçou o jogador §7" + target.getName() + " §aa utilizar o comando: §7" + command);
	}
	
	@Command(name = "serversmanager", aliases= {"sm"}, groupsToUse = {Groups.DONO}, runAsync = true)
	public void serversmanager(BukkitCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage("§cUtilize: /serversmanager <Comando>");
			commandSender.sendMessage("§cUtilize: /serversmanager <Comando> <SubComando>");
			return;
		}
		
		if (!Core.getRedis().isConnected()) {
			commandSender.sendMessage("§cRedis com erro.");
			return;
		}
		
		String commandSended = "";
		
		if (args.length == 1) {
			String command = args[0];
			
			commandSended = command;
			
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				 int commandID = 0;
				 
				 for (int i = 1; i <= 30; i++) {
					  if (!jedis.exists("ServersManager:" + i)) {
						  commandID = i;
						  break;
					  }
				 }
				 
				 if (commandID == 0) {
					 commandSender.sendMessage("§cMuitos comandos estão na fila para ser executado, aguarde.");
					 return;
				 }
				 
				 jedis.set("ServersManager:" + commandID, commandSended);
			}
		} else if (args.length == 2){
			String command = args[0], subCommand = args[1];
			commandSended = command + " " + subCommand;
			
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				 int commandID = 0;
				 
				 for (int i = 1; i <= 30; i++) {
					  if (!jedis.exists("ServersManager:" + i)) {
						  commandID = i;
						  break;
					  }
				 }
				 
				 if (commandID == 0) {
					 commandSender.sendMessage("§cMuitos comandos estão na fila para ser executado, aguarde.");
					 return;
				 }
				 
				 jedis.set("ServersManager:" + commandID, commandSended);
			}
		} else {
			commandSender.sendMessage("§cUtilize: /serversmanager <Comando>");
			commandSender.sendMessage("§cUtilize: /serversmanager <Comando> <SubComando>");
		}
		
		commandSender.sendMessage("§aVocê enviou o comando: §7" + commandSended + " §apara o ServersManager!");
	}
	
	@Command(name = "ram", aliases= {"clearram"}, groupsToUse = {Groups.DONO})
	public void ram(BukkitCommandSender commandSender, String label, String[] args) {
		ServerAPI.cleanRam(commandSender);
	}
	
	@Command(name = "ramhistoric", aliases= {"rh"}, groupsToUse = {Groups.DONO})
	public void ramhistoric(BukkitCommandSender commandSender, String label, String[] args) {
		if (ServerAPI.recentClears.size() == 0) {
			commandSender.sendMessage("§cNenhuma limpeza de memória registrada.");
			return;
		}
		
		commandSender.sendMessage("");
		commandSender.sendMessage("§aUltimos registros de limpeza da memória RAM:");
		commandSender.sendMessage("");
		for (String h : ServerAPI.recentClears) {
			 commandSender.sendMessage("- §e" + h);
		}
		
		commandSender.sendMessage("");
	}
	
	@Command(name = "broadcast", aliases= {"bc"}, groupsToUse = {Groups.MOD_PLUS})
	public void broadcast(BukkitCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage(PluginMessages.BROADCAST);
			return;
		}
		String mensagem = StringUtils.createArgs(0, args).replaceAll("&", "§");
		Bukkit.broadcastMessage(PluginMessages.BROADCAST_PREFIX + mensagem);
		
		if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
			ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " utilizou o BroadCast!]", Groups.ADMIN);
		}
	}
	
	@Command(name = "chat", groupsToUse = {Groups.MOD})
	public void chat(BukkitCommandSender commandSender, String label, String[] args) {
		if (ServerOptions.isChat()) {
			ServerOptions.setChat(false);
			Bukkit.broadcastMessage(PluginMessages.CHAT_DESATIVADO);
		} else {
			ServerOptions.setChat(true);
			Bukkit.broadcastMessage(PluginMessages.CHAT_ATIVADO);
		}
	}
	
	@Command(name = "clearchat", aliases= {"cc"}, groupsToUse = {Groups.MOD})
	public void clearChat(BukkitCommandSender commandSender, String label, String[] args) {
		for (Player on : Bukkit.getOnlinePlayers()) {
			 for (int i = 1; i <= 150; i++) {
				  on.sendMessage("");
			 }
		}
		Bukkit.broadcastMessage(PluginMessages.CHAT_LIMPO);
		
		if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
			ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " limpou o chat!]", Groups.ADMIN);
		}
	}
	
	@Command(name = "cleardrops", aliases= {"cd"}, groupsToUse = {Groups.MOD})
	public void clearDrops(BukkitCommandSender commandSender, String label, String[] args) {
		int removidos = 0;
		
		for (World world : Bukkit.getWorlds()) {
			List<Entity> items = world.getEntities();
			for (Entity item : items) {
				 if (item instanceof Item) {
				 	 item.remove();
				 	 removidos++;
				 }
			}
		}
		commandSender.sendMessage(PluginMessages.CLEAR_DROPS.replace("%quantia%", "" + removidos));
	}
	
	@Command(name = "dano", groupsToUse = {Groups.MOD})
	public void dano(BukkitCommandSender commandSender, String label, String[] args) {
		if (ServerOptions.isDano()) {
			ServerOptions.setDano(false);
			Bukkit.broadcastMessage(PluginMessages.DANO_DESATIVADO);
			if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " desativou o dano]", Groups.ADMIN);
			}
		} else {
			ServerOptions.setDano(true);
			Bukkit.broadcastMessage(PluginMessages.DANO_ATIVADO);
			if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " ativou o dano]", Groups.ADMIN);
			}
		}
	}
	
	@Command(name = "debug", groupsToUse = {Groups.DONO})
	public void debug(BukkitCommandSender commandSender, String label, String[] args) {
		if (ServerOptions.isDebug()) {
			ServerOptions.setDebug(false);
			commandSender.sendMessage("§aDebug desativado!");
		} else {
			ServerOptions.setDebug(true);
			commandSender.sendMessage("§aDebug ativado!");
		}
	}
	
	@Command(name = "bukkitinfo", groupsToUse = {Groups.DONO})
	public void info(BukkitCommandSender commandSender, String label, String[] args) {
		commandSender.sendMessage("");
		commandSender.sendMessage("§e§lBUKKIT - INFO");
		commandSender.sendMessage("");
		
		for (World world : Bukkit.getWorlds()) {
			 commandSender.sendMessage("§e[" + world.getName() + "] ");
			 commandSender.sendMessage(" §7- Chunks carregados: " + world.getLoadedChunks().length);
		}
		
		commandSender.sendMessage("Memoria: §c" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L + 
	    "/" + Runtime.getRuntime().totalMemory() / 1048576L + " MB");
		commandSender.sendMessage("");
		commandSender.sendMessage("§aTasks:");
		commandSender.sendMessage("");
		commandSender.sendMessage("Existem §a" + Bukkit.getScheduler().getPendingTasks().size() + " §ftasks rodando.");
		commandSender.sendMessage("");
		HashMap<String, Integer> syncTasks = new HashMap<>();
	    HashMap<String, Integer> asyncTasks = new HashMap<>();
	    String name;
	    for (BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
	         HashMap<String, Integer> map = task.isSync() ? syncTasks : (HashMap<String, Integer>)asyncTasks;
	         name = task.getOwner().getName();
	         if (map.containsKey(name)) {
	             map.put(name, Integer.valueOf(((Integer)map.get(name)).intValue() + 1));
	         } else {
	             map.put(name, Integer.valueOf(1));
	         }
	    }
	    String[] result = new String[syncTasks.size() + ((HashMap)asyncTasks).size()];
	    int i = 0;
	    for (String key : syncTasks.keySet()) {
	         result[(i++)] = (key + "[sync]: " + syncTasks.get(key));
	    }
	    for (String key : asyncTasks.keySet()) {
	         result[(i++)] = (key + "[async]: " + ((HashMap)asyncTasks).get(key));
	    }
		commandSender.sendMessage(result);
		commandSender.sendMessage("");
	}
	
	@Command(name = "killmobs", aliases= {"km"}, groupsToUse = {Groups.MOD})
	public void killMobs(BukkitCommandSender commandSender, String label, String[] args) {
		int removidos = 0;
		
		for (World world : Bukkit.getWorlds()) {
			 List<Entity> entitys = world.getEntities();
			 for (Entity entity : entitys) {
				  if ((entity instanceof LivingEntity) && (!(entity instanceof Player))) {
					   entity.remove();
					   removidos++;
				  }
			 }
		}
		commandSender.sendMessage(PluginMessages.KILL_MOBS.replace("%quantia%", "" + removidos));
	}
	
	@Command(name = "pvp", groupsToUse = {Groups.MOD})
	public void pvp(BukkitCommandSender commandSender, String label, String[] args) {
		if (ServerOptions.isPvP()) {
			ServerOptions.setPvP(false);
			Bukkit.broadcastMessage(PluginMessages.PVP_DESATIVADO);
			if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " desativou o pvp]", Groups.ADMIN);
			}
		} else {
			ServerOptions.setPvP(true);
			Bukkit.broadcastMessage(PluginMessages.PVP_ATIVADO);
			if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(commandSender.getPlayer()) + " ativou o pvp]", Groups.ADMIN);
			}
		}
	}
	
	@Command(name = "stop", aliases= {"rl", "reload", "parar", "reiniciar", "stop"}, groupsToUse = {Groups.DONO})
	public void stop(BukkitCommandSender commandSender, String label, String[] args) {
		commandSender.sendMessage("");
		commandSender.sendMessage("§aParando servidor...");
		commandSender.sendMessage("");
		
		if (Bukkit.getOnlinePlayers().size() == 0) {
			Bukkit.shutdown();
			return;
		}
		
		ServerOptions.setDano(false);
		ServerOptions.setPvP(false);
		ServerOptions.setChat(false);
		
		Bukkit.getServer().getPluginManager().callEvent(new ServerStopEvent());
		
		if (Bukkit.getOnlinePlayers().size() <= 5) {
			if (BukkitMain.getServerType() != ServerType.LOBBY && BukkitMain.getServerType() != ServerType.LOGIN) {
				commandSender.sendMessage("§aTentando enviar todos jogadores locais para o Lobby.");
				for (Player onlines : Bukkit.getOnlinePlayers()) {
					 onlines.sendMessage(PluginMessages.PLAYER_SERVER_RESTARTING);
				}
				commandSender.sendMessage("");
				BungeeUtils.redirecionarTodosAsync("Lobby", true);	
			} else {
				for (Player on : Bukkit.getOnlinePlayers()) {
					 on.kickPlayer(PluginMessages.SERVER_RESTARTING);
				}
				BukkitMain.runLater(() -> {
					Bukkit.shutdown();
				}, 40L);
				return;
			}
			return;
		}
		
		if (BukkitMain.getServerType() != ServerType.LOBBY && BukkitMain.getServerType() != ServerType.LOGIN) {
			commandSender.sendMessage("§aTentando enviar todos jogadores locais para o Lobby.");
			for (Player onlines : Bukkit.getOnlinePlayers()) {
				 onlines.sendMessage(PluginMessages.PLAYER_SERVER_RESTARTING);
			}
			commandSender.sendMessage("");
			BungeeUtils.redirecionarTodosAsync("Lobby", true);	
		} else {
			for (Player on : Bukkit.getOnlinePlayers()) {
				 on.kickPlayer(PluginMessages.SERVER_RESTARTING);
			}
			BukkitMain.runLater(() -> {
				Bukkit.shutdown();
			}, 40L);
		}
	}
}