package me.jomi.mimiRPG.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;

public abstract class Func {
	public static String prefix(String nazwa) {
		return "§2[§a" + nazwa + "§2]§6 ";
	}
	public static String msg(String tekst, Object... uzupełnienia) {
		for (Object u : uzupełnienia)
			tekst = tekst.replaceFirst("%s", "§e" + u + "§6");
		return "§6" + tekst + "§6";
	}
	// podręczny raport
	public static Krotka<String, Object> r(String info, Object stan) {
		return new Krotka<>(info, stan);
	}
	
	public static void napisz(String komu, String co) {
		Player p = Bukkit.getPlayer(komu);
		if (p != null)
			p.sendMessage(co);
	}
	
	public static String odpolszcz(String text) {
		char[] znaki = text.toLowerCase().toCharArray();
		int i = 0;
		for (char c : znaki) {
			switch(c) {
			case 'ą': c = 'a'; break;
			case 'ć': c = 'c'; break;
			case 'ę': c = 'e'; break;
			case 'ł': c = 'l'; break;
			case 'ń': c = 'n'; break;
			case 'ó': c = 'o'; break;
			case 'ś': c = 's'; break;
			case 'ź': c = 'z'; break;
			case 'ż': c = 'z'; break;
			}
			znaki[i++] = c;
		}
		return new String(znaki);
	}
	public static String inicjały(String text) {
		text = text.trim();
		if (text.isEmpty()) return text;
		StringBuilder s = new StringBuilder();
		
		s.append(text.charAt(0));
		
		boolean b = false;
		for (char c : text.toCharArray())
			if (c == ' ')
				b = true;
			else if (b) {
				s.append(c);
				b = false;
			}
		
		return s.toString();
	}
	
	public static long czasSekundy() {
		return System.currentTimeMillis() / 1000;
	}

	public static String DoubleToString(double liczba) {
		String całości = IntToString((int) liczba);
		double r = liczba - (int) liczba;
		String reszta = "";
		if (r != 0)
			if ((""+r).length()>5) {
				reszta += (""+r).substring(1, 5);
				while (reszta.endsWith("0"))
					reszta = reszta.substring(0, reszta.length()-1);
			}
			else
				reszta += (""+r).substring(1);
		return całości + reszta;
	}
	public static List<String> BooleanToString(boolean bool, List<String> tak, List<String> nie) {
		return bool ? tak : nie;
	}
	public static String BooleanToString(boolean bool, String tak, String nie) {
		return bool ? tak : nie;
	}
	public static String IntToString(int liczba) {
		String r = "";
		for (char x : ("" + liczba).toCharArray())
			r = x + r;
		String w = "";
		int i = 0;
		for (char x : r.toCharArray()) {
			w += x;
			i ++;
			if (i >= 3) {
				w += ",";
				i = 0;
			}
		}
		r = "";
		for (char x : w.toCharArray())
			r = x + r;
		if (r.startsWith(","))
			return r.substring(1);
		return r;
	}
	public static String listToString(Object[] lista, int start) {
		return listToString(Lists.newArrayList(lista), start, " ");
	}
	public static String listToString(Object[] lista) {
		return listToString(Lists.newArrayList(lista), 0, " ");
	}
	public static String listToString(Object[] lista, String wstawka) {
		return listToString(Lists.newArrayList(lista), 0, wstawka);
	}
	public static String listToString(Object[] lista, int start, String wstawka) {
		return listToString(Lists.newArrayList(lista), start, wstawka);
	}
	public static String listToString(Iterable<?> lista, int start) {
		return listToString(lista, start, " ");
	}
	public static String listToString(Iterable<?> Lista, int start, String wstawka) {
		List<?> lista = Lists.newArrayList(Lista);
		StringBuilder s = new StringBuilder(lista.size() > start ? ""+lista.get(start) : "");
		int i=0;
		for (Object obj : lista)
			if (i++ > start)
				s.append(wstawka).append(obj == null ? null : obj.toString());
		return s.toString();
	}
	public static String locToString(Location loc) {
		Function<Double, String> func = x -> zaokrąglij(x, 1) + "";
		return String.format("%sx %sy %sz %s/%s",
				func.apply(loc.getX()), func.apply(loc.getY()), func.apply(loc.getZ()),
				(int) loc.getPitch(), (int) loc.getYaw());
	}
	
	public static String odkoloruj(String text) {
		if (text == null) return null;
		return text.replace("&", "&&").replace("§", "&");
	}
	public static String koloruj(String text) {
		if (text == null) return null;
		text = kolorkiRGB(przejścia(text));
		return text.replace("&", "§").replace("§§", "&");
	}
	public static String usuńKolor(String text) {
		StringBuilder strB = new StringBuilder();
		boolean pomiń = false;
		for (char znak : text.toCharArray()) {
			if (pomiń || znak == '§') {
				pomiń = !pomiń;
				continue;
			}
			strB.append(znak);
		}
		return strB.toString();
	}
	public static String przejścia(String text) {
		while (text.contains("&%")) {
			int index = text.indexOf("&%");
			String m = text.substring(index+2);
			if (m.contains("&"))
				m = m.substring(0, m.indexOf("&"));
			if (m.contains("§"))
				m = m.substring(0, m.indexOf("§"));
			try {
				String przejście = przejście(m);
				text = text.substring(0, index) + przejście + text.substring(index + 2 + m.length());
			} catch (IndexOutOfBoundsException | NumberFormatException e) {
				text = text.substring(0, index) + text.substring(index + 2);
			}
		}
		return text;
	}
	private static String przejście(String text) {
		String hex1 = text.substring(0, 6);
		if (text.charAt(6) != '-')
			throw new IndexOutOfBoundsException();
		String hex2 = text.substring(7, 13);
		text = text.substring(13);
		
		if (text.isEmpty())
			return "";
		
		int stop;
		
		int rakt = Integer.parseInt(hex1.substring(0, 2), 16);
		stop  	 = Integer.parseInt(hex2.substring(0, 2), 16);
		int rskok = (stop - rakt) / text.length();
		
		int gakt = Integer.parseInt(hex1.substring(2, 4), 16);
		stop  	 = Integer.parseInt(hex2.substring(2, 4), 16);
		int gskok = (stop - gakt) / text.length();
		
		int bakt = Integer.parseInt(hex1.substring(4, 6), 16);
		stop  	 = Integer.parseInt(hex2.substring(4, 6), 16);
		int bskok = (stop - bakt) / text.length();
		
		
		StringBuilder w = new StringBuilder();
		Consumer<Integer> hex = liczba -> {
			String _hex = (Integer.toHexString(liczba)+'0');
			w.append('§').append(_hex.charAt(0)).append('§').append(_hex.charAt(1));
		};
		for (char znak : text.toCharArray()) {
			w.append("§x");
			hex.accept(rakt); hex.accept(gakt);	hex.accept(bakt);
			rakt += rskok;	  gakt += gskok;	bakt += bskok;
			w.append(znak);
		}
		return w.toString();
	}
	private static String kolorkiRGB(String msg) {
		StringBuffer rgbBuilder = new StringBuffer();
		Matcher rgbMatcher = Pattern.compile("(&)?&#([0-9a-fA-F]{6})").matcher(msg);
		while (rgbMatcher.find()) {
			if (rgbMatcher.group(1) == null)
				try {
					String hexCode = rgbMatcher.group(2);
					rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
					continue;
				} catch (NumberFormatException e) {}
			rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
		}
		rgbMatcher.appendTail(rgbBuilder);
		return rgbBuilder.toString();
	}
	private static String parseHexColor(String hexColor) throws NumberFormatException {
	    if (hexColor.startsWith("#"))
	      hexColor = hexColor.substring(1); 
	    if (hexColor.length() != 6)
	      throw new NumberFormatException("Invalid hex length"); 
	    java.awt.Color.decode("#" + hexColor);
	    
	    StringBuilder assembledColorCode = new StringBuilder("§x");
	    for (char curChar : hexColor.toCharArray())
	      assembledColorCode.append("§").append(curChar); 
	    return assembledColorCode.toString();
	  }
	public static List<String> koloruj(List<String> lista) {
		int i=0;
		for (String linia : lista)
			lista.set(i++, koloruj(linia));
		return lista;
	}
	
	public static double Double(Object liczba) {
		if (liczba instanceof Double)
			return (double) liczba;
		if (liczba instanceof Integer)
			return (int) liczba;
		return Double((String) liczba, 0);
	}
	public static double Double(String liczba, double domyślna) {
		if (liczba.contains("."))
			try {
				return Double.parseDouble(liczba.trim());
			} catch(NumberFormatException er) {
				return domyślna;
			}
		return Int(liczba, (int) domyślna);
	}
	public static int Int(String liczba, int domyslna) {
		try {
			return Integer.parseInt(liczba.trim());
		} catch(NumberFormatException nfe) {
			return domyslna;
		}
	}
	
	public static ItemStack customModelData(ItemStack item, Integer customModelData) {
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(customModelData);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack pokolorujZbroje(ItemStack item, Color kolor) {
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(kolor);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
		
	}
	public static ItemStack połysk(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack enchantuj(ItemStack item, Enchantment enchant, int lvl) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(enchant, lvl, true);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack ukryj(ItemStack item, ItemFlag flaga) {
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(flaga);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack dodajLore(ItemStack item, String linia) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = Lists.newArrayList();
		lore.add(linia);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack ustawLore(ItemStack item, String linia, int nrLini) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) lore = Lists.newArrayList();
		while (nrLini >= lore.size())
			lore.add("");
		lore.set(nrLini, linia);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack wstawLore(ItemStack item, String linia, int nrLini) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) lore = Lists.newArrayList();
		while (nrLini >= lore.size())
			lore.add("");
		lore.add(nrLini, linia);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack ustawLore(ItemStack item, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setLore(koloruj(lore));
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack nazwij(ItemStack item, String nazwa) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(koloruj(nazwa));
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack stwórzItem(Material materiał) {
		return stwórzItem(materiał, 1, null, null);
	}
	public static ItemStack stwórzItem(Material materiał, String nazwa) {
		return stwórzItem(materiał, 1, nazwa, null);
	}
	public static ItemStack stwórzItem(Material materiał, String nazwa, String... lore) {
		return stwórzItem(materiał, 1, nazwa, Lists.newArrayList(lore));
	}
	public static ItemStack stwórzItem(Material materiał, String nazwa, List<String> lore) {
		return stwórzItem(materiał, 1, nazwa, lore);
	}
	public static ItemStack stwórzItem(Material materiał, int ilość) {
		return stwórzItem(materiał, ilość, null, null);
	}
	public static ItemStack stwórzItem(Material materiał, int ilość, String nazwa) {
		return stwórzItem(materiał, ilość, nazwa, null);
	}
	public static ItemStack stwórzItem(Material materiał, int ilość, String nazwa, List<String> lore) {
		ItemStack item;
		ItemMeta meta;
		
		item = new ItemStack(materiał, ilość);
		meta = item.getItemMeta();
		
		// Nazwa
		if (nazwa != null)
			meta.setDisplayName(koloruj(nazwa));
		
		// Lore
		if (lore != null) {
			for (int i=0; i<lore.size(); i++) 
				lore.set(i, koloruj(lore.get(i)));
			meta.setLore(lore);
		}
		
		// Wykończenie
        item.setItemMeta(meta);
        return item;
	}	
	public static ItemStack dajGłówkę(String url) {
		return dajGłówkę(null, url, null);
	}
	public static ItemStack dajGłówkę(String nazwa, String url) {
		return dajGłówkę(nazwa, url, null);
	}
	public static ItemStack dajGłówkę(String nazwa, String url, List<String> lore) {
		ItemStack item;
		SkullMeta meta;
		
		item = new ItemStack(Material.PLAYER_HEAD);
		meta = (SkullMeta) item.getItemMeta();
		if (nazwa != null)
			meta.setDisplayName(koloruj(nazwa));
		if (lore != null) {
			for (int i=0; i<lore.size(); i++) 
				lore.set(i, koloruj(lore.get(i)));
			meta.setLore(lore);
		}
		
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        }
        catch (IllegalArgumentException|NoSuchFieldException|SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }
		
        item.setItemMeta(meta);
        return item;	
	}
	public static boolean dajItem(Player p, ItemStack item) {
		if (p.getInventory().firstEmpty() == -1) {
			p.getWorld().dropItem(p.getLocation(), item);
			return false;
		} else {
			p.getInventory().addItem(item);
			return true;
		}
	}
	
	public static boolean losuj(double szansa) {
		double rand = Math.random();
		return 0 < rand && rand <= szansa;
	}
	public static double losuj(double min, double max) {
		int _min = (int) (min * 10_000);
		int _max = (int) (max * 10_000);
		
		double w = losuj(_min, _max);
		
		return w / 10_000;
	}
	public static int losuj(int min, int max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	public static <T> T losuj(Iterable<T> zCzego) {
		List<T> lista = Lists.newArrayList(zCzego);
		if (lista.isEmpty())
			return null;
		return lista.get(losujWZasięgu(lista.size()));
	}
	public static int losujWZasięgu(int max) {
		return Func.losuj(0, max-1);
	}
	public static <T> T losujPop(List<T> zCzego) {
		if (zCzego.isEmpty())
			return null;
		return zCzego.remove(losujWZasięgu(zCzego.size()));
	}
	
	public static double zaokrąglij(double liczba, int miejsca) {
		liczba *= Math.pow(10, miejsca);
		liczba  = (double) (int) liczba;
		liczba /= Math.pow(10, miejsca);
		return liczba;
	}
	public static <T> T max(Iterable<T> iterable, BinaryOperator<T> func) {
		T w = null;
		
		for (T el : iterable)
			if (w == null)
				w = el;
			else
				w = func.apply(w, el);
		
		return w;
	}
	public static <T> T max(Iterable<T> iterable, Function<T, Integer> func) {
		T w = null;
		int max = 0;
		
		for (T el : iterable) {
			int x = func.apply(el);
			if (w == null || x > max) {
				max = x;
				w = el;
			}
		}
		
		
		return w;
	}
	public static int max(Iterable<Integer> iterable) {
		return max(iterable, (a, b) -> Math.max(a, b));
	}
	public static int min(Iterable<Integer> iterable) {
		return max(iterable, (a, b) -> Math.min(a, b));
	}
	public static <T> Krotka<T, T> minMax(T a, T b, BinaryOperator<T> min, BinaryOperator<T> max) {
		return new Krotka<>(
				min.apply(a, b),
				max.apply(a, b)
				);
	}
	
	@SuppressWarnings("resource")
	public static boolean wyjmijPlik(String co, String gdzie) {
		String nazwaPluginu = Main.plugin.getName();
		File f2 = new File(gdzie);
		try {
			JarFile jar = new JarFile("plugins/"+nazwaPluginu+".jar");
			JarEntry plik = jar.getJarEntry("me/jomi/"+nazwaPluginu+"/" + co);
			if (plik == null)
				return false;
			InputStream inputStream = jar.getInputStream(plik);
			
			int read;
            byte[] bytes = new byte[1024];
            FileOutputStream outputStream = new FileOutputStream(f2);
            
            while ((read = inputStream.read(bytes)) != -1)
            	outputStream.write(bytes, 0, read);

            outputStream.close();
            return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean przenieśPlik(String co, String gdzie) {
		return new File(co).renameTo(new File(gdzie));
	}
	public static String czytajPlik(File plik) {
		String w = "";
		try {
			Scanner reader = new Scanner(plik, "utf-8");
			while (reader.hasNextLine()) {
		    	String data = reader.nextLine();
		        w += '\n' + Func.koloruj(data);
		    }
		    reader.close();
		    } catch (FileNotFoundException e) {
		    	Main.plugin.getLogger().warning("Błąd w trakcie czytania pliku " + plik.getAbsolutePath());
		    }
		return w.isEmpty() ? w : w.substring(1);
	}
	public static List<Class<?>> wszystkieKlasy() {
		List<Class<?>> lista = Lists.newArrayList();
		try {
			JarFile jar = new JarFile("plugins/"+Main.plugin.getName()+".jar");
			Enumeration<JarEntry> scieżki = jar.entries();
			while (scieżki.hasMoreElements()) {
				String sc = scieżki.nextElement().toString();
				if (!sc.endsWith(".class")) continue;
				sc = sc.substring(0, sc.length()-6).replace('/', '.');
				try {
					lista.add(Class.forName(sc, false, Main.classLoader)); // XXX użyć tego w ModuleManagerze
				} catch (Throwable e) {}
			}
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lista;
	}

	public static String nieNullStr(String str) {
		return str == null ? "" : str;
	}
	public static Object nieNullList(Object lista) {
		return lista != null ? lista : Lists.newArrayList();
	}
	public static <T> List<T> nieNullList(List<T> lista) {
		return lista != null ? lista : Lists.newArrayList();
	}
	
	public static String ostatni(String[] stringi) {
		if (stringi.length == 0) return "";
		return stringi[stringi.length-1];
	}
	public static List<String> tnij(String napis, String regex) {
		final List<String> lista = Lists.newArrayList();
		if (napis.isEmpty()) return lista;
		while (true) {
			int i = napis.indexOf(regex);
			if (i == -1) {
				lista.add(napis);
				return lista;
			}
			
			lista.add(napis.substring(0, i));
			napis = napis.substring(i + regex.length());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void dodajWszystkie(List<T> lista, T... elementy) {
		for (T el : elementy)
			lista.add(el);
	}
	@SuppressWarnings("unchecked")
	public static <T> void dodajWszystkie(Set<T> set, T... elementy) {
		for (T el : elementy)
			set.add(el);
	}
	
	public static boolean zawiera(Location loc, Location róg1, Location róg2) {
		Predicate<Function<Location, Double>> mieściSię = func -> {
			double co = func.apply(loc);
			double x1 = func.apply(róg1);
			double x2 = func.apply(róg2);
			return  (co >= x1 && co <= x2) || 
					(co <= x1 && co >= x2);
		};
		return  mieściSię.test(Location::getX) &&
				mieściSię.test(Location::getY) &&
				mieściSię.test(Location::getZ);
		
	}
	
	public static String czas(int sekundy) {
		if (sekundy <= 0) return "0 sekund";
		int minuty 	= sekundy / 60;	sekundy	%= 60;
		int godziny = minuty  / 60;	minuty  %= 60;
		int dni 	= godziny / 24;	godziny %= 24;
		
		Function<Integer, String> odmiana = ile -> {
			if (ile == 1)
				return "ę";
			switch (ile % 10) {
			case 2:
			case 3:
			case 4:
				return "y";
			default:
				return "";
			}
		};
		
		StringBuilder w = new StringBuilder();
		if (dni != 0)		w.append(dni)	 .append(dni == 1 ? " dzień " : " dni ");
		if (godziny != 0) 	w.append(godziny).append(" godzin") .append(odmiana.apply(godziny)).append(' ');
		if (minuty != 0) 	w.append(minuty) .append(" minut")  .append(odmiana.apply(minuty)) .append(' ');
		if (sekundy != 0) 	w.append(sekundy) .append(" sekund").append(odmiana.apply(sekundy)).append(' ');
		return w.length() == 0 ? "0 sekund" : w.substring(0, w.length()-1);
	}

	public static String permisja(String permisja) {
		permisja = permisja.toLowerCase();
		String plugin = Main.plugin.getName().toLowerCase();
		if (!permisja.startsWith(plugin + '.'))
			permisja = plugin + '.' + permisja;
		return permisja;
	}

	public static boolean powiadom(CommandSender p, String msg) {
		p.sendMessage(msg);
		return true;
	}
	public static boolean powiadom(String prefix, CommandSender sender, String msg, Object... uzupełnienia) {
		return powiadom(sender, prefix + Func.msg(msg, uzupełnienia));
	}

	public static Inventory CloneInv(Inventory inv, String nazwa) {
		Inventory _inv = Bukkit.createInventory(inv.getHolder(), inv.getSize(), nazwa);
		for (int i=0; i<inv.getSize(); i++)
			_inv.setItem(i, inv.getItem(i).clone());
		return _inv;
	}

	public static class Task {
		static final List<Task> wszystkie = Lists.newArrayList();
		Runnable lambda;
		int id;
		
		public Task(int ticki, Runnable lambda) {
			this.lambda = lambda;
			id = opóznij(ticki, this::wykonaj);
			wszystkie.add(this);
		}
		
		public void wykonaj() {
			lambda.run();
			usuń();
		}
		public void anuluj() {
			Bukkit.getScheduler().cancelTask(id);
			usuń();
		}
		private void usuń() {
			int i = -1;
			for (Task task : wszystkie)
				if (++i >= 0 && task.id == id) {
					wszystkie.remove(i);
					break;
				}
		}
	}
	public static int opóznij(int ticki, Runnable lambda) {
		return Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, lambda, ticki);
	}
	public static Task opóznijWykonajOnDisable(int ticki, Runnable lambda) {
		return new Task(ticki, lambda);
	}
	public static void onDisable() {
		for (Task task : Task.wszystkie)
			task.lambda.run();
		for (Task task : Task.wszystkie)
			Bukkit.getScheduler().cancelTask(task.id);
		Task.wszystkie.clear();
	}

	public static OfflinePlayer graczOfflineUUID(String uuid) {
		return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
	}
	public static OfflinePlayer graczOffline(String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (p != null) return p;
		
		for (OfflinePlayer gracz : Bukkit.getOfflinePlayers())
			if (gracz.getName().equalsIgnoreCase(nick))
				return gracz;
		return null;
	}

	public static boolean porównaj(ItemStack item1, ItemStack item2) {
		if (item1 == null || item2 == null) return item1 == item2;
		return item1.isSimilar(item2);
	}

	public static void zdemapuj(Object obj, Map<String, Object> mapa) {
		if (obj == null) return;
		
		Class<?> clazz = obj.getClass();
		
		for (Entry<String, Object> en : mapa.entrySet())
			try {
				if (en.getKey().equals("==")) continue;
				if (en.getKey().equals("=mimi=")) continue;
				Field field = dajField(clazz, en.getKey());
				field.setAccessible(true);
				if (!field.isAnnotationPresent(Mapowane.class))
					throw new Throwable();
				if (field.getType().isEnum())
					try {
						field.set(obj, field.getType().getMethod("valueOf", String.class).invoke(null, en.getValue()));
					} catch (Throwable _e) {
						Main.warn(String.format("Nieprawidłowa wartość wyliczeniowa \"%s\" dla pola \"%s\" przy demapowianiu klasy %s",
								en.getValue(), en.getKey(), clazz.getName()));
					}
				else
					field.set(obj, en.getValue());
			} catch (Throwable e) {
				Main.warn("Nieprawidłowa nazwa pola \"" + en.getKey() + "\" przy demapowaniu klasy " + clazz.getName());
			}

		try {
			for (Field field : głębokiSkanKlasy(clazz)) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(Mapowane.class) && field.get(obj) == null) { 
					if (List.class.isAssignableFrom(field.getType()))
						field.set(obj, Lists.newArrayList());
					else if (field.getType().isEnum()) {
						Enum<?>[] enumy = (Enum<?>[]) field.getType().getMethod("values").invoke(null);
						if (enumy.length > 0)
							field.set(obj, enumy[0]);
					}
				}
	
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	public static Map<String, Object> zmapuj(Object obj) {
		Map<String, Object> mapa = new HashMap<String, Object>();
		Class<?> clazz = obj.getClass();
		for (Field field : głębokiSkanKlasy(clazz))
			try {
				if (!field.isAnnotationPresent(Mapowane.class)) continue;
				field.setAccessible(true);
				String name = field.getName();
				mapa.put(name, zmapuj_wez(field, obj));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		return mapa;
	}
	private static Object zmapuj_wez(Field field, Object obj) throws Throwable {
		Object w = field.get(obj);
		if (w == null) return w;
		if (field.getType().isEnum())
			return field.getType().getMethod("name").invoke(w);
		return w;
	}

	public static <K, V> List<V> wezUstaw(Map<K, List<V>> mapa, K klucz) {
		List<V> obj = mapa.get(klucz);
		if (obj == null)
			mapa.put(klucz, obj = Lists.newArrayList());
		return obj;
	}
	public static <K, V> V wezUstaw(Map<K, V> mapa, K klucz, Supplier<V> domyślna) {
		V w = mapa.get(klucz);
		if (w == null)
			mapa.put(klucz, w = domyślna.get());
		return w;
	}
	public static <K, V> V wezUstaw(Map<K, V> mapa, K klucz, V domyślna) {
		V w = mapa.get(klucz);
		if (w == null)
			mapa.put(klucz, w = domyślna);
		return w;
	}

	@SuppressWarnings("unchecked")
	public static <T> boolean multiEquals(T co, T... czemu) {
		for (T _co : czemu)
			if (co == null && _co == null)
				return true;
			else if (co != null && co.equals(_co))
				return true;
		return false;
	}

	public static <T> void wykonajDlaNieNull(T obj, Consumer<T> func) {
		if (obj != null)
			func.accept(obj);
	}
	public static <T, V> V zwrotDlaNieNull(T obj, Function<T, V> func) {
		if (obj != null)
			return func.apply(obj);
		return null;
	}
	
	public static void multiTry(Class<? extends Throwable> error, Runnable... funkcje) {
		for (Runnable r : funkcje)
			try {
				r.run();
				return;
			} catch (Throwable e) {
				if (error.isAssignableFrom(error))
					continue;
				throw e;
			}
	}
	public static void multiTry(Runnable... funkcje) {
		for (Runnable r : funkcje)
			try {
				r.run();
			} catch (Throwable e) {}
	}
	
	public static void ustawMetadate(Metadatable naCzym, String id, Object value) {
		naCzym.setMetadata(id, new FixedMetadataValue(Main.plugin, value));
	}

	@SuppressWarnings("unchecked")
	public static <T> T utwórz(Class<T> clazz) {
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("=mimi=", clazz.getName());
		return (T) Mapowany.deserialize(mapa);
		
	}

	public static List<Field> głębokiSkanKlasy(Class<?> clazz) {
		return głębokiSkanKlasy(clazz, Sets.newConcurrentHashSet());
	}
	private static List<Field> głębokiSkanKlasy(Class<?> clazz, Set<String> nazwyfieldsów) {
		List<Field> lista = Lists.newArrayList();
		if (clazz == null || clazz.getName().equals(Object.class.getName()))
			return lista;
		
		for (Field field : clazz.getDeclaredFields())
			if (nazwyfieldsów.add(field.getName()))
				lista.add(field);
		
		for (Field field : głębokiSkanKlasy(clazz.getSuperclass(), nazwyfieldsów))
			lista.add(field);
		
		return lista;
	}
	public static Field dajField(Class<?> clazz, String nazwa) throws Throwable {
		if (clazz.getName().equals(Object.class.getName()))
			throw new NoSuchFieldException();
		try {
			return clazz.getDeclaredField(nazwa);
		} catch (NoSuchFieldException e) {
			return dajField(clazz.getSuperclass(), nazwa);
		}
	}
	public static Method dajMetode(Class<?> clazz, String nazwa, Class<?>... klasy) throws Throwable {
		if (clazz.getName().equals(Object.class.getName()))
			throw new NoSuchMethodException();
		try {
			return clazz.getDeclaredMethod(nazwa, klasy);
		} catch (NoSuchMethodException e) {
			return dajMetode(clazz.getSuperclass(), nazwa);
		}
	}
	public static List<Class<?>> dajKlasy(Class<?> clazz) {
		List<Class<?>> lista = Lists.newArrayList(clazz);
		
		while (!clazz.getName().equals(Object.class.getName()))
			lista.add(clazz = clazz.getSuperclass());
		
		return lista;
	}

	public static <T1, T2> List<T2> wykonajWszystkim(Iterable<T1> lista, Function<T1, T2> func) {
		List<T2> _lista = Lists.newArrayList();
		for (T1 el : lista)
			_lista.add(func.apply(el));
		return _lista;
	}

	static class IterableBloków implements Iterable<Block> {
		Iterator<Block> iterator;
		IterableBloków(Iterator<Block> iterator) {
			this.iterator = iterator;
		}
		@Override
		public Iterator<Block> iterator() {
			return iterator;
		}
		
	}
	static class IteratorBloków implements Iterator<Block> {
		Krotka<Integer, Integer> kx;
		Krotka<Integer, Integer> ky;
		Krotka<Integer, Integer> kz;
		boolean next = true;
		World świat;
		int x;
		int y;
		int z;
		
		IteratorBloków(Location róg1, Location róg2) {
			świat = róg1.getWorld();
			Function<Function<Location, Integer>, Krotka<Integer, Integer>> krotka = 
					func -> Func.minMax(func.apply(róg1), func.apply(róg2), Math::min, Math::max);
			kx = krotka.apply(Location::getBlockX); x = kx.a;
			ky = krotka.apply(Location::getBlockY); y = ky.a;
			kz = krotka.apply(Location::getBlockZ); z = kz.a;
		}
		
		@Override
		public boolean hasNext() {
			return next;
		}
		@Override
		public Block next() {
			Block blok = świat.getBlockAt(x, y, z);
			if (++x > kx.b) {
				x = kx.a;
				if (++z > kz.b) {
					z = kz.a;
					next = ++y <= ky.b;
				}
			}
			return blok;
		}
	}
	public static Iterable<Block> bloki(Location róg1, Location róg2) {
		return new IterableBloków(blokiIterator(róg1, róg2));
	}
	public static Iterator<Block> blokiIterator(Location róg1, Location róg2) {
		return new IteratorBloków(róg1, róg2);
	}
}
