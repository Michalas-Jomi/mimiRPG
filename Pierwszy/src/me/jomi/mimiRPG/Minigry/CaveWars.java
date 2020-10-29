package me.jomi.mimiRPG.Minigry;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.LosyProporcjonalne;

@Moduł
public class CaveWars extends MinigraDrużynowa {
	public static class Box extends Mapowany {
		@Mapowane Material blok;
		@Mapowane int szansa;
	}
	public static class Arena extends MinigraDrużynowa.Arena {
		LosyProporcjonalne<Material> blokiAreny;
		@Mapowane List<Drużyna> drużyny;
		@Mapowane List<Box> BlokiAreny;
		@Mapowane Location róg1;
		@Mapowane Location róg2;
		
		void skończoneGenerowanie() {
			if (startuje) {
				_start();
				for (Player p : gracze)
					p.resetTitle();
			}
		}
		boolean startuje = false;
		@Override
		void start() {
			super.start();
			
			startuje = true;
			if (wygenerowana())
				_start();
			else
				for (Player p : gracze)
					p.sendTitle("§aGenerowanie areny", "§2Za moment zostaniecie przeteleportowani", 20,  2400, 200);
		}
		void _start() {
			startuje = false;
			for (Drużyna drużyna : drużyny) {
				if (drużyna.gracze <= 0) continue;
				int x = Func.losuj(róg1.getBlockX() + 2, róg2.getBlockX() - 1);
				int y = Func.losuj(róg1.getBlockY() + 0, róg2.getBlockY() - 3);
				int z = Func.losuj(róg1.getBlockZ() + 2, róg2.getBlockZ() - 0);
				drużyna.respawn = new Location(róg1.getWorld(), x, y, z);
				
				for (Block blok : Func.bloki(drużyna.respawn.clone().add(-2, 0, -2), drużyna.respawn.clone().add(1, 3, 0)))
					blok.setType(Material.AIR, false);
				
				drużyna.respawn.getBlock().setType(Material.TORCH);
			}
			
			for (Player p : gracze) {
				p.teleport(inst.drużyna(p).respawn);
				p.setGameMode(GameMode.SURVIVAL);
				p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20*60*60*6, 1));
			}

			zapiszWygenerowanieJako(false);			
		}
		
		void zabity(Player kto, Player kiler) {
			inst.staty(kto).śmierci++;
			Func.wykonajDlaNieNull(kiler, p -> inst.staty(p).kille++);
			
			if (kiler != null)
				napiszGraczom("%s został zabity przez %s", inst.nick(kto), inst.nick(kiler));
			else
				napiszGraczom("%s umarł", inst.nick(kto));
			
			opuść(kto);
		}
				
		@Override
		<D extends MinigraDrużynowa.Drużyna> void ubierz(Player p, D drużyna) {
			ubierz(p, drużyna, false, false, false, true);
		}
		
		public void Init() {
			Function<Function<Location, Integer>, Krotka<Integer, Integer>> krotka = 
					func -> Func.minMax(func.apply(róg1), func.apply(róg2), Math::min, Math::max);
			Krotka<Integer, Integer> kx = krotka.apply(Location::getBlockX);
			Krotka<Integer, Integer> ky = krotka.apply(Location::getBlockY);
			Krotka<Integer, Integer> kz = krotka.apply(Location::getBlockZ);
			róg1 = new Location(róg1.getWorld(), kx.a, ky.a, kz.a);
			róg2 = new Location(róg2.getWorld(), kx.b, ky.b, kz.b);
			
			List<Krotka<Integer, Material>> lista = Lists.newArrayList();
			for (Box box : BlokiAreny)
				lista.add(new Krotka<>(box.szansa, box.blok));
			blokiAreny = new LosyProporcjonalne<>(lista);
		}
		
		void wygenerujArene(Runnable runnable) {
			wygenerujArene(Func.blokiIterator(róg1, róg2), runnable);
		}
		private void wygenerujArene(Iterator<Block> iterator, Runnable runnable) {
			int licz = 0;
			int mx = Main.ust.wczytajLubDomyślna("Minigry.Budowanie Aren.Max Bloki", 50_000);
			while (iterator.hasNext()) {
				Block blok = iterator.next();
				blok.setType(blokiAreny.losuj(), false);
				
				BlockData data = blok.getBlockData();
				if (data instanceof Leaves) {
					((Leaves) data).setPersistent(true);
					blok.setBlockData(data, false);
				}
				
				if (++licz > mx) {
					Func.opóznij(Main.ust.wczytajLubDomyślna("Minigry.Budowanie Aren.Ticki Przerw", 1),
							() -> wygenerujArene(iterator, runnable));
					return;
				}
			}
			
			zapiszWygenerowanieJako(true);
			
			if (runnable != null)
				runnable.run();
		}
		private boolean wygenerowana() {
			Set<String> set = configDane.wczytajLubDomyślna("CaveWars.Wygenerowane", Sets::newConcurrentHashSet);
			return set.contains(nazwa);
		}
		private void zapiszWygenerowanieJako(boolean wygenerowana) {
			Set<String> set = configDane.wczytajLubDomyślna("CaveWars.Wygenerowane", Sets::newConcurrentHashSet);
			if (wygenerowana)
				set.add(nazwa);
			else
				set.remove(nazwa);
			configDane.ustaw_zapisz("CaveWars.Wygenerowane", set);
			
		}
		
		@Override List<Drużyna> getDrużyny() 	   { return drużyny; }
		@Override Supplier<Statystyki> noweStaty() { return Statystyki::new; }

		CaveWars inst;
		@Override MinigraDrużynowa getInstMinigraDrużynowa() { return inst; }
		@Override <M extends Minigra> void setInst(M inst)	 {this.inst = (CaveWars) inst; }
		@Override int getMinDrużyny() { return 2; }
	}
	public static class Drużyna extends MinigraDrużynowa.Drużyna {
		Location respawn;
	}
	public static class Statystyki extends Minigra.Statystyki {
		@Mapowane int rozkopaneBloki;
		@Mapowane int śmierci;
		@Mapowane int kille;
		
		@Override
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor) {
			super.rozpiska(cons, usuwaćKolor);

			cons.accept(_rozpiska("kill ratio", _rozpiska(kille, kille + śmierci)));
			cons.accept(_rozpiska("Kille", kille));
			cons.accept(_rozpiska("Zgony", śmierci));
		}

		@Override void sprawdzTopke(Player p) {}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void zabicie(PlayerDeathEvent ev) {
		Player p = ev.getEntity();
		Func.wykonajDlaNieNull(arena(p), a -> a.zabity(p, p.getKiller()));
	}
	@EventHandler(priority = EventPriority.LOW)
	public void uderzenie(EntityDamageByEntityEvent ev) {
		Predicate<Entity> niegrana = e -> {
			Arena a = arena(e);
			if (a == null)
				return true;
			if (a.grane)
				return false;
			ev.setCancelled(true);
			return true;
		};
		if (niegrana.test(ev.getEntity()))	return;
		if (niegrana.test(ev.getDamager()))	return;

		Drużyna d1 = drużyna((Player) ev.getEntity());
		Drużyna d2 = drużyna((Player) ev.getDamager());
		if (!d1.equals(d2)) return;
		ev.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void kopanie(BlockBreakEvent ev) {
		Player p = ev.getPlayer();
		Arena arena = arena(p);
		if (arena != null && arena.grane)
			staty(p).rozkopaneBloki++;
	}

	@Override
	Minigra.Arena zaczynanaArena() {
		if (zaczynanaArena != null) 
			return zaczynanaArena;
		
		Arena arena = (Arena) super.zaczynanaArena();
		if (arena == null) 
			return null;
		
		if (!arena.wygenerowana())
			arena.wygenerujArene(arena::skończoneGenerowanie);
		
		return arena;
	}
	
	
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }
	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Drużyna 	drużyna (Entity p) { return super.drużyna(p); }

	final Config configAreny = new Config("configi/minigry/CaveWarsAreny");
	@Override Config getConfigAreny() 	 { return configAreny; }

	@Override String getMetaId() 		 { return "mimiMinigraCaveWarsArena"; }
	@Override String getMetaDrużynaId()  { return "mimiMinigraCaveWarsDrużyna"; }
	@Override String getMetaStatystyki() { return "mimiMinigraCaveWarsStatystyki"; }
	final String prefix = Func.prefix("CaveWars");
	@Override String getPrefix() { return prefix; }
}
