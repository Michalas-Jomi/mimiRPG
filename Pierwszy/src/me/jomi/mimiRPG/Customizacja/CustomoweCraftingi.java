package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
@Przeładowalny.WymagaReloadBukkitData
public class CustomoweCraftingi implements Przeładowalny {
	public static final Config config = new Config("Customowe Craftingi");
	
	public static ShapedRecipe shaped(NamespacedKey nms, ItemStack item, String[] linie, HashMap<Character, String> mapa, String grupa) {
		ShapedRecipe rec = new ShapedRecipe(nms, item);
		
		rec.shape(linie);
		
		for (char klucz : mapa.keySet()) {
			String str = mapa.get(klucz);
			Func.wykonajDlaNieNull(mapaTagów.get(str),
					choice -> rec.setIngredient(klucz, choice),
					() -> rec.setIngredient(klucz, Func.StringToEnum(Material.class, str)));
		}
		
		Func.wykonajDlaNieNull(grupa, grp -> rec.setGroup(grp));
		
		return rec;
	}
	static ShapedRecipe wczytajShaped(NamespacedKey nms, String klucz) {
		ItemStack item		= config.wczytajItem(klucz + ".item");
		List<String> lista	= config.wczytajListe(klucz + ".układ");

		String linia		= Func.listToString(lista, 0, "");
		Set<Character> set	= Sets.newConcurrentHashSet();
		for (int i=0; i < linia.length(); i++)
			set.add(linia.charAt(i));
		
		HashMap<Character, String> mapa = new HashMap<>();
		set.forEach(znak -> mapa.put(znak, config.wczytajStr(klucz + "." + znak)));
		
		String[] arr = new String[lista.size()];
		for (int i=0; i<arr.length; i++)
			arr[i] = lista.get(i);
		
		return shaped(nms, item, arr, mapa, config.wczytajStr(klucz + ".grupa"));
	}
	private static void wczytaj(String klucz) {
		String typ = config.wczytajLubDomyślna(klucz + ".typ", "shaped");
		
		NamespacedKey nms = new NamespacedKey(Main.plugin, klucz);
		
		Recipe rec;
		switch (typ.toLowerCase().replace(" ", "")) {
		case "shaped":
		case "crafting":
		case "craftingtable":
			rec = wczytajShaped(nms, klucz);
			break;
		case "craftingulepszenia":
		case "craftingulepszenie":
		case "cu": // TODO uzupełnić szablon
			rec = CustomoweCraftingiUlepszanie.wczytaj(config, nms, klucz);
			break;
		default:
			return;
		}
		
		zarejestruj(nms, rec);
	}
	
	public static void zarejestruj(NamespacedKey nms, Recipe rec) {
		Main.plugin.getServer().removeRecipe(nms);
		Main.plugin.getServer().addRecipe(rec);
	}
	
	static final HashMap<String, MaterialChoice> mapaTagów = new HashMap<>();
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			// TODO przetestować zapominanie recept, które są usuwane
			for (Object przepis :  Main.ust.wczytajLubDomyślna("ZablokowaneCraftingi", Lists.newArrayList()))
				Main.plugin.getServer().removeRecipe(NamespacedKey.minecraft((String) przepis));
			
			mapaTagów.clear();
			CustomoweCraftingiUlepszanie.reset();
			
			Func.wykonajDlaNieNull(config.sekcja("Tagi"), sekcja -> {
				for (String klucz : sekcja.getKeys(false))
					try {
						List<Material> lista = Func.wykonajWszystkim(sekcja.getStringList(klucz), str -> Func.StringToEnum(Material.class, str));
						mapaTagów.put(klucz, new MaterialChoice(lista));
					} catch (Throwable e) {
						Main.warn("Niepoprawny tag CutomowychCraftingów " + klucz);
					}
				
			});
			
			for (String klucz : config.klucze(false))
				try {
					if (klucz.equalsIgnoreCase("Tagi"))
						continue;
					wczytaj(klucz);	
				} catch (Throwable e) {
					Main.warn("Problem przy Customowym Craftingu \"" + klucz + "\"");
					e.printStackTrace();
				}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Customowe Craftingi", config.klucze(false).size());
	}
}
