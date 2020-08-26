package me.jomi.mimiRPG.CustomowyDrop;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Prze³adowalny;

public class CustomowyDrop implements Listener, Prze³adowalny{

	public static final Config configBloki = new Config("Customowy Drop Bloki", "CustomowyDrop/Customowy Drop Bloki");
	public static final Config configMoby  = new Config("Customowy Drop Moby", "CustomowyDrop/Customowy Drop Moby");

	public static final HashMap<String, List<Drop>> mapa = new HashMap<>();
	
	private static int _bloki;
	private static int _moby;
	public void prze³aduj() {
		mapa.clear();
		
		_prze³aduj(configBloki);
		_bloki = 0;
		for (List<Drop> lista : mapa.values())
			_bloki += lista.size();
		
		_prze³aduj(configMoby);
		int wszystko = 0;
		for (List<Drop> lista : mapa.values())
			wszystko += lista.size();
		_moby = wszystko - _bloki;
	}
	public String raport() {
		return "§6Customowe Dropy z bloków: §e" + _bloki + 
		 "\n" + "§6Customowe Dropy z mobów: §e" + _moby;
			
	}
	
	@SuppressWarnings("unchecked")
	private static void _prze³aduj(Config config) {
		config.prze³aduj();

		for (String typ : config.klucze(false)) {
			List<Object> lista = (List<Object>) config.wczytaj(typ);
			typ = typ.replace("x", "");
			LinkedHashMap<String, Object> _mapa = (LinkedHashMap<String, Object>) lista.get(0);
			Drop drop = new Drop(
					!(boolean)  _mapa.getOrDefault("pierwotnyDrop", true),
					Config.item(_mapa.get("glowa")),
					(String) 	_mapa.get("imie"));
			for (int i=1; i<lista.size(); )
				drop.dodajDrop((double) lista.get(i++), (double) lista.get(i++), Config.item(lista.get(i++)));
			if (!mapa.containsKey(typ))
				mapa.put(typ, Lists.newArrayList());
			mapa.get(typ).add(drop);
		}
	}
	
	@EventHandler
	public void niszczenieBloku(BlockBreakEvent ev) {
		if (ev.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		String mat = ev.getBlock().getType().toString();
		if (!mapa.containsKey(mat)) return;
		ItemStack narzêdzie = ev.getPlayer().getInventory().getItemInMainHand();
		if (narzêdzie != null && narzêdzie.hasItemMeta() && narzêdzie.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) return;
		if (ev.isDropItems()) {
			Location loc = ev.getBlock().getLocation();
			for (Drop drop : mapa.get(mat)) {
				drop.dropnij(loc, narzêdzie.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
				if (drop.wy³¹czPierwotny)
					ev.setDropItems(false);
			}
		}
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void œmieræMoba(EntityDeathEvent ev) {
		final String typ = ev.getEntityType().toString();
		final LivingEntity mob = ev.getEntity();
		if (mapa.containsKey(typ))
			for (Drop drop : mapa.get(typ)) 
				if (drop.warunek(mob)) {
					ItemStack broñ = mob.getKiller() != null ? mob.getKiller().getInventory().getItemInMainHand() : null;
					int lvl = 0;
					if (broñ != null)
						lvl = broñ.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
					Location loc = mob.getLocation();
					if (drop.wy³¹czPierwotny)
						ev.getDrops().clear();
					drop.dropnij(loc, lvl);
				}
	}
}
