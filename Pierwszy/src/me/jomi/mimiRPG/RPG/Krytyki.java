package me.jomi.mimiRPG.RPG;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Krytyki implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void atak(EntityDamageByEntityEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getDamage() <= 0) return;
		if (!(ev.getDamager() instanceof Player)) return;
		
		GraczRPG gracz = GraczRPG.gracz((Player) ev.getDamager());
		
		if (gracz.szansaKryta.losuj()) {
			ev.setDamage(ev.getDamage() * gracz.dmgKryt.wartość());
			Func.particle(Particle.CRIT, ev.getEntity().getLocation(), 150, 1, 1, 1, 1);
		}
	}
}
