package me.jomi.mimiRPG.MineZ;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class RailGuny extends AbstractKarabiny<RailGuny.RailGun> {
	public static final String prefix = Func.prefix(RailGuny.class);
	
	public static class RailGun extends AbstractKarabiny.Karabin {
		@Mapowane int zasięg;
		
		@Override
		protected void strzel(Player p, Vector wzrok) {
			RayTraceResult rezult = p.getWorld().rayTrace(p.getEyeLocation(), wzrok, zasięg, FluidCollisionMode.NEVER, true, 0, e -> e instanceof LivingEntity && ! p.getUniqueId().equals(e.getUniqueId()));
			
			
			if (rezult != null) {
				Func.wykonajDlaNieNull(rezult.getHitEntity(), e -> {
					LivingEntity mob = (LivingEntity) e;
					mob.setHealth(mob.getHealth() - dmgPrzeszycie);
					mob.damage(dmg, p);
				});
				
				Location loc = rezult.getHitPosition().toLocation(p.getWorld());
				
				if (zasięgC4 > 0 && Main.włączonyModół(Bazy.class))
					Bazy.detonateC4(loc, (float) zasięgC4);

				if (mocWybuchu > 0)
					p.getWorld().createExplosion(loc, (float) mocWybuchu, false, false, p);
				
				if (particleWybuchu != null)
					particleWybuchu.zresp(loc);
				
			}
			
			if (kolorOgonaPocisku != null) {
				Location start = p.getEyeLocation();
				Location loc = start.clone();
				double maxDystans = rezult == null ? zasięg : start.distance(rezult.getHitPosition().toLocation(start.getWorld()));
				while (true) {
					loc.add(wzrok);
					if (loc.distance(start) > maxDystans)
						break;
					Func.particle(loc, 1, 0, 0, 0, 0, kolorOgonaPocisku.kolor(), .5f);
				}
			}
		}
	}
	
	public RailGuny() {
		super("edytujrailgun", "configi/RailGuny", RailGun.class);
	}
}
