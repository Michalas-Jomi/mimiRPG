package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class Pomoc extends Komenda implements Przeładowalny{
	public static final String prefix = Func.prefix("Pomoc");
	public final Config config = new Config("Pomoc");
	
	public Pomoc() {
		super("mimipomoc", prefix + "/mimipomoc <sekcja>", "pomoc", "mimihelp");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, config.klucze(false));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		if (!config.klucze(false).contains(args[0]))
			return Main.powiadom(sender, prefix + "Niepoprawna sekcja: §e" + args[0]);
		config.wczytajNapis(args[0]).wyświetl(sender);
		return true;
	}
	

	@Override
	public void przeładuj() {
		config.przeładuj();
	}

	@Override
	public String raport() {
		return "§6Pomoce: §e" + config.klucze(false).size();
	}
	
}