package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.MineZ.Karabiny;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class CustomoweItemy extends Komenda {
	public static final String prefix = Func.prefix("Customowe Itemy");
	
	public CustomoweItemy() {
		super("customowyitem", prefix + "/citem [(baza <nick> <item>) / (bazy <item>) / (karabin [broń / ammo] <item>)]", "citem");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) {
			List<String> lista = Lists.newArrayList("baza");
			if (Main.włączonyModół(Bazy.class))
				lista.add("bazy");
			if (Main.włączonyModół(Karabiny.class))
				lista.add("karabin");
			return utab(args, lista);
		}
		switch (args[0].toLowerCase()) {
		case "baza":
			if (args.length == 2)
				return null;
			return uzupełnijTabComplete(Func.listToString(args, 2), Baza.itemy.keySet());
		case "bazy":
			if (args.length == 2)
				return utab(args, Bazy.getBazy());
			break;
		case "karabin":
			if (args.length == 2)
				return utab(args, "broń", "ammo");
			if (args.length == 3)
				return utab(args, Karabiny.getKarabiny());
			break;
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) return false;
		
		if (args[0].equalsIgnoreCase("baza")) {
			// /citem baza <nick> <nazwa>
			if (args.length < 3) return false;
			return dajzBazy(sender, args[1], Func.listToString(args, 2));
		}
		
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		
		Player p = (Player) sender;
		ItemStack item;
		switch (args[0].toLowerCase()) {
		case "bazy":
			// /citem bazy <nazwa>
			if (!Main.włączonyModół(Bazy.class))
				return Func.powiadom(p, prefix + "Ta funkcja aktualnie jest wyłączona w configu");
			item = Bazy.getBaze(args[1]);
			if (item == null)
				return Func.powiadom(prefix, p, "Niepoprawna nazwa %s", args[1]);
			break;
		case "karabin":
			// /citem karabin [broń / ammo] <nazwa>
			if (!Main.włączonyModół(Karabiny.class))
				return Func.powiadom(p, prefix + "Ta funkcja aktualnie jest wyłączona w configu");
			Function<String, ItemStack> func;
			switch (args[1].toLowerCase()) {
			case "broń":
			case "bron":
				func = Karabiny::getKarabin;
				break;
			case "ammo":
				func = Karabiny::getAmmunicje;
				break;
			default:
				return Func.powiadom(p, prefix + "/citem karabin [broń / ammo] <karabin>");
			}
			if (args.length < 3)
				return Func.powiadom(p, prefix + "/citem karabin [broń / ammo] <karabin>");
			String nazwa = Func.listToString(args, 2);
			item = func.apply(nazwa);
			if (item == null) 
				return Func.powiadom(prefix, p, "Niepoprawna nazwa karabinu %s", nazwa);
			Func.dajItem(p, item);
			return Func.powiadom(prefix, p, "Otrzymałeś %s %s", args[1], nazwa);
		default:
			return false;
		}
		Func.dajItem(p, item);
		return Func.powiadom(prefix, p, "Otrzymałeś item", args[1]);
	}
	private boolean dajzBazy(CommandSender sender, String nick, String item) {
		Player p = Bukkit.getPlayer(nick);
		if (p == null || !p.isOnline())
			sender.sendMessage(prefix + Func.msg("Niepoprawna nazwa gracza: %s.", nick));
		else {
			nick = p.getDisplayName();
			ItemStack _item = Baza.itemy.get(item);
			if (item == null)
				sender.sendMessage(prefix + Func.msg("Niepoprawny item: %s.", item));
			else {
				p.getInventory().addItem(_item);
				sender.sendMessage(prefix + Func.msg("Dano %s customowy item %s.", nick, item));
			}
		}
		return true;
	}
	
	public static String raport() {
		return "§6CustomoweItemy: §e" + Baza.itemy.size();
	}
	
}
