package me.jomi.mimiRPG.SkyBlock.Multi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEvent;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Krytyki implements Listener {
	
	public static class LiczenieKrytykaEvent extends PlayerEvent {
		
		private double dmg = 1; // dodatkowy dmg
		private double szansa = .2;
		
		public final Entity atakowany;
		
		public LiczenieKrytykaEvent(Player p, Entity atakowany) {
			super(p);
			this.atakowany = atakowany;
		}
		
		public double getDmg() {
			return this.dmg;
		}
		public void zwiększDmg(double mnożnik) {
			dmg += mnożnik;
		}
		
		public double getSzansa() {
			return szansa;
		}
		public void zwiększSzanse(double bonus) {
			szansa += bonus;
		}
		
		
		private static final HandlerList handlers = new HandlerList();
		public static HandlerList getHandlerList() { return handlers; }
		@Override public HandlerList getHandlers() { return handlers; }
	}

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void atak(EntityDamageByEntityEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getDamage() <= 0) return;
		if (ev.getDamager() instanceof Player) {
			LiczenieKrytykaEvent ev2 = new LiczenieKrytykaEvent((Player) ev.getDamager(), ev.getEntity());
			Bukkit.getPluginManager().callEvent(ev2);
			
			if (Func.losuj(ev2.getSzansa())) {
				ev.setDamage(ev.getDamage() * (1 + ev2.getDmg()));
			}
		}
	}
}
