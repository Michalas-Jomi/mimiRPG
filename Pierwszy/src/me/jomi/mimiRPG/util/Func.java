package me.jomi.mimiRPG.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Mapowany.Mapowane;
import me.jomi.mimiRPG.util.Funkcje.FunctionN;

import io.papermc.paper.adventure.DisplayNames;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class Func {
	public static String prefix(String nazwa) {
		return koloruj("§2[§a" + nazwa + "§2]§6 ");
	}
	public static String prefix(Class<?> clazz) {
		try {
			String prefix = (String) Func.dajField(clazz, "prefix").get(null);
			if (prefix == null)
				throw new NoSuchFieldException();
			return prefix;
		} catch (NoSuchFieldException e) {
			return Func.prefix(clazz.getSimpleName());
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}
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

	public static void broadcast(String msg, String perm) {
		Bukkit.broadcast(Func.toComponent(msg), perm);
	}
	public static void broadcast(String msg) {
		Bukkit.broadcast(Func.toComponent(msg));
	}
	public static String getDisplayName(Player p) {
		return DisplayNames.getLegacy(me.jomi.mimiRPG.util.NMS.nms(p));
	}
	public static String getDisplayName(ItemMeta meta) {
		return fromComponent(meta.displayName());
	}
	public static ItemMeta setDisplayName(ItemMeta meta, String name) {
		meta.displayName(Func.toComponent(name));
		return meta;
	}
	public static Inventory createInventory(InventoryHolder holder, int size, String name) {
		return Bukkit.createInventory(holder, size, toComponent(name));
	}
	public static List<String> getLore(ItemMeta meta) {
		if (meta.lore() == null) return null;
		return Func.wykonajWszystkim(meta.lore(), Func::fromComponent);
	}
	public static ItemMeta setLore(ItemMeta meta, List<String> lore) {
		meta.setLore((lore == null || lore.isEmpty()) ? null : lore);
		return meta;
	}
	
	public static String getTitle(InventoryView inv) {
		return fromComponent(inv.title());
	}
	public static void sendActionBar(Player p, String actionBar) {
		try {
			Func.dajMetode(p.getClass(), "sendActionBar", Component.class).invoke(p, toComponent(actionBar));
		} catch (Throwable e) {
			Func.throwEx(e);
		}
	}

	public static Component toComponent(String str) {
		TextComponent comp = PaperAdventure.LEGACY_SECTION_UXRC.deserialize(str);
		Func.forEach(TextDecoration.values(), dec -> {
			if (comp.decoration(dec) == TextDecoration.State.NOT_SET)
				comp.decoration(dec, TextDecoration.State.FALSE);
		});
		return comp;
	}
	public static String fromComponent(Component component) {
		return PaperAdventure.LEGACY_SECTION_UXRC.serialize(component);
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
	public static String progres(int ileJest, int ilePotrzeba, int ileZnaków, String znak, String kolorZrobione, String kolorNieZrobione) {
		StringBuilder strB = new StringBuilder();
		
		double procent = ileJest / (double) ilePotrzeba;
		
		strB.append(kolorZrobione);
		
		int zrobione = 0;
		
		while (zrobione / (double) ileZnaków < procent) {
			strB.append(znak);
			zrobione++;
		}
		strB.append(kolorNieZrobione);
		while (zrobione++ < ileZnaków)
			strB.append(znak);
		
		return strB.toString();
	}
	
	public static long czasSekundy() {
		return System.currentTimeMillis() / 1000;
	}
	public static String data(String format) {
		return data(System.currentTimeMillis(), format);
	}
	public static String data() {
		return data(System.currentTimeMillis());
	}
	public static String data(long miliSekundy) {
		return data(miliSekundy, "dd.MM.yyyy HH:mm");
	}
	public static String data(long miliSekundy, String format) {
		return new SimpleDateFormat(format).format(new Date(miliSekundy));
	}
	
	@SuppressWarnings("unchecked")
	public static <E> E StringToEnum(Class<E> clazz, String str) {
		try {
			Method met = clazz.getMethod("valueOf", String.class);
			str = str.replace(' ', '_');
			try {
				return (E) met.invoke(null, str);
			} catch (Throwable e) {}
			return (E) met.invoke(null, str.toUpperCase());
		} catch (Throwable e) {
			String msg = null;
			try {
				msg = "Nie odnaleziono wartości " + str + " w " + Arrays.asList((Enum<?>[]) Func.dajMetode(clazz, "values").invoke(null));
			} catch (Throwable e1) {
				e1.printStackTrace();
				throw new IllegalArgumentException(e);
			}
			throw new IllegalArgumentException(msg, e);
		}
	}
	
	public static String nazwaItemku(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		return meta.hasDisplayName() ? Func.getDisplayName(meta) : Func.enumToString(item.getType());
	}
	public static String enumToString(Enum<?> en) {
		return title(en.toString());
	}
	public static String title(String str) {
		StringBuilder strB = new StringBuilder();
		boolean duża = true;
		char ost = ' ';
		for (char c : str.toLowerCase().replace("_", " ").toCharArray()) {
			strB.append(duża ? Character.toUpperCase(c) : c);
			if (c != '§' && ost != '§')
				duża = c == ' ';
			ost = c;
		}
		return strB.toString();
	}
	public static String DoubleToString(double liczba) {
		String całości = IntToString((int) liczba);
		double r = liczba - (int) liczba;
		String reszta = "";
		if (r != 0)
			if (String.valueOf(r).length() > 5) {
				reszta += String.valueOf(r).substring(1, 5);
				while (reszta.endsWith("0"))
					reszta = reszta.substring(0, reszta.length()-1);
			}
			else
				reszta += String.valueOf(r).substring(1);
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
		for (char x : String.valueOf(liczba).toCharArray())
			r = x + r;
		String w = "";
		int i = 0;
		for (char x : r.toCharArray()) {
			w += x;
			if (x == '-')
				continue;
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
	public static String listToString(Iterable<?> lista) {
		return listToString(lista, 0, " ");
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
	public static String locBlockToString(Location loc) {
		return String.format("%sx %sy %sz", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	public static String arrayToString(Object[] array) {
		return arrayToStringBuffer(array).toString();
	}
	private static StringBuffer arrayToStringBuffer(Object[] array) {
		StringBuffer strB = new StringBuffer();
		strB.append('[');
		for (int i=0; i < array.length; i++) {
			if (array[i] == null)
				strB.append("null");
			else if (array[i].getClass().isArray())
				strB.append(arrayToStringBuffer((Object[]) array[i]));
			else
				strB.append(array[i]);
			if ( i + 1 < array.length)
				strB.append("§f, ");
		}
		return strB.append("] len = ").append(array.length);
	}
	
	public static String odkoloruj(String text) {
		if (text == null) return null;
		return text.replace("&", "&&").replace("§", "&");
	}
	public static String koloruj(String text) {
		if (text == null) return null;
		//if (text.contains("§"))
		//	return text;
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
		Matcher matcher = Pattern.compile("([&§])?[&§]%([0-9a-fA-F]{6})-([0-9a-fA-F]{6})(.*)").matcher(text);
		StringBuffer strB = new StringBuffer();
		if (matcher.find()) {
			if (matcher.group(1) == null) {
				StringBuilder kolor = new StringBuilder();
				String efekty = "lmnok&§";
				StringBuilder reszta = new StringBuilder();
				boolean specjalny = false;
				boolean koniec = false;
				for (char znak : matcher.group(4).toCharArray())
					if (koniec)
						reszta.append(znak);
					else if (specjalny) {
						specjalny = false;
						if (efekty.contains(String.valueOf(znak)))
							kolor.append('§').append(znak);
						else {
							reszta.append('§').append(znak);
							koniec = true;
						}
					} else if (znak == '&' || znak == '§')
						specjalny = true;
					else
						kolor.append(znak);
				matcher.appendReplacement(strB, przejście(matcher.group(2), matcher.group(3), kolor.toString()).replace("$", "\\$"));
				strB.append(przejścia(reszta.toString()));
			} else
				matcher.appendReplacement(strB, "$1%$2-$3$4");
		}
		matcher.appendTail(strB);
		return strB.toString();
	}
	private static String przejście(String hex1, String hex2, String text) {
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
		
		String prefix = "";
		
		StringBuilder w = new StringBuilder();
		Consumer<Integer> hex = liczba -> {
			String _hex = (Integer.toHexString(liczba)+'0');
			w.append('§').append(_hex.charAt(0)).append('§').append(_hex.charAt(1));
		};
		boolean specjalne = false;
		for (char znak : text.toCharArray())
			if (specjalne) {
				specjalne = false;
				prefix += "§" + String.valueOf(znak);
			} else if (znak == '§' || znak == '&')
				specjalne = true;
			else {
				w.append("§x");
				hex.accept(rakt); hex.accept(gakt);	hex.accept(bakt);
				rakt += rskok;	  gakt += gskok;	bakt += bskok;
				w.append(prefix).append(znak);
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
	
	public static double DoubleObj(Object liczba) {
		if (liczba instanceof Double)
			return (double) liczba;
		if (liczba instanceof Integer)
			return (int) liczba;
		return Double((String) liczba, 0);
	}
	public static float Float(Object liczba) {
		return (float) DoubleObj(liczba);
	}
	public static double Double(String liczba) throws NumberFormatException {
		liczba = liczba.trim();
		int mn = 1;
		if (liczba.endsWith("%")) {
			liczba = liczba.substring(0, liczba.length() - 1);
			mn = 100;
		}
		if (liczba.contains("."))
			return Double.parseDouble(liczba) / mn;
		return ((double) Int(liczba)) / mn;
	}
	public static int Int(Object liczba) throws NumberFormatException {
		if (liczba == null)
			return 0;
		if (liczba instanceof Integer || liczba.getClass().isPrimitive())
			return (int) liczba;
		return Integer.parseInt((liczba.toString()).trim());
	}
	
	public static double Double(String liczba, double domyślna) {
		try {
			return Double(liczba);
		} catch(NumberFormatException e) {
			return domyślna;
		}
	}
	public static int Int(String liczba, int domyslna) {
		try {
			return Int(liczba);
		} catch(NumberFormatException e) {
			return domyslna;
		}
	}
	public static String rzymskie(int liczba) {
		String[] znaki = {"M", "CM", "D", "C", "XC", "L", "X", "IX", "V", "I"};
		int[] wartości = {1000, 900, 500, 100, 90, 50, 10, 9, 5, 1};
		StringBuilder strB = new StringBuilder();
		 
		for (int i = 0; i < wartości.length; i++) {
			int numer = liczba / wartości[i];
			if (numer == 0) continue;
			if (numer == 4 && i > 0)
				strB.append(znaki[i] + znaki[i - 1]);
			else {
				String znak = znaki[i];
				for (int j=0; j < numer; j++)
					strB.append(znak);
			}
			liczba %= wartości[i];
		}
		return strB.toString();
	}
	
	public static ItemStack typ(ItemStack item, Material mat) {
		item.setType(mat);
		return item;
	}
	public static ItemStack ilość(ItemStack item, int ilość) {
		item.setAmount(ilość);
		return item;
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
		List<String> lore = Func.getLore(meta);
		if (lore == null)
			lore = Lists.newArrayList();
		lore.add(koloruj(linia));
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack ustawLore(ItemStack item, String linia, int nrLini) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Func.getLore(meta);
		if (lore == null) lore = Lists.newArrayList();
		while (nrLini >= lore.size())
			lore.add("");
		lore.set(nrLini, koloruj(linia));
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack wstawLore(ItemStack item, String linia, int nrLini) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Func.getLore(meta);
		if (lore == null) lore = Lists.newArrayList();
		while (nrLini >= lore.size())
			lore.add("");
		lore.add(nrLini, linia);
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack ustawLore(ItemStack item, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		lore = koloruj(lore);
		Func.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack nazwij(ItemStack item, String nazwa) {
		ItemMeta meta = item.getItemMeta();
		Func.setDisplayName(meta, koloruj(nazwa));
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
	public static ItemStack stwórzItem(Material materiał, String nazwa, int ilość, String... lore) {
		return stwórzItem(materiał, ilość, nazwa, Lists.newArrayList(lore));
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
		
		item = new ItemStack(materiał, ilość > 64 ? 1 : Math.max(1, ilość));
		meta = item.getItemMeta();
		
		// Nazwa
		if (nazwa != null)
			Func.setDisplayName(meta, koloruj(nazwa));
		
		// Lore
		if (lore != null) {
			for (int i=0; i<lore.size(); i++) 
				lore.set(i, koloruj(lore.get(i)));
			Func.setLore(meta, lore);
		}
		
		// Wykończenie
        item.setItemMeta(meta);
        return item;
	}	
	public static ItemStack dajGłówkę(String url) {
		return dajGłówkę(null, url);
	}
	public static ItemStack dajGłówkę(String nazwa, String url) {
		return dajGłówkę(nazwa, url, (List<String>) null);
	}
	public static ItemStack dajGłówkę(String nazwa, String url, String... lore) {
		return dajGłówkę(nazwa, url, Lists.newArrayList(lore));
	}
	public static ItemStack dajGłówkę(String nazwa, String url, List<String> lore) {
		Config config = new Config("configi/głowy uuid");
		
		UUID uuid;
		String uuidstr = config.wczytajStr(url);
		if (uuidstr == null) {
			uuid = UUID.randomUUID();
			config.ustaw_zapisz(url, uuid.toString());
		} else
			uuid = UUID.fromString(uuidstr);
		
		ItemStack item;
		SkullMeta meta;
		
		item = new ItemStack(Material.PLAYER_HEAD);
		meta = (SkullMeta) item.getItemMeta();
		
		if (nazwa != null)	Func.setDisplayName(meta, koloruj(nazwa));
		if (lore != null)	Func.setLore(meta, koloruj(lore));
		
		GameProfile profile = new GameProfile(uuid, null);
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            Field profileField = Func.dajField(meta.getClass(), "profile");
            profileField.set(meta, profile);
        }
        catch (Throwable error) {
            error.printStackTrace();
        }
		
        item.setItemMeta(meta);
        return item;	
	}
	public static ItemStack dajGłowe(OfflinePlayer p) {
		return ustawGłowe(p, new ItemStack(Material.PLAYER_HEAD));
	}
	public static ItemStack ustawGłowe(OfflinePlayer p, ItemStack item) {
		SkullMeta Cmeta = (SkullMeta) item.getItemMeta();
		Cmeta.setOwningPlayer(p);
		item.setItemMeta(Cmeta);
		return item;
	}
	public static boolean dajWPremium(Player p, int ile) {
		return dajItem(p, Baza.walutaPremium, ile);
	}
	public static boolean dajItem(Player p, ItemStack item, int ile) {
		item = item.clone();
		boolean w = true;
		while (ile > 0) {
			item.setAmount(Math.min(64, ile));
			ile -= item.getAmount();
			w = w && Func.dajItem(p, item);
		}
		return w;
	}
	public static boolean dajItem(Player p, ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR))
			return false;
		if (p.getInventory().firstEmpty() == -1) {
			p.getWorld().dropItem(p.getLocation(), item);
			return false;
		} else {
			p.getInventory().addItem(item);
			return true;
		}
	}
	public static boolean dajItem(Projectile p, ItemStack item) {
		return p.getShooter() != null && dajItem(p, item);
	}
	public static void zabierzItem(Inventory inv, int slot) {
		zabierzItem(slot, inv::getItem, inv::setItem);
	}
	public static void zabierzItem(PlayerInventory inv, EquipmentSlot slot) {
		zabierzItem(slot, inv::getItem, inv::setItem);
	}
	private static <T> void zabierzItem(T slot, Function<T, ItemStack> get, BiConsumer<T, ItemStack> set) {
		ItemStack item = get.apply(slot);
		item.setAmount(item.getAmount() - 1);
		if (item.getAmount() <= 0) {
			item.setType(Material.GLASS_PANE);
			item.setType(Material.AIR);
			set.accept(slot, null);
		} else
			set.accept(slot, item);
	}
	public static boolean zabierzItem(Inventory inv, ItemStack item) {
		// nie testowane
		int doZabrania = item.getAmount();
		for (int i=0; i < inv.getSize(); i++) {
			ItemStack item2 = inv.getItem(i);
			if (item.isSimilar(item2)) {
				if (item2.getAmount() >= doZabrania) {
					item2.setAmount(item2.getAmount() - doZabrania);
					doZabrania -= item2.getAmount();
					inv.setItem(i, item2.getAmount() <= 0 ? null : item2);
				} else {
					inv.setItem(i, null);
					doZabrania -= item2.getAmount();
				}
				
				if (doZabrania <= 0)
					return true;
			}
		}
		
		item = item.clone();
		item.setAmount(item.getAmount() - doZabrania);
		inv.addItem(item);
		return false;
	}
	public static boolean posiada(Inventory inv, List<ItemStack> itemy) {
		List<ItemStack> potrzebne = Lists.newArrayList();
		for (ItemStack item : itemy) potrzebne.add(item.clone());
		
		for (ItemStack _item : inv) {
			Func.wykonajDlaNieNull(_item, item -> {
				int ile = item.getAmount();
				ItemStack pitem;
				for (int i=0; i<potrzebne.size(); i++)
					if ((pitem = potrzebne.get(i)).isSimilar(item)) {
						int a = pitem.getAmount();
						pitem.setAmount(a - ile);
						if (pitem.getAmount() <= 0)
							potrzebne.remove(i--);
						if ((ile -= a) <= 0)
							break;
					}
			});
		}
		
		return potrzebne.isEmpty();
	}
	public static void zabierz(Inventory inv, List<ItemStack> itemy) {
		for (ItemStack _item : itemy) {
			Func.wykonajDlaNieNull(_item, item -> {
				ItemStack iitem;
				item = item.clone();
				for (int i=0; i<inv.getSize() && item.getAmount() > 0; i++)
					if ((iitem = inv.getItem(i)) != null && iitem.isSimilar(item)) {
						int a = iitem.getAmount();
						iitem.setAmount(a - item.getAmount());
						item.setAmount(item.getAmount() - a);
						if (iitem.getAmount() <= 0)
							inv.setItem(i, null);
					}
			});
		}
	}
	
	public static abstract class abstractHolder implements InventoryHolder {
		protected Inventory inv;
		
		public abstractHolder(int rzędy, String nazwa) {
			inv = Func.stwórzInv(this, rzędy, nazwa);
		}
		
		@Override
		public Inventory getInventory() {
			return inv;
		}
	}
	public static class FuncIdHolder implements InventoryHolder {
		private static int _id = 0;
		private final int id;
		private Inventory inv;
		
		private FuncIdHolder() { this.id = _id++; }
		public int getId() { return id; }
		@Override public Inventory getInventory() { return inv; }
	}
	public static Krotka<Inventory, Integer> stwórzInvZId(int rzędy, String nazwa) {
		FuncIdHolder holder = new FuncIdHolder();
		return new Krotka<>(holder.inv = stwórzInv(holder, rzędy, nazwa), holder.id);
	}
	public static Inventory stwórzInv(int rzędy, String nazwa) {
		return stwórzInv(null, rzędy, nazwa);
	}
	public static Inventory stwórzInv(InventoryHolder holder, int rzędy, String nazwa) {
		return Func.createInventory(holder, rzędy * 9, koloruj(nazwa));
	}
	public static void ustawPuste(Inventory inv) {
		ustawPuste(inv, Baza.pustySlot);
	}
	public static void ustawPuste(Inventory inv, ItemStack pustySlot) {
		for (int i=0; i<inv.getSize(); i++)
			inv.setItem(i, pustySlot);
	}
	public static void wypełnij(Inventory inv) {
		int slot;
		while ((slot = inv.firstEmpty()) != -1)
			inv.setItem(slot, Baza.pustySlot);
	}
	public static Inventory CloneInv(Inventory inv, String nazwa) {
		Inventory _inv = Func.createInventory(inv.getHolder(), inv.getSize(), nazwa);
		for (int i=0; i<inv.getSize(); i++)
			_inv.setItem(i, inv.getItem(i).clone());
		return _inv;
	}
	public static int potrzebneRzędy(int sloty) {
		return (sloty - 1) / 9 + 1;
	}
	public static int[] sloty(int potrzebneSloty, int rzędy) {
		if (rzędy == 1)
			switch (potrzebneSloty) {
			case 0: return new int[] {};
			case 1: return new int[] {4};
			case 2: return new int[] {3, 5};
			case 3: return new int[] {2, 4, 6};
			case 4: return new int[] {1, 3, 5, 7};
			case 5: return new int[] {2, 3, 4, 5, 6};
			case 6: return new int[] {1, 2, 3, 5, 6, 7};
			case 7: return new int[] {1, 2, 3, 4, 5, 6, 7};
			case 8: return new int[] {0, 1, 2, 3, 5, 6, 7, 8};
			case 9: return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
			default:return null;
			}
		
		
		int[] sloty = new int[potrzebneSloty];
		
		int ubytek = potrzebneSloty / rzędy;
		int reszta = potrzebneSloty % rzędy;
		
		int index = 0;
		
		int mn = 0;
		
		while (potrzebneSloty > 0) {
			int dodatek = reszta-- > 0 ? 1 : 0;
			potrzebneSloty -= ubytek + dodatek;
			for (int i : sloty(ubytek + dodatek, 1))
				sloty[index++] = mn*9 + i;
			mn++;
		}
		
		
		return sloty;
	}

	
	public static String losujPrzejścieKolorów(String msg) {
		List<Character> cyfry = Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');
		StringBuilder str = new StringBuilder("&%");

		for (int i=0; i < 6; i++) str.append(losuj(cyfry));
		str.append('-');
		for (int i=0; i < 6; i++) str.append(losuj(cyfry));
		
		return Func.koloruj(str + msg);
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
	public static <T> T losuj(T[] zCzego) {
		return zCzego.length == 0 ? null : zCzego[losujWZasięgu(zCzego.length)];
	}
	public static <T> T losuj(Iterable<T> zCzego) {
		return losuj(Lists.newArrayList(zCzego));
	}
	public static <T> T losuj(List<T> zCzego) {
		if (zCzego.isEmpty())
			return null;
		return zCzego.get(losujWZasięgu(zCzego.size()));
	}
	public static String losujZnaki(int min, int max, String alfabet) {
		StringBuilder str = new StringBuilder();
		
		char[] znaki = alfabet.toCharArray();
		int ile = Func.losuj(min, max);
		for (int i=0; i < ile; i++)
			str.append(znaki[losujWZasięgu(znaki.length)]);
		
		return str.toString();
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
		liczba  = (int) liczba;
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
		Object obj = max(iterable, (a, b) -> Math.max(a, b));
		if (obj == null)
			return 0;
		return (int) obj;
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
	/**
	 * 
	 * @param <T> typ objektów listy
	 * @param numer szukany numer (rezulatat "wartość")
	 * @param posortowanaLista posortowana rosnąco Lista według wartości wynikających z "wartość"
	 * @param wartość funkcja zwracająca wartość dla obiektu listy
	 * @return zwraca obiekt którego wartość == "numer", jesli
	 * jeśli na osi punkt od "numer" nie istnieje, zwrócony zostanie istniejący punkt po jego prawej stronie
	 */
	public static <T> T wyszukajBinarnieP(double numer, List<T> posortowanaLista, Function<T, Double> wartość) {
		return posortowanaLista.get(wyszukajBinarniePIndex(numer, posortowanaLista, wartość));
	}
	public static <T> int wyszukajBinarniePIndex(double numer, List<T> posortowanaLista, Function<T, Double> wartość) {
		int l = 0;
		int r = posortowanaLista.size() - 1;
		
		while (l < r) {
			int s = l + ((r - l) / 2);
			double w = wartość.apply(posortowanaLista.get(s));
			
			if (w < numer)
				l = s + 1;
			else
				r = s;
		}
		return l;
	}
	public static <T> int insort(T obj, List<T> posortowanaLista, Function<T, Double> wartość) {
		int index = wyszukajBinarniePIndex(wartość.apply(obj), posortowanaLista, wartość);
		posortowanaLista.add(index, obj);
		return index;
	}
	public static double stringToDouble(String str) {
		double w = 0;
		double dzielnik = 1;
		for (char znak : Func.usuńKolor(Func.odpolszcz(str)).toCharArray()) {
			w += znak / dzielnik;
			dzielnik *= znak;
		}
		return w;
	}
	public static <T> List<T> posortuj(List<T> lista, Function<T, Double> wartość) {
		List<Krotka<T, Double>> nowa = new ArrayList<>();
		
		lista.forEach(el -> Func.insort(new Krotka<>(el, wartość.apply(el)), nowa, k -> k.b));
		
		lista.clear();
		nowa.forEach(krotka -> lista.add(krotka.a));
		
		return lista;
	}
	public static <T> int wyszukajBinarnieLIndex(double numer, List<T> posortowanaLista, Function<T, Double> wartość) {
		int l = 0;
		int r = posortowanaLista.size() - 1;
		
		while (l < r) {
			int s = (int) Math.round(l + ((r - l) / 2d));
			double w = wartość.apply(posortowanaLista.get(s));
			
			if (w == numer)
				return s;
			if (w > numer)
				r = s - 1;
			else
				l = s;
		}
		return r;
	}
	public static boolean xWZakresie(String zakres, int x) {
		Integer min = null, max = null;
		List<String> min_max = Func.tnij(zakres, "..");
		if (min_max.size() == 1)
			min = max = Func.Int(min_max.get(0));
		else {
			Function<String, Integer> Int = str -> str.isEmpty() ? null : Func.Int(str);
			min = Int.apply(min_max.get(0));
			max = Int.apply(min_max.get(1));
		}
		
		return (min == null || min <= x) && (max == null || x <=max);
	}
	
	public static boolean wyjmijPlik(String co, String gdzie) {
		try {
			return wezZJara(co, in -> {
					int read;
					byte[] bytes = new byte[1024];
					try (OutputStream out = new FileOutputStream(new File(gdzie))) {
						while ((read = in.read(bytes)) != -1)
							out.write(bytes, 0, read);
					}
					return true;
			});
		} catch (NullPointerException e) {
			return false;
		}
	}
	public static String wczytajZJara(String co) {
		return wezZJara(co, Func::readFullString);
	}
	@SuppressWarnings("resource")
	private static <T> T wezZJara(String co, FunctionN<InputStream, T> func) {
		String nazwaPluginu = Main.plugin.getName();
		try {
			JarFile jar = new JarFile("plugins/" + nazwaPluginu + ".jar");
			JarEntry plik = jar.getJarEntry("me/jomi/" + nazwaPluginu + "/" + co);
			if (plik == null)
				return null;
			try (InputStream in = jar.getInputStream(plik)) {
				return func.apply(in);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readFullString(InputStream in) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b); 
		
		int read;
        byte[] bytes = new byte[1024];
    	while ((read = in.read(bytes)) != -1)
			out.write(bytes, 0, read);
        
        return b.toString("utf-8");
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
					lista.add(Class.forName(sc, false, Main.classLoader));
				} catch (Throwable e) {}
			}
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lista;
	}

	public static String nieNull(String str) {
		return str == null ? "" : str;
	}
	public static <T> List<T> nieNull(List<T> lista) {
		return lista != null ? lista : Lists.newArrayList();
	}
	public static ItemStack nieNull(ItemStack item) {
		return item != null ? item : Func.stwórzItem(Material.POPPY);
	}
	
	public static String ostatni(String[] stringi) {
		if (stringi.length == 0) return "";
		return stringi[stringi.length-1];
	}
	public static List<String> tnij(String napis, String regex) {
		final List<String> lista = Lists.newArrayList();
		if (napis == null || napis.isEmpty()) return lista;
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
	
	public static <T> void forEach(T[] args, Consumer<T> cons) {
		for (T arg : args)
			cons.accept(arg);
	}
	public static <T> void forEach(T[] args, Predicate<T> warunek, Consumer<T> cons) {
		for (int i=0; i < args.length; i++)
			if (warunek.test(args[i]))
				cons.accept(args[i]);
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

	public static Object wczytajJSON(String ścieżka) {
		return wczytajJSON(ścieżka, "UTF-8");
	}
	public static Object wczytajJSON(String ścieżka, String kodowanie) {
		if (!ścieżka.endsWith(".json"))
			ścieżka += ".json";
		try {
			JSONParser parser = new JSONParser();
			InputStreamReader in = new InputStreamReader(new FileInputStream(ścieżka), kodowanie);
			return parser.parse(new BufferedReader(in));
		} catch (IOException | ParseException e) {
			return null;
		}
	}
	
	public static boolean zawiera(Location loc, Location róg1, Location róg2) {
		return zawiera(true, true, true, loc, róg1, róg2);
	}
	public static boolean zawiera(boolean x, boolean y, boolean z, Location loc, Location róg1, Location róg2) {
		Predicate<Function<Location, Double>> mieściSię = func -> {
			double co = func.apply(loc);
			double x1 = func.apply(róg1);
			double x2 = func.apply(róg2);
			return  (co >= x1 && co <= x2) || 
					(co <= x1 && co >= x2);
		};
		return  (!x || mieściSię.test(Location::getX)) &&
				(!y || mieściSię.test(Location::getY)) &&
				(!z || mieściSię.test(Location::getZ));
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
	/**
	 * 
	 * @param str string z czasem
	 * @return sekundy
	 * @throws NumberFormatException gdy format str jest niepoprawny
	 */
	public static int czas(String str) throws NumberFormatException {
		str = str.replace("", "");
		
		int w = 0;
		int mn = 0;
		StringBuilder liczba = new StringBuilder();
		while (!str.isEmpty()) {
			char c = str.charAt(0);
			str = str.substring(1);
			switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				liczba.append(c);
				break;
			case '-':
				if (liczba.isEmpty())
					liczba.append(c);
				else
					throw new NumberFormatException();
				break;
			case 'd': mn = 8640;break;
			case 'g': mn = 360;	break;
			case 'm': mn = 60;	break;
			case 's': mn = 1; 	break;
			default:
				throw new NumberFormatException();
			}
			if (mn != 0) {
				w += Func.Int(liczba.toString()) * mn;
				mn = 0;
				liczba = new StringBuilder();
			}
		}
		
		if (!liczba.toString().isEmpty())
			w += Func.Int(liczba.toString());
		
		return w;
		
	}
	
	public static String permisja(String permisja) {
		permisja = permisja.toLowerCase();
		String plugin = Main.plugin.getName().toLowerCase();
		if (!permisja.startsWith(plugin + '.'))
			permisja = plugin + '.' + permisja;
		return permisja;
	}

	public static boolean powiadom(CommandSender p, String msg, Object... uzupełnienia) {
		return powiadom(p, Func.msg(msg, uzupełnienia), true);
	}
	public static boolean powiadom(CommandSender p, String msg, boolean zwrot) {
		p.sendMessage(msg);
		return zwrot;
	}
	public static boolean powiadom(String prefix, CommandSender sender, String msg, Object... uzupełnienia) {
		return powiadom(sender, prefix + Func.msg(msg, uzupełnienia));
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
		if (ticki > 0)
			return Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, lambda, ticki);
		else
			return Bukkit.getScheduler().runTask(Main.plugin, lambda).getTaskId();
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
	public static Player gracz(CommandSender sender, String selektor) {
		Player p = Bukkit.getPlayer(selektor);
		if (p == null) {
			for (Entity e : Bukkit.selectEntities(sender, selektor))
				if (e instanceof Player)
					return (Player) e;
			if (p == null)
				Func.powiadom("", sender, "Gracz %s nie jest aktualnie online!", selektor);
		}
		return p;
	}
	
	public static boolean porównaj(ItemStack item1, ItemStack item2) {
		if (item1 == null || item2 == null) return item1 == item2;
		return item1.isSimilar(item2);
	}

	// z mapy w obiekt
	public static void zdemapuj(Object obj, Map<String, Object> mapa) {
		if (obj == null) return;
		
		Class<?> clazz = obj.getClass();
		
		for (Entry<String, Object> en : mapa.entrySet())
			try {
				if (en.getKey().equals("==")) continue;
				if (en.getKey().equals("=mimi=")) continue;
				
				Field field = dajField(clazz, en.getKey());
				
				if (!field.isAnnotationPresent(Mapowane.class))
					throw new Throwable();
				
				field.set(obj, zdemapuj_wez(field.getGenericType(), en.getValue()));
				
			} catch (Throwable e) {
				Main.warn("Nieprawidłowa nazwa pola \"" + en.getKey() + "\" przy demapowaniu klasy " + clazz.getName());
			}

		try {
			for (Field field : dajFields(clazz)) {
				Func.ustawAccessible(field);
				if (field.isAnnotationPresent(Mapowane.class) && !field.getDeclaredAnnotation(Mapowane.class).nieTwórz() && field.get(obj) == null) {
					if (List.class.isAssignableFrom(field.getType()))
						field.set(obj, Lists.newArrayList());
					else if (HashMap.class.isAssignableFrom(field.getType()))
						field.set(obj, new HashMap<>());
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
	// z obiektu w mape
	public static Map<String, Object> zmapuj(Object obj) {
		Map<String, Object> mapa = new HashMap<String, Object>();
		Class<?> clazz = obj.getClass();
		for (Field field : dajFields(clazz))
			try {
				if (!field.isAnnotationPresent(Mapowane.class)) continue;
				Func.ustawAccessible(field);
				String name = field.getName();
				mapa.put(name, zmapuj_wez(field.getGenericType(), field.get(obj)));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		return mapa;
	}
	@SuppressWarnings("unchecked")
	private static Object zdemapuj_wez(Type type, Object obj) throws Throwable {
		if (obj == null) return obj;
		
		if (type instanceof ParameterizedType) {
			ParameterizedType Ptype = (ParameterizedType) type;
			
			Type typ = Ptype.getActualTypeArguments()[0];
			
			if (Ptype.getRawType() instanceof Class && ((Class<?>) Ptype.getRawType()).isAssignableFrom(List.class)) {
				List<Object> lista = (List<Object>) obj;
				for (int i=0; i < lista.size(); i++)
					lista.set(i, zdemapuj_wez(typ, lista.get(i)));
			}
			
			
		} else if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isEnum())
				return Func.StringToEnum(clazz, (String) obj);
			
			if (clazz.getName().equals(Object.class.getName()))
				return obj;
			
			if (clazz.isAssignableFrom(ItemStack.class))
				return Config.item(obj);
			if (clazz.isAssignableFrom(Drop.class))
				return Config.drop(obj);
			if (clazz.isAssignableFrom(SelektorItemów.class))
				return Config.selektorItemów(obj);
			if (clazz.isAssignableFrom(Float.TYPE))
				return Func.Float(obj);
		} else {
			Main.error(obj);
			Main.error(obj == null ? null : obj.getClass());
			Main.error("Nieprzewidzany typ przy Demapowaniu: " + type, "\n", type.getClass());
			throw new Error();
		}
		return obj;
	}
	@SuppressWarnings("unchecked")
	private static Object zmapuj_wez(Type type, Object obj) throws Throwable {
		if (obj == null) return obj;
		if (type instanceof ParameterizedType) {
			ParameterizedType typ = (ParameterizedType) type;
			
			if (typ.getRawType() instanceof List) {
				List<Object> lista = (List<Object>) obj;
				for (int i=0; i < lista.size(); i++)
					lista.set(i, zmapuj_wez(typ.getActualTypeArguments()[0], lista.get(i)));
			}
		} else if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isEnum())
				return Func.dajMetode(clazz, "name").invoke(obj);
			if (clazz.isAssignableFrom(ItemStack.class))
				return Config.zserializujItem(obj);
			if (clazz.isAssignableFrom(Drop.class))
				return Config._drop(obj);
			if (clazz.isAssignableFrom(SelektorItemów.class))
				return Config._selektorItemów(obj);
		}
		return obj;
	}
	
	public static <K, V> HashMap<K, V> stwórzMape(K k1, V v1) { 												return _stwórzMape(k1, v1);}
	public static <K, V> HashMap<K, V> stwórzMape(K k1, V v1, K k2, V v2) { 									return _stwórzMape(k1, v1, k2, v2);}
	public static <K, V> HashMap<K, V> stwórzMape(K k1, V v1, K k2, V v2, K k3, V v3) { 						return _stwórzMape(k1, v1, k2, v2, k3, v3);} 
	public static <K, V> HashMap<K, V> stwórzMape(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) { 			return _stwórzMape(k1, v1, k2, v2, k3, v3, k4, v4);}
	public static <K, V> HashMap<K, V> stwórzMape(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) { return _stwórzMape(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);}
	@SuppressWarnings("unchecked")
	private static <K, V> HashMap<K, V> _stwórzMape(Object... objs) {
		HashMap<K, V> mapa = new HashMap<>();
		for (int i=0; i < objs.length; i += 2)
			mapa.put((K) objs[i], (V) objs[i+1]);
		return mapa;
	}
	@SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> stwórzMape(Krotka<K, V>... krotki) {
		HashMap<K, V> mapa = new HashMap<>();
		for (Krotka<K, V> krotka : krotki)
			mapa.put(krotka.a, krotka.b);
		return mapa;
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
		wykonajDlaNieNull(obj, o -> true, func);
	}
	@SuppressWarnings("unchecked")
	public static <T> void wykonajDlaNieNull(Object obj, Class<T> clazz, Consumer<T> func) {
		if (clazz.isInstance(obj))
			wykonajDlaNieNull((T) obj, func);
	}
	public static <T> void wykonajDlaNieNull(T obj, Predicate<T> warunek, Consumer<T> func) {
		wykonajDlaNieNull(obj, warunek, func, null);
	}
	@SuppressWarnings("unchecked")
	public static <T> void wykonajDlaNieNull(Object obj, Class<T> clazz, Predicate<T> warunek, Consumer<T> func) {
		if (clazz.isInstance(obj))
			wykonajDlaNieNull((T) obj, warunek, func);
	}
	@SuppressWarnings("unchecked")
	public static <T> void wykonajDlaNieNull(Object obj, Class<T> clazz, Consumer<T> func, Runnable dlaObjNull) {
		if (clazz.isInstance(obj))
			wykonajDlaNieNull((T) obj, func, dlaObjNull);
	}
	public static <T> void wykonajDlaNieNull(T obj, Consumer<T> func, Runnable dlaObjNull) {
		wykonajDlaNieNull(obj, o -> true, func, dlaObjNull);
	}
	public static <T> void wykonajDlaNieNull(T obj, Predicate<T> warunek, Consumer<T> func, Runnable dlaObjNull) {
		if (obj != null && warunek.test(obj))
			func.accept(obj);
		else if (dlaObjNull != null)
			dlaObjNull.run();
	}

	public static <T, R> R zwrotDlaNieNull(T obj, Function<T, R> func) {
		return zwrotDlaNieNull(obj, func, null);
	}
	public static <T, R> R zwrotDlaNieNull(T obj, Function<T, R> func, R domyslna) {
		if (obj != null)
			return func.apply(obj);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T pewnyCast(Object obj) {
		return (T) obj;
	}
	
	public static <T> T domyślna(T obj, T domyślna) {
		return obj != null ? obj : domyślna;
	}
	public static <T> T domyślna(T obj, Supplier<T> domyślna) {
		return obj != null ? obj : domyślna.get();
	}
	public static <T> T domyślnaTry(Supplier<T> obj, T domyślna) {
		try {
			return obj.get();
		} catch (Throwable e) {
			return domyślna;
		}
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
	public static boolean dziedziczy(Class<?> clazz1, Class<?> clazz2) {
		while (clazz1 != null) {
			if (clazz1.isAssignableFrom(clazz2))
				return true;
			clazz1 = clazz1.getSuperclass();
		}
		return false;
	}

	public static void ustawAccessible(AccessibleObject ao) {
		ustawAccessible(ao, true);
	}
	public static void ustawAccessible(AccessibleObject ao, boolean accessible) {
		if (System.getSecurityManager() == null)
			ao.setAccessible(accessible);
		else
			AccessController.doPrivileged((PrivilegedAction<?>) () -> {
				ao.setAccessible(accessible);
				return null;
			});
	}
	public static Object dajZField(Object obj, String nazwa) throws Throwable {
		return dajField(obj.getClass(), nazwa).get(obj);
	}
	public static Field dajField(Class<?> clazz, String nazwa) {
		return dajZKlasy(clazz, NoSuchFieldException.class, klasa -> klasa.getDeclaredField(nazwa));
	}
	public static Method dajMetode(Class<?> clazz, String nazwa, Class<?>... klasy) throws Throwable {
		try {
			return dajZKlasy(clazz, NoSuchMethodException.class, klasa -> klasa.getDeclaredMethod(nazwa, klasy));
		} catch (Throwable e) {
			return dajZKlasy(clazz, NoSuchMethodException.class, klasa -> klasa.getMethod(nazwa, klasy));
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> dajKonstruktor(Class<T> clazz, Class<?>... klasy) throws Throwable {
		return (Constructor<T>) dajZKlasy(clazz, NoSuchMethodException.class, klasa -> klasa.getDeclaredConstructor(klasy));
	}
	private static interface dajZKlasyInterface<R> {
		R call(Class<?> clazz) throws Throwable;
	}
	private static <T extends AccessibleObject> T dajZKlasy(Class<?> clazz, Class<?> error, dajZKlasyInterface<T> getDeclared) {
		try {
			T obj = getDeclared.call(clazz);
			Func.ustawAccessible(obj);
			return obj;
		} catch (Throwable e) {
			if (e.getClass().isAssignableFrom(error))
				if (clazz.getName().equals(Object.class.getName()))
					throw Func.throwEx(e);
				else
					return dajZKlasy(clazz.getSuperclass(), error, getDeclared);
			throw Func.throwEx(e);
		}	
	}
	public static List<Field> dajFields(Class<?> clazz) {
		return dajZKlasy(clazz, Sets.newConcurrentHashSet(), Class::getDeclaredFields, Field::getName);
	}
	public static List<Method> dajMetody(Class<?> clazz) {
		List<Method> lista = dajZKlasy(clazz, null, Class::getDeclaredMethods, Method::getName);
		dajZKlasy(clazz, null, Class::getMethods, null).forEach(lista::add);
		return lista;
		
	}
	private static <T extends AccessibleObject> List<T> dajZKlasy(Class<?> clazz, Set<String> nazwy, Function<Class<?>, T[]> getDeclared, Function<T, String> name) {
		List<T> lista = Lists.newArrayList();
		
		if (clazz == null)
			return lista;
		
		for (T obj : getDeclared.apply(clazz))
			if (nazwy == null || nazwy.add(name.apply(obj))) {
				try {
					Func.ustawAccessible(obj);
					lista.add(obj);
				} catch (InaccessibleObjectException e) {
				}
			}

		if (!clazz.getName().equals(Object.class.getName()))
			for (T obj : dajZKlasy(clazz.getSuperclass(), nazwy, getDeclared, name))
				lista.add(obj);
		
		return lista;
	}
	public static List<Class<?>> dajKlasy(Class<?> clazz) {
		List<Class<?>> lista = Lists.newArrayList(clazz);
		
		while (!clazz.getName().equals(Object.class.getName())) {
			if (clazz.isAssignableFrom(Integer.class))	lista.add(int.class);
			if (clazz.isAssignableFrom(Float.class))	lista.add(float.class);
			if (clazz.isAssignableFrom(Boolean.class))	lista.add(boolean.class);
			if (clazz.isAssignableFrom(Double.class))	lista.add(double.class);
			if (clazz.isAssignableFrom(Float.class))	lista.add(float.class);
			if (clazz.isAssignableFrom(Character.class))lista.add(char.class);
			lista.add(clazz = clazz.getSuperclass());
		}
		
		return lista;
	}
	@SuppressWarnings("deprecation")
	public static <T> T nowaInstancja(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		try {
			return clazz.getConstructor().newInstance();
		} catch (Throwable e) {}
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Throwable e) {}
		return clazz.newInstance();
	}
	public static <T1, T2> List<T2> wykonajWszystkim(Iterable<T1> lista, Function<T1, T2> func) {
		List<T2> _lista = Lists.newArrayList();
		for (T1 el : lista)
			_lista.add(func.apply(el));
		return _lista;
	}
	public static <T1, T2> List<T2> wykonajWszystkim(T1[] lista, Function<T1, T2> func) {
		return wykonajWszystkim(Lists.newArrayList(lista), func);
	}
	
	public static RuntimeException throwEx(Throwable t) {
		if (t == null)
			throw new NullPointerException();
		throw throwEx0(t);
	}
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T throwEx0(Throwable t) throws T {
		throw (T) t;
	}
	
	public static <T> List<T> przefiltruj(T[] lista, Predicate<T> warunek) {
		return przefiltruj(Lists.newArrayList(lista), warunek);
	}
	public static <T> List<T> przefiltruj(Iterable<T> lista, Predicate<T> warunek) {
		List<T> _lista = Lists.newArrayList();
		for (T el : lista)
			if (warunek.test(el))
				_lista.add(el);
		return _lista;
	}
	
	public static <T1, T2> List<Krotka<T1, T2>> zip(Iterable<T1> c1, Iterable<T2> c2) {
		Iterator<T1> it1 = c1.iterator();
		Iterator<T2> it2 = c2.iterator();
		
		List<Krotka<T1, T2>> lista = Lists.newArrayList();
		
		while (it1.hasNext() && it2.hasNext())
			lista.add(new Krotka<>(it1.next(), it2.next()));
		
		return lista;
	}
	public static <T1, T2> void zip(Iterable<T1> c1, Iterable<T2> c2, BiConsumer<T1, T2> func) {
		zip(c1, c2).forEach(k -> func.accept(k.a, k.b));
	}
	public static List<?> zip(Iterable<?>... c) {
		List<?> lista = zip(c[0], c[1]);
		for (int i = 2; i < c.length; i++)
			lista = zip(c[i], lista);
		return lista;
	}
	
	public static interface FunctionXYZ<T> {
		public T wykonaj(int x, int y, int z);
	}
	public static class IteratorBloków<T> implements Iterator<T> {
		Krotka<Integer, Integer> kx;
		Krotka<Integer, Integer> ky;
		Krotka<Integer, Integer> kz;
		boolean next = true;
		int x;
		int y;
		int z;
		
		FunctionXYZ<T> func;
		
		public IteratorBloków(Location róg1, Location róg2, FunctionXYZ<T> funkcja) {
			Function<Function<Location, Integer>, Krotka<Integer, Integer>> krotka = 
					func -> Func.minMax(func.apply(róg1), func.apply(róg2), Math::min, Math::max);
			kx = krotka.apply(Location::getBlockX); x = kx.a;
			ky = krotka.apply(Location::getBlockY); y = ky.a;
			kz = krotka.apply(Location::getBlockZ); z = kz.a;
			this.func = funkcja;
		}
		
		@Override
		public boolean hasNext() {
			return next;
		}
		@Override
		public T next() {
			T w = func.wykonaj(x, y, z);
			if (++x > kx.b) {
				x = kx.a;
				if (++z > kz.b) {
					z = kz.a;
					next = ++y <= ky.b;
				}
			}
			return w;
		}
	}
	public static Iterable<Block> bloki(Location róg1, Location róg2) {
		Iterator<Block> iterator = blokiIterator(róg1, róg2);
		return () -> iterator;
	}
	public static Iterator<Block> blokiIterator(Location róg1, Location róg2) {
		World świat = róg1.getWorld();
		return new IteratorBloków<>(róg1, róg2, świat::getBlockAt);
	}
	public static void wykonajNaBlokach(Location róg1, Location róg2, Predicate<Block> cons) {
		wykonajNaBlokach(róg1, róg2, cons, null);
	}
	public static void wykonajNaBlokach(Location róg1, Location róg2, Predicate<Block> cons, Runnable callback) {
		wykonajNaBlokach(blokiIterator(róg1, róg2), cons, callback);
	}
	private static void wykonajNaBlokach(Iterator<Block> it, Predicate<Block> cons, Runnable callback) {
		int mx = Baza.BudowanieAren.maxBloki;
		int licz = 0;
		
		while (it.hasNext())
			if (cons.test(it.next()) && ++licz >= mx) {
				Func.opóznij(Baza.BudowanieAren.tickiPrzerw, () -> wykonajNaBlokach(it, cons, callback));
				return;
			}
		Func.wykonajDlaNieNull(callback, Runnable::run);
	}
	public static void wykonajNaBlokach(Location róg1, Location róg2, FunctionXYZ<Boolean> cons) {
		wykonajNaBlokach(new IteratorBloków<>(róg1, róg2, cons));
	}
	private static void wykonajNaBlokach(Iterator<Boolean> it) {
		//int mx = Main.ust.wczytajLubDomyślna("Budowanie Aren.Max Bloki", 50_000); TODO wczytywać w bazie
		//Main.ust.wczytajLubDomyślna("Budowanie Aren.Ticki Przerw", 1)
		int mx = Baza.BudowanieAren.maxBloki;
		int licz = 0;
		
		while (it.hasNext())
			if (it.next() && ++licz >= mx) {
				Func.opóznij(Baza.BudowanieAren.tickiPrzerw, () -> wykonajNaBlokach(it));
				return;
			}
	}
	
	public static void tpSpawn(Player p) {
		p.teleport(spawn(p));
	}
	public static Location spawn(Player p) {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsSpawn")) {
            EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
            Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            return essentialsSpawn.getSpawn(essentials.getUser(p).getGroup());
        } else {
            World świat = Bukkit.getWorld("world");
            return (świat == null ? Bukkit.getWorlds().get(0) : świat).getSpawnLocation();
        }
	}
	
	public static boolean wklejSchemat(String schematScieżka, Location loc) {
		return wklejSchemat(schematScieżka, loc.getWorld(), locToVec3(loc));
	}
	public static boolean wklejSchemat(String schematScieżka, World świat, int x, int y, int z) {
		return wklejSchemat(schematScieżka, świat, BlockVector3.at(x, y, z));
	}
	public static boolean wklejSchemat(String schematScieżka, World świat, BlockVector3 vec3) {
		File file = new File(schematScieżka);
		try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file));
				EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(świat))) {
			Operations.complete(
					new ClipboardHolder(reader.read())
		            .createPaste(editSession)
		            .to(vec3)
		            .ignoreAirBlocks(true)
		            .build()
		            );
		} catch (IOException e) {
			return false;
		} catch (WorldEditException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static RegionManager regiony(World świat) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(świat));
	}
	public static BlockVector3 locToVec3(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}
	
    public static void zipFile(String sourceFile, String gdzie) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(gdzie.endsWith(".zip") ? gdzie : (gdzie + ".zip"))) {
			try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
				File fileToZip = new File(sourceFile);
				zipFile(fileToZip, fileToZip.getName(), zipOut);
			}
		}
    }
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            
            for (File childFile : fileToZip.listFiles())
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
			zipOut.putNextEntry(new ZipEntry(fileName));
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0)
				zipOut.write(bytes, 0, length);
		}
    }
	public static void unzipFile(String fileZip, String gdzie) throws IOException {
		File destDir = new File(gdzie);
		byte[] buffer = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
			    File newFile = newFile(destDir, zipEntry);
			    if (zipEntry.isDirectory()) {
			        if (!newFile.isDirectory() && !newFile.mkdirs())
						throw new IOException("Failed to create directory " + newFile);
			    } else {
			        // fix for Windows-created archives
			        File parent = newFile.getParentFile();
			        if (!parent.isDirectory() && !parent.mkdirs())
						throw new IOException("Failed to create directory " + parent);
			        
			        // write file content
		            try (FileOutputStream fos = new FileOutputStream(newFile)) {
		            	int len;
		            	while ((len = zis.read(buffer)) > 0)
		            		fos.write(buffer, 0, len);
		            };
			    }
			    	zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        if (!destFile.getCanonicalPath().startsWith(destinationDir.getCanonicalPath() + File.separator))
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());

        return destFile;
    }
	
    public static void usuń(File plik) {
    	if (plik.isDirectory())
    		Func.forEach(plik.listFiles(), Func::usuń);
    	plik.delete();
    }
    
	public static void particle(Particle particle, Location loc, int ilość, double dx, double dy, double dz, double prędkość) {
		loc.getWorld().spawnParticle(particle, loc, ilość, dx, dy, dz, prędkość);
	}
	public static void particle(Location loc, int ilość, double dx, double dy, double dz, double prędkość, Color kolor, float wielkość) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc, ilość, dx, dy, dz, prędkość, new Particle.DustOptions(kolor, wielkość));
	}
	public static void particle(Player p, Location loc, int ilość, double dx, double dy, double dz, double prędkość, Color kolor, float wielkość) {
		p.spawnParticle(Particle.REDSTONE, loc, ilość, dx, dy, dz, prędkość, new Particle.DustOptions(kolor, wielkość));
	}
	public static void particle(Location loc, Location loc2, double step, BiConsumer<Location, Integer> bic) {
		loc = loc.clone();
		int steps = (int) (loc.distance(loc2) / step);
		double x = Math.abs(loc.getX() - loc2.getX()) / steps;
		double y = Math.abs(loc.getY() - loc2.getY()) / steps;
		double z = Math.abs(loc.getZ() - loc2.getZ()) / steps;

		if (loc.getX() > loc2.getX()) x *= -1;
		if (loc.getY() > loc2.getY()) y *= -1;
		if (loc.getZ() > loc2.getZ()) z *= -1;
		
		for (int i=0; i < steps; i++) {
			bic.accept(loc, i);
			loc.add(x, y, z);
		}
		bic.accept(loc2, steps);
	}

	public static String replaceGroup(Pattern pattern, String co, int grupa, String naCo) {
	    return replaceGroup(pattern, co, grupa, 1, naCo);
	}
	public static String replaceGroup(Pattern pattern, String co, int grupa, int ile, String naCo) {
	    Matcher m = pattern.matcher(co);
	    for (int i = 0; i < ile; i++)
	        if (!m.find()) return co;
	    return new StringBuilder(co).replace(m.start(grupa), m.end(grupa), naCo).toString();
	}

	public static ArmorStand zrespNietykalnyArmorStand(Location loc) {
		return zrespNietykalnyArmorStand(loc, null);
	}
	public static ArmorStand zrespNietykalnyArmorStand(Location loc, String nick) {
		ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		
		if (nick != null) {
			armorStand.setCustomName(nick);
			armorStand.setCustomNameVisible(true);
		}
		armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.HAND,  	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.HEAD,  	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.CHEST,	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.LEGS,		LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.FEET,  	LockType.ADDING_OR_CHANGING);
		armorStand.setInvulnerable(true);
		armorStand.setCollidable(false);
		armorStand.setInvisible(true);
		armorStand.setGravity(false);
		armorStand.setSmall(true);
		armorStand.teleport(loc);
		
		return armorStand;
	}
	
	private static final File backUpFile = new File("plugins/mimiRPG/backupy");
	public static void backUp(File file) {
		backUp(file, Baza.ilośćBackapów);
	}
	public static void backUp(File file, int limit) {
		if (file.length() == 0L) return;
		try {
			File dir = new File(backUpFile, file.getName().substring(0, file.getName().indexOf('.')));
			dir.mkdirs();
			
			Function<Integer, File> getFile = nr -> new File(dir, file.getName() + (nr != 0 ? " (" + nr + ")" : ""));
			
			if (backUp(getFile.apply(0), file)) return;
			
			for (int licz = 1; licz < limit; licz++)
				if (backUp(getFile.apply(licz), file))
					return;
			
			getFile.apply(0).delete();
			for (int licz = 1; licz < limit; licz++)
				getFile.apply(licz).renameTo(getFile.apply(licz - 1));
			
			if (!backUp(getFile.apply(limit - 1), file))
				Main.warn(Func.prefix("Backup") + Func.msg("nie można zbackapować pliku %s", file.getName()));
		} catch (Throwable e) {
			Main.warn(Func.prefix("Backup") + Func.msg("Nie można zrobić backupu pliku %s", file.getAbsolutePath()));
			e.printStackTrace();
		}
	}
	private static boolean backUp(File gdzie, File co) {
		if (gdzie.exists()) return false;
		
		try {
			gdzie.createNewFile();
			
			FileInputStream  in  = new FileInputStream(co);
			FileOutputStream out = new FileOutputStream(gdzie);
			
			out.write(in.readAllBytes());
			
			in.close();
			out.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
