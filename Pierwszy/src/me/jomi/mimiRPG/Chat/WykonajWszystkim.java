package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class WykonajWszystkim extends Komenda {

	public WykonajWszystkim() {
		super("wykonajwszystkim");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0)
			return Func.powiadom(sender, Func.prefix("Wykonaj Wszystkim") + "/wykonajwszystkim <komenda>\n§aWykona komende z konsoli dla każdego gracza osobno podmieniając {gracz} na nick gracza");
		String komenda = Func.listToString(args, 0);
		for (Player p : Bukkit.getOnlinePlayers())
			Main.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), komenda.replace("{gracz}", p.getName()));
		return true;
	}

}
