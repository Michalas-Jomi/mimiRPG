package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
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
	static final HashMap<String, Minigra> mapaGier = new HashMap<>();
	
	public Minigry() {
		super("minigra", null, "mg");
		Main.dodajPermisje(permCmdBypass);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 2 && args[0].equalsIgnoreCase("paintball"))
			return utab(args, "dołącz", "opuść", "staty", "stopnie", "topka");
		if (args.length <= 2 && args[0].equalsIgnoreCase("cavewars"))
			return utab(args, "dołącz", "opuść", "staty");
		if (args.length <= 1)
			return utab(args, Func.wykonajWszystkim(mapaGier.values(), m -> m.getClass().getSimpleName().toLowerCase()));
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		Func.wykonajDlaNieNull(mapaGier.get(args[0].toLowerCase()), m -> m.onCommand(sender, args));
		return true;
	}
}
