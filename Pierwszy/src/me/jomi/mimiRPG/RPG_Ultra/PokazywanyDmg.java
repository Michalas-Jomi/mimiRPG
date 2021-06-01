package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.World;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class PokazywanyDmg implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void damage(EntityDamageEvent ev) {
		if (!ev.isCancelled())
			zrespDmg(ev.getEntity(), ev.getFinalDamage(), "c");
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void heal(EntityRegainHealthEvent ev) {
		if (!(ev.getEntity() instanceof LivingEntity)) return;
		if (ev.isCancelled()) return;
		
		LivingEntity mob = (LivingEntity) ev.getEntity();
		
		double maxHp = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double hp = mob.getHealth();
		
		zrespDmg(ev.getEntity(), Math.min(ev.getAmount(), maxHp - hp), "a");
	}
	
	private void zrespDmg(Entity entity, double _dmg, String kolor) {
		int dmg = (int) (_dmg > (int) _dmg ? _dmg + 1 : _dmg);
		
		World world = NMS.nms(entity.getWorld());
		
		Location loc = entity.getLocation();
		Vector dir = loc.getDirection().rotateAroundY(Math.toRadians(Func.losujWZasięgu(360))).multiply(Func.losuj(.75, 1.5));
		loc = loc.add(dir.getX(), 0, dir.getZ());
		
		EntityArmorStand as = new EntityArmorStand(world, loc.getX(), loc.getY(), loc.getZ());
		as.setCustomNameVisible(true);
		as.setCustomName(CraftChatMessage.fromStringOrNull("§" + kolor + dmg));
		as.setSmall(true);
		as.setNoGravity(true);
		as.setInvisible(true);
		as.setInvulnerable(true);
		
		world.addEntity(as, SpawnReason.CUSTOM);
		Func.opóznij(10, () -> as.getBukkitEntity().remove());
	}

}
