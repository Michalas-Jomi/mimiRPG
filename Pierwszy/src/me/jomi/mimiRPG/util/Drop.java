package me.jomi.mimiRPG.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;

public class Drop implements ConfigurationSerializable, Cloneable {
	public List<Drop> drop;
	public boolean tylkoJeden = false;
	
	public ItemStack item;
	
	public int rolle = 1;
	public double szansa = 1;
	
	public int min_ilość = -1;
	public int max_ilość = -1;
	
	// item
	// item szansa
	// item szansa ilość
	// item szansa ilość xrolle
	// item szansa min_ilość-max_ilość
	// item szansa min_ilość-max_ilość xrolle
	// mat
	// mat szansa
	// ...
	// drop
	// drop szansa
	// ...
	public static Drop wczytaj(String nazwa) {
		String[] części = nazwa.split(" ");
		Drop drop = Baza.dropy.get(części[0]);
		if (drop == null)
			drop = new Drop(Config.item(części[0]));
		drop = drop.clone();
		switch (części.length) {
		default:
			Main.warn("Niepoprawna ilość parametrów dropu " + nazwa + " pomijanie nadwyżkowych");
		case 4:
			if ((drop.rolle = Func.Int(części[3], -1)) <= 0) {
				Main.warn(String.format("Niepoprawna liczba rolli drop %s \"%s\", rolle muszą być liczbą dodatnią", nazwa.split(" ")[3], nazwa));
				drop.rolle = 1;
			}
		case 3:
			String[] minmax = części[2].split("-");
			drop.min_ilość = Func.Int(minmax[0], -1);
			drop.max_ilość = minmax.length > 1 ? Func.Int(minmax[1], -1) : drop.min_ilość;
		case 2:
			int mn = 1;
			if (części[1].endsWith("%")) {
				części[1] = części[1].substring(0, części[1].length() - 1);
				mn = 100;
			}
			if ((drop.szansa = Func.Double(części[1], -1) / mn) < 0) {
				Main.warn(String.format("Niepoprawna szansa dropu %s \"%s\", przyjmowanie szansa = 1.0 (100%)", nazwa.split(" ")[1], nazwa));
				drop.szansa = 1;
			}
		case 1:
			return drop;
		}
	}
	public Drop(ItemStack item) {
		this.item = item;
	}
	@SuppressWarnings("unchecked")
	public Drop(Map<String, Object> mapa) {
		tylkoJeden = (boolean) mapa.getOrDefault("tylko jeden", false);
		max_ilość = (int) mapa.getOrDefault("max ilość", -1);
		min_ilość = (int) mapa.getOrDefault("min ilość", -1);
		szansa = Func.Double(mapa.getOrDefault("szansa", 1));
		rolle = (int) mapa.getOrDefault("rolle", 1);
		if ((item = Config.item(mapa.get("item"))) == null)
			Func.wykonajDlaNieNull((List<Object>) mapa.get("drop"), dropy -> {
				drop = Lists.newArrayList();
				dropy.forEach(drop -> this.drop.add(Config.drop(drop)));
			});
	}
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> mapa = new HashMap<>();
		if (tylkoJeden) 		mapa.put("tylko jeden", tylkoJeden);
		if (max_ilość != -1) 	mapa.put("max ilość", max_ilość);
		if (min_ilość != -1) 	mapa.put("min ilość", min_ilość);
		if (szansa < 1)			mapa.put("szansa", szansa);
		if (rolle != 1)			mapa.put("rolle", rolle);
		if (item != null) 		mapa.put("item", Config._item(item));
		else 					mapa.put("drop", drop);
		return mapa;
	}
	
	
	public boolean dropnij(Player p) {
		boolean w = false;
		for (int i=0; i<rolle; i++) {
			boolean ww = w = Func.losuj(szansa);
			w = w || ww;
			if (ww)
				if (item != null)
					Func.dajItem(p, item);
				else
					for (Drop drop : this.drop)
						if (drop.dropnij(p) && tylkoJeden)
							break;
		}
		return w;
	}
	
	@Override
	public Drop clone() {
		Drop drop = new Drop(item);
		drop.drop = this.drop;
		drop.max_ilość = max_ilość;
		drop.min_ilość = min_ilość;
		drop.szansa = szansa;
		drop.rolle = rolle;
		drop.tylkoJeden = tylkoJeden;
		return drop;
	}
	
	/*
	 * nazwa:
	 * 	 ==: Drop
	 *   drop:
	 *   - szansa: .1
	 *     drop: stone
	 *   - drop:
	 *     - cobblestone
	 *     - iron_ingot
	 *   - drop3
	 *   tylkoJeden: true
	 * 
	 * 
	 * 
	 * 
	 */
}
