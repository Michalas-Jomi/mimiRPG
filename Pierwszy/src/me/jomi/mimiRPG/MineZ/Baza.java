package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Gracze.Gracz;

public class Baza {
	ProtectedCuboidRegion region;
	World świat;
	
	Baza(int x, int y, int z, int dx, int dy, int dz, World świat, Player właściciel) {
		Player p = właściciel;
		this.świat = świat;
				
		String nazwaBazy = String.format("baza%sx%sy%sz", x, y, z);
		region = new ProtectedCuboidRegion(
				nazwaBazy,
				BlockVector3.at(x+dx, y+dy, z+dz),
				BlockVector3.at(x-dx, y-1, z-dz)
				);
		Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(p.getName());
		region.setOwners(owners);
		region.setPriority(Bazy.config.wczytajInt("ustawienia.prority baz"));
		region.setFlag(Main.flagaCustomoweMoby, "brak");
		region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
		region.setFlag(Main.flagaC4, 		   StateFlag.State.ALLOW);
		
		Gracz g = Gracz.wczytaj(p.getName());
		Func.wezUstaw(g.bazy, świat.getName()).add(nazwaBazy);
		g.zapisz();
		
		// ognisko to rdzeń bazy, zniszczenie ogniska = usunięcie bazy
		Func.opóznij(1, () -> new Location(świat, x, y, z).getBlock().setType(Material.CAMPFIRE));
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

	Baza(World świat, String nazwa) {
		region = (ProtectedCuboidRegion) Bazy.inst.regiony(świat).getRegion(nazwa);
		this.świat = świat;
	}
	private Baza(World świat, ProtectedCuboidRegion region) {
		this.region = region;
		this.świat = świat;
	}
	static Baza wczytaj(World świat, ProtectedRegion region) {
		if (świat == null) return null;
		if (!(region instanceof ProtectedCuboidRegion)) return null;
		Pattern patern = Pattern.compile("baza-?\\d+x-?\\d+y-?\\d+z");
		if (patern.matcher(region.getId()).find())
			return new Baza(świat, (ProtectedCuboidRegion) region);
		return null;
	}
	
	void usuń() {
		for (String owner : region.getOwners().getPlayers()) {
			Gracz g = Gracz.wczytaj(owner);
			List<String> bazy = g.bazy.get(świat.getName());
			if (bazy == null) continue;
			if (bazy.remove(region.getId()) && bazy.size() == 0)
				g.bazy.remove(świat.getName());
		}
		Bazy.inst.regiony(świat).removeRegion(region.getId());
	}

	// TODO ceny surowkami
	void ulepsz(int ile) {
		ulepsz(region::getMaximumPoint, region::setMaximumPoint, ile, ile);
		ulepsz(region::getMinimumPoint, region::setMinimumPoint, -ile, 0);
	}
	private void ulepsz(Supplier<BlockVector3> supplier, Consumer<BlockVector3> consumer, int xz, int y) {	
		consumer.accept(supplier.get().add(xz, y, xz));
	}
}
