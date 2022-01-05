package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class DwuręczneTopory implements Listener {
	@EventHandler
	public void swapHand(PlayerSwapHandItemsEvent ev) {
		test(ev.getPlayer());
	}
	@EventHandler
	public void click(InventoryClickEvent ev) {
		if (ev.getWhoClicked() instanceof Player)
			test((Player) ev.getWhoClicked());
	}
	@EventHandler
	public void click(EntityPickupItemEvent ev) {
		if (ev.getEntity() instanceof Player)
			test((Player) ev.getEntity());
	}
	@EventHandler
	public void slot(PlayerItemHeldEvent ev) {
		test(ev.getPlayer());
	}
	
	public void test(Player p) {
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			Func.wykonajDlaNieNull(p.getInventory().getItemInMainHand(), main -> {
				Func.wykonajDlaNieNull(p.getInventory().getItemInOffHand(), off -> {
					if (main.getType().toString().endsWith("_AXE")) {
						ItemStack item = off.clone();
						p.getInventory().setItemInOffHand(null);
						Func.dajItem(p, item);
					}
				});
			});
		});
	}
}
