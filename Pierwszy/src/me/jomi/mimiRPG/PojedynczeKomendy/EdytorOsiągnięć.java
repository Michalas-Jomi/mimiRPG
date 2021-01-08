package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R2.advancement.CraftAdvancement;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

import net.minecraft.server.v1_16_R2.Advancement;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.LepszaMapa;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class EdytorOsiągnięć implements Przeładowalny, Listener {
	static class DataPack {
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
			
			Advancement adv;
			
			Ramka ramka;
			boolean hidden;
			boolean show_toast;
			boolean announce_to_chat;
			
			@SuppressWarnings("unchecked")
			private Osiągnięcie(Drzewko drzewko, File plik, Advancement adv) {
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
				
				@SuppressWarnings("deprecation")
				Advancement adv = ((CraftAdvancement) Bukkit.getAdvancement(new NamespacedKey(Main.plugin.getName().toLowerCase(), scieżka))).getHandle();
				
				Drzewko drzewko = Drzewko.wczytaj(części.get(0));
				return Func.domyślna(drzewko.mapa.get(części.get(1)), () -> new Osiągnięcie(drzewko, new File(sc + fscieżka), adv));
			}
			
			@SuppressWarnings("unchecked")
			public void zapisz() {
				//JSONObject jsonDisplay = new JSONObject();
				
				//jsonDisplay.put("frame", ramka.name().toLowerCase());
				//jsonDisplay.put("hidden", hidden);
				//jsonDisplay.put("show_toast", show_toast);
				//jsonDisplay.put("announce_to_chat", announce_to_chat);
				
				//jsonDisplay.put("title", nazwa.wJson());
				//jsonDisplay.put("description", opis.wJson());
				//jsonDisplay.put("icon", new Gson().toJsonTree(ikona));

				
				JSONObject json = new JSONObject();
				//json.put("display", jsonDisplay);
				json.put("display", adv.c().k());
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
	}
	
	static class Osiągnięcie extends Mapowany {
		@Mapowane String scieżka;
		@Mapowane List<ItemStack> nagroda;
		@Mapowane List<Kryterium> kryteria;
	}
	abstract static class Kryterium extends Mapowany {
		@Mapowane int ile;
		
	}
	static class KryteriumWykop extends Mapowany {
		@Mapowane Material blok;
		@Mapowane ItemSelektor item;
	}
	static class ItemSelektor extends Mapowany {
		@Mapowane ItemStack kopia; // null jeśli item nie ma być sprawdzany przez ItemStack.isSimillar
		
		static class Lista extends Mapowany {
			// null gdziekolwiek oznacza pomijanie tego kryterium
			@Mapowane List<Material> typ;
			@Mapowane List<String> enchanty; // ench - "<nazwa>-<lvl>" np. "fire_aspect-2" "fire_aspect-..2" "fire_aspect-1..2" "fire_aspect-1.."
			@Mapowane List<String> nazwa; // wyrażenia regularne dla nazwy
			@Mapowane Boolean unbreakable;
			@Mapowane String durability; // tak samo jak <lvl> w ench/ 0 oznacza maxa, 2 oznacza ubyte 2 durability itd
			
			public boolean spełnia(ItemStack item) {
				try {
					if (typ != null && !spełniaTyp(item.getType()))
						return false;
					if (enchanty != null) // TODO enchanty lepiej przemyśleć
						for (Map.Entry<Enchantment, Integer> en : item.getItemMeta().getEnchants().entrySet())
							if (!spełniaEnchanty(en.getKey(), en.getValue()))
								return false;
					if (nazwa != null && !spełniaNazwe(item.getItemMeta().getDisplayName()))
						return false;
					if (unbreakable != null && !spełniaUnbreakable(item.getItemMeta().isUnbreakable()))
						return false;
					if (durability != null && !spełniaDurability(((Damageable) item.getItemMeta()).getDamage()))
						return false;
				} catch(Throwable e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
			public boolean spełniaTyp(Material typ) {
				for (Material mat : this.typ)
					if (mat == typ)
						return true;
				return false;
			}
			public boolean spełniaEnchanty(Enchantment ench, int lvl) {
				for (String kod : enchanty) {
					List<String> części = Func.tnij(kod, "-");
					if (!ench.equals(Enchantment.getByKey(NamespacedKey.minecraft(części.get(0)))))
						continue;
					if (części.size() > 1 && Func.xWZakresie(części.get(1), lvl))
						return true;
				}
				return false;
			}
			public boolean spełniaNazwe(String nazwa) {
				for (String możliwa : this.nazwa)
					if (Pattern.compile(możliwa).matcher(nazwa).matches())
						return true;
				return false;
			}
			public boolean spełniaUnbreakable(boolean unbreakable) {
				return this.unbreakable == unbreakable;
			}
			public boolean spełniaDurability(int durability) {
				return Func.xWZakresie(this.durability, durability);
			}
			
		}
		
		@Mapowane Lista czarnaLista; // null jeśli nieakceptowalne jest wszystko spoza akceptowalnych i wymaganych
		@Mapowane Lista wymagane; // null jeśli kopia != null
		@Mapowane Lista akceptowalne; // null jeśli akceptowane jest wszystko spozaczarnej listy i akceptowanych
	
		
		
	}
	static class KryteriumZabij extends Mapowany {
		
	}
	
	static String sc;
	
	/*@EventHandler
	public void osiągnięcia(PlayerAdvancementDoneEvent ev) {
		Advancement adv = ((CraftAdvancement) ev.getAdvancement()).getHandle();
		
		IChatMutableComponent d1 = ChatSerializer.a("{\"text\":\"D1\"}");
		IChatMutableComponent d2 = ChatSerializer.a("{\"text\":\"D2\"}");
		AdvancementDisplay display = new AdvancementDisplay(
				CraftItemStack.asNMSCopy(new ItemStack(Material.STONE_SWORD)),
				d1,
				d2,
				new MinecraftKey("testowanie", "display"),
				AdvancementFrameType.TASK,
				false,
				false,
				false);
		AdvancementRewards reward = new AdvancementRewards(20, new MinecraftKey[0], new MinecraftKey[0], null);
		
		CraftServer server = (CraftServer) Bukkit.getServer();
		
		
		CraftWorld świat = (CraftWorld) ev.getPlayer().getWorld();
		
		server.getHandle();
		
		//CraftMagicNumbers a;
		//CraftMagicNumbers.INSTANCE.loadAdvancement(new NamespacedKey(Main.plugin, ""), "");
		
		
		Advancement adv2 = new Advancement(new MinecraftKey("testowanie", "adv1"), null, display, reward, new HashMap<>(), new String[0][0]);

		//Main.log(adv, "\n", adv2);
	}*/

	@Override
	public void przeładuj() {
		try {
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
					writer.write("{\n  \"pack\": {\n    \"pack_format\": 6,\n    \"description\": \"mimiDataPack dla " + Main.plugin.getName() + "\"\n  }\n}");
					writer.flush();
				}
	 		}

			for (File strona : new File(sc).listFiles())
				if (strona.isDirectory()) {
					DataPack.Drzewko drzewko = DataPack.Drzewko.wczytaj(strona.getName());
					
					for (File plik : strona.listFiles())
						DataPack.Osiągnięcie.wczytaj(drzewko.nazwa + "/" + plik.getName());
				}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		
		Main.reloadBukkitData();
	}
	@Override
	public Krotka<String, Object> raport() {
		int licz = 0;
		for (DataPack.Drzewko drzewko : DataPack.Drzewko.mapaDrzew.values())
			licz += drzewko.mapa.size();
		return Func.r("Wczytane osiągnięcia", licz + "/" + DataPack.Drzewko.mapaDrzew.size());
	}
}
