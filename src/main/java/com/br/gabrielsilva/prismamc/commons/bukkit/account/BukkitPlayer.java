package com.br.gabrielsilva.prismamc.commons.bukkit.account;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.protocol.ProtocolGetter;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.Sidebar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.permissions.AttachmentManager;
import com.br.gabrielsilva.prismamc.commons.core.data.Data;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BukkitPlayer {

	private String nick, lastMessage;
	private UUID uniqueId;
	private Sidebar sideBar;
	private AttachmentManager attachment;
	private Tag tag;
	private DataHandler dataHandler;
	private boolean is1_8;
	private PlayerRank rank;
	
	private long lastChangeSkin;
	
	public BukkitPlayer(String nick, UUID uniqueId) {
		setNick(nick);
		setUniqueId(uniqueId);
		setLastMessage("");
		setSideBar(null);
		setTag(Groups.MEMBRO.getTag());
		setRank(PlayerRank.UNRANKED);
		setDataHandler(new DataHandler(nick));
		set1_8(false);
		setLastChangeSkin(System.currentTimeMillis() + 10000);
	}
	
	public void updateRank() {
		setRank(PlayerRank.getRanking(getInt(DataType.XP)));
	}
	
	public boolean isStaffer() {
		return getData(DataType.GRUPO).getGrupo().getNivel() >= Groups.TRIAL.getNivel();
	}
	
	public boolean podeTrocarSkin() {
		if (getLastChangeSkin() == 0L) {
			return true;
		}
		
		return getLastChangeSkin() < System.currentTimeMillis();
	}
	
	public boolean containsFake() {
		if (getDataHandler().isCategoryLoaded(DataCategory.PRISMA_PLAYER)) {
			return !getString(DataType.FAKE).equals("");
		} else {
			return false;
		}
	}
	
	public void handleTimers() {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		handleTimers(player);
	}
	
	public void handleTimers(Player player) {
		if (getData(DataType.DOUBLECOINS_TIME).getLong() != 0L) {
			if (System.currentTimeMillis() > dataHandler.getData(DataType.DOUBLECOINS_TIME).getLong()) {
				player.sendMessage("§aO seu DoubleCoins acabou!");
				getData(DataType.DOUBLECOINS_TIME).setValue(0L);
				getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.DOUBLECOINS_TIME);
			}
		}
		 
		if (getData(DataType.DOUBLEXP_TIME).getLong() != 0L) {
			if (System.currentTimeMillis() > dataHandler.getData(DataType.DOUBLEXP_TIME).getLong()) {
				player.sendMessage("§aO seu DoubleXP acabou!");
				getData(DataType.DOUBLEXP_TIME).setValue(0L);
				getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.DOUBLEXP_TIME);
			}
		}
		 
		if (getData(DataType.GRUPO_TIME).getLong() != 0L) {
			if (System.currentTimeMillis() > getData(DataType.GRUPO_TIME).getLong()) {
		        Groups tag = getData(DataType.GRUPO).getGrupo();
		         
		        player.sendMessage(PluginMessages.GRUPO_EXPIRADO.replace("%grupo%", tag.getCor() + "§l" + tag.getNome()));
		         
		        getAttachment().removePermissions(getAttachment().getPermissions());
		        getAttachment().addPermissions(BukkitMain.getManager().getPermissionManager().getPermsFromGroup("membro"));
		         
		        List<String> permsPlayer = getData(DataType.PERMS).getList();
		        if (permsPlayer.size() != 0) {
		            getAttachment().addPermissions(permsPlayer);
		        }
		         
		        TagAPI.update(player, Groups.MEMBRO);
		         
		        getData(DataType.GRUPO).setValue("Membro");
		        getData(DataType.GRUPO_TIME).setValue(0L);
		         
		        getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, true, DataType.GRUPO, DataType.GRUPO_TIME);
		         
		        BukkitClient.sendPacket(player, new PacketBungeeUpdateField(getNick(), "ProxyPlayer", "UpdatePermissions", "membro"));
		        
		        permsPlayer = null;
		        tag = null;
		        
		        PlayerAPI.updateTab(player);
			}
		 }
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(getUniqueId());
	}
	
	public Data getData(DataType dataType) {
		return getDataHandler().getData(dataType);
	}
	
	public int getInt(DataType dataType) {
		return getDataHandler().getInt(dataType);
	}
	
	public String getString(DataType dataType) {
		return getDataHandler().getString(dataType);
	}
	
	public Boolean getBoolean(DataType dataType) {
		return getDataHandler().getBoolean(dataType);
	}
	
	public Long getLong(DataType dataType) {
		return getDataHandler().getLong(dataType);
	}
	
	public void remove(DataType dataType) {
		remove(dataType, 1);
	}
	
	public void remove(DataType dataType, int value) {
		getDataHandler().getData(dataType).remove(value);
	}
	
	public void add(DataType dataType) {
		add(dataType, 1);
	}
	
	public void add(DataType dataType, int value) {
		getDataHandler().getData(dataType).add(value);
	}
	
	public void set(DataType dataType, Object value) {
		getDataHandler().getData(dataType).setValue(value);
	}
	
	public void addXP(int quantia) {
		final int xpAtual = getInt(DataType.XP);
		getData(DataType.XP).setValue(xpAtual + quantia);
		
		PlayerRank newRank = PlayerRank.getRanking(xpAtual + quantia);
		
		if (getRank() != newRank) {
			setRank(newRank);
			//rank uped
			getPlayer().sendMessage("§aVocê subiu de §lRANK!");
			TagAPI.update(getPlayer(), getTag());
			PlayerAPI.updateTab(getPlayer());
		}
		
		newRank = null;
	}
	
	public void removeXP(int quantia) {
		final int xpAtual = getInt(DataType.XP);
		
		if ((xpAtual - quantia) <= 0) {
			getData(DataType.XP).setValue(0);
		} else {
			getData(DataType.XP).setValue(xpAtual - quantia);
		}
		
		PlayerRank newRank = PlayerRank.getRanking(xpAtual + quantia);
		
		if (getRank() != newRank) {
			setRank(newRank);
			getPlayer().sendMessage("§cVocê caiu de §lRANK!");
			//rank down
			TagAPI.update(getPlayer(), getTag());
			PlayerAPI.updateTab(getPlayer());
		}
		newRank = null;
	}
	
	public void updateVersion(Player player) {
		if (ProtocolGetter.getVersion(player) > 5) {
			this.is1_8 = true;
		}
	}
}