package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;

@Moduł
public class Patrzeq extends Komenda implements Listener {
	public static String prefix = Func.prefix("Podgląd Ekwipunku");
	
	private ItemStack brakSlotu = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&2Brak slotu", Arrays.asList("§aPo lewej masz po koleji", "§6Buty §eSpodnie §6Klate §eHełm §6Lewą rękę"));
	private HashMap<String, Inventory> mapa = new HashMap<>();
	
	public Patrzeq() {
		super("patrzeq", "/patrzeq <gracz>");
		Main.dodajPermisje("patrzeq.modyfikuj", "patrzeq.nietykalność");
	}

	int wstrzymane = 0;
	boolean przepisz(Inventory co, Inventory gdzie) {
		return przepisz(co, gdzie, false);
	}
	boolean przepisz(Inventory co, Inventory gdzie, boolean ignorujWstrzymanie) {
		if (ignorujWstrzymanie)
		if (ignorujWstrzymanie) wstrzymane++;
		if (co == null || gdzie == null) return false;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
            public void run() {
        		if (wstrzymane > 0 && !ignorujWstrzymanie) return;
            	for (int i=0; i<4*9+5; i++)
        			gdzie.setItem(i, co.getItem(i));
            	if (ignorujWstrzymanie)
            		wstrzymane--;
            }
        }, 1);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0) return false;
		if (!(sender instanceof Player)) return Func.powiadom(sender, "Musisz być graczem żeby tego użyć");
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) return Func.powiadom(sender, prefix + "Niepoprawna nazwa gracza: §e" + args[0]);
		if (!mapa.containsKey(p.getName())) {
			Inventory inv = Bukkit.createInventory(p, 5*9, "§1§lPodgląd gracza §4§l" + p.getName());
			for (int i=4*9+5; i<5*9; i++)
				inv.setItem(i, brakSlotu);
			mapa.put(p.getName(), inv);
			odświeżCałe(p);
		}
		((Player) sender).openInventory(mapa.get(p.getName()));
		return true;
	}
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		int slot = ev.getRawSlot();
		if (slot >= 5*9 || slot < 0) return;
		InventoryHolder _holder = ev.getInventory().getHolder();
		if (!(_holder instanceof Player)) return;
		Player holder = (Player) _holder;
		if (!(ev.getView().getTitle().equals("§1§lPodgląd gracza §4§l" + holder.getName()))) return;
		if (holder.hasPermission("patrzeq.nietykalność") ||
				!ev.getWhoClicked().hasPermission("patrzeq.modyfikuj") ||
				(slot >= 4*9+5 && slot < 5*9))
			ev.setCancelled(true);
		else
			przepisz(ev.getInventory(), holder.getInventory(), true);
	}
	@EventHandler
	public void przeciąganie(InventoryDragEvent ev) {
		InventoryHolder _holder = ev.getInventory().getHolder();
		if (!(_holder instanceof Player)) return;
		Player holder = (Player) _holder;
		if (!(ev.getView().getTitle().equals("§1§lPodgląd gracza §4§l" + holder.getName()))) return;
		if (holder.hasPermission("patrzeq.nietykalność") ||
				!ev.getWhoClicked().hasPermission("patrzeq.modyfikuj"))
			for (int slot : ev.getRawSlots())
				if (slot >= 0 && slot < 5*9) {
					ev.setCancelled(true);
					return;
				}
		przepisz(ev.getInventory(), holder.getInventory(), true);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		InventoryHolder _holder = ev.getInventory().getHolder();
		if (!(_holder instanceof Player)) return;
		Player holder = (Player) _holder;
		if (!(ev.getView().getTitle().equals("§1§lPodgląd gracza §4§l" + holder.getName()))) return;
		if (ev.getInventory().getViewers().size() <= 1)
			mapa.remove(holder.getName());
	}

	void odświeżCałe(Player p) {
		if (!przepisz(p.getInventory(), mapa.get(p.getName()))) return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
            public void run() {
            	odświeżCałe(p);
            }
        }, 1);
	}
	
	
}
