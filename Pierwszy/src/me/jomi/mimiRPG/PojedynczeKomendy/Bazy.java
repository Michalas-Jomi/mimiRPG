package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
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
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class Bazy implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Baza");
	RegionContainer regiony;
	Config config = new Config("Bazy");
	
	static Bazy inst;
	public Bazy() {
		inst = this;
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	public static boolean warunekModułu() {
		return Main.worldGuard;
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public String raport() {
		return "§6Itemy dla Baz/schematów: §e" + ((List<?>) config.wczytaj("bazy")).size();
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
		try {
			for (Map<String, ?> mapa : (List<Map<String, ?>>) config.wczytaj("bazy"))
				if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
					ev.setCancelled(true);
					
					boolean zabierz = Baza.wczytaj(x, y, z, świat, item, ev, 
							(Map<String, Object>) mapa.get("baza")) != null;
					
					if (mapa.containsKey("schemat") && !blokuj &&
							wklejSchemat((String) mapa.get("schemat"), świat, x, y, z))
						zabierz = true;
					
					blokuj = false;
					
					if (zabierz) {
						item.setAmount(item.getAmount()-1);
						ev.getPlayer().getEquipment().setItemInMainHand(item);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
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
			return false;
		}
		return true;
	}
}

class Baza {
	Player p;
	Baza(int x, int y, int z, int dx, int dy, int dz, String nazwa, World świat, Player właściciel) {
		p = właściciel;
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(
				String.format("%sx%sy%sz%s%s", nazwa, x, y, z, p.getName()),
				BlockVector3.at(x+dx, y+dy, z+dz), BlockVector3.at(x-dx, y-1, z-dz));
		Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(p.getName());
		region.setOwners(owners);
		region.setPriority(Bazy.inst.config.wczytajInt("ustawienia", "prority baz"));
		region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
		region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
		region.setFlag(Flags.BUILD, StateFlag.State.DENY);
	}
	
	static Baza wczytaj(int x, int y, int z, World świat, ItemStack item,
									BlockPlaceEvent ev, Map<String, Object> mapa) {
		if (mapa == null) return null;
		int dx = (int) mapa.get("dx");
		int dy = (int) mapa.get("dy");
		int dz = (int) mapa.get("dz");
		Collection<ProtectedRegion> regiony=Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).getRegions().values();
		if (	
				sprawdzRóg(świat, regiony, x+dx, y+dy, z+dz) &&
				sprawdzRóg(świat, regiony, x-dx, y+dy, z+dz) &&
				sprawdzRóg(świat, regiony, x+dx, y-1,  z+dz) &&
				sprawdzRóg(świat, regiony, x+dx, y+dy, z-dz) &&
				sprawdzRóg(świat, regiony, x-dx, y-1,  z+dz) &&
				sprawdzRóg(świat, regiony, x-dx, y+dy, z-dz) &&
				sprawdzRóg(świat, regiony, x+dx, y-1,  z-dz) &&
				sprawdzRóg(świat, regiony, x-dx, y-1,  z-dz)
				)
			return new Baza(x, y, z, dx, dy, dz, 
					(String) mapa.getOrDefault("nazwa", "Baza"), świat, ev.getPlayer());
		Bazy.inst.blokuj = true;
		ev.getPlayer().sendMessage(Bazy.prefix + "Nie możesz tu postawić swojej bazy");
		return null;
	}
	static boolean sprawdzRóg(World świat, Collection<ProtectedRegion> regiony, int x, int y, int z) {
		List<ProtectedRegion> lista = Lists.newArrayList();
		for (ProtectedRegion region : regiony)
			if (region.contains(BlockVector3.at(x, y, z)))
				lista.add(region);
		return new RegionResultSet(lista, null).testState(null, Main.flagaStawianieBaz);
	}
}



