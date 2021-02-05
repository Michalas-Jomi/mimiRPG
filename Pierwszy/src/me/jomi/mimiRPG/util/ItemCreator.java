package me.jomi.mimiRPG.util;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

public class ItemCreator {
	public static class Creator<T extends ItemMeta> {
		ItemStack item;
		T meta;
		
		@SuppressWarnings("unchecked")
		public Creator(ItemStack item, Class<T> clazz) {
			this.item = item;
			this.meta = (T) item.getItemMeta();
		}
		
		public Creator<T> typ(Material mat) {
			item.setType(mat);
			return this;
		}
		public Creator<T> nazwa(String nazwa) {
			meta.setDisplayName(Func.koloruj(nazwa));
			return this;
		}
		public Creator<T> lore(String... lore) {
			meta.setLore(Func.koloruj(Lists.newArrayList(lore)));
			return this;
		}
		public Creator<T> customModelData(int model) {
			meta.setCustomModelData(model);
			return this;
		}
		public Creator<T> enchant(Enchantment ench, int lvl) {
			meta.addEnchant(ench, lvl, true);
			return this;
		}
		public Creator<T> flaga(ItemFlag... flagi) {
			for (ItemFlag flaga : flagi)
				meta.addItemFlags(flaga);
			return this;
		}
		public Creator<T> unbreakable(boolean unbreakable) {
			meta.setUnbreakable(unbreakable);
			return this;
		}
		public Creator<T> unbreakable() {
			meta.setUnbreakable(true);
			return this;
		}
		public Creator<T> localizedName(String nazwa) {
			meta.setLocalizedName(nazwa);
			return this;
		}
		public Creator<T> modifire(Attribute attr, AttributeModifier attrmodifier) {
			meta.addAttributeModifier(attr, attrmodifier);
			return this;
		}
		public Creator<T> ilość(int ilość) {
			item.setAmount(ilość);
			return this;
		}
		public Creator<T> custom(Consumer<T> ustaw) {
			ustaw.accept(meta);
			return this;
		}
		public ItemStack stwórz() {
			item.setItemMeta(meta);
			return item;
		}
		
	}
	public static Creator<?> nowy(Material mat) {
		return nowy(new ItemStack(mat));
	}
	public static Creator<?> nowy(ItemStack item) {
		return nowy(item, item.getItemMeta());
	}
	@SuppressWarnings("unchecked")
	public static <T extends ItemMeta> Creator<T> nowy(ItemStack item, T meta) {
		return new Creator<>(item, (Class<T>) meta.getClass());
	}
}
