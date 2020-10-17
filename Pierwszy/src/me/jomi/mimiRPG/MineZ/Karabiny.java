package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
		
		pocisk.remove();
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
	
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		
		ItemStack item = ev.getItem();
		
		for (Karabin karabin : karabiny.values())
			if (Func.porównaj(karabin.item, item)) {
				karabin.strzel(ev.getPlayer());
				return;
			}
	}

	
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


