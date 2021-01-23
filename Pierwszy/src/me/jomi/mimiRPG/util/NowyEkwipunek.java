package me.jomi.mimiRPG.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.earth2me.essentials.User;
import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;


public class NowyEkwipunek {
	private static class Dane {
		final Collection<PotionEffect> efekty;
		final List<ItemStack> itemy;
		final Boolean vanish;
		final Location loc;
		final boolean lata;
		final GameMode gm;
		final int exp;

		//Krotka<Location, Krotka<Integer, Krotka<GameMode, List<ItemStack>>>>
		public Dane(Location loc, int exp, GameMode gm, boolean lata, Boolean vanish, List<ItemStack> itemy, Collection<PotionEffect> efekty) {
			this.vanish = vanish;
			this.efekty = efekty;
			this.itemy = itemy;
			this.lata = lata;
			this.exp = exp;
			this.loc = loc;
			this.gm = gm;
		}
	}
	// nick <stara Lokacja, <exp, itemy>>
	static final HashMap<String, Dane> mapa = new HashMap<>();
	
	public static void dajNowy(Player p)						{ dajNowy(p, null, null); }
	public static void dajNowy(Player p, Location gdzieTepnąć)	{ dajNowy(p, gdzieTepnąć, null); }
	public static void dajNowy(Player p, Location gdzieTepnąć, GameMode gm) {
		Inventory inv = p.getInventory();
		String nick = p.getName();

		if (mapa.containsKey(nick))
			wczytajStary(p);
		
		
		List<ItemStack> itemy = Lists.newArrayList();
		for (int i=0; i<9*4+5; i++) 
			itemy.add(inv.getItem(i));
		
		Boolean vanish = null;
		if (Main.essentials != null) {
			User user = Main.essentials.getUser(p);
			vanish = user.isVanished();
			user.setVanished(false);
		}
		
		mapa.put(p.getName(), new Dane(p.getLocation(), Poziom.policzCałyExp(p), p.getGameMode(), p.isFlying(), vanish, itemy, p.getActivePotionEffects()));
		
		p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
		
		p.setFlying(false);
		
		inv.clear();
		p.setExp(0);
		p.setLevel(0);
		if (gdzieTepnąć != null)
			p.teleport(gdzieTepnąć);
		if (gm != null)
			p.setGameMode(gm);
	}
	
	public static void wczytajStary(Player p) {
		Inventory inv = p.getInventory();
		Dane dane = mapa.get(p.getName());
		if (dane == null) 
			return;

		
		inv.clear();
		for (int i=0; i<9*4+5; i++)
			inv.setItem(i, dane.itemy.get(i));
		
		p.setGameMode(dane.gm);
		
		p.setFlying(dane.lata);// w razie gdyby inny plugin sprawdzał Player.isFlying to przy teleportacji
		p.teleport(dane.loc);
		p.setFlying(dane.lata);// w razie jakby inny plugin wyłączył przy teleportacji
		
		p.giveExp(dane.exp);	
		
		p.addPotionEffects(dane.efekty);
		
		if (dane.vanish != null)
			Main.essentials.getUser(p).setVanished(dane.vanish);

		
		mapa.remove(p.getName());
	}
}
