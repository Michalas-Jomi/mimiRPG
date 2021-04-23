package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class KomendyInfo extends Komenda implements Listener {
	public static final String prefix = Func.prefix("Komendy Info");
	public KomendyInfo() {
		super("komendyinfo", prefix + "/komendyInfo (lista | gracz)");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			if (args[0].equals("lista")) {
				p.sendMessage(prefix + "Gracze którzy wykrywają komendy: ");
				for (CommandSender odbiorca : gracze)
					p.sendMessage("§6- §e" + odbiorca.getName());
				p.sendMessage("");
			} else {
				Player p2 = Bukkit.getPlayer(args[0]);
				if (p2 == null) {
					p.sendMessage(prefix + "Nieprawidłowa nazwa gracza: " + args[0]);
					return true;
				}
				if (gracze.contains(p2)) {
					gracze.remove(p2);
					p2.sendMessage(prefix + "Nie podglądasz już komend graczy");
					p.sendMessage (prefix + "Gracz §e" + p2.getName() + "§6 nie podgląda już komend graczy");
					return true;
				} else {
					gracze.add(p2);
					p2.sendMessage(prefix + "Dzięki prawomocnikowi §e" + p.getName() + "§6 podglądasz komendy graczy");
					p.sendMessage (prefix + "Gracz §e" + p2.getName() + "§6 od teraz podgląda komendy graczy");
					return true;
				}
			}
		} else {
			if (gracze.contains(p)) {
				gracze.remove(p);
				p.sendMessage(prefix + "Nie podglądasz już komend graczy");
			} else {
				gracze.add(p);
				p.sendMessage(prefix + "Podglądasz komendy graczy");
			}
		}
		return true;
	}

	public List<CommandSender> gracze = Lists.newArrayList();
	@EventHandler(priority=EventPriority.LOWEST)
	public void wykrywanieKomend(PlayerCommandPreprocessEvent ev) {
		for (CommandSender p : gracze)
			if (p != null)
				p.sendMessage("§8[§7Cmd§8] §7" + ev.getPlayer().getName() + ": " + ev.getMessage());
	}
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		gracze.remove(ev.getPlayer());
		Main.log(ev.getPlayer().getDisplayName() + "§f wyszedł z gry");
	}
}
