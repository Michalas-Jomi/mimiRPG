package me.jomi.mimiRPG.Minigry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_16_R3.PacketPlayOutMap;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG_Ultra.CustomoweMapy;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class Bingo extends Minigra {
	static final int idMapy;
	static final ItemStack itemMapy;
	static BufferedImage skreślenie;
	static {
		try {
			itemMapy = Func.stwórzItem(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) itemMapy.getItemMeta();
			MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
			view.setTrackingPosition(false);
			view.setUnlimitedTracking(false);
			view.setScale(Scale.NORMAL);
			view.setLocked(true);
			view.setCenterX(0);
			view.setCenterZ(0);
			meta.setMapView(view);
			itemMapy.setItemMeta(meta);
			
			idMapy = meta.getMapView().getId();
		} catch (Throwable e) {
			e.printStackTrace();
			throw Func.throwEx(e);
		}
	}
	public static class Arena extends Minigra.Arena {
		@Mapowane String customowaMapa = "bingo";
		@Mapowane String folderItemów = "bingo";
		List<Material> materiały = new ArrayList<>();
		Map<String, Set<Material>> znalezione = new HashMap<>();
		byte[] mapa;
		World world;
		
		// start / koniec
		private static final PotionEffect startowyEfekt = new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1, false, false, false);
		@Override
		public void start() {
			mapa = losujMape(losujItemy());
			PacketPlayOutMap packet = CustomoweMapy.packet(idMapy, mapa);
			
			world = new WorldCreator("Bingo_" + nazwa)
					.generatorSettings("{\"biome\"\\:\"minecraft\\:plains\"}")
					.environment(Environment.NORMAL)
					.generateStructures(false)
					.type(WorldType.NORMAL)
					.hardcore(true)
					.createWorld();
			
			WorldBorder border = world.getWorldBorder();
			border.setCenter(0, 0);
			border.setSize(1500);
			border.setSize(10, 60 * 60);
			border.setDamageAmount(2d);
			
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			world.setGameRule(GameRule.RANDOM_TICK_SPEED, 20);
			world.setGameRule(GameRule.DO_FIRE_TICK, false);
			world.setGameRule(GameRule.SPAWN_RADIUS, 0);
			world.setDifficulty(Difficulty.HARD);
			world.setKeepSpawnInMemory(false);
			world.setAutoSave(false);
			world.setHardcore(true);
			world.setPVP(true);
			
			super.start();
			
			Supplier<Integer> los = () -> Func.losuj(-500, 500);
			gracze.forEach(p -> {
				p.teleport(new Location(world, los.get(), 200, los.get()));
				znalezione.put(p.getName(), new HashSet<>());
				p.getInventory().setItemInOffHand(itemMapy);
				p.addPotionEffect(startowyEfekt);
				p.setGameMode(GameMode.SURVIVAL);
			});
			Func.opóznij(10, () -> gracze.forEach(p -> NMS.wyślij(p, packet)));
			
			napiszGraczom("Potrzebne Itemy: %s", Func.wykonajWszystkim(materiały, Func::enumToString));
			napiszGraczom("Arena wystartowała, znajdzcie wszystkie potrzebne itemy, albo zabijcie wszystkich przeciwników aby zwyciężyć!");
		}
		private List<BufferedImage> losujItemy() {
			List<BufferedImage> wynik = new ArrayList<>();
			File[] ikony = new File(Main.path + folderItemów).listFiles();

			Consumer<File> cons = ikona -> {
				Material mat;
				try {
					mat = Func.StringToEnum(Material.class, ikona.getName().substring(0, ikona.getName().lastIndexOf('.')));
				} catch (Throwable e) {
					Main.warn("Niepoprawny item w plikach bingo: " + ikona.getName());
					return;
				}
				
				if (materiały.contains(mat))
					return;
				
				try {
					wynik.add(ImageIO.read(ikona));
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				materiały.add(mat);
			};
			
			int limit = 250;
			
			while (limit-- > 0) {
				File ikona = Func.losuj(ikony);
				
				cons.accept(ikona);
				
				if (materiały.size() >= 25)
					return wynik;
			}
			
			Func.forEach(ikony, ikona -> {
				if (materiały.size() < 25)
					cons.accept(ikona);
			});
			
			if (materiały.size() == wynik.size() && materiały.size() == 25)
				return wynik;
			
			throw new IllegalArgumentException("Nie odnaleziono wystarczająco ikon bingo aby rozpocząć grę");
		}
		
		@Override
		public void koniec() {
			super.koniec();
			Bukkit.unloadWorld(world, false);
			Func.usuń(world.getWorldFolder());

			materiały.clear();
			znalezione.clear();
			world = null;
			mapa = null;
		}
		

		public void podniósł(Player p, Material mat) {
			if (materiały.contains(mat) && znalezione.get(p.getName()).add(mat)) {
				napiszGraczom("%s znalazł %s! (%s/%s)", p.getDisplayName(), Func.enumToString(mat), znalezione.size(), materiały.size());
				wyślijMape(p);
				
				Func.wykonajDlaNieNull(inst.staty(p), stat -> stat.znalezioneItemy++);

				if (znalezione.size() == materiały.size())
					wygrana(p);
			}
		}
		private void wyślijMape(Player p) {
			byte[] mapa = new byte[this.mapa.length];
			for (int i=0; i < mapa.length; i++)
				mapa[i] = this.mapa[i];
			
			Set<Material> znalezione = this.znalezione.get(p.getName());
			for (int i=0; i < materiały.size(); i++) {
				Material mat = materiały.get(i);
				if (znalezione.contains(mat)) {
					int y = 18 + 19 * (i / 5) + 10;
					int x = 28 + 19 * (i % 5) - 10;
					
					CustomoweMapy.umieść(skreślenie, mapa, x, y);
				}
			}
			
			NMS.wyślij(p, CustomoweMapy.packet(idMapy, mapa));
		}
		
		public byte[] losujMape(List<BufferedImage> ikony) {
			byte[] mapa = new byte[128 * 128];
			
			if (ikony.size() != 25)
				throw new IllegalArgumentException("Nieodpowiednia ilość ikon: " + ikony.size());
			
			try {
				mapa = CustomoweMapy.wczytaj(customowaMapa);
			} catch (Throwable e) {
				if (Main.włączonyModół(CustomoweMapy.class))
					Main.warn(inst.getPrefix() + nazwa + " Nieodnaleziono customowej mapy " + customowaMapa + " ");
				for (int i=0; i < mapa.length; i++)
					mapa[i] = (byte) (45 * 4 + 1);
			}
			
			int m1 = 3;
			int m2 = 18;
			Iterator<BufferedImage> it = ikony.iterator();
			for (int y = m2 + 10; y < 128 - m2 + 10; y += 16 + m1)
				for (int x = m2; x < 128 - m2; x += 16 + m1)
					CustomoweMapy.umieść(it.next(), mapa, x, y);
			
			for (int x = m2 - 2; x <= 128 - m2 + 2; x += 16 + m1)
				for (int y = m2 - 2 + 10; y < 128 - m2 + 10 + 1; y++)
					mapa[y * 128 + x] = (byte) (45 * 4 + 0);
			for (int y = m2 - 2 + 10; y <= 128 - m2 + 2 + 10; y += 16 + m1)
				for (int x = m2 - 2; x < 128 - m2 + 1; x++)
					mapa[y * 128 + x] = (byte) (45 * 4 + 0);
			
			return mapa;
		}
		
		
		@Override
		boolean poprawna() {
			File f = new File(Main.path + folderItemów);
			
			if (!f.exists()) {
				Main.warn("Nieodnaleziono katalogu z itemami bingo dla areny " + nazwa);
				return false;
			}
			
			Func.forEach(f.listFiles(), file -> {
				try {
					Func.StringToEnum(Material.class, file.getName().substring(0, file.getName().lastIndexOf('.')));
				} catch (Throwable e) {
					Main.warn("Niepoprawna ikona " + file.getName() + " dla areny bingo " + nazwa);
				}
				
			});
			
			return super.poprawna();
		}
		
		Bingo inst;
		@Override Minigra getInstMinigra() { return inst; }
		@Override <M extends Minigra> void setInst(M inst) { this.inst = (Bingo) inst; }
		@Override
		int policzGotowych() {
			return gracze.size();
		}
	}
	public static class Statystyki extends Minigra.Statystyki {
		@Mapowane int znalezioneItemy;
		
		@Override
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor, Minigra minigra) {
			super.rozpiska(cons, usuwaćKolor, minigra);

			int punkty = policzPunkty(minigra);
			cons.accept(_rozpiska("Znalezione Itemy", znalezioneItemy));
			cons.accept(_rozpiska("Punkty", punkty));
			użyjRangi(ranga(punkty, minigra), usuwaćKolor, cons);
		}
	}
	
	@Override
	public void przeładuj() {
		super.przeładuj();
		try {
			skreślenie = ImageIO.read(new File(Main.path + "skreślenie.png"));
		} catch (IOException e) {
			Main.warn("Brak pliku " + Main.path + "skreślenie.png, powinien mieć wymiary 16x16");
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void podnoszenie(EntityPickupItemEvent ev) {
		if (!ev.isCancelled()) 
			Func.wykonajDlaNieNull(arena(ev.getEntity()), arena -> arena.podniósł((Player) ev.getEntity(), ev.getItem().getItemStack().getType()));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void craftowanie(CraftItemEvent ev) {
		if (!ev.isCancelled()) 
			Func.wykonajDlaNieNull(arena(ev.getWhoClicked()), arena -> arena.podniósł((Player) ev.getWhoClicked(), ev.getCurrentItem().getType()));
	}
	@EventHandler
	public void dropnienie(PlayerDropItemEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getPlayer()), arena -> {
			if (ev.getItemDrop().getItemStack().getType() == Material.FILLED_MAP) {
				Func.powiadom(getPrefix(), ev.getPlayer(), "Nie wyrzucaj, to ci się jeszcze może przydać");
				ev.setCancelled(true);
			}
		});
	}
	@EventHandler
	public void spadanie(EntityDamageEvent ev) {
		if (ev.getCause() == DamageCause.FALL)
			Func.wykonajDlaNieNull(arena(ev.getEntity()), arena -> {
				if (System.currentTimeMillis() - arena.startAreny <= 10_000)
					ev.setCancelled(true);
			});
	}
	@Override
	@EventHandler
	public void śmierć(PlayerDeathEvent ev) {
		super.śmierć(ev);
		Func.wykonajDlaNieNull(arena(ev.getEntity()), arena -> arena.opuść(ev.getEntity()));
	}
	
	@Override @SuppressWarnings("unchecked") Arena 		arena(Entity p) { return super.arena(p); }
	@Override @SuppressWarnings("unchecked") Statystyki staty(Entity p) { return super.staty(p); }
	
	@Override String getMetaStatystyki() { return "mimiMinigraBingoStatystyki"; }
	@Override String getMetaId() 		 { return "mimiMinigraBingo"; }
	@Override Supplier<? extends Statystyki> noweStaty() { return Statystyki::new; }
}
