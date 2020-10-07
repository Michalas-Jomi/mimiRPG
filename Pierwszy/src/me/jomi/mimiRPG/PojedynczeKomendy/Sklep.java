package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.iridium.iridiumskyblock.User;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class Sklep extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Sklep");
	final HashMap<String, Strona> otwarte = new HashMap<>();
	final HashMap<String, Strona> strony = new HashMap<>();
	public static Sklep inst;
	public Sklep() {
		super("sklep");
		inst = this;
	}

	@Override
	public void przeładuj() {
		for (String gracz : otwarte.keySet()) {
			Player p = Bukkit.getPlayer(gracz);
			if (p == null) continue;
			p.closeInventory();
			p.sendMessage(prefix + "Przeładowywanie Sklepu");
		}
		otwarte.clear();
		strony.clear();
		File f = new File (Main.path + "sklep");
		if (!f.exists())
			f.mkdirs();
		else 
			skanuj(f);
	}
	private void skanuj(File f) {
		for (File plik : f.listFiles())
			if (plik.isDirectory())
				skanuj(plik);
			else if (plik.getName().endsWith(".yml"))
				wczytaj(plik);
			else
				Main.warn("Nieznany plik", plik.getAbsolutePath() + ", ignorowanie go.");
	}
	private void wczytaj(File plik) {
		Config config;
		try {
			config = new Config(plik);
		} catch (Exception e) {
			Main.error("Nieprawidłowy plik:", plik.getAbsolutePath(), "omijanie go");
			return;
		}
		new Strona(config);
	}

	@Override
	public String raport() {
		return "§6Wczytane Strony Sklepu: §e" + strony.size();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, strony.keySet());
		else
			return Lists.newArrayList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Sklep jest dostępny tylko dla graczy");
		Player p = (Player) sender;
		if (args.length >= 1)
			otwórz(p, args[0]);
		else if (strony.containsKey("Główna"))
			otwórz(p, "Główna");
		else
			p.sendMessage(prefix + "Nie podano żadnej strony");
		return true;
	}
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) return;
		Player p = (Player) ev.getWhoClicked();
		Strona strona = otwarte.get(p.getName());
		if (strona != null) {
			int slot = ev.getRawSlot();
			if (slot >= 0 && slot < strona.inv.getSize()) {
				ev.setCancelled(true);
				otwarte.get(p.getName()).kliknięty(p, slot, ev.getClick(), ev.getCurrentItem());
			}
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (otwarte.containsKey(ev.getPlayer().getName()))
			otwarte.remove(ev.getPlayer().getName());
	}
	void otwórz(Player p, String strona) {
		if (strony.containsKey(strona)) {
			strony.get(strona).otwórz(p);
		} else
			p.sendMessage(prefix + "§cTa stona nie istnieje");
	}
}

class Strona {
	final static ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l");
	String nazwa;
	Inventory inv;
	String nazwaInv;
	List<SklepItem> specjalneItemy = Lists.newArrayList();
	
	boolean bezpośrednia = true;
	public Strona(Config config) {
		nazwa = config.f.getName();
		nazwa = nazwa.substring(0, nazwa.lastIndexOf('.'));
		int sloty = config.wczytajLubDomyślna("rzędy", 6);
		nazwaInv = config.wczytajLubDomyślna("nazwa", "§1§lSklep");
		inv = Bukkit.createInventory(null, sloty*9, nazwaInv);
		for (int i=0; i<inv.getSize(); i++)
			inv.setItem(i, pustySlot);
		
		for (String _slot : config.klucze(false)) {
			int slot = Func.Int(_slot.replace("_", ""), -1);
			if (slot == -1 || slot > inv.getSize()) {
				if (_slot.equals("rzędy") || _slot.equals("nazwa")) continue;
				Main.warn("Niepoprawny nr slotu (" + _slot + ") w pliku: " + config.f.getAbsolutePath());
				continue;
			}
			

			boolean ustaw = true;
			
			ItemStack item = config.wczytajItem(_slot + ".item");
			if (item == null) {
				Main.warn("Niepoprawny item (slot: " + slot + ") w pliku: " + config.f.getAbsolutePath());
				continue;
			}
			
			item.setAmount(Math.min(config.wczytajLubDomyślna(_slot + ".ilość", 1), 64));
			
			double max = -1;
			double min = 0;
			try {
				max = config.wczytajDouble(_slot + ".pkt_max");
			} catch(Exception e) {}
			try {
				min = config.wczytajDouble(_slot + ".pkt_min");
			} catch(Exception e) {}
			if (max != -1 || min != 0) {
				bezpośrednia = false;
				ustaw = false;
				specjalneItemy.add(new SklepItem(item, slot, min, max));
			}
			
			String strona = config.wczytajLubDomyślna(_slot + ".strona", "");
			if (!strona.isEmpty()) {
				specjalneItemy.add(new SklepItem(item, slot, strona));
			} else {
				UnaryOperator<String> cena = czynność -> {
					int _cena = config.wczytajLubDomyślna(_slot + ".cena " + czynność, 0);
					return _cena == 0 ? "§cBrak możliwości " + czynność : "§6Cena " + czynność + ":§e " + _cena;
				};
				ItemMeta meta = item.getItemMeta();
				List<String> lore = Func.nieNullList(meta.getLore());
				lore.add(0, "§7------------------------------");
				lore.add(0, "");
				lore.add(0, "§9PPM §8- §akup");
				lore.add(0, "§9LPM §8- §asprzedaj");
				lore.add(0, "§9SHIFT §e+ §9PPM §8- §akup wszystko");
				lore.add(0, "§9SHIFT §e+ §9LPM §8- §asprzedaj wszystko");
				lore.addAll(0, config.wczytajListe("stały lore"));
				lore.add(0, cena.apply("sprzedarzy"));
				lore.add(0, cena.apply("kupna"));
				meta.setLore(lore);
				item.setItemMeta(meta);
			}
			
			if (ustaw)
				inv.setItem(slot, item);
		}
		Sklep.inst.strony.put(nazwa, this);
	}
	
	void otwórz(Player p) {
		if (bezpośrednia || !Main.iridiumSkyblock)
			p.openInventory(inv);
		else {
			Inventory _inv = Func.CloneInv(inv, nazwaInv);
			double pkt = User.getUser(p).getIsland().getValue();
			for (SklepItem item : specjalneItemy)
				if (item.strona == null && (pkt >= item.pkt_min && (item.pkt_max == -1 || pkt < item.pkt_max)))
					_inv.setItem(item.slot, item.item);
			p.openInventory(_inv);
		}
		Sklep.inst.otwarte.put(p.getName(), this);
	}
	
	void kliknięty(Player p, int slot, ClickType typ, ItemStack klikanyItem) {
		if (!Main.ekonomia) {
			p.sendMessage(Sklep.prefix + "Na serwerze nie ma ekonomi, więc sklep nie działa");
			return;
		}
		
		for (SklepItem Sitem : specjalneItemy) 
			if (Sitem.slot == slot && Sitem.strona != null &&
				(bezpośrednia || (!bezpośrednia && Func.porównaj(Sitem.item, klikanyItem)))) {
					Sklep.inst.otwórz(p, Sitem.strona);
					return;
			}
		if (Func.porównaj(klikanyItem, pustySlot)) return;
		
		ItemStack finalnyItem = klikanyItem.clone();
		ItemMeta meta = finalnyItem.getItemMeta();
		List<String> lore = meta.getLore();
		lore = lore.subList(2, lore.size());
		meta.setLore(lore);
		finalnyItem.setItemMeta(meta);
		
		PlayerInventory pinv = p.getInventory();
		Function<Integer, Double> kwota = nrLini -> {
			String linia = klikanyItem.getItemMeta().getLore().get(nrLini);
			return Func.Double(linia.substring(linia.lastIndexOf(' ')+1), 0) / finalnyItem.getAmount();
		};
		Consumer<Integer> sprzedaj = limit -> {
			double cena = kwota.apply(1);
			if (cena <= 0) return;
			
			int limitStart = limit;
			ItemStack item2;
			for (int i=0; i<4*9 && limit > 0; i++)
				if ((item2 = pinv.getItem(i)) != null && Func.porównaj(finalnyItem, item2))
					if (item2.getAmount() > limit) {
						limit = 0;
						item2.setAmount(item2.getAmount() - limit);
					} else {
						limit -= item2.getAmount();
						pinv.setItem(i, null);
					}
			int sprzedane = limitStart - limit;
			Main.econ.depositPlayer(p, sprzedane * cena);
		};
		Consumer<Integer> kup = limit -> {
			double cena = kwota.apply(0);
			if (cena <= 0) return;
			
			double kasa = Main.econ.getBalance(p);
			limit = Math.min(limit, (int) (kasa / cena));
			int limitStart = limit;
			ItemStack item2;
			for (int i=0; i<4*9 && limit > 0; i++)
				if ((item2 = pinv.getItem(i)) == null || Func.porównaj(finalnyItem, item2)) {
					int akt = item2 == null ? 0 : item2.getAmount();
					int dodatek = Math.min(limit, finalnyItem.getMaxStackSize() - akt);
					limit -= dodatek;
					item2 = finalnyItem.clone();
					item2.setAmount(dodatek + akt);
					pinv.setItem(i, item2);
				}
			int kupione = limitStart - limit;
			Main.econ.withdrawPlayer(p, kupione * cena);
		};
		switch(typ) {
		case LEFT:
			sprzedaj.accept(finalnyItem.getAmount());
			break;
		case SHIFT_LEFT:
			sprzedaj.accept(64*6*9);
			break;
		case RIGHT:
			kup.accept(finalnyItem.getAmount());
			break;
		case SHIFT_RIGHT:
			kup.accept(64*6*9);
			break;
		default:
			break;
		}
	}
	
}

class SklepItem {
	int slot;
	double pkt_min;
	double pkt_max;
	ItemStack item;
	String strona;
	SklepItem(ItemStack item, int slot, double pkt_min, double pkt_max) {
		this.pkt_min = pkt_min;
		this.pkt_max = pkt_max;
		this.slot = slot;
		this.item = item;
	}
	SklepItem(ItemStack item, int slot, String strona) {
		this.slot = slot;
		this.item = item;
		this.strona = strona;
	}
}

