package me.jomi.mimiRPG.util;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class KomendaZItemami<T> extends Komenda {
	public KomendaZItemami(String komenda) {
		super(komenda, "/" + komenda + " <nazwa> (nick)");
	}
	
	public abstract ItemStack getItem(T obj);
	public abstract T getItem(String nazwa);
	public abstract Collection<String> getItemy();
	public abstract String getPrefix();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, getItemy());
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		
		Player p = null;
		
		if (sender instanceof Player)
			p = (Player) sender;
		
		if (args.length >= 2)
			try {
				p = (Player) Bukkit.selectEntities(sender, args[1]).get(0);
			} catch (Throwable e) {
				return Func.powiadom(sender, getPrefix() + "Niepoprawny gracz %s", args[1]);
			}
		
		if (p == null)
			return Func.powiadom(sender, getPrefix() + "/" + label + " <nazwa> <nick>");
		
		Player gracz = p;
		Func.wykonajDlaNieNull(getItem(args[0]), obj -> {
				Func.dajItem(gracz, getItem(obj));
				Func.powiadom(sender, getPrefix() + "dano %s %s", gracz.getDisplayName(), args[0]);
			}, () -> Func.powiadom(sender, getPrefix() + "Nieprawidłowa nazwa: %s", args[0]));
		
		return true;
	}
}
