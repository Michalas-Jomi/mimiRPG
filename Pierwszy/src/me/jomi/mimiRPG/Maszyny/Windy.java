package me.jomi.mimiRPG.Maszyny;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;


//TODO edytor
//TODO szablon "dane": WindaData

@Moduł
public class Windy implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Winda");
	
	public static class WindaData extends Mapowany {
		@Mapowane List<Winda> windy;
	}
	public static class Winda extends Mapowany {
		@Mapowane Set<Location> lokacje = new HashSet<>();
		@Mapowane ItemStack item;
		@Mapowane String id; // klucz w configu
		
		private static boolean bezpieczna(Location loc) {
			loc = loc.clone();
			return	loc.add(0, 1, 0).getBlock().isPassable() && 
					loc.add(0, 1, 0).getBlock().isPassable();
		}
		private Location fabryka(Location start, Predicate<Integer> yGit, int y_zmiana) {
			start = start.clone();
			while (yGit.test(start.getBlockY()))
				if (lokacje.contains(start.add(0, y_zmiana, 0)) && bezpieczna(start))
					return start;
			return null;
			
		}
		public Location doGóry(Location start) { return fabryka(start, y -> y <= start.getWorld().getMaxHeight(), 1); }
		public Location doDołu(Location start) { return fabryka(start, y -> y >= 0,								 -1); }
	
		public String getKodCustom() {
			return "winda_" + id;
		}
	}
	
	static void tp(Player p, Location loc) {
		p.teleport(loc);
		loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, .3f, .75f);
		Func.particle(Particle.SNEEZE, loc.add(0, 1, 0), 100, .5, 1, .5, 0);
	}
	
	
	static Map<Location, Winda> zLokacji = new HashMap<>();
	static Config config = new Config("configi/Windy");
	static WindaData dane;
	
	
	private void przenieś(Player p, Cancellable ev, BiFunction<Winda, Location, Location> bic) {
		Location loc = p.getLocation().add(0, -1, 0).getBlock().getLocation();
		
		Func.wykonajDlaNieNull(zLokacji.get(loc), winda -> 
			Func.wykonajDlaNieNull(bic.apply(winda, loc), doTp -> {
				if (ev != null) ev.setCancelled(true);
				Location tp = p.getLocation();
				tp.setY(doTp.getY() + 1);
				tp(p, tp);
			}));
	}
	@EventHandler
	public void skakanie(PlayerStatisticIncrementEvent ev) {
		if (ev.getStatistic() == Statistic.JUMP)
			przenieś(ev.getPlayer(), ev, Winda::doGóry);
		
	}
	@EventHandler
	public void kucanie(PlayerToggleSneakEvent ev) {
		if (!ev.getPlayer().isSneaking())
			przenieś(ev.getPlayer(), null, Winda::doDołu);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void kopanie(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		Func.wykonajDlaNieNull(zLokacji.remove(ev.getBlock().getLocation()), winda -> {
			ev.setDropItems(false);
			ev.setExpToDrop(0);
			
			Func.dajItem(ev.getPlayer(), winda.item);
			new Napis(prefix + "Zebrano windę ").dodaj(Napis.item(winda.item)).wyświetl(ev.getPlayer());
			
			winda.lokacje.remove(ev.getBlock().getLocation());
			config.ustaw_zapisz("dane", dane);
		});
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void stawianie(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		if (!ev.canBuild()) return;
		
		for (Winda winda : dane.windy)
			if (ev.getItemInHand().isSimilar(winda.item)) {
				zLokacji.put(ev.getBlock().getLocation(), winda);
				winda.lokacje.add(ev.getBlock().getLocation());
				
				new Napis(prefix + "Postawiono windę ").dodaj(Napis.item(winda.item)).wyświetl(ev.getPlayer());
				
				config.ustaw_zapisz("dane", dane);
				break;
			}
	}
	
	@Override
	public void przeładuj() {
		zLokacji.clear();
		
		config.przeładuj();
		
		if (dane != null)
			dane.windy.forEach(winda -> CustomoweItemy.customoweItemy.remove(winda.getKodCustom()));
		
		dane = config.wczytajPewny("dane");
		if (dane == null)
			dane = Func.utwórz(WindaData.class);
		
		dane.windy.forEach(winda -> CustomoweItemy.customoweItemy.put(winda.getKodCustom(), winda.item));
		dane.windy.forEach(winda -> winda.lokacje.forEach(loc -> zLokacji.put(loc, winda)));
	}
	@Override
	public Krotka<String, Object> raport() {
		int sum[] = new int[]{0};
		dane.windy.forEach(winda -> sum[0] += winda.lokacje.size());
		return Func.r("Wczytane Windy", dane.windy.size() + "/" + sum[0]);
	}
	
	@Override
	public int czas() {
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			dane.windy.forEach(winda -> winda.lokacje.forEach(loc -> {
				Func.particle(Particle.END_ROD, loc.clone().add(.5, 1.2, .5), 1, .3, 0, .3, 0);
				Func.particle(Particle.COMPOSTER, loc.clone().add(.5, 1, .5), Func.losuj(2, 5), .1, 0, .1, 0);
			}));
		});
		return 15;
	}
}
