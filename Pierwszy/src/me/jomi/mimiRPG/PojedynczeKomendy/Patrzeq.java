package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Patrzeq extends Komenda implements Listener{
	public static String prefix = Func.prefix("Podgl�d Ekwipunku");
	
	private static HashMap<String, String> mapa = new HashMap<>();
	private static ItemStack brakSlotu = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&2Brak slotu", Arrays.asList("�aPo lewej masz po koleji", "�6Buty �eSpodnie �6Klate �eHe�m �6Lew� r�k�"));

	public Patrzeq() {
	    super("patrzeq", prefix + "/patrzeq <gracz>");
	    Main.dodajPermisje("patrzeq.modyfikuj");
	}
	// Od�wie�a podanemu obserwatorowi
	private static void od�wie�(String nick) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
				Player p = Bukkit.getPlayer(nick);
				Inventory inv = p.getOpenInventory().getInventory(0);
				PlayerInventory Pinv = Bukkit.getPlayer(mapa.get(nick)).getInventory();
				for (int i=0; i<4*9+5; i++)
					inv.setItem(i, Pinv.getItem(i));
				for (int i=41; i<5*9; i++)
					inv.setItem(i, brakSlotu);
		    }
		}, 1);
	}
	// Od�wie�a wszystkim obserwatorom podanego gracza
	private boolean od�wie�Wszystkim(String imie) {
		if (mapa.containsKey(imie)) {
			if (Bukkit.getPlayer(mapa.get(imie)).hasPermission("mimiRPG.patrzeq"))
				return true;
			od�wie�Gracza(mapa.get(imie), imie);
			return false;
		}
		if (mapa.containsValue(imie))
			for (String nick: mapa.keySet())
				if (mapa.get(nick).equals(imie))
					od�wie�(nick);
		return false;
	}
	// Od�wie�a ekwipunek gracza wzgl�dem obserwatora
	private void od�wie�Gracza(String gracz, String obserwator) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	PlayerInventory Pinv = Bukkit.getPlayer(gracz).getInventory();
				Inventory inv = Bukkit.getPlayer(obserwator).getOpenInventory().getInventory(0);
				for (int i=0; i<4*9+5; i++) 
					Pinv.setItem(i, inv.getItem(i));
				od�wie�Wszystkim(gracz);
		    }
		}, 1);
	}
	
	
	// Przetwarza nr slotu z otwartego eq na skrzynie
	private static int przetw�rz(int slot, boolean przewarza�) {
		if (!przewarza�) return slot;
		if (slot == 45)
			return 40;
		if (slot < 9) 
			switch(slot) {
			case 5: return 39;
			case 6: return 38;
			case 7: return 37;
			case 8: return 36;
			default: return -1;
			}
		if (slot >= 36)
			return slot - 36;
		return slot;
	}
	// od�wie�a slot w ekwipunku
	private void od�wie�Slot(int slot, Inventory Pinv, Inventory inv, boolean przewarza�) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	int i = przetw�rz(slot, przewarza�);
		    	if (i == -1 || i >= 4*9+5) return;
		    	Pinv.setItem(i, inv.getItem(i));
		    }
		}, 1);
	}
	// od�wie�a slot graczowi
	private boolean od�wie�SlotGracz(String nick, int slot, Inventory inv) {
		if (!mapa.containsKey(nick)) return false;
		if (Bukkit.getPlayer(mapa.get(nick)).hasPermission("mimiRPG.patrzeq.modyfikuj")) return true;
		od�wie�Slot(slot, Bukkit.getPlayer(mapa.get(nick)).getInventory(), inv, false);
		return false;
	}
	// od�wie�a slot obserwatorom gracza
	private boolean od�wie�SlotObserwator(String nick, int slot) {
		if (!mapa.containsValue(nick)) return false;
		for (String obserwator : mapa.keySet())
			if (mapa.get(obserwator).equals(nick))
				od�wie�Slot(slot, Bukkit.getPlayer(obserwator).getOpenInventory().getInventory(0), Bukkit.getPlayer(nick).getInventory(), true);
		return false;
	}
	
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev){
		String nick = ev.getPlayer().getName();
		if (mapa.containsKey(nick))
			mapa.remove(nick);
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		String nick = ev.getWhoClicked().getName();
		int slot = ev.getRawSlot();
		if (mapa.containsKey(nick) && !Bukkit.getPlayer(nick).hasPermission("mimiRPG.patrzeq.modyfikuj")) 
			{ev.setCancelled(true); return;}
		if (slot < 0 || !(mapa.containsKey(nick) || mapa.containsValue(nick))) return;
		if ((mapa.containsKey(nick) && slot > 40 && slot < 45)) 
			{ev.setCancelled(true); return;}
		List<ClickType> typy = Arrays.asList(ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT, ClickType.NUMBER_KEY);
		if (typy.contains(ev.getClick())) {
			if (od�wie�Wszystkim(nick))
				ev.setCancelled(true);
			return;
		}
		if (od�wie�SlotGracz(nick, slot, ev.getInventory()))
			{ev.setCancelled(true); return;}
	if (od�wie�SlotObserwator(nick, slot))
		{ev.setCancelled(true); return;}
	}
	@EventHandler
	public void przeciaganie(InventoryDragEvent ev) {
		String nick = ev.getWhoClicked().getName();
		if (mapa.containsKey(nick) && !Bukkit.getPlayer(nick).hasPermission("mimiRPG.patrzeq.modyfikuj")) 
			{ev.setCancelled(true); return;}
		if (min(ev.getRawSlots()) >= 45)
			return;
		for (int slot : ev.getRawSlots()) {
			if (slot >= 45 || slot < 0) continue;
			if (od�wie�SlotGracz(nick, slot, ev.getInventory())){
				ev.setCancelled(true);
				return;
			}
			if (od�wie�SlotObserwator(nick, slot)) {
				ev.setCancelled(true);
				return;
			}
		}
	}
	@EventHandler
	public void upuszczanie(PlayerDropItemEvent ev) {
		od�wie�Wszystkim(ev.getPlayer().getName());
	}
	@EventHandler
	public void umieranie(PlayerDeathEvent ev) {
		od�wie�Wszystkim(ev.getEntity().getName());
	}
	@EventHandler
	public void podnoszenie(EntityPickupItemEvent ev) {
		if (ev.getEntity() instanceof Player)
			od�wie�Wszystkim(ev.getEntity().getName());
	}
	
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		String nick = ev.getPlayer().getName();
		if (mapa.containsValue(nick)) {
			for (Object it : mapa.keySet().toArray())
				if (mapa.get(it).equals(nick)) {
					Player p = Bukkit.getPlayer((String) it);
					p.sendMessage(prefix + "Gracz �e" + nick + "�6 w�a�nie wszed� w stan offline");
					p.closeInventory();
					mapa.remove(p.getName());
				}
		}
	}
	public static int min(Set<Integer> s) {
		Iterator<Integer> it = s.iterator();
		int m = it.next();
		while (it.hasNext())
			m = Math.min(m, it.next());
		return m;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Gracz patrzy gracza, nie inaczej");
		Player obserwator = (Player) sender;
		if (args.length < 1) return false;
		String nick = args[0];
			
		Player p = Bukkit.getPlayer(nick);
		if (p == null) {
			obserwator.sendMessage(prefix + "Niepoprawny nick gracza");
			return true;
		}
		if (!Bukkit.getOnlinePlayers().contains(p)) {
			obserwator.sendMessage(prefix + "Gracz �e" + p.getName() + "�6 nie jest online");
			return true;
		}
		if (p.hasPermission("mimiRPG.patrzeq.modyfikuj")) {
			obserwator.sendMessage(prefix + "Nie mo�esz podgl�da� gracza �e" + p.getName());
			return true;
		}
		Inventory inv = Bukkit.createInventory(obserwator, 5*9, "Podl�d Ekwipunku");
		obserwator.openInventory(inv);
		
		mapa.put(obserwator.getName(), nick);
		od�wie�(obserwator.getName());
		return true;
	}
}
