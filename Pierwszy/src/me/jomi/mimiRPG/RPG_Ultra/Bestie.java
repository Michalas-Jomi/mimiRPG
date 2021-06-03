package me.jomi.mimiRPG.RPG_Ultra;

import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Bestie implements Listener {
	public static final String tagBesti = "mimiBestia";
	public static final String metaBesti = "mimiBestia";
	
	public static class DropRPG {
		public final ItemStack item;
		public final double szansa;
		public final int min_ilość;
		public final int max_ilość;

		public DropRPG(ItemStack item, double szansa, int min_ilość, int max_ilość) {
			this.max_ilość = max_ilość;
			this.min_ilość = min_ilość;
			this.szansa = szansa;
			this.item = item;
		}
		
		public ItemStack dropnij() {
			if (Func.losuj(szansa))
				return Func.ilość(item.clone(), Func.losuj(min_ilość, max_ilość));
			return null;
		}
		public void dropnij(List<ItemStack> dropy) {
			Func.wykonajDlaNieNull(dropnij(), dropy::add);
		}
	}
	public static class Bestia {
		public final List<DropRPG> dropy;
		public final int exp_łowcy;
		public final int exp;
		public final double kasa;
		public final String nazwa;		 // np. Miner Zombie lvl 42
		public final String grupa;		// np. Miner Zombie
		public final String kategoria; // np. Jaskiniowe
		
		Bestia(List<DropRPG> dropy, double kasa, int exp, int exp_łowcy, String kategoria, String grupa, String nazwa) {
			this.dropy = Func.nieNull(dropy);
			this.exp_łowcy = exp_łowcy;
			this.kasa = kasa;
			this.exp = exp;
			
			this.nazwa		= Objects.requireNonNull(nazwa);
			this.grupa		= grupa 	== null ? nazwa : grupa;
			this.kategoria	= kategoria == null ? grupa : kategoria;
		}
		
		public void ustanówBestią(Entity mob) {
			Func.ustawMetadate(mob, metaBesti, this);
			mob.addScoreboardTag(tagBesti);
		}
	}
	
	
	public Bestia bestia(Entity mob) {
		if (!mob.hasMetadata(metaBesti))
			return null;
		else
			return (Bestia) mob.getMetadata(metaBesti).get(0).value();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void chunkLoad(ChunkLoadEvent ev) {
		Func.forEach(ev.getChunk().getEntities(), e -> {
			if (e.getScoreboardTags().contains(tagBesti) && !e.hasMetadata(metaBesti))
				e.remove();
		});
	}
	@EventHandler(priority = EventPriority.LOW)
	public void śmierćMoba(EntityDeathEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (ev.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev2 = (EntityDamageByEntityEvent) ev.getEntity().getLastDamageCause();
				Func.wykonajDlaNieNull(bestia(ev2.getDamager()), bestia -> {
					GraczRPG gracz = GraczRPG.gracz((Player) ev.getEntity());
					String klucz = new StringBuilder()
							.append("bestia_zgon_")
							.append(bestia.kategoria).append('_')
							.append(bestia.grupa).append('_')
							.append(bestia.nazwa).toString();
					gracz.zwiększStatystykę(klucz);
				});
			}
			return;
		}
		
		
		ev.getDrops().clear();
		Func.wykonajDlaNieNull(bestia(ev.getEntity()), bestia -> {
			bestia.dropy.forEach(drop -> drop.dropnij(ev.getDrops()));
			ev.setDroppedExp(bestia.exp);
			
			Func.wykonajDlaNieNull(ev.getEntity().getKiller(), killer -> {
				GraczRPG gracz = GraczRPG.gracz(killer);
				gracz.ścieżka_łowca.zwiększExp(bestia.exp_łowcy);
				gracz.dodajKase(bestia.kasa);

				String klucz = new StringBuilder()
						.append("bestia_kill_")
						.append(bestia.kategoria).append('_')
						.append(bestia.grupa).append('_')
						.append(bestia.nazwa).toString();
				gracz.zwiększStatystykę(klucz);
			});
		}, () -> ev.setDroppedExp(0));
	}
}
