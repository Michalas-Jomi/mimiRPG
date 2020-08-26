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

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;

public class Wymienianie implements Listener{
	public static String prefix = Func.prefix("Handel");
	private static ItemStack �rodek = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "�2Wymiana", Arrays.asList("�bAby tranzakcja by�� udana", "�boboje graczy musi j� zaakceptowa�"));
	
	public static HashMap<String, String> prozby = new HashMap<>();
	public static List<String> handluj�cy = Lists.newArrayList();
	
	
	private static void handel(Player p1, Player p2) {
		String nick1 = p1.getName();
		String nick2 = p2.getName();
		Inventory inv = stw�rzMenu(nick1, nick2);
		p1.openInventory(inv);
		p2.openInventory(inv);
		handluj�cy.add(nick1);
		handluj�cy.add(nick2);
	}
	private static void anulujHandel(Inventory inv, Player anulant) {
		Player p1 = (Player) inv.getViewers().get(0);
		Player p2 = (Player) inv.getViewers().get(1);
		anulujHandel(inv, p1, p2);
		if (anulant.getName().equals(p1.getName())) {
			p2.sendMessage(prefix + "�e" + anulant.getName() + "�6 anulowa� handel");
			anulant.sendMessage(prefix + "Anulowa�e� handel z graczem �e" + p2.getName());
		} else {
			p1.sendMessage(prefix + "�e" + anulant.getName() + "�6 anulowa� handel");
			anulant.sendMessage(prefix + "Anulowa�e� handel z graczem �e" + p1.getName());
			
		}
		
	}
	private static void anulujHandel(Inventory inv, Player p1, Player p2) {
		wy��czHandel(p1, p2);
		for(int y=0; y<4; y++)
			for (int x=0; x<3; x++) {
				dajItem(p1, inv.getItem(y*9+x));
				dajItem(p2, inv.getItem(y*9+x+6));
			}
	}
	private static void wy��czHandel(Player p1, Player p2) {
		handluj�cy.remove(p1.getName());
		handluj�cy.remove(p2.getName());
		prozby.remove(p1.getName());
		prozby.remove(p2.getName());
		p1.closeInventory();
		p2.closeInventory();
	}
	
	private static Inventory stw�rzMenu(String nick1, String nick2) {
		Inventory inv = Bukkit.createInventory(null, 4*9, "�1�lWymiana�2");
		for (int i=4; i<9*4; i+=9)
			inv.setItem(i, �rodek);
		
		ItemStack g�os = dajG�os(nick1, false);
		for (int i=3; i<9*4; i+=9)
			inv.setItem(i, g�os);
		
		g�os = dajG�os(nick2, false);
		for (int i=5; i<9*4; i+=9)
			inv.setItem(i, g�os);
		
		return inv;
	}
	private static ItemStack dajG�os(String nick, boolean zaakceptowano) {
		if (zaakceptowano)
			return Func.stw�rzItem(Material.LIME_STAINED_GLASS_PANE, 1, "�aZaakceptowano", 	   Arrays.asList("�b" + nick));
		else
			return Func.stw�rzItem(Material.RED_STAINED_GLASS_PANE,  1, "�cNie zaakceptowano", Arrays.asList("�b" + nick));
	}
	public static void dajItem(Player p, ItemStack item) {
		if (item == null || item.getType().isAir()) return;
		if (p.getInventory().firstEmpty() == -1)
			p.getWorld().dropItem(p.getLocation(), item);
		else
			p.getInventory().addItem(item);
	}
	private static void udana(Inventory inv) {
		Player p1 = (Player) inv.getViewers().get(0);
		Player p2 = (Player) inv.getViewers().get(1);
		wy��czHandel(p1, p2);
		for (int y=0; y<4; y++)
			for (int x=0; x<3; x++) {
				int slot = y*9+x;
				dajItem(p1, inv.getItem(slot+6));
				dajItem(p2, inv.getItem(slot));
			}
		p1.sendMessage(prefix + "Handel z gracze �e" + p2.getName() + "�6 zako�czy� si� sukcesem");
		p2.sendMessage(prefix + "Handel z gracze �e" + p1.getName() + "�6 zako�czy� si� sukcesem");
	}
	
	@EventHandler
	public void handel(PlayerInteractEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof Player)) return;
		Player p  = ev.getPlayer();
		if (!p.hasPermission("mimiRPG.handel")) return;
		if (!p.isSneaking()) return;
		String nick1 = p.getName();
		if (prozby.containsKey(nick1)) return;
		if (handluj�cy.contains(nick1)) return;
		Player p2 = (Player) ev.getRightClicked();
		String nick2 = p2.getName();
		if (nick1.equals(prozby.get(nick2)))
			handel(p2, p);
		else {
			prozby.put(nick1, nick2);
			p.sendMessage(prefix  + "Wys�ano propozycj� handlu do gracza �e"   + nick2);
			p2.sendMessage(prefix + "Otrzymano propozycj� handlu od gracza �e" + nick1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			    public void run() {
			    	if (nick2.equals(prozby.get(nick1)) && !handluj�cy.contains(nick1)) {
			    		prozby.remove(nick1);
			    		p.sendMessage(prefix  + "Propozycja handlu z graczem �e" + nick2 + "�6 wygas�a");
			    		p2.sendMessage(prefix + "Propozycja handlu gracza �e" + nick1 + "�6 wygas�a");
			    	}
			    }
			}, 400);
		}
	}
	@EventHandler
	public void podnoszenieItem�w(EntityPickupItemEvent ev) {
		if (handluj�cy.contains(ev.getEntity().getName()))
			ev.setCancelled(true);
	}
	@EventHandler
	public void przeci�ganie(InventoryDragEvent ev) {
		if (handluj�cy.contains(ev.getWhoClicked().getName())) {
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
		if (handluj�cy.contains(ev.getPlayer().getName()))
			anulujHandel(ev.getInventory(), (Player) ev.getPlayer());
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (!handluj�cy.contains(ev.getWhoClicked().getName())) return;
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
		String lewy = nick;
		if (!lewa)
			lewy = handluj�cy.get(handluj�cy.indexOf(nick) - 1);
		wype�nijKolumne(inv, dajG�os(lewy, false), 3);
		wype�nijKolumne(inv, dajG�os(prozby.get(lewy), false), 5);
	}
	private static void zaakceptuj(Inventory inv, String nick, boolean lewa) {
		int st = dajSt(lewa);
		boolean zaakceptowane = inv.getItem(st).getType().equals(Material.LIME_STAINED_GLASS_PANE);
		wype�nijKolumne(inv, dajG�os(nick, !zaakceptowane), st);
		if (!zaakceptowane) {
			if (st == 5) st = 3;
			else st = 5;
			if (inv.getItem(st).getType().equals(Material.LIME_STAINED_GLASS_PANE))
				udana(inv);
		}
	}
	private static int dajSt(boolean lewa) {
		if (lewa) 
			return 3; 
		return 5;
	}
	public static void wype�nijKolumne(Inventory inv, ItemStack item, int st) {
		for (int i=st; i<inv.getSize(); i+=9)
			inv.setItem(i, item);
	}
	private boolean sprawdzSlot(int slot, boolean lewa) {
		if (lewa  && slot % 9 < 3) return true;
		if (!lewa && slot % 9 > 5) return true;
		if (slot >= 4*9 || slot < 0) return true;
		return false;
	}
}
