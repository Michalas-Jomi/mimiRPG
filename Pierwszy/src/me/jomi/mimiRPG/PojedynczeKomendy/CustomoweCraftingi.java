package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;

public class CustomoweCraftingi implements Prze³adowalny {
	public static final Config config = new Config("Customowe Craftingi");
	
	public static void dodaj(String nazwa, ItemStack item, String[] linie, HashMap<Character, Material> mapa) {
		NamespacedKey nms = new NamespacedKey(Main.plugin, nazwa);
		ShapedRecipe rec = new ShapedRecipe(nms, item);
		rec.shape(linie);
		for (char klucz : mapa.keySet())
			rec.setIngredient(klucz, mapa.get(klucz));
		
		Main.plugin.getServer().removeRecipe(nms);
		Main.plugin.getServer().addRecipe(rec);
	}
	private static void wczytaj(String klucz) {
		ItemStack item = config.wczytajItem(klucz + ".item");
		List<String> lista = config.wczytajListe(klucz + ".uk³ad");
		String linia = Func.listToString(lista, 0, "");
		Set<Character> set = Sets.newConcurrentHashSet();
		for (int i=0; i<linia.length(); i++) {
			char z = linia.charAt(i);
			set.add(z);
		}
		HashMap<Character, Material> mapa = new HashMap<>();
		for (char znak : set) {
			mapa.put(znak, Material.valueOf(config.wczytajStr(klucz + "." + znak).toUpperCase()));
		}
			
		String[] arr = new String[lista.size()];
		for (int i=0; i<arr.length; i++)
			arr[i] = lista.get(i);
		dodaj(klucz, item, arr, mapa);
	}
	
	public void prze³aduj() {
		config.prze³aduj();
		
		// Usuwanie niechcianych craftingów
		for (Object przepis :  Main.ust.wczytajLubDomyœlna("ZablokowaneCraftingi", Lists.newArrayList()))
			Main.plugin.getServer().removeRecipe(NamespacedKey.minecraft((String) przepis));
		
		for (String klucz : config.klucze(false))
			wczytaj(klucz);
	}
	public String raport() {
		return "§6Customowe Craftingi: §e" + config.klucze(false).size();
	}
	
	
}
