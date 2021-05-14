package me.jomi.mimiRPG.api;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
	
	public static void włącz() {
		try {
			rg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			
			for (Flag<?> flaga : new Flag<?>[]{
				flagaUżywanieWiadra = new StateFlag("NapelnianieWiadra", true),
				flagaStawianieBaz = new StateFlag("StawianieBaz", true),
				flagaRadiacja = new StateFlag("Radiacja", false),
				flagaC4 = new StateFlag("C4", false),
				flagaCustomoweMoby = new StringFlag("CustomoweMoby")
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
	}
}
