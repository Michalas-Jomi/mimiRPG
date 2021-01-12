package me.jomi.mimiRPG.util;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;

public class SelektorItemów extends Mapowany {
	public static class Lista extends Mapowany {
		// null gdziekolwiek oznacza pomijanie tego kryterium
		@Mapowane List<Material> typ;
		@Mapowane List<String> nazwa; // wyrażenia regularne dla nazwy
		@Mapowane List<String> durability; // tak samo jak <lvl> w ench/ 0 oznacza maxa, 2 oznacza ubyte 2 durability itd
		@Mapowane List<String> customModelData;
		@Mapowane Boolean unbreakable;
		
		final List<Krotka<Enchantment, Predicate<Integer>>> enchanty = Lists.newArrayList();
		
		@Mapowane List<String> _enchanty; // ench - "<nazwa>-<lvl>" np. "fire_aspect-2" "fire_aspect-..2" "fire_aspect-1..2" "fire_aspect-1.."
		void Init() {
			_enchanty.forEach(str -> {
				List<String> ench_lvl = Func.tnij(str, "-");
				enchanty.add(new Krotka<>(
						Enchantment.getByKey(NamespacedKey.minecraft(ench_lvl.get(0))),
						lvl -> ench_lvl.size() < 2 || Func.xWZakresie(ench_lvl.get(1), lvl)
						));
			});
		}
		
		
		public boolean spełniaTyp(Material typ) {
			if (typ == null)
				return true;
			for (Material mat : this.typ)
				if (mat == typ)
					return true;
			return false;
		}
		public boolean spełniaNazwe(String nazwa) {
			if (this.nazwa == null)
				return true;
			for (String możliwa : this.nazwa)
				if (Pattern.compile(możliwa).matcher(nazwa).matches())
					return true;
			return false;
		}
		public boolean spełniaUnbreakable(boolean unbreakable) {
			return this.unbreakable == null || this.unbreakable == unbreakable;
		}
		public boolean spełniaDurability(int durability) {
			if (this.durability == null)
				return true;
			for (String kod : this.durability)
				if (Func.xWZakresie(kod, durability))
					return true;
			return false;
		}
		public boolean spełniaCustomModelData(Integer customModelData) {
			if (this.customModelData == null)
				return true;
			if (customModelData == null)
				return false;
			for (String kod : this.customModelData)
				if (Func.xWZakresie(kod, customModelData))
					return true;
			return false;
			
		}

		public boolean zawieraEnchant(Map.Entry<Enchantment, Integer> en) {
			return zawieraEnchant(en.getKey(), en.getValue());
		}
		public boolean zawieraEnchant(Enchantment ench, int lvl) {
			for (Krotka<Enchantment, Predicate<Integer>> krotka : enchanty)
				if (krotka.a.equals(ench))
					return true;
			return false;
		}
		public boolean spełniaWszystkieEnchanty(Map<Enchantment, Integer> mapa) {
			for (Krotka<Enchantment, Predicate<Integer>> krotka : enchanty)
				if (!mapa.containsKey(krotka.a) || !krotka.b.test(mapa.get(krotka.a)))
					return false;
			return true;
		}
	}
	
	void Init() {
		if (czarnaLista == null && akceptowalne == null)
			if (kopia == null && wymagane == null)
				return;
			else
				throw new Error("Niepoprawny Selektor Itemów, nie może być jednocześnie czarnaLista jak lista akceptowalnych równa null");
		if (kopia == null && wymagane == null)
			throw new Error("Niepoprawny Selektor Itemów, nie może być jednocześnie kopia jak i lista wymagań równa null");
	}
	
	@Mapowane ItemStack kopia; // null jeśli item nie ma być sprawdzany przez ItemStack.isSimillar
	
	@Mapowane Lista czarnaLista; // null jeśli nieakceptowalne jest wszystko spoza akceptowalnych i wymaganych
	@Mapowane Lista wymagane; // null jeśli kopia != null
	@Mapowane Lista akceptowalne; // null jeśli akceptowane jest wszystko spozaczarnej listy i akceptowanych
	
	public boolean pasuje(ItemStack item) {
		if (kopia != null)
			return kopia.isSimilar(item);
		
		ItemMeta meta = item.getItemMeta();
		

		if (!wymagane.spełniaWszystkieEnchanty(item.getItemMeta().getEnchants()))
			return false;
		
		
		boolean unbreakable = meta.isUnbreakable();
		int durability = meta instanceof Damageable ? ((Damageable) meta).getDamage() : 0;
		Integer customModelData = meta.hasCustomModelData() ? meta.getCustomModelData() : null;
		Material typ = item.getType();
		String nazwa = meta.hasDisplayName() ? meta.getDisplayName() : "";
		
		if (akceptowalne == null) {
			if (	wymagane.spełniaUnbreakable(unbreakable) &&
					wymagane.spełniaDurability(durability) &&
					wymagane.spełniaCustomModelData(customModelData) &&
					wymagane.spełniaTyp(typ) &&
					wymagane.spełniaNazwe(nazwa)
					)
				return false;
			if (	czarnaLista.spełniaUnbreakable(unbreakable) ||
					czarnaLista.spełniaDurability(durability) ||
					czarnaLista.spełniaCustomModelData(customModelData) ||
					czarnaLista.spełniaTyp(typ) ||
					czarnaLista.spełniaNazwe(nazwa)
					)
				return false;
			if (meta.hasEnchants())
				for (Map.Entry<Enchantment, Integer> en : meta.getEnchants().entrySet())
					if (czarnaLista.zawieraEnchant(en.getKey(), en.getValue()))
						return false;
		} else if (czarnaLista == null) {
			if (	test(Lista::spełniaUnbreakable, unbreakable) ||
					test(Lista::spełniaDurability, durability) ||
					test(Lista::spełniaCustomModelData, customModelData) ||
					test(Lista::spełniaTyp, typ) ||
					test(Lista::spełniaNazwe, nazwa)
					)
				return false;
			if (!meta.hasEnchants())
				if (!wymagane.enchanty.isEmpty())
					return false;
				else {}
			else
				for (Map.Entry<Enchantment, Integer> en : meta.getEnchants().entrySet())
					if (test(Lista::zawieraEnchant, en))
						return false;
		} else
			throw new Error("Nie można jednocześnie używać czarnej listy i akceptowalnej listy w pojedyńczym Selektorze Itemów!");
		return true;
	}
	private <T> boolean test(BiPredicate<Lista, T> test, T obj) {
		return !(test.test(wymagane, obj) || test.test(akceptowalne, obj));
	}
}
