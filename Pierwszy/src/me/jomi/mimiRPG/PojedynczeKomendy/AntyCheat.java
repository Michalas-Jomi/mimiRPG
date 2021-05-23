package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.CraftingInventory;

import me.jomi.mimiRPG.Moduły.Moduł;

@Moduł
public class AntyCheat implements Listener {
	@EventHandler
	public void wpisywanieKomend(PlayerCommandPreprocessEvent ev) {
		if (ev.getPlayer().getOpenInventory().getTopInventory() instanceof CraftingInventory) return;
		
		ev.setCancelled(true);
		ev.getPlayer().kickPlayer("§cWykryto niedozwolone zachowanie");
	}
}
