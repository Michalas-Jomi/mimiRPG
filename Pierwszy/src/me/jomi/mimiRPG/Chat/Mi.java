package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Mi extends Komenda {

	public Mi() {
	    super("mi", Func.prefix("Wiadomoœæ Console") + "/mi <wiadomoœæ>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) 
			return Main.powiadom(sender, "Tej komendy mo¿na u¿ywaæ tylko z konsoli");
		if (args.length < 1) return false;
		String pref = Main.ust.wczytajLubDomyœlna("WiadomoscConsole", "[Konsola]") + " ";
		Bukkit.broadcastMessage(Func.koloruj(pref + Func.listToString(args, 0)));
		return true;
	}

}
