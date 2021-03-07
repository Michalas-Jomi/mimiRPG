package me.jomi.mimiRPG.util;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Chat.Raport;

public class Przeładuj extends Komenda {
	public Przeładuj() {
		super("przeładuj");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupełnijTabComplete(Func.listToString(args, 0), Przeładowalny.przeładowalne.keySet());
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0)
			przeładuj(sender);
		else if (Przeładowalny.przeładowalne.containsKey(args[0]))
			przeładuj(sender, args[0]);
		else
			sender.sendMessage("§cNieporawna nazwa " + args[0]);
		return true;
	}
	
	private void przeładuj(CommandSender sender) {
		Baza.przeładuj();
		for (Przeładowalny p : Przeładowalny.przeładowalne.values())
			przeładuj(sender, p);
		sender.sendMessage("§aPrzeładowano wszystko");
	}
	public static void przeładuj(CommandSender sender, String co) {
		Baza.przeładuj();
		przeładuj(sender, Przeładowalny.przeładowalne.get(co));
		sender.sendMessage("§aPrzeładowano " + co);
	}
	public static void przeładuj(CommandSender sender, Przeładowalny p) {
		if (p.getClass().isAnnotationPresent(Przeładowalny.WymagaReloadBukkitData.class)) {
			if (!Main.przeładowywanaBukkitData()) {
				Main.reloadBukkitData();
				return;
			}
		}
		
		p.przeładuj();
		String r = Raport.raport(p);
		if (sender instanceof Player)
			sender.sendMessage(r);
		Main.log(r);
			
	}
}
