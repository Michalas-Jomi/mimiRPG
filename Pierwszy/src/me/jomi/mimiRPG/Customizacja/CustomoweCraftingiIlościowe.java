package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class CustomoweCraftingiIlościowe implements Listener {
    static Map<NamespacedKey, int[]> mapa = new HashMap<>();
    
    public static void reset() {
    	mapa.clear();
    }
    
    public static Recipe wczytaj(Config config, NamespacedKey nms, String klucz) {
		ShapedRecipe r = CustomoweCraftingi.wczytajShaped(nms, klucz);
		
		int[] doMapy = new int[9];
		int[] index = new int[]{0};
		
		config.wczytajListe(klucz + ".ilości").forEach(linia -> {
			List<String> liczby = Func.tnij(linia, " ");
			
			for (int i=0; i < 3; i++) {
				int x = index[0]++;
				try {
					doMapy[x] = Func.Int(liczby.remove(0));
				} catch (IndexOutOfBoundsException e) {
					doMapy[x] = 0;
				}
			}
		});
		
		mapa.put(nms, doMapy);
		
		return r;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent ev) {
        if (ev.getRecipe() instanceof ShapedRecipe)
			Func.wykonajDlaNieNull(mapa.get(((ShapedRecipe) ev.getRecipe()).getKey()), potrzebne -> sprawdz(ev.getInventory(), potrzebne));
    }
    // czy mozna wycraftować
    private boolean sprawdz(CraftingInventory inv, int[] potrzebne) {
		int i = 0;
		for (ItemStack item : inv.getMatrix()) {
			int ilość = potrzebne[i++];
			if (item == null) {
				if (ilość > 0) {
					inv.setResult(null);
					return false;
				} else
					continue;
			} else if (item.getAmount() < ilość) {
				inv.setResult(null);
				return false;
			}
		}
		return true;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(CraftItemEvent ev) {
        if (ev.getRecipe() instanceof  ShapedRecipe) {
        	Func.wykonajDlaNieNull(mapa.get(((ShapedRecipe) ev.getRecipe()).getKey()), potrzebne -> {
        		
        		ItemStack[] matrix = ev.getInventory().getMatrix();
        		
        		if (!sprawdz(ev.getInventory(), potrzebne))
        			return;
                
                ev.setCancelled(true);
                if (ev.getWhoClicked().getItemOnCursor().getType() != Material.AIR)
                    return;
                

        		int i = 0;
        		for (ItemStack item : matrix) {
        			int ilość = potrzebne[i++];
        			if (item != null)
						item.setAmount(item.getAmount() - ilość);
        		}
                
                ev.getWhoClicked().setItemOnCursor(ev.getRecipe().getResult());
                ev.getInventory().setResult(sprawdz(ev.getInventory(), potrzebne) ? ev.getRecipe().getResult() : null);
        	});
        }
    }
}
