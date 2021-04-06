package me.jomi.mimiRPG.Chat;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Pomoc extends Komenda implements Przeładowalny{
	public static final String prefix = Func.prefix("Pomoc");
	public final Config config = new Config("Pomoc");
	
	public Pomoc() {
		super("mimipomoc", prefix + "/pomoc <sekcja>", "pomoc", "mimihelp");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, config.klucze());
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		Predicate<String> pomoc = s -> {
			if (!config.klucze().contains(s))
				return false;
			config.wczytajNapis(s).wyświetl(sender);
			return true;
		};
		
		if (args.length < 1)
			return pomoc.test("Główna");
		
		if (!pomoc.test(args[0]))
			return Func.powiadom(sender, prefix + "Niepoprawna sekcja: §e" + args[0]);
		return true;
	}
	

	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Pomoce", config.klucze().size());
	}	
}
