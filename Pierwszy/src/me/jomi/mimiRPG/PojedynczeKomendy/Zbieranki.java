package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Zbieranki implements Listener {
	Set<UUID> uuids = new HashSet<>(); 
	
	
	void ukryj(Player p, Entity armor) {
		Packet<?> packet = new PacketPlayOutEntityEquipment(
				armor.getEntityId(),
				Arrays.asList(new Pair<>(EnumItemSlot.HEAD, ItemStack.b))
				);
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);;
	}
	void zresp(Location loc, org.bukkit.inventory.ItemStack item) {
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.getEquipment().setHelmet(item);
		as.setInvulnerable(true);
		as.setGravity(false);
		as.setVisible(false);
		as.teleport(loc);
	}
	
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		Gracz g = Gracz.wczytaj(ev.getPlayer());
		for (int i=0; i < g.zbierankiZebrane.size(); i++) {
			UUID uuid = UUID.fromString(g.zbierankiZebrane.get(i));
			Entity e = Bukkit.getEntity(uuid);
			if (e == null || !(e instanceof ArmorStand))
				g.zbierankiZebrane.remove(i--);
			else
				ukryj(ev.getPlayer(), e);
		}
	}
	@EventHandler
	public void interact(PlayerInteractAtEntityEvent ev) {
		UUID uuid = ev.getRightClicked().getUniqueId();
		if (uuids.contains(uuid)) {
			ArmorStand armorStand = (ArmorStand) ev.getRightClicked();
			Gracz g = Gracz.wczytaj(ev.getPlayer());
			if (!g.zbierankiZebrane.contains(uuid.toString())) {
				Func.particle(Particle.TOTEM, armorStand.getLocation().add(0, armorStand.getHeight() + .2, 0), 20, 1, 1, 1, .1);
				Func.particle(Particle.ASH,   armorStand.getLocation().add(0, armorStand.getHeight() + .2, 0), 5, 0, 0, 0, 0);
				
				g.zbierankiZebrane.add(uuid.toString());
				g.zapisz();
				
				ukryj(ev.getPlayer(), armorStand);
				
				Func.dajItem(ev.getPlayer(), armorStand.getEquipment().getHelmet());
			}
		}
	}
}
