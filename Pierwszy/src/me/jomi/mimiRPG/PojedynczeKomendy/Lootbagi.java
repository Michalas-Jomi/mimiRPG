package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Lootbagi extends Komenda implements Listener, Przeładowalny {
	public static class Lootbag {
		public static final String prefix = Lootbagi.prefix;
		public String nazwa;
		protected ItemStack item = new ItemStack(Material.LEATHER);
		protected List<ItemStack> wygrane = Lists.newArrayList();
		protected Inventory inv;
		private boolean broadcast;
		private int ilośćItemów = 1;
		
		public Lootbag(String nazwa) {
			this.nazwa = Func.koloruj(nazwa);
			Lootbagi.lootbagi.put(this.nazwa, this);
			
			item = Func.stwórzItem(Material.LEATHER, 1, "&6Lootbag " + nazwa, Arrays.asList(
					"PPM aby podejrzeć", "Shift + PPM aby otworzyć", "/lootbag edytuj aby dodać drop", 
					"/lootbag item aby ustawić item lootbaga"));
			
			ustawItemy(Arrays.asList(Func.stwórzItem(Material.FIREWORK_ROCKET, 1, 
					"&6Przykładowa fajerwerka", Arrays.asList("Wrzuć tu itemy które mogą wydropić"))));
		}
		public void zapisz(Config config, boolean zapisz) {
			config.ustaw(nazwa + ".wygrane", wygrane);
			config.ustaw(nazwa + ".item", item);
			config.ustaw(nazwa + ".broadcast", broadcast);
			
			if (zapisz)
				config.zapisz();
		}
		public static Lootbag wczytaj(Config config, String nazwa) {
			Lootbag lootbag = new Lootbag(nazwa);
			
			lootbag.wygrane = config.wczytajItemy(nazwa + ".wygrane");
			if (lootbag.wygrane == null) lootbag.wygrane = Lists.newArrayList();
			
			lootbag.item = config.wczytajItem(nazwa + ".item");
			lootbag.broadcast = config.wczytaj(nazwa + ".broadcast", false);
			lootbag.ilośćItemów = config.wczytaj(nazwa + ".ilośćItemów", 1);
			
			lootbag.ustawPodgląd();
			
			return lootbag;
		}
		
		public void ustawItemy(List<ItemStack> itemy) {
			wygrane = itemy;
			ustawPodgląd();
		}
		private void ustawPodgląd() {
			inv = Func.createInventory(null, Math.min(54, (((wygrane.size()-1) / 9 + 1) * 9)), "§1Lootbag §r" + nazwa);
			for (int i=0; i < wygrane.size() && i < 54; i++)
				inv.setItem(i, wygrane.get(i));
		}
		
		public List<ItemStack> losuj() {
			Set<Integer> wygrywające = Sets.newConcurrentHashSet();
			while (wygrywające.size() < ilośćItemów && wygrywające.size() < wygrane.size())
				wygrywające.add(Func.losujWZasięgu(wygrane.size()));
			
			List<ItemStack> itemy = Lists.newArrayList();
			for (int i : wygrywające)
				itemy.add(wygrane.get(i));
			
			return itemy;
		}
		
		public void otwórz(Player p) {
			Napis n = new Napis();
			n.dodaj("§cWylosowałeś ");
			for (ItemStack item : losuj()) {
				Func.dajItem(p, item);
				n.dodaj(Napis.item(item)).dodaj(" ");
			}
			ItemStack itemWRęce = p.getInventory().getItemInMainHand();
			itemWRęce.setAmount(itemWRęce.getAmount()-1);
			p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation(), 50, .3, .5, .3);
			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, .6f, .6f);
			if (broadcast) Func.broadcast(prefix+"§e"+Func.getDisplayName(p)+"§6 otworzył lootbag "+nazwa+"§6!");
			n.dodaj("§6z " + nazwa).wyświetl(p);
		}
		
		@Override
		public String toString() {
			return String.format("Lootbag(%s, item:%s, wygrane:%s)", nazwa, item.getType(), wygrane.size());
		}
	}
	
	public static final HashMap<String, Lootbag> lootbagi = new HashMap<>();
	public static final HashMap<String, Inventory> itemy = new HashMap<>();
	public static final Config config = new Config("configi/lootbagi");
	public static final String prefix = Func.prefix("Lootbagi");
	
	public Lootbagi() {
		super("lootbag");
	}
	
	@Override
	public void przeładuj() {
		for (Inventory inv : itemy.values())
			for (HumanEntity p : inv.getViewers()) {
				p.closeInventory();
				p.sendMessage("§aPrzeładowywanie Pluginu");
			}
		config.przeładuj();
		lootbagi.clear();
		for (String klucz : config.klucze())
			Lootbag.wczytaj(config, klucz);
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Lootbagi", lootbagi.size());
	}
	
	private boolean jestLootbagiem(ItemStack item) {
		
		return	item != null &&
				item.hasItemMeta() &&
				item.getItemMeta().hasDisplayName() &&
				Func.getDisplayName(item.getItemMeta()).startsWith("§6Lootbag ");
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if (!Func.multiEquals(ev.getAction(), org.bukkit.event.block.Action.RIGHT_CLICK_AIR, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
		
		Player p = ev.getPlayer();
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		
		if (jestLootbagiem(p.getInventory().getItemInOffHand()))
			ev.setCancelled(true);
		
		if (!jestLootbagiem(item)) return;
		
		String nazwa = Func.getDisplayName(item.getItemMeta());
		nazwa = nazwa.substring(nazwa.indexOf(" ")+1);
		
		Lootbag lootbag = lootbagi.get(nazwa);
		if (lootbag == null) return;
		
		ev.setCancelled(true);
		
		if (!Func.porównaj(item, lootbag.item))
			return;
		if (!p.hasPermission("mimiRPG.lootbag.otworz")) {
			p.sendMessage(prefix + "Nie masz uprawnień na otwieranie lootbagów");
			return;
		}
		if (lootbag.wygrane.isEmpty()) {
			p.sendMessage(prefix + "Ten lootbag jest pusty");
			return;
		}
		if (p.getInventory().firstEmpty() == -1) {
			p.sendMessage(prefix + "Twój ekwipunek jest pełny");
			return;
		}
		
		if (p.isSneaking())
			lootbag.otwórz(p);
		else
			p.openInventory(lootbag.inv);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (!Func.getTitle(ev.getView()).startsWith("§1Edytuj Lootbag§7 ")) return;
		final HumanEntity p = ev.getPlayer();
		final String nazwa = Func.getTitle(ev.getView()).substring(19);
		Lootbag lootbag = lootbagi.get(nazwa);
		if (lootbag == null) {
			p.sendMessage(prefix + "§cLootbag nie istnieje!");
			return;
		}
		final List<ItemStack> eqItemy = Lists.newArrayList();
		for (int i=0; i < ev.getInventory().getSize(); i++) {
			ItemStack item = ev.getInventory().getItem(i);
			if (item != null && !item.getType().isAir())
				eqItemy.add(item);
		}
		if (eqItemy.isEmpty()) {
			p.sendMessage(prefix + "§cNie ustawiono żadnych dropów");
			return;
		}
		lootbag.ustawItemy(eqItemy);
		lootbag.zapisz(config, true);
		p.sendMessage(prefix + "§aZapisano lootbag §r" + lootbag.nazwa);
		if (ev.getViewers().size() == 1) 
			itemy.remove(nazwa);
	}
	
	@EventHandler
	public void interkacjaEq(InventoryInteractEvent ev) {
		if (Func.getTitle(ev.getView()).startsWith("§1Lootbag §r"))
			ev.setCancelled(true);
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (Func.getTitle(ev.getView()).startsWith("§1Lootbag §r"))
			ev.setCancelled(true);
	}
	@EventHandler
	public void przeciaganieEq(InventoryDragEvent ev) {
		if (Func.getTitle(ev.getView()).startsWith("§1Lootbag §r"))
			ev.setCancelled(true);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> lista = Lists.newArrayList();
		String ost = "";
		if (args.length <= 1) {
			lista = Arrays.asList("stwórz edytuj usuń item daj lista".split(" "));
			ost = args.length == 1 ? args[0] : "";
		} else {
			lista = Lists.newArrayList(lootbagi.keySet());
			for (int i=0; i<lista.size(); i++)
				lista.set(i, Func.odkoloruj(lista.get(i)));
			ost = Func.listToString(args, 1);
		}
		return uzupełnijTabComplete(ost, lista);
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("l"))) {
			sender.sendMessage("§6Lootbagi: §a" + (lootbagi.keySet().isEmpty() ? "§ebrak" : Func.listToString(Lists.newArrayList(lootbagi.keySet()), 0, "§f, §a")));
			return true;
		}
		if (args.length < 2) return info(sender);
		final String nazwa = Func.koloruj(Func.listToString(args, 1));
		Player p = null;
		Lootbag lootbag = lootbagi.get(nazwa);
		if (sender instanceof Player)
			p = (Player) sender;
		switch(args[0].toLowerCase()) {
		case "s":
		case "stworz":
		case "stwórz":
			if (lootbagi.containsKey(nazwa)) {
				sender.sendMessage(prefix + "Lootbag o tej nazwie już istnieje");
				return true;
			}
			new Lootbag(nazwa);
			sender.sendMessage(prefix + "Stworzono Lootbag §e" + nazwa);
			break;
		case "u":
		case "usun":
		case "usuń":
			if (!lootbagi.containsKey(nazwa)) {
				sender.sendMessage(prefix + "Lootbag o tej nazwie nie istnieje");
				return true;
			}
			if (itemy.containsKey(nazwa))
				for (HumanEntity gracz : itemy.get(nazwa).getViewers()) {
					gracz.closeInventory();
					gracz.sendMessage("§e" + sender.getName() + " §6 właśnie usunął lootbag §e" + nazwa);
				}
			lootbagi.remove(nazwa);
			config.ustaw_zapisz(nazwa, null);
			sender.sendMessage(prefix + "Usunięto Lootbag §e" + nazwa);
			break;
		case "e":
		case "edytuj":
			if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			if (!lootbagi.containsKey(nazwa)) return Func.powiadom(p, prefix + "Lootbag o tej nazwie nie istnieje");

			Inventory inv = itemy.get(nazwa);
			if (inv == null) {
				inv = Func.createInventory(null, 9*6, "§1Edytuj Lootbag§7 " + lootbag.nazwa);
				itemy.put(nazwa, inv);
				for (ItemStack item : lootbag.wygrane)
					inv.setItem(inv.firstEmpty(), item);
			}
			
			if (lootbag.wygrane.size() > 9*6) return Func.powiadom(p, prefix + "ten lootbag jest za duży aby edytować go poza plikiem");
			
			p.openInventory(inv);
			break;
		case "i":
		case "item":
			if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			ItemStack item = p.getInventory().getItemInMainHand();
			if (lootbag == null)
				return Func.powiadom(p, prefix + "Lootbag §e" + nazwa + "§6 nie istnieje");
			if (item == null || item.getType().isAir())
				return Func.powiadom(p, prefix + "Nie możesz ustawić itemu lootbaga jago powietrze");
			
			lootbag.item = item;
			lootbag.item.setAmount(1);
			ItemMeta meta = lootbag.item.getItemMeta();
			Func.setDisplayName(meta, "§6Lootbag " + lootbag.nazwa);
			lootbag.item.setItemMeta(meta);
			lootbag.zapisz(config, true);
			p.sendMessage(prefix + "Ustawiono item lootbaga §e" + lootbag.nazwa);
			break;
		case "d":
		case "daj":
			if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			if (lootbag == null)
				return Func.powiadom(p, prefix + "Lootbag §e" + nazwa + "§6 nie istnieje");
			if (p.getInventory().firstEmpty() == -1)
				return Func.powiadom(p, prefix + "Twój ekwipunek jest pełny");
			p.sendMessage(prefix + "Wziołeś lootbag §e" + lootbag.nazwa);
			Func.dajItem(p, lootbag.item);
			break;
		default:
			return info(sender);
		}
		return true;
	}
	private boolean info(CommandSender p) {
		Napis n = new Napis("\n\n\n\n§6-----§l> §aLootbagi §6§l<§6-----");
		n.dodaj(new Napis("\n§c> §e/lootbag stwórz <nazwa>", 	"§bTworzy nowego lootbaga", 			Action.SUGGEST_COMMAND, "/lootbag stwórz "));
		n.dodaj(new Napis("\n§c> §e/lootbag edytuj <nazwa>", 	"§bEdytuje zawartość lootbaga", 		Action.SUGGEST_COMMAND, "/lootbag edytuj "));
		n.dodaj(new Napis("\n§c> §e/lootbag usuń <nazwa>", 	"§bUsuwa lootbaga", 					Action.SUGGEST_COMMAND, "/lootbag usuń "));
		n.dodaj(new Napis("\n§c> §e/lootbag item <nazwa>", 	"§bustawia item lootbaga, jakby ikona", Action.SUGGEST_COMMAND, "/lootbag item "));
		n.dodaj(new Napis("\n§c> §e/lootbag daj <nazwa>", 	"§bdaje ci 1 lootbag danego typu", 		Action.SUGGEST_COMMAND, "/lootbag daj "));
		n.dodaj(new Napis("\n§c> §e/lootbag lista", 			"§bwyświetla wszystkie lootbagi", 		Action.RUN_COMMAND, 	"/lootbag lista"));
		n.dodaj("\n");
		n.wyświetl(p);
		return true;
	}
}
