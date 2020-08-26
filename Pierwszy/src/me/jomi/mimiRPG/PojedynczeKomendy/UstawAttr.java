package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class UstawAttr extends Komenda {
	public UstawAttr() {
	    super("ustawattr", prefix + "/ustawAttr <Atrybut> [gracz] [wartoœæ]");
	}

	public static String prefix = "§2[§aUstaw Atrybut§2] §6";
	
	private static Attribute dajAttr(String nazwa) {
		switch(nazwa){
		case "Zdrowie":
		case "hp":
			return Attribute.GENERIC_MAX_HEALTH;
		case "Obra¿enia":
		case "dmg":
			return Attribute.GENERIC_ATTACK_DAMAGE;
		case "Pancerz":
		case "def":
			return Attribute.GENERIC_ARMOR;
		case "Prêdkoœæ_ataku":
		case "atsp":
			return Attribute.GENERIC_ATTACK_SPEED;
		case "Odpornoœæ_na_odrzut":
		case "knock":
			return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
		default:
			return null;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "Zdrowie", "Obra¿enia", "Pancerz", "Prêdkoœæ_ataku", "Odpornoœæ_na_odrzut");
		if (args.length >= 3)
			return Lists.newArrayList();
		return null;
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;

		Attribute attr = dajAttr(args[0]);
		if (attr == null) {
			p.sendMessage(prefix + "Nie poprawna nazwa atrybutu: §e" + args[0]);
			return true;
		}
		
		Player gracz = null;
		if (args.length >= 2) {
			gracz = Bukkit.getPlayer(args[1]);
			if (!Bukkit.getOnlinePlayers().contains(gracz)) {
				p.sendMessage(prefix + "Gracz §e" + args[1] + "§6 nie jest online");
				return true;
			};
		}
		if (p instanceof Player)
			gracz = (Player) p;
		if (gracz == null)
			return Main.powiadom(p, prefix + "Konsole jest ponad tym");
		
		double ile = 0;
		if (args.length >= 3) {
			try {
				ile = Func.zaokr¹glij(Double.parseDouble(args[2].trim()), 2);
			} catch(NumberFormatException er) {
				p.sendMessage(prefix + "§e" + args[2] + "§6 nie jest poprawn¹ liczb¹");
				return true;
			}
		}

		gracz.closeInventory();
		switch (args.length) {
		case 1:
		case 2:
			p.sendMessage(prefix + "Atrybut §e" + args[0] + "§6 gracza §e" + gracz.getName()  + "§6 wynosi §e" + Func.zaokr¹glij(gracz.getAttribute(attr).getBaseValue(), 2));
			return true;
		case 3:
			gracz.getAttribute(attr).setBaseValue(ile);
			p.sendMessage(prefix + "Ustawiono atrybut §e" + args[0] + "§6 gracza §e" + gracz.getName() + "§6 na §e" + ile);
			return true;
		}
		return false;
	}

}
