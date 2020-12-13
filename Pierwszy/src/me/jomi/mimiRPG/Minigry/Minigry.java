package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

// TODO rzucanie blokami z lewej ręgi z wyrzutni

@Moduł
public class Minigry extends Komenda {
	static final String permCmdBypass = Func.permisja("minigry.bypasskomendy");
	static final HashMap<String, Minigra> mapaGier = new HashMap<>();
	
	public Minigry() {
		super("minigra", null, "mg");
		ustawKomende("opuśćMinigre", null, null);
		Main.dodajPermisje(permCmdBypass);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 2 && args[0].equalsIgnoreCase("paintball")) // TODO uniwersalnić
			return utab(args, "dołącz", "staty", "stopnie", "topka");
		if (args.length <= 2)
			return utab(args, "dołącz", "staty");
		if (args.length <= 1)
			return utab(args, Func.wykonajWszystkim(mapaGier.values(), m -> m.getClass().getSimpleName().toLowerCase()));
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("opuśćMinigre")) {
			if (!(sender instanceof Player))
				return Func.powiadom(sender, "Tylko Gracz może brać udział w minigrach");
			for (Minigra mg : mapaGier.values()) {
				Minigra.Arena arena = mg.arena((Player) sender);
				if (arena != null) {
					arena.opuść((Player) sender);
					break;
				}
			}
			return true;
		}
		if (args.length < 1) return false;
		Func.wykonajDlaNieNull(mapaGier.get(args[0].toLowerCase()), m -> m.onCommand(sender, args));
		return true;
	}
}


