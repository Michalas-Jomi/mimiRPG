package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class G這wa extends Komenda {

	public G這wa() {
	    super("g這wa", Func.prefix("G這wa") + "/g這wa <url>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Nie ma g這wy dla ciebie");
		if (args.length < 1) return false;
		((Player) sender).getInventory().addItem(Func.dajG堯wk�("�1G這wa", Func.listToString(args, 0), null));
		return true;
	}

}
