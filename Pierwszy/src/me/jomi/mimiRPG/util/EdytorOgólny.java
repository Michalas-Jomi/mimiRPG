package me.jomi.mimiRPG.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import net.md_5.bungee.api.chat.ClickEvent.Action;


public class EdytorOgólny {
	String komenda;
	Class<?> clazz;
	
	private final HashMap<String, EdytorOgólnyInst> mapa = new HashMap<>();

	public EdytorOgólny(String komenda, Class<?> clazz) {
		if (!komenda.startsWith("/"))
			komenda = "/" + komenda;
		this.komenda = komenda;
		this.clazz = clazz;
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
	
	private class EdytorOgólnyInst {
		Config config;
		String ścieżka;
	
		CommandSender sender;
		Player p;
		
		Object obiekt;
	
	
		public EdytorOgólnyInst(CommandSender sender, Config config, String ścieżkaWConfigu) {
			this.ścieżka = ścieżkaWConfigu;
			this.config = config;
	
			this.sender = sender;
			if (sender instanceof Player)
				this.p = (Player) sender;
			
			try {
				obiekt = config.wczytaj(ścieżkaWConfigu);
			} catch (Throwable e) {}
			
			if (obiekt == null)
				try {
					obiekt = Func.utwórz(clazz);
				} catch (Throwable e) {}
		}
		
		public boolean onCommand(String[] args) {
			try {
				if (args[1].equals("-zatwierdz")) {
					zatwierdz();
					return true;
				}
				komenda(args);
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				
			} catch (Throwable e) {
				sender.sendMessage("§cNie ingeruj w edytor");
			}
			try {
				sender.sendMessage("§5" + clazz);
				edytor(obiekt, "§0", "edytor", komenda + " ", false).dodaj(new Napis("\n§a[zatwierdz]", "§bKliknij aby zatwierdzić", komenda + " edytor -zatwierdz")).dodaj("\n").wyświetl(sender);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return true;
		}
		@SuppressWarnings("unchecked")
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
				} else if (args[i].equals("[]")) {
					if (args[i+1].equals("dodaj")) {
						Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
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
						field = obj.getClass().getDeclaredField(klucz);
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
			case "Location": 	return p.getLocation();
			case "ItemStack": 	return p.getInventory().getItemInMainHand();
			}
			throw new Throwable();
		}
		Object konwertuj(Class<?> klasa, String[] args, int i) throws Throwable {
			if (klasa.isEnum()) 
				return klasa.getMethod("valueOf", String.class).invoke(null, args[i]);
			
			switch (klasa.getSimpleName()) {
			case "int":
			case "Integer":		return Integer.parseInt(args[i]);
			case "double":
			case "Double":		return Double.parseDouble(args[i]);
			case "float":
			case "Float":		return Float.parseFloat(args[i]);
			case "String":		return Func.listToString(args, i);
			case "Location":	return p.getLocation();
			case "char":
			case "Character":	return args[i].charAt(0);
			case "ItemStack":	return p.getInventory().getItemInMainHand();
			}
			Main.warn("Nieprzewidziany typ w EdytorzeOgólnym przy konwerowaniu: " + klasa.getSimpleName());
			return null;
		}
	
		Napis edytor(Object objekt, String pref, String nazwa, String scieżka, boolean wLiście) throws Throwable {
			scieżka += nazwa + " ";
			
			if (objekt instanceof ConfigurationSerializable && !objekt.getClass().getName().startsWith("org.bukkit")) {
				pref += "-";
				Napis n = new Napis((wLiście ? "\n" : "") + pref + "§9" + nazwa);
				for (Field field : objekt.getClass().getDeclaredFields()) {
					field.setAccessible(true);
					if (field.isAnnotationPresent(Mapowane.class)) {
						n.dodaj("\n");
						n.dodaj(edytor(field.get(objekt), pref, field.getName(), scieżka, false));
					}
				}	
				return n;
			} else if (objekt instanceof List) {
				Napis n = new Napis(pref + "§6" + nazwa + "§8: §2[");
				int i=0;
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
					n.dodaj(pref).dodaj("§6" + nazwa + "§8: ");
				if (objekt == null)
					n.dodaj(new Napis("§e" + objekt, "§bKliknij aby utworzyć", scieżka + "<null>"));
				else if (objekt instanceof ItemStack)
					n.dodaj(Napis.item((ItemStack) objekt).clickEvent(Action.RUN_COMMAND, scieżka + ">>"));
				else if (objekt instanceof Location)
					n.dodaj(new Napis("§e" + Func.locToString((Location) objekt), "§bKliknij aby ustawić", scieżka + ">>"));
				else
					n.dodaj(new Napis("§e" + objekt, "§bKliknij aby ustawić", scieżka + ">> "));
				return n;
			}
		}
			
		void zatwierdz() throws Throwable {
			config.ustaw_zapisz(ścieżka, obiekt);
			sender.sendMessage("Zapisano w " + ścieżka + " w " + config.path());
		}
	}
}

