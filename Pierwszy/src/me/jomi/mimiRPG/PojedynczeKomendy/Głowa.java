package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class G³owa extends Komenda {

	public G³owa() {
	    super("g³owa", Func.prefix("G³owa") + "/g³owa <url>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Nie ma g³owy dla ciebie");
		if (args.length < 1) return false;
		((Player) sender).getInventory().addItem(Func.dajG³ówkê("§1G³owa", Func.listToString(args, 0), null));
		return true;
	}

}
