package me.jomi.mimiRPG.SkyBlock.Multi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityItem;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class PokazywanyDmg implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void damage(EntityDamageEvent ev) {
		zrespDmg(ev.getEntity(), ev.getFinalDamage());
	}
	
	private void zrespDmg(Entity entity, double dmg) {
		Location loc = entity.getLocation();
		Vector dir = loc.getDirection().rotateAroundY(Math.toRadians(Func.losujWZasięgu(360))).multiply(Func.losuj(.75, 1.5));
		loc = loc.add(dir.getX(), 0, dir.getZ());
		
		EntityArmorStand as = new EntityArmorStand(NMS.nms(entity.getWorld()), loc.getX(), loc.getY(), loc.getZ());
		as.setCustomNameVisible(true);
		as.setCustomName(CraftChatMessage.fromStringOrNull("§c" + (int) dmg));
		as.setSmall(true);
		as.setNoGravity(true);
		as.setInvisible(true);
		as.setInvulnerable(true);
		
		NMS.nms(entity.getWorld()).addEntity(as);

		/*
		CraftArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		
		armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.HAND,  	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.HEAD,  	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.CHEST,	LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.LEGS,		LockType.ADDING_OR_CHANGING);
		armorStand.addEquipmentLock(EquipmentSlot.FEET,  	LockType.ADDING_OR_CHANGING);
		//armorStand.setCustomName("§c" + (int) dmg);
		//armorStand.setCustomNameVisible(true);
		//armorStand.setInvulnerable(true);
		armorStand.setCollidable(false);
		//armorStand.setInvisible(true);
		//armorStand.setGravity(false);
		//armorStand.setSmall(true);
		armorStand.teleport(loc);
		*/
		
		//Func.opóznij(10, armorStand::remove);
		Func.opóznij(10, () -> as.killEntity());
		
//		Item item = (Item) entity.getWorld().spawnEntity(entity.getLocation().add(0, entity.getHeight() / 3d * 2, 0), EntityType.DROPPED_ITEM);
	//	item.setCustomName("§c" + (int) dmg);
		//item.setCustomNameVisible(true);
		//item.setPickupDelay(100);
		
		/*
  		ItemStack ikona = null;
  		
		if (entity instanceof Lootable) {
			ikona = Func.losuj(((Lootable) entity).getLootTable().populateLoot(new Random(),
					new LootContext.Builder(entity.getLocation()).lootedEntity(entity).killer(null).luck(1f).lootingModifier(3).build()));
			if (ikona != null)
				ikona.setAmount(1);
		}
*/
		
		//item.setItemStack(ikona != null ? ikona : new ItemStack(Material.GHAST_TEAR));
		
		EntityItem nms = new EntityItem(NMS.nms(entity.getWorld()), entity.getLocation().getZ(), entity.getLocation().getY() + entity.getHeight() / 3 * 2, entity.getLocation().getZ(), CraftItemStack.asNMSCopy(Func.stwórzItem(Material.GHAST_TEAR)));
		nms.setCustomName(CraftChatMessage.fromStringOrNull("§c" + (int) dmg, false));
		nms.setCustomNameVisible(true);
		nms.pickupDelay = 10;
		
		Main.log(NMS.nms(entity.getWorld()).addEntity(nms, SpawnReason.CUSTOM));
		
		
		/*
		PacketPlayOutSpawnEntity packet = new net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity(nms, 2);
		entity.getNearbyEntities(10, 10, 10).forEach(p -> {
			if (p instanceof Player)
				NMS.nms((Player) p).playerConnection.sendPacket(packet);
		});
		*/
//		item.remove();
		
		//Func.opóznij(10, item::remove);
	}

}
