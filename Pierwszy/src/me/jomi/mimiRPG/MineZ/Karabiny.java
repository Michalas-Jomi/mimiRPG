package me.jomi.mimiRPG.MineZ;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Karabiny extends AbstractKarabiny<Karabiny.Karabin> {
	public static final String prefix = Func.prefix(Karabiny.class);
	
	
	public Karabiny() {
		super("edytujkarabin", "Karabiny", Karabin.class);
	}

	
	public static class Karabin extends AbstractKarabiny.Karabin {
		@Mapowane EntityType typPocisku = EntityType.ARROW;
		@Mapowane double siłaStrzału = 3;
		
		
		@Override
		protected void strzel(Player p, Vector wzrok) {
			Projectile pocisk = (Projectile) p.getWorld().spawnEntity(p.getEyeLocation(), typPocisku);
			Func.ustawMetadate(pocisk, "mimiPocisk", nazwa);
			pocisk.setVelocity(wzrok.multiply(siłaStrzału));
			pocisk.setInvulnerable(true);
			pocisk.setShooter(p);
			if (kolorOgonaPocisku != null)
				Func.opóznij(2, () -> tickPocisku(pocisk));
		}
		private void tickPocisku(Projectile pocisk) {
			if (pocisk.isDead()) return;
			Func.particle(pocisk.getLocation(), 1, 0, 0, 0, 0, kolorOgonaPocisku.kolor(), 1f);
			Func.opóznij(1, () -> tickPocisku(pocisk));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void usuwanieStzał(ProjectileHitEvent ev) {
		Projectile pocisk = ev.getEntity();
		if (!pocisk.hasMetadata("mimiPocisk")) return;

		Location loc = pocisk.getLocation();
		
		Karabin karabin = karabiny.get(pocisk.getMetadata("mimiPocisk").get(0).asString());	
		if (karabin.mocWybuchu > 0)
			pocisk.getWorld().createExplosion(pocisk.getLocation(), (float) karabin.mocWybuchu, false, false, (Player) pocisk.getShooter());
		if (karabin.particleWybuchu != null)
			karabin.particleWybuchu.zresp(loc);
		
		loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0, 0, 0, .1);

		pocisk.remove();
	}
	@EventHandler(priority = EventPriority.LOW)
	public void postrzelenie(EntityDamageByEntityEvent ev) {
		Entity damager = ev.getDamager();
		if (!(damager instanceof Projectile)) return;
		
		Projectile pocisk = (Projectile) damager;
		if (!pocisk.hasMetadata("mimiPocisk")) return;
		
		Karabin karabin = karabiny.get(pocisk.getMetadata("mimiPocisk").get(0).asString());
		ev.setDamage(karabin.dmg);
	}
}

