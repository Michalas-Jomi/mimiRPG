package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

// TODO przeanalizować

@Moduł
public class Plecak extends Komenda implements Listener {
	private static ItemStack zablokowanySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&4Slot Niedostępny", Arrays.asList("Można odblokować pod komendą", "/menu"));
	
	public Plecak() {
		super("plecak");
	}

	private static boolean otwórz(Player p) {
		List<ItemStack> itemy = Gracz.wczytaj(p).plecak;
		while (itemy.size() < 3)
			itemy.add(null);
		int sloty = itemy.size();
		int do9 = sloty % 9 == 0 ? sloty : sloty / 9 * 9 + 9;
		Inventory inv = Func.createInventory(p, do9, "plecak");
		for (int i=0	; i<sloty ; i++) inv.setItem(i, itemy.get(i));
		for (int i=sloty; i<do9	  ; i++) inv.setItem(i, zablokowanySlot);
		p.openInventory(inv);
		return true;
	}
	@EventHandler
	public static void zamknij(InventoryCloseEvent ev) {
		if (!Func.getTitle(ev.getView()).equalsIgnoreCase("plecak")) return;
		Gracz gracz = Gracz.wczytaj(ev.getPlayer().getName());
		Inventory inv = ev.getInventory();
		for (int i=0; i<Math.max(gracz.plecak.size(), 3); i++)
			gracz.plecak.set(i, inv.getItem(i));
		gracz.zapisz();
	}
	@EventHandler
	public static void kliknięcie(InventoryClickEvent ev) {
		if (!Func.getTitle(ev.getView()).equalsIgnoreCase("plecak")) return;
		
		if (Func.porównaj(ev.getCurrentItem(), zablokowanySlot))
			ev.setCancelled(true);
	}
	
	public static void ulepsz(Player p, String imie) {
		Gracz gracz = Gracz.wczytaj(imie);
		int sloty = gracz.plecak.size();
		if (sloty >= 6*9) {
			p.sendMessage("Osiągnięto już maksymalny poziom plecaka");
			return;
		}
		gracz.plecak.add(null);
		gracz.zapisz();
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			otwórz((Player) sender);
		else
			sender.sendMessage("I co ja moge zrobić?");
		return true;
	}
}
