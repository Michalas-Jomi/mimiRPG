package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.Maszyny.Budownik;
import me.jomi.mimiRPG.Miniony.Minion;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

public class Raport extends Komenda {
	public static final String prefix = Func.prefix("Raport");
	
	public Raport() {
		super("raport", prefix + "/raport (sekcja)");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) {
			List<String> lista = Lists.newArrayList(Przeładowalny.przeładowalne.keySet());
			lista.add("handlery");
			return utab(args, lista);
		} else if (args.length == 2 && args[0].equalsIgnoreCase("handlery")) {
			List<String> lista = Func.wykonajWszystkim(Bukkit.getPluginManager().getPlugins(), Plugin::getName);
			lista.add("wszystkie");
			return utab(args, lista);
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("handlery")) {
				Plugin plugin;
				if (args.length >= 2) {
					if (args[1].equalsIgnoreCase("wszystkie")) {
						int wszystkie = 0;
						sender.sendMessage(prefix + "Zarejestrowane handlery:");
						for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
							int ile = HandlerList.getRegisteredListeners(p).size();
							sender.sendMessage(Raport.raport(Func.r(p.getName(), ile)));
							wszystkie += ile;
						}
						return Func.powiadom(sender, "Wszystkie: %s", wszystkie);
					}
					plugin = Bukkit.getPluginManager().getPlugin(args[1]);
					if (plugin == null)
						return Func.powiadom(sender, prefix + "Nieodnaleziono pluginu %s", args[1]);
				} else
					plugin = Main.plugin;
				
				return Func.powiadom(sender, prefix + "Zarejestrowane handlery pluginu %s: %s", plugin.getName(), HandlerList.getRegisteredListeners(plugin).size());
			}
			
			Przeładowalny p = Przeładowalny.przeładowalne.get(args[0]);
			
			HandlerList.getRegisteredListeners(Main.plugin);
			
			if (p == null) return Func.powiadom(sender, prefix + "Nieprawidłowa sekcja");
			sender.sendMessage("\n\n\n§6~~~~~~§emimiRaport§6~~~~~~\n");
			if (args[0].equals(Moduły.class.getSimpleName()))
				sender.sendMessage(Moduły.moduły());
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
