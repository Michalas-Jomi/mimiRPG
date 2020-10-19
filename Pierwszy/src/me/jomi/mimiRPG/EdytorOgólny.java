package me.jomi.mimiRPG;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class EdytorOgólny<T extends ConfigurationSerializable> {
	Config config;
	String ścieżka;
	Class<T> clazz;
	String komenda;
	
	HashMap<String, Object> mapa = new HashMap<>();
	CommandSender sender;
	Player p;
	
	
	public EdytorOgólny(Config config, String ścieżkaWConfigu, String komenda, Class<T> clazz) {
		this.ścieżka = ścieżkaWConfigu;
		this.config = config;
		this.clazz = clazz;
		this.komenda = komenda;
		Main.log(config.wczytaj(ścieżkaWConfigu));// XXX
		try {
			mapa = (HashMap<String, Object>) skanuj(clazz);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	static Map<String, Object> skanuj(Class<?> clazz) throws Throwable {
		HashMap<String, Object> mapa = new HashMap<>();
		boolean dajMape = false;
		Object obj = null;
		try {
			obj = clazz.getConstructor(Map.class).newInstance(mapa);
		} catch (Throwable e) {}
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Mapowane.class)) {
				Class<?> _clazz = field.getType();
				dajMape = true;
				if (ConfigurationSerializable.class.isAssignableFrom(_clazz))
					mapa.put(field.getName(), skanuj(_clazz));
				else if (List.class.isAssignableFrom(field.getType()))
					mapa.put(field.getName(), Lists.newArrayList());
				else
					mapa.put(field.getName(), obj == null ? null : field.get(obj));
			}
		}
		if (dajMape)
			return mapa;
		return null;
	}
	
	public boolean onCommand(CommandSender sender, String[] args) {
		this.sender = sender;
		if (sender instanceof Player)
			p = (Player) sender;
		try {
			if (args[1].equals("-zatwierdz")) {
				zatwierdz();
				return true;
			}
			komenda(args);
		} catch (Throwable e) {
			e.printStackTrace(); // XXX
			sender.sendMessage("§cNie ingeruj w edytor");
		}
		edytor(mapa, 0, clazz.getSimpleName(), komenda + " edytor ").dodaj(new Napis("\n§a[zatwierdz]", "§bKliknij aby zatwierdzić", komenda + " edytor -zatwierdz")).dodaj("\n").wyświetl(sender);
		return true;
	}
	@SuppressWarnings("unchecked")
	void komenda(String[] args) throws Throwable {
		Object ost = null;
		Object obj = mapa;
		Class<?> klasa = clazz;
		Field field = null;
		for (int i=1; i < args.length; i++) {
			if (args[i].equals(">>")) {
				if (ost instanceof Map) {
					Map<String, Object> _mapa = (Map<String, Object>) ost;
					if (!_mapa.containsKey(args[i-1]))
						throw new Throwable();
					_mapa.put(args[i-1], konwertuj(klasa, args, i+1));
					
				} else {
					List<Object> lista = (List<Object>) ost;
					lista.set(Integer.parseInt(args[i-1]), konwertuj(klasa, args, i+1));
				}
				return;
			}
			if (args[i].equals("[]")) {
				List<Object> lista = (List<Object>) obj;
				if (args[i+1].equals("dodaj")) { // [] dodaj
					ParameterizedType parametry = (ParameterizedType) field.getGenericType();
					klasa = (Class<?>) parametry.getActualTypeArguments()[0];
					lista.add(skanuj(klasa));
				} else if (args[i+1].equals("usuń")) { // [] usuń <index>
					lista.remove(Integer.parseInt(args[i+2]));
				}
				return;
			}
			if (obj instanceof Map) {
				ost = obj;
				obj = ((Map<?, ?>) obj).get(args[i]);
				field = klasa.getDeclaredField(args[i]);
				klasa = field.getType();
			} else if (obj instanceof List) {
				ost = obj;
				obj = ((List<?>) obj).get(Integer.parseInt(args[i]));
				ParameterizedType parametry = (ParameterizedType) field.getGenericType();
				klasa = (Class<?>) parametry.getActualTypeArguments()[0];
			}
		}
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
		Main.warn("Nieprzewidziany typ w EdytorzeOgólnym: " + klasa.getSimpleName());
		return null;
	}

	
	@SuppressWarnings("unchecked")
	Napis element(Object obj, int poziom, String nazwa, String scieżka) {
		scieżka += nazwa + " ";
		if (obj instanceof Map)
			return edytor((Map<String, Object>) obj, poziom + 1, nazwa, scieżka);
		else if (obj instanceof List) {
			Napis n = new Napis();
			n.dodaj("§2[");
			int i=0;
			for (Object el : (List<Object>) obj)
				n.dodaj(element(el, poziom, "" + i, scieżka))
						.dodaj(new Napis("§c{X}", "§cKliknij aby usunąć\nelement z §4" + nazwa, scieżka + "[] usuń " + i++))
						.dodaj("§d, §e");
			n.dodaj("§2] ");
			n.dodaj(new Napis("§a[+]", "§bDodaj Element §3" + nazwa, scieżka + "[] dodaj"));
			return n;
		} else
			return new Napis("§e" + obj, "§bKliknij aby ustawić", scieżka + ">> ");
	}
	
	Napis edytor(Map<String, Object> mapa, int poziom, String nazwa, String scieżka) {
		StringBuilder nty = new StringBuilder("§0");
		for (int i=0; i<poziom; i++) nty.append('-'); nty.append("§6");
		String pref = nty.toString();
		
		Napis n = new Napis("\n" + pref + "§9" + nazwa);
		for (Entry<String, Object> en : mapa.entrySet()) {
			n.dodaj("\n");
			n.dodaj(pref).dodaj("§6" + en.getKey()).dodaj("§8: §e");
			Object val = en.getValue();
			n.dodaj(element(val, poziom, en.getKey(), scieżka));
		}
		
		return n;
	}
	
	void zatwierdz() throws Throwable {
		config.ustaw_zapisz(ścieżka, clazz.getConstructor(Map.class).newInstance(mapa));
	}
}











