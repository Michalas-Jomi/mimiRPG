package me.jomi.mimiRPG.Frakcje;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class BlokadaEnchantów implements Przeładowalny, Listener {
	
	
	final List<Enchantment> zablokowane = new ArrayList<>();
	
	@EventHandler
	public void nakładanieWKowadle(PrepareAnvilEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getItem(1), dodatek -> {
			if (dodatek.getType() == Material.ENCHANTED_BOOK) {
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) dodatek.getItemMeta();
				zablokowane.forEach(ench -> {
					if (meta.hasStoredEnchant(ench)) {
						ev.getInventory().setItem(2, null);
						ev.setResult(null);
					}
				});
			}
		});
	}
	
	@Override
	public void przeładuj() {
		zablokowane.clear();
		
		Main.ust.wczytajListe("BlokadaEnchantów").forEach(nazwa -> {
			try {
				zablokowane.add(Enchantment.getByKey(NamespacedKey.minecraft(nazwa.toLowerCase())));
			} catch (IllegalArgumentException e) {
				Main.warn("Niepoprawny zablokowany enchant: " + nazwa);
			}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Zablokowane Enchanty", zablokowane.size());
	}
	
}
