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
	
	public Drop() {}
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
		
		szansaPerPoziom = Func.DoubleObj(mapa.getOrDefault("szansaPerPoziom", 0));
		rollePerPoziom = (int) mapa.getOrDefault("rollePerPoziom", 0);
		
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
		if (item != null) 		mapa.put("item", Config.zserializujItem(item));
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
			ItemStack _item = new ItemStack(item.getType());
			for (Enchantment ench : new Enchantment[] {Enchantment.LOOT_BONUS_BLOCKS, Enchantment.LOOT_BONUS_MOBS, Enchantment.LUCK})
				if (ench.canEnchantItem(_item))
					w += item.getEnchantmentLevel(ench);
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
		
		if (item == null) {
			int licz = 500;
			int ile = Func.losuj(min_ilość, max_ilość);
			while (licz-- > 0 && (itemy.size() < (min_ilość <= -1 ? 1 : ile)))
				itemy.addAll(subDropnij(poziom));
			while (itemy.size() > (max_ilość <= -1 ? 1 : ile))
				itemy.remove(Func.losujWZasięgu(itemy.size()));
		} else
			itemy.addAll(subDropnij(poziom));
		
		return itemy;
	}
	private List<ItemStack> subDropnij(int poziom) {
		List<ItemStack> itemy = Lists.newArrayList();
		for (int i=0; i < (rolle + poziom * rollePerPoziom); i++)
			if (Func.losuj(szansa + poziom * szansaPerPoziom))
				if (item != null)
					itemy.add(Func.ilość(item.clone(), Func.losuj(min_ilość == -1 ? item.getAmount() : min_ilość, max_ilość == -1 ? item.getAmount() : max_ilość)));
				else if (this.drop != null)
					for (Drop drop : this.drop) {
						List<ItemStack> subItemy = drop.dropnij(poziom);
						itemy.addAll(subItemy);
						if (!subItemy.isEmpty() && tylkoJeden)
							break;
					}
				else 
					Main.warn("Niepoprawny drop " + this);
		return itemy;
	}

	private boolean dropnij(int poziom, Consumer<ItemStack> cons) {
		List<ItemStack> itemy = dropnij(poziom);
		itemy.forEach(cons);
		return !itemy.isEmpty();
	}
	
	public boolean dropnijNaRandSloty(Inventory inv) { return dropnijNaRandSloty(inv, 0); }
	public boolean dropnijNaRandSloty(Inventory inv, int poziom) {
		List<ItemStack> itemy;
		if (inv.firstEmpty() == -1 || (itemy = dropnij(poziom)).isEmpty())
			return false;
		
		while (!itemy.isEmpty() && inv.firstEmpty() != -1) {
			
			int slot;
			do
				slot = Func.losujWZasięgu(inv.getSize());
			while (inv.getItem(slot) != null);
			
			inv.setItem(slot, itemy.remove(Func.losujWZasięgu(itemy.size())));
		}
		return true;
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
		drop.szansaPerPoziom = szansaPerPoziom;
		drop.rollePerPoziom = rollePerPoziom;
		return drop;
	}
	//// item szansa(%) min_ilość-max_ilość (modifiers...)
	// modifiers: xrolle +szansa(%) +xrolle
	@Override
	public String toString() {
		if (item != null)
			return String.format("%s %s %s-%s x%s +%s +x%s", item, (szansa * 100) + "%", min_ilość, max_ilość, rolle, (szansaPerPoziom * 100) + "%", rollePerPoziom);
		else if (drop != null) {
			StringBuilder strB = new StringBuilder("Drop(tylkoJeden=").append(tylkoJeden).append(", [\n");
			for (Drop drop : drop)
				strB.append(drop).append(",\n");
			return strB.append("])").toString();
		} else
			return String.format("PustyDrop %s %s-%s x%s +%s +x%s", (szansa * 100) + "%", min_ilość, max_ilość, rolle, (szansaPerPoziom * 100) + "%", rollePerPoziom);
	}
	
	
	// TODO kod wygenerowany przez eclipse, przeanalizować
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((drop == null) ? 0 : drop.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + max_ilość;
		result = prime * result + min_ilość;
		result = prime * result + rolle;
		result = prime * result + rollePerPoziom;
		long temp;
		temp = Double.doubleToLongBits(szansa);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(szansaPerPoziom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (tylkoJeden ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Drop other = (Drop) obj;
		if (drop == null) {
			if (other.drop != null)
				return false;
		} else if (!drop.equals(other.drop))
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (max_ilość != other.max_ilość)
			return false;
		if (min_ilość != other.min_ilość)
			return false;
		if (rolle != other.rolle)
			return false;
		if (rollePerPoziom != other.rollePerPoziom)
			return false;
		if (Double.doubleToLongBits(szansa) != Double.doubleToLongBits(other.szansa))
			return false;
		if (Double.doubleToLongBits(szansaPerPoziom) != Double.doubleToLongBits(other.szansaPerPoziom))
			return false;
		if (tylkoJeden != other.tylkoJeden)
			return false;
		return true;
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
