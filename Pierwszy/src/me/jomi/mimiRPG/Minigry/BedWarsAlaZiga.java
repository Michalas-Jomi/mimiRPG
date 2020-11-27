package me.jomi.mimiRPG.Minigry;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

@Moduł
public class BedWarsAlaZiga extends MinigraDrużynowa {
	public static final String prefix = Func.prefix("Bedwars ala Ziga");
	public static class Arena extends MinigraDrużynowa.Arena {
		@Mapowane List<Drużyna> drużyny;
		@Mapowane List<Ruda> rudy;
		
		Set<Integer> doAnulowania = Sets.newConcurrentHashSet();
		
		@Override
		void koniec() {
			for (Integer id : doAnulowania)
				Bukkit.getScheduler().cancelTask(id);
			doAnulowania.clear();
			super.koniec();
		}
		
		
		BedWarsAlaZiga inst;
		@Override MinigraDrużynowa getInstMinigraDrużynowa() { return inst; }
		@Override <M extends Minigra> void setInst(M inst)	 { this.inst = (BedWarsAlaZiga) inst; }

		@Override List<? extends Drużyna> getDrużyny() { return drużyny; }
		@Override int getMinDrużyny() 				   { return 2; }

		@Override Supplier<? extends me.jomi.mimiRPG.Minigry.Minigra.Statystyki> noweStaty() { return Statystyki::new; }
	}
	public static class Drużyna extends MinigraDrużynowa.Drużyna {
		
	}
	public static class Ruda extends Mapowany {
		@Mapowane Material blok = Material.IRON_ORE;
		@Mapowane int sekundyOdrespiania = 10;
	}
	public static class Statystyki extends Minigra.Statystyki {

		@Override
		void sprawdzTopke(Player p) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void niszczenie(BlockBreakEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getPlayer()), arena -> {
			for (Ruda ruda : arena.rudy)
				if (ruda.blok.equals(ev.getBlock().getType())) {
					Krotka<Integer, Material> k = new Krotka<>(0, ev.getBlock().getType());
					arena.doAnulowania.add(k.a = Func.opóznij(ruda.sekundyOdrespiania, () -> {
						arena.doAnulowania.remove(k.a);
						ev.getBlock().setType(k.b);
					}));
					break;
				}
		});
	}
	
	
	
	
	// Override

	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Drużyna 	drużyna (Entity p) { return super.drużyna(p); }
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }
	

	private final Config configAreny = new Config("configi/BedWarsAlaZiga Areny");
	@Override Config getConfigAreny()	 { return configAreny; }

	@Override String getMetaStatystyki() { return "mimiBedWarsAlaZigaStaty"; }
	@Override String getMetaDrużynaId()	 { return "mimiBedWarsAlaZigaDrużyna"; }
	@Override String getMetaId()		 { return "mimiMinigraBedWarsAlaZiga"; }

	@Override String getPrefix() { return prefix; }
}
