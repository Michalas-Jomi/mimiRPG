package me.jomi.mimiRPG.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import me.jomi.mimiRPG.Mapowany;

public class ParticleZwykłe extends Mapowany {
	@Mapowane Particle particle;
	@Mapowane int ilość = 20;
	@Mapowane double dx = 1;
	@Mapowane double dy = 1;
	@Mapowane double dz = 1;
	@Mapowane double prędkość = 1;
	@Mapowane int ilośćOdnowień = 1;
	@Mapowane int tickiMiędzyOdnowieniami;
	
	@Mapowane double dmg = 0;
	@Mapowane int tickiPalenia = 0;
	
	
	public void zresp(Location loc) {
		zresp(loc, ilośćOdnowień);
	}
	private void zresp(Location loc, int ilość) {
		if (ilość <= 0) return;
		
		Func.particle(particle, loc, this.ilość, dx, dy, dz, prędkość);
		Func.opóznij(tickiMiędzyOdnowieniami, () -> zresp(loc, ilość - 1));
		
		if (dmg > 0) {
			loc.getWorld().getNearbyEntities(loc, dx, dy, dz).forEach(mob -> {
				if (mob instanceof LivingEntity) {
					((LivingEntity) mob).damage(dmg);
					mob.setFireTicks(Math.max(mob.getFireTicks(), tickiPalenia));
				}
			});
		}
	}
}
