package me.jomi.mimiRPG.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;

public class MultiConfig {
	final Map<String, Config> mapaConfigów = new HashMap<>();
	final Map<String, Config> mapaNazwConfigów = new HashMap<>();
	final File dir;
	
	public MultiConfig(String ścieżka) {
		this(new File(Main.path + ścieżka));
	}
	public MultiConfig(File dir) {
		this.dir = dir;
		przeładuj();
	}
	
	
	public void przeładuj() {
		if (!dir.exists())
			dir.mkdirs();
		mapaConfigów.clear();
		mapaNazwConfigów.clear();
		wczytaj(dir);
	}
	private void wczytaj(File dir) {
		for (File file : dir.listFiles())
			if (file.isDirectory())
				wczytaj(file);
			else {
				Config config = new Config(file);
				config.klucze().forEach(klucz -> mapaConfigów.put(klucz, config));
				mapaNazwConfigów.put(file.getPath(), config);
			}
	}
	
	public Set<String> klucze() {
		return mapaConfigów.keySet();
	}
	public ConfigurationSection sekcja(String ścieżka) {
		return mapaConfigów.get(st(ścieżka)).sekcja(ścieżka);
	}
	
	public Object wczytaj(String ścieżka) {
		return mapaConfigów.get(st(ścieżka)).wczytaj(ścieżka);
	}
	public ItemStack wczytajItem(String ścieżka) {
		return Config.item(wczytaj(ścieżka));
	}
	
	
	public void ustaw(String ścieżka, Object obj) {
		Config config = mapaConfigów.get(st(ścieżka));
		if (config != null)
			config.ustaw(ścieżka, obj);
		else
			ustaw(ścieżka.replace('.', '/'), ścieżka, obj);
	}
	public void ustaw(String plik, String ścieżka, Object obj) {
		plik = plik.replace('\\', '/');
		Config config = mapaNazwConfigów.get(plik);
		if (config == null && !plik.endsWith(".yml"))
			config = mapaNazwConfigów.get(plik += ".yml");
		if (config == null)
			config = mapaNazwConfigów.get(plik = (Main.path + plik));
		if (config == null) {
			config = new Config(new File(plik));
			mapaNazwConfigów.put(plik, config);
			mapaConfigów.put(st(ścieżka), config);
		}
		config.ustaw(ścieżka, obj);
	}
	
	
	private String st(String ścieżka) {
		int i = ścieżka.indexOf('.');
		return i == -1 ? ścieżka : ścieżka.substring(0, i);
	}
}
