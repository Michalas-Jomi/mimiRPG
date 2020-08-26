package me.jomi.mimiRPG.Chat;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Gracze.Gracze;

public class KolorPisania extends Komenda implements Listener {
	public static String prefix = Func.prefix("Kolor Pisania");
	
	public KolorPisania() {
	    super("kolorpisania", prefix + "/kolorPisania <nick> <symbol koloru>", "kp");
	    Main.dodajPermisje("przej�ciakolor�w");
	}
	
	
	@EventHandler(priority=EventPriority.LOW)
	public void Pisanie(AsyncPlayerChatEvent ev) {
		if (ev.isCancelled()) return;
		String msg = ev.getMessage();
		String symbol = Gracze.gracz(ev.getPlayer().getName()).kolorPisania;
		msg = symbol + msg;
		if (ev.getPlayer().hasPermission("mimirpg.przej�ciakolor�w"))
			msg = Func.przej�cia(msg);
			
		ev.setMessage(msg);
	}

	// Blokada zabijania Invulnerable Mob�w
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (ev.getEntity().isInvulnerable())
			ev.setCancelled(true);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 2)
			return false;
		if (Bukkit.getPlayer(args[0]) == null) 
			return Main.powiadom(p, prefix + "Gracz �e" + args[0] + "�6 nie jest online");
		String symbol = args[1];
		if (symbol.equalsIgnoreCase("&r"))
			symbol = "";
		else {
			for (int i=2; i<args.length; i++)
				symbol += " " + args[i];
			if (symbol.length() > 100) {
				p.sendMessage(prefix + "Maksymalna ilo�� znak�w to �e100");
				return true;
			}
		}
		Gracz gracz = Gracze.gracz(args[0]);
		gracz.kolorPisania = symbol;
		gracz.config.ustaw_zapisz("kolorPisania", symbol);
		if (symbol != "") p.sendMessage(prefix + "Zmieniono domy�lny kolor pisania gracza �e" + args[0] + "�6 na " + symbol);
		else 				p.sendMessage(prefix + "Zresetowano domy�lny kolor pisania gracza �e" + args[0]);	
		return true;
	}
	
	
}
