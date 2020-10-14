package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;

@Moduł
public class ZamienEq extends Komenda{
	public static final String prefix = Func.prefix("Zamiana eq");

	public ZamienEq() {
		super("zamieńeq", prefix + "/zamieńeq <nick> (nick)");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> gracze = Lists.newArrayList();
		for (OfflinePlayer gracz : Bukkit.getOfflinePlayers()) {
			String imie = gracz.getName();
			if (imie.startsWith(args.length <= 0 ? "" : args[args.length-1]))
				gracze.add(imie);
		}
		return gracze;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		if (args.length >= 2 && istnieje(sender, args[0]) && istnieje(sender, args[1])) {
			zamień(sender, args[0], args[1]);
		} else if (args.length == 1) {
			if (!(sender instanceof Player)) return false;
			zamień(sender, sender.getName(), args[0]);
		}
		return true;
	}
	private void zamień(CommandSender sender, String nick1, String nick2) {
		if (nick1.equals(nick2)) {
			sender.sendMessage(prefix + "Nie możesz zamiń eq gracza z nim samym");
			return;
		}
		
		Player p;
		p = Bukkit.getPlayer(nick1);
		if (p != null) p.kickPlayer("§6Zamiana eq z §e" + nick2);
		p = Bukkit.getPlayer(nick2);
		if (p != null) p.kickPlayer("§6Zamiana eq z §e" + nick1);
		
		String u1 = Func.graczOffline(nick1).getUniqueId().toString();
		String u2 = Func.graczOffline(nick2).getUniqueId().toString();
		
		// u1 -> switch
		if (!Func.przenieśPlik(sc(u1, false), sc("aaaSWITCH", false)))  Main.log("§eNie odnaleziono pliku gracza", nick1, "Gracz", nick2, "otrzyma pusty ekwipunek");
		Func.przenieśPlik(	   sc(u1, true),  sc("aaaSWITCH", true));
		
		// u2 -> u1
		if (!Func.przenieśPlik(sc(u2, false), sc(u1, false))) Main.log("§eNie odnaleziono pliku gracza", nick2, "Gracz", nick1, "otrzyma pusty eqwipunek");
		Func.przenieśPlik(	   sc(u2, true),  sc(u1, true));
		
		// switch -> u2
		Func.przenieśPlik(sc("aaaSWITCH", false), sc(u2, false));
		Func.przenieśPlik(sc("aaaSWITCH", true),  sc(u2, true));
		
		Main.log("Zamieniono eq, ec i exp graczy", nick1, nick2);
	}
	private String sc(String uuid, boolean old) {
		return "world/playerdata/" + uuid + ".dat" + (old ? "_old" : "");
	}
	private boolean istnieje(CommandSender p, String nick) {
		if (Func.graczOffline(nick) != null)
			return true;
		p.sendMessage("Gracz " + nick + " nigdy nie był online!");
		return false;
	}

}
