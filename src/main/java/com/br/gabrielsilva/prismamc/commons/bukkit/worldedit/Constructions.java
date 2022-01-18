package com.br.gabrielsilva.prismamc.commons.bukkit.worldedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.actionbar.ActionBar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;

@Getter @Setter
public class Constructions {
	
	private Player owner;
	private int blocksPorTick, maxBlocks, blockAtual, blocksPerSecond;
	private List<Location> locations;
	private List<Material> blocksToSet;
	private boolean finished, resetando, cancelTask, in18;
	private Random random;
	private Long started;
	private HashMap<String, Material> blocksToReset;
	
	public Constructions(Player owner, List<Location> locations) {
		this.owner = owner;
		this.blocksPorTick = 2;
		this.blockAtual = 0;
		this.blocksPerSecond = blocksPorTick * 20;
		this.finished = false;
		this.resetando = false;
		this.cancelTask = false;
		this.locations = new ArrayList<>();
		this.blocksToReset = new HashMap<>();
		this.blocksToSet = new ArrayList<>();
		for (Location l : locations) {
			 this.locations.add(l);
		}
		this.in18 = BukkitMain.getManager().getDataManager().getBukkitPlayer(owner.getUniqueId()).is1_8();
		this.maxBlocks = locations.size();
		this.random = new Random();
	}
	
	public void processBlocks() {
		for (int i = 0; i < maxBlocks; i++) {
			 Location location = locations.get(this.blockAtual + i);
			 String formated = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
			 if (this.blocksToReset.containsKey(formated)) {
				 continue;
			 }
			 Block block = location.getBlock();
			 this.blocksToReset.put(formated, block.getType());
		}
	}
	
	public void start() {
		processBlocks();
		
		this.started = System.currentTimeMillis();
		
		new BukkitRunnable() {
		public void run() {
			if (cancelTask) {
				cancel();
				sendMessageIfPlayerIsOnline("§e§lWORLDEDIT §fConstrução concluída em: §e" + DateUtils.getElapsed(started), true);
				blockAtual = 0;
				finished = true;
				return;
			}
			putBlock();
		}
		}.runTaskTimer(BukkitMain.getInstance(), 1L, 1L);
	}
	
	public void startRegress() {
		this.blockAtual = 0;
		this.resetando = true;
		new BukkitRunnable() {
		public void run() {
			if (cancelTask) {
				cancel();
				sendMessageIfPlayerIsOnline("§e§lWORLDEDIT §fConstrução resetada com sucesso.", true);
				finished = true;
				BukkitMain.getManager().getWorldEditManager().removeConstructionByUUID(getOwner().getUniqueId());
				return;
			}
			regressBlock();
		}
		}.runTaskTimer(BukkitMain.getInstance(), 1L, 1L);
	}
	
	public void sendMessageIfPlayerIsOnline(String mensagem, boolean msg) {
		if (getOwner() != null && getOwner().isOnline()) {
			if (isIn18()) {
			    ActionBar.sendActionBarWithoutCheck(getOwner(), mensagem);
			}
			if (msg) {
				getOwner().sendMessage(mensagem);
			}
		}
	}
	
	public void putBlock() {
		if (this.finished) {
			return;
		}
		if (this.blockAtual >= maxBlocks) {
			this.cancelTask = true;
			return;
		}
		
		int atualBlocksPertick = this.blocksPorTick;
		for (int i = 0; i < atualBlocksPertick; i++) {
			 try {
				 Location location = locations.get(this.blockAtual + i);
				 Material escolhido = getRandomOurExactBlock();
				 setAsyncBlock(location.getWorld(), location, escolhido.getId());
			 } catch (IndexOutOfBoundsException e) {
				 continue;
			 } catch (NullPointerException e) {
				 continue;
			 }
		}
		
		sendMessageIfPlayerIsOnline("§e§lWORLDEDIT §fConstrução em andamento... §e("+
		    ServerAPI.reformularValor(blockAtual)+"/"+ServerAPI.reformularValor(maxBlocks)+") " + ServerAPI.reformularValor(blocksPerSecond) + "b/ps", false);
		
		this.blockAtual+=atualBlocksPertick;
	}
	
	public void regressBlock() {
		if (this.finished) {
			return;
		}
		if (this.blockAtual >= maxBlocks) {
			this.cancelTask = true;
			return;
		}
		int atualBlocksPertick = this.blocksPorTick;
		for (int i = 0; i < atualBlocksPertick; i++) {
			 try {
				 Location location = locations.get(this.blockAtual + i);
				 Block block = location.getBlock();
				 
				 String formated = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
				 Material material = blocksToReset.get(formated);
				 setAsyncBlock(location.getWorld(), location, material.getId());
			 } catch (IndexOutOfBoundsException e) {
				 continue;
			 } catch (NullPointerException e) {
				 continue;
			 }
		}
		sendMessageIfPlayerIsOnline("§e§lWORLDEDIT §fResetando construção... §e("+
			    ServerAPI.reformularValor(blockAtual)+"/"+ServerAPI.reformularValor(maxBlocks)+") " + ServerAPI.reformularValor(blocksPerSecond) + "b/ps", false);
		this.blockAtual+=atualBlocksPertick;
	}
	
	public void setAsyncBlock(World world, Location location, int blockId) {
		setAsyncBlock(world, location, blockId, (byte) 0);
	}
	
	public void setAsyncBlock(World world, Location location, int blockId, byte data) {
		setAsyncBlock(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockId, data);
	}
	
	public void setAsyncBlock(World world, int x, int y, int z, int blockId, byte data) {
		net.minecraft.server.v1_8_R3.World w = ((CraftWorld) world).getHandle();
		net.minecraft.server.v1_8_R3.Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
		BlockPosition bp = new BlockPosition(x, y, z);
		int i = blockId + (data << 12);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(i);
		chunk.a(bp, ibd);
		w.notify(bp);
	}
	
	public Material getRandomOurExactBlock() {
		if (blocksToReset.size() == 1) {
			return blocksToSet.get(0);
		}
		return blocksToSet.get(random.nextInt(blocksToSet.size()));
	}
	
	public void setBlocksPorTick(int blocksPorTick) {
		this.blocksPorTick = blocksPorTick;
		this.blocksPerSecond = blocksPorTick * 20;
	}
	
	public void setBlocksToSet(List<Material> blocksToSet) {
		for (Material l : blocksToSet) {
			 this.blocksToSet.add(l);
		}
	}
}