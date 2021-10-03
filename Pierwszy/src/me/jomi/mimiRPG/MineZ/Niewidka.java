package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class Niewidka extends Komenda implements Listener {
	public Niewidka() {
		super("niewidka", "/niewidka [uniewidzialnij | odniewidzialnij] <selektor>");
		
		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
		Func.wykonajDlaNieNull(main.getTeam("mimiNiewidka"), Team::unregister);
		team = main.registerNewTeam("mimiNiewidka");
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		team.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
		team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
		team.setCanSeeFriendlyInvisibles(true);
		team.displayName(Func.toComponent("Niewidka"));
	}

	Team team;
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "uniewidzialnij", "odniewidzialnij");
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		List<Entity> en;
		try {
			en = Bukkit.selectEntities(sender, Func.listToString(args, 1));
		} catch (Throwable e) {
			return false;
		}
		Consumer<Player> cons;
		switch (args[0].toLowerCase()) {
		case "u":
		case "uw":
		case "unw":
		case "uniewidzialnij":
			cons = p -> {
				team.addEntry(p.getName());
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*60*60*5, 1, false, false, false));
			};
			break;
		case "o":
		case "ow":
		case "dnw":
		case "odniewidzialnij":
			cons = p ->  {
				team.removeEntry(p.getName());
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
			};
			break;
		default:
			return false;
		}
		for (Entity e : en)
			if (e instanceof Player)
				cons.accept((Player) e);
		return true;
	}

	@EventHandler()
	public void teleport(PlayerTeleportEvent ev) {
		if (team.removeEntry(ev.getPlayer().getName()))
			ev.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
	}

}
