package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Chat.Raport;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CustomowyDrop implements Listener, Przeładowalny {
	static class Drop {
		List<Item> itemy = Lists.newArrayList();
		boolean wyłączPierwotny;
		boolean blokuj;
		ItemStack itemNaGłowie;
		String imie;
		
		public Drop(boolean wyłączPierwotny, boolean blokuj, ItemStack itemNaGłowie, String imie) {
			this.wyłączPierwotny = wyłączPierwotny;
			this.itemNaGłowie = itemNaGłowie;
			this.blokuj = blokuj;
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

		@Override
		public String toString() {
			return String.format("§rDrop(imie=%s)", Func.odkoloruj(imie));
		}
		
	}
	static class Item {
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
	
	public static final Config configBloki = new Config("Customowy Drop Bloki");
	public static final Config configMoby  = new Config("Customowy Drop Moby");

	// Material : [Drop]
	public static final HashMap<String, List<Drop>> mapa = new HashMap<>();
	
	private static int _bloki;
	private static int _moby;
	@Override
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
	@Override
	public Krotka<String, Object> raport() {
		return Func.r(
				Raport.raport(Func.r("Customowe Dropy z bloków", _bloki)) + "\n" 
				+ "Customowe Dropy z mobów", _moby);
			
	}
	
	@SuppressWarnings("unchecked")
	private static void _przeładuj(Config config) {
		config.przeładuj();

		for (String typ : config.klucze()) {
			List<Object> lista = (List<Object>) config.wczytaj(typ);
			typ = typ.replace("x", "");
			LinkedHashMap<String, Object> _mapa = (LinkedHashMap<String, Object>) lista.get(0);
			Drop drop = new Drop(
					!(boolean)  _mapa.getOrDefault("pierwotnyDrop", true),
					 (boolean)  _mapa.getOrDefault("blokuj", false),
					Config.item(_mapa.get("glowa")),
					(String) 	_mapa.get("imie"));
			for (int i=1; i<lista.size(); )
				drop.dodajDrop((double) lista.get(i++), (double) lista.get(i++), Config.item(lista.get(i++)));
			if (!mapa.containsKey(typ))
				mapa.put(typ, Lists.newArrayList());
			mapa.get(typ).add(drop);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void spadającyBlok(EntityDropItemEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getEntity() instanceof FallingBlock)
			Func.wykonajDlaNieNull(mapa.get(((FallingBlock) ev.getEntity()).getBlockData().getMaterial().toString()), dropy -> {
				for (Drop drop : dropy)
					if (drop.blokuj) {
						ev.setCancelled(true);
						return;
					}
				dropy.forEach(drop -> {
					if (drop.wyłączPierwotny)
						ev.setCancelled(true);
					drop.dropnij(ev.getEntity().getLocation(), 0);
				});
			});
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void niszczenieBloku(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		String mat = ev.getBlock().getType().toString();
		if (!mapa.containsKey(mat)) return;
		
		for (Drop drop : mapa.get(mat))
			if (drop.blokuj) {
				ev.setDropItems(false);
				ev.setExpToDrop(0);
				return;
			}
		
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
	@EventHandler
	public void wyrzucanieItemów(PlayerDropItemEvent ev) {
		if (ev.isCancelled()) return;
		
		ItemStack item = ev.getItemDrop().getItemStack();
		try {
			Func.wykonajDlaNieNull(mapa.get(item.getType().toString()), dropy -> {
				dropy.forEach(drop -> {
					if (drop.blokuj)
						throw new RuntimeException();
				});
			});
		} catch (RuntimeException e) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(Func.msg(Func.prefix("Blokady") + "Hej! Nie wyrzucaj tego."));
		}
		
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void blokDropiącyItemy(ItemSpawnEvent ev) {
		if (ev.isCancelled()) return;
		
		ItemStack item = ev.getEntity().getItemStack();
		try {
			Func.wykonajDlaNieNull(mapa.get(item.getType().toString()), dropy -> {
				dropy.forEach(drop -> {
					if (drop.blokuj)
						throw new RuntimeException();
				});
			});
		} catch (RuntimeException e) {
			ev.setCancelled(true);
			ev.getEntity().remove();
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


