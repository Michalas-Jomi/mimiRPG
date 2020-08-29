package me.jomi.mimiRPG;

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
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class Napis implements ConfigurationSerializable {
	private TextComponent txt;
	
	public Napis(String tekst, String hover, ClickEvent.Action akcja, String treść) {
		txt = new TextComponent(tekst);
		this.clickEvent(akcja, treść);
		this.hover(hover);
	}
	public Napis(String tekst, ClickEvent.Action akcja, String treść) {
		txt = new TextComponent(tekst);
		this.clickEvent(akcja, treść);
	}
	public Napis(String tekst, String hover, String executowanaKomenda) {
		txt = new TextComponent(tekst);
		this.clickEvent(Action.RUN_COMMAND, executowanaKomenda);
		this.hover(hover);
	}
	public Napis(String tekst, String hover) {
		txt = new TextComponent(tekst);
		this.hover(hover);
	}
	public Napis(String tekst) {
		txt = new TextComponent(tekst);
	}
	public Napis() {
		txt = new TextComponent("");
	}
	
	public void hover(String tekst) {
		TextComponent[] h = {new TextComponent(tekst)};
		txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, h));
	}
	public void clickEvent(ClickEvent.Action akcja, String treść) {
		txt.setClickEvent(new ClickEvent(akcja, treść));
	}

	public void dodaj(String co) {
		txt.addExtra(co);
	}
	public void dodaj(Napis co) {
		txt.addExtra(co.txt);
	}
	public void dodaj(TextComponent co) {
		txt.addExtra(co);
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
		net.minecraft.server.v1_16_R1.ItemStack item2;
		item2 = CraftItemStack.asNMSCopy(item);
		Napis n = new Napis("§b[§9"+item.getAmount()+"§3x§b " +
				(item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item2.getName().getString()+"§b]§r"));
		TextComponent[] b = {new TextComponent("{id:\""+item.getType().toString().toLowerCase()+"\",Count:1,tag:"+item2.getOrCreateTag()+"}")};
		n.txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, b));
		return n;
	}
	
	public String toString() {
		return txt.toLegacyText() + "§r";
	}
	public Napis clone() {
		Napis n = new Napis();
		n.txt = txt.duplicate();
		return n;
	}
	public boolean equals(Object obj) {
		if (obj instanceof Napis)
			return txt.equals(((Napis) obj).txt);
		return false;
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
				ost = new Napis(s);
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
		mapa.put("hover", txt.getHoverEvent().getValue()[0].toLegacyText());
		mapa.put("komenda", txt.getClickEvent().getValue());
		mapa.put("akcja", txt.getClickEvent().getAction().toString());
		return mapa;
	}
	
}
