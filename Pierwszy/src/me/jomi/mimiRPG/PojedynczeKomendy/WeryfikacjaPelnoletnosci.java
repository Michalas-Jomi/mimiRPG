package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;

public class WeryfikacjaPelnoletnosci implements Listener{
	
	private static Inventory inv = Bukkit.createInventory(null, 27, "�4Wst�p tylko dla �ldoros�ych!");
	private static String prefix = "�2[�a18+�2] �6";
	
	public WeryfikacjaPelnoletnosci() {
		ItemStack item = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&9Czy masz uko�czone 18 lat?", null);
		for (int i=0; i<27; i++)
			inv.setItem(i, item);
		inv.setItem(11, Func.stw�rzItem(Material.LIME_STAINED_GLASS_PANE, 1, "&a&lTAK", null));
		inv.setItem(15, Func.stw�rzItem(Material.RED_STAINED_GLASS_PANE,  1, "&c&lNIE", null));
	}
	
	@EventHandler
	public void wybor(InventoryClickEvent ev) {
		if (ev.getView().getTitle().equals("�4Wst�p tylko dla �ldoros�ych!")) {
			ev.setCancelled(true);
			ItemStack item = ev.getCurrentItem();
			if (item == null) return;
			Player p = (Player) ev.getWhoClicked();
			switch(item.getItemMeta().getDisplayName()) {
			case "�a�lTAK":
				p.closeInventory();
				p.sendMessage(prefix + "Przyznano dost�p");
				p.chat("/warp burdeltp");
				return;
			case "�c�lNIE":
				p.closeInventory();
				p.sendMessage(prefix + "��danie odrzucone");
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
