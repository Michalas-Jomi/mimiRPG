package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class Mi extends Komenda {
	public static final String prefix = Func.prefix("Wiadomość Console");

	public Mi() {
	    super("mi", prefix + "/mi <wiadomość>");
	    ustawKomende("lis", prefix + "/lis <wiadomość>", null);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) 
			throwFormatMsg("Tej komendy można używać tylko z konsoli");
		if (args.length < 1) return false;
		String pref = Main.ust.wczytajD("WiadomoscConsole" + (cmd.getName().equalsIgnoreCase("lis") ? " lis" : "")) + " ";
		Bukkit.broadcastMessage(Func.koloruj(pref + Func.listToString(args, 0)));
		return true;
	}

}
