package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Przeładowalny;

// TODO szablonowy config

@Moduł
public class Karabiny implements Listener, Przeładowalny {
	static final HashMap<String, Karabin> karabiny = new HashMap<>();
	static final Config config = new Config("Karabiny");
	
	@EventHandler(priority = EventPriority.HIGH)
	public void usuwanieStzał(ProjectileHitEvent ev) {
		Projectile pocisk = ev.getEntity();
		if (!pocisk.hasMetadata("mimiPocisk")) return;
		
		Location loc = pocisk.getLocation();
		pocisk.remove();
		loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0, 0, 0, .1);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void postrzelenie(EntityDamageByEntityEvent ev) {
		Entity damager = ev.getDamager();
		if (!(damager instanceof Projectile)) return;
		
		Projectile pocisk = (Projectile) damager;
		if (!pocisk.hasMetadata("mimiPocisk")) return;
		
		Karabin karabin = karabiny.get(pocisk.getMetadata("mimiPocisk").get(0).asString());
		
		ev.setDamage(karabin.dmg);
	}
	
	private Karabin karabin(ItemStack item) {
		for (Karabin karabin : karabiny.values())
			if (Func.porównaj(karabin.item, item))
				return karabin;
		return null;
	}
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getAction().equals(Action.PHYSICAL)) return;
		
		Karabin karabin = karabin(ev.getItem());
		if (karabin == null) return;
		
		switch (ev.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			karabin.strzel(ev.getPlayer());
			break;
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			karabin.przybliż(ev.getPlayer());
			break; // TODO member gildi nie może zniszczyć ogniska
		default:
			break;
		}
	}
	@EventHandler public void __(PlayerQuitEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDeathEvent ev)			{ Karabin.odbliż(ev.getEntity()); }
	@EventHandler public void __(InventoryOpenEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerItemHeldEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDropItemEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerSwapHandItemsEvent ev)	{ Karabin.odbliż(ev.getPlayer()); }
	
	@Override
	public void przeładuj() {
		karabiny.clear();
		config.przeładuj();
		for (String klucz : config.klucze(false))
			try {
				Karabin.class.cast(config.wczytaj(klucz));
			} catch (Throwable e) {
				Main.warn("Niepoprawny karabin " + klucz + " w Karabiny.yml");
			}
	}
	@Override
	public String raport() {
		return "§6Wczytane karabiny: §e" + karabiny.size();
	}
}


