package me.jomi.mimiRPG.PojedynczeKomendy;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class BezpiecznePociski implements Listener {	
	@EventHandler
	public void damage(EntityDamageEvent ev) {
		if (
				Func.multiEquals(ev.getEntityType(), EntityType.ITEM_FRAME, EntityType.ARMOR_STAND) &&
				Func.multiEquals(ev.getCause(), DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION, DamageCause.PROJECTILE)
				)
			ev.setCancelled(true);
	}
}
