package me.jomi.mimiRPG.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.TextComponentSerializer;

import me.jomi.mimiRPG.Main;


public class Napis implements ConfigurationSerializable {
	TextComponent txt;
	
	public Napis(TextComponent tekst) {
		txt = tekst;
	}
	public Napis(String tekst) {
		this(new TextComponent(tekst));
	}
	public Napis(String tekst, String hover) {
		this(tekst);
		this.hover(hover);
	}
	public Napis(String tekst, ClickEvent.Action akcja, String treść) {
		this(tekst);
		this.clickEvent(akcja, treść);
	}
	public Napis(String tekst, String hover, ClickEvent.Action akcja, String treść) {
		this(tekst, hover);
		this.clickEvent(akcja, treść);
	}
	public Napis(String tekst, String hover, String treść, ClickEvent.Action akcja) {
		this(tekst, hover, akcja, treść);
	}
	public Napis(String tekst, String hover, String executowanaKomenda) {
		this(tekst, hover, executowanaKomenda.endsWith(">> ") ? Action.SUGGEST_COMMAND : Action.RUN_COMMAND, executowanaKomenda);
	}
	public Napis() {
		this("");
	}
	
	public Napis hover(String tekst) {
		Text t = new Text(Func.koloruj(tekst));
		txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, t));
		return this;
	}
	public Napis clickEvent(ClickEvent.Action akcja, String treść) {
		txt.setClickEvent(new ClickEvent(akcja, treść));
		return this;
	}
	public Napis clickEvent(String treść) {
		return clickEvent(treść.endsWith(">> ") ? Action.SUGGEST_COMMAND : Action.RUN_COMMAND, treść);
	}

	public Napis dodajK(String co) {
		return dodaj(Func.koloruj(co));
	}
	public Napis dodajEndK(String... co) {
		for (String str : co)
			dodaj(Func.koloruj(str)).dodaj("\n");
		return this;
	}
	public Napis dodajEnd(String... co) {
		for (String str : co)
			dodaj(str).dodaj("\n");
		return this;
	}
	public Napis dodajEnd(Napis... co) {
		for (Napis _co : co)
			dodaj(_co).dodaj("\n");
		return this;
	}
	public Napis dodaj(String... co) {
		for (String _co : co)
			txt.addExtra(_co);
		return this;
	}
	public Napis dodaj(Napis... co) {
		for (Napis _co : co)
			txt.addExtra(_co.txt);
		return this;
	}
	public Napis dodaj(TextComponent... co) {
		for (TextComponent _co : co)
			txt.addExtra(_co);
		return this;
	}
	
	public void wyświetl(CommandSender p) {
		if (p instanceof Player) p.spigot().sendMessage(txt);
		else 					 p.sendMessage(this.toString());
	}
	public void wyświetlWszystkim() {
		for (Player p : Bukkit.getOnlinePlayers())
			wyświetl(p);
		wyświetl(Bukkit.getConsoleSender());
	}
	
	
	public static Napis osiągnięcie(String datapack, String osiągnięcie) {
		String sciezka = "world/datapacks/"+datapack+"/data/"+datapack+"/advancements/"+osiągnięcie+".json";
		JSONObject plik;
		JSONParser parser = new JSONParser();
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(sciezka), "UTF-8");
			plik = (JSONObject) parser.parse(new BufferedReader(in));
			JSONObject display = (JSONObject) plik.get("display");
			String nazwa = _osiągnięcie((JSONObject) display.get("title"));
			String opis  = _osiągnięcie((JSONObject) display.get("description"));
			return new Napis(nazwa, nazwa + "\n" + opis);
		} catch (IOException | ParseException e) {
			Bukkit.getLogger().warning("Nie poprawny plik " + sciezka);
			e.printStackTrace();
		}
		return new Napis("Osiągnięcie");
	}
	@SuppressWarnings("unchecked")
	private static String _osiągnięcie(JSONObject obj) {
		if (obj == null) return "";
		String w = (String) obj.getOrDefault("text", "Osiągnięcie");
		ChatColor kolor = ChatColor.valueOf(((String) obj.getOrDefault("color", "white")).toUpperCase());
		String pref = kolor + "";
		Object bold = obj.get("bold");
		if (bold != null && (boolean) bold)
			pref += "§l";
		return pref + w;
	}
	
	public static Napis item(ItemStack item) {
		if (item == null)
			return new Napis("[brak itemu]");
		
		String ver = Bukkit.getServer().getClass().getName();
		ver = ver.replace("org.bukkit.craftbukkit.", "");
		ver = ver.replace("." + Bukkit.getServer().getClass().getSimpleName(), "");
		
		try {
			Class<?> classCraftItemStack = Class.forName("org.bukkit.craftbukkit." + ver + ".inventory.CraftItemStack", false, Main.classLoader);
			Object nmsItem = Func.dajMetode(classCraftItemStack, "asNMSCopy", ItemStack.class).invoke(null, item);
			
			String nazwa;
			if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
				nazwa = Func.getDisplayName(item.getItemMeta());
			else {
				Object nazwaItemu = Func.dajMetode(nmsItem.getClass(), "getName").invoke(nmsItem);
				nazwa = (String) Func.dajMetode(nazwaItemu.getClass(), "getString").invoke(nazwaItemu);
			}
			
			Napis n = new Napis("§b[" + (item.getAmount() != 1 ? "§9"+item.getAmount() + "§3x§b " : "") + nazwa + "§b]§r");
			Item b = new Item(
					item.getType().toString().toLowerCase(),
					item.getAmount(),
					ItemTag.ofNbt(Func.dajMetode(nmsItem.getClass(), "getOrCreateTag").invoke(nmsItem).toString()));
			n.txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, b));
				
			return n;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return txt.toLegacyText() + "§r";
	}
	@Override
	public Napis clone() {
		Napis n = new Napis();
		n.txt = txt.duplicate();
		return n;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Napis)
			return txt.equals(((Napis) obj).txt);
		return false;
	}

	static final Gson gson = new GsonBuilder().registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).create();
	public static Napis zRawJson(String rawJson) {
		return new Napis(gson.fromJson(rawJson, TextComponent.class));
	}
	public JsonElement wJson() {
		return gson.toJsonTree(txt);
	}
	
	public static Napis wczytaj(String napis) {
		Napis n = new Napis();
		Napis ost = new Napis();
		String str;
		for (String s : Func.tnij(Func.koloruj(napis), "||")) {
			if ((str = _sprawdz(s, "hover: ", "h: ")) != null)
				ost.hover(s.substring(str.length()));
			else if ((str = _sprawdz(s, "komenda: ", "k: ")) != null)
				ost.clickEvent(Action.RUN_COMMAND, s.substring(str.length()));
			else if ((str = _sprawdz(s, "komenda<", "k<")) != null) {
				String akcja =  s.substring(str.length(), s.indexOf(">"));
				String komenda = s.substring(str.length() + 3 + akcja.length()); // 3 = ">: ".length()
				ost.clickEvent(Action.valueOf(_akcja(akcja)), komenda);
			}
			else {
				n.dodaj(ost);
				ost = new Napis(s.replace("\\n", "\n"));
			}
		}
		n.dodaj(ost);
		
		return n;
	}
	private static String _sprawdz(String str, String... sprawdzane) {
		for (String alias : sprawdzane)
			if (str.startsWith(alias))
				return alias;
		return null;
	}
	private static String _akcja(String akcja) {
		if (akcja.length() == 1)
			switch (akcja.toLowerCase()) {
			case "r": return "RUN_COMMAND";
			case "s": return "SUGGEST_COMMAND";
			case "c": return "COPY_TO_CLIPBOARD";
			case "o": return "OPEN_URL";
			}
		return akcja.toUpperCase();
	}
	@SuppressWarnings("unchecked")
	public Napis(Map<String, Object> mapa) {
		txt = new TextComponent(Func.koloruj((String) mapa.get("text")));
		
		if (mapa.containsKey("hover"))
			hover(Func.koloruj((String) mapa.get("hover")));
		
		if (mapa.containsKey("komenda"))
			clickEvent(Action.valueOf(((String) mapa.getOrDefault("akcja", "RUN_COMMAND")).toUpperCase()),
					 Func.koloruj((String)mapa.get("komenda")));
		
		if (mapa.containsKey("extra"))
			dodaj(new Napis((Map<String, Object>) mapa.get("extra")));
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("text", txt.getText());
		mapa.put("hover", txt.getHoverEvent().getContents().get(0).toString());
		mapa.put("komenda", txt.getClickEvent().getValue());
		mapa.put("akcja", txt.getClickEvent().getAction().toString());
		return mapa;
	}
}
