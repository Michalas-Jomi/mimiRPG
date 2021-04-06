package me.jomi.mimiRPG.Edytory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Napis;


public class EdytorOgólny<T> {
	public final String komenda;
	public final Class<T> clazz;
	
	@SuppressWarnings("serial")
	public static class DomyślnyWyjątekException extends Error {}
	
	
	private final HashMap<String, EdytorOgólnyInst> mapa = new HashMap<>();
	
	
	// ścieżka : (główny obiekt, obiekt na ścieżce) -> napis
	final HashMap<String, BiFunction<T, String, Napis>> wyjątki = new HashMap<>();
	final List<BiConsumer<T, String>> listaDlaZatwierdzenia = Lists.newArrayList();
	final List<BiConsumer<T, EdytorOgólnyInst>> listaDlaZatwierdzenia2 = Lists.newArrayList();
	final List<BiConsumer<T, String>> listaDlaInit = Lists.newArrayList();
	final List<BiConsumer<T, T>> listaPoZatwierdz = Lists.newArrayList();
	
	public void zarejestrójWyjątek(String ścieżka, BiFunction<T, String, Napis> bif) {
		wyjątki.put(ścieżka.trim(), bif);
	}
	public void zarejestrujOnZatwierdz(BiConsumer<T, String> bic) {
		listaDlaZatwierdzenia.add(bic);
	}
	public void zarejestrujOnZatwierdzZEdytorem(BiConsumer<T, EdytorOgólnyInst> bic) {
		listaDlaZatwierdzenia2.add(bic);
	}
	public void zarejestrujOnInit(BiConsumer<T, String> bic) {
		listaDlaInit.add(bic);
	}
	public void zarejestrujPoZatwierdz(BiConsumer<T, T> bic) {
		listaPoZatwierdz.add(bic);
	}
	
	public EdytorOgólny(String komenda, Class<T> clazz) {
		if (!komenda.startsWith("/"))
			komenda = "/" + komenda;
		this.komenda = komenda;
		this.clazz = clazz;
	}
	
	public boolean maEdytor(CommandSender sender) {
		return mapa.containsKey(sender.getName());
	}

	public List<String> wymuśConfig_onTabComplete(Config config, CommandSender sender, String label, String[] args) {
		switch (args.length) {
		case 0:
		case 1:
			return Komenda.utab(args, "edytor");
		case 2:
			return Komenda.utab(args, "-t", "-u");
		default:
			return Komenda.utab(args, config.klucze());
		}
	}
	public boolean wymuśConfig_onCommand(String prefix, String config, CommandSender sender, String label, String args[]) {
		if (args.length <= 2 && !maEdytor(sender))
			return Func.powiadom(sender, prefix + "/" + label + " edytor -t <nazwa>");
		else if (args.length >= 2 && args[1].equals("-t"))
			args[2] = config + "|" + args[2];
		return onCommand(sender, label, args);
		
	}
	public boolean onCommand(CommandSender sender, String label, String args[]) {
		EdytorOgólnyInst edytor = mapa.get(sender.getName());
		
		try {
			if (args[1].equals("-u")) {
				mapa.remove(sender.getName());
				return Func.powiadom(sender, "§9Juz nic nie edytujesz");
			}
			else if (args[1].equals("-t")) {
				List<String> lista = Func.tnij(Func.listToString(args, 2), "|");
				String plik = lista.get(0);
				String ścieżka = lista.get(1);
				if (plik.endsWith(".yml"))
					plik = plik.substring(0, plik.length() - 4);
				if (!plik.isEmpty() && !ścieżka.isEmpty()) {
					edytor = new EdytorOgólnyInst(sender, new Config(plik), ścieżka);
					mapa.put(sender.getName(), edytor);
					Bukkit.dispatchCommand(sender, komenda.substring(1) + " edytor");
					return true;
				}
			}
			throw new Throwable();
		} catch (Throwable e) {
			if (edytor == null)
				return Func.powiadom(sender, "§9Aktualnie nie edytujesz nic\n§9użyj edytor -t <nazwapliku>|<śccieżka w pliku>\n"
						+ "§9np.: /" + label + " edytor -t configi/testy/test|test1.test2.inne testy.akt\n§9Aby zakończyć edycja"
								+ " bez zapisu wpisz /" + label + " edytor -u");
		}
		sender.sendMessage("\n\n\n\n\n§5\"" + edytor.ścieżka + "\" " + edytor.config.path());
		return edytor.onCommand(args);
	}
	
	public class EdytorOgólnyInst {
		public Config config;
		public String ścieżka;
	
		CommandSender sender;
		Player p;
		
		public T obiekt;
		private T kopiaObiektu;
		
		
		@SuppressWarnings("unchecked")
		public EdytorOgólnyInst(CommandSender sender, Config config, String ścieżkaWConfigu) {
			this.ścieżka = ścieżkaWConfigu;
			this.config = config;
	
			this.sender = sender;
			if (sender instanceof Player)
				this.p = (Player) sender;
			
			try {
				obiekt = (T) config.wczytaj(ścieżkaWConfigu);
			} catch (Throwable e) {}
			
			if (obiekt == null)
				try {
					obiekt = Func.utwórz(clazz);
					listaDlaInit.forEach(bic -> bic.accept(obiekt, ścieżka));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			
			kopiaObiektu = (T) ((Mapowany) obiekt).clone();
		}
		
		public boolean onCommand(String[] args) {
			try {
				if (args[1].equals("-zatwierdz")) {
					zatwierdz();
					return true;
				}
				komenda(args);
			} catch (ArrayIndexOutOfBoundsException e) {
				
			} catch (Throwable e) {
				e.printStackTrace();
				sender.sendMessage("§cNie ingeruj w edytor");
			}
			try {
				sender.sendMessage("§5" + clazz);
				edytor(obiekt, "§0", "edytor", komenda + " ", false).dodaj(
						new Napis("\n§a[zatwierdz]", "§bKliknij aby zatwierdzić", komenda + " edytor -zatwierdz")).dodaj("\n").wyświetl(sender);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return true;
		}
		@SuppressWarnings({ "unchecked" })
		void komenda(String[] args) throws Throwable {
			Object ost = null;
			Object obj = obiekt;
			Field field = null;
			
			for (int i=1; i < args.length; i++) {
				String klucz = args[i];
				if (args[i].equals(">>")) {
					if (ost instanceof List) {
						Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
						Object wartość = konwertuj(clazz, args, i+1);
						((List<Object>) ost).set(Integer.parseInt(args[i-1]), wartość);
					} else {
						Object wartość = konwertuj(field.getType(), args, i+1);
						field.set(ost, wartość);
					}
				} else if (args[i].equals("()")) {
					Class<?> rzutowana = Class.forName(args[i+1], false, EdytorOgólny.class.getClassLoader());
					if (ost instanceof List) {
						Object wartość = konwertuj(rzutowana, args, i+3);
						((List<Object>) ost).set(Integer.parseInt(args[i-1]), wartość);
					} else {
						Object wartość = konwertuj(rzutowana, args, i+3);
						field.set(ost, wartość);
					}
				} else if (args[i].equals("[]")) {
					if (args[i+1].equals("dodaj")) {
						Type x = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
						Class<?> clazz = ((Class<?>) (x instanceof Class ? x : ((TypeVariable<?>) x).getBounds()[0]));
						domyślna(clazz, ((List<Object>) field.get(ost))::add);
					} else if (args[i+1].equals("usuń")) {
						((List<Object>) field.get(ost)).remove(Integer.parseInt(args[i+2]));
					} else
						throw new Throwable();
				} else if (args[i].equals("<null>")) {
					Field f = field; Object _ost = ost;
					domyślna(field.getType(), o -> {try{f.set(_ost, o);}catch(Throwable e){}});
				} else {
					ost = obj;
					if (obj instanceof List) {
						obj = ((List<?>) obj).get(Integer.parseInt(klucz));
					} else {
						field = Func.dajField(obj.getClass(), klucz);
						field.setAccessible(true);
						obj = field.get(obj);
					}
					continue;
				}
				return;
			}
		}
		
		void domyślna(Class<?> clazz, Consumer<Object> consumer) throws Throwable {
			try {
				consumer.accept(domyślna(clazz));
			} catch (Throwable e1) {
				consumer.accept(Func.utwórz(clazz));
			}			
		}
		Object domyślna (Class<?> klasa) throws Throwable {
			switch (klasa.getSimpleName()) {
			case "Integer": 	return 0;
			case "Float": 		return 0f;
			case "Double": 		return 0d;
			case "String": 		return "-";
			case "Character": 	return '-';
			case "Boolean":		return false;
			case "Location": 	return p.getLocation();
			case "List":		return Lists.newArrayList();
			case "ItemStack": 	return p.getInventory().getItemInMainHand();
			}
			if (klasa.isEnum())
				return Func.losuj(Lists.newArrayList((Object[]) klasa.getMethod("values").invoke(null)));
			throw new Throwable();
		}
		Object konwertuj(Class<?> klasa, String[] args, int i) throws Throwable {
			if (klasa.isEnum()) 
				return Func.StringToEnum(klasa, args[i]);
			
			try {
				if (args[i].equals("null"))
					return null;
			} catch (ArrayIndexOutOfBoundsException e) {}
			
			switch (klasa.getSimpleName()) {
			case "int":
			case "Integer":		return Integer.parseInt(args[i]);
			case "double":
			case "Double":		return Double.parseDouble(args[i]);
			case "float":
			case "Float":		return Float.parseFloat(args[i]);
			case "boolean":
			case "Boolean":		return Boolean.parseBoolean(args[i]);
			case "String":		return Func.listToString(args, i);
			case "Location":	return p.getLocation();
			case "char":
			case "Character":	return args[i].charAt(0);
			case "ItemStack":	return p.getInventory().getItemInMainHand();
			case "List":		return Lists.newArrayList();
			}
			Main.warn("Nieprzewidziany typ w EdytorzeOgólnym przy konwerowaniu: " + klasa.getSimpleName());
			return null;
		}
	
		Napis edytor(Object objekt, String pref, String nazwa, String scieżka, boolean wLiście) throws Throwable {
			scieżka += nazwa + " ";

			List<String> lsc = Func.tnij(scieżka.trim(), " ");
			for (int i=0; i < lsc.size(); i++)
				if (Func.Int(lsc.get(i), -1) != -1)
					lsc.set(i, "<int>");
			String sc = String.join(" ", lsc);
			if (wyjątki.containsKey(sc))
				try {
					Napis n = wyjątki.get(sc).apply(obiekt, scieżka);
					if (n == null)
						return new Napis();
					return new Napis(pref).dodaj(n);
				} catch (DomyślnyWyjątekException e) {}
			
			if (objekt instanceof ConfigurationSerializable && !objekt.getClass().getName().startsWith("org.bukkit")) {
				pref += "-";
				Napis n = new Napis((wLiście ? "\n" : "")).dodaj(new Napis(pref, "§bKliknij aby ustawić null\n§3" + nazwa, scieżka + ">> null")).dodaj("§9" + nazwa);
				for (Field field : Func.dajFields(objekt.getClass())) {
					field.setAccessible(true);
					if (field.isAnnotationPresent(Mapowane.class)) {
						n.dodaj("\n");
						n.dodaj(edytor(field.get(objekt), pref, field.getName(), scieżka, false));
					}
				}
				return n;
			} else if (objekt instanceof List) {
				Napis n = new Napis().dodaj(new Napis(pref, "§bKliknij aby ustawić null\n§3" + nazwa, scieżka + ">> null")).dodaj("§6" + nazwa + "§8: §2[");
				int i = 0;
				for (Object obj : (List<?>) objekt) {
					n.dodaj(edytor(obj, pref, "" + i, scieżka, true));
					n.dodaj(new Napis("§c{X}", "§cKliknij aby usunąć element z listy §4" + nazwa, scieżka + "[] usuń " + i++));
					n.dodaj("§d, ");
				}
				n.dodaj("§2] ");
				n.dodaj(new Napis("§a[+]", "§bKliknij aby dodać element do listy §3" + nazwa, scieżka + "[] dodaj"));
				return n;
			} else {
				Napis n = new Napis();
				if (!wLiście)
					n.dodaj(new Napis(pref, "§bKliknij aby ustawić null\n§3" + nazwa, scieżka + ">> null")).dodaj("§6" + nazwa + "§8: ");
				if (objekt == null)
					n.dodaj(new Napis("§e" + objekt, "§bKliknij aby utworzyć", scieżka + "<null>"));
				else if (objekt instanceof ItemStack)
					n.dodaj(Napis.item((ItemStack) objekt).clickEvent(Action.RUN_COMMAND, scieżka + ">>"));
				else if (objekt instanceof Location)
					n.dodaj(new Napis("§e" + Func.locToString((Location) objekt), "§bKliknij aby ustawić", scieżka + ">>"));
				else if (objekt instanceof Boolean)
					n.dodaj(new Napis((Boolean) objekt ? "§aTak" : "§cNie", "§bKliknij aby zmienić", scieżka + ">> " + !((Boolean) objekt)));
				else
					n.dodaj(new Napis("§e" + objekt, "§bKliknij aby ustawić", scieżka + ">> "));
				return n;
			}
		}
			
		void zatwierdz() throws Throwable {
			listaDlaZatwierdzenia.forEach(cons -> cons.accept(obiekt, ścieżka));
			listaDlaZatwierdzenia2.forEach(cons -> cons.accept(obiekt, this));
			config.ustaw_zapisz(ścieżka, obiekt);
			listaPoZatwierdz.forEach(cons -> cons.accept(kopiaObiektu, obiekt));
			sender.sendMessage("Zapisano w " + ścieżka + " w " + config.path());
		}
	}

}

