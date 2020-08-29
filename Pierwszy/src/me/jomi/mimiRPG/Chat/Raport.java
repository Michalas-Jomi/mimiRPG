package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze�adowalny;
import me.jomi.mimiRPG.Maszyny.Budownik;
import me.jomi.mimiRPG.Miniony.Minion;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.CustomoweItemy;

public class Raport extends Komenda {
	public static final String prefix = Func.prefix("Raport");
	
	public Raport() {
		super("raport", prefix + "/raport (sekcja)");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, Prze�adowalny.prze�adowalne.keySet());
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			Prze�adowalny p = Prze�adowalny.prze�adowalne.get(args[0]);
			if (p == null) return Main.powiadom(sender, prefix + "Nieprawid�owa sekcja");
			sender.sendMessage("\n\n\n�6~~~~~~�emimiRaport�6~~~~~~\n");
			sender.sendMessage(p.raport());
			sender.sendMessage("");
			return true;
		}
		sender.sendMessage("\n\n�6~~~~~~�emimiRaport�6~~~~~~");
		sender.sendMessage("�6Ekonomia: �e" + Func.BooleanToString(Main.ekonomia, "�aJest", "�cNie ma"));
		if (Main.w��czonyMod�(ChatGrupowy.class))
			sender.sendMessage("�6Czaty Grupowe: �e" + ChatGrupowy.inst.mapa.keySet().size());
		if (Main.w��czonyMod�(Miniony.class))
			if (Miniony.w��czone) sender.sendMessage("�6Miniony: �e" + Minion.mapa.size());
			else 				  sender.sendMessage("�6Miniony: �cWy��czone");	
		if (Main.w��czonyMod�(Glosowanie.class))
			sender.sendMessage("�6G�osowania: �e" + Glosowanie.mapa.size());
		if (Main.w��czonyMod�(CustomoweItemy.class))
			sender.sendMessage(CustomoweItemy.raport());
		if (Main.w��czonyMod�(Budownik.class))
			sender.sendMessage("�6Aktywne Budowniki:�e " + Budownik.budowniki.size());
		for (Prze�adowalny p : Prze�adowalny.prze�adowalne.values())
			sender.sendMessage(p.raport());
		return true;
	}

}
