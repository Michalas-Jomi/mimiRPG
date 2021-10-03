package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.inventory.CraftingInventory;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class AntyCheat implements Listener {
	@EventHandler
	public void wpisywanieKomend(PlayerCommandPreprocessEvent ev) {
		if (ev.getPlayer().getOpenInventory().getTopInventory() instanceof CraftingInventory) return;
		
		ev.setCancelled(true);
		ev.getPlayer().kick(Func.toComponent("§cWykryto niedozwolone zachowanie"), Cause.ILLEGAL_ACTION);
	}
}
