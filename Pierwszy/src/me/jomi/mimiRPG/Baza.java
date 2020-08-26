package me.jomi.mimiRPG;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

public class Baza{
	public static final HashMap<String, ItemStack> itemy = new HashMap<>();
	
	public Baza() {	
		prze³aduj();
	}
	public static void prze³aduj() {
		Main.ust.prze³aduj();
		
		wczytajCustomoweItemy();
	}

	private static void wczytajCustomoweItemy() {
		itemy.clear();
		Config config = new Config("Customowe Itemy", "CustomowyDrop/Customowe Itemy");
		for (String klucz : config.klucze(false))
			itemy.put(klucz, (ItemStack) config.wczytaj(klucz));
	}
}
