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

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Instrukcja;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;

public class Sklep extends Komenda implements Listener, Prze³adowalny, Instrukcja {
	public static final String prefix = Func.prefix("Sklep");
	final HashMap<String, Strona> otwarte = new HashMap<>();
	final HashMap<String, Strona> strony = new HashMap<>();
	public static Sklep inst;
	public Sklep() {
		super("sklep");
		inst = this;
	}

	@Override
	public void prze³aduj() {
		for (String gracz : otwarte.keySet()) {
			Player p = Bukkit.getPlayer(gracz);
			if (p == null) continue;
			p.closeInventory();
			p.sendMessage(prefix + "Prze³adowywanie Sklepu");
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
			Main.error("Nieprawid³owy plik:", plik.getAbsolutePath(), "omijanie go");
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
			return Main.powiadom(sender, prefix + "Sklep jest dostêpny tylko dla graczy");
		Player p = (Player) sender;
		if (args.length >= 1)
			otwórz(p, args[0]);
		else if (strony.containsKey("G³ówna"))
			otwórz(p, "G³ówna");
		else
			p.sendMessage(prefix + "Nie podano ¿adnej strony");
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
				otwarte.get(p.getName()).klikniêty(p, slot, ev.getClick());
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
			Strona str = strony.get(strona);
			p.openInventory(str.inv);
			otwarte.put(p.getName(), str);
		} else
			p.sendMessage(prefix + "§cTa stona nie istnieje");
	}

	@Override
	public void info(CommandSender p, int strona) {
		p.sendMessage("§9PPM §c- §bKupujesz");
		p.sendMessage("§9Shift + PPM §c- §bKupujesz mo¿liwie du¿o");
		p.sendMessage("§9LPM §c- §bSprzedajesz");
		p.sendMessage("§9Shift + LPM §c- §bSprzedajesz mo¿liwie du¿o");
		p.sendMessage("§9/sklep <strona>");
	}
}

class Strona {
	final static ItemStack pustySlot = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§1§l");
	final HashMap<Integer, String> mapa = new HashMap<>();
	String nazwa;
	Inventory inv;
	public Strona(Config config) {
		nazwa = config.f.getName();
		nazwa = nazwa.substring(0, nazwa.lastIndexOf('.'));
		int sloty = config.wczytajLubDomyœlna("rzêdy", 6);
		inv = Bukkit.createInventory(null, sloty*9, config.wczytajLubDomyœlna("nazwa", "§1§lSklep"));
		for (int i=0; i<inv.getSize(); i++)
			inv.setItem(i, pustySlot);
		for (String _slot : config.klucze(false)) {
			int slot = Func.Int(_slot, -1);
			if (slot == -1 || slot > inv.getSize()) {
				if (_slot.equals("rzêdy") || _slot.equals("nazwa")) continue;
				Main.warn("Niepoprawny nr slotu (" + _slot + ") w pliku: " + config.f.getAbsolutePath());
				continue;
			}
			ItemStack item = config.wczytajItem(_slot + "." + "item");
			if (item == null) {
				Main.warn("Niepoprawny item (slot: " + slot + ") w pliku: " + config.f.getAbsolutePath());
				continue;
			}
			
			item.setAmount(Math.min(config.wczytajLubDomyœlna(_slot + ".iloœæ", 1), 64));
			
			String strona = config.wczytajLubDomyœlna(_slot + ".strona", "");
			if (!strona.isEmpty()) {
				mapa.put(slot, strona);
				inv.setItem(slot, item);
				continue;
			}
			
			UnaryOperator<String> cena = czynnoœæ -> {
				int _cena = config.wczytajLubDomyœlna(_slot + ".cena " + czynnoœæ, 0);
				return _cena == 0 ? "§cBrak mo¿liwoœci " + czynnoœæ : "§6Cena " + czynnoœæ + ":§e " + _cena;
			};
			ItemMeta meta = item.getItemMeta();
			List<String> lore = Func.nieNullList(meta.getLore());
			lore.add(0, cena.apply("sprzedarzy"));
			lore.add(0, cena.apply("kupna"));
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.setItem(slot, item);
		}
		Sklep.inst.strony.put(nazwa, this);
	}
	
	void klikniêty(Player p, int slot, ClickType typ) {
		if (!Main.ekonomia) {
			p.sendMessage(Sklep.prefix + "Na serwerze nie ma ekonomi, wiêc sklep nie dzia³a");
			return;
		}
		if (mapa.containsKey(slot)) {
			Sklep.inst.otwórz(p, mapa.get(slot));
			return;
		}
		ItemStack klikanyItem = inv.getItem(slot);
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


