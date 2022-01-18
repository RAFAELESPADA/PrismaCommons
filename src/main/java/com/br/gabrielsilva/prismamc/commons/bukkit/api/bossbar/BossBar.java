package com.br.gabrielsilva.prismamc.commons.bukkit.api.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

@Getter @Setter
public class BossBar {

	private Player player;
	private EntityWither wither;
	private boolean cancelar, tempo;
	private int segundos;
	
	public BossBar(Player p, String mensagem, boolean tempo) {
		this.player = p;
		this.cancelar = false;
		this.tempo = tempo;
		this.segundos = 20;
		
		this.wither = new EntityWither(((CraftWorld) p.getWorld()).getHandle());
		
		wither.setInvisible(true);
		wither.setCustomName(mensagem);
		wither.getEffects().clear();
		wither.setLocation(getViableLocation().getX(), getViableLocation().getY(), getViableLocation().getZ(), 0, 0);
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(wither));
	}
	
	public void onSecond() {
		if (cancelar) {
			remover();
			return;
		}
		if (!player.isOnline()) {
			remover();
			this.cancelar = true;
			return;
		}
		if (!tempo) {
			atualizar();
			return;
		}
		
		if (segundos == 0) {
			remover();
			this.cancelar = true;
			return;
		}	
		segundos--;
		atualizar();
	}
	
	public void atualizar() {
		if (wither == null || player == null) {
			return;
		}
		wither.setLocation(getViableLocation().getX(), getViableLocation().getY(), getViableLocation().getZ(), 0, 0);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(wither));
	}

	public Location getViableLocation() {
		if (player == null)
			return new Location(Bukkit.getWorld("world"), 0, 0, 0);
		return player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(28));
	}
	
	public void remover() {
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(wither.getId()));
	}
}