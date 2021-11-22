package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class PaneleSłoneczne implements Listener, Zegar, Przeładowalny {
	public static final String prefix = Func.prefix(PaneleSłoneczne.class);
	
	static int maxEnergia = 5;
	private static final String tagNapisNazwa = "mimiPanelGóra"; 
	private static final String tagNapisTimer = "mimiPanelTimer"; 
	
	static final Map<UUID, Panel> mapaPaneli = new HashMap<>();
	
	static class Panel {
		private static final Pattern pattern = Pattern.compile("§eEnergia§8: §9(\\d+) ?(§7\\((\\d+):(\\d+)\\))?(§4Zasłonięty!)?");
		
		final Location locParticle;
		ArmorStand tytuł;
		ArmorStand napis;
		int energia;
		int minuty;
		int sekundy;
		boolean zasłonięty;
		
		Panel(ArmorStand napis) {
			Main.log(prefix + "loadowany panel na %s", Func.locBlockToString(napis.getLocation()));
			this.napis = napis;
			sprawdzPattern();
			
			ArmorStand _tytuł = null;
			for (Entity e : napis.getNearbyEntities(0, .3, 0))
				if (e.getScoreboardTags().contains(tagNapisNazwa)) {
					_tytuł = (ArmorStand) e;
					break;
				}
			this.tytuł = _tytuł;
			
			locParticle = napis.getLocation().add(0, .3, 0);
			
			if (napis.getLocation().getBlock().getType() != Material.DAYLIGHT_DETECTOR)
				Bukkit.getScheduler().runTask(Main.plugin, this::usuń);
		}
		void sprawdzPattern() {
			Matcher matcher = pattern.matcher(napis.getCustomName());
			if (!matcher.matches())
				throw new IllegalArgumentException("Niepoprawna nazwa timera panelu słonecznego: " + napis.getCustomName());
			
			energia = Func.Int(matcher.group(1));
			
			minuty  = matcher.group(3) != null ? Func.Int(matcher.group(3)) : 60;
			sekundy = matcher.group(4) != null ? Func.Int(matcher.group(4)) : 0;
			
			zasłonięty = matcher.group(5) != null;
		}
		
		private int doSprawdzenia = 0;
		public boolean aktywny() {
			return	!zasłonięty &&
					energia < maxEnergia &&
					napis.getLocation().getChunk().isLoaded();
		}
		public void czas() {
			if (energia >= maxEnergia) return;
			if (!napis.getLocation().getChunk().isLoaded()) {
				return;
			}
			
			if (--doSprawdzenia <= 0) {
				doSprawdzenia = Func.losuj(30, 180);
				boolean zasłonięty = zasłonięty();
				
				if (this.zasłonięty != zasłonięty) {
					this.zasłonięty = zasłonięty;
					odświeżNapis();
				}
			}
			if (doSprawdzenia % 30 == 0)
				Bukkit.getScheduler().runTask(Main.plugin, this::odświeżEntityNapisów);
			
			if (zasłonięty)
				return;
			
			particle();
			if (doSprawdzenia % 5 == 0)
				sprawdzDzień();

			
			if (--sekundy < 0) {
				sekundy = 59;
				if (--minuty < 0) {
					minuty = 60;
					sekundy = 0;
					energia++;
					napis.getWorld().playSound(napis.getLocation(), Sound.ENTITY_CREEPER_HURT, .5f, .5f);
				}
			}
			
			odświeżNapis();
		}
		private boolean zasłonięty() {
			Location loc = napis.getLocation();
			
			while (loc.getY() <= 255) {
				loc.add(0, 1, 0);
				if (!loc.getBlock().getType().isAir() && !loc.getBlock().getType().toString().contains("GLASS"))
					return true;
			}
			
			return false;
		}
		
		public void ustawEnergię(int energia) {
			this.energia = energia;
			odświeżNapis();
		}
		public int getEnergia() {
			return energia;
		}
		
		public void sprawdzDzień() {
	        long time = napis.getWorld().getTime();
	        boolean dzień = time < 12300 || time > 23850;
	        try {
	        	DaylightDetector detector = (DaylightDetector) napis.getLocation().getBlock().getBlockData();
		        if (dzień == detector.isInverted()) {
		        	detector.setInverted(!dzień);
		        	napis.getLocation().getBlock().setBlockData(detector);
		        }
	        } catch (ClassCastException e) {
	        	Bukkit.getScheduler().runTask(Main.plugin, this::usuń);
	        }
		}
		
		public void particle() {
			if (!zasłonięty && Func.losuj(.5))
				Func.particle(Particle.VILLAGER_HAPPY, locParticle, Func.losuj(2, 5), .2, 0, .2, 1);
		}
		
		public void odświeżNapis() {
			if (energia >= maxEnergia)
				napis.setCustomName("§eEnergia§8: §9" + energia);
			else if (zasłonięty)
				napis.setCustomName("§eEnergia§8: §9" + energia + " §4Zasłonięty!");
			else
				napis.setCustomName(String.format("§eEnergia§8: §9%s §7(%02d:%02d)", energia, minuty, sekundy));
		}
		
		public void usuń() {
			if (napis != null) {
				mapaPaneli.remove(napis.getUniqueId());
				
				napis.getLocation().getBlock().setType(Material.AIR);
				napis.remove();
			}
			if (tytuł != null)
				tytuł.remove();
		}
	
		public void odświeżEntityNapisów() {
			Func.wykonajDlaNieNull(Bukkit.getEntity(napis.getUniqueId()), _napis -> Func.wykonajDlaNieNull(Bukkit.getEntity(tytuł.getUniqueId()), _tytuł -> {
				napis = (ArmorStand) _napis;
				tytuł = (ArmorStand) _tytuł;
				
				odświeżNapis();
			}));
		}
	}
	
	
	public PaneleSłoneczne() {
		Func.opóznij(1, () -> Bukkit.getWorlds().forEach(world -> Func.forEach(world.getLoadedChunks(), this::sprawdzChunk)));
	}
	
	
	
	public static void postawPanel(Location loc) {
		loc = loc.getBlock().getLocation().add(.5, .3, .5);
		loc.getBlock().setType(Material.DAYLIGHT_DETECTOR);
		
		zresp(loc, "§a§lPanel Słoneczny", tagNapisNazwa);
		
		ArmorStand armorStand = zresp(loc.add(0, -.25, 0), "§eEnergia§8: §90 §7(60:00)", tagNapisTimer);

		
		znajdz(armorStand);
	}
	private static ArmorStand zresp(Location loc, String customName, String tag) {
		ArmorStand armorStand = Func.zrespNietykalnyArmorStand(loc, customName);
		
		armorStand.addScoreboardTag(tag);
		
		return armorStand;
	}

	static void znajdz(Entity armorStand) {
		if (armorStand instanceof ArmorStand && armorStand.getScoreboardTags().contains(tagNapisTimer))
			if (mapaPaneli.containsKey(armorStand.getUniqueId()))
				Main.warn(prefix + "Próba podwojenia panelu na " + Func.locBlockToString(armorStand.getLocation()));
			else
				mapaPaneli.put(armorStand.getUniqueId(), new Panel((ArmorStand) armorStand));
	}
	
	public static Panel znajdz(Block blok) {
		Location loc = blok.getLocation();

		for (Entity e : loc.getWorld().getNearbyEntities(loc.getBlock().getBoundingBox(), e ->
					e.getLocation().getBlock().getLocation().equals(loc) &&
					e.getScoreboardTags().contains(tagNapisTimer))
				)
			return mapaPaneli.get(e.getUniqueId());
		
		return null;
	}
	
	
	private final Set<String> sprawdzoneChunki = new HashSet<>();
	void sprawdzChunk(Chunk chunk) {
		if (sprawdzoneChunki.add(new StringBuilder().append(chunk.getX()).append(' ').append(chunk.getZ()).toString()))
			Func.forEach(chunk.getEntities(), PaneleSłoneczne::znajdz);
	}
	@EventHandler
	public void ładowanieChunka(ChunkLoadEvent ev) {
		sprawdzChunk(ev.getChunk());
	}
	
	
	
	@EventHandler
	public void uderzanieNapisu(EntityDamageEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains(tagNapisNazwa) || ev.getEntity().getScoreboardTags().contains(tagNapisTimer))
			ev.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void stawianieBloków(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		
		if (ev.getItemInHand().isSimilar(itemPanelu))
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				postawPanel(ev.getBlock().getLocation());
				Main.log(prefix + Func.msg("%s postawił panel %s %s",
						ev.getPlayer().getName(), ev.getBlock().getWorld().getName(), Func.locBlockToString(ev.getBlock().getLocation())));
			});
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void niszczenieBloków(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		
		if (ev.getBlock().getType() == Material.DAYLIGHT_DETECTOR)
			Func.wykonajDlaNieNull(znajdz(ev.getBlock()), panel -> {
				ev.setDropItems(false);
				Bukkit.getScheduler().runTask(Main.plugin, () -> {
					panel.usuń();
					Func.dajItem(ev.getPlayer(), itemPanelu);
					Main.log(prefix + Func.msg("%s podniósł panel %s %s",
							ev.getPlayer().getName(), ev.getBlock().getWorld().getName(), Func.locBlockToString(ev.getBlock().getLocation())));
				});
			});
	}
	
	
	@Override
	public int czas() {
		mapaPaneli.values().forEach(Panel::czas);
		return 20;
	}
	
	
	ItemStack itemPanelu;
	
	
	@Override
	public void przeładuj() {
		itemPanelu = Main.ust.wczytajItemD("PaneleSłoneczne.item");
		CustomoweItemy.customoweItemy.put("panelSłoneczny", itemPanelu);
	}
	@Override
	public Krotka<String, Object> raport() {
		AtomicInteger licz = new AtomicInteger();
		mapaPaneli.values().forEach(panel -> {
			if (panel.aktywny())
				licz.incrementAndGet();
		});
		
		return Func.r("Aktywne Panele Słoneczne", licz.get());
	}
}
