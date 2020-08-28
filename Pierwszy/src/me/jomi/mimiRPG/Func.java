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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public abstract class Func {
	public static String prefix(String nazwa) {
		return "�2[�a" + nazwa + "�2]�6 ";
	}
	public static String msg(String tekst, Object... uzupe�nienia) {
		for (Object u : uzupe�nienia)
			tekst = tekst.replaceFirst("%s", "�e" + u + "�6");
		return "�6" + tekst + "�6";
	}
	public static int losuj(int min, int max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	public static String DoubleToString(double liczba) {
		String ca�o�ci = IntToString((int) liczba);
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
		return ca�o�ci + reszta;
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
				s.append(wstawka).append(obj.toString());
		return s.toString();
	}
	public static double sprawdz_poprawno��(String liczba, double domy�lna) {
		try {
			return Double.parseDouble(liczba.trim());
		} catch(NumberFormatException er) {
			return domy�lna;
		}
	}
	
	public static String koloruj(String text) {
		if (text == null) return null;

		text = kolorkiRGB(przej�cia(text));
		return text.replace("&", "�").replace("��", "&");
	}
	public static String przej�cia(String text) {
		while (text.contains("&%")) {
			int index = text.indexOf("&%");
			String m = text.substring(index+2);
			if (m.contains("&"))
				m = m.substring(0, m.indexOf("&"));
			if (m.contains("�"))
				m = m.substring(0, m.indexOf("�"));
			try {
				String przej�cie = przej�cie(m);
				text = text.substring(0, index) + przej�cie + text.substring(index + 2 + m.length());
			} catch (IndexOutOfBoundsException | NumberFormatException e) {
				text = text.substring(0, index) + text.substring(index + 2);
			}
		}
		return text;
	}
	private static String przej�cie(String text) {
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
		for (char znak : text.toCharArray()) {
			w.append("�x");
			hex(w, rakt);	hex(w, gakt);	hex(w, bakt);
			rakt += rskok;	gakt += gskok;	bakt += bskok;
			w.append(znak);
		}
		return w.toString();
	}
	private static void hex(StringBuilder strB, int liczba) {
		String w = (Integer.toHexString(liczba)+'0');
		strB.append('�').append(w.charAt(0)).append('�').append(w.charAt(1));
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
	    
	    StringBuilder assembledColorCode = new StringBuilder("�x");
	    for (char curChar : hexColor.toCharArray())
	      assembledColorCode.append("�").append(curChar); 
	    return assembledColorCode.toString();
	  }
	public static String odkoloruj(String text) {
		if (text == null) return null;
		return text.replace("&", "&&").replace("�", "&");
	}
	public static String usu�Kolor(String text) {
		StringBuilder strB = new StringBuilder();
		boolean pomi� = false;
		for (char znak : text.toCharArray()) {
			if (pomi� || znak == '�') {
				pomi� = !pomi�;
				continue;
			}
			strB.append(znak);
		}
		return strB.toString();
	}
	
	public static ItemStack po�ysk(ItemStack item) {
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
	public static ItemStack nazwij(ItemStack item, String nazwa) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(nazwa);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack stw�rzItem(Material materia�) {
		return stw�rzItem(materia�, 1, null, null);
	}
	public static ItemStack stw�rzItem(Material materia�, String nazwa) {
		return stw�rzItem(materia�, 1, nazwa, null);
	}
	public static ItemStack stw�rzItem(Material materia�, String nazwa, String... lore) {
		return stw�rzItem(materia�, 1, nazwa, Lists.newArrayList(lore));
	}
	public static ItemStack stw�rzItem(Material materia�, String nazwa, List<String> lore) {
		return stw�rzItem(materia�, 1, nazwa, lore);
	}
	public static ItemStack stw�rzItem(Material materia�, int ilo��) {
		return stw�rzItem(materia�, ilo��, null, null);
	}
	public static ItemStack stw�rzItem(Material materia�, int ilo��, String nazwa) {
		return stw�rzItem(materia�, ilo��, nazwa, null);
	}
	public static ItemStack stw�rzItem(Material materia�, int ilo��, String nazwa, List<String> lore) {
		ItemStack item;
		ItemMeta meta;
		
		item = new ItemStack(materia�, ilo��);
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
		
		// Wyko�czenie
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
	public static final UUID uuid = UUID.randomUUID();
	public static ItemStack dajG��wk�(String url) {
		return dajG��wk�(null, url, null);
	}
	public static ItemStack dajG��wk�(String nazwa, String url) {
		return dajG��wk�(nazwa, url, null);
	}
	public static ItemStack dajG��wk�(String nazwa, String url, List<String> lore) {
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
		
		GameProfile profile = new GameProfile(uuid, null);
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
	public static double zaokr�glij(double liczba, int miejsca) {
		liczba *= Math.pow(10, miejsca);
		liczba  = (double) (int) liczba;
		liczba /= Math.pow(10, miejsca);
		return liczba;
	}
	@SuppressWarnings("resource")
	public static boolean wyjmijPlik(String co, String gdzie) {
		String nazwaPluginu = Main.plugin.getDescription().getName();
		File f2 = new File(gdzie);
		try {
			JarFile jar = new JarFile("plugins/"+nazwaPluginu+".jar");
			JarEntry plik = jar.getJarEntry("me/jomi/"+nazwaPluginu+"/" + co);
			InputStream inputStream = jar.getInputStream(plik);
			
			int read;
            byte[] bytes = new byte[1024];
            FileOutputStream outputStream = new FileOutputStream(f2);
            
            while ((read = inputStream.read(bytes)) != -1) {
            	outputStream.write(bytes, 0, read);
            }
            outputStream.close();
            return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean przenie�Plik(String co, String gdzie) {
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
		    	Main.plugin.getLogger().warning("B��d w trakcie czytania pliku " + plik.getAbsolutePath());
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
}
