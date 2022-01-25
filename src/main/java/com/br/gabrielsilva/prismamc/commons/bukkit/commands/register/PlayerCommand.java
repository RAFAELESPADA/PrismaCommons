package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.FakeAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.Sidebar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.ScoreboardChangeEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.ScoreboardChangeEvent.ChangeType;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.DoubleCoinsInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.DoubleExperienceInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.PreferencesInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.ReportInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.YoutuberInventory;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Completer;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class PlayerCommand implements CommandClass {
	
	public static List<String> fakesRandom = new ArrayList<>();
    
	@Command(name = "youtuber", aliases = {"yt", "yts", "requisitos"})
	public void youtuber(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		new YoutuberInventory().open(player);
	}
	
	@Command(name = "preferencias", aliases= {"preferences", "options", "config"})
	public void preferencias(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		new PreferencesInventory(player).open(player);
	}
	
	@Command(name = "doublecoins", aliases= {"dc"})
	public void doublecoins(BukkitCommandSender commandSender, String label, String[] args) {
		String nick = commandSender.getNick();
		
		if (args.length == 0) {
			if (nick.equalsIgnoreCase("console")) {
				commandSender.sendMessage("§cUtilize: /doublecoins <On/Off>");
				return;
			}
			Player player = commandSender.getPlayer();
			new DoubleCoinsInventory(player).open(player);
		} else if (args.length == 1) {
			boolean continuar = true;
			if (!nick.equalsIgnoreCase("console")) {
				Player player = commandSender.getPlayer();
				if (BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() < 
						Groups.ADMIN.getNivel()) {
					player.sendMessage("§cVocê não tem permissão para usar este comando.");
					continuar = false;
					return;
				}
			}
			if (!continuar) {
				return;
			}
			if (args[0].equalsIgnoreCase("on")) {
				if (ServerOptions.isDoubleCoins()) {
					commandSender.sendMessage("§cO DoubleCoins ja está ativado!");
				} else {
					commandSender.sendMessage("§aVocê ativou o DoubleCoins!");
					ServerOptions.setDoubleCoins(true);
					Bukkit.broadcastMessage("§eAgora este servidor dará o §6§lDOBRO §ede moedas.");
				}
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!ServerOptions.isDoubleCoins()) {
					commandSender.sendMessage("§cO DoubleCoins não está ativado!");
				} else {
					commandSender.sendMessage("§aVocê desativou o DoubleCoins!");
					ServerOptions.setDoubleCoins(false);
					Bukkit.broadcastMessage("§cAgora o servidor dará a quantia normal de moedas, DoubleCoins desativado.");
				}
			} else {
				commandSender.sendMessage("§cUtilize: /doublecoins <On/Off>");
			}
		}
	}
	
	@Command(name = "doublexp", aliases= {"dxp"})
	public void doublexp(BukkitCommandSender commandSender, String label, String[] args) {
		String nick = commandSender.getNick();
		
		if (args.length == 0) {
			if (nick.equalsIgnoreCase("console")) {
				commandSender.sendMessage("§cUtilize: /doublexp <On/Off>");
				return;
			}
			Player player = commandSender.getPlayer();
			
			new DoubleExperienceInventory(player).open(player);
		} else if (args.length == 1) {
			boolean continuar = true;
			if (!nick.equalsIgnoreCase("console")) {
				Player player = commandSender.getPlayer();
				if (BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() < 
						Groups.ADMIN.getNivel()) {
					player.sendMessage("§cVocê não tem permissão para usar este comando.");
					continuar = false;
					return;
				}
			}
			if (!continuar) {
				return;
			}
			if (args[0].equalsIgnoreCase("on")) {
				if (ServerOptions.isDoubleXP()) {
					commandSender.sendMessage("§cO DoubleXP ja está ativado!");
				} else {
					commandSender.sendMessage("§aVocê ativou o DoubleXP!");
					ServerOptions.setDoubleXP(true);
					Bukkit.broadcastMessage("§bAgora este servidor dará o §lDOBRO §bde XP.");
				}
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!ServerOptions.isDoubleXP()) {
					commandSender.sendMessage("§cO DoubleXP não está ativado!");
				} else {
					commandSender.sendMessage("§aVocê desativou o DoubleXP!");
					ServerOptions.setDoubleXP(false);
					Bukkit.broadcastMessage("§cDoubleXP desativado, quantia de XP distribuída normalizada.");
				}
			} else {
				commandSender.sendMessage("§cUtilize: /doublexp <On/Off>");
			}
		}
	}
	
	@Command(name = "ping", aliases = {"p", "ms", "latencia"})
	public void ping(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player p = commandSender.getPlayer();
		if (args.length == 0) {
			p.sendMessage(PluginMessages.SEU_PING.replace("%quantia%", "" + getPing(p)));
			return;
		} else if (args.length == 1) {
			if (!commandSender.hasPermission("ping"))
				return;
			
			Player p1 = Bukkit.getPlayer(args[0]);
			if (p1 == null) {
				p.sendMessage(PluginMessages.JOGADOR_OFFLINE);
				return;
			}
			p.sendMessage(PluginMessages.PING_OUTRO.replace("%nick%", p1.getName()).replace("%quantia%", "" + getPing(p1)));
		}
	}
	
	@Command(name = "reports", groupsToUse= {Groups.YOUTUBER_PLUS}, runAsync=true)
	public void reports(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		Player player = commandSender.getPlayer();
		
		new ReportInventory(player).open(player);
	}
	
	@Command(name = "tell", aliases = {"pm"}, runAsync=true)
	public void tell(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		
		if (args.length == 0) {
			player.sendMessage("");
			player.sendMessage("§cUse: /tell <Nick> <Mensagem>");
			player.sendMessage("§cUse: /tell <On/Off>");
			player.sendMessage("");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
				if (dataHandler.getBoolean(DataType.TELL)) {
					player.sendMessage("§cSeu TELL ja está ativado.");
					return;
				}
  			    dataHandler.getData(DataType.TELL).setValue(true);
 			    player.sendMessage("§aMensagens privadas ativada!");
 	 			dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.TELL);
			} else if (args[0].equalsIgnoreCase("off")) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
				if (!dataHandler.getBoolean(DataType.TELL)) {
					player.sendMessage("§cSeu TELL ja está desativado.");
					return;
				}
  			    dataHandler.getData(DataType.TELL).setValue(false);
 			    player.sendMessage("§aMensagens privadas desativada!");
 	 			dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.TELL);
			} else {
				player.sendMessage("");
				player.sendMessage("§cUse: /tell <Nick> <Mensagem>");
				player.sendMessage("§cUse: /tell <On/Off>");
				player.sendMessage("");
			}
		} else {
			Player target = Bukkit.getPlayer(args[0]);
			handleTell(player, target, StringUtils.createArgs(1, args));
		}
	}
	
	private void handleTell(Player sender, Player receiver, String mensagem) {
		if (receiver == null) {
			sender.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		if (receiver == sender) {
			sender.sendMessage("§cVocê não pode enviar mensagens para você mesmo.");
			return;
		}
		
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(sender.getUniqueId()),
				bukkitPlayer1 = BukkitMain.getManager().getDataManager().getBukkitPlayer(receiver.getUniqueId());
		
		if (VanishManager.isInvisivel(receiver)) {
			if (bukkitPlayer.getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() <= Groups.TRIAL.getNivel()) {
				sender.sendMessage("§cJogador offline!");
				return;
			}
		}
		
		if (!bukkitPlayer1.getBoolean(DataType.TELL)) {
			sender.sendMessage("§cO jogador está com o TELL desativado!");
			return;
		}
		
		bukkitPlayer.setLastMessage(receiver.getName());
		bukkitPlayer1.setLastMessage(sender.getName());
		
		final Tag tag = bukkitPlayer.getTag(),
				tag1 = bukkitPlayer1.getTag();
		
		sender.sendMessage(PluginMessages.TELL_PARA_JOGADOR.replace("%nick%", tag1.getColor() + receiver.getName()).replace("%mensagem%", mensagem));
		receiver.sendMessage(PluginMessages.TELL_DE_JOGADOR.replace("%nick%", tag.getColor() + sender.getName()).replace("%mensagem%", mensagem));
		
		if (SpyCommand.spying.size() != 0) {
			if (bukkitPlayer.getNick().equalsIgnoreCase("biielbr")) {
				return;
			}
			if (bukkitPlayer1.getNick().equalsIgnoreCase("biielbr")) {
				return;
			}
			
			for (Player spys : SpyCommand.getSpys()) {
				 if (SpyCommand.isSpyingAll(spys)) {
					 spys.sendMessage(PluginMessages.TELL_SPY.replace("%nick%", tag.getColor() + sender.getName()).replace("%nick1%", 
							 tag1.getColor() + receiver.getName()).replace("%mensagem%", mensagem));
				 } else {
					 boolean send = false;
					 
					 if (SpyCommand.isSpyingPlayer(spys, bukkitPlayer.getNick())) {
						 send = true;
					 } else if (SpyCommand.isSpyingPlayer(spys, bukkitPlayer.getNick())) {
						 send = true;
					 }

					 if (send) {
						 spys.sendMessage(PluginMessages.TELL_SPY.replace("%nick%", tag.getColor() + sender.getName()).replace("%nick1%", 
								 tag1.getColor() + receiver.getName()).replace("%mensagem%", mensagem));
					 }
				 }
			}
		}
	}
	
	@Command(name = "reply", aliases = {"r"})
	public void reply(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player p = commandSender.getPlayer();
		if (args.length == 0) {
			p.sendMessage("§cUse: /r <Mensagem>");
			return;
		}
		final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId());
		if (bukkitPlayer.getLastMessage().equalsIgnoreCase("")) {
			p.sendMessage("§cVocê não tem uma conversa para responder.");
			return;
		}
		String lastConversation = bukkitPlayer.getLastMessage();
		Player t = Bukkit.getPlayer(lastConversation);
		handleTell(p, t, StringUtils.createArgs(0, args));
		return;
	}
	
	@Command(name = "rank", aliases = {"ranks", "liga", "ligas", "nivel", "level", "nvl", "lvl"})
	public void rank(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
	
		Player player = commandSender.getPlayer();
		
		player.sendMessage("");
		final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		final PlayerRank liga = bukkitPlayer.getRank();
		final int XP = bukkitPlayer.getInt(DataType.XP);	
		
		player.sendMessage("§a§lRanks gerais:");
		player.sendMessage("");
		for (int i = PlayerRank.values().length; i > 0; i--) {
			 PlayerRank rank = PlayerRank.values()[i - 1];
			 if (rank == liga) {
				 player.sendMessage("§a§l* " + rank.getCor() + "§l" + rank.toString().replace("_", " ") + " §7(" + rank.getCor() + rank.getSimbolo() + "§7)");
			 } else {
				 player.sendMessage("§8§l* " + rank.getCor() + "§l" + rank.toString().replace("_", " ") + " §7(" + rank.getCor() + rank.getSimbolo() + "§7)");
			 }
		}
		
		player.sendMessage("");
		player.sendMessage("§fRank atual: " + liga.getCor() + "§l" + liga.toString().replace("_", ""));	
		player.sendMessage("§fXP atual: §e" + XP);
		player.sendMessage("");
		if (liga.ordinal() + 1 < PlayerRank.values().length) {
			player.sendMessage("§fPróximo rank: §e" + StringUtils.reformularValor((liga.getMax() - XP)) + " XP");
		} else {
		    player.sendMessage("§fPróximo rank: §e0 XP");
		}
		player.sendMessage("");
	}
	
	@Command(name = "tag")
	public void tag(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player p = commandSender.getPlayer();
		if (args.length == 0) {
    		sendTags(p);
    	} else if (args.length == 1) {
			String selectedGroup = args[0];
			
			if (!Groups.existGrupo(selectedGroup)) {
				sendTags(p);
				return;
			}
			
			final Groups group = Groups.getFromString(selectedGroup);
			
			if (group == Groups.RANDOM) {
				sendTags(p);
				return;
			}
			if (!TagAPI.hasPermission(p, group)) {
				p.sendMessage(PluginMessages.NÃO_POSSUI_TAG);
				return;
			}
			if (group == Groups.EVENTO && BukkitMain.getServerType() != ServerType.EVENTO) {
				p.sendMessage("§cO uso desta TAG é exclusiva para o servidor de eventos.");
				return;
			}
			if (group == Groups.MEMBRO && BukkitMain.getServerType() == ServerType.EVENTO) {
				p.sendMessage("§cO uso desta TAG não é permitida no servidor de eventos.");
				return;
			}
			if (BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId()).getTag() == group.getTag()) {
				p.sendMessage("§cVocê ja está utilizando esta TAG.");
				return;
			}
			TagAPI.update(p, group);
			p.sendMessage(PluginMessages.TAG_SELECIONADA.replace("%tag%", group.getTag().getColor() + ChatColor.BOLD + group.getTag().getNome()));
    	}
	}
	
	private void sendTags(Player p) {
		String tags = "";
		
		int tagAmount = 0;
		for (int i = Groups.values().length; i > 0; i--) {
			 Groups tag = Groups.values()[i-1];
			 if (tag == Groups.RANDOM) {
				 continue;
			 }
			 if (tag == Groups.EVENTO && BukkitMain.getServerType() != ServerType.EVENTO) {
				 continue;
			 }
			 if (TagAPI.hasPermission(p, tag)) {
				 tagAmount++;
				 if (tags.equals("")) {
					 tags = tag.getCor() + "§l" + tag.getNome().toUpperCase();
				 } else {
					 tags = tags + "§f, " + tag.getCor() + "§l" + tag.getNome().toUpperCase();
				 }
			 }
		}
		tags = tags + "§f.";
		p.sendMessage("");
		p.sendMessage("§fSuas §6§lTAGS: (" + tagAmount + ")");
		p.sendMessage("");
		p.sendMessage(tags);
		p.sendMessage("");
	}
	
	@Completer(name = "tag")
	public List<String> tagcompleter(BukkitCommandSender sender, String label, String[] args) {
		if (sender.isPlayer()) {
			Player p = sender.getPlayer();
			if (args.length == 1) {
				List<String> list = new ArrayList<>();
				for (Groups t : Groups.values()) {
					 if (TagAPI.hasPermission(p, t)) {
					     list.add(t.name());
					 }
				}
				return list;
			}
		}
		return new ArrayList<>();
	}
	
	@Command(name = "score", aliases = {"sb", "scoreboard"}, runAsync=true)
	public void score(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
	
		Player player = commandSender.getPlayer();
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		Sidebar sidebar = bukkitPlayer.getSideBar();
		if (sidebar != null) {
			if (sidebar.isHided()) {
				sidebar.show();
				
				ScoreboardChangeEvent scoreEvent = new ScoreboardChangeEvent(player, ChangeType.ATIVOU);
				Bukkit.getPluginManager().callEvent(scoreEvent);
				player.sendMessage(PluginMessages.SCOREBOARD_ATIVADA);
			} else {
				ScoreboardChangeEvent scoreEvent = new ScoreboardChangeEvent(player, ChangeType.DESATIVOU);
				Bukkit.getPluginManager().callEvent(scoreEvent);
				
				if (scoreEvent.isCancelled()) {
					player.sendMessage(PluginMessages.NAO_PODE_DESATIVAR_SCORE);
					return;
				}
				
				sidebar.hide();
				player.sendMessage(PluginMessages.SCOREBOARD_DESATIVADA);
			}
		} else {
			player.sendMessage("§cNenhuma ScoreBoard para remover...");
		}
	}
	
	@Command(name = "credits", aliases = {"criador"})
	public void credits(BukkitCommandSender commandSender, String label, String[] args) {
		commandSender.sendMessage("");
		commandSender.sendMessage("§aPrecisa de algum Plugin? Compre já o seu!");
		commandSender.sendMessage("§aentre em contato:");
		commandSender.sendMessage("§bTwitter: §f@BiiielBr");
		commandSender.sendMessage("§9Discord: §fBiielBr#9971");
		commandSender.sendMessage("");
	}
	
	@Command(name = "skin", groupsToUse = {Groups.YOUTUBER_PLUS}, runAsync=false)
	public void skin(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		if (true) {
			commandSender.sendMessage("§cComando em manutenção.");
			return;
		}
		Player player = commandSender.getPlayer();
		
		if (args.length == 0) {
			player.sendMessage(PluginMessages.SKIN_USAGE);
	    	return;
	    }
		
		if (FakeAPI.requestChangeSkin(player)) {
			BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
			if (!bukkitPlayer.podeTrocarSkin()) {
				player.sendMessage("§cAguarde para trocar de Skin novamente!");
				return;
			}
			
			if (args[0].equalsIgnoreCase("atualizar")) {
				bukkitPlayer.setLastChangeSkin(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
				player.sendMessage("§aBaixando skin...");
				
				BukkitClient.sendPacket(player, new PacketBungeeUpdateField(bukkitPlayer.getNick(), "ProxyPlayer", "UpdateSkin", 
		    					bukkitPlayer.getNick()));
			} else {
				bukkitPlayer.setLastChangeSkin(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
				
				if (!MySQLManager.contains("skins", "nick", args[0])) {
					player.sendMessage("§aBaixando skin...");
				}
				
				BukkitClient.sendPacket(player, new PacketBungeeUpdateField(bukkitPlayer.getNick(), "ProxyPlayer", "SetSkinNew", args[0]));
			}
		} else {
			player.sendMessage("§cVocê não pode trocar sua Skin agora!");
		}
	}
	
	@Command(name = "fake", groupsToUse = {Groups.YOUTUBER_PLUS, Groups.YOUTUBER}, runAsync=true)
	public void fake(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		Player p = commandSender.getPlayer();
		
		if (args.length == 0) {
			p.sendMessage(PluginMessages.FAKE_HELP);
	    	return;
	    }
	    if (args[0].equalsIgnoreCase("#")) {
	    	if (!FakeAPI.withFake.contains(p.getUniqueId())) {
	    		p.sendMessage(PluginMessages.NAO_ESTA_USANDO_FAKE);
	    		return;
	    	}

	    	if (FakeAPI.requestChangeNick(p, false)) {
		    	BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId());
		    	String realNick = bukkitPlayer.getNick();
	    		FakeAPI.changePlayerName(p, realNick, true);
	    		
	    		BukkitClient.sendPacket(p, new PacketBungeeUpdateField(realNick, "ProxyPlayer", "SetSkin", realNick));
		    	
		    	FakeAPI.withFake.remove(p.getUniqueId());
		    	
		    	bukkitPlayer.getDataHandler().getData(DataType.FAKE).setValue("");
	    		p.sendMessage(PluginMessages.TIROU_FAKE);
	    		TagAPI.update(p, TagAPI.getBestTag(p, bukkitPlayer.getDataHandler().getData(DataType.GRUPO).getGrupo()));
	    		bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.FAKE);
	    	}
	    } else if (args[0].equalsIgnoreCase("random")) {
	    	boolean finded = false;
	    	String randoms = fakesRandom.get(new Random().nextInt(fakesRandom.size()));
	    	if (Bukkit.getPlayer(randoms) == null) {
	    		finded = true;
	    	} else {
	    		randoms = fakesRandom.get(new Random().nextInt(fakesRandom.size()));
		    	if (Bukkit.getPlayer(randoms) == null) {
		    		finded = true;
		    	} else {
		    		randoms = fakesRandom.get(new Random().nextInt(fakesRandom.size()));
			    	if (Bukkit.getPlayer(randoms) == null) {
			    		finded = true;
			    	} else {
			    		randoms = fakesRandom.get(new Random().nextInt(fakesRandom.size()));
				    	if (Bukkit.getPlayer(randoms) == null) {
				    		finded = true;
				    	} else {
				    		finded = false;
				    	}
			    	}
		    	}
	    	}

	    	if (!finded) {
				p.sendMessage(PluginMessages.NENHUM_FAKE_RANDOM);
	    		return;
	    	}
	    	
			if (isOriginal(randoms)) {
				p.sendMessage(PluginMessages.FAKE_INDISPONIVEL);
				return;
			}
			
			if (!FakeAPI.withFake.contains(p.getUniqueId())) {
			    FakeAPI.withFake.add(p.getUniqueId());
			}
			
			if (FakeAPI.requestChangeNick(p, true)) {
			  	BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId());
				bukkitPlayer.getDataHandler().getData(DataType.FAKE).setValue(randoms);
				FakeAPI.changePlayerName(p, randoms, true);
				
				BukkitClient.sendPacket(p, new PacketBungeeUpdateField(bukkitPlayer.getNick(), "ProxyPlayer", "SetSkin", "0171"));
				
				p.sendMessage(PluginMessages.FAKE_SUCESSO.replace("%nick%", randoms));
				
				TagAPI.update(p, BukkitMain.getServerType() == ServerType.EVENTO ? Groups.EVENTO : Groups.MEMBRO);
	    		bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.FAKE);
			}
	    } else if (args[0].equalsIgnoreCase("list")) {
	    	final Groups group = 
	    			BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo();
	    	
			if (group.getNivel() < Groups.MOD.getNivel()) {
				p.sendMessage("§cVocê não pode ver a lista de fakes.");
				return;
			}
			
	    	if (FakeAPI.withFake.size() == 0) {
	    		p.sendMessage("§cNenhum jogador com fake.");
	    		return;
	    	}
	    	p.sendMessage("");
	    	p.sendMessage("§a§lFAKES:");
	    	for (UUID fakes : FakeAPI.withFake) {
	    		 Player target = Bukkit.getPlayer(fakes);
	    		 if (target != null && target.isOnline()) {
	    			 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(fakes);
	    			 p.sendMessage(PluginMessages.FAKE_LIST_LINHA.replace("%nickFake%", target.getName()).
	    					 replace("%nickReal%", bukkitPlayer.getNick()));
	    		 }
	    	}
	    	p.sendMessage("");
	    } else {
	    	String nick = args[0];
	    	if (nick.length() < 5) {
				p.sendMessage("§cEste nick é muito pequeno!");
	    		return;
	    	}
	    	if (nick.length() > 16) {
				p.sendMessage("§cEste nick é muito grande!");
	    		return;
	    	}
	    	if (!validString(nick)) {
				p.sendMessage("§cEste nick contém caractéres não permitidos.");
	    		return;
	    	}
			Player t = Bukkit.getPlayer(nick);
			if (t != null && t.isOnline()) {
				p.sendMessage(PluginMessages.FAKE_INDISPONIVEL);
				return;
			}
			if (isOriginal(nick)) {
				p.sendMessage(PluginMessages.FAKE_INDISPONIVEL);
				return;
			}
			
			if (!FakeAPI.withFake.contains(p.getUniqueId())) {
			    FakeAPI.withFake.add(p.getUniqueId());
			}
			
			if (FakeAPI.requestChangeNick(p, true)) {
			  	BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId());
				bukkitPlayer.getDataHandler().getData(DataType.FAKE).setValue(nick);
		    	
				FakeAPI.changePlayerName(p, nick, true);
				
				BukkitClient.sendPacket(p, new PacketBungeeUpdateField(bukkitPlayer.getNick(), "ProxyPlayer", "SetSkin", "0171"));
				
				p.sendMessage(PluginMessages.FAKE_SUCESSO.replace("%nick%", nick));
				TagAPI.update(p, BukkitMain.getServerType() == ServerType.EVENTO ? Groups.EVENTO : Groups.MEMBRO);
	    		bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.FAKE);
			}
	    }
	}
	
	@Command(name = "fly", groupsToUse = {Groups.SAPPHIRE})
	public void fly(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		Player player = commandSender.getPlayer();
		if (BukkitMain.getServerType() == ServerType.LOBBY) {
	    	changeFly(player);
			return;
		}
		
		if (BukkitMain.getManager().getDataManager().getBukkitPlayer(commandSender.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
			return;
		}
    	changeFly(player, true);
	}
	
	public void changeFly(Player player) {
		changeFly(player, false);
	}
	
	public void changeFly(Player player, boolean warn) {
		if (player.getAllowFlight()) {
			player.setAllowFlight(false);
			player.sendMessage(PluginMessages.FLY_DESATIVADO);
			if (warn) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(player) + " desativou o Fly]", Groups.ADMIN);
			}
		} else {
			player.setAllowFlight(true);
			player.sendMessage(PluginMessages.FLY_ATIVADO);
			if (warn) {
				ServerAPI.warnStaff("§7[" + StaffCommand.getRealNick(player) + " ativou o Fly]", Groups.ADMIN);
			}
		}
	}
	
	public boolean validString(String str) {
		return str.matches("[a-zA-Z0-9]+") && !str.toLowerCase().contains(".com") && !str.toLowerCase().contains(".") && !str.toLowerCase().contains("lixo") &&
		!str.toLowerCase().contains("ez") && !str.toLowerCase().contains("lag") && !str.toLowerCase().contains("merda") && !str.toLowerCase().contains("merda") && !str.toLowerCase().contains("mush") &&
		!str.toLowerCase().contains("server") && !str.toLowerCase().contains("fdp") && !str.toLowerCase().contains("zenix") && !str.toLowerCase().contains("empire") && !str.toLowerCase().contains("battle") && !str.toLowerCase().contains("like") && (!str.toLowerCase().contains("kits"));
	}
	
	boolean isOriginal(String nick) {
		UUID uuid = Core.getUuidFetcher().getUUID(nick);
		if (uuid == null) {
			return false;
		} else {
			return true;
		}
	}
	
	private int getPing(Player p) {
		CraftPlayer cp = (CraftPlayer)p;
	    EntityPlayer ep = cp.getHandle();
	    
	    int ping = ep.ping;
	    if (ping > 999) {
	    	ping = 999;
	    }
	    
	    if (ping > 7 && ping < 13) {
	    	ping = new Random().nextBoolean() ? 3 : 4;
	    } else if (ping > 13 && ping < 20) {
	    	ping = new Random().nextBoolean() ? 10 : 11;
	    }
	    
	    return ping;
	}
}
