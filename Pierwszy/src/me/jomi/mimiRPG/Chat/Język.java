package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class Język extends Komenda {
	public static final String prefix = Func.prefix("Język");
	
	public Język() {
		super("język", "/język <język>");
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, Func.wykonajWszystkim(Gracz.Język.values(), j -> j.toString().toLowerCase()));
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 2 && args[0].equalsIgnoreCase("-p"))
			Func.wykonajDlaNieNull(Bukkit.getPlayer(args[1]),
					p  -> sender.sendMessage(prefix + Func.msg("Język gracza %s %s", p.getDisplayName(), Func.enumToString(Gracz.wczytaj(p).język))),
					() -> sender.sendMessage(prefix + Func.msg("%s nie jest aktualnie na serwerze", args[1])));
		else if (sender instanceof Player && args.length >= 1) {
			try {
				Gracz.Język język = Func.StringToEnum(Gracz.Język.class, args[0]);
				Gracz g = Gracz.wczytaj(sender.getName());
				g.język = język;
				g.zapisz();
				return true;
			} catch (Throwable e) {
				return false;
			}
		}
		return false;
	}

}
