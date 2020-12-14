package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class EdytorUogólniony extends Komenda {
	public static final String prefix = Func.prefix("Edytor Ogólny");
	
	final HashMap<String, EdytorOgólny<?>> mapa = new HashMap<>();
	
	
	public EdytorUogólniony() {
		super("edytorogólny", "/eo edytor", "eo");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "edytor", "nowy", "zapomnij");
		if (args.length == 2 && args[1].equalsIgnoreCase("edytor"))
			return utab(args, "-t", "-u");
		return Lists.newArrayList();
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		switch(args[0].toLowerCase()) {
		case "edytor":
			EdytorOgólny<?> edytor = mapa.get(sender.getName());
			if (edytor == null)
				return Func.powiadom(prefix, sender, "Aby korzystać z edytora najpierw utwórz edytor");
			return edytor.onCommand(sender, label, args);
		case "nowy":
			if (args.length < 2)
				return Func.powiadom(prefix, sender, "/eo nowy <package.klasa>");
			try {
				mapa.put(sender.getName(), new EdytorOgólny<>("/edytorogólny", Class.forName(args[1], false, Main.classLoader)));
			} catch (Throwable e) {
				return Func.powiadom(prefix, sender, "Nieprawidłowa klasa %s", args[1]);
			}
			return Func.powiadom(prefix, sender, "Utworzono nowy edytor dla Ciebie");
		case "zapomnij":
			mapa.remove(sender.getName());
			return Func.powiadom(prefix, sender, "Nie korzystasz już z edytora");
		}
		return Func.powiadom(prefix, sender, "Nieznana operacja %s", args[0]);
	}
}


