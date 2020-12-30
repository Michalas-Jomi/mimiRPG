package me.jomi.mimiRPG.util;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

public class ItemCreator {
	public static class Creator {
		ItemStack item;
		ItemMeta meta;
		
		public Creator typ(Material mat) {
			item.setType(mat);
			return this;
		}
		public Creator nazwa(String nazwa) {
			meta.setDisplayName(Func.koloruj(nazwa));
			return this;
		}
		public Creator lore(String... lore) {
			meta.setLore(Func.koloruj(Lists.newArrayList(lore)));
			return this;
		}
		public Creator customModelData(int model) {
			meta.setCustomModelData(model);
			return this;
		}
		public Creator enchant(Enchantment ench, int lvl) {
			meta.addEnchant(ench, lvl, true);
			return this;
		}
		public Creator flaga(ItemFlag... flagi) {
			for (ItemFlag flaga : flagi)
				meta.addItemFlags(flaga);
			return this;
		}
		public Creator unbreakable(boolean unbreakable) {
			meta.setUnbreakable(unbreakable);
			return this;
		}
		public Creator unbreakable() {
			meta.setUnbreakable(true);
			return this;
		}
		public Creator localizedName(String nazwa) {
			meta.setLocalizedName(nazwa);
			return this;
		}
		public Creator modifire(Attribute attr, AttributeModifier attrmodifier) {
			meta.addAttributeModifier(attr, attrmodifier);
			return this;
		}
		public Creator ilość(int ilość) {
			item.setAmount(ilość);
			return this;
		}
		public ItemStack stwórz() {
			item.setItemMeta(meta);
			return item;
		}
		
	}
	public static Creator nowy(Material mat) {
		Creator c = new Creator();
		c.item = new ItemStack(mat);
		c.meta = c.item.getItemMeta();
		return c;
	}
	public static Creator nowy(ItemStack item) {
		Creator c = new Creator();
		c.item = item;
		c.meta = c.item.getItemMeta();
		return c;
	}
}
