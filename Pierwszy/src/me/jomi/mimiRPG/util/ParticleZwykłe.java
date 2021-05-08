package me.jomi.mimiRPG.util;

import org.bukkit.Location;
import org.bukkit.Particle;

import me.jomi.mimiRPG.Mapowany;

public class ParticleZwykłe extends Mapowany {
	@Mapowane Particle particle;
	@Mapowane int ilość = 20;
	@Mapowane double dx = 1;
	@Mapowane double dy = 1;
	@Mapowane double dz = 1;
	@Mapowane double prędkość = 1;
	
	
	public void zresp(Location loc) {
		Func.particle(particle, loc, ilość, dx, dy, dz, prędkość);
	}
}
