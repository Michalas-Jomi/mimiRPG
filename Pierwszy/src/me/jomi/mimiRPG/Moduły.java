package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

public class Moduły implements Przeładowalny {
	int włączone = 0;
	
	static final HashMap<String, Klasa> mapa = new HashMap<>();
	
	static final List<Class<?>> klasy = Lists.newArrayList();
	 
	public Moduły() {
		for (Class<?> clazz : Func.wszystkieKlasy())
			if (clazz.isAnnotationPresent(Moduł.class))
				klasy.add(clazz);
	}
	
	@Override
	public void przeładuj() {
		włącz(Main.ust.sekcja("Moduły"));
	}
	@Override
	public String raport() {
		return "§6Włączone Moduły: §e" + włączone + "§6/§e" + klasy.size();
	}
	
	public void włącz(ConfigurationSection sekcja) {
		włączone = 0;
		Consumer<Class<?>> włączModuł = klasa -> {
			boolean warunek;
			try {
				warunek = (boolean) klasa.getMethod("warunekModułu").invoke(null);
			} catch (NoSuchMethodException e) {
				warunek = true;
			} catch (Throwable e) {
				warunek = false;
			}
			
			if (warunek) {
				try {
					mapa.get(klasa.getSimpleName()).włącz();
					włączone++;
				} catch (Throwable e) {
					Main.error("Problem przy tworzeniu:", klasa.getSimpleName());
				}
			}
		};
		boolean przeładować = false;
		for (Class<?> klasa : klasy) {
			String nazwa = klasa.getSimpleName();
			if (!mapa.containsKey(nazwa))
				mapa.put(nazwa, new Klasa(klasa));
			Klasa _klasa = mapa.get(nazwa);
			boolean w = _klasa.włączony;
			if (sekcja.getBoolean(nazwa)) {
				włączModuł.accept(klasa);
				if (!w && _klasa.włączony && _klasa.inst instanceof Komenda)
					przeładować = true;
				if (!w && _klasa.włączony && Main.pluginEnabled)
					Main.log("§aWłączono Moduł: " + nazwa);
			} else {
				mapa.get(klasa.getSimpleName()).wyłącz();
				if (w && !_klasa.włączony && Main.pluginEnabled)
					Main.log("§cWyłączono Moduł: " + nazwa);
			}
		}
		if (przeładować)
			Bukkit.getServer().reloadData();
	}

	public static boolean włączony(String moduł) {
		Klasa klasa = mapa.get(moduł);
		return klasa == null ? false : klasa.włączony;
	}	
}

class Klasa {
	Class<?> klasa;
	Object inst;
	boolean włączony = false;
	Klasa(Class<?> klasa) {
		this.klasa = klasa;
	}
	
	Object włącz() throws InstantiationException, IllegalAccessException {
		if (włączony) return inst;
		if (inst == null)
			inst = klasa.newInstance();
		Main.zarejestruj(inst);
		włączony = true;
		return inst;
	}
	void wyłącz() {
		if (!włączony) return;
		if (inst != null)
			Main.wyrejestruj(inst);
		włączony = false;
	}
	
	public String toString() {
		return "Moduły.Klasa(" + klasa.getSimpleName() + ")~" + (włączony ? "Włączona" : "Wyłączona");
	}
}
