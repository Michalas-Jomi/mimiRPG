package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BlockSkrzyńNaZwierzętach implements Listener {
	@EventHandler
	public void blokadaSkrzynekNaZwierzetach(PlayerInteractEntityEvent ev) {
		EntityType typ = ev.getRightClicked().getType();
		Function<ItemStack, Boolean> sprawdz = item -> {
			if (item == null) return false;
			Material mat = item.getType();
			if (mat.equals(Material.CHEST) || mat.equals(Material.TRAPPED_CHEST))
				if (typ.equals(EntityType.MULE) || typ.equals(EntityType.LLAMA) || 
						typ.equals(EntityType.TRADER_LLAMA) || typ.equals(EntityType.DONKEY))
					return true;
			return false;
		};
		PlayerInventory inv = ev.getPlayer().getInventory();
		if (sprawdz.apply(inv.getItemInMainHand()) || sprawdz.apply(inv.getItemInOffHand()))
			ev.setCancelled(true);
	}
}
