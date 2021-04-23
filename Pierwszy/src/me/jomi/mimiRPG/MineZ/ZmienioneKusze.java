package me.jomi.mimiRPG.MineZ;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.CrossbowMeta;

import me.jomi.mimiRPG.Moduły.Moduł;

@Moduł
public class ZmienioneKusze implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void __(PlayerInteractEvent ev) {
		if (!Material.CROSSBOW.equals(ev.getMaterial())) return;
		if (!ev.getItem().getItemMeta().hasCustomModelData()) return;
		if (((CrossbowMeta) ev.getItem().getItemMeta()).hasChargedProjectiles())
			ev.setCancelled(true);
	}
}
