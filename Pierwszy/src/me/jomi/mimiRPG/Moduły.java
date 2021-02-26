package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

public class Moduły implements Przeładowalny {
	static class Klasa {
		Class<?> klasa;
		Object inst;
		boolean włączony = false;
		Klasa(Class<?> klasa) {
			this.klasa = klasa;
		}
		
		Object włącz() throws InstantiationException, IllegalAccessException {
			if (włączony) return inst;
			if (inst == null)
				inst = Func.nowaInstancja(klasa);
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
		
		@Override
		public String toString() {
			return "Moduły.Klasa(" + klasa.getSimpleName() + ")~" + (włączony ? "Włączona" : "Wyłączona");
		}
	}
	
	int włączone = 0;
	
	static final List<Class<?>> klasy = Lists.newArrayList();
	static final HashMap<String, Klasa> mapa = new HashMap<>();
	
	public Moduły() {
		List<List<Class<?>>> klasy = Lists.newArrayList();
		for (int i=0; i < 5; i++)
			klasy.add(Lists.newArrayList());
		
		for (Class<?> clazz : Func.wszystkieKlasy())
			if (clazz.isAnnotationPresent(Moduł.class))
				klasy.get(clazz.getDeclaredAnnotation(Moduł.class).priorytet().poziom).add(clazz);
		
		for (List<Class<?>> subKlasy : klasy)
			subKlasy.forEach(Moduły.klasy::add);
	}
	
	@Override
	public void przeładuj() {
		włącz(Main.ust.sekcja("Moduły"));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Włączone Moduły", włączone + "/" + klasy.size());
	}
	
	public static String moduły() {
		StringBuilder strB1 = new StringBuilder();
		StringBuilder strB2 = new StringBuilder();
		
		mapa.values().forEach(klasa -> (klasa.włączony ? strB1 : strB2).append("§r, §").append(klasa.włączony ? 'a' : 'c').append(klasa.klasa.getSimpleName()));
		
		return  "§6Wyłączone Moduły§8: " + (strB2.toString().isEmpty() ? "§4Brak" : strB2.substring(4)) + "\n" +
				"§6Włączone Moduły§8: "  + (strB1.toString().isEmpty() ? "§4Brak" : strB1.substring(4));
	}
	
	void włączModuł(Class<?> klasa) {
		String nazwa = klasa.getSimpleName();
		if (!mapa.containsKey(nazwa))
			mapa.put(nazwa, new Klasa(klasa));
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
				e.printStackTrace();
			}
		}
	}
	public void włącz(ConfigurationSection sekcja) {
		włączone = 0;
		boolean przeładować = false;
		for (Class<?> klasa : klasy) {
			String nazwa = klasa.getSimpleName();
			if (!mapa.containsKey(nazwa))
				mapa.put(nazwa, new Klasa(klasa));
			Klasa _klasa = mapa.get(nazwa);
			boolean w = _klasa.włączony;
			if (sekcja.getBoolean(nazwa)) {
				włączModuł(klasa);
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
		if (przeładować && Main.pluginEnabled)
			Main.reloadBukkitData();
	}

	public static boolean włączony(String moduł) {
		Klasa klasa = mapa.get(moduł);
		return klasa == null ? false : klasa.włączony;
	}	
}
