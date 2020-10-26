package me.jomi.mimiRPG.Minigry;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Minigry extends Komenda {
	static final String permCmdBypass = Func.permisja("minigra.paintball.bypasskomendy");
	
	public Minigry() {
		super("minigra", null, "mg");
		Main.dodajPermisje(permCmdBypass);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 2 && args[0].equalsIgnoreCase("paintball"))
			return utab(args, "dołącz", "opuść", "staty", "stopnie", "topka");
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		switch (args[0].toLowerCase()) {
		case "paintball":
			return Paintball.onCommand(sender, args);
		}
		return true;
	}
}
