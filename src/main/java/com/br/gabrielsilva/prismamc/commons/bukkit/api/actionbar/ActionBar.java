package com.br.gabrielsilva.prismamc.commons.bukkit.api.actionbar;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class ActionBar {

    public static void sendActionBar(Player player, String rawMessage) {
    	if (!BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).is1_8()) {
    		return;
    	}
    	
        CraftPlayer craftPlayer = (CraftPlayer) player;
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + rawMessage + "\"}");
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(cbc, (byte) 2);
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }
    
    public static void sendActionBarWithoutCheck(Player player, String rawMessage) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + rawMessage + "\"}");
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(cbc, (byte) 2);
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }
}