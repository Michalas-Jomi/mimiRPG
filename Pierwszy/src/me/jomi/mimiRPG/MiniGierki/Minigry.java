package me.jomi.mimiRPG.MiniGierki;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;

public class Minigry extends Komenda implements Prze³adowalny {
	
	public Minigry() {
		super("minigra", 			 Func.prefix("Minigra") + "/miniGra <minigra>", "mg");
	    ustawKomende("minigraustaw", Func.prefix("Minigra") + "/minigraustaw <minigra> <nazwa>", null);
		ustawKomende("tc", 			 Func.prefix("Minigra") + "/tc wiadomoœæ", null);
		Main.dodajPermisje("minigry.komendy");
		new Golf();
		new Sumo();
		new PaintBall();
		new OneShotOneKill();
	}
	public void prze³aduj() {
		MiniGra.config.prze³aduj();
		for (MiniGra mg : Main.minigry.values())
			mg.prze³aduj();
	}
	public String raport() {
		String w = "§6Minigry:";
		for (MiniGra mg : Main.minigry.values())
			w += "\n§b--§6" + mg.nazwa + ": §e" + mg.areny.obiekty.size() + " aren";
		return w;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("tc"))
			return null;
		List<String> lista = null;
		if (args.length <= 1)
			return lista = Lists.newArrayList(Main.minigry.keySet().iterator());
		
		if (cmd.getName().equalsIgnoreCase("minigraustaw") && args.length == 2)
			if (args[1].equals("PaintBall"))
				lista = Arrays.asList("start", "koniec", "czerwoni", "niebiescy");
			else 
				lista = Arrays.asList("start", "koniec");
		
		return uzupe³nijTabComplete(args, lista);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Minigry to nie miejsce dla ciebie");
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("tc"))
			return Main.powiadom(p, "§2[§aMinigry§2] §6Tej komendy mo¿na u¿ywaæ tylko w minigrach");
				
		if (cmd.getName().equalsIgnoreCase("minigraustaw")) {
			if (args.length < 2) return false;
			if (Main.minigry.containsKey(args[0]))
				Main.minigry.get(args[0]).ustawLokacje(p, args);
			else
				p.sendMessage("§2[§aMinigry§2] §6Niepoprawna nazwa minigry: §e" + args[0]);
			return true;
		}
		
		if (args.length <= 0) return false;
		if (Main.minigry.containsKey(args[0]))
			Main.minigry.get(args[0]).dolacz(p);
		else
			p.sendMessage("§2[§aMinigry§2] §6Niepoprawna nazwa minigry: §e" + args[0]);
		return true;
	}
}
