package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;

public class CustomoweItemy extends Komenda {
	public static final String prefix = Func.prefix("Customowe Itemy");
	
	public CustomoweItemy() {
		super("customowyitem", null, "citem");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupełnijTabComplete(Func.listToString(args, 0), Lists.newArrayList(Baza.itemy.keySet()));
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch(args.length) {
		case 0:
			break;
		case 1:
			if (sender instanceof Player)
				return daj(sender, sender.getName(), args[0]);
			else
				break;
		default:
			return daj(sender, args[1], args[0]);
		}
		sender.sendMessage(prefix + "/customowyitem <item> (gracz)");
		return true;
	}
	
	private boolean daj(CommandSender sender, String nick, String item) {
		Player p = Bukkit.getPlayer(nick);
		if (p == null || !p.isOnline())
			sender.sendMessage(prefix + Func.msg("Niepoprawna nazwa gracza: %s.", nick));
		else {
			ItemStack _item = Baza.itemy.get(item);
			if (item == null)
				sender.sendMessage(prefix + Func.msg("Niepoprawny item: %s.", item));
			else {
				if (p.getInventory().firstEmpty() == -1)
					sender.sendMessage(prefix + Func.msg("Ekwipunek gracz %s jest pełny", nick));
				else {	
					p.getInventory().addItem(_item);
					sender.sendMessage(prefix + Func.msg("Dano %s customowy item %s.", nick, item));
				}
			}
		}
		return true;
	}

	public static String raport() {
		return "§6CustomoweItemy: §e" + Baza.itemy.size();
	}
	
}
