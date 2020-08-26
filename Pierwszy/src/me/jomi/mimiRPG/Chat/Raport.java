package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;
import me.jomi.mimiRPG.Maszyny.Budownik;
import me.jomi.mimiRPG.Miniony.Minion;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.CustomoweItemy;

public class Raport extends Komenda {

	public Raport() {
		super("raport");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("§6~~~~~~§emimiRaport§6~~~~~~");
		sender.sendMessage("§6Ekonomia: §e" + Func.BooleanToString(Main.ekonomia, "§aJest", "§cNie ma"));
		if (Main.w³¹czonyModó³(ChatGrupowy.class))
			sender.sendMessage("§6Czaty Grupowe: §e" + ChatGrupowy.mapa.keySet().size());
		if (Main.w³¹czonyModó³(Miniony.class))
			if (Miniony.w³¹czone) sender.sendMessage("§6Miniony: §e" + Minion.mapa.size());
			else 				  sender.sendMessage("§6Miniony: §cWy³¹czone");	
		if (Main.w³¹czonyModó³(Glosowanie.class))
			sender.sendMessage("§6G³osowania: §e" + Glosowanie.mapa.size());
		if (Main.w³¹czonyModó³(CustomoweItemy.class))
			sender.sendMessage(CustomoweItemy.raport());
		if (Main.w³¹czonyModó³(Budownik.class))
			sender.sendMessage("§6Aktywne Budowniki:§e " + Budownik.budowniki.size());
		for (Prze³adowalny p : Prze³adowalny.prze³adowalne.values())
			sender.sendMessage(p.raport());
		return true;
	}

}
