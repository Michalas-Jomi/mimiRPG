package me.jomi.mimiRPG.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;

public class InteractManager {
	public static class InteractListener implements Listener {
		@EventHandler
		public void interakcja(PlayerInteractEvent ev) {
			Map<Material, Map<Item, Predicate<PlayerInteractEvent>>> mapa = null;
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
			
			Func.wykonajDlaNieNull(mapa.get(item.getType()), mapaItemów ->
				Func.wykonajDlaNieNull(mapaItemów.get(new Item(item)), pred -> {
					if (pred.test(ev)) {
						item.setAmount(item.getAmount() - 1);
						ev.getPlayer().getInventory().setItemInMainHand(item.getAmount() <= 0 ? null : item);
					}
				}));
		}
	}
	public static enum Sposób {
		LEWY,
		PRAWY,
		OBA;
		
		private Map<Material, Map<Item, Predicate<PlayerInteractEvent>>> mapa = null;
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
	
	private static boolean zarejestrowanyListener = false;
	public static void zarejestruj(ItemStack item, Sposób sposób, Predicate<PlayerInteractEvent> pred) {
		if (item == null)
			return;
		
		if (sposób == Sposób.OBA) {
			zarejestruj(item, Sposób.PRAWY,	pred);
			zarejestruj(item, Sposób.LEWY,	pred);
			return;
		}
		
		if (!zarejestrowanyListener) {
			Main.zarejestruj(new InteractListener());
			zarejestrowanyListener = true;
		}
		
		if (sposób.mapa == null)
			sposób.mapa = new EnumMap<>(Material.class);
		
		Map<Item, Predicate<PlayerInteractEvent>> mapa = sposób.mapa.get(item.getType());
		if (mapa == null) {
			mapa = new HashMap<>();
			sposób.mapa.put(item.getType(), mapa);
		}
		
		Item _item = new Item(item);
		if (mapa.containsKey(_item))
			throw new IllegalArgumentException("Item " + Napis.item(item) + " jest już zarejestrowany w Manadżerze interakcji");
		else
			mapa.put(_item, pred);
	}
	public static void wyrejestruj(ItemStack item, Sposób sposób) {
		if (item == null)
			return;
		
		Map<Item, Predicate<PlayerInteractEvent>> mapaItemów = sposób.mapa.get(item.getType());
		
		mapaItemów.remove(new Item(item));
		
		if (mapaItemów.isEmpty()) {
			sposób.mapa.remove(item.getType());
			if (sposób.mapa.isEmpty())
				sposób.mapa = null;
		}
	}
}
