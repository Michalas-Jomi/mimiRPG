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

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze�adowalny;

public class Miniony extends Komenda implements Listener, Prze�adowalny {
	public static ItemStack itemKopacz  = stw�rzItem(Material.COBBLESTONE,		"�1�lMinion �1Kopacz�9",	Arrays.asList("&aU�yj tego narz�dzia (PPM)", "&aAby zespawnowa� swojego miniona", "&bid: &dnowy"));
	public static ItemStack itemFarmer  = stw�rzItem(Material.HAY_BLOCK, 		"�1�lMinion �aFarmer�9",	Arrays.asList("&aU�yj tego narz�dzia (PPM)", "&aAby zespawnowa� swojego miniona", "&bid: &dnowy"));
	public static ItemStack itemRzeznik = stw�rzItem(Material.BONE_BLOCK, 		"�1�lMinion �cRze�nik�9",	Arrays.asList("&aU�yj tego narz�dzia (PPM)", "&aAby zespawnowa� swojego miniona", "&bid: &dnowy"));
	
	public static String prefix = Func.prefix("Minion");
	public static HashMap<String, Integer> otwarte = new HashMap<>();
	
	public static int od�wie�anieMinion�wTicki = 5;
	
	private static boolean wy��czanie = false;
	public static boolean w��czone = false;
	
	public void prze�aduj() {
		Minion.mapaJedzenia.clear();
		for (String klucz : Main.ust.sekcja("Miniony.jedzenie").getKeys(false))
			Minion.mapaJedzenia.put(Material.valueOf(klucz.toUpperCase()), Main.ust.wczytajDouble("Miniony.jedzenie", klucz));
	}
	public String raport() {
		return "�6Potrawy minion�w: �e" + Minion.mapaJedzenia.size();
	}
	
	private static void zegar() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	zegar();
		    	for (Minion minion : Minion.mapa.values())
		    		minion._mimiTick(od�wie�anieMinion�wTicki);
		    }
		}, od�wie�anieMinion�wTicki);
	}
	
	public Miniony() {
		super("minion");
		Main.dodajPermisje("minion.bypass");
		Statystyka.ZainicjujStatystyki();
		usu�Stare();
		wczytajMiniony();
		zegar();
	}
	private static void usu�Stare() {
		for (Entity en : Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e[tag=mimiMinion]"))
			en.remove();
	}
	public static void wczytajMiniony() {
		usu�Stare();
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
				case "�1Kopacz":  new Kopacz(config);  break;
				case "�aFarmer":  new Farmer(config);  break;
				case "�cRze�nik": new Rze�nik(config); break;
				}
			}
		else
			folder.mkdirs();
		w��czone = true;
	}
	public static void zapiszMiniony() {
		Main.log("Zapisywanie Minion�w");
		wy��czanie = true;
		for (Minion m : Minion.mapa.values()) {
			m.zapisz();
			m.getBukkitEntity().remove();
		}
		w��czone = false;
	}
	
	private static ItemStack stw�rzItem(Material mat, String nazwa, List<String> lore) {
		ItemStack item = Func.stw�rzItem(mat, 1, nazwa, lore);
		Func.enchantuj(item, Enchantment.LUCK, -1);
		Func.ukryj(item, ItemFlag.HIDE_ENCHANTS);
		return item;
	}
	
	@EventHandler
	public void klikniecieMoba(PlayerInteractEntityEvent ev) {
		if (!w��czone) return;
		Entity mob = ev.getRightClicked();
		Player p = ev.getPlayer();
		int id = idminiona(mob);
		if (id == -1) return;
		if (otwarte.containsValue(id)) return;
		ev.setCancelled(true);
		Minion.mapa.get(id).klikni�ty(p);
	}
	
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (!w��czone) return;
		Player p = (Player) ev.getPlayer();
		if (otwarte.containsKey(p.getName()))
			Minion.mapa.get(otwarte.get(p.getName())).zamknij(p, ev.getInventory(), ev.getView().getTitle().equals("�1�lMinion"));
	}
	
	@EventHandler
	public void klikni�cieEq(InventoryClickEvent ev) {
		if (!w��czone) return;
		Player p = (Player) ev.getWhoClicked();
		String nick = p.getName();
		if (otwarte.containsKey(nick))
			if (Minion.mapa.get(otwarte.get(nick)).klikni�cie(p, ev))
				ev.setCancelled(true);
	}
	
	@EventHandler
	public void bicieMiniona(EntityDamageByEntityEvent ev) {
		if (!w��czone) return;
		if (idminiona(ev.getEntity()) != -1)
			ev.setCancelled(true);
		int id = idminiona(ev.getDamager());
		if (id != -1)
			Minion.mapa.get(id).u�yjNarz�dzia();
	}
	
	@EventHandler
	public void �mier�Miniona(EntityDeathEvent ev) {
		if (!w��czone) return;
		int id = idminiona(ev.getEntity());
		if (id != -1) {
			Minion minion = Minion.mapa.get(id);
			ev.getDrops().clear();
			ev.setDroppedExp(0);
			if (!wy��czanie) {
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
		if (!w��czone) return;
		if (ev.getEntity().getScoreboardTags().contains("mimiMinion"))
			ev.setCancelled(false);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void budowanie(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		if (!w��czone) return;
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		if (item == null || item.getType().equals(Material.AIR)) return;
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
		String nazwa = item.getItemMeta().getDisplayName();
		switch(nazwa) {
		case "�1�lMinion �1Kopacz�9":
		case "�1�lMinion �aFarmer�9":
		case "�1�lMinion �cRze�nik�9":
			break;		
		default:
			return;
		}
		Player p = ev.getPlayer();

		if (item.getItemMeta().getLore().size() > 5)
			switch (nazwa) {
			case "�1�lMinion �1Kopacz�9":
				new Kopacz(p, item);
				break;
			case "�1�lMinion �aFarmer�9":
				new Farmer(p, item);
				break;
			case "�1�lMinion �cRze�nik�9":
				new Rze�nik(p, item);
				break;	
			}
		else 
			switch(nazwa) {
				case "�1�lMinion �1Kopacz�9":
					new Kopacz(p.getLocation(), p.getName());
					break;
				case "�1�lMinion �aFarmer�9":
					new Farmer(p.getLocation(), p.getName());
					break;
				case "�1�lMinion �cRze�nik�9":
					new Rze�nik(p.getLocation(), p.getName());
					break;		
		}
		ev.setCancelled(true);
		item.setAmount(item.getAmount() - 1);
		p.getInventory().setItemInMainHand(item);
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupe�nijTabComplete(args, Arrays.asList("w��cz", "wy��cz", "Kopacz", "Rze�nik", "Farmer"));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		switch(args[0]) {
		case "w��cz":
			if (Miniony.w��czone)
				sender.sendMessage(Miniony.prefix + "Miniony s� ju� w��czone");
			else {
				Miniony.wczytajMiniony();
				sender.sendMessage(Miniony.prefix + "W��czono miniony");
			}
			return true;
		case "wy��cz":
			if (Miniony.w��czone) {
				Miniony.zapiszMiniony();
				sender.sendMessage(Miniony.prefix + "Wy��czono miniony");
			} else
				sender.sendMessage(Miniony.prefix + "Miniony s� ju� wy��czone");
			return true;
		}
		Player p;
		if (args.length >= 2) {
			p = Bukkit.getPlayer(args[1]);
			if (p == null || !p.isOnline())
				return Main.powiadom(sender, prefix + "Niepoprawna nazwa gracza: " + args[1]);
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
		case "Rze�nik":
			p.getInventory().addItem(itemRzeznik);
			break;
		}
		return true;
	}
}
