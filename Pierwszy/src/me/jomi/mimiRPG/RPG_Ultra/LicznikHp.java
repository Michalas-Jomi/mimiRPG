package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class LicznikHp implements Listener {
	private static final String tagMobówZLicznikiem = "mimiMozZLicznikiemHp";
	private static final String metaMobówZLicznikiem = "mimiMozZLicznikiemHp";
	
	public static void ustawLicznikHp(LivingEntity mob) {
		mob.setCustomNameVisible(true);
		mob.addScoreboardTag(tagMobówZLicznikiem);
		Func.ustawMetadate(mob, metaMobówZLicznikiem, mob.getCustomName() == null ? Func.enumToString(mob.getType()) : mob.getCustomName());
		odświeżLicznikHp(mob);
	}
	public static void odświeżLicznikHp(LivingEntity mob) {
		double maxHp = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double hp = mob.getHealth();
		
		double procent = hp / maxHp;
		
		char kolor;
		if		(procent > .9)	kolor = 'a';
		else if (procent > .75)	kolor = '2';
		else if (procent > .5)	kolor = 'e';
		else if (procent > .2)	kolor = 'c';
		else					kolor = '4';
		
		String nazwa = mob.getMetadata(metaMobówZLicznikiem).get(0).asString();
		
		mob.setCustomName(String.format("§9%s §%s%s§8/§a%s§4♥", nazwa, kolor, (int) hp, (int) maxHp));
	}
	
	@EventHandler
	public void spawn(EntitySpawnEvent ev) {
		if (ev.getEntity() instanceof Monster)
			ustawLicznikHp((LivingEntity) ev.getEntity());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void obrażenia(EntityDamageEvent ev) {
		obsłużEvent(ev);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void heal(EntityRegainHealthEvent ev) {
		obsłużEvent(ev);
	}
	private void obsłużEvent(EntityEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains(tagMobówZLicznikiem))
			Bukkit.getScheduler().runTask(Main.plugin, () -> odświeżLicznikHp((LivingEntity) ev.getEntity()));
	}
	
}
