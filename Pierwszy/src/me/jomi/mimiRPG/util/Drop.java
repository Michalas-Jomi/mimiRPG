package me.jomi.mimiRPG.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;

public class Drop implements ConfigurationSerializable, Cloneable {
	public List<Drop> drop;
	public boolean tylkoJeden = false;
	
	public ItemStack item;
	
	public int rolle = 1;
	public double szansa = 1;
	
	public int min_ilość = -1;
	public int max_ilość = -1;
	
	public int rollePerPoziom = 0;
	public double szansaPerPoziom = 0;
	
	// item
	// item szansa(%)
	// item szansa(%) ilość
	// item szansa(%) ilość (modifiers...)
	// item szansa(%) min_ilość-max_ilość
	// item szansa(%) min_ilość-max_ilość (modifiers...)
	// item szansa(%) min_ilość-max_ilość
	// mat
	// mat szansa(%)
	// ...
	// drop
	// drop szansa(%)
	// ...
	//
	// modifiers: xrolle +szansa(%) +xrolle
	
	public static Drop wczytaj(String nazwa) {
		String[] części = nazwa.split(" ");

		Drop _drop = Baza.dropy.get(części[0]);
		Drop drop = _drop == null ? new Drop(Config.item(części[0])) : _drop.clone();
		
		boolean syntax[] = new boolean[]{false, false, false};
		
		Consumer<String> modifier = str -> {
			boolean plus = str.startsWith("+");
			if (plus)
				str = str.substring(1);
			
			boolean roll = str.startsWith("x");
			if (roll)
				str = str.substring(1);
			
			Object obj;
			try {
				obj = roll ? Func.Int(str) : Func.Double(str);
			} catch(NumberFormatException e) {
				Main.warn(String.format("Niepoprawna liczba w Dropie: %s \"%s\"", str, nazwa));
				return;
			}
			
			
			
			int i;
			if (roll && !plus) {
				drop.rolle = (int) obj;
				i = 0;
			} else if (roll && plus) {
				drop.rollePerPoziom = (int) obj;
				i = 1;
			} else if (!roll && plus) {
				drop.szansaPerPoziom = (double) obj;
				i = 2;
			} else {
				Main.warn("Nieprawidłowy modifier w Dropie: \"" + nazwa + "\" wymagany jeden z prefixów\n\"x\" - rolle; \"+x\" - rollePerPoziom; \"+\" - szansaPerPoziom");
				return;
			}
			
			if (syntax[i])
				Main.warn("Powielony modifier w Dropie \"" + new String[]{"x", "+x", "+"}[i] + "\", używanie wcześniejszego\"" + nazwa + "\"");
			syntax[i] = true;			
		};
		
		switch (części.length) {
		default:
			Main.warn("Nadprogramowa ilość modifierów dropu " + nazwa);
		case 6:
		case 5:
		case 4:
			for (int i=części.length-1; i > 2; i--)
				modifier.accept(części[i]);
		case 3:
			String[] minmax = części[2].split("-");
			drop.min_ilość = Func.Int(minmax[0], -1);
			drop.max_ilość = minmax.length > 1 ? Func.Int(minmax[1], -1) : drop.min_ilość;
		case 2:
			if ((drop.szansa = Func.Double(części[1], -1)) < 0) {
				Main.warn(String.format("Niepoprawna szansa dropu %s \"%s\", przyjmowanie szansa = 1.0 (100%)", nazwa.split(" ")[1], nazwa));
				drop.szansa = 1;
			}
		}
		return drop;
	}
	public Drop(ItemStack item) {
		this.item = item;
	}
	@SuppressWarnings("unchecked")
	public Drop(Map<String, Object> mapa) {
		tylkoJeden = (boolean) mapa.getOrDefault("tylko jeden", false);
		
		max_ilość = (int) mapa.getOrDefault("max ilość", -1);
		min_ilość = (int) mapa.getOrDefault("min ilość", -1);
		
		szansa = Func.DoubleObj(mapa.getOrDefault("szansa", 1));
		rolle = (int) mapa.getOrDefault("rolle", 1);
		
		szansaPerPoziom = Func.DoubleObj(mapa.getOrDefault("szansaPerPoziom", 1));
		rollePerPoziom = (int) mapa.getOrDefault("rollePerPoziom", 1);
		
		if ((item = Config.item(mapa.get("item"))) == null)
			Func.wykonajDlaNieNull((List<Object>) mapa.get("drop"), dropy -> {
				drop = Lists.newArrayList();
				dropy.forEach(drop -> this.drop.add(Config.drop(drop)));
			});
	}
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> mapa = new HashMap<>();
		if (rolle != 1)			mapa.put("rolle", rolle);
		if (szansa < 1)			mapa.put("szansa", szansa);
		if (max_ilość != -1) 	mapa.put("max ilość", max_ilość);
		if (min_ilość != -1) 	mapa.put("min ilość", min_ilość);
		if (tylkoJeden) 		mapa.put("tylko jeden", tylkoJeden);
		if (rollePerPoziom != 0)mapa.put("rollePerPoziom",  rollePerPoziom);
		if (szansaPerPoziom!= 0)mapa.put("szansaPerPoziom", szansaPerPoziom);
		if (item != null) 		mapa.put("item", Config._item(item));
		else 					mapa.put("drop", drop);
		return mapa;
	}
	
	
	public static int poziom(LivingEntity e, ItemStack item) {
		int w = 0;
		
		if (e != null && e.hasPotionEffect(PotionEffectType.LUCK))
			w += e.getPotionEffect(PotionEffectType.LUCK).getAmplifier() + 1;
		if (e != null && e.hasPotionEffect(PotionEffectType.UNLUCK))
			w -= e.getPotionEffect(PotionEffectType.UNLUCK).getAmplifier() + 1;
		
		if (item != null) {
			w += item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
			w += item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		}
		
		return w;
	}
	
	public boolean dropnij(Location loc) 			 { return dropnij(loc, 0); }
	public boolean dropnij(Location loc, int poziom) { return dropnij(poziom, item -> loc.getWorld().dropItem(loc, item)); }
	
	public boolean dropnij(Inventory inv)			  {	return dropnij(inv, 0); }
	public boolean dropnij(Inventory inv, int poziom) {	return dropnij(poziom, inv::addItem); }
	
	public boolean dropnij(Player p) 				 { return dropnij(p, poziom(p, null)); }
	public boolean dropnij(Player p, ItemStack item) { return dropnij(p, poziom(p, item)); }
	public boolean dropnij(Player p, int poziom) 	 { return dropnij(poziom, item -> Func.dajItem(p, item)); }
	
	public List<ItemStack> dropnij() { return dropnij(0); }
	public List<ItemStack> dropnij(int poziom) {
		List<ItemStack> itemy = Lists.newArrayList();
		for (int i=0; i<(rolle + poziom * rollePerPoziom); i++)
			if (Func.losuj(szansa + poziom * szansaPerPoziom))
				if (item != null)
					itemy.add(item.clone());
				else
					for (Drop drop : this.drop)
						if (!drop.dropnij(poziom).isEmpty() && tylkoJeden)
							break;
		return itemy;
	}
	
	private boolean dropnij(int poziom, Consumer<ItemStack> cons) {
		List<ItemStack> itemy = dropnij(poziom);
		itemy.forEach(cons);
		return !itemy.isEmpty();
	}
	
	@Override
	public Drop clone() {
		Drop drop = new Drop(item);
		drop.drop = this.drop;
		drop.max_ilość = max_ilość;
		drop.min_ilość = min_ilość;
		drop.szansa = szansa;
		drop.rolle = rolle;
		drop.tylkoJeden = tylkoJeden;
		return drop;
	}
	
	/*
	 * nazwa:
	 * 	 ==: Drop
	 *   drop:
	 *   - szansa: .1
	 *     drop: stone
	 *   - drop:
	 *     - cobblestone
	 *     - iron_ingot
	 *   - drop3
	 *   tylkoJeden: true
	 * 
	 * 
	 * 
	 * 
	 */
}
