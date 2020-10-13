package me.jomi.mimiRPG;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public abstract class Func {
	public static String prefix(String nazwa) {
		return "§2[§a" + nazwa + "§2]§6 ";
	}
	public static String msg(String tekst, Object... uzupełnienia) {
		for (Object u : uzupełnienia)
			tekst = tekst.replaceFirst("%s", "§e" + u + "§6");
		return "§6" + tekst + "§6";
	}
	
	public static Inventory CloneInv(Inventory inv, String nazwa) {
		Inventory _inv = Bukkit.createInventory(inv.getHolder(), inv.getSize(), nazwa);
		for (int i=0; i<inv.getSize(); i++)
			_inv.setItem(i, inv.getItem(i).clone());
		return _inv;
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
	public static String listToString(Object[] lista, int start, String wstawka) {
		return listToString(Lists.newArrayList(lista), start, wstawka);
	}
	public static String listToString(List<?> lista, int start) {
		return listToString(lista, start, " ");
	}
	public static String listToString(List<?> lista, int start, String wstawka) {
		StringBuilder s = new StringBuilder(lista.size() > start ? ""+lista.get(start) : "");
		int i=0;
		for (Object obj : lista)
			if (i++ > start)
				s.append(wstawka).append(obj == null ? null : obj.toString());
		return s.toString();
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
	
	public static String koloruj(String text) {
		if (text == null) return null;
		text = kolorkiRGB(przejścia(text));
		return text.replace("&", "§").replace("§§", "&");
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
	    Color.decode("#" + hexColor);
	    
	    StringBuilder assembledColorCode = new StringBuilder("§x");
	    for (char curChar : hexColor.toCharArray())
	      assembledColorCode.append("§").append(curChar); 
	    return assembledColorCode.toString();
	  }
	public static String odkoloruj(String text) {
		if (text == null) return null;
		return text.replace("&", "&&").replace("§", "&");
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
	public static ItemStack nazwij(ItemStack item, String nazwa) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(nazwa);
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
	
	public static boolean dajItem(Player p, ItemStack item) {
		if (p.getInventory().firstEmpty() == -1) {
			p.getWorld().dropItem(p.getLocation(), item);
			return false;
		} else {
			p.getInventory().addItem(item);
			return true;
		}
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
	
	public static boolean porównaj(ItemStack item1, ItemStack item2) {
		ItemStack item1c = item1.clone();
		ItemStack item2c = item2.clone();
		item1c.setAmount(1);
		item2c.setAmount(1);
		return item1c.equals(item2c);
	}
	
	public static boolean losuj(double szansa) {
		double rand = Math.random();
		return 0 < rand && rand <= szansa;
	}
	/**
	 * losuje liczbe z zakresu min - max (włącznie)
	 * 
	 * @param min minimalna możliwa wylosowana liczba
	 * @param max maksywamlna możliwa wylosowana liczba
	 * 
	 * @return liczba z przedziału <min, max>
	 */
	public static int losuj(int min, int max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	public static <T> T losuj(List<T> lista) {
		if (lista.isEmpty())
			return null;
		return lista.get(losuj(0, lista.size()-1));
	}
	public static int losujWZasięg(int max) {
		return Func.losuj(0, max-1);
	}
	public static double zaokrąglij(double liczba, int miejsca) {
		liczba *= Math.pow(10, miejsca);
		liczba  = (double) (int) liczba;
		liczba /= Math.pow(10, miejsca);
		return liczba;
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
	
	public static OfflinePlayer graczOffline(String nick) {
		for (OfflinePlayer gracz : Bukkit.getOfflinePlayers()) {
			if (gracz.getName().equalsIgnoreCase(nick))
				return gracz;
		}
		return null;
	}
	public static String nieNullStr(String str) {
		return str == null ? "" : str;
	}
	public static <T> List<T> nieNullList(List<T> lista){
		return lista != null ? lista : Lists.newArrayList();
	}
	public static String ostatni(String[] stringi) {
		if (stringi.length == 0) return "";
		return stringi[stringi.length-1];
	}
	public static List<String> tnij(String napis, String regex) {
		final List<String> lista = Lists.newArrayList();
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

	public static void opóznij(int ticki, Runnable lambda) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, lambda, ticki);
	}

	private static boolean mieściSię(double co, double x1, double x2) {
		return  (co >= x1 && co <= x2) || 
				(co <= x1 && co >= x2);
	}
	public static boolean zawiera(Location loc, Location róg1, Location róg2) {
		return  mieściSię(loc.getX(), róg1.getX(), róg2.getX()) &&
				mieściSię(loc.getY(), róg1.getY(), róg2.getY()) &&
				mieściSię(loc.getZ(), róg1.getZ(), róg2.getZ());
		
	}
	
	public static String czas(int sekundy) {
		if (sekundy <= 0) return "0 sekund";
		int minuty 	= sekundy / 60;	sekundy	%= 60;
		int godziny = minuty  / 60;	minuty  %= 60;
		int dni 	= godziny / 24;	godziny %= 24;
		
		Function<Integer, Character> odmiana = ile -> {
			switch (ile % 10) {
			case 1:
				return 'ę';
			case 2:
			case 3:
			case 4:
				return 'y';
			default:
				return null;
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
}
