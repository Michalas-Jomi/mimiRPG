package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class połączoneEq implements Przeładowalny, Listener {
	public static class Ekwipunki extends Mapowany implements InventoryHolder {
		public static class Itemki implements ConfigurationSerializable {
			public Inventory eq;
			public Inventory ec;

			@SuppressWarnings("unchecked")
			public Krotka<Inventory, Inventory> getEq(Ekwipunki eqs) {
				if (mapa != null) {
					eq = Bukkit.createInventory(eqs, 4*9, "Ekwipunek");
					ec = Bukkit.createInventory(eqs, 3*9, "Ender Chest");

					BiConsumer<Inventory, HashMap<Integer, ItemStack>> bic = (inv, mapa) -> mapa.forEach(inv::setItem);
					
					bic.accept(eq, (HashMap<Integer, ItemStack>) mapa.get("eq"));
					bic.accept(ec, (HashMap<Integer, ItemStack>) mapa.get("ec"));
					
					mapa = null;
				}
				return new Krotka<>(eq, ec);
			}
			
			public Itemki() {}
			private Map<String, Object> mapa;
			public Itemki(Map<String, Object> _mapa) {
				this.mapa = _mapa;
			}
			
			@Override
			public Map<String, Object> serialize() {
				HashMap<String, Object> _mapa = new HashMap<>();
				
				Function<Inventory, HashMap<Integer, ItemStack>> func = inv -> {
					HashMap<Integer, ItemStack> mapa = new HashMap<>();
					
					Krotka<Integer, ?> k = new Krotka<>(0, 0);
					inv.forEach(item -> mapa.put(k.a++, item));
					
					return mapa;
				};
				
				_mapa.put("eq", func.apply(eq));
				_mapa.put("ec", func.apply(ec));
				
				return _mapa;
			}
		}
		@Mapowane public Itemki s1;
		@Mapowane public Itemki s2;
		@Mapowane public Itemki s3;
		
		@Override public Inventory getInventory() { return null; }
	}
	
	void połącz(Player p) {
		String uuid = p.getUniqueId().toString();
		p.saveData();

		Ekwipunki eqs = new Ekwipunki();
		
		BiConsumer<String, Ekwipunki.Itemki> cons = (scieżka, itemki) -> {
			File nowe = new File(scieżka + "/" + uuid + ".dat");
			if (!nowe.exists())
				try {
					nowe.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			File stare = new File("world/playerdata/" + uuid + ".dat");
			File chwilowy = new File(scieżka + "/mimi chwilowy pliczek.dat");
			stare.renameTo(new File(chwilowy.getAbsolutePath()));
			nowe.renameTo(new File(stare.getAbsolutePath()));
			stare.renameTo(new File(nowe.getAbsolutePath()));
			
			p.loadData();
			
			itemki.eq = Bukkit.createInventory(eqs, 5*9, "Ekwipunek " + scieżka);
			itemki.ec = Bukkit.createInventory(eqs, 3*9, "Ender Chest " + scieżka);
			
			int i=0;
			for (ItemStack item : p.getInventory())  itemki.eq.setItem(i++, item);
			i = 0;
			for (ItemStack item : p.getEnderChest()) itemki.ec.setItem(i++, item);
		
			p.getInventory().clear();
			p.getEnderChest().clear();
		};
		
		cons.accept("s2", eqs.s2);
		cons.accept("s3", eqs.s3);
		cons.accept("s2", eqs.s1);
		
		config.ustaw_zapisz(p.getName(), eqs);
	}

	static final Inventory gui;
	static final int guiId;
	
	static {
		Krotka<Inventory, Integer> k = Func.stwórzInvZId(4, "&3&lWybierz Ekwipunek");
		guiId = k.b;
		gui = k.a;
		
		Func.wypełnij(gui);

		gui.setItem(11, Func.stwórzItem(Material.CHEST, 1, "&1&lEkwipunek &l&lMine&4&lZ &a&ls1"));
		gui.setItem(13, Func.stwórzItem(Material.CHEST, 2, "&1&lEkwipunek &l&lMine&4&lZ &a&ls2"));
		gui.setItem(15, Func.stwórzItem(Material.CHEST, 3, "&1&lEkwipunek &l&lMine&4&lZ &a&ls3"));
		gui.setItem(20, Func.stwórzItem(Material.ENDER_CHEST, 1, "&1&5Ender Chest &l&lMine&4&lZ &a&ls1"));
		gui.setItem(22, Func.stwórzItem(Material.ENDER_CHEST, 2, "&1&5Ender Chest &l&lMine&4&lZ &a&ls2"));
		gui.setItem(24, Func.stwórzItem(Material.ENDER_CHEST, 3, "&1&5Ender Chest &l&lMine&4&lZ &a&ls3"));
	}
	
	
	void otwórz(Player p) {
		p.openInventory(gui);
		p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
	}
	

	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (!(
				ev.getInventory().getHolder() instanceof Func.FuncIdHolder && 
				ev.getInventory().getHolder() != null && 
				((Func.FuncIdHolder) ev.getInventory().getHolder()).getId() == guiId
				))
			return;
		
		int slot = ev.getRawSlot();
		if (slot >= ev.getInventory().getSize() || slot < 0)
			return;
		
		String fk;
		switch (ev.getCurrentItem().getType()) {
		case CHEST: 	  fk = "a"; break;
		case ENDER_CHEST: fk = "b"; break;
		default: return;
		}
		
		Ekwipunki eqs = (Ekwipunki) config.wczytaj(ev.getWhoClicked().getName());
		try {
			Ekwipunki.Itemki itemki = (Ekwipunki.Itemki) Ekwipunki.class.getDeclaredField("s" + ev.getCurrentItem().getAmount()).get(eqs);
			ev.getWhoClicked().openInventory((Inventory) Krotka.class.getDeclaredField(fk).get(itemki.getEq(eqs)));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getInventory().getHolder() instanceof Ekwipunki && ev.getInventory().getHolder() != null)
			config.ustaw_zapisz(ev.getPlayer().getName(), ev.getInventory().getHolder());
	}
	
	
	final Config config = new Config("configi/PołączoneEq");
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane eq", config.klucze(false).size());
	}
}
