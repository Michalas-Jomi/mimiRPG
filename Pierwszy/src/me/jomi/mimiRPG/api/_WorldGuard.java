package me.jomi.mimiRPG.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.Func;

public class _WorldGuard {
	public static WorldGuardPlugin rg;
    public static StringFlag flagaCustomoweMoby;
	public static StateFlag flagaStawianieBaz;
	public static StateFlag flagaC4;
	public static StateFlag flagaUżywanieWiadra;
	public static StateFlag flagaRadiacja;
	public static StateFlag flagaGod;
	public static StateFlag flagaTargowisko;
	
	public static void włącz() {
		try {
			rg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			
			for (Flag<?> flaga : new Flag<?>[] {
				flagaCustomoweMoby	= new StringFlag("CustomoweMoby"),
				flagaUżywanieWiadra	= new StateFlag("NapelnianieWiadra", true),
				flagaStawianieBaz	= new StateFlag("StawianieBaz", true),
				flagaTargowisko 	= new StateFlag("Targowisko", false),
				flagaRadiacja		= new StateFlag("Radiacja", false),
				flagaGod			= new StateFlag("god", false),
				flagaC4				= new StateFlag("C4", false)
			})
				WorldGuard.getInstance().getFlagRegistry().register(flaga);
			
			Main.zarejestruj(new ListenerFlag());
			
		} catch (NoClassDefFoundError e) {
			Baza.APIs.brakPluginu("WorldGuard");
		}
	}
	
	
	public static class ListenerFlag implements Listener {
		@EventHandler
		public void pobieranieWody(PlayerBucketFillEvent ev) {
			if (_WorldGuard.rg != null && !Func.regiony(ev.getBlock().getWorld()).getApplicableRegions(Func.locToVec3(ev.getBlock().getLocation()))
					.testState(_WorldGuard.rg.wrapPlayer(ev.getPlayer()), _WorldGuard.flagaUżywanieWiadra))
				ev.setCancelled(true);
		}
		@EventHandler
		public void dmg(EntityDamageEvent ev) {
			if (ev.getEntity() instanceof Player && _WorldGuard.rg != null) {
				ApplicableRegionSet regiony = Func.regiony(ev.getEntity().getWorld()).getApplicableRegions(Func.locToVec3(ev.getEntity().getLocation()));
				if (regiony.testState(localPlayer((Player) ev.getEntity()), flagaGod)) {
					ev.setCancelled(true);
				}
			}
			
		}
	}
	
	public static LocalPlayer localPlayer(Player p) {
		return _WorldGuard.rg.wrapPlayer(p);
	}
}
