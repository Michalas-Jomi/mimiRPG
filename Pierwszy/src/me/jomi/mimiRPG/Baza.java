package me.jomi.mimiRPG;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Baza{
	public static final Config config = new Config("configi/Baza");
	public static final HashMap<String, ItemStack> itemy = new HashMap<>();
	public static ConfigurationSection grupy;
	
	public Baza() {	
		prze�aduj();
	}
	public static void prze�aduj() {
		Main.ust.prze�aduj();

		config.prze�aduj();
									
		wczytajCustomoweItemy();
		wczytajGrupy();
	}

	public static Grupa grupa(String nazwa) {
		Object obj = grupy.get(nazwa);
		if (obj == null)
			return new Grupa();
		return (Grupa) obj;
	}
	
	private static void wczytajCustomoweItemy() {
		itemy.clear();
		Config config = new Config("Customowe Itemy", "CustomowyDrop/Customowe Itemy");
		for (String klucz : config.klucze(false))
			itemy.put(klucz, (ItemStack) config.wczytaj(klucz));
	}
	private static void wczytajGrupy() {
		grupy = config.sekcja("grupy");
		// TODO doda� config do wyjecia z grupy: {}
		
	}
}
