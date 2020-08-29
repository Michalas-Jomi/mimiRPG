package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Instrukcja;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Pomoc extends Komenda {
	public static final String prefix = Func.prefix("Pomoc");
	
	public Pomoc() {
		super("mimipomoc", prefix + "/mimipomoc <sekcja> (strona)", "pomoc", "mimihelp");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, Instrukcja.mapa.keySet());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		if (!Instrukcja.mapa.containsKey(args[0]))
			return Main.powiadom(sender, prefix + "Niepoprawna sekcja: §e" + args[0]);
		int strona = 1;
		if (args.length >= 2) {
			strona = Func.Int(args[1], -1);
			if (strona == -1)
				return Main.powiadom(sender, prefix + "Niepoprawny numer Strony: §e" + args[1]);
		}
		Instrukcja.mapa.get(args[0]).info(sender, strona);
		return true;
	}
	
}
