package me.jomi.mimiRPG.PojedynczeKomendy;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Chat.Raport;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class KalendarzAdwentowy extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Kalendarz Adwentowy");
	public KalendarzAdwentowy() {
		super("kalendarzAdwentowy", null, "ka", "kadw");
		ustawKomende("edytujKalendarzAdwentowy", null, null);
	}
	final String nazwaConfigu = "configi/KalendarzAdwentowy";
	private final ItemStack doOdebrania = Func.stwórzItem(Material.YELLOW_STAINED_GLASS_PANE, "&a&lOdbierz");
	private final ItemStack odebrany 	= Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, "&a&lOdebrane");
	private final ItemStack niedostępny = Func.stwórzItem(Material.RED_STAINED_GLASS_PANE, "", "&cdziś nie jest odpowiedni dzień na ten item");
	private final ItemStack pusty		= Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "&1&l ");
	
	
	List<ItemStack> itemy;
	

	// Override

	@Override
	public void przeładuj() {
		itemy = new Config(nazwaConfigu).wczytajItemy("itemy");
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Itemy Adwentowe", itemy.size());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko graczo może tego użyć");
		if (cmd.getName().equals("edytujKalendarzAdwentowy"))
			edytuj((Player) sender);
		else
			odbierz((Player) sender);
		return true;
	}
	
	
	
	// Odbieranie
	
	Set<String> odbierający = Sets.newConcurrentHashSet();
	void odbierz(Player p) {
		Inventory inv = Bukkit.createInventory(null, ((itemy.size() - 1) / 9 + 1) * 9, Func.koloruj("&4&lKalendarz Adwentowy"));
		Gracz g = Gracz.wczytaj(p);
		int i = -1;
		int dziś = ZonedDateTime.now().getDayOfMonth();
		for (int j=0; j < itemy.size(); j++)
			if (g.DzienneNagrodyodebrane.contains(++i)) inv.setItem(i, odebrany);
			else if (i == (dziś - 1)) 					inv.setItem(i, doOdebrania);
			else 										inv.setItem(i, Func.nazwij(niedostępny.clone(), "&a&lDzień &c&l" + (i+1)));
		while (++i < inv.getSize() && i >= 0)
			inv.setItem(i, pusty);
		p.openInventory(inv);
		odbierający.add(p.getName());
	}
	@EventHandler
	public void klikaniEq(InventoryClickEvent ev) {
		if (odbierający.contains(ev.getWhoClicked().getName())) {
			int slot = ev.getRawSlot();
			if (slot >= ev.getInventory().getSize() || slot < 0)
				return;
			ev.setCancelled(true);
			Gracz g = Gracz.wczytaj(ev.getWhoClicked().getName());
			if (slot + 1 == ZonedDateTime.now().getDayOfMonth() &&
					!g.DzienneNagrodyodebrane.contains(slot)) {
				Func.dajItem((Player) ev.getWhoClicked(), itemy.get(g.DzienneNagrodyodebrane.size()));
				ev.getWhoClicked().sendMessage(prefix + "Otrzymałeś dzisiejszą nagrodę");
				g.DzienneNagrodyodebrane.add(slot);
				g.zapisz();
				odbierz((Player) ev.getWhoClicked());
			}
		}
	}
	@EventHandler
	public void zamykanie(InventoryCloseEvent ev) {
		odbierający.remove(ev.getPlayer().getName());
	}
	
	
	
	
	// Edycja

	Set<String> edytujący = Sets.newConcurrentHashSet();
	void edytuj(Player p) {
		Inventory inv = Bukkit.createInventory(null, 6*9, Func.koloruj("&4&lEdytor Kalendarza Adventowego"));
		int i=0;
		for (ItemStack item : itemy)
			inv.setItem(i++, item);
		p.openInventory(inv);
		edytujący.add(p.getName());
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (edytujący.remove(ev.getPlayer().getName())) {
			List<ItemStack> lista = Lists.newArrayList();
			for (ItemStack item : ev.getInventory())
				Func.wykonajDlaNieNull(item, lista::add);
			new Config(nazwaConfigu).ustaw_zapisz("itemy", lista);
			przeładuj();
			ev.getPlayer().sendMessage(Raport.raport(this));
		}
	}
}
