package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class Bazy implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Baza");
	RegionContainer regiony;
	Config config = new Config("Bazy");
	Config configDane = new Config("configi/Bazy Dane");
	
	static Bazy inst;
	public Bazy() {
		inst = this;
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public String raport() {
		return "§6Itemy dla Baz/schematów/C4: §e" + ((List<?>) config.wczytaj("bazy")).size();
	}
	
	
	@EventHandler
	public void explozja(ExplosionPrimeEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains("mimiC4")) {
			ev.setCancelled(true);
			
			ConfigurationSection mapa = config.sekcja("c4");
			if (mapa == null)
				return;
			
			List<String> niezniszczalne = config.wczytajListe("c4.niezniszczalne");
			Consumer<Block> zniszcz = blok -> {
				String mat = blok.getType().toString();
				Object obj = mapa.get(mat);
				if (obj != null)
					blok.setType(Material.valueOf((String) obj));
				else if (!niezniszczalne.contains(mat))
					blok.setType(Material.AIR);
			};
			
			Location loc = ev.getEntity().getLocation();
			Location _loc = loc.clone();
			RegionManager regiony = Bazy.inst.regiony.get(BukkitAdapter.adapt(loc.getWorld()));
			float zasięg = ev.getRadius();
			int mx = (int) (zasięg*2+1);
			loc.add(-zasięg, -zasięg, -zasięg);
			for (int y=0; y<mx; y++) {
				for (int z=0; z<mx; z++) {
					for (int x=0; x<mx; x++) {
						double dystans = _loc.distance(loc);
						if (dystans <= zasięg) {
							int szansa = dystans < zasięg/3*1 ? 90 : (dystans < zasięg/3*2 ? 60 : 30);
							if (Func.losuj(1, 100) <= szansa &&
									regiony.getApplicableRegions(
											BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
											.testState(null, Main.flagaC4))
								zniszcz.accept(loc.getBlock());
						}
						loc.add(1, 0, 0);
					}
					loc.add(-mx, 0, 1);
				}
				loc.add(0, 1, -mx);
			}
		}
	}
	
	boolean blokuj = false;
	@SuppressWarnings("unchecked")
	@EventHandler
	public void stawianie(BlockPlaceEvent ev) {
		World świat = ev.getBlock().getWorld();
		ItemStack item = ev.getPlayer().getEquipment().getItemInMainHand();
		int x = ev.getBlock().getX();
		int y = ev.getBlock().getY();
		int z = ev.getBlock().getZ();
		
		Consumer<Map<String, Object>> wejście = mapa -> {
			ev.setCancelled(true);
			
			Supplier<Integer> zabierzItem = () -> {
				item.setAmount(item.getAmount()-1);
				ev.getPlayer().getEquipment().setItemInMainHand(item);
				return 0;
			};
			
			// C4
			Map<String, Object> mapaC4 = (Map<String, Object>) mapa.get("c4");
			if (mapaC4 != null) {
				
				float zasięg = (float) (double) mapaC4.getOrDefault("zasięg", 1f);
				int czas   	 = (int)   			mapaC4.getOrDefault("czas",   1);
				Location loc = ev.getBlock().getLocation().add(.5, 0, .5);
				
				TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
				tnt.addScoreboardTag("mimiC4");
				tnt.setFuseTicks(czas);
				tnt.setGravity(false);
				tnt.setYield(zasięg);
				tnt.setVelocity(new Vector());
				try { 
					tnt.setCustomName(ev.getItemInHand().getItemMeta().getDisplayName());
				} catch (Exception e) {}
				
				zabierzItem.get();
				return;
			}
			
			// Baza/Schemat
			boolean zabierz = Baza.wczytaj(x, y, z, świat, item, ev, 
					(Map<String, Object>) mapa.get("baza")) != null;
			
			
			if (mapa.containsKey("schemat") && !blokuj && 
					Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
						.getApplicableRegions(BlockVector3.at(x, y, z))
						.testState(Main.rg.wrapPlayer(ev.getPlayer()), Flags.BUILD) &&
					wklejSchemat((String) mapa.get("schemat"), świat, x, y, z))
						zabierz = true;
			
			blokuj = false;
			
			if (zabierz) 
				zabierzItem.get();
		};
		if (config.klucze(false).contains("bazy"))
			for (Map<String, Object> mapa : (List<Map<String, Object>>) config.wczytaj("bazy"))
				if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
					wejście.accept(mapa);
					return;
				}
	}
	boolean wklejSchemat(String schematScieżka, World świat, int x, int y, int z) {
		String scieżka = Main.path + schematScieżka;
		File file = new File(scieżka);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file));
				EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
						.getEditSession(BukkitAdapter.adapt(świat), -1)) {
			Operations.complete(
					new ClipboardHolder(reader.read())
		            .createPaste(editSession)
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(true)
		            .build()
		            );
		} catch (IOException  e) {
			Main.warn("Nie odnaleziono pliku " + scieżka + " schemat z Bazy.yml nie został wybudowany.");
			return false;
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		return true;
	}
}

class Baza {
	Player p;
	Baza(int x, int y, int z, int dx, int dy, int dz, World świat, Player właściciel) {
		p = właściciel;
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(
				String.format("Bazax%sy%sz%s%s", x, y, z, p.getName()),
				BlockVector3.at(x+dx, y+dy, z+dz),
				BlockVector3.at(x-dx, y-1, z-dz)
				);
		Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(p.getName());
		region.setOwners(owners);
		region.setPriority(Bazy.inst.config.wczytajInt("ustawienia", "prority baz"));
		region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
		region.setFlag(Main.flagaC4, 		   StateFlag.State.ALLOW);
	}
	
	static Baza wczytaj(int x, int y, int z, World świat, ItemStack item, BlockPlaceEvent ev, Map<String, Object> mapa) {
		if (mapa == null) return null;
		int dx = (int) mapa.get("dx");
		int dy = (int) mapa.get("dy");
		int dz = (int) mapa.get("dz");
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(
				"mimiBazaTestowana",
				BlockVector3.at(x+dx, y+dy, z+dz),
				BlockVector3.at(x-dx, y-1,  z-dz)
				);
		if (Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
				.getApplicableRegions(region).testState(null, Main.flagaStawianieBaz))
			return new Baza(x, y, z, dx, dy, dz, świat, ev.getPlayer());
		Bazy.inst.blokuj = true;
		ev.getPlayer().sendMessage(Bazy.prefix + "Nie możesz tu postawić swojej bazy");
		return null;
	}
	
}



