package me.jomi.mimiRPG.RPG_oddzielne;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.util.Func;

public abstract class Bonusy extends Mapowany {
	static abstract class Bonus extends Mapowany {
		public abstract void dodajDoItemu(ItemMeta meta);
	}
	public static class BonusAttr extends Bonus {
		@Mapowane Attribute atrybut;
		@Mapowane double atrybutWartość;
		@Mapowane Operation sposóbDodania;
		
		@Override
		public void dodajDoItemu(ItemMeta meta) {
			meta.addAttributeModifier(atrybut, new AttributeModifier("mimi" + atrybut + atrybutWartość + sposóbDodania, atrybutWartość, sposóbDodania));
		}
		
	}
	public static class BonusEnch extends Bonus {
		public Enchantment ench;
		@Mapowane private String enchant;
		@Mapowane int enchantLvl = 1;
		
		
		@Override
		protected void Init() {
			if (enchant != null)
				ench = Enchantment.getByKey(CraftNamespacedKey.fromString(enchant));
		}
		
		@Override
		public void dodajDoItemu(ItemMeta meta) {
			meta.addEnchant(ench, enchantLvl, false);
		}
	}
	
	@Mapowane List<BonusAttr> bonusyAttr;
	@Mapowane List<BonusEnch> bonusyEnch;
	@Mapowane List<ItemFlag> flagi;
	@Mapowane Integer customModelData;
	@Mapowane String nazwa;
	@Mapowane Boolean unbreakable;
	@Mapowane(nieTwórz = true) Material nowyTyp;
	
	public void dodajDoItemu(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		bonusyEnch.forEach(bonus -> bonus.dodajDoItemu(meta));
		bonusyAttr.forEach(bonus -> bonus.dodajDoItemu(meta));
		if (nazwa != null)
			meta.setDisplayName(Func.koloruj(nazwa));
		flagi.forEach(meta::addItemFlags);
		Func.wykonajDlaNieNull(unbreakable, meta::setUnbreakable);
		Func.wykonajDlaNieNull(customModelData, meta::setCustomModelData);
		Func.wykonajDlaNieNull(nowyTyp, item::setType);
		item.setItemMeta(meta);
	}
}
