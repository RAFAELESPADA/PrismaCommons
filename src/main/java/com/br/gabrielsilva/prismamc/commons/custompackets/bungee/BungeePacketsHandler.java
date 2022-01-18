package com.br.gabrielsilva.prismamc.commons.custompackets.bungee;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.register.StaffCommand;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.SkinApplier;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.SkinStorage;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.MojangAPI.SkinRequestException;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketKickPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketSetClanTag;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketTeleportPlayerByReport;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketCheckPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketFindPlayerByReport;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketLoadClan;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketUpdateSkin;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class BungeePacketsHandler {

	public void handleCheckPlayer(PacketCheckPlayer packet) throws HandlePacketException {
		final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getNick());
    	if (player == null) {
    		BungeeClient.sendPacketToServer(BungeeClient.getServerByName(packet.getSendedBy()), 
    				new PacketKickPlayer(packet.getNick(), "§cEntre pelo IP principal: \n§cprismamc.com.br"));
    	}
	}
	
	public void handleLoadClan(PacketLoadClan packet) throws HandlePacketException {
		if (!Core.getClanManager().hasClanData(packet.getClan())) {
			Core.getClanManager().loadClan(packet.getClan());
		}
		
    	ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getNick());
    	if (player != null) {
            if (Core.getClanManager().hasClanData(packet.getClan())) {
                Clan clan = Core.getClanManager().getClan(packet.getClan());
                clan.addOnline(player);
                
                BungeeClient.sendPacketToServer(player, new PacketSetClanTag(clan.getNome(), clan.getTag()));
                
                BungeeMain.getManager().getSessionManager().getSession(player).setClan(packet.getClan());
            }
    	}
	}

	public void handleFindPlayerByReport(PacketFindPlayerByReport packet) throws HandlePacketException {
		ProxiedPlayer staffer = ProxyServer.getInstance().getPlayer(packet.getNickStaffer()),
				target = ProxyServer.getInstance().getPlayer(packet.getNickTarget());
		
		if (staffer == null) {
			return;
		}
		
		if (target == null) {
			staffer.sendMessage("§cJogador offline!");
			return;
		}
		
		Jedis jedis = null;
		
		try {
			jedis = Core.getRedis().getPool().getResource();
		} catch (Exception ex) {
			jedis = null;
		} 
		
		if (jedis == null) {
			staffer.sendMessage("§cOcorreu um erro...");
			return;
		}
		
		for (int i = 1; i <= 100; i++) {
			 if (jedis.exists("report:" + i)) {
				 Map<String, String> hash = jedis.hgetAll("report:" + i);
				 if (hash.containsKey("nick")) {
					 if (hash.get("nick").equalsIgnoreCase(packet.getNickTarget())) {
						 hash.put("glassID", "4");
							 
						 jedis.hmset("report:" + i, hash);
						 jedis.expire("report:" + i, 3600);
						 break;
					 }
				 }
			 }
		}
		
		jedis.close();
		jedis = null;
		
		if (!staffer.getServer().getInfo().getName().equalsIgnoreCase(target.getServer().getInfo().getName())) {
			staffer.connect(target.getServer().getInfo());
			
			BungeeMain.runLater(() -> {
				ProxiedPlayer target1 = ProxyServer.getInstance().getPlayer(packet.getNickTarget());
				if (target1 == null) {
					staffer.sendMessage("§cJogador deslogou!");
					return;
				}
				
				if (staffer == null) {
					return;
				}
				
				
				BungeeClient.sendPacketToServer(target.getServer().getInfo(), 
						new PacketTeleportPlayerByReport(packet.getNickStaffer(), packet.getNickTarget()));
				
			}, 1500, TimeUnit.MILLISECONDS);
		} else {
			BungeeClient.sendPacketToServer(target.getServer().getInfo(), 
					new PacketTeleportPlayerByReport(packet.getNickStaffer(), packet.getNickTarget()));
		}
	}
	
	public void handleLoadUpdateSkin(PacketUpdateSkin packet) throws HandlePacketException {
		ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(packet.getNick());
		
		if (proxiedPlayer != null) {
			SkinApplier.applySkin(proxiedPlayer, packet.getSkinToApply());
		}
	}

	public void handleUpdateField(PacketBungeeUpdateField packet) throws HandlePacketException {
		final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getNick());
		
    	if (player != null) {
    		if (packet.getType().equalsIgnoreCase("ProxyPlayer")) {
    			if (packet.getField().equalsIgnoreCase("AddPerm")) {
    				ProxyServer.getInstance().getPluginManager().dispatchCommand(
    						ProxyServer.getInstance().getConsole(), "addperm " + player.getName() + " " + packet.getFieldValue());
    			} else if (packet.getField().equalsIgnoreCase("SetSkin")) {
    				SkinApplier.applySkin(player, packet.getFieldValue());
    			} else if (packet.getField().equalsIgnoreCase("UpdatePermissions")) { 
     				if (!BungeeMain.isValid(player)) {
    					return;
    				}
       				
       	    		final Groups group = Groups.getFromString(packet.getFieldValue());
       	    		
       	    		BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(player);
       	    		bungeePlayer.setInStaffChat(false);
       	    		bungeePlayer.setGrupo(group);
    			} else if (packet.getField().equalsIgnoreCase("UpdateSkin")) {
    				try {
    					SkinStorage.createSkin(packet.getFieldValue());
    					
    					SkinStorage.getOrCreateSkinForPlayer(packet.getFieldValue());
    		            SkinStorage.setPlayerSkin(packet.getNick(), packet.getFieldValue());
    		            
    		            BungeeMain.runLater(() -> {
    			        	SkinApplier.applySkin(player, packet.getFieldValue());
    			        	player.sendMessage("§aSkin atualizada com sucesso!");
    		            });
    				} catch (SkinRequestException e) {
    		        	player.sendMessage("§cOcorreu um erro ao tentar atualizar sua skin, tente novamente.");
    		        }
    			} else if (packet.getField().equalsIgnoreCase("SetSkinNew")) { 
    				try {
    					SkinStorage.getOrCreateSkinForPlayer(packet.getFieldValue());
    		            SkinStorage.setPlayerSkin(packet.getNick(), packet.getFieldValue());
    		            
    		            BungeeMain.runLater(() -> {
    			        	SkinApplier.applySkin(player, packet.getFieldValue());
    			        	player.sendMessage("§aSkin alterada com sucesso");
    		            });
    		        } catch (SkinRequestException e) {
    		        	player.sendMessage("§cOcorreu um erro ao tentar alterar sua skin, tente novamente.");
    		        }
    			} else if (packet.getField().equalsIgnoreCase("AdicionarSessao")) {
    	    		if (BungeeMain.getManager().getSessionManager().hasSession(player)) {
    	    			BungeeMain.getManager().getSessionManager().getSession(player).setAddress(packet.getFieldValue());
    	    			return;
    	    		}
    	    		BungeeMain.getManager().getSessionManager().addSession(player);
    	    		player.sendMessage(TextComponent.fromLegacyText("§aAgora você tem uma sessão no servidor!"));
    	    		BungeeMain.getManager().getSessionManager().getSession(player).setAddress(packet.getFieldValue());
    			} else if (packet.getField().equalsIgnoreCase("AddPerm")) {
    				StaffCommand.handleAddPerm(player.getName(), packet.getFieldValue());
    			} else if (packet.getField().equalsIgnoreCase("UpdateBungeePlayer")) {
					boolean value = Boolean.valueOf(packet.getExtraValue());
    				if (packet.getFieldValue().equalsIgnoreCase("reports")) {
    					if (BungeeMain.getManager().getSessionManager().hasSession(player)) {
    						BungeeMain.getManager().getSessionManager().getSession(player).setViewReports(value);
    					}
    				} else if (packet.getFieldValue().equalsIgnoreCase("staffchat")) {
    					if (BungeeMain.getManager().getSessionManager().hasSession(player)) {
    						BungeeMain.getManager().getSessionManager().getSession(player).setInStaffChat(value);
    					}
    				}
    			}
    		} else if (packet.getType().equalsIgnoreCase("Clan")) {
    			if (!Core.getClanManager().hasClanData(packet.getFieldValue())) {
    				return;
    			}
    			
    			if (packet.getField().equalsIgnoreCase("AddElo")) {
    				Core.getClanManager().getClan(packet.getFieldValue()).addELO(Integer.valueOf(packet.getExtraValue()));
    			} else if (packet.getField().equalsIgnoreCase("RemoveElo")) {
    				Core.getClanManager().getClan(packet.getFieldValue()).removeELO(Integer.valueOf(packet.getExtraValue()));
    			}
    		}
    	}
	}
}