package me.jomi.mimiRPG.Frakcje;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
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
	@EventHandler
	public void enchantowanie(InventoryClickEvent ev) {
		if (!(ev.getInventory() instanceof EnchantingInventory))
			return;
		
		ev.setCurrentItem(przerób(ev.getCurrentItem()));
	}
	@EventHandler
	public void enchantowanie(InventoryCloseEvent ev) {
		if (!(ev.getInventory() instanceof EnchantingInventory))
			return;
		
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			Inventory inv = ev.getPlayer().getInventory();
			for (int i=0; i < inv.getSize(); i++)
				inv.setItem(i, przerób(inv.getItem(i)));
		});
	}
	
	private ItemStack przerób(ItemStack item) {
		if (item == null) return item;
		
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;
		
		zablokowane.forEach(ench -> {
			if (meta instanceof EnchantmentStorageMeta)
				((EnchantmentStorageMeta) meta).removeStoredEnchant(ench);
			else
				meta.removeEnchant(ench);
		});
		
		item.setItemMeta(meta);
		return item;
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
