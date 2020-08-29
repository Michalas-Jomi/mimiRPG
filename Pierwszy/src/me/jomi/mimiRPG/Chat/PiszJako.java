package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;

public class PiszJako extends Komenda {

	public PiszJako() {
	    super("piszjako", Func.prefix("Pisz Jako") + "/piszJako <gracz> <text>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) return false;
		List<Entity> en = Bukkit.selectEntities(sender, args[0]);
		if (en.isEmpty()) {
			sender.sendMessage(Func.prefix("Pisz Jako") + " Niepoprawna nazwa gracza: §e" + args[0]);
			return true;
		}
		for (Entity e : en)
			if (e instanceof Player)
				((Player) e).chat(Func.listToString(args, 1));
		return true;
	}
	
	

}
