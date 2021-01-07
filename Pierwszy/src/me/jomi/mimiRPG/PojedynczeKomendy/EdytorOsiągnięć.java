package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.LepszaMapa;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class EdytorOsiągnięć {
	static class Drzewko {
		// nazwa: drzewko
		final static HashMap<String, Drzewko> mapaDrzew = new HashMap<>();
		// nazwa: osiągnięcie
		final HashMap<String, Osiągnięcie> mapa = new HashMap<>();
		
		String nazwa;
		
		private Drzewko(String nazwa) {
			mapaDrzew.put(nazwa, this);
			this.nazwa = nazwa;
		}
		public static Drzewko wczytaj(String nazwa) {
			return Func.domyślna(mapaDrzew.get(nazwa), () -> new Drzewko(nazwa));
		}
	}
	static class Osiągnięcie {
		Osiągnięcie parent; // null dla roota
		Drzewko drzewko;
		File plik;
		
		Napis opis;
		Napis nazwa;
		ItemStack ikona;
		
		Ramka ramka;
		boolean hidden;
		boolean show_toast;
		boolean announce_to_chat;
		
		@SuppressWarnings("unchecked")
		private Osiągnięcie(Drzewko drzewko, File plik) {
			drzewko.mapa.put(plik.getName().substring(0, plik.getName().length() - 5), this);
			this.drzewko = drzewko;
			this.plik = plik;
			
			LepszaMapa<String> mapa = new LepszaMapa<String>((JSONObject) Func.wczytajJSON(plik.getAbsolutePath()));
			
			LepszaMapa<Object> display = mapa.getLMap("display");
			hidden = display.getBoolean("hidden");
			show_toast = display.getBoolean("show_toast");
			announce_to_chat = display.getBoolean("announce_to_chat");
			ramka = Func.StringToEnum(Ramka.class, display.getString("frame"));
			
			nazwa = Napis.zRawJson(display.getJSONObject("title").toJSONString());
			opis = Napis.zRawJson(display.getJSONObject("description").toJSONString());
			ikona = new Gson().fromJson(display.getJSONObject("icon").toJSONString(), ItemStack.class);
			
			if (mapa.containsKey("parent"))
				parent = wczytaj(mapa.getString("parent"));
		}
		public static Osiągnięcie wczytaj(String scieżka) {
			if (scieżka.contains(":"))
				scieżka = scieżka.substring(scieżka.indexOf(':') + 1);
			String fscieżka = scieżka;
			
			List<String> części = Func.tnij(scieżka, "/");
			
			Drzewko drzewko = Drzewko.wczytaj(części.get(0));
			return Func.domyślna(drzewko.mapa.get(części.get(1)), () -> new Osiągnięcie(drzewko, new File(sc + fscieżka)));
		}
		
		@SuppressWarnings("unchecked")
		public void zapisz() {
			JSONObject jsonDisplay = new JSONObject();
			
			jsonDisplay.put("frame", ramka.name().toLowerCase());
			jsonDisplay.put("hidden", hidden);
			jsonDisplay.put("show_toast", show_toast);
			jsonDisplay.put("announce_to_chat", announce_to_chat);
			
			jsonDisplay.put("title", nazwa.wJson());
			jsonDisplay.put("description", opis.wJson());
			jsonDisplay.put("icon", new Gson().toJsonTree(ikona));

			
			JSONObject json = new JSONObject();
			json.put("display", jsonDisplay);
			json.put("parent", Main.plugin.getName().toLowerCase() + ":" + drzewko.nazwa + "/" + nazwa);
		
			try (FileWriter writer = new FileWriter(plik, StandardCharsets.UTF_8)) {
				writer.write(json.toJSONString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static enum Ramka {
		TASK,
		GOAL,
		CHALLENGE;
	}
	
	
	static String sc;
	public EdytorOsiągnięć() throws IOException {
		String pl = Main.plugin.getName().toLowerCase();
		String scGłówna = "world/datapacks/" + pl + "/";
		sc = scGłówna + "data/" + pl + "/advancements/";
		
		File dir = new File(sc);
		if (!dir.exists())
			dir.mkdirs();
		
		File f = new File(scGłówna + "pack.mcmeta");
		if (!f.exists()) {
			f.createNewFile();
			try (FileWriter writer = new FileWriter(f, StandardCharsets.UTF_8)) {
				writer.write("{\n  \"pack\": {\n    \"pack_format\": 6,\n    \"description\": \"mimiDataPack by Michałas\"\n  }\n}");
			}
 		}
		
		wczytaj();
	}
	
	void wczytaj() {
		for (File strona : new File(sc).listFiles())
			if (strona.isDirectory()) {
				Drzewko drzewko = Drzewko.wczytaj(strona.getName());
				
				for (File plik : strona.listFiles())
					Osiągnięcie.wczytaj(drzewko.nazwa + "/" + plik.getName());
			}
	}
}
