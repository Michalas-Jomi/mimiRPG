package me.jomi.mimiRPG.Maszyny;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Barrel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Budownik extends Komenda implements Listener, Zegar {
	public static final String prefix = Func.prefix("Budownik");
	private final ItemStack itemSkrzynia = Func.połysk(Func.stwórzItem(Material.BARREL, "&6&lBudownik",
		"&bTo jest jeden z rogów Budownika", "&bDrugi otrzymasz po postawieniu tego",
		"&bGdy oba będą postawione,", "&bBudownik będzie budował od rogu do rogu blokami", "&bZ tego bloku"));
	private final ItemStack itemRóg = Func.połysk(Func.stwórzItem(Material.CHISELED_STONE_BRICKS, "&e&lRóg Budownika",
		"&bTo jest róg budownika", "&bBudownik będzie budować od bloku postawionego wcześniej",
		"&bDo tego rogu surowcami", "&bZ tamtego rogu (Beczki)"));
	
	static final HashMap<String, Location> mapa = new HashMap<>();
	public static final List<_Budownik> budowniki = Lists.newArrayList();
	
	static final Config config = new Config("configi/Budowniki");
	public Budownik() {
		super("budownik", prefix + "/budownik <gracz>");
		ustawKomende("rógbudownika", null, null);
		Main.plugin.getCommand("rógbudownika").setPermission(null);
		for (String klucz : config.klucze())
			budowniki.add(_Budownik.wczytaj(config, klucz));
	}
	@Override
	public int czas() {
		for (_Budownik budownik : budowniki)
			budownik.particle();
		for (Location loc : mapa.values())
			loc.getWorld().spawnParticle(Particle.COMPOSTER, loc.clone().add(.5, .6, .5), 10, .6, .7, .6, 0);
		return 5;
	}
	public static void wyłączanie() {
		for (Location loc : mapa.values())
			loc.getBlock().setType(Material.AIR);
		for (_Budownik budownik : budowniki)
			budownik.zapisz(config);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		ItemStack item;
		if (cmd.getName().equals("budownik"))
			item = itemSkrzynia;
		else
			item = itemRóg;
		if (sender instanceof Player)
			Func.dajItem(((Player) sender), item);
		else
			if (args.length >= 1) {
				Player p = Bukkit.getPlayer(args[0]);
				if (p == null)
					sender.sendMessage(prefix + "Nieodnaleziono gracza§e " + args[0]);
				else
					Func.dajItem(p, item);
			} else
				return false;
		return true;
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	void stawianie(BlockPlaceEvent ev) {
		if (!ev.canBuild() || ev.isCancelled())
			return;
		Player p = ev.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		if (Func.porównaj(item, itemSkrzynia)) {
			if (mapa.containsKey(p.getName())) {
				ev.setCancelled(true);
				p.sendMessage(prefix + "Najpierw ustaw róg poprzedniego Budownika, zamin zabierzesz się za kolejny, jeśli go zgubiłeś użyj /rógbudownika");
				return;
			}
			if (p.getGameMode().equals(GameMode.CREATIVE))
				item.setAmount(item.getAmount() - 1);
			p.sendMessage(prefix + "Skrzynia budownika ustawiona, pora na róg");
			Func.dajItem(p, itemRóg);
			mapa.put(p.getName(), ev.getBlock().getLocation());
		} else if (Func.porównaj(item, itemRóg)) {
			ev.setCancelled(true);
			if (!mapa.containsKey(p.getName())) {
				p.sendMessage(prefix + "Nie ustawiasz aktualnie żadnego budownika");
				p.getInventory().setItemInMainHand(null);
				return;
			}
			if (mapa.get(p.getName()).getWorld().getUID().equals(ev.getBlock().getLocation().getWorld().getUID()) && 
					mapa.get(p.getName()).distance(ev.getBlock().getLocation()) <= 100) {
				new _Budownik(p, mapa.get(p.getName()), ev.getBlock().getLocation());
				mapa.remove(p.getName());
				p.getInventory().setItemInMainHand(null);
			} else
				p.sendMessage(prefix + "Budownik nie jest w stanie budować na tak daleki dystans");
		}
	}
	@EventHandler
	void niszczenie(BlockBreakEvent ev) {
		if (ev.getBlock().getType().equals(itemSkrzynia.getType())) {
			Location loc = ev.getBlock().getLocation();
			if (mapa.containsValue(loc)) {
				for (Entry<String, Location> en : mapa.entrySet())
					if (en.getValue().equals(loc)) {
						mapa.remove(en.getKey());
						Player p = Bukkit.getPlayer(en.getKey());
						if (p != null)
							p.sendMessage(prefix + "Twój Budownik został zniszczony");
						break;
					}
				Func.dajItem(ev.getPlayer(), itemSkrzynia);
				ev.setCancelled(true);
				ev.getBlock().setType(Material.AIR, true);
			}
			for (int i=0; i<budowniki.size(); i++)
				if (budowniki.get(i).skrzynia.equals(loc)) {
					budowniki.get(i).usuńZConfigu(config);
					budowniki.remove(i);
					return;
				}
		}
	}
	
	@EventHandler
	void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getView().getTitle().equals(itemSkrzynia.getItemMeta().getDisplayName())) {
			Location loc = ev.getInventory().getLocation();
			for (_Budownik budownik : budowniki)
				if (budownik.skrzynia.equals(loc)) {
					budownik.buduj();
					return;
				}
		}
	}
}

class _Budownik {
	static final String prefix = Budownik.prefix;
	Location skrzynia;
	Location rog;
	Location akt;
	int[] zwiększanie = {0, 0, 0};
	String id;
	_Budownik(Player p, Location skrzynia, Location rog) {
		this.skrzynia = skrzynia;
		akt = skrzynia.clone();
		this.rog = rog;
		
		zwiększanie();

		Budownik.budowniki.add(this);
		
		p.sendMessage(prefix + "Budownik poprawnie zainstalowany");
		
		następna();
		buduj();
		
		int i = 0;
		while (Budownik.config.klucze().contains(""+(++i)));
		id = "" + i;
		zapisz(Budownik.config);
	}
	private void zwiększanie() {
		zwiększanie[0] = skrzynia.getBlockX() <= rog.getBlockX() ? 1 : -1;
		zwiększanie[1] = skrzynia.getBlockY() <= rog.getBlockY() ? 1 : -1;
		zwiększanie[2] = skrzynia.getBlockZ() <= rog.getBlockZ() ? 1 : -1;
	}
	void następna() {
		akt.add(zwiększanie[0], 0, 0);
		if (akt.getBlockX() == rog.getBlockX() + zwiększanie[0]) {
			akt.setX(skrzynia.getX());
			akt.add(0, 0, zwiększanie[2]);
			if (akt.getBlockZ() == rog.getBlockZ() + zwiększanie[2]) {
				akt.setZ(skrzynia.getZ());
				akt.add(0, zwiększanie[1], 0);
				if (akt.getBlockY() == rog.getBlockY() + zwiększanie[1]) {
					koniec();
					return;
				}
			}
		}
		if (!akt.getBlock().getType().isAir())
			następna();
	}
	boolean koniec = false;
	void koniec() {
		koniec = true;
		for (int i=0; i<Budownik.budowniki.size(); i++)
			if (skrzynia.equals(Budownik.budowniki.get(i).skrzynia)) {
				Budownik.budowniki.remove(i);
				Budownik.config.ustaw(""+id, null);
				break;
			}
		Bukkit.selectEntities(Bukkit.getConsoleSender(), "@a[x=263,y=63,z=575,distance=..3]");
		for (Entity en : skrzynia.getWorld().getNearbyEntities(skrzynia, 20, 30, 20))
			if (en instanceof Player)
				((Player) en).sendTitle("§6§lBudowa ukończona", "§eBudownik zakończył już swoją prace", 20, 80, 20);
		((Barrel) skrzynia.getBlock().getState()).setCustomName("§e§l§oBeczunia Po Budownikowa");
		skrzynia.getWorld().playSound(skrzynia, Sound.ENTITY_WITHER_SHOOT, 1, 0);
	}
	
	void buduj() {
		if (koniec) return;
		Inventory inv = ((Barrel) skrzynia.getBlock().getState()).getInventory();
		for (ItemStack item : inv)
			if (item != null && item.getType().isBlock() && !item.getType().toString().endsWith("SHULKER_BOX") && !item.getItemMeta().hasEnchants()) {
				akt.getBlock().setType(item.getType());
				item.setAmount(item.getAmount() - 1);
				akt.getWorld().spawnParticle(Particle.CLOUD, akt, 20, .5, .5, .5, .05);
				akt.getWorld().playSound(akt, Sound.BLOCK_STONE_PLACE, .6f, 1);
				następna();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		            @Override
					public void run() {
						buduj();
		            }
		        }, 1);
				return;
			}
	}
	
	void usuńZConfigu(Config config) {
		config.ustaw(id, null);
	}
	
	void particle() {
		skrzynia.getWorld().spawnParticle(Particle.SPELL_WITCH, skrzynia.clone().add(.5,  1, .5), 2,  .1, .1, .1, 0);
		rog		.getWorld().spawnParticle(Particle.SPELL_WITCH, rog.clone()		.add(.5, .5, .5), 2,  .1, .1, .1, 0);
		akt		.getWorld().spawnParticle(Particle.FLAME, 		akt.clone()		.add(.5, .5, .5), 10, .3, .3, .3, 0);
	}
	
	void zapisz(Config config) {
		config.ustaw(id + ".skrzynia", skrzynia);
		config.ustaw(id + ".rog", rog);
		config.ustaw_zapisz(id + ".akt", akt);
	}
	private _Budownik() {}
	static _Budownik wczytaj(Config config, String sciezka) {
		_Budownik budownik = new _Budownik();
		budownik.skrzynia = (Location) config.wczytaj(sciezka + ".skrzynia");
		budownik.rog = (Location) config.wczytaj(sciezka + ".rog");
		budownik.akt = (Location) config.wczytaj(sciezka + ".akt");
		budownik.id = sciezka;
		budownik.zwiększanie();
		return budownik;
	}
	
}
