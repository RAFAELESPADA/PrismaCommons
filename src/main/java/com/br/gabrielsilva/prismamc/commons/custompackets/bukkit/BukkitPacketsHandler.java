package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit;

import java.util.List;

import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.FakeAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.title.TitleAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.listeners.ChatListener;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketKickPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketRespawnPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketSetClanTag;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketTeleportPlayerByReport;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;

public class BukkitPacketsHandler {

	public void handleKickPlayer(PacketKickPlayer packet) throws HandlePacketException {
		Player target = ServerAPI.getExactPlayerByNick(packet.getNick());
		
    	if (target != null && target.isOnline()) {
    		target.kickPlayer(packet.getMotivo());
    	}
    	
    	Core.console("[CustomPacket] " + packet.getNick() + " recebeu um Kick via Packet. Motivo: " + packet.getMotivo());
	}

	public void handleKickPlayer(PacketRespawnPlayer packet) throws HandlePacketException {
		Player target = ServerAPI.getExactPlayerByNick(packet.getNick());
    	if (target != null && target.isOnline()) {
        	FakeAPI.respawnPlayer(target);
    	}
	}

	public void handleSetClanTag(PacketSetClanTag packet) throws HandlePacketException {
		ChatListener.clanTag.put(packet.getClan(), packet.getTag());
	}

	public void handleTeleportPlayerByReport(PacketTeleportPlayerByReport packet) throws HandlePacketException {
		Player staffer = ServerAPI.getExactPlayerByNick(packet.getNickStaffer()),
				target = ServerAPI.getExactPlayerByNick(packet.getNickTarget());
	
		if (staffer == null) {
			return;
		}
	
		if (target == null) {
			staffer.sendMessage("§cJogador offline.");
			return;
		}
	
		if (!VanishManager.inAdmin(staffer)) {
			VanishManager.changeAdmin(staffer, true);
		}
		
		BukkitMain.runLater(() -> {
			staffer.teleport(target);
		}, 5);
	}

	public void handleUpdateField(PacketUpdateField packet) throws HandlePacketException {
		final Player target = ServerAPI.getExactPlayerByNick(packet.getNick());
		
    	if (target != null && target.isOnline()) {
    		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
    		
    		DataHandler dataHandler = bukkitPlayer.getDataHandler();
    		
    		if (packet.getType().equalsIgnoreCase("CustomPlayer")) {
    			if (packet.getField().equalsIgnoreCase("FirstLogin")) {
    				dataHandler.getData(DataType.FIRST_LOGGED_IN).setValue(Long.valueOf(packet.getFieldValue()));
    			} else if (packet.getField().equalsIgnoreCase("LastLoggedIn")) {
    				dataHandler.getData(DataType.LAST_LOGGED_IN).setValue(Long.valueOf(packet.getFieldValue()));
    			} else if (packet.getField().equalsIgnoreCase("Clan")) {
    				dataHandler.getData(DataType.CLAN).setValue(packet.getFieldValue());
    				
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.CLAN);
    				
    				PlayerAPI.updateTab(target);
    				TagAPI.update(target, bukkitPlayer.getTag());
    			} else if (packet.getField().equalsIgnoreCase("Grupo")) {
    				final Long tempo = Long.valueOf(packet.getExtraValue());
    				
    				bukkitPlayer.getAttachment().resetPermissions();
    				
    				final Tag tag = Groups.getFromString(packet.getFieldValue()).getTag();
    				
    				dataHandler.getData(DataType.GRUPO).setValue(tag.getNome());
    				dataHandler.getData(DataType.GRUPO_TIME).setValue(tempo);
    				
    				TagAPI.update(target, tag);
    				
    				TitleAPI.enviarTitulos(target, "§6§lRANK", "§fAgora você é " + tag.getColor() + "§l" + tag.getNome(), 
    						1, 1, 5);
  
    				target.sendMessage(PluginMessages.GRUPO_ATUALIZADO.replace("%grupo%", tag.getColor() + "§l" + tag.getNome()).replace(
    				"%tempo%", (tempo == 0 ? "§apermamentemente." : "§fpor: §a" + DateUtils.formatDifference(tempo + 900))));
    				
    				bukkitPlayer.getAttachment().addPermissions(
    						BukkitMain.getManager().getPermissionManager().getPermsFromGroup(packet.getFieldValue().toLowerCase()));
    				
    				VanishManager.updateInvisibles(target);
    				
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.GRUPO, DataType.GRUPO_TIME);
    				PlayerAPI.updateTab(target);
    			} else if (packet.getField().equalsIgnoreCase("Coins")) {
    				dataHandler.getData(DataType.COINS).add(Integer.valueOf(packet.getFieldValue()));
    				
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.COINS);
    				PlayerAPI.updateTab(target);
    			} else if (packet.getField().equalsIgnoreCase("Cash")) {
    				dataHandler.getData(DataType.CASH).add(Integer.valueOf(packet.getFieldValue()));
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.CASH);
    				PlayerAPI.updateTab(target);
    			} else if (packet.getField().equalsIgnoreCase("XP")) {
    				bukkitPlayer.addXP(Integer.valueOf(packet.getFieldValue()));
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.XP);
    				PlayerAPI.updateTab(target);
    			} else if (packet.getField().equalsIgnoreCase("SetPrivatePerms")) {
    				dataHandler.getData(DataType.PERMS).getList().clear();
    				
    				List<String> newPerms = StringUtils.reformuleFormattedWithoutSpace(packet.getFieldValue());
    				for (String string : newPerms) {
    					 dataHandler.getData(DataType.PERMS).getList().add(string);
    				}
    				
    			 	BukkitMain.getManager().getPermissionManager().injectPermissions(target);
    				dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.PERMS);
    			} else if (packet.getField().equalsIgnoreCase("Preferencias")) {
    				if (!packet.hasExtraValue()) {
    					return;
    				}
    				boolean value = Boolean.valueOf(packet.getExtraValue());
    				
    				if (packet.getFieldValue().equalsIgnoreCase("staffchat")) {
    					dataHandler.getData(DataType.STAFFCHAT).setValue(value);
    					dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.STAFFCHAT);
    				} else if (packet.getFieldValue().equalsIgnoreCase("reports")) {
    					dataHandler.getData(DataType.REPORTS).setValue(value);
    					dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.REPORTS);
    				}
    			}
    		}
    	}
	}
}