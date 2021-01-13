package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

/**
 * 
 * Rozszerzenie do Modułu CustomoweCraftingi
 * 
 * @author Michalas-Jomi
 * 
 */
@Moduł
public class CustomoweCraftingiUlepszanie implements Listener {
	static void reset() {
		mapa.clear();
	}

	// rezult recepty: (receptra, mat przed) 
	static final HashMap<ItemStack, Krotka<ShapedRecipe, Material>> mapa = new HashMap<>();

	public static Recipe wczytaj(Config config, NamespacedKey nms, String klucz) {
		ShapedRecipe r = CustomoweCraftingi.wczytajShaped(nms, klucz);
		mapa.put(r.getResult(), new Krotka<>(r, Objects.requireNonNull(Material.valueOf(config.wczytaj(klucz + ".U").toString().toUpperCase()))));
		return r;
	}
	
	@EventHandler
	public void prepare(PrepareItemCraftEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getResult(), rezult ->
				Func.wykonajDlaNieNull(mapa.get(rezult), krotka -> {
					if (ev.getRecipe() instanceof ShapedRecipe && ((ShapedRecipe) ev.getRecipe()).getKey().equals(krotka.a.getKey())) {
						for (ItemStack item : ev.getInventory().getMatrix())
							if (item != null && item.getType() == krotka.b) {
								ev.getInventory().setResult(Func.typ(item.clone(), rezult.getType()));
								return;
							}
						Main.error("Coś poszło nie tak w \"CustomoweCraftingiUlepszanie\" dla " + ((ShapedRecipe) ev.getRecipe()).getKey());
					}
		}));
	}
}

