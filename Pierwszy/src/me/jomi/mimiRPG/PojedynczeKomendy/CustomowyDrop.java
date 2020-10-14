package me.jomi.mimiRPG.PojedynczeKomendy;

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
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Przeładowalny;

@Moduł
public class CustomowyDrop implements Listener, Przeładowalny{

	public static final Config configBloki = new Config("Customowy Drop Bloki");
	public static final Config configMoby  = new Config("Customowy Drop Moby");

	public static final HashMap<String, List<Drop>> mapa = new HashMap<>();
	
	private static int _bloki;
	private static int _moby;
	public void przeładuj() {
		mapa.clear();
		
		_przeładuj(configBloki);
		_bloki = 0;
		for (List<Drop> lista : mapa.values())
			_bloki += lista.size();
		
		_przeładuj(configMoby);
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
	private static void _przeładuj(Config config) {
		config.przeładuj();

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
		ItemStack narzędzie = ev.getPlayer().getInventory().getItemInMainHand();
		if (narzędzie != null && narzędzie.hasItemMeta() && narzędzie.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) return;
		if (ev.isDropItems()) {
			Location loc = ev.getBlock().getLocation();
			for (Drop drop : mapa.get(mat)) {
				drop.dropnij(loc, narzędzie.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
				if (drop.wyłączPierwotny)
					ev.setDropItems(false);
			}
		}
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void śmierćMoba(EntityDeathEvent ev) {
		final String typ = ev.getEntityType().toString();
		final LivingEntity mob = ev.getEntity();
		if (mapa.containsKey(typ))
			for (Drop drop : mapa.get(typ)) 
				if (drop.warunek(mob)) {
					ItemStack broń = mob.getKiller() != null ? mob.getKiller().getInventory().getItemInMainHand() : null;
					int lvl = 0;
					if (broń != null)
						lvl = broń.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
					Location loc = mob.getLocation();
					if (drop.wyłączPierwotny)
						ev.getDrops().clear();
					drop.dropnij(loc, lvl);
				}
	}
}

class Drop {
	List<Item> itemy = Lists.newArrayList();
	boolean wyłączPierwotny;
	ItemStack itemNaGłowie;
	String imie;
	
	public Drop(boolean wyłączPierwotny, ItemStack itemNaGłowie, String imie) {
		this.wyłączPierwotny = wyłączPierwotny;
		this.itemNaGłowie = itemNaGłowie;
		if (imie != null)
			imie = Func.koloruj(imie);
		this.imie = imie;
	}
	public boolean warunek(LivingEntity mob) {
		if (imie != null && !imie.equals(mob.getCustomName()))
			return false;
		ItemStack glowa = mob.getEquipment().getHelmet();
		if (glowa == null || glowa.getType().isAir())
			glowa = null;
		if (itemNaGłowie != null && !itemNaGłowie.equals(glowa))
			return false;
		return true;
	}
	public void dodajDrop(double szansa, double bonusEnch, ItemStack item) {
		itemy.add(new Item(szansa, bonusEnch, item));
	}
	public void dropnij(Location loc, int lvl) {
		for (Item item : itemy)
			item.upuść(loc, lvl);
	}

	public String toString() {
		return String.format("§rDrop(imie=%s)", Func.odkoloruj(imie));
	}
	
}

class Item {
	private int pełne;
	private double szansa;
	private ItemStack item;
	private double bonusEnch;
	
	public Item(double szansa, double bonusEnch, ItemStack item) {
		this.item = item;
		pełne = (int) szansa;
		this.bonusEnch = bonusEnch;
		this.szansa =  szansa - pełne;
	}
	
	public void upuść(Location loc, int lvl) {
		double szansa = this.szansa + (lvl * bonusEnch);
		for (int i=0; i<pełne; i++)
			if (Math.random() <= szansa)
				loc.getWorld().dropItem(loc, item);
	}
	
}
