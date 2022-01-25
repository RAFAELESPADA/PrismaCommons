package com.br.gabrielsilva.prismamc.commons.bukkit.listeners;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

public class DamageListener implements Listener {

	static HashMap<Material, Double> damageMaterial = new HashMap<>();
	public static HashMap<UUID, Long> receiveHitCooldown = new HashMap<>();

	public static boolean hasCritical = true;
	public static int chance = 30;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player))
			return;

		if (e.isCancelled())
			return;

		Player d = (Player) e.getDamager();

		double dano = 1.0;
		ItemStack itemStack = d.getItemInHand();
		if (itemStack != null) {
			dano = damageMaterial.get(itemStack.getType());

			if (itemStack.containsEnchantment(Enchantment.DAMAGE_ALL)) {
				dano += itemStack.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			}
		}

		for (PotionEffect effect : d.getActivePotionEffects()) {
			if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
				dano += ((effect.getAmplifier() + 1) * 2);
			} else if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
				dano -= (effect.getAmplifier() + 1);
			}
		}

		if (hasCritical) {
			if (isCrital(d)) {
				dano += 1.0D;
			}
		}

		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();

			for (PotionEffect effect : p.getActivePotionEffects()) {
				if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
					dano += (effect.getAmplifier() + 1);
				}
			}

		}
		e.setDamage(dano);
	}

	public void setup() {
		hasCritical = true;
		chance = 16;

		for (Material materiais : Material.values()) {
			String name = materiais.name().toLowerCase();
			if ((!name.contains("sword")) && (!name.contains("pickaxe")) && (!name.contains("spade")) && (!name.contains("axe"))) {
				damageMaterial.put(materiais, 1.0D);
				continue;
			}
			if (name.contains("wood_sword")) ;
			{
				damageMaterial.put(materiais, 1.5D);
			}
			if (name.contains("stone_sword")) ;
			{
				damageMaterial.put(materiais, 2.0D);
			}
			if (name.contains("iron_sword")) ;
			{
				damageMaterial.put(materiais, 2.5D);
			}
			if (name.contains("diamond_sword")) ;
			{
				damageMaterial.put(materiais, 3.0D);

			}
		}
	}
	
	private Random random = new Random();
	
	boolean isCrital(Player p) {
	    return (p.getFallDistance() > 0.0F) && (!p.isOnGround()) &&
	    		(random.nextInt(100) <= chance) && (!p.hasPotionEffect(PotionEffectType.BLINDNESS));
	}
}
