package me.jomi.mimiRPG.util;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class Drop implements ConfigurationSerializable {
	List<Drop> drop;
	boolean tylkoJeden = false;
	
	ItemStack item;
	
	double szansa = 1;
	
	int min_ilość = -1;
	int max_ilość = -1;
	

	public Drop(ItemStack item) {
		this.item = item;
	}
	@SuppressWarnings("unchecked")
	public Drop(Map<String, Object> mapa) {
		tylkoJeden = (boolean) mapa.getOrDefault("tylko Jeden", false);
		max_ilość = (int) mapa.getOrDefault("max ilość", -1);
		min_ilość = (int) mapa.getOrDefault("min ilość", -1);
		szansa = Func.Double(mapa.getOrDefault("szansa", 1));
		if ((item = Config.item(mapa.get("item"))) == null)
			Func.wykonajDlaNieNull((List<Object>) mapa.get("drop"), dropy -> {
				drop = Lists.newArrayList();
				dropy.forEach(drop -> {
					if (drop instanceof Map)
						this.drop.add(new Drop((Map<String, Object>) drop));
					else
						this.drop.add(new Drop(Config.item(drop)));
				});
			});
	}
	@Override
	public Map<String, Object> serialize() {
		return null;
	}
	
	
	public void Dropnij(Player p) {
		if (item != null)
			Func.dajItem(p, item);
		else {
			
		}
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
