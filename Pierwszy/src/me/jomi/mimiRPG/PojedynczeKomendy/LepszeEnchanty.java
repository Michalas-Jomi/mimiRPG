package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class LepszeEnchanty implements Listener, Przeładowalny {
	public boolean jestLepszymEnchantem(ItemStack item) {
		return  item != null && 
				item.getType().equals(Material.ENCHANTED_BOOK) &&
				(!wymaganaFlaga() || item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieEq(InventoryClickEvent ev) {
		if (!ev.getClick().equals(ClickType.RIGHT)) return;
		if (ev.getCurrentItem() == null) return;
		if (!jestLepszymEnchantem(ev.getCursor())) return;
		EnchantmentStorageMeta ksiazka = (EnchantmentStorageMeta) ev.getCursor().getItemMeta();
		ItemMeta meta = ev.getCurrentItem().getItemMeta();
		HashMap<String, Integer> mapa = new HashMap<>();
		for (Enchantment enchant : ksiazka.getStoredEnchants().keySet()) {
			mapa.put(enchant.toString(), meta.getEnchantLevel(enchant));
			meta.removeEnchant(enchant);
			if (meta.hasConflictingEnchant(enchant))
				return;
		}
		List<Enchantment> lista = Lists.newArrayList();
		for (Entry<Enchantment, Integer> en : ksiazka.getStoredEnchants().entrySet())
			if (en.getKey().canEnchantItem(ev.getCurrentItem())) {
				meta.addEnchant(en.getKey(), Math.max(mapa.remove(en.getKey().toString()), en.getValue()), true);
				lista.add(en.getKey());
			}
		ev.getCurrentItem().setItemMeta(meta);
		ev.setCancelled(true);
		
		for (Enchantment enchant : lista)
			ksiazka.removeStoredEnchant(enchant);
		
		if (ksiazka.getStoredEnchants().isEmpty())
			ev.getCursor().setType(Material.BOOK);
		else
			ev.getCursor().setItemMeta(ksiazka);
	}

	boolean wymaganaFlaga() { // TODO dodać w szablonie
		return Main.ust.wczytajLubDomyślna("LepszeEnchanty.wymagana Flaga enchantów", true);
	}
	
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wymagana flaga", wymaganaFlaga() ? "Tak" : "Nie");
	}
	@Override public void przeładuj() {}
}
