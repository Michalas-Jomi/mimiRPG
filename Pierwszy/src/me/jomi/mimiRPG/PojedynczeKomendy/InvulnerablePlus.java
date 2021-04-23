package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.jomi.mimiRPG.Moduły.Moduł;


@Moduł
public class InvulnerablePlus implements Listener {
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (ev.getEntity().isInvulnerable())
			ev.setCancelled(true);
	}
}
