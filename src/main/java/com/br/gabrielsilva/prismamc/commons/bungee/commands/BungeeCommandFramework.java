package com.br.gabrielsilva.prismamc.commons.bungee.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeeCommandFramework implements CommandFramework {

	public final static ExecutorService commandExecutor = Executors
			.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Command Executor Bungee").build());
	
	public static int COMMANDS_EXECUTEDS = 0;
	
	private final Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
	private final Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();
	private final Plugin plugin;

	public BungeeCommandFramework(Plugin plugin) {
		this.plugin = plugin;
		this.plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeCompleter());
	}

	public boolean handleCommand(CommandSender sender, String label, String[] args) {
		StringBuilder line = new StringBuilder();
		
		line.append(label);
		
		for (int i = 0; i < args.length; i++) {
			 line.append(" " + args[i]);
		}
		
		for (int i = args.length; i >= 0; i--) {
			 StringBuilder buffer = new StringBuilder();
			 buffer.append(label.toLowerCase());
			 
			 for (int x = 0; x < i; x++) {
				 buffer.append(".").append(args[x].toLowerCase());
			 }
			 
		 	 String cmdLabel = buffer.toString();
			 if (commandMap.containsKey(cmdLabel)) {
				 Entry<Method, Object> entry = commandMap.get(cmdLabel);
				 Command command = entry.getKey().getAnnotation(Command.class);
				
				 if (sender instanceof ProxiedPlayer) {
				 	 ProxiedPlayer p = (ProxiedPlayer) sender;
					
					 if (!BungeeMain.isValid(p)) {
						 p.sendMessage(PluginMessages.NAO_POSSUI_SESSAO);
						 Core.getLogsCommandBungee().add(
									DateUtils.getCalendario() + p.getName() + " tentou utilizar o comando: " + line + " e falhou (Não Possuí Sessão) "
											+ " ["+p.getServer().getInfo().getName() + "]");
						 return true;
					 }
					
					 Groups tagPlayer = BungeeMain.getManager().getSessionManager().getSession(p).getGrupo();
					
					 boolean semPermissao = true;
					 for (int uses = 0; uses < command.groupsToUse().length; uses++) {
						  Groups tag = command.groupsToUse()[uses];
						  if (tagPlayer.getNivel() >= tag.getNivel()) {
							  semPermissao = false;
							  break;
						  }
					 }
					
					 tagPlayer = null;
					
					 if (semPermissao) {
						 if (hasCommand(p, command.name().toLowerCase())) {
						 	 semPermissao = false;
					 	 }
					 }
					 
					 if (semPermissao) {
						 p.sendMessage(PluginMessages.SEM_PERMISSÃO);
						 Core.getLogsCommandBungee().add(
									DateUtils.getCalendario() + p.getName() + " tentou utilizar o comando: " + line + " e falhou (Sem Permissão) "
											+ " ["+p.getServer().getInfo().getName() + "]");
						 return true;
					 }
					 Core.getLogsCommandBungee().add(
								DateUtils.getCalendario() + p.getName() + " utilizou o comando: " + line
										+ " ["+p.getServer().getInfo().getName() + "]");
					 p = null;
				 } else {
					 Core.getLogsCommandBungee().add(
								DateUtils.getCalendario() + "CONSOLE" + " utilizou o comando: " + line);
				 }

				 COMMANDS_EXECUTEDS++;
				
				 if (command.runAsync()) {
					 commandExecutor.execute(() -> {
						 try {
						 	 entry.getKey().invoke(entry.getValue(), new BungeeCommandSender(sender), label, args);
					 	 } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
							 e.printStackTrace();
						 }
					 });
				 } else {
					 try {
						 entry.getKey().invoke(entry.getValue(), new BungeeCommandSender(sender), label, args);
				 	 } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
						 e.printStackTrace();
					 }
				 }
				 return true;
			 }
		}
		return true;
	}
	
	public boolean hasCommand(ProxiedPlayer proxiedPlayer, String command) {
		if (proxiedPlayer.hasPermission("prismamc.cmd.all")) {
			return true;
		}
		if (proxiedPlayer.hasPermission("prismamc.cmd." + command)) {
			return true;
		}
		return false;
	}

	@Override
	public void registerCommands(CommandClass cls) {
		for (Method m : cls.getClass().getMethods()) {
			if (m.getAnnotation(Command.class) != null) {
				Command command = m.getAnnotation(Command.class);
				if (m.getParameterTypes().length > 3 || m.getParameterTypes().length <= 2
						|| !BungeeCommandSender.class.isAssignableFrom(m.getParameterTypes()[0])
								&& !String.class.isAssignableFrom(m.getParameterTypes()[1])
								&& !String[].class.isAssignableFrom(m.getParameterTypes()[2])) {
					System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
					continue;
				}
				registerCommand(command, command.name(), m, cls);
				for (String alias : command.aliases()) {
					registerCommand(command, alias, m, cls);
				}
			} else if (m.getAnnotation(Completer.class) != null) {
				Completer comp = m.getAnnotation(Completer.class);
				if (m.getParameterTypes().length > 3 || m.getParameterTypes().length <= 2
						|| m.getParameterTypes()[0] != ProxiedPlayer.class
								&& m.getParameterTypes()[1] != String.class
								&& m.getParameterTypes()[2] != String[].class) {
					System.out.println(
							"Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
					continue;
				}
				if (m.getReturnType() != List.class) {
					System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
					continue;
				}
				registerCompleter(comp.name(), m, cls);
				for (String alias : comp.aliases()) {
					registerCompleter(alias, m, cls);
				}
			}
		}
	}

	/**
	 * Registers all the commands under the plugin's help
	 */

	private void registerCommand(Command command, String label, Method m, Object obj) {
		Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
		commandMap.put(label.toLowerCase(), entry);
		String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

		net.md_5.bungee.api.plugin.Command cmd;
		if (command.permission().isEmpty())
			cmd = new BungeeCommand(cmdLabel);
		else
			cmd = new BungeeCommand(cmdLabel, command.permission());
		plugin.getProxy().getPluginManager().registerCommand(plugin, cmd);
	}

	private void registerCompleter(String label, Method m, Object obj) {
		completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
	}

	class BungeeCommand extends net.md_5.bungee.api.plugin.Command {

		protected BungeeCommand(String label) {
			super(label);
		}

		protected BungeeCommand(String label, String permission) {
			super(label, permission);
		}

		@Override
		public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
			handleCommand(sender, getName(), args);
		}

	}

	public class BungeeCompleter implements Listener {

		@EventHandler
		public void onTabComplete(TabCompleteEvent event) {
			if (!(event.getSender() instanceof ProxiedPlayer))
				return;
			ProxiedPlayer player = (ProxiedPlayer) event.getSender();
			String[] split = event.getCursor().replaceAll("\\s+", " ").split(" ");
			String[] args = new String[split.length - 1];
			for (int i = 1; i < split.length; i++) {
				args[i - 1] = split[i];
			}
			String label = split[0].substring(1);
			for (int i = args.length; i >= 0; i--) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(label.toLowerCase());
				for (int x = 0; x < i; x++) {
					buffer.append(".").append(args[x].toLowerCase());
				}
				String cmdLabel = buffer.toString();
				if (completers.containsKey(cmdLabel)) {
					Entry<Method, Object> entry = completers.get(cmdLabel);
					try {
						event.getSuggestions().clear();
						event.getSuggestions()
								.addAll((List<String>) entry.getKey().invoke(entry.getValue(), player, label, args));
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}
