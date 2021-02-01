package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.ItemCreator;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Hitman extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Hitman");
	public static class Oferta extends Mapowany {
		@Mapowane List<ItemStack> itemy = Lists.newArrayList();
		@Mapowane String zleceniodawca;
		@Mapowane String zleceniodawcaDisplayName;
	}
	
	Config config = new Config("configi/Hitman");
	
	final static String permNietykalność = Func.permisja("hitman.nietykalność");
	public Hitman() {
		super("hitman");
		ustawKomende("zleć", "/zleć <nick>", null);
		Main.dodajPermisje(permNietykalność);
	}


	// Zlecenie / zabijanie
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void zabicie(PlayerDeathEvent ev) {
		Player zabity = ev.getEntity();
		Func.wykonajDlaNieNull((Oferta) config.wczytaj(zabity.getName()), oferta ->
			Func.wykonajDlaNieNull(zabity.getKiller(), p -> {
				Player z = Bukkit.getPlayer(oferta.zleceniodawca);
				
				Bukkit.broadcastMessage(prefix + Func.msg("%s zabił gracza %s i otrzymał należną mu nagrodę od %s!",
						p.getDisplayName(), zabity.getDisplayName(), z == null ? oferta.zleceniodawcaDisplayName : z.getDisplayName()));
				
				config.ustaw_zapisz(zabity.getName(), null);
				
				for (ItemStack item : oferta.itemy)
					Func.dajItem(p, item);
				
				for (int i=0; i<menu.getSize(); i++)
					if (menu.getItem(i).getItemMeta().getDisplayName().substring(2).equals(zabity.getName())) {
						if (i == menu.getSize()-1)
							menu.setItem(i, Baza.pustySlot);
						else {
							int ost = i;
							try {
								while (menu.getItem(++ost + 1).getType().equals(Material.PLAYER_HEAD));
							} catch (ArrayIndexOutOfBoundsException e) {
								ost = menu.getSize() - 1;
							}
							menu.setItem(i, menu.getItem(ost));
							menu.setItem(ost, Baza.pustySlot);
						}
						break;
					}
			})
		);
	}	
	public void zleć(Player kto, Player kogo, List<ItemStack> itemy) {
		Oferta of = new Oferta();
		of.zleceniodawca = kto.getName();
		of.zleceniodawcaDisplayName = kto.getDisplayName();
		of.itemy = itemy;
		config.ustaw_zapisz(kogo.getName(), of);
		
		Gracz g = Gracz.wczytaj(kto);
		g.HitmanOstatnieZgłoszenie = (int) (System.currentTimeMillis() / 1000);
		g.zapisz();
		
		dodajGłówke(of, kogo.getName());
		
		Bukkit.broadcastMessage(prefix + Func.msg("%s zlecił zabicie %s! Zabij i zdobądz nagrode!",
				kto.getDisplayName(), kogo.getDisplayName()));
	}
	
	
	
	void dodajGłówke(Oferta oferta, String kogo) {
		int i;
		if ((i = menu.firstEmpty()) == -1) {
			i = -1;
			while (menu.getItem(++i).getType().equals(Material.PLAYER_HEAD));
		}
		menu.setItem(i, 
				ItemCreator.nowy(Func.dajGłowe(Func.graczOffline(kogo)))
				.nazwa("&9" + kogo)
				.lore("&6Zleceniodawca: &e" + oferta.zleceniodawcaDisplayName)
				.stwórz()
				);
	}
	
	
	// gui
	
	Inventory menu = Bukkit.createInventory(null, 5*9, Func.koloruj("&4&lHitman"));
	
	final Set<String> otwarte = Sets.newConcurrentHashSet();
	void otwórz(Player p) {
		p.openInventory(menu);
		p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		otwarte.add(p.getName());
	}
	void pokażNagrody(Player p, String kogo) {
		Oferta of = (Oferta) config.wczytaj(kogo);
		Inventory inv = Bukkit.createInventory(p, ((of.itemy.size() - 1) / 9 + 2) * 9, Func.koloruj("&4&lNagrody za &c&l&o" + kogo));
		int i=0;
		for (ItemStack item : of.itemy)
			inv.setItem(i++, item);
		while (i < inv.getSize())
			inv.setItem(i++, Baza.pustySlot);
		inv.setItem(inv.getSize() - 5, Func.stwórzItem(Material.BARRIER, "&4Powrót"));
		p.openInventory(inv);
		otwarte.add(p.getName());
		p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
	}
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (ev.getRawSlot() < 0 || ev.getRawSlot() >= ev.getInventory().getSize())
			return;
		if (otwarte.contains(ev.getWhoClicked().getName())) {
			switch (ev.getCurrentItem().getType()) {
			case PLAYER_HEAD:
				pokażNagrody((Player) ev.getWhoClicked(), ev.getCurrentItem().getItemMeta().getDisplayName().substring(2));
				break;
			case BARRIER:
				otwórz((Player) ev.getWhoClicked());
				break;
			default:
				break;
			}
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		otwarte.remove(ev.getPlayer().getName());
		
		Func.wykonajDlaNieNull(wyznaczający.remove(ev.getPlayer().getName()), doZabicia -> {
			List<ItemStack> itemy = Lists.newArrayList();
			List<ItemStack> wymagane = Lists.newArrayList(wymagane());
			final List<Integer> doUsunięcia = Lists.newArrayList();
			for (ItemStack item : ev.getInventory())
				Func.wykonajDlaNieNull(item, __ -> {
					itemy.add(item);
					ItemStack w;
					doUsunięcia.clear();
					for (int i=0; i<wymagane.size(); i++)
						if ((w = wymagane.get(i)).isSimilar(item)) {
							w.setAmount(w.getAmount() - item.getAmount());
							if (w.getAmount() <= 0)
								doUsunięcia.add(i);
						}
					while (!doUsunięcia.isEmpty())
						wymagane.remove((int) doUsunięcia.remove(doUsunięcia.size() - 1));
				});
			if (wymagane.isEmpty()) 
				zleć((Player) ev.getPlayer(), doZabicia, itemy);
			else {
				Napis n = new Napis(prefix + "Aby zlecić czyjeś zabicie musisz w wynagrodzeniu umieścić jeszcze conajmniej: ");
				for (ItemStack item : wymagane)
					n.dodaj(Napis.item(item));
				n.wyświetl(ev.getPlayer());
				for (ItemStack item : itemy)
					Func.dajItem((Player) ev.getPlayer(), item);
			}
		});
	}
	
	final HashMap<String, Player> wyznaczający = new HashMap<>();
	void wyznaczNagrode(Player p, Player kogo) {
		wyznaczający.put(p.getName(), kogo);
		Inventory inv = Bukkit.createInventory(null, 5*9, Func.koloruj("&4&lNagroda za " + kogo.getDisplayName()));
		p.openInventory(inv);
	}
	
	
	// Override / util

	
	List<ItemStack> wymagane() {
		return Main.ust.wczytajItemy("Hitman.wymagane itemy");
	}
	
	@Override public Krotka<String, Object> raport() { return Func.r("Wczytane oferty", config.klucze(false).size()); }
	@Override public void przeładuj() {
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			config.przeładuj();
			menu.clear();
			for (Entry<String, Object> en : config.mapa(false).entrySet())
				dodajGłówke((Oferta) en.getValue(), en.getKey());
			while (menu.firstEmpty() != -1)
				menu.setItem(menu.firstEmpty(), Baza.pustySlot);
		});
	}
	
	
	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) { return null; }
	@Override public boolean 	  onCommand	   (CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		
		if (cmd.getName().equals("hitman"))
			otwórz(p);
		else {
			if (args.length < 1)
				return false;
			Player zlecony = Bukkit.getPlayer(args[0]);
			if (zlecony == null)
				return Func.powiadom(sender, prefix + "Nie możesz zlecić zabicia gracza, który jest offline");
			if (zlecony.hasPermission(permNietykalność))
				return Func.powiadom(sender, prefix + "Nie możesz zlecić tego gracza, gdyż jest on nietylakny");
			if (zlecony.getName().equals(p.getName()))
				return Func.powiadom(sender, prefix + "Nie możesz zlecić zabicia samego siebie");
			if (config.klucze(false).contains(zlecony.getName()))
				return Func.powiadom(sender, prefix + "Na tego gracze jest już zlecenie");
			if (config.klucze(false).size() >= menu.getSize())
				return Func.powiadom(sender, prefix + "Rynek pełen, wykonaj pare zleceń aby zwolnić miejsce");
			Gracz g = Gracz.wczytaj(p);
			int czas = Main.ust.wczytajLubDomyślna("Hitman.deley zgłoszenia", 12*60*60);
			czas = czas - (int) ((System.currentTimeMillis() / 1000) - g.HitmanOstatnieZgłoszenie);
			if (czas > 0)
				return Func.powiadom(sender, prefix + "Musisz poczekać jeszcze " + Func.czas(czas) + " zanim kogoś zlecisz");
			wyznaczNagrode(p, zlecony);
		}
		return true;
	}
}
