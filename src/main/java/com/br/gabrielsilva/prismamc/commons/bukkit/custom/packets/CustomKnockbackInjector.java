package com.br.gabrielsilva.prismamc.commons.bukkit.custom.packets;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.packets.utils.Hit;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.FieldResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.values.ConfigValues;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.values.ConfigValues.ValuesGlobalConfig;

import lombok.Getter;
import lombok.Setter;

public class CustomKnockbackInjector {

	private Map<UUID, Hit> damaged;
	
	@Getter @Setter
	private double 
	
	groundHorizMultiplier,
	groundVertMultiplier,
	airHorizMultiplier,
	airVertMultiplier,
	sprintMultiplierHoriz,
	sprintMultiplierVert,
	sprintYawFactor;
	
	public CustomKnockbackInjector(Plugin pl) {
		damaged = new ConcurrentHashMap<>();
		
		if (!BukkitMain.getManager().getConfigManager().isConfigLoaded()) {
			BukkitMain.getManager().getConfigManager().loadGlobalConfig();
		}
		
		setGroundHorizMultiplier(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_GROUND_HORIZONTAL_MULTIPLIER.getKey()));
		setGroundVertMultiplier(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_GROUND_VERTICAL_MULTIPLIER.getKey()));
		
		setAirHorizMultiplier(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_AIR_HORIZONTAL_MULTIPLIER.getKey()));
		setAirVertMultiplier(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_AIR_VERTICAL_MULTIPLIER.getKey()));
		
		setSprintMultiplierHoriz(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_SPRINT_MULTIPLIER_HORIZONTAL.getKey()));
		setSprintMultiplierVert(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_SPRINT_MULTIPLIER_VERTICAL.getKey()));
		setSprintYawFactor(ConfigValues.getDoubleByGlobalConfig(ValuesGlobalConfig.KNOCKBACK_SPRINT_YAW_FACTOR.getKey()));
		
		registerListeners();
	}
	
    /**private void velocityHandler(final PacketEvent event) {
        final WrapperPlayServerEntityVelocity velPacketWrapped = new WrapperPlayServerEntityVelocity(event.getPacket());
        Player player = null;
        final int idFromPacket = velPacketWrapped.getEntityId();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (p.getEntityId() == idFromPacket) {
                player = p;
                break;
            }
        }
        if (player == null) {
            return;
        }
        if (!this.damaged.containsKey(player.getUniqueId())) {
            return;
        }
        final Hit hit = this.damaged.get(player.getUniqueId());
        this.damaged.remove(player.getUniqueId());
        
        Vector resultKb;
        
		if (hit != null) {
		    Vector initKb = hit.getDirection().setY(1);
		    Vector attackerYaw = hit.getAttacker().getLocation().getDirection().normalize().setY(1);
		        
		    if (hit.isSprintKb()) {
		        resultKb = initKb.clone().multiply(1.0D - getSprintYawFactor()).add(attackerYaw.clone().multiply(getSprintYawFactor()));
		            
		        resultKb.setX(resultKb.getX() * getSprintMultiplierHoriz());
		        resultKb.setY(resultKb.getY() * getSprintMultiplierVert());
		        resultKb.setZ(resultKb.getZ() * getSprintMultiplierHoriz());
		    } else {
		     	resultKb = initKb;
		    }
		        
		    double horizMultiplier = player.isOnGround() ? getGroundHorizMultiplier() : getAirHorizMultiplier();
		    double vertMultiplier = player.isOnGround() ? getGroundVertMultiplier() : getAirVertMultiplier();
		      
		    resultKb = new Vector(resultKb.getX() * horizMultiplier, resultKb.getY() * vertMultiplier, resultKb.getZ() * horizMultiplier);
		        
		    if (hit.getKbEnchantLevel() < 0) {
		        double KBEnchAdder = hit.getKbEnchantLevel() * 0.45D;
		        double distance = Math.sqrt(Math.pow(resultKb.getX(), 2.0D) + Math.pow(resultKb.getZ(), 2.0D));
		        double ratioX = resultKb.getX() / distance;
		        ratioX = ratioX * KBEnchAdder + resultKb.getX();
		        double ratioZ = resultKb.getZ() / distance;
		        ratioZ = ratioZ * KBEnchAdder + resultKb.getZ();
		        resultKb = new Vector(ratioX, resultKb.getY(), ratioZ);
		    }
            velPacketWrapped.setVelocityX(resultKb.getX());
            velPacketWrapped.setVelocityY(resultKb.getY());
            velPacketWrapped.setVelocityZ(resultKb.getZ());
        }
    }*/

	private void registerListeners() {
		
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			
			@EventHandler(ignoreCancelled = true)
			public void onDamage(EntityDamageByEntityEvent e) {
				if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player))
			        return; 
			    
			    Player attacker = (Player)e.getDamager();
			    Player victim = (Player)e.getEntity();
			    Vector direction = new Vector(victim.getLocation().getX() - attacker.getLocation().getX(), 0.0D, victim.getLocation().getZ() - attacker.getLocation().getZ());
			    int kbEnchantLevel = attacker.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK);
			    Hit hit = new Hit(direction.normalize(), attacker.isSprinting(), kbEnchantLevel, attacker);
			    damaged.put(victim.getUniqueId(), hit);
			}
			
			@EventHandler
			public void onQuit(PlayerQuitEvent event) {
				if (damaged.containsKey(event.getPlayer().getUniqueId())) {
					damaged.remove(event.getPlayer().getUniqueId());
				}
			}
			
			@EventHandler
			public void onPlayerVelocity(PlayerVelocityEvent event) {
				Player player = event.getPlayer();
				EntityDamageEvent lastDamage = player.getLastDamageCause();
				if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) {
					return;
				}
				if (((EntityDamageByEntityEvent) lastDamage).getDamager() instanceof Player) {
					if (damaged.containsKey(player.getUniqueId())) {
						final Hit hit = damaged.get(player.getUniqueId());
					
				        damaged.remove(player.getUniqueId());
				        
				        Vector resultKb;
				        
						if (hit != null) {
						    Vector initKb = hit.getDirection().setY(1);
						    Vector attackerYaw = hit.getAttacker().getLocation().getDirection().normalize().setY(1);
						        
						    if (hit.isSprintKb()) {
						        resultKb = initKb.clone().multiply(1.0D - getSprintYawFactor()).add(attackerYaw.clone().multiply(getSprintYawFactor()));
						            
						        resultKb.setX(resultKb.getX() * getSprintMultiplierHoriz());
						        resultKb.setY(resultKb.getY() * getSprintMultiplierVert());
						        resultKb.setZ(resultKb.getZ() * getSprintMultiplierHoriz());
						    } else {
						     	resultKb = initKb;
						    }
						        
						    double horizMultiplier = player.isOnGround() ? getGroundHorizMultiplier() : getAirHorizMultiplier();
						    double vertMultiplier = player.isOnGround() ? getGroundVertMultiplier() : getAirVertMultiplier();
						      
						    resultKb = new Vector(resultKb.getX() * horizMultiplier, resultKb.getY() * vertMultiplier, resultKb.getZ() * horizMultiplier);
						        
						    if (hit.getKbEnchantLevel() < 0) {
						        double KBEnchAdder = hit.getKbEnchantLevel() * 0.45D;
						        double distance = Math.sqrt(Math.pow(resultKb.getX(), 2.0D) + Math.pow(resultKb.getZ(), 2.0D));
						        double ratioX = resultKb.getX() / distance;
						        ratioX = ratioX * KBEnchAdder + resultKb.getX();
						        double ratioZ = resultKb.getZ() / distance;
						        ratioZ = ratioZ * KBEnchAdder + resultKb.getZ();
						        resultKb = new Vector(ratioX, resultKb.getY(), ratioZ);
						    }
						    
						    event.getVelocity().setX(resultKb.getX());
						    event.getVelocity().setY(resultKb.getY());
						    event.getVelocity().setZ(resultKb.getZ());
						}
					}
				}
			}
			
		}, BukkitMain.getInstance());
	}
	
	public static void setPacketValue(String field, Object value, Object packet) {
		FieldResolver fieldResolver = new FieldResolver(packet.getClass());
		try {
			fieldResolver.resolve(field).set(packet, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Object getPacketValue(String field, Object packet) {
		FieldResolver fieldResolver = new FieldResolver(packet.getClass());
		try {
			return fieldResolver.resolve(field).get(packet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}