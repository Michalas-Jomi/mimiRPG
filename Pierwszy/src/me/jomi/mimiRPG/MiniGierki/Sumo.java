package me.jomi.mimiRPG.MiniGierki;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jomi.mimiRPG.Func;

public class Sumo extends MiniGra{

	public Sumo() {
		super("Sumo", "Sumo");
	}

	protected void start(Arena arena) {
		super.start(arena);
		ItemStack patyk = Func.stwórzItem(Material.STICK, 1, "&2Sumo Patyk", Arrays.asList("Paryk stworzony do walki sumo"));
		ItemMeta meta = patyk.getItemMeta();
		meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
		patyk.setItemMeta(meta);
		for (Player p : arena.gracze) {
			p.getInventory().setItem(4, patyk);
		}
	}
	
	protected void zwycięstwo(Arena arena) {
		if (arena.gracze.size() != 1) return;
		Bukkit.broadcastMessage(prefix + "§e" + arena.gracze.get(0).getName() + "§6 wygrywa rozgrywkę!");
		opuść(arena.gracze.get(0), false);
		arena.grane = false;
	}

	@EventHandler
	public void przegrana(EntityDamageByBlockEvent ev) {
		if (!(ev.getEntity() instanceof Player)) return;
		super.palenie(ev);
		Player p = (Player) ev.getEntity();
		Arena arena = arenaGracza(p);
		if (arena != null && arena.grane)
			opuść(p, true);
	}
	
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (!(ev.getEntity() instanceof Player)) return;
		Player p = (Player) ev.getEntity();
		Arena arena = arenaGracza(p);
		if (arena != null && arena.grane)
			ev.setDamage(.1);
	}
	@EventHandler
	public void otrzymanieDmg(EntityDamageEvent ev) {
		return;
	}
}
