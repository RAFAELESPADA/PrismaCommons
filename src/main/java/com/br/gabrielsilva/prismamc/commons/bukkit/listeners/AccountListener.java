package com.br.gabrielsilva.prismamc.commons.bukkit.listeners;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.FakeAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.register.SchematicCommand;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.register.SpyCommand;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketCheckPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketLoadClan;

public class AccountListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		if (!BukkitMain.isLoaded()) {
			event.disallow(Result.KICK_OTHER, "§cO servidor está sendo estabilizado, aguarde.");
			return;
		}
		
		if (Bukkit.getPlayer(event.getUniqueId()) != null) {
			event.disallow(Result.KICK_OTHER, "§cVocê ja está online...");
			return;
		}
		
		final String nick = event.getName();
		
		if (!StringUtils.validUsername(nick)) {
			event.disallow(Result.KICK_OTHER, "§cNick inválido.");
			return;
		}
	      
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
			return;
		}
		
		final UUID uuid = event.getUniqueId();
		
		final String address = event.getAddress().getHostAddress();
		
		BukkitMain.getManager().getDataManager().addBukkitPlayer(uuid, nick);
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(uuid);
		
		boolean error = false;
		
		if (BukkitMain.getServerType() == ServerType.LOGIN) {

		
			if (!bukkitPlayer.getDataHandler().load(DataCategory.REGISTER)) {
				error = true;
			}
			
			if (!bukkitPlayer.getDataHandler().load(DataCategory.PRISMA_PLAYER)) {
				error = true;
			}
		} else {
			
			if (!bukkitPlayer.getDataHandler().load(DataCategory.PRISMA_PLAYER)) {
				error = true;
			}
				
			if (!bukkitPlayer.getDataHandler().load(DataCategory.PREFERENCIAS)) {
				error = true;
			}
			
			if (!bukkitPlayer.getString(DataType.CLAN).equalsIgnoreCase("Nenhum")) {
				handleLoadTag(bukkitPlayer.getString(DataType.CLAN));
			}
		}
		
		if (error) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cOcorreu um erro ao tentar carregar suas informações.");
			BukkitMain.getManager().getDataManager().removeBukkitPlayerIfExists(uuid);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void scoreListener(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		
		Player player = event.getPlayer();
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if (BukkitMain.getServerType() == ServerType.LOGIN) {
			TagAPI.update(player, Groups.RANDOM);
			return;
		}
		
		VanishManager.updateInvisibles(player);
			
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		bukkitPlayer.updateRank();
		
		bukkitPlayer.updateVersion(player);
		bukkitPlayer.handleTimers(player);
		
		if (!bukkitPlayer.getString(DataType.FAKE).equalsIgnoreCase("")) {
			if (bukkitPlayer.getDataHandler().getData(DataType.GRUPO).getGrupo().getTag().getNivel() <= 1) {
				TagAPI.update(player, 
						TagAPI.getBestTag(player, bukkitPlayer.getData(DataType.GRUPO).getGrupo()));
				return;
			}
			final Player target = Bukkit.getPlayer(bukkitPlayer.getString(DataType.FAKE));
			if (target != null) {
				TagAPI.update(player, 
						TagAPI.getBestTag(player, bukkitPlayer.getData(DataType.GRUPO).getGrupo()));
				return;
			}
			if (!FakeAPI.withFake.contains(player.getUniqueId())) {
			    FakeAPI.withFake.add(player.getUniqueId());
			}
			FakeAPI.changePlayerName(player, bukkitPlayer.getString(DataType.FAKE), true);
			player.sendMessage("§aVocê está utilizando o nick Fake.");
			TagAPI.update(player, Groups.MEMBRO);
		} else {
			TagAPI.update(player, 
					TagAPI.getBestTag(player, bukkitPlayer.getData(DataType.GRUPO).getGrupo()));
		}
		
		PlayerAPI.updateTab(player);
		
		BukkitMain.runLater(() -> {
			if (!player.isOnline()) {
				return;
			}
			
			BukkitClient.sendPacket(player, new PacketCheckPlayer(bukkitPlayer.getNick()));
			
			if (!bukkitPlayer.getString(DataType.CLAN).equalsIgnoreCase("Nenhum")) {
				BukkitClient.sendPacket(player, new PacketLoadClan(bukkitPlayer.getNick(), bukkitPlayer.getString(DataType.CLAN)));
			}
			
			if (BukkitMain.getServerType() != ServerType.HG && BukkitMain.getServerType() != ServerType.EVENTO) {
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() >= Groups.TRIAL.getNivel()) {
					if (bukkitPlayer.getBoolean(DataType.JOIN_ADMIN)) {
						VanishManager.changeAdmin(player);
					}
				}
			}
		}, 2L);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent event) {
		if (!BukkitMain.getManager().getDataManager().hasBukkitPlayer(event.getPlayer().getUniqueId())) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cOcorreu um erro ao carregar suas informações.");
			return;
		}
		if (BukkitMain.getServerType() == ServerType.LOGIN) {
			return;
		}
		BukkitMain.getManager().getPermissionManager().injectPermissions(event.getPlayer());
	}
	
	@EventHandler
	public void onLogin2(PlayerLoginEvent event) {
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(event.getPlayer().getUniqueId());
		
		if (event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST) {
			if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.YOUTUBER_PLUS.getNivel()) {
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cApenas jogadores autorizados, podem entrar neste servidor.");
			} else {
				event.allow();
			}
		}
		
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			BukkitMain.getManager().getDataManager().removeBukkitPlayerIfExists(event.getPlayer().getUniqueId());
		} else {
			handleLoadTag(bukkitPlayer.getString(DataType.CLAN));
		}
	}
	
	private void handleLoadTag(String clanName) {
		if (BukkitMain.getServerType() == ServerType.LOGIN) {
			return;
		}
		if (!clanName.equalsIgnoreCase("Nenhum")) {
			
			if (!ChatListener.clanTag.containsKey(clanName)) {
				 try {
					 PreparedStatement preparedStatement = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM clans where nome='"+clanName+"'");
					 ResultSet result = preparedStatement.executeQuery();
					 if (result.next()) {
						 ChatListener.clanTag.put(clanName, result.getString("tag"));
					 }
						
					 result.close();
					 preparedStatement.close();
				 } catch (SQLException ex) {
					 ex.printStackTrace();
				 }
			}
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.MINUTO) {
			return;
		}
		
		ServerAPI.sendLogsAsync();
		
		if (BukkitMain.getServerType() == ServerType.HG || BukkitMain.getServerType() == ServerType.LOGIN ||
				BukkitMain.getServerType() == ServerType.EVENTO) {
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers()) {
		  	 BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).handleTimers(player);
		}
		
		//ServerAPI.cleanRam(null);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		
		Scoreboard board = event.getPlayer().getScoreboard();
		if (board != null) {
			for (Team t : board.getTeams()) {
				 t.unregister();
				 t = null;
			}
			for (Objective ob : board.getObjectives()) {
				 ob.unregister();
				 ob = null;
			}
			board = null;
		}
		event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
	
		if (player.getScoreboard() != null) {
			for (Team team : player.getScoreboard().getTeams()) {
				 team.unregister();
				 team = null;
			}
		}
		
		if (FakeAPI.withFake.contains(player.getUniqueId())) {
			FakeAPI.withFake.remove(player.getUniqueId());
		}
		if (ChatListener.chatCooldown.containsKey(player.getUniqueId())) {
			ChatListener.chatCooldown.remove(player.getUniqueId());
		}
    	if (SchematicCommand.POSITIONS.containsKey(player)) {
            SchematicCommand.POSITIONS.remove(player);
    	}
    	if (DamageListener.receiveHitCooldown.containsKey(player.getUniqueId())) {
    		DamageListener.receiveHitCooldown.remove(player.getUniqueId());
    	}
    	
    	if (SpyCommand.spying.containsKey(player.getUniqueId())) {
    		SpyCommand.spying.remove(player.getUniqueId());
    	}
    	
		VanishManager.remove(player);
		BukkitMain.getManager().getDataManager().removeBukkitPlayer(player.getUniqueId());
	}
}