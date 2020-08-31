package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.MiniGierki.Stare.MiniGra;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class Lootbagi extends Komenda implements Listener, Przeładowalny{
	public static final String prefix = Func.prefix("Lootbagi");
	public static final Config config = new Config("configi/lootbagi");
	public static final HashMap<String, Lootbag> lootbagi = new HashMap<>();
	public static final HashMap<String, Inventory> itemy = new HashMap<>();
	
	public Lootbagi() {
		super("lootbag");
	}
	
	public void przeładuj() {
		for (Inventory inv : itemy.values())
			for (HumanEntity p : inv.getViewers()) {
				p.closeInventory();
				p.sendMessage("§aPrzeładowywanie Pluginu");
			}
		config.przeładuj();
		lootbagi.clear();
		for (String klucz : config.klucze(false))
			Lootbag.wczytaj(config, klucz);
	}
	public String raport() {
		return "§6Lootbagi: §e" + lootbagi.size();
	}
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();
		ItemStack item = ev.getItem();
		if (item == null) return;
		if (ev.getAction().toString().contains("LEFT")) return;
		if (!(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && 
				item.getItemMeta().getDisplayName().startsWith("§6Lootbag "))) return;
		String nazwa = item.getItemMeta().getDisplayName();
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
			MiniGra.powiadomOp(prefix + "§cLootbag " + lootbag + " nie ma żadnych wygranych");
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
		if (!ev.getView().getTitle().startsWith("§1Edytuj Lootbag§7 ")) return;
		final HumanEntity p = ev.getPlayer();
		final String nazwa = ev.getView().getTitle().substring(19);
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
		if (ev.getView().getTitle().startsWith("§1Lootbag §r"))
			ev.setCancelled(true);
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getView().getTitle().startsWith("§1Lootbag §r"))
			ev.setCancelled(true);
	}
	@EventHandler
	public void przeciaganieEq(InventoryDragEvent ev) {
		if (ev.getView().getTitle().startsWith("§1Lootbag §r"))
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("l"))) {
			sender.sendMessage("§6Lootbagi: §a" + 
(lootbagi.keySet().isEmpty() ? "§ebrak" : Func.listToString(Lists.newArrayList(lootbagi.keySet()), 0, "§f, §a")));
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
			if (!(sender instanceof Player)) return Main.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			if (!lootbagi.containsKey(nazwa)) return Main.powiadom(p, prefix + "Lootbag o tej nazwie nie istnieje");

			Inventory inv = itemy.get(nazwa);
			if (inv == null) {
				inv = Bukkit.createInventory(null, 9*6, "§1Edytuj Lootbag§7 " + lootbag.nazwa);
				itemy.put(nazwa, inv);
				for (ItemStack item : lootbag.wygrane)
					inv.setItem(inv.firstEmpty(), item);
			}
			
			if (lootbag.wygrane.size() > 9*6) return Main.powiadom(p, prefix + "ten lootbag jest za duży aby edytować go poza plikiem");
			
			p.openInventory(inv);
			break;
		case "i":
		case "item":
			if (!(sender instanceof Player)) return Main.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			ItemStack item = p.getInventory().getItemInMainHand();
			if (lootbag == null)
				return Main.powiadom(p, prefix + "Lootbag §e" + nazwa + "§6 nie istnieje");
			if (item == null || item.getType().isAir())
				return Main.powiadom(p, prefix + "Nie możesz ustawić itemu lootbaga jago powietrze");
			
			lootbag.item = item;
			lootbag.item.setAmount(1);
			ItemMeta meta = lootbag.item.getItemMeta();
			meta.setDisplayName("§6Lootbag " + lootbag.nazwa);
			lootbag.item.setItemMeta(meta);
			lootbag.zapisz(config, true);
			p.sendMessage(prefix + "Ustawiono item lootbaga §e" + lootbag.nazwa);
			break;
		case "d":
		case "daj":
			if (!(sender instanceof Player)) return Main.powiadom(sender, prefix + "tej komendy może użyć tylko gracz");
			
			if (lootbag == null)
				return Main.powiadom(p, prefix + "Lootbag §e" + nazwa + "§6 nie istnieje");
			if (p.getInventory().firstEmpty() == -1)
				return Main.powiadom(p, prefix + "Twój ekwipunek jest pełny");
			p.sendMessage(prefix + "Wziołeś lootbag §e" + lootbag.nazwa);
			p.getInventory().addItem(lootbag.item);
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

class Lootbag {
	public static final String prefix = Lootbagi.prefix;
	public String nazwa;
	protected ItemStack item = new ItemStack(Material.LEATHER);
	protected List<ItemStack> wygrane = Lists.newArrayList();
	protected Inventory inv;
	private boolean broadcast;
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
		
		lootbag.wygrane = config.wczytajItemy(nazwa, "wygrane");
		if (lootbag.wygrane == null) lootbag.wygrane = Lists.newArrayList();
		
		lootbag.item = config.wczytajItem(nazwa, "item");
		lootbag.broadcast = (boolean) config.wczytajLubDomyślna(nazwa + ".broadcast", false);
		
		lootbag.ustawPodgląd();
		
		return lootbag;
	}
	
	public void ustawItemy(List<ItemStack> itemy) {
		wygrane = itemy;
		ustawPodgląd();
	}
	private void ustawPodgląd() {
		inv = Bukkit.createInventory(null, Math.min(54, (((wygrane.size()-1) / 9 + 1) * 9)), "§1Lootbag §r" + nazwa);
		for (int i=0; i < wygrane.size() && i < 54; i++)
			inv.setItem(i, wygrane.get(i));
	}
	
	public ItemStack losuj() {
		return wygrane.get(Func.losuj(0, wygrane.size()-1));
	}
	
	public void otwórz(Player p) {
		p.getInventory().addItem(losuj());
		ItemStack itemWRęce = p.getInventory().getItemInMainHand();
		itemWRęce.setAmount(itemWRęce.getAmount()-1);
		p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation(), 50, .3, .5, .3);
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, .6f, .6f);
		if (broadcast) Bukkit.broadcastMessage(prefix+"§e"+p.getDisplayName()+"§6 otworzył lootbag "+nazwa+"§6!");
	}
	
	public String toString() {
		return String.format("Lootbag(%s, item:%s, wygrane:%s)", nazwa, item.getType(), wygrane.size());
	}
}
