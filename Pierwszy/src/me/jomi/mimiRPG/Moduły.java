package me.jomi.mimiRPG;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

public class Moduły implements Przeładowalny {
	@Target(value=ElementType.TYPE)
	@Retention(value=RetentionPolicy.RUNTIME)
	public static @interface Moduł {
		public Priorytet priorytet() default Priorytet.NORMALNY;
		public enum Priorytet {
			NAJWYŻSZY(0),
			WYSOKI(1),
			NORMALNY(2),
			NISKI(3),
			NAJNIŻSZY(4);

			int poziom;
			Priorytet(int poziom) {
				this.poziom = poziom;
			}
		}
	}
	
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
				try (Timming timming = new Timming("Włączany Moduł " + klasa.getSimpleName())) {
					inst = Func.nowaInstancja(klasa);
				}
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
	
	public static String moduły() {
		List<Klasa> moduły = new ArrayList<>();
		
		moduły.addAll(mapa.values());
		
		Func.posortuj(moduły, k -> {
			double w = 0;
			double dzielnik = 1;
			for (char znak : Func.odpolszcz(k.klasa.getSimpleName()).toCharArray()) {
				w += znak / dzielnik;
				dzielnik *= znak;
			}
			return w;
		});
		
		StringBuilder strB = new StringBuilder();
		moduły.forEach(moduł -> strB.append('§').append(moduł.włączony ? 'a' : 'c').append(moduł.klasa.getSimpleName()).append("§r, "));
		
		String w = strB.toString();
		return w.substring(0, w.length() - 2);
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
		for (Class<?> klasa : klasy) {
			String nazwa = klasa.getSimpleName();
			if (!mapa.containsKey(nazwa))
				mapa.put(nazwa, new Klasa(klasa));
			Klasa _klasa = mapa.get(nazwa);
			boolean w = _klasa.włączony;
			if (sekcja.getBoolean(nazwa)) {
				włączModuł(klasa);
				if (!w && _klasa.włączony && Main.pluginEnabled)
					Main.log("§aWłączono Moduł: " + nazwa);
			} else {
				mapa.get(klasa.getSimpleName()).wyłącz();
				if (w && !_klasa.włączony && Main.pluginEnabled)
					Main.log("§cWyłączono Moduł: " + nazwa);
			}
		}
	}

	public static boolean włączony(String moduł) {
		Klasa klasa = mapa.get(moduł);
		return klasa == null ? false : klasa.włączony;
	}	


	@Override
	public void przeładuj() {
		włącz(Main.ust.sekcja("Moduły"));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Włączone Moduły", włączone + "/" + klasy.size());
	}
}
