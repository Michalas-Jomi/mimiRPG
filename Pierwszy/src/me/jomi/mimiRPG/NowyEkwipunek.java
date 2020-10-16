package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;


public class NowyEkwipunek {
	// nick <stara Lokacja, <exp, itemy>>
	static final HashMap<String, Krotka<Location, Krotka<Integer, Krotka<GameMode, List<ItemStack>>>>> mapa = new HashMap<>();
	
	public static void dajNowy(Player p) { dajNowy(p, null, null); }
	public static void dajNowy(Player p, Location gdzieTepnąć) { dajNowy(p, gdzieTepnąć, null); }
	public static void dajNowy(Player p, Location gdzieTepnąć, GameMode gm) {
		Inventory inv = p.getInventory();
		String nick = p.getName();

		if (mapa.containsKey(nick))
			wczytajStary(p);
		
		
		List<ItemStack> itemy = Lists.newArrayList();
		for (int i=0; i<9*4+5; i++) 
			itemy.add(inv.getItem(i));
		
		mapa.put(p.getName(),
				new Krotka<>(p.getLocation(),
						new Krotka<>(Poziom.policzCałyExp(p),
								new Krotka<>(p.getGameMode(), itemy))));
		
		inv.clear();
		p.setExp(0);
		p.setLevel(0);
		if (gdzieTepnąć != null)
			p.teleport(gdzieTepnąć);
		if (gm != null)
			p.setGameMode(gm);
	}
	
	public static void wczytajStary(Player p) {
		String nick = p.getName();
		Inventory inv = p.getInventory();
		Krotka<Location, Krotka<Integer, Krotka<GameMode, List<ItemStack>>>> krotka = mapa.get(nick);
		if (krotka == null) 
			return;

		inv.clear();
		for (int i=0; i<9*4+5; i++)
			inv.setItem(i, krotka.b.b.b.get(i));
		
		p.setGameMode(krotka.b.b.a);
		p.giveExp(krotka.b.a);	
		p.teleport(krotka.a);
		
		mapa.remove(nick);
	}
}
