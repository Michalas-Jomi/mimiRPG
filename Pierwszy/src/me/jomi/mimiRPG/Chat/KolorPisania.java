package me.jomi.mimiRPG.Chat;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.collect.Lists;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Grupa;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Gracze.Gracze;

@Moduł
public class KolorPisania extends Komenda implements Listener {
	public static String prefix = Func.prefix("Kolor Pisania");
	
	public KolorPisania() {
	    super("kolorpisania", prefix + "/kolorPisania <nick> <symbol koloru>", "kp");
	    Main.dodajPermisje("przejściakolorów");
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void Pisanie(AsyncPlayerChatEvent ev) {
		if (ev.isCancelled()) return;
		String msg = ev.getMessage();
		StringBuilder msgB = new StringBuilder();
		
		if (Main.perms != null)
			for (String grupa : Main.perms.getPlayerGroups(ev.getPlayer()))
				if (Baza.grupy.contains(grupa)) {
					Grupa grp = (Grupa) Baza.grupy.get(grupa);
					msgB.append(grp.kolorpisania);
				}
		
		String symbol = Gracze.gracz(ev.getPlayer().getName()).kolorPisania;
		msgB.append(symbol).append(msg);
		
		msg = msgB.toString();
		if (ev.getPlayer().hasPermission("mimirpg.przejściakolorów"))
			msg = Func.przejścia(msg);
		ev.setMessage(msg);
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void PisanieMonitor(AsyncPlayerChatEvent ev) {
		if (ev.isCancelled()) return;
		String msg = ev.getMessage();
			if (msg.contains("§x"))
				Main.log(Func.usuńKolor(ev.getPlayer().getDisplayName()) + ": " + Func.usuńKolor(msg));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 2)
			return false;
		if (Bukkit.getPlayer(args[0]) == null) {
			if (!args[0].startsWith("g:") && Main.perms != null && 
					Lists.newArrayList(Main.perms.getGroups()).contains(args[0].substring(2)))
				return Func.powiadom(p, prefix + "Gracz §e" + args[0] + "§6 nie jest online");
		}
		String symbol = args[1];
		if (symbol.equalsIgnoreCase("&r"))
			symbol = "";
		else {
			for (int i=2; i<args.length; i++)
				symbol += " " + args[i];
			if (symbol.length() > 100) {
				p.sendMessage(prefix + "Maksymalna ilość znaków to §e100");
				return true;
			}
		}
		if (args[0].startsWith("g:")) {
			Grupa grupa = Baza.grupa(args[0].substring(2));
			grupa.kolorpisania = symbol;
			Baza.config.ustaw_zapisz("grupy."+args[0].substring(2), grupa);
			if (symbol != "") p.sendMessage(prefix + "Zmieniono domyślny kolor pisania grupy §e" + args[0].substring(2) + "§6 na " + symbol);
			else 			  p.sendMessage(prefix + "Zresetowano domyślny kolor pisania grupy §e" + args[0].substring(2));	
			return true;
		}
		Gracz gracz = Gracze.gracz(args[0]);
		gracz.kolorPisania = symbol;
		gracz.config.ustaw_zapisz("kolorPisania", symbol);
		if (symbol != "") p.sendMessage(prefix + "Zmieniono domyślny kolor pisania gracza §e" + args[0] + "§6 na " + symbol);
		else 			  p.sendMessage(prefix + "Zresetowano domyślny kolor pisania gracza §e" + args[0]);	
		return true;
	}
}
