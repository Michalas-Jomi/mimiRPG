package me.jomi.mimiRPG.SkyBlock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Boostery extends Komenda implements Listener {
	public static final String prefix = Func.prefix("Boostery");
	
	public Boostery() {
		super("booster", "/booster <booster> <nick> <czas>");
	}
	
	
	// fly
	static Set<String> latający = new HashSet<>();
	@EventHandler
	public void zmianaFly(PlayerToggleFlightEvent ev) {
		if (latający.contains(ev.getPlayer().getName()) && !ev.isFlying())
			ev.setCancelled(true);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "fly");
		else if (args.length == 3)
			return utab(args, "5m", "15m", "1h");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3)
			return false;
		
		Player p = Func.gracz(sender, args[1]);
		if (p == null)
			return Func.powiadom(sender, prefix + "niepoprawny gracz %s", args[1]);
		
		int sekundy = Func.czas(Func.listToString(args, 2));
		
		switch (args[0].toLowerCase()) {
		case "fly":
			if (latający.contains(p.getName()))
				return Func.powiadom(sender, prefix + "Gracz już ma aktywny ten booster");
			Func.powiadom(p, prefix + "Otrzymano booster %s na %s", args[0], Func.czas(sekundy));
			Func.powiadom(sender, prefix + "Dano booster %s dla %s na %s", args[0], p.getDisplayName(), Func.czas(sekundy));
			latający.add(p.getName());
			p.setAllowFlight(true);
			p.setFlying(true);
			Func.opóznij(sekundy * 20, () -> {
				latający.remove(p.getName());
				Func.powiadom(p, prefix + "booster %s skończył się", args[0]);
				Func.powiadom(sender, prefix + "booster %s gracza %s skończył się", args[0], p.getName());
				try { p.setFlying(false);
				} catch(Throwable e) {}
				p.setAllowFlight(false);
			});
		}
		
		return true;
	}
}
