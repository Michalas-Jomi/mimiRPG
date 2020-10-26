package me.jomi.mimiRPG.SkyBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class WeryfikacjaPelnoletnosci implements Listener{
	
	private static Inventory inv = Bukkit.createInventory(null, 27, "§4Wstęp tylko dla §ldorosłych!");
	private static String prefix = "§2[§a18+§2] §6";
	
	public WeryfikacjaPelnoletnosci() {
		ItemStack item = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&9Czy masz ukończone 18 lat?", null);
		for (int i=0; i<27; i++)
			inv.setItem(i, item);
		inv.setItem(11, Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, 1, "&a&lTAK", null));
		inv.setItem(15, Func.stwórzItem(Material.RED_STAINED_GLASS_PANE,  1, "&c&lNIE", null));
	}
	
	@EventHandler
	public void wybor(InventoryClickEvent ev) {
		if (ev.getView().getTitle().equals("§4Wstęp tylko dla §ldorosłych!")) {
			ev.setCancelled(true);
			ItemStack item = ev.getCurrentItem();
			if (item == null) return;
			Player p = (Player) ev.getWhoClicked();
			switch(item.getItemMeta().getDisplayName()) {
			case "§a§lTAK":
				p.closeInventory();
				p.sendMessage(prefix + "Przyznano dostęp");
				p.chat("/warp burdeltp");
				return;
			case "§c§lNIE":
				p.closeInventory();
				p.sendMessage(prefix + "Żądanie odrzucone");
				return;
			}
		}
	}
	@EventHandler
	public void komenda(PlayerCommandPreprocessEvent ev) {
		String[] lista = ev.getMessage().split(" ");
		if (lista.length >= 2 && lista[0].equalsIgnoreCase("/warp") && lista[1].equalsIgnoreCase("burdel")) {
			ev.setCancelled(true);
			ev.getPlayer().openInventory(inv);
		}
	}
	
}
