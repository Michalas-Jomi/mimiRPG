package me.jomi.mimiRPG.MineZ;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.MineZ.połączoneEq.Ekwipunki.Itemki;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;


@Moduł
public class połączoneEq extends Komenda implements Listener {
	public połączoneEq() {
		super("połączEq", "/połączEq (gracz)");
	}


	public static class Ekwipunki extends Mapowany implements InventoryHolder {
		public static class Itemki implements ConfigurationSerializable {
			public Inventory eq;
			public Inventory ec;

			@SuppressWarnings("unchecked")
			public Krotka<Inventory, Inventory> getEq(Ekwipunki eqs) {
				if (mapa != null) {
					eq = Bukkit.createInventory(eqs, 5*9, "Ekwipunek");
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
					
					Krotka<Integer, ?> k = new Krotka<>(-1, 0);

					if (inv != null)
						inv.forEach(item -> {
							k.a++;
							Func.wykonajDlaNieNull(item, _item -> mapa.put(k.a, _item));
						});
					
					return mapa;
				};
				
				_mapa.put("eq", func.apply(eq));
				_mapa.put("ec", func.apply(ec));
				
				return _mapa;
			}
		}
		@Mapowane public Itemki s1;
		@Mapowane public Itemki s2;
		//@Mapowane public Itemki s3;
		
		@Override
		public void Init() {
			s1.getEq(this);
			s2.getEq(this);
			//s3.getEq(this);
		}
		
		@Override public Inventory getInventory() { return null; }
	}
	
	void połącz(Player p) {
		String uuid = p.getUniqueId().toString();
		p.saveData();

		Ekwipunki eqs = new Ekwipunki();
		eqs.s1 = new Ekwipunki.Itemki();
		eqs.s2 = new Ekwipunki.Itemki();
		Itemki fake = new Ekwipunki.Itemki();
		
		BiConsumer<String, Ekwipunki.Itemki> cons = (scieżka, itemki) -> {
			File dir = new File(scieżka);
			if (!dir.exists())
				dir.mkdirs();
			
			
			final String nowe = scieżka + "/" + uuid + ".dat";
			final String stare = "world/playerdata/" + uuid + ".dat";
			final String chwilowy = scieżka + "/mimi chwilowy pliczek.dat";
			
			
			
			File nowy = new File(nowe);
			if (!nowy.exists())
				try {
					nowy.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			new File(stare).renameTo(new File(chwilowy));
			new File(nowe).renameTo(new File(stare));
			new File(chwilowy).renameTo(new File(nowe));
			
			p.loadData();
			
			new File(stare).delete();
			
			itemki.eq = Bukkit.createInventory(eqs, 5*9, "Ekwipunek " + scieżka);
			itemki.ec = Bukkit.createInventory(eqs, 3*9, "Ender Chest " + scieżka);
			
			int i=0;
			for (ItemStack item : p.getInventory()) itemki.eq.setItem(i++, item);
			i = 0;
			for (ItemStack item : p.getEnderChest()) itemki.ec.setItem(i++, item);
		
			p.getInventory().clear();
			p.getEnderChest().clear();
		};
		
		cons.accept("s2", eqs.s2);
		cons.accept("s3", fake);
		cons.accept("s2", eqs.s1);
		
		getConfig(p.getUniqueId()).ustaw_zapisz(p.getName(), eqs);
	}

	static final Inventory gui;
	static final int guiId;
	
	static {
		Krotka<Inventory, Integer> k = Func.stwórzInvZId(4, "&3&lWybierz Ekwipunek");
		guiId = k.b;
		gui = k.a;
		
		Func.wypełnij(gui);

		gui.setItem(12, Func.stwórzItem(Material.CHEST, 1, "&1&lEkwipunek &7&lMine&4&lZ &a&ls1"));
		gui.setItem(14, Func.stwórzItem(Material.CHEST, 2, "&1&lEkwipunek &7&lMine&4&lZ &a&ls2"));
		//gui.setItem(15, Func.stwórzItem(Material.CHEST, 3, "&1&lEkwipunek &7&lMine&4&lZ &a&ls3"));
		gui.setItem(21, Func.stwórzItem(Material.ENDER_CHEST, 1, "&1&5Ender Chest &7&lMine&4&lZ &a&ls1"));
		gui.setItem(23, Func.stwórzItem(Material.ENDER_CHEST, 2, "&1&5Ender Chest &7&lMine&4&lZ &a&ls2"));
		//gui.setItem(24, Func.stwórzItem(Material.ENDER_CHEST, 3, "&1&5Ender Chest &7&lMine&4&lZ &a&ls3"));
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
		
		Ekwipunki eqs = (Ekwipunki) getConfig(ev.getWhoClicked().getUniqueId()).wczytaj(ev.getWhoClicked().getName());
		if (eqs == null)
			połącz((Player) ev.getWhoClicked());
		eqs = (Ekwipunki) getConfig(ev.getWhoClicked().getUniqueId()).wczytaj(ev.getWhoClicked().getName());
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
			getConfig(ev.getPlayer().getUniqueId()).ustaw_zapisz(ev.getPlayer().getName(), ev.getInventory().getHolder());
	}
	
	
	Config getConfig(UUID uuid) {
		return new Config("configi/PołączoneEq/" + uuid);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		if (sender instanceof Player)
			p = (Player) sender;
		if (args.length > 0)
			p = Bukkit.getPlayer(args[0]);
		
		if (p == null)
			return false;
		
		otwórz(p);
		
		return true;
	}
}
