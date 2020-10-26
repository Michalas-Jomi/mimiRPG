package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class UstawAttr extends Komenda {
	public UstawAttr() {
	    super("ustawattr", prefix + "/ustawAttr <Atrybut> [gracz] [wartość]");
	}

	public static String prefix = "§2[§aUstaw Atrybut§2] §6";
	
	private static Attribute dajAttr(String nazwa) {
		switch(nazwa){
		case "Zdrowie":
		case "hp":
			return Attribute.GENERIC_MAX_HEALTH;
		case "Obrażenia":
		case "dmg":
			return Attribute.GENERIC_ATTACK_DAMAGE;
		case "Pancerz":
		case "def":
			return Attribute.GENERIC_ARMOR;
		case "Prędkość_ataku":
		case "atsp":
			return Attribute.GENERIC_ATTACK_SPEED;
		case "Odporność_na_odrzut":
		case "knock":
			return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
		default:
			return null;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "Zdrowie", "Obrażenia", "Pancerz", "Prędkość_ataku", "Odporność_na_odrzut");
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
		if (p instanceof Player)
			gracz = (Player) p;
		if (args.length >= 2) {
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null) 
				return Func.powiadom(p, prefix + "Gracz §e" + args[1] + "§6 nie jest online");
		}
		if (gracz == null)
			return Func.powiadom(p, prefix + "Konsole jest ponad tym");
		
		double ile = 0;
		if (args.length >= 3) {
			try {
				ile = Func.zaokrąglij(Double.parseDouble(args[2].trim()), 2);
			} catch(NumberFormatException er) {
				p.sendMessage(prefix + "§e" + args[2] + "§6 nie jest poprawną liczbą");
				return true;
			}
		}

		gracz.closeInventory();
		switch (args.length) {
		case 1:
		case 2:
			p.sendMessage(prefix + "Atrybut §e" + args[0] + "§6 gracza §e" + gracz.getName()  + "§6 wynosi §e" + Func.zaokrąglij(gracz.getAttribute(attr).getBaseValue(), 2));
			return true;
		case 3:
			gracz.getAttribute(attr).setBaseValue(ile);
			p.sendMessage(prefix + "Ustawiono atrybut §e" + args[0] + "§6 gracza §e" + gracz.getName() + "§6 na §e" + ile);
			return true;
		}
		return false;
	}

}
