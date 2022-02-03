package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

import net.milkbowl.vault.economy.EconomyResponse;

@Moduł
public class Targ extends Komenda implements Listener, Przeładowalny {
	private static ItemStack itemBrak = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, 1, "§6§2 ", null);
	private static ItemStack itemBrakTowaru = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "§6§2 ", null);
	private static ItemStack itemOdśwież = Func.dajGłówkę("§6Odśwież", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTg4N2NjMzg4YzhkY2ZjZjFiYThhYTVjM2MxMDJkY2U5Y2Y3YjFiNjNlNzg2YjM0ZDRmMWMzNzk2ZDNlOWQ2MSJ9fX0=");
	private static ItemStack itemTowary = Func.dajGłówkę("§6Pokaż tylko własne towary", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMThlZmE1YWM4NmVkYjdhYWQyNzFmYjE4YjRmNzg3ODVkMGY0OWFhOGZjNzMzM2FlMmRiY2JmY2E4NGIwOWI5ZiJ9fX0=");
	private static ItemStack itemPoprzedniaStrona = Func.stwórzItem(Material.WRITABLE_BOOK, 1, "§6Poprzednia strona", null);
	private static ItemStack itemNastępnaStrona = Func.stwórzItem(Material.WRITABLE_BOOK, 1, "§6Następna strona",	  null);
	public static Config config = new Config("configi/targ");
	private static List<ItemStack> Itemy = Lists.newArrayList();
	private static HashMap<String, List<ItemStack>> menu = new HashMap<>();
	private static HashMap<String, Integer> strony = new HashMap<>();
	public  static String prefix = Func.prefix("Targ");
	private static List<String> gracze;
	public static int maxCena = 10_000_000;
	public Targ() {
	    super("wystaw", prefix + "/wystaw <cena> [ilość]");
		ustawKomende("targ", null, null);
		przeładuj();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		config.przeładuj();
		// wczytywanie graczy z pliku
		gracze = (List<String>) config.wczytaj("gracze");
		if (gracze == null)
			gracze = Lists.newArrayList();
		
		// wczytywanie itemów z pliku
		Itemy = Lists.newArrayList();
		for (String nick : gracze)
			Itemy.addAll((List<ItemStack>) config.wczytaj(nick));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Itemy Targu", Itemy.size());
	}
	
	private static ItemStack przetwórzItem(ItemStack item, double cena, String gracz) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Func.getLore(meta);
		if (lore == null)
			lore = Lists.newArrayList();
		lore.add("");
		lore.add("§6Cena: §e" + cena + "$");
		lore.add("§6Sprzedawca: §e" + gracz);
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	private static ItemStack odtwórzItem(ItemStack item) {
		if (item == null) return null;
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Func.getLore(meta);
		int len = lore.size();
		lore = lore.subList(0, len-3);
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	private static boolean dajMenu(Player p) {
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Ta komenda nie działa poprawnie! wpisz §e/raport §6aby dowiedzieć się więcej");
			return true;
		}
		Inventory inv = Func.createInventory(p, 6*9,"§6§lTarg");
		List<ItemStack> lista = Lists.newArrayList();
		lista.addAll(Itemy);
		lista = Lists.reverse(lista);
		menu.put(p.getName(), lista);
		
		inv.setItem(49, itemOdśwież);
		inv.setItem(46, itemTowary);
		inv.setItem(45, itemPoprzedniaStrona);
		inv.setItem(53, itemNastępnaStrona);
		for (int i=47; i<9*6-1; i++)
			if (inv.getItem(i) == null)
				inv.setItem(i, itemBrak);
		p.openInventory(inv);
		zmieńStrone(p, 0, true);
		return true;
	}
	private static void zmieńStrone(Player p, int strona, boolean pierwsze) {
		if (strona < 0) return;
		List<ItemStack> lista = menu.get(p.getName());
		int max = lista.size();
		if (!pierwsze)
			if (strona*45 > max) return;
		Inventory inv = p.getOpenInventory().getInventory(0);
		strony.put(p.getName(), strona);
		for (int i=0; i<5*9; i++)
			inv.setItem(i, itemBrakTowaru);
		for (int i=strona*45; i<(strona+1)*45; i++) {
			if (i >= max)
				break;
			inv.setItem(i % 45, lista.get(i));
		}
	}
	@SuppressWarnings({ "deprecation", "unchecked" })
	private void kup(Player p, ItemStack item) {
		if (!Itemy.contains(item)) {
			p.sendMessage(prefix + "Tego przedmiotu nie ma już na targu");
			return;
		}
		List<String> lore = item.getItemMeta().getLore();
		int len = lore.size();
		String sprzedawca = lore.get(len-1).split(" ")[1].substring(2);
		if (sprzedawca.equals(p.getName())) {
			Main.panelTakNie(p, "§4§lCzy napewno chcesz wycofać ofertę na §c" + Func.nazwaItemku(item), "§aTak", "§cNie", () -> wycofajItem(p, item), () -> {});
			return;
		}
		String s = lore.get(len-2).split(" ")[1];
		s = s.substring(2, s.length()-1);
		double cena = Func.Double(s, -1);
		if (cena > Main.econ.getBalance(p)) {
			p.sendMessage(prefix + "Nie stać cię na to");
			return;
		}
		if (p.getInventory().firstEmpty() == -1) {
			p.sendMessage(prefix + "Nie masz wolnego miejsca w ekwipunku");
			return;
		}
		
		Main.panelTakNie(p, "§4§lCzy napewno chcesz kupić §c" + Func.nazwaItemku(item), "§aTak§6, zapłacę " + cena + "$", "§cNie§6, dziękuję", () -> {
			if (!Itemy.contains(item)) {
				p.sendMessage(prefix + "Tego przedmiotu nie ma już na targu");
				return;
			}
			EconomyResponse r = Main.econ.withdrawPlayer(p, cena);
			Main.econ.depositPlayer(sprzedawca, cena);
	        if(r.transactionSuccess()) {
	        	p.sendMessage(String.format(prefix + "Kupiłeś przedmiot od gracza §e%s§6 za §e%s$§6 zostało ci §e%s$", sprzedawca, Func.DoubleToString(r.amount), Func.DoubleToString(r.balance)));
	        	Player sp = Bukkit.getPlayer(sprzedawca);
	        	if (sp != null && sp.isOnline())
	        		sp.sendMessage(String.format(prefix + "Gracz §e%s§6 kupił od ciebie przedmiot za §e%s$", p.getName(), Func.DoubleToString(r.amount)));
	        } else {
	            p.sendMessage(String.format(prefix + "Wystąpił problem: §c%s", r.errorMessage));
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
			p.getInventory().addItem(odtwórzItem(item));
			odświeżOferte(p);
		}, () -> {});
	}
	@SuppressWarnings({ "deprecation", "unchecked" })
	private static void wystawItem(Player p, double cena) {
		ItemStack item = p.getItemInHand();
		String nick = p.getName();
		if (item == null || item.getType().equals(Material.AIR))
			{p.sendMessage(prefix + "Musisz trzymać coś w ręce aby tego użyć"); return;}
		if (cena < 1)
			{p.sendMessage(prefix + "Nie możesz sprzedać nic za mniej niż §e1$"); return;}
		if (cena > maxCena)
			{p.sendMessage(prefix + "Nie możesz sprzedać nic za więcej niż §e" + maxCena + "$"); return;}
		List<ItemStack> oferty = (List<ItemStack>) config.wczytaj(nick);
		if (oferty == null)
			oferty = Lists.newArrayList();
		if (oferty.size() >= limitOfert(p))
			{p.sendMessage(prefix + "Osiągnięto już limit ofert"); return;}
		if (!gracze.contains(nick)) {
			gracze.add(nick);
			config.ustaw_zapisz("gracze", gracze);
		}
		item = przetwórzItem(item, cena, nick);
		Itemy.add(item);
		oferty.add(item);
		config.ustaw_zapisz(nick, oferty);
		p.setItemInHand(new ItemStack(Material.AIR));
		p.sendMessage(prefix + "Wystawiono item za §e" + Func.DoubleToString(cena) + "$");
	}
	private static final Pattern limitOfertPattern = Pattern.compile("mimirpg\\.targ\\.limit\\.(\\d+)");
	public static int limitOfert(Player p) {
		AtomicInteger ai = new AtomicInteger(0);
		p.getEffectivePermissions().forEach(perm -> {
			if (perm.getValue()) {
				Matcher matcher = limitOfertPattern.matcher(perm.getPermission());
				if (matcher.matches())
					ai.set(Math.max(ai.get(), Func.Int(matcher.group(1))));
			}
		});
		return ai.get();
	}
	@SuppressWarnings("unchecked")
	private void wycofajItem(Player p, ItemStack item) {
		if (p.getInventory().firstEmpty() == -1)
			{p.sendMessage(prefix + "Twój ekwipunek jest pełny"); return;}
		String nick = p.getName();
		List<ItemStack> oferty = (List<ItemStack>) config.wczytaj(nick);
		if (!oferty.remove(item))
			{p.sendMessage(prefix + "Tej oferty nie ma już na rynku"); return;}; 
		if (oferty.size() == 0) {
			gracze.remove(nick);
			config.ustaw("gracze", gracze);
			config.ustaw_zapisz(nick, null);
		} else
			config.ustaw_zapisz(nick, oferty);
		Itemy.remove(item);
		p.sendMessage(prefix + "Wycofano item");
		p.getInventory().addItem(odtwórzItem(item));
		if (Func.getTitle(p.getOpenInventory()).equals("§6§lTwoje oferty"))
			pokażSwojeOferty(p);
		else
			odświeżOferte(p);
	}
	private void odświeżOferte(Player p) {
		dajMenu(p);
	}
	@SuppressWarnings("unchecked")
	private void pokażSwojeOferty(Player p) {
		Inventory inv = Func.createInventory(p, 18, "§6§lTwoje oferty");
		ItemStack nic = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "§aKliknij item aby go wycofać", null);
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
		inv.setItem(17, Func.dajGłówkę("§6Powrót do targu", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0="));
		p.openInventory(inv);
	}
	
	@EventHandler
	public void kliknięcie(InventoryClickEvent ev) {
		Player p = (Player) ev.getWhoClicked();
		ItemStack item = ev.getCurrentItem();
		int slot = ev.getRawSlot();
		switch (Func.getTitle(ev.getView())) {
		case "§6§lTarg":
			if (slot >= 6*9 || slot < 0) return;
			ev.setCancelled(true);
			
			String nazwa = Func.nazwaItemku(item);
			if (item.isSimilar(itemBrakTowaru)) return;
			if (slot < 5*9) {kup(p, item); return;}
			switch(nazwa) {
			case "§6Poprzednia strona":
				zmieńStrone(p, strony.get(p.getName())-1, false);
				break;
			case "§6Następna strona":
				zmieńStrone(p, strony.get(p.getName())+1, false);
				break;
			case "§6Odśwież":
				odświeżOferte(p);
				break;
			case "§6Pokaż tylko własne towary":
				pokażSwojeOferty(p);
				break;
			}
			return;
		case "§6§lTwoje oferty":
			if (slot >= 18 || slot < 0) return;
			ev.setCancelled(true);
			if (slot == 17) 
				dajMenu(p);
			else if (!Func.nazwaItemku(item).equals("§aKliknij item aby go wycofać"))
				wycofajItem(p, item);
			return;
		}
		
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Lists.newArrayList();
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Targ to nie miejsce dla ciebie");
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("targ"))
			return dajMenu(p);
		
		if (args.length < 1) return false;
		String cena = args[0];
		
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Ta komenda nie działa poprawnie! wpisz §e/raport §6aby dowiedzieć się więcej");
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
