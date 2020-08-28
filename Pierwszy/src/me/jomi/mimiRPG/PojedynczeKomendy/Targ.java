package me.jomi.mimiRPG.PojedynczeKomendy;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze�adowalny;
import net.milkbowl.vault.economy.EconomyResponse;

public class Targ extends Komenda implements Listener, Prze�adowalny{
	public static Config config = new Config("configi/targ");
	private static List<ItemStack> Itemy = Lists.newArrayList();
	private static HashMap<String, List<ItemStack>> menu = new HashMap<>();
	private static HashMap<String, Integer> strony = new HashMap<>();
	public  static String prefix = Func.prefix("Targ");
	private static List<String> gracze;
	public static int limitOfert = 5;
	public static int maxCena = 10_000_000;
	public Targ() {
	    super("wystaw", prefix + "/wystaw <cena> [ilo��]");
		ustawKomende("targ", null, null);
		prze�aduj();
	}
	@SuppressWarnings("unchecked")
	public void prze�aduj() {
		config.prze�aduj();
		// wczytywanie graczy z pliku
		gracze = (List<String>) config.wczytaj("gracze");
		if (gracze == null)
			gracze = Lists.newArrayList();
		
		// wczytywanie item�w z pliku
		Itemy = Lists.newArrayList();
		for (String nick : gracze)
			Itemy.addAll((List<ItemStack>) config.wczytaj(nick));
	}
	public String raport() {
		return "�6Itemy Targu: �e" + Itemy.size();
	}
	private static ItemStack przetw�rzItem(ItemStack item, double cena, String gracz) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = Lists.newArrayList();
		lore.add("");
		lore.add("�6Cena: �e" + cena + "$");
		lore.add("�6Sprzedawca: �e" + gracz);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	private static ItemStack odtw�rzItem(ItemStack item) {
		if (item == null) return null;
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		int len = lore.size();
		lore = lore.subList(0, len-3);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	private static boolean dajMenu(Player p) {
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Ta komenda nie dzia�a poprawnie! wpisz �e/raport �6aby dowiedzie� si� wi�cej");
			return true;
		}
		Inventory inv = Bukkit.createInventory(p, 6*9,"�6�lTarg");
		List<ItemStack> lista = Lists.newArrayList();
		lista.addAll(Itemy);
		menu.put(p.getName(), lista);
		
		ItemStack brak = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "�6�2 ", null);
		inv.setItem(49, Func.dajG��wk�("�6Od�wie�", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTg4N2NjMzg4YzhkY2ZjZjFiYThhYTVjM2MxMDJkY2U5Y2Y3YjFiNjNlNzg2YjM0ZDRmMWMzNzk2ZDNlOWQ2MSJ9fX0=", null));
		inv.setItem(46, Func.dajG��wk�("�6Poka� tylko w�asne towary", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMThlZmE1YWM4NmVkYjdhYWQyNzFmYjE4YjRmNzg3ODVkMGY0OWFhOGZjNzMzM2FlMmRiY2JmY2E4NGIwOWI5ZiJ9fX0=", null));
		inv.setItem(45, Func.stw�rzItem(Material.WRITABLE_BOOK, 1, "�6Poprzednia strona", null));
		inv.setItem(53, Func.stw�rzItem(Material.WRITABLE_BOOK, 1, "�6Nast�pna strona",	  null));
		for (int i=47; i<9*6-1; i++)
			if (inv.getItem(i) == null)
				inv.setItem(i, brak);
		p.openInventory(inv);
		zmie�Strone(p, 0, true);
		return true;
	}
	private static void zmie�Strone(Player p, int strona, boolean pierwsze) {
		if (strona < 0) return;
		List<ItemStack> lista = menu.get(p.getName());
		int max = lista.size();
		if (!pierwsze)
			if (strona*45 > max) return;
		Inventory inv = p.getOpenInventory().getInventory(0);
		ItemStack brakTowaru = Func.stw�rzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "�6�2 ", null);
		strony.put(p.getName(), strona);
		for (int i=0; i<5*9; i++)
			inv.setItem(i, brakTowaru);
		for (int i=strona*45; i<(strona+1)*45; i++) {
			if (i >= max)
				break;
			inv.setItem(i % 45, lista.get(i));
		}
	}
	@SuppressWarnings({ "deprecation", "unchecked" })
	private void kup(Player p, ItemStack item) {
		if (!Itemy.contains(item)) {
			p.sendMessage(prefix + "Tego przedmiotu nie ma ju� na targu");
			return;
		}
		List<String> lore = item.getItemMeta().getLore();
		int len = lore.size();
		String sprzedawca = lore.get(len-1).split(" ")[1].substring(2);
		if (sprzedawca.equals(p.getName())) {
			wycofajItem(p, item);
			return;
		}
		String s = lore.get(len-2).split(" ")[1];
		s = s.substring(2, s.length()-1);
		double cena = Func.Double(s, -1);
		if (cena > Main.econ.getBalance(p)) {
			p.sendMessage(prefix + "Nie sta� ci� na to");
			return;
		}
		if (p.getInventory().firstEmpty() == -1) {
			p.sendMessage(prefix + "Nie masz wolnego miejsca w ekwipunku");
			return;
		}
		EconomyResponse r = Main.econ.withdrawPlayer(p, cena);
		Main.econ.depositPlayer(sprzedawca, cena);
        if(r.transactionSuccess()) {
        	p.sendMessage(String.format(prefix + "Kupi�e� przedmiot od gracza �e%s�6 za �e%s$�6 zosta�o ci �e%s$", sprzedawca, Func.DoubleToString(r.amount), Func.DoubleToString(r.balance)));
        	Player sp = Bukkit.getPlayer(sprzedawca);
        	if (sp != null && sp.isOnline())
        		sp.sendMessage(String.format(prefix + "Gracz �e%s�6 kupi� od ciebie przedmiot za �e%s$", p.getName(), Func.DoubleToString(r.amount)));
        } else {
            p.sendMessage(String.format(prefix + "Wyst�pi� problem: �c%s", r.errorMessage));
            return;
        }
        Itemy.remove(item);
        List<ItemStack> of = (List<ItemStack>) config.wczytaj(sprzedawca);
        of.remove(item);
		if (of.size() == 0) {
			gracze.remove(sprzedawca);
			config.ustaw("gracze", gracze);
			config.ustaw_zapisz(sprzedawca, null);
		} else
			config.ustaw_zapisz(sprzedawca, of);
		p.getInventory().addItem(odtw�rzItem(item));
		od�wie�Oferte(p);
	}
	@SuppressWarnings({ "deprecation", "unchecked" })
	private static void wystawItem(Player p, double cena) {
		ItemStack item = p.getItemInHand();
		String nick = p.getName();
		if (item == null || item.getType().equals(Material.AIR))
			{p.sendMessage(prefix + "Musisz trzyma� co� w r�ce aby tego u�y�"); return;}
		if (cena < 1)
			{p.sendMessage(prefix + "Nie mo�esz sprzeda� nic za mniej ni� �e1$"); return;}
		if (cena > maxCena)
			{p.sendMessage(prefix + "Nie mo�esz sprzeda� nic za wi�cej ni� �e" + maxCena + "$"); return;}
		List<ItemStack> oferty = (List<ItemStack>) config.wczytaj(nick);
		if (oferty == null)
			oferty = Lists.newArrayList();
		if (oferty.size() >= limitOfert)
			{p.sendMessage(prefix + "Osi�gni�to ju� limit ofert"); return;}
		if (!gracze.contains(nick)) {
			gracze.add(nick);
			config.ustaw_zapisz("gracze", gracze);
		}
		item = przetw�rzItem(item, cena, nick);
		Itemy.add(item);
		oferty.add(item);
		config.ustaw_zapisz(nick, oferty);
		p.setItemInHand(new ItemStack(Material.AIR));
		p.sendMessage(prefix + "Wystawiono item za �e" + Func.DoubleToString(cena) + "$");
	}
	@SuppressWarnings("unchecked")
	private void wycofajItem(Player p, ItemStack item) {
		if (p.getInventory().firstEmpty() == -1)
			{p.sendMessage(prefix + "Tw�j ekwipunek jest pe�ny"); return;}
		String nick = p.getName();
		List<ItemStack> oferty = (List<ItemStack>) config.wczytaj(nick);
		oferty.remove(item);
		if (oferty.size() == 0) {
			gracze.remove(nick);
			config.ustaw("gracze", gracze);
			config.ustaw_zapisz(nick, null);
		} else
			config.ustaw_zapisz(nick, oferty);
		Itemy.remove(item);
		p.sendMessage(prefix + "Wycofano item");
		p.getInventory().addItem(odtw�rzItem(item));
		if (p.getOpenInventory().getTitle().equals("�6�lTwoje oferty"))
			poka�SwojeOferty(p);
		else
			od�wie�Oferte(p);
	}
	private void od�wie�Oferte(Player p) {
		dajMenu(p);
	}
	@SuppressWarnings("unchecked")
	private void poka�SwojeOferty(Player p) {
		Inventory inv = Bukkit.createInventory(p, 18, "�6�lTwoje oferty");
		ItemStack nic = Func.stw�rzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "�aKliknij item aby go wycofa�", null);
		for (int i=0; i<17; i++)
			inv.setItem(i, nic);
		int i = 0;
		List<ItemStack> lista =  (List<ItemStack>) config.wczytaj(p.getName());
		if (lista != null) {
			for (ItemStack item : lista) {
				inv.setItem(i, item);
				i++;
			}
		}
		inv.setItem(17, Func.dajG��wk�("�6Powr�t do targu", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0=", null));
		p.openInventory(inv);
	}
	
	@EventHandler
	public void klikni�cie(InventoryClickEvent ev) {
		Player p = (Player) ev.getWhoClicked();
		ItemStack item = ev.getCurrentItem();
		int slot = ev.getRawSlot();
		switch (ev.getView().getTitle()) {
		case "�6�lTarg":
			if (slot >= 6*9 || slot < 0) return;
			ev.setCancelled(true);
			String nazwa = item.getItemMeta().getDisplayName();
			if (nazwa.equals("�6�2 ")) return;
			if (slot < 5*9) {kup(p, item); return;}
			switch(nazwa) {
			case "�6Poprzednia strona":
				zmie�Strone(p, strony.get(p.getName())-1, false);
				break;
			case "�6Nast�pna strona":
				zmie�Strone(p, strony.get(p.getName())+1, false);
				break;
			case "�6Od�wie�":
				od�wie�Oferte(p);
				break;
			case "�6Poka� tylko w�asne towary":
				poka�SwojeOferty(p);
				break;
			}
			return;
		case "�6�lTwoje oferty":
			if (slot >= 18 || slot < 0) return;
			ev.setCancelled(true);
			if (slot == 17) 
				dajMenu(p);
			else if (!item.getItemMeta().getDisplayName().equals("�aKliknij item aby go wycofa�"))
				wycofajItem(p, item);
			return;
		}
		
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Lists.newArrayList();
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Targ to nie miejsce dla ciebie");
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("targ"))
			return dajMenu(p);
		
		if (args.length < 1) return false;
		String cena = args[0];
		
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Ta komenda nie dzia�a poprawnie! wpisz �e/raport �6aby dowiedzie� si� wi�cej");
			return true;
		}
		double koszt = Func.Double(cena, -1);
		if (koszt == -1) {
			p.sendMessage(prefix + "Niepoprawna liczba: " + cena);
			return true;
		}
		wystawItem(p, koszt);
		return true;
	}
}
