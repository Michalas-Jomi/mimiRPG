package me.jomi.mimiRPG;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.util.Config;

public class Baza implements Listener {
	public static final Config config = new Config("configi/Baza");
	public static final HashMap<String, ItemStack> itemy = new HashMap<>();
	public static ConfigurationSection grupy;
	
	public Baza() {	
		przeładuj();
	}
	public static void przeładuj() {
		Main.ust.przeładuj();

		config.przeładuj();
									
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
		Config config = new Config("Customowe Itemy");
		for (String klucz : config.klucze(false))
			itemy.put(klucz, (ItemStack) config.wczytaj(klucz));
	}
	private static void wczytajGrupy() {
		grupy = config.sekcja("grupy");
	}

	
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Gracz.mapa.remove(ev.getPlayer().getName());
	}
}
