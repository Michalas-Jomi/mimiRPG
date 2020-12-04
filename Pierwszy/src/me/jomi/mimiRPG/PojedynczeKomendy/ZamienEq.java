package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
// TODO naprawić
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
		if (istnieje(sender, args[0]))
			if (args.length >= 2 && istnieje(sender, args[1]))
				zamień(sender, args[0], args[1]);
			else if (args.length == 1) {
				if (!(sender instanceof Player))
					return false;
				zamień(sender, sender.getName(), args[0]);
			}
		return true;
	}
	private void zamień(CommandSender sender, String nick1, String nick2) {
		if (nick1.equals(nick2)) {
			sender.sendMessage(prefix + "Nie możesz zamiń eq gracza z nim samym");
			return;
		}

		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick1), Player::saveData);
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick2), Player::saveData);
		
		String u1 = Func.graczOffline(nick1).getUniqueId().toString();
		String u2 = Func.graczOffline(nick2).getUniqueId().toString();

		if (new File(sc("aaaSWITCH", false)).delete() || new File(sc("aaaSWITCH", true)).delete())
			Main.warn(prefix + "Wykryto starego switcha, usuwanie go");
		
		// u1 -> switch
		przenieś(sc(u1, false), sc("aaaSWITCH", false));
		przenieś(sc(u1, true),  sc("aaaSWITCH", true));
		
		// u2 -> u1
		przenieś(sc(u2, false), sc(u1, false));
		przenieś(sc(u2, true),  sc(u1, true));
		
		// switch -> u2
		przenieś(sc("aaaSWITCH", false), sc(u2, false));
		przenieś(sc("aaaSWITCH", true),  sc(u2, true));
		
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick1), Player::loadData);
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick2), Player::loadData);
		
		Main.log("Zamieniono eq, ec i exp graczy", nick1, nick2);
	}
	private void przenieś(String co, String gdzie) {
		File f = new File(co);
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		f.renameTo(new File(gdzie));
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
