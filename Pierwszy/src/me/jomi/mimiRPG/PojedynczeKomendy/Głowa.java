package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Głowa extends Komenda {

	public Głowa() {
	    super("głowa", Func.prefix("Głowa") + "/głowa <url>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Nie ma głowy dla ciebie");
		if (args.length < 1) return false;
		((Player) sender).getInventory().addItem(Func.dajGłówkę("§1Głowa", Func.listToString(args, 0), null));
		return true;
	}

}
