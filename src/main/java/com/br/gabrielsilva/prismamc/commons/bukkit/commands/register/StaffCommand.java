package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.menus.ProfileInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.Constructions;
import com.br.gabrielsilva.prismamc.commons.core.base.BasePunishment;
import com.br.gabrielsilva.prismamc.commons.core.base.PunishmentType;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.google.common.base.Charsets;

public class StaffCommand implements CommandClass {
	
	@Command(name = "account", aliases= {"acc", "perfil", "conta", "stats", "info"})
	public void account(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		
		ProfileInventory accountMenu = null;
		
		BasePunishment baseBan = null,
				baseMute = null;
		
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()),
				bukkitPlayerTarget = null;
		
		String nickToLoad = "";
		
		boolean error = false;
		
		if (args.length == 1) {
			if (BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() <
					Groups.YOUTUBER.getNivel()) {
				player.sendMessage(PluginMessages.SEM_PERMISSÃO);
				return;
			}
			
			String nick = MySQLManager.getString("accounts", "nick", args[0], "nick");
			if (nick.equalsIgnoreCase("N/A")) {
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
 			
			Player target = ServerAPI.getExactPlayerByNick(nick);
			if (target != null) {
				bukkitPlayerTarget = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
				
				accountMenu = new ProfileInventory("Conta de " + bukkitPlayerTarget.getNick());
			}
			
			if (accountMenu == null) {
				target = Bukkit.getPlayer(nick);
			
				if (target != null) {
					bukkitPlayerTarget = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
					accountMenu = new ProfileInventory("Conta de " + bukkitPlayerTarget.getNick());
				}
			}
			
			if (bukkitPlayerTarget == null) {
				bukkitPlayerTarget = new BukkitPlayer(nick, UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes(Charsets.UTF_8)));
				accountMenu = new ProfileInventory("Conta de " + bukkitPlayerTarget.getNick());
			}
			
			try {
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.HUNGER_GAMES)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.HUNGER_GAMES);
				}
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.GLADIATOR)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.GLADIATOR);
				}
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.KITPVP)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.KITPVP);
				}
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.PRISMA_PLAYER)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.PRISMA_PLAYER);
				}
			} catch (Exception ex) {
				BukkitMain.console("Ocorreu um erro ao tentar abrir o perfil de um jogador -> " + ex.getLocalizedMessage());
				error = true;
			}
			
		} else {
			accountMenu = new ProfileInventory("Sua conta");
			
			bukkitPlayerTarget = bukkitPlayer;
			
			try {
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.HUNGER_GAMES)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.HUNGER_GAMES);
				}
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.GLADIATOR)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.GLADIATOR);
				}
				if (!bukkitPlayerTarget.getDataHandler().isCategoryLoaded(DataCategory.KITPVP)) {
					bukkitPlayerTarget.getDataHandler().load(DataCategory.KITPVP);
				}
			} catch (Exception ex) {
				BukkitMain.console("Ocorreu um erro ao tentar abrir o perfil de um jogador -> " + ex.getLocalizedMessage());
				error = true;
			}
		}
		
		if (error) {
			player.sendMessage("§cOcorreu um erro, tente novamente.");
			return;
		}
		
		nickToLoad = bukkitPlayerTarget.getNick();
		
		baseBan = new BasePunishment(nickToLoad, bukkitPlayerTarget.getString(DataType.LAST_IP), PunishmentType.BAN);
		try {
			baseBan.load();
		} catch (Exception e1) {
			error = true;
		}
		
		baseMute = new BasePunishment(nickToLoad, bukkitPlayerTarget.getString(DataType.LAST_IP), PunishmentType.MUTE);
		try {
			baseMute.load();
		} catch (Exception e) {
			error = true;
		}
		
		if (error) {
			player.sendMessage("§cOcorreu um erro, tente novamente.");
			return;
		}
		
		accountMenu.createInventory(bukkitPlayer.getNick(), bukkitPlayerTarget, baseBan, baseMute);
		
		accountMenu.open(player);
	}
	
	@Command(name = "gamemode", aliases= {"gm"}, groupsToUse = {Groups.MOD})
	public void gm(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		if (args.length == 0) {
			changeGameMode(player);
			ServerAPI.warnStaff("§7[" + getRealNick(player) + " alterou o seu GameMode para " + (player.getGameMode() == GameMode.CREATIVE ? "CRIATIVO" : "SOBREVIVÊNCIA") + "]", Groups.ADMIN);
			return;
		}
		if (args.length == 1) {
			Player target = ServerAPI.getExactPlayerByNick(args[0]);
			if (target == null) {
				if (args[0].equalsIgnoreCase("0")) {
					changeGameMode(player, GameMode.SURVIVAL);
				} else if (args[0].equalsIgnoreCase("1")) {
					changeGameMode(player, GameMode.CREATIVE);
				} else {
					changeGameMode(player);
				}
				ServerAPI.warnStaff("§7[" + getRealNick(player) + " alterou o seu GameMode para " + (player.getGameMode() == GameMode.CREATIVE ? "CRIATIVO" : "SOBREVIVÊNCIA") + "]", Groups.ADMIN);
				return;
			}
			changeGameMode(target);
			player.sendMessage("§aVocê alterou o GameMode de §7" + target.getName() + " para o MODO: " +
			    (target.getGameMode() == GameMode.CREATIVE ? "criativo" : "sobrevivência") + ".");
			
			ServerAPI.warnStaff("§7[" + getRealNick(player) + " alterou o GameMode para de " + target.getName() + " para "
			+ (target.getGameMode() == GameMode.CREATIVE ? "CRIATIVO" : "SOBREVIVÊNCIA") + "]", Groups.ADMIN);
			return;
		}
	}
	
	public static String getRealNick(Player target) {
		return BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId()).getNick();
	}
	
	private void changeGameMode(Player player) {
		changeGameMode(player, null);
	}
	
	private void changeGameMode(Player player, GameMode preference) {
		boolean continuar = true;
		
		if (preference != null) {
			if (preference == player.getGameMode()) {
				player.sendMessage("§cVocê ja está nesse GameMode.");
				continuar = false;
				return;
			}
		}
		
		if (!continuar) {
			return;
		}
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			player.setGameMode(GameMode.SURVIVAL);
			player.sendMessage("§aVocê está no MODO sobrevivência.");
		} else {
			player.setGameMode(GameMode.CREATIVE);
			player.sendMessage("§aVocê está no MODO criativo.");
		}
	}
	
	@Command(name = "admin", aliases = {"adm"}, groupsToUse = Groups.YOUTUBER_PLUS)
	public void admin(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		VanishManager.changeAdmin(player);
	}
	
	@Command(name = "invsee", aliases = {"inv"}, groupsToUse = {Groups.TRIAL})
	public void invsee(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
	    Player p = commandSender.getPlayer();
	    if (args.length == 0) {
	    	p.sendMessage(PluginMessages.INVSEE);
	    } else if (args.length == 1) {
		    Player d = Bukkit.getPlayer(args[0]);
		    if (d == null) {
		    	p.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			    return;
		    }
		    if (d == p) {
		    	p.sendMessage("§cVocê não pode abrir o seu inventário.");
		    	return;
		    }
		    p.setMetadata("inventory-view", new FixedMetadataValue(BukkitMain.getInstance(), d.getUniqueId().toString()));
		    p.openInventory(d.getInventory());
		    p.sendMessage(PluginMessages.INVSEE_SUCESSO.replace("%nick%", d.getName()));
		    
			ServerAPI.warnStaff("§7[" + getRealNick(p) + " abriu o inventário de " + d.getName() + "]", Groups.ADMIN);
		} else {
		    p.sendMessage(PluginMessages.INVSEE);
	    }
	}
	
	boolean running = false;
	
	@Command(name = "synctpall", aliases = {"stpall"}, groupsToUse= {Groups.MOD})
	public void synctpall(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
	    Player p = commandSender.getPlayer();
		if (running) {
			p.sendMessage("§cJá existe um tpall sincronizado em andamento.");
			return;
		}
		running = true;
		startSyncTpall(p);
		ServerAPI.warnStaff("§7[" + getRealNick(p) + " puxou todos os jogadores em async]", Groups.ADMIN);
	}
	
	private void startSyncTpall(Player player) {
		new BukkitRunnable() {
			int teleporteds = 0;
			Long iniciado = System.currentTimeMillis();
			Location loc = player.getLocation();
			ArrayList<Player> players = (ArrayList<Player>) player.getWorld().getPlayers();
			int toTeleport = players.size();
			public void run() {
				if (teleporteds >= toTeleport) {
					cancel();
					running = false;
					if (player.isOnline()) {
						player.sendMessage("§aTodos os jogadores puxados sincronizadamente em: §7" + DateUtils.getElapsed(iniciado));
					}
					return;
				}
				for (int i = 0; i < 2; i++) {
					try {
						Player t = players.get(teleporteds + i);
					    if ((t != null) && (t != player)) {
				             t.teleport(loc);
					    }
					} catch (IndexOutOfBoundsException e) {
						continue;
					} catch (NullPointerException e) {
						continue;
					}
				}
				teleporteds=teleporteds+=2;
			}
		}.runTaskTimer(BukkitMain.getInstance(), 2L, 2L);
	}
	
	@Command(name = "tpall", groupsToUse= {Groups.ADMIN})
	public void tpall(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
	    Player p = commandSender.getPlayer();
		Location loc = p.getLocation();
		for (Player ons : Bukkit.getOnlinePlayers()) {
			 if (ons != p) {
				 ons.teleport(loc);
			 }
		}
		p.sendMessage("§aTodos os jogadores teleportados.");
		ServerAPI.warnStaff("§7[" + getRealNick(p) + " puxou todos os jogadores]", Groups.ADMIN);
	}
	
	@Command(name = "worldedit", aliases= {"we"}, groupsToUse = {Groups.ADMIN})
	public void worldedit(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		if (args.length == 0) {
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUse: /worldedit <Machado/Undo>");
			commandSender.sendMessage("§cUse: /worldedit set <ID>");
			commandSender.sendMessage("§cUse: /worldedit setblockpertick <Quantidade>");
			commandSender.sendMessage("");
			return;
		}
	    Player player = commandSender.getPlayer();
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("machado")) {
			    player.getInventory().addItem(new ItemBuilder().material(Material.WOOD_AXE).name("§e§lWORLDEDIT").build());
			    player.sendMessage("§e§lWORLDEDIT §fVocê recebeu o Machado.");
			} else if (args[0].equalsIgnoreCase("undo")) {
				if (!BukkitMain.getManager().getWorldEditManager().hasRollingConstructionByUUID(player.getUniqueId())) {
					commandSender.sendMessage("§e§lWORLDEDIT §fVocê não possuí uma construção.");
					return;
				}
				Constructions construction = BukkitMain.getManager().getWorldEditManager().getConstructionByUUID(player.getUniqueId());
				if (construction.isFinished()) {
					commandSender.sendMessage("§e§lWORLDEDIT §fA construção ainda está em andamento.");
					return;
				}
				if (construction.isResetando()) {
					commandSender.sendMessage("§e§lWORLDEDIT §fA construção já está em sendo resetada.");
					return;
				}
				construction.startRegress();
				commandSender.sendMessage("§e§lWORLDEDIT §fRetirando blocos...");
			} else {
				commandSender.sendMessage("");
				commandSender.sendMessage("§cUse: /worldedit <Machado/Undo>");
				commandSender.sendMessage("§cUse: /worldedit set <ID>");
				commandSender.sendMessage("§cUse: /worldedit setblockpertick <Quantidade>");
				commandSender.sendMessage("");
			}
		} else if (args.length == 2) {
			if (args[0].contentEquals("setblockpertick")) {
				if (!ServerAPI.isInteger(args[1])) {
					commandSender.sendMessage("§cUse: /worldedit setblockpertick <Quantidade>");
					return;
				}
				if (!BukkitMain.getManager().getWorldEditManager().hasRollingConstructionByUUID(player.getUniqueId())) {
					commandSender.sendMessage("§e§lWORLDEDIT §fVocê não possuí uma construção.");
					return;
				}
				int quantia = Integer.valueOf(args[1]);
				if (quantia > 1000) {
					commandSender.sendMessage("§e§lWORLDEDIT §fValor máximo de apenas 1,000 blocos.");
					return;
				}
				Constructions construction = BukkitMain.getManager().getWorldEditManager().getConstructionByUUID(player.getUniqueId());
				construction.setBlocksPorTick(quantia);
				commandSender.sendMessage("§e§lWORLDEDIT §fValor alterado.");
			} else if (args[0].equalsIgnoreCase("set")) {
				String idsOriginal = args[1];
				String ids = args[1].replaceAll(",", "");
				if (!ServerAPI.isInteger(ids)) {
					commandSender.sendMessage("§cUse: /worldedit set <ID>");
					return;
				}
				if (BukkitMain.getManager().getWorldEditManager().hasRollingConstructionByUUID(player.getUniqueId())) {
					if (!BukkitMain.getManager().getWorldEditManager().getConstructionByUUID(player.getUniqueId()).isFinished()) {
						commandSender.sendMessage("§e§lWORLDEDIT §fVocê já possuí uma construção em andamento.");
						return;
					}
				}
				if (BukkitMain.getManager().getWorldEditManager().continueEdit(player)) {
					List<Material> materiaisIds = new ArrayList<>();
					
					if (idsOriginal.contains(",")) {
						boolean error = false;
						for (String string : idsOriginal.split(",")) {
							 try {
								 materiaisIds.add(Material.getMaterial(Integer.valueOf(string)));
							 } catch (NullPointerException ex) {
								 error = true;
								 break;
							 }
						}
						if (error) {
							player.sendMessage("§e§lWORLDEDIT §fOcorreu um erro ao tentar encontrar o material.");
							return;
						}
						
					} else {
						try {
							materiaisIds.add(Material.getMaterial(Integer.valueOf(args[1])));
						} catch (NullPointerException ex) {
							player.sendMessage("§e§lWORLDEDIT §fOcorreu um erro ao tentar encontrar o material.");
							return;
						}
					}
					
					player.sendMessage("§e§lWORLDEDIT §fProcessando blocos...");
					List<Location> locations = null;
					try {
						locations = BukkitMain.getManager().getWorldEditManager().getLocationsFromTwoPoints(
								BukkitMain.getManager().getWorldEditManager().getPos1(player), BukkitMain.getManager().getWorldEditManager().getPos2(player));
					} catch (Exception ex) {
						player.sendMessage("§e§lWORLDEDIT §fErro ao processar os blocos...");
						return;
					} finally {
						player.sendMessage("§e§lWORLDEDIT §e" + ServerAPI.reformularValor(locations.size()) + " §fblocos processados.");
					}
					
					BukkitMain.getManager().getWorldEditManager().addConstructionByUUID(player, locations);
					BukkitMain.getManager().getWorldEditManager().getConstructionByUUID(player.getUniqueId()).setBlocksToSet(materiaisIds);
					BukkitMain.getManager().getWorldEditManager().getConstructionByUUID(player.getUniqueId()).start();
					
					locations.clear();
					materiaisIds.clear();
				}
			} else {
				commandSender.sendMessage("");
				commandSender.sendMessage("§cUse: /worldedit <Machado/Undo>");
				commandSender.sendMessage("§cUse: /worldedit set <ID>");
				commandSender.sendMessage("§cUse: /worldedit setblockpertick <Quantidade>");
				commandSender.sendMessage("");
			}
		} else {
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUse: /worldedit <Machado/Undo>");
			commandSender.sendMessage("§cUse: /worldedit set <ID>");
			commandSender.sendMessage("§cUse: /worldedit setblockpertick <Quantidade>");
			commandSender.sendMessage("");
		}
	}
	
	@Command(name = "tp", groupsToUse= {Groups.YOUTUBER_PLUS})
	public void tp(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
	    Player p = commandSender.getPlayer();
	    if (args.length == 0) {
			p.sendMessage("");
			p.sendMessage("§cUse: /tp <Player>");
			p.sendMessage("§cUse: /tp <Player1> <Player2>");
			p.sendMessage("§cUse: /tp <X, Y, Z>");
			p.sendMessage("§cUse: /tp <Nick> <X, Y, Z>");
			p.sendMessage("");
		} else if (args.length == 1) {
			Player t = p.getServer().getPlayer(args[0]);
			if (t == null) {
				p.sendMessage("§cJogador offline.");
        	    return;
			}
            p.teleport(t.getLocation());
            p.sendMessage("§aTeleportado com sucesso.");
    		ServerAPI.warnStaff("§7[" + getRealNick(p) + " se teleportou para o " + getRealNick(t) + "]", Groups.ADMIN);
		} else if (args.length == 2) {
			Player p1 = p.getServer().getPlayer(args[0]);
            Player p2 = p.getServer().getPlayer(args[1]);
            if (p1 == null) {
				p.sendMessage("§cJogador offline.");
        	    return;
            }
            if (p2 == null) {
				p.sendMessage("§cJogador offline.");
        	    return;
            }
            p1.teleport(p2.getLocation());
            p.sendMessage("§a[§7" + p1.getName() + "§a teleportado para §7" + p2.getName() + "§a]");
            
    		ServerAPI.warnStaff("§7[" + getRealNick(p1) + " foi teleportado para o " + getRealNick(p2) + " pelo " + commandSender.getNick() + "]", Groups.ADMIN);
            
		} else if (args.length == 3) {
			if ((args[0] == null) || (!ServerAPI.isInteger(args[0]))) {
	    	     p.sendMessage("§cUse: /tp <X, Y, Z>");
	    	     return;
			}
	        if ((args[1] == null) || (!ServerAPI.isInteger(args[1]))) {
	    	     p.sendMessage("§cUse: /tp <X, Y, Z>");
	    	     return;
	        }
	        if ((args[2] == null) || (!ServerAPI.isInteger(args[2]))) {
	    	     p.sendMessage("§cUse: /tp <X, Y, Z>");
	    	     return;
	        }
	        int x = Integer.parseInt(args[0]);
	        int y = Integer.parseInt(args[1]);
	        int z = Integer.parseInt(args[2]);
	        Location loc = new Location(p.getWorld(), x + 0.500, y, z + 0.500);
	        p.teleport(loc);
	        p.sendMessage("§aTeleportado para: §7" + x + ", " + y + ", " + z + "§a.");
	        
    		ServerAPI.warnStaff("§7[" + getRealNick(p) + " se teleportou para as coordenadas: " + x + ", " + y + ", " + z + "]", Groups.ADMIN);
		} else if (args.length == 4) {
			Player t = p.getServer().getPlayer(args[0]);
            if (t == null) {
			    p.sendMessage("§cJogador offline.");
        	    return;
            }
	        if ((args[1] == null) || (!ServerAPI.isInteger(args[1]))) {
	    	     p.sendMessage("§cUse: /tp <Nick> <X, Y, Z>");
	    	     return;
	        }
	        if ((args[2] == null) || (!ServerAPI.isInteger(args[2]))) {
	    	     p.sendMessage("§cUse: /tp <Nick> <X, Y, Z>");
	    	     return;
	        }
	        if ((args[3] == null) || (!ServerAPI.isInteger(args[3]))) {
	    	     p.sendMessage("§cUse: /tp <Nick> <X, Y, Z>");
	    	     return;
	        }
	        int x = Integer.parseInt(args[1]);
	        int y = Integer.parseInt(args[2]);
	        int z = Integer.parseInt(args[3]);
	        Location loc = new Location(p.getWorld(), x + 0.500, y, z + 0.500);
	        p.teleport(loc);
	        p.sendMessage("§7" + t.getName() + "§a teleportado para: §7" + x + ", " + y + ", " + z + "§a.");
	   		ServerAPI.warnStaff("§7[" + getRealNick(t) + " foi teleportado para as coordenadas: " + x + ", " + y + ", " + z + " pelo " + commandSender.getNick() + "]", Groups.ADMIN);
		} else {
			p.sendMessage("");
			p.sendMessage("§cUse: /tp <Player>");
			p.sendMessage("§cUse: /tp <Player1> <Player2>");
			p.sendMessage("§cUse: /tp <X, Y, Z>");
			p.sendMessage("§cUse: /tp <Nick> <X, Y, Z>");
			p.sendMessage("");
		}
	}
}