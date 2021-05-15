package me.jomi.mimiRPG.SkyBlock.Multi;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Bestie implements Listener {
	private static final String tagBestii = "mimiBestia";
	
	public static void ustawLicznikHp(LivingEntity mob) {
		mob.setCustomNameVisible(true);
		mob.addScoreboardTag(tagBestii);
		odświeżLicznikHp(mob);
	}
	
	private static void odświeżLicznikHp(LivingEntity mob) {
		double maxHp = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double hp = mob.getHealth();
		
		double procent = hp / maxHp;
		
		char kolor;
		if		(procent > .9)	kolor = 'a';
		else if (procent > .75)	kolor = '2';
		else if (procent > .5)	kolor = 'e';
		else if (procent > .2)	kolor = 'c';
		else					kolor = '4';
		
		mob.setCustomName(String.format("§9%s §%s%s§8/§a%s§4♥", Func.enumToString(mob.getType()), kolor, (int) hp, (int) maxHp));
	}
	
	@EventHandler
	public void spawn(EntitySpawnEvent ev) {
		if (ev.getEntity() instanceof Monster)
			ustawLicznikHp((LivingEntity) ev.getEntity());
	}
	@EventHandler
	public void obrażenia(EntityDamageEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains(tagBestii))
			odświeżLicznikHp((LivingEntity) ev.getEntity());
	}
	
}
