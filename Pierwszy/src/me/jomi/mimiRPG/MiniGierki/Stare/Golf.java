package me.jomi.mimiRPG.MiniGierki.Stare;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftChicken;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jomi.mimiRPG.Func;

public class Golf extends MiniGra{
	protected static HashMap<String, Chicken> mapaKur = new HashMap<>();
	protected ItemStack jajko = Func.stwórzItem(Material.EGG, 1, "&cReset piłki", Arrays.asList("Porzuca aktualną piłkę", "i oddaje kolejkę"));
	
	public Golf() {
		super("Golf", "Golf");
	}
	
	public void wyłącz() {
		super.wyłącz();
		if (!mapaKur.isEmpty())
			for (Chicken kura : mapaKur.values())
				if (kura != null)
					kura.remove();
	}
	public void przeładuj() {
		if (!mapaKur.isEmpty())
			for (Chicken kura : mapaKur.values())
				if (kura != null)
					kura.remove();
		super.przeładuj();
	}
	
	protected void zwycięstwo(Arena arena) {}

	public void zwyciestwo(Player p) {
		Bukkit.broadcastMessage(prefix +"§e" + p.getName() + " §6ukończył tor!");
		opuść(p, false);
	}
	
	protected ItemStack dajKij(int lvl) {
		ItemStack item = Func.stwórzItem(Material.STICK, 1, "&2&o&lGolfowy Patyczek &e"+lvl, Arrays.asList("Stworzony do gry w golfa", "Jeden z kurczaków to twoja piłka"));
		ItemMeta  meta = item.getItemMeta();
		meta.addEnchant(Enchantment.KNOCKBACK, lvl, true);
		item.setItemMeta(meta);
		return item;
	}
	public void start(Arena arena) {
		super.start(arena);
		ItemStack item  = dajKij(1);
		ItemStack item2 = dajKij(2);
		ItemStack item3 = dajKij(3);
		ItemStack item4 = dajKij(5);
		ItemStack item5 = dajKij(10);
		for (Player p : arena.gracze) {
			p.getInventory().setItem(1, item);
			p.getInventory().setItem(2, item2);
			p.getInventory().setItem(3, item3);
			p.getInventory().setItem(4, item4);
			p.getInventory().setItem(5, item5);
			p.getInventory().setItem(6, Func.stwórzItem(Material.ENDER_PEARL, 1, "&9Teleport do piłki", null));
			mapaKur.put(p.getName(), null);
			p.setAllowFlight(true);
			p.sendMessage(prefix + "Możesz latać");
		}
		następny(arena);
	}
	
	protected void zrespKure(Player p, Arena arena) {
		String nick = p.getName();
		if (mapaKur.get(nick) == null || mapaKur.get(nick).isDead()) {
			Chicken kura = (Chicken) arena.start.getWorld().spawnEntity(arena.start, EntityType.CHICKEN);
			kura.setCustomName("§6Piłka gracza " + nick);
			kura.setRemoveWhenFarAway(false);
			kura.setCustomNameVisible(true);
			kura.setCollidable(false);
			kura.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
			mapaKur.put(nick, kura);
		}
	}
	
	public void opuść(Player p, boolean komunikat) {
		Arena arena = arenaGracza(p);
		String nick = p.getName();
		int index = arena.znajdzGracza(p);
		if (arenaGracza(p).grane) {
			if (mapaKur.get(nick) != null)
				mapaKur.get(nick).remove();
			mapaKur.remove(nick);
		}
		super.opuść(p, komunikat);
		if (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE)) {
			p.setAllowFlight(false);
			p.sendMessage(prefix + "Nie możesz już latać");
		}
		arena.policzGłosy(this);
		if (arena.grane) {
			if (arena.gracze.size() <= 0) {
				arena.grane = false;
				return;
			}
			if (arena.zmienna >= index) {
				arena.zmienna -= 1;
				następny(arena);
			}
			if (arena.gracze.size() == 1) 
				opuść(arena.gracze.get(0), false);
		}
	}
	
	public void następny(Arena arena) {
		if (arena.gracze.size() <= 0)
			return;
		if (arena.gracze.size() == 1) {
			opuść(arena.gracze.get(0), false);
			return;
		}
		arena.zmienna += 1;
		if (arena.zmienna >= arena.gracze.size())
			arena.zmienna = 0;
		for (Player p : arena.gracze) {
			p.sendMessage(prefix + "Kolej gracza §e" + arena.gracze.get(arena.zmienna).getName());
			p.getInventory().setItem(0, new ItemStack(Material.AIR));
			p.getInventory().setItem(7, new ItemStack(Material.AIR));
		}
		arena.gracze.get(arena.zmienna).getInventory().setItem(7, jajko);
		zrespKure(arena.gracze.get(arena.zmienna), arena);
	}
	
	@EventHandler
	public void wejscieNaBlok(EntityCombustByBlockEvent ev) {
		if (mapaKur.containsValue(ev.getEntity()) && ev.getCombuster().getType().equals(Material.SOUL_FIRE)) {
			String nazwa = ev.getEntity().getName();
			String nick = nazwa.split(" ")[2];
				zwyciestwo(Bukkit.getPlayer(nick));
		}
	}
	
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		super.uderzenie(ev);
		if (!(ev.getDamager() instanceof Player)) return;
		Player p = (Player) ev.getDamager();
		Arena arena = arenaGracza(p);
		if (arena != null && arena.grane && ev.getEntity() instanceof CraftChicken && (ev.getEntity().getName()+" 1").split(" ")[0].equals(Func.koloruj("&6Piłka"))) {
			if (ev.getDamager().getName().equals(arena.gracze.get(arena.zmienna).getName()) && ev.getEntity().getName().split(" ")[2].equals(ev.getDamager().getName())) {
				ev.setDamage(0);
				następny(arena);
			}
			else {
				if (ev.getEntity().getName().split(" ")[2].equals(ev.getDamager().getName()))
					ev.getDamager().sendMessage(prefix + "to nie twoja kolej stary");
				else
					ev.getDamager().sendMessage(prefix + "to nie twoja piłka stary");
				ev.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		super.użycie(ev);
		Player p = ev.getPlayer();
		Arena arena = arenaGracza(p);
		if (arena == null || ev.getAction().toString().startsWith("LEFT")) return;
		ItemStack item = ev.getItem();
		if (item == null) return;
		switch (item.getType()) {
		case ENDER_PEARL:
			Chicken kura = mapaKur.get(p.getName());
			if (kura != null && !kura.isDead())
				p.teleport(kura);
			else
				p.sendMessage(prefix + "Twoja piłka aktuanie nie istnieje");
			ev.setCancelled(true);
			return;
		case EGG:
			int i = arena.znajdzGracza(p);
			if (i != arena.zmienna) return;
			mapaKur.get(p.getName()).remove();
			mapaKur.put(p.getName(), null);
			następny(arena);
			ev.setCancelled(true);
			return;
		default:
			return;
		}
	}
}
