package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Miniony.Minion;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.CustomoweItemy;
import me.jomi.mimiRPG.SkyBlock.Budownik;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

public class Raport extends Komenda {
	public static final String prefix = Func.prefix("Raport");
	
	public Raport() {
		super("raport", prefix + "/raport (sekcja)");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, Przeładowalny.przeładowalne.keySet());
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			Przeładowalny p = Przeładowalny.przeładowalne.get(args[0]);
			if (p == null) return Func.powiadom(sender, prefix + "Nieprawidłowa sekcja");
			sender.sendMessage("\n\n\n§6~~~~~~§emimiRaport§6~~~~~~\n");
			sender.sendMessage(raport(p));
			sender.sendMessage("");
			return true;
		}
		sender.sendMessage("\n\n§6~~~~~~§emimiRaport§6~~~~~~");
		sender.sendMessage("§6Ekonomia: §e" + Func.BooleanToString(Main.ekonomia, "§aJest", "§cNie ma"));
		if (Main.włączonyModół(ChatGrupowy.class))
			sender.sendMessage("§6Czaty Grupowe: §e" + ChatGrupowy.inst.mapa.keySet().size());
		if (Main.włączonyModół(Miniony.class))
			if (Miniony.włączone) sender.sendMessage("§6Miniony: §e" + Minion.mapa.size());
			else 				  sender.sendMessage("§6Miniony: §cWyłączone");	
		if (Main.włączonyModół(Glosowanie.class))
			sender.sendMessage("§6Głosowania: §e" + Glosowanie.mapa.size());
		if (Main.włączonyModół(CustomoweItemy.class))
			sender.sendMessage(CustomoweItemy.raport());
		if (Main.włączonyModół(Budownik.class))
			sender.sendMessage("§6Aktywne Budowniki:§e " + Budownik.budowniki.size());
		for (Przeładowalny p : Przeładowalny.przeładowalne.values())
			sender.sendMessage(raport(p));
		return true;
	}

	public static String raport(Przeładowalny p) {
		try {
			return raport(p.raport());
		} catch (Throwable e) {
			Main.error("Błąd w raporcie " + p.getClass().getSimpleName() + " " + e.getCause());
			e.printStackTrace();
			return "§4" + p.getClass().getSimpleName() + " Błąd";
		}
	}
	public static String raport(Krotka<String, Object> raport) {
		return "§6" + raport.a + ": §e" + raport.b;	
	}
}
