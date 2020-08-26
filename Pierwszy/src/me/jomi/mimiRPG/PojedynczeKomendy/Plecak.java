package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Prze³adowalny;

public class Plecak extends Komenda implements Listener, Prze³adowalny{
	private static ItemStack zablokowanySlot = wezZablokowanySlot();
	public static Config config = new Config("configi/plecaki");
	
	public Plecak() {
		super("plecak");
	}
	public void prze³aduj() {
		config.prze³aduj();
	}
	public String raport() {
		return "§6Plecaki: §e" + config.klucze(false).size();
	}
	
	public static void ustawieniaDomyœlne(Player p) {
		String imie = p.getName();
		if (config.wczytaj(imie + ".sloty") == null)
			config.ustaw_zapisz(imie + ".sloty", 2); 
		if (config.wczytaj(imie + ".itemy") == null) {
			config.ustaw_zapisz(imie + ".itemy", config.wczytaj("BrakItemowSlotmimiRPGSLot"));
		}
		
	}

	private static boolean otwórz(Player p) {
		ustawieniaDomyœlne(p);
		p.openInventory(wczytaj_eq(p.getName()));
		return true;
	}
	@EventHandler
	public static void zamknij(InventoryCloseEvent ev) {
		if (!ev.getView().getTitle().equalsIgnoreCase("plecak")) return;
		Player p = (Player) ev.getPlayer();
		zapisz_eq(ev.getInventory(), p.getName());
	}

	@EventHandler
	public static void klikniêcie(InventoryClickEvent ev) {
		if (!ev.getView().getTitle().equalsIgnoreCase("plecak")) return;
		
		Player p = (Player) ev.getWhoClicked();
		int sloty = (int) config.wczytaj(p.getName() + ".sloty");
		
		if (ev.getRawSlot() >= sloty && ev.getRawSlot() < do9(sloty))
			ev.setCancelled(true);
		return;
	}
	
	public static void ulepsz(Player p, String imie) {
		int sloty = (int) config.wczytaj(imie + ".sloty");
		if (sloty >= 6*9) 
			{p.sendMessage("Osi¹gniêto ju¿ maksymalny poziom plecaka"); return;}
		config.ustaw_zapisz(imie + ".sloty", sloty+1);
	}
	
	public static int wczytaj_sloty(Player p) {
		return (int) config.wczytaj(p.getName() + ".sloty");
	}
	private static Inventory wczytaj_eq(String imie) {
		int sloty = (int) config.wczytaj(imie + ".sloty");
		Inventory inv = Bukkit.createInventory(Bukkit.getPlayer(imie), do9(sloty), "plecak");
		for (int i=0; i<sloty; i++)
			inv.setItem(i, config.wczytajItem(imie + ".itemy." + i));
		for (int i=sloty; i<do9(sloty); i++)
			inv.setItem(i, zablokowanySlot);
		return inv;
	}
	private static void zapisz_eq(Inventory inv, String imie) {
		int sloty = (int) config.wczytaj(imie + ".sloty");
		for (int i=0; i<sloty; i++) 
			config.ustaw(imie + ".itemy." + i, inv.getItem(i));
		config.zapisz();
	}
	private static int do9(int x) {
		if (x % 9 == 0) return x;
		return ((int)(x / 9))*9+9;
	}
	private static ItemStack wezZablokowanySlot() {
		ItemStack item = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&4Slot Niedostêpny", Arrays.asList("Mo¿na odblokowaæ pod komend¹", "/menu"));
		return item;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			otwórz((Player) sender);
		else
			sender.sendMessage("I co ja moge zrobiæ?");
		return true;
	}
	
}
