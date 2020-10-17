package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.function.Function;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.MineZ.Karabiny;

@Moduł
public class CustomoweItemy extends Komenda {
	public static final String prefix = Func.prefix("Customowe Itemy");
	
	public CustomoweItemy() {
		super("customowyitem", prefix + "/citem [baza / bazy / karabin]", "citem");
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
			return uzupełnijTabComplete(Func.listToString(args, 1), Baza.itemy.keySet());
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
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		Player p = (Player) sender;
		ItemStack item;
		switch (args[0].toLowerCase()) {
		case "baza":
			// /citem baza <nazwa>
			return dajzBazy(p, args[1]);
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
			item = func.apply(args[2]);
			if (item == null) 
				return Func.powiadom(prefix, p, "Niepoprawna nazwa karabinu %s", args[2]);
			Func.dajItem(p, item);
			return Func.powiadom(prefix, p, "Otrzymałeś %s %s", args[1], args[2]);
		default:
			return false; // TODO
		}
		Func.dajItem(p, item);
		return Func.powiadom(prefix, p, "Otrzymałeś item", args[1]);
	}
	private boolean dajzBazy(Player sender, String item) {
		Player p = sender;
		String nick = p.getDisplayName();
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
