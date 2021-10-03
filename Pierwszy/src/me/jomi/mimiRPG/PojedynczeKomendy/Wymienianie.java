package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Wymienianie implements Listener{
	public static String prefix = Func.prefix("Handel");
	private static ItemStack środek = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "§2Wymiana", Arrays.asList("§bAby tranzakcja byłą udana", "§boboje graczy musi ją zaakceptować"));
	
	public static HashMap<String, String> prozby = new HashMap<>();
	public static List<String> handlujący = Lists.newArrayList();
	
	
	private static void handel(Player p1, Player p2) {
		String nick1 = p1.getName();
		String nick2 = p2.getName();
		Inventory inv = stwórzMenu(nick1, nick2);
		p1.openInventory(inv);
		p2.openInventory(inv);
		handlujący.add(nick1);
		handlujący.add(nick2);
	}
	
	private static void anulujHandel(Inventory inv, Player anulant) {
		Player p1 = (Player) inv.getViewers().get(0);
		Player p2 = (Player) inv.getViewers().get(1);
		rozdajItemy(inv, p1, p2);
		if (anulant.getName().equals(p1.getName())) {
			Player p = p1;
			p2 = p1;
			p1 = p;
		}
		p1.sendMessage(prefix + "§e" + p2.getName() + "§6 anulował handel");
		p2.sendMessage(prefix + "Anulowałeś handel z graczem §e" + p1.getName());
		
	}
	private static void udana(Inventory inv) {
		Player p1 = (Player) inv.getViewers().get(0);
		Player p2 = (Player) inv.getViewers().get(1);
		rozdajItemy(inv, p2, p1);
		p1.sendMessage(prefix + "Handel z graczem §e" + p2.getName() + "§6 zakończył się sukcesem");
		p2.sendMessage(prefix + "Handel z graczem §e" + p1.getName() + "§6 zakończył się sukcesem");
	}
	
	private static void rozdajItemy(Inventory inv, Player lewy, Player prawy) {
		wyłączHandel(lewy, prawy);
		for(int y=0; y<4; y++)
			for (int x=0; x<3; x++) {
				int slot = y*9+x;
				Func.dajItem(lewy, inv.getItem(slot));
				Func.dajItem(prawy, inv.getItem(slot+6));
			}
	}
	private static void wyłączHandel(Player p1, Player p2) {
		handlujący.remove(p1.getName());
		handlujący.remove(p2.getName());
		
		prozby.remove(p1.getName());
		prozby.remove(p2.getName());

		Func.dajItem(p1, p1.getItemOnCursor()); p1.setItemOnCursor(null);
		Func.dajItem(p2, p2.getItemOnCursor()); p2.setItemOnCursor(null);
		
		p1.closeInventory();
		p2.closeInventory();
	}
	
	
	
	// Menu
	
	private static Inventory stwórzMenu(String nick1, String nick2) {
		Inventory inv = Func.createInventory(null, 4*9, "§1§lWymiana§2");
		for (int i=4; i<9*4; i+=9)
			inv.setItem(i, środek);
		
		ItemStack głos = dajGłos(nick1, false);
		for (int i=3; i<9*4; i+=9)
			inv.setItem(i, głos);
		
		głos = dajGłos(nick2, false);
		for (int i=5; i<9*4; i+=9)
			inv.setItem(i, głos);
		
		return inv;
	}
	private static ItemStack dajGłos(String nick, boolean zaakceptowano) {
		return Func.stwórzItem(
				zaakceptowano ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
				(zaakceptowano ? "§a" : "§c") + "Nie zaakceptowano",
				Arrays.asList("§b" + nick));
	}
	
	
	// EventHandler
	
	@EventHandler
	public void handel(PlayerInteractEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof Player)) return;
		Player p  = ev.getPlayer();
		
		if (!p.hasPermission("mimiRPG.handel")) return;
		if (!p.isSneaking()) return;
		
		String nick1 = p.getName();
		
		if (prozby.containsKey(nick1)) return;
		if (handlujący.contains(nick1)) return;
		
		Player p2 = (Player) ev.getRightClicked();
		String nick2 = p2.getName();
		
		if (Bukkit.getPlayer(nick2) == null) return;
		
		if (nick1.equals(prozby.get(nick2)))
			handel(p2, p);
		else {
			prozby.put(nick1, nick2);
			p.sendMessage(prefix  + "Wysłano propozycję handlu do gracza §e"   + nick2);
			p2.sendMessage(prefix + "Otrzymano propozycję handlu od gracza §e" + nick1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			    @Override
				public void run() {
			    	if (nick2.equals(prozby.get(nick1)) && !handlujący.contains(nick1)) {
			    		prozby.remove(nick1);
			    		p.sendMessage(prefix  + "Propozycja handlu z graczem §e" + nick2 + "§6 wygasła");
			    		p2.sendMessage(prefix + "Propozycja handlu gracza §e" + nick1 + "§6 wygasła");
			    	}
			    }
			}, 400);
		}
	}
	@EventHandler
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (handlujący.contains(ev.getEntity().getName()))
			ev.setCancelled(true);
	}
	@EventHandler
	public void przeciąganie(InventoryDragEvent ev) {
		if (handlujący.contains(ev.getWhoClicked().getName())) {
			boolean lewa = prozby.containsKey(ev.getWhoClicked().getName());
			for (int slot : ev.getRawSlots())
				if (!sprawdzSlot(slot, lewa)) {
					ev.setCancelled(true);
					return;
				}
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (handlujący.contains(ev.getPlayer().getName()))
			anulujHandel(ev.getInventory(), (Player) ev.getPlayer());
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (!handlujący.contains(ev.getWhoClicked().getName())) return;
		switch (ev.getClick()) {
		case DOUBLE_CLICK:
		case SHIFT_RIGHT:
		case SHIFT_LEFT:
			ev.setCancelled(true);
			return;
		default:
			int slot = ev.getRawSlot();
			if (slot >= 9*4 || slot < 0) return;
			
			String nick = ev.getWhoClicked().getName();
			boolean lewa = prozby.containsKey(nick);
			if (!sprawdzSlot(slot, lewa))
				ev.setCancelled(true);
			else
				odakceptuj(ev.getInventory(), nick, lewa);

			if ((lewa && slot % 9 == 3) || (!lewa && slot % 9 == 5))
				zaakceptuj(ev.getInventory(), nick, lewa);
		}
	}
	private static void odakceptuj(Inventory inv, String nick, boolean lewa) {
		String lewy = lewa ? nick : handlujący.get(handlujący.indexOf(nick) - 1);
		wypełnijKolumne(inv, dajGłos(lewy, false), 3);
		wypełnijKolumne(inv, dajGłos(prozby.get(lewy), false), 5);
	}
	private static void zaakceptuj(Inventory inv, String nick, boolean lewa) {
		int st = lewa ? 3 : 5;
		boolean zaakceptowane = inv.getItem(st).getType().equals(Material.LIME_STAINED_GLASS_PANE);
		wypełnijKolumne(inv, dajGłos(nick, !zaakceptowane), st);
		if (!zaakceptowane && inv.getItem(st == 5 ? 3 : 5).getType().equals(Material.LIME_STAINED_GLASS_PANE))	
			udana(inv);
	}
	public static void wypełnijKolumne(Inventory inv, ItemStack item, int st) {
		for (int i=st; i<inv.getSize(); i+=9)
			inv.setItem(i, item);
	}
	private boolean sprawdzSlot(int slot, boolean lewa) {
		return  (lewa  && slot % 9 < 3) ||
				(!lewa && slot % 9 > 5) ||
				(slot >= 4*9 || slot < 0);
	}
}
