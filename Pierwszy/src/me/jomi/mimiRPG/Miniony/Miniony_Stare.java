package me.jomi.mimiRPG.Miniony;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Miniony_Stare extends Komenda implements Listener, Przeładowalny {
	public static ItemStack itemKopacz  = stwórzItem(Material.COBBLESTONE,		"§1§lMinion §1Kopacz§9",	Arrays.asList("&aUżyj tego narzędzia (PPM)", "&aAby zespawnować swojego miniona", "&bid: &dnowy"));
	public static ItemStack itemFarmer  = stwórzItem(Material.HAY_BLOCK, 		"§1§lMinion §aFarmer§9",	Arrays.asList("&aUżyj tego narzędzia (PPM)", "&aAby zespawnować swojego miniona", "&bid: &dnowy"));
	public static ItemStack itemRzeznik = stwórzItem(Material.BONE_BLOCK, 		"§1§lMinion §cRzeźnik§9",	Arrays.asList("&aUżyj tego narzędzia (PPM)", "&aAby zespawnować swojego miniona", "&bid: &dnowy"));
	
	public static String prefix = Func.prefix("Minion");
	public static HashMap<String, Integer> otwarte = new HashMap<>();
	
	public static int odświeżanieMinionówTicki = 5;
	
	private static boolean wyłączanie = false;
	public static boolean włączone = false;
	
	@Override
	public void przeładuj() {
		Minion.mapaJedzenia.clear();
		for (String klucz : Main.ust.sekcja("Miniony.jedzenie").getKeys(false))
			Minion.mapaJedzenia.put(Material.valueOf(klucz.toUpperCase()), Main.ust.wczytajDouble("Miniony.jedzenie" + klucz));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Potrawy minionów", Minion.mapaJedzenia.size());
	}
	
	private static void zegar() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    @Override
			public void run() {
		    	zegar();
		    	for (Minion minion : Minion.mapa.values())
		    		minion._mimiTick(odświeżanieMinionówTicki);
		    }
		}, odświeżanieMinionówTicki);
	}
	
	public Miniony_Stare() {
		super("minion");
		Main.dodajPermisje("minion.bypass");
		Statystyka.ZainicjujStatystyki();
		usuńStare();
		wczytajMiniony();
		zegar();
	}
	private static void usuńStare() {
		for (Entity en : Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e[tag=mimiMinion]"))
			en.remove();
	}
	public static void wczytajMiniony() {
		usuńStare();
		File folder = new File("plugins/mimiRPG/miniony");
		if (folder.exists())
			for (File plik : folder.listFiles()) {
				String path = plik.getPath();
				path = "miniony/" + path.substring(path.lastIndexOf("\\")+1, path.length()-4);
				Config config = new Config(path);
				String imie = (String) config.wczytaj("imie");
				if (imie == null)
					continue;				
				switch(imie) {
				case "§1Kopacz":  new Kopacz(config);  break;
				case "§aFarmer":  new Farmer(config);  break;
				case "§cRzeźnik": new Rzeźnik(config); break;
				}
			}
		else
			folder.mkdirs();
		włączone = true;
	}
	public static void zapiszMiniony() {
		Main.log("Zapisywanie Minionów");
		wyłączanie = true;
		for (Minion m : Minion.mapa.values()) {
			m.zapisz();
			m.getBukkitEntity().remove();
		}
		włączone = false;
	}
	
	private static ItemStack stwórzItem(Material mat, String nazwa, List<String> lore) {
		ItemStack item = Func.stwórzItem(mat, 1, nazwa, lore);
		Func.enchantuj(item, Enchantment.LUCK, -1);
		Func.ukryj(item, ItemFlag.HIDE_ENCHANTS);
		return item;
	}
	
	@EventHandler
	public void klikniecieMoba(PlayerInteractEntityEvent ev) {
		if (!włączone) return;
		Entity mob = ev.getRightClicked();
		Player p = ev.getPlayer();
		int id = idminiona(mob);
		if (id == -1) return;
		if (otwarte.containsValue(id)) return;
		ev.setCancelled(true);
		Minion.mapa.get(id).kliknięty(p);
	}
	
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (!włączone) return;
		Player p = (Player) ev.getPlayer();
		if (otwarte.containsKey(p.getName()))
			Minion.mapa.get(otwarte.get(p.getName())).zamknij(p, ev.getInventory(), Func.getTitle(ev.getView()).equals("§1§lMinion"));
	}
	
	@EventHandler
	public void kliknięcieEq(InventoryClickEvent ev) {
		if (!włączone) return;
		Player p = (Player) ev.getWhoClicked();
		String nick = p.getName();
		if (otwarte.containsKey(nick))
			if (Minion.mapa.get(otwarte.get(nick)).kliknięcie(p, ev))
				ev.setCancelled(true);
	}
	
	@EventHandler
	public void bicieMiniona(EntityDamageByEntityEvent ev) {
		if (!włączone) return;
		if (idminiona(ev.getEntity()) != -1)
			ev.setCancelled(true);
		int id = idminiona(ev.getDamager());
		if (id != -1)
			Minion.mapa.get(id).użyjNarzędzia();
	}
	
	@EventHandler
	public void śmierćMiniona(EntityDeathEvent ev) {
		if (!włączone) return;
		int id = idminiona(ev.getEntity());
		if (id != -1) {
			Minion minion = Minion.mapa.get(id);
			ev.getDrops().clear();
			ev.setDroppedExp(0);
			if (!wyłączanie) {
				minion.zrespMoba();
				minion.zapisz();
				
			}
		}
	}
	
	private int idminiona(Entity mob) {
		Set<String> tagi = mob.getScoreboardTags();
		if (tagi.contains("mimiMinion"))
			for (String tag : tagi)
				if (tag.startsWith("mimiMinion_"))
					return Func.Int(tag.substring(11), -1);
		return -1;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void spawn(EntitySpawnEvent ev) {
		if (!włączone) return;
		if (ev.getEntity().getScoreboardTags().contains("mimiMinion"))
			ev.setCancelled(false);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void budowanie(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		if (!włączone) return;
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		if (item == null || item.getType().equals(Material.AIR)) return;
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
		String nazwa = Func.getDisplayName(item.getItemMeta());
		switch(nazwa) {
		case "§1§lMinion §1Kopacz§9":
		case "§1§lMinion §aFarmer§9":
		case "§1§lMinion §cRzeźnik§9":
			break;		
		default:
			return;
		}
		Player p = ev.getPlayer();

		if (Func.getLore(item.getItemMeta()).size() > 5)
			switch (nazwa) {
			case "§1§lMinion §1Kopacz§9":
				new Kopacz(p, item);
				break;
			case "§1§lMinion §aFarmer§9":
				new Farmer(p, item);
				break;
			case "§1§lMinion §cRzeźnik§9":
				new Rzeźnik(p, item);
				break;	
			}
		else 
			switch(nazwa) {
				case "§1§lMinion §1Kopacz§9":
					new Kopacz(p.getLocation(), p.getName());
					break;
				case "§1§lMinion §aFarmer§9":
					new Farmer(p.getLocation(), p.getName());
					break;
				case "§1§lMinion §cRzeźnik§9":
					new Rzeźnik(p.getLocation(), p.getName());
					break;		
		}
		ev.setCancelled(true);
		item.setAmount(item.getAmount() - 1);
		p.getInventory().setItemInMainHand(item);
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupełnijTabComplete(args, Arrays.asList("włącz", "wyłącz", "Kopacz", "Rzeźnik", "Farmer"));
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		switch(args[0]) {
		case "włącz":
			if (Miniony_Stare.włączone)
				sender.sendMessage(Miniony_Stare.prefix + "Miniony są już włączone");
			else {
				Miniony_Stare.wczytajMiniony();
				sender.sendMessage(Miniony_Stare.prefix + "Włączono miniony");
			}
			return true;
		case "wyłącz":
			if (Miniony_Stare.włączone) {
				Miniony_Stare.zapiszMiniony();
				sender.sendMessage(Miniony_Stare.prefix + "Wyłączono miniony");
			} else
				sender.sendMessage(Miniony_Stare.prefix + "Miniony są już wyłączone");
			return true;
		}
		Player p;
		if (args.length >= 2) {
			p = Bukkit.getPlayer(args[1]);
			if (p == null || !p.isOnline())
				return Func.powiadom(sender, prefix + "Niepoprawna nazwa gracza: " + args[1]);
		} else if (sender instanceof Player)
			p = (Player) sender;
		else
			return false;
		switch(args[0]) {
		case "f":
		case "Farmer":
			p.getInventory().addItem(itemFarmer);
			break;
		case "k":
		case "Kopacz":
			p.getInventory().addItem(itemKopacz);
			break;
		case "r":
		case "Rzeźnik":
			p.getInventory().addItem(itemRzeznik);
			break;
		}
		return true;
	}
}
