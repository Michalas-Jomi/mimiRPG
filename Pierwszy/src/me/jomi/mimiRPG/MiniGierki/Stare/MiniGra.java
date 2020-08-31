package me.jomi.mimiRPG.MiniGierki.Stare;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Kolejka;
import me.jomi.mimiRPG.Main;

public abstract class MiniGra implements Listener{
	protected ItemStack Znie   = Func.stwórzItem(Material.RED_CONCRETE,  1, "&cNie oddałeś głosu na rozpoczęcie gry", null);
	protected ItemStack Ztak   = Func.stwórzItem(Material.LIME_CONCRETE, 1, "&2Oddałeś głos na rozpoczęcie gry", 	  null);
	protected ItemStack Gtak   = Func.stwórzItem(Material.BOOK, 		 1, "&2Zagłosuj na rozpoczęcie gry", 		  null);
	protected ItemStack Iopuść = Func.stwórzItem(Material.BARRIER, 		 1, "&4Opuść rozgrywkę", 					  null);
	protected ItemStack Gnie   = Func.stwórzItem(Material.BOOK, 		 1, "&cWycofaj swój głos", 					  null);
	public static Config config = new Config("configi/Mini Gry");
	protected Kolejka<Arena> areny = new Kolejka<>();
	private static int maxIlośćAren;
	protected HashMap<String, Arena> mapa = new HashMap<>();
	
	protected HashMap<String, Long> czekanie = new HashMap<String, Long>();
	
	public String prefix;
	public String nazwa;
	
	public MiniGra(String Prefix, String Nazwa) {
		Main.minigry.put(Nazwa, this);
		prefix = Func.prefix(Prefix);
		nazwa = Nazwa;
		Main.dodajPermisje("minigra." + nazwa);
		
		ItemMeta meta = Gnie.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		Gnie.setItemMeta(meta);
		
		config.ustaw_zapiszDomyślne("maxIlośćAren", 10);
	}
	
	public void wyłącz() {
		for (Arena arena : areny.obiekty)
			for (Player p : arena.gracze) {
				NowyEkwipunek.wczytajStary(p);
				p.teleport(arena.koniec);
				p.sendMessage(prefix + "Wyłączanie pluginu");
				for (PotionEffect ef: p.getActivePotionEffects())
					p.removePotionEffect(ef.getType());
			}
	}
	
	protected abstract void zwycięstwo(Arena arena);
	
	public void przeładuj() {
		for (Arena arena : areny.obiekty) {
			arena.grane = false;
			if (arena.gracze != null) {
				List<Player> pp = Lists.newArrayList();
				for (Player p : arena.gracze)
					pp.add(p);
				for (Player p : pp) {
					opuść(p, false);
					p.sendMessage(prefix + "Przeładowywanie pluginu");
				}
			}
		}
		areny.obiekty.clear();
		
		maxIlośćAren = (int) config.wczytajLubDomyślna("maxIlośćAren", 5);
		
		
		for (int i=1; i <= maxIlośćAren; i++) {
			Arena a = Arena.wczytaj(nazwa + "." + i, config);
			if (a != null)
				areny.dodaj(a);
		}
		if (areny.obiekty.size() <= 0)
			powiadomOp(prefix + "§c Nie ustawiono żadnej areny, lub plik jest niepoprawny");
		Main.log(prefix + "§aWczytano§e " + areny.obiekty.size() + " §aaren do minigry §e" + nazwa);
	}
	protected Arena arenaGracza(Player p) {
		return mapa.get(p.getName());
	}
	
	public boolean dolacz(Player p) {
		if (!p.hasPermission("mimiRPG.minigra." + nazwa)) {
			p.sendMessage(prefix + "Nie masz dostępu do tej minigierki");
			return true;
		}
		Arena arena = areny.obiekty.get(0);
		if (arena.grane) {
			p.sendMessage(prefix + "Aktualnie trwa mecz");
			return true;
		}
		if (arena.gracze.contains(p)) {
	        if(czekanie.containsKey(p.getName())) {
	            long czas = ((czekanie.get(p.getName())/1000) + 3) - (System.currentTimeMillis()/1000);
	            if (czas > 1) {
	                p.sendMessage(prefix + "Nie możesz zmienić głosu jeszcze przez §e" + czas + "§6 sekundy");
	                return true;
	            }
	            if(czas > 0) {
	                p.sendMessage(prefix + "Nie możesz zmienić głosu jeszcze przez sekundę");
	                return true;
	            }
	        }
	        czekanie.put(p.getName(), System.currentTimeMillis());
			głosuj(p);
			return true;
		}
		arena.gracze.add(p);
		arena.głosy.add(false);
		mapa.put(p.getName(), arena);
		for (Player gracz : arena.gracze)
			gracz.sendMessage(prefix + "Gracz §e" + p.getName() + "§6 dołącza do gry");
		NowyEkwipunek.dajNowy(p);
		p.getInventory().setHelmet(Znie);
		p.getInventory().setItem(4, Gtak);
		p.teleport(arena.start);
		p.getInventory().setItem(8, Iopuść);
		return false;
	}
	public void opuść(Player p, boolean komunikat) {
		Arena arena = arenaGracza(p);
		int i = arena.znajdzGracza(p);
		if (komunikat && arena.grane)
			for (Player gracz : arena.gracze)
				gracz.sendMessage(prefix + "gracza §e" + p.getName() + "§6 zmiotło z planszy");
		else if (komunikat && !arena.grane)
			for (Player gracz : arena.gracze)
				gracz.sendMessage(prefix + "gracz §e" + p.getName() + "§6 opuścił grę");
		for (Player gracz : arena.gracze)
			for (PotionEffect ef: gracz.getActivePotionEffects())
				gracz.removePotionEffect(ef.getType());
		arena.głosy.remove(i);
		arena.gracze.remove(i);
		mapa.remove(p.getName());
		NowyEkwipunek.wczytajStary(p);
		p.sendMessage(prefix + "Opusciłeś grę");
		p.teleport(arena.koniec);
		p.playSound(p.getLocation(), Sound.BLOCK_COMPOSTER_READY, .2f, 1);
		if (arena.grane)
			zwycięstwo(arena);
		else
			arena.policzGłosy(this);
	}
	protected void start(Arena arena) {
		PotionEffect saturacja = new PotionEffect(PotionEffectType.SATURATION, 40000, 20, false, false, false);
		for (Player p : arena.gracze) {
			for (PotionEffect ef: p.getActivePotionEffects())
				p.removePotionEffect(ef.getType());
			p.teleport(arena.start);
			p.getInventory().clear();
			p.getInventory().setItem(8, Iopuść);
			p.sendMessage(prefix + "Zaczynamy rozgrywkę");
			p.addPotionEffect(saturacja);
			p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, .4f, 1);
			p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_WORK, .6f, 1);
			p.setGameMode(GameMode.ADVENTURE);
		}
		arena.grane = true;
		areny.następny_wróć();
	}
	protected void głosuj(Player p) {
		Arena arena = arenaGracza(p);
		int i = arena.znajdzGracza(p);
		arena.głosy.set(i, !arena.głosy.get(i));
		if (arena.głosy.get(i)) {
			p.sendMessage(prefix + "Zagłosowano na rozpoczącie gry");
			p.getInventory().setItem(4, Gnie);
			p.getInventory().setHelmet(Ztak);
		} else {
			p.sendMessage(prefix + "Anulowano głos na rozpoczęcie gry");
			p.getInventory().setItem(4, Gtak);
			p.getInventory().setHelmet(Znie);
		}
		arena.policzGłosy(this);
	}
	public void ustawLokacje(Player p, String args[]) {
		String sciezka = args[1];
		if (args.length < 3) {
			p.sendMessage(prefix + "/minigraustaw <minigra> <lokacja> <numer>");
			return;
		}
		String nr = args[2];
		if (!(sciezka.equals("start") || sciezka.equals("koniec"))) {
			p.sendMessage(prefix + "Niepoprawna nazwa lokacji: §e" + sciezka);
			return;
		}
		config.ustaw_zapisz(nazwa + "." + nr + "." + sciezka, p.getLocation());
		p.sendMessage(prefix + "Ustawiono lokacje §e" + sciezka + "§6 aby działała poprawnie nalezy użyć jeszcze §e/przeladuj");
	}
	protected void powiadom(String msg, List<Player> ludzie) {
		for (Player p : ludzie)
			p.sendMessage(prefix + msg);
	}
	public static void powiadomOp(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
		for (Player gracz : Bukkit.getOnlinePlayers())
			if (gracz.hasPermission("mimiRPG.powiadomienia"))
				gracz.sendMessage(msg);
	}
	
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Player p = ev.getPlayer();
		Arena arena = arenaGracza(p);
		if (arena != null)
			opuść(p, true);
	}
	@EventHandler
	public void klikanieWEq(InventoryClickEvent ev) {
		Player p = (Player) ev.getWhoClicked();
		if (!p.hasPermission("mimiRPG.minigry.komendy") && arenaGracza(p) != null)
			ev.setCancelled(true);
	}
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();
		Arena arena = arenaGracza(p);
		if (arena == null || ev.getAction().toString().startsWith("LEFT")) return;
		ItemStack item = ev.getItem();
		if (item == null) return;
		switch (item.getType()) {
		case BARRIER:
			opuść(p, true);
			ev.setCancelled(true);
			p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.4f, 1);
			break;
		case BOOK:
			dolacz(p);
			ev.setCancelled(true);
			break;
		default:
			break;	
		}
	}
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		Arena arena = arenaGracza(ev.getPlayer());
		if (arena != null) {
			String komenda = ev.getMessage().split(" ")[0];
			if (komenda.equalsIgnoreCase("/tc")) {
				ev.setCancelled(true);
				if (ev.getMessage().split(" ").length == 1) {
					ev.getPlayer().sendMessage(prefix + "/tc <wiadomość>");
				}
				powiadom("§b" + ev.getPlayer().getName() + "§7: §r" + Func.listToString(ev.getMessage().split(" "), 1), arena.gracze);
				return;
			}
			if (komenda.equalsIgnoreCase("/msg") || komenda.equalsIgnoreCase("/r") || ev.getPlayer().hasPermission("mimiRPG.minigry.komendy")) return;
			ev.getPlayer().sendMessage(prefix + "§cW minigrach można używać tylko komend /msg /r /tc");
			ev.setCancelled(true);
			Bukkit.getConsoleSender().sendMessage(prefix + "Anulowano graczowi §e" + ev.getPlayer().getName() + "§6 komendę: §e" + ev.getMessage());
		}
	}
	@EventHandler
	public void wyrzucanieItemów(PlayerDropItemEvent ev) {
		if (!ev.getPlayer().hasPermission("mimiRPG.minigry.komendy") && arenaGracza(ev.getPlayer()) != null)
			ev.setCancelled(true);
	}
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (ev.getEntity() instanceof Player)
			if (mapa.containsKey(ev.getEntity().getName()))
				ev.setCancelled(true);
	}
	@EventHandler
	public void palenie(EntityDamageByBlockEvent ev) {
		if (ev.getEntity() instanceof Player)
			if (mapa.containsKey(ev.getEntity().getName()))
				ev.setCancelled(true);
	}
	@EventHandler
	public void otrzymanieDmg(EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player)
			if (mapa.containsKey(ev.getEntity().getName()))
				ev.setCancelled(true);
	}
}
