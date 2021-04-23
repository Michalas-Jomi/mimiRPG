package me.jomi.mimiRPG;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import me.jomi.mimiRPG.util.Func;

public class InteractManager {
	public static class InteractListener implements Listener {
		@EventHandler
		public void interakcja(PlayerInteractEvent ev) {
			Map<Material, Multimap<Item, Predicate<PlayerInteractEvent>>> mapa = null;
			switch (ev.getAction()) {
			case PHYSICAL:
				return;
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				mapa = Sposób.LEWY.mapa;
				break;
			case RIGHT_CLICK_AIR:
			case RIGHT_CLICK_BLOCK:
				mapa = Sposób.PRAWY.mapa;
				break;
			}
			
			if (mapa == null)
				return;
			
			ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
			
			Func.wykonajDlaNieNull(mapa.get(item.getType()), multimapa ->
				Func.wykonajDlaNieNull(multimapa.get(new Item(item)), managery ->
					managery.forEach(pred -> {
						if (pred.test(ev)) {
							item.setAmount(item.getAmount() - 1);
							ev.getPlayer().getInventory().setItemInMainHand(item.getAmount() <= 0 ? null : item);
						}
					})));
			
		}
	}
	public static enum Sposób {
		LEWY,
		PRAWY,
		OBA;
		
		private Map<Material, Multimap<Item, Predicate<PlayerInteractEvent>>> mapa = null;
	}
	
	
	private static class Item {
		public final ItemStack item;
		
		public Item(ItemStack item) {
			this.item = item;
		}
		
		@Override
		public int hashCode() {
			return hashCode(item);
		}
		public static int hashCode(ItemStack item) {
			int hash = 1;
			hash = hash * 31 + item.getType().hashCode();
			hash = hash * 31 + (item.hasItemMeta() ? item.getItemMeta().hashCode() : 0);
			return hash;
			
		}
	}
	
	public static void zarejestruj(ItemStack item, Sposób sposób, boolean zabieraj, Predicate<PlayerInteractEvent> pred) {
		if (sposób == Sposób.OBA) {
			zarejestruj(item, Sposób.PRAWY,	zabieraj, pred);
			zarejestruj(item, Sposób.LEWY,	zabieraj, pred);
			return;
		}
		
		if (sposób.mapa == null) {
			Main.zarejestruj(new InteractListener());
			sposób.mapa = new EnumMap<>(Material.class);
		}
		
		Multimap<Item, Predicate<PlayerInteractEvent>> multimapa = sposób.mapa.get(item.getType());
		if (multimapa == null) {
			multimapa = HashMultimap.create();
			sposób.mapa.put(item.getType(), multimapa);
		}
		
		multimapa.put(new Item(item), pred);
	}
}
