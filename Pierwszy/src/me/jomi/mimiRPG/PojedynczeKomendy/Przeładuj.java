package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;

public class Prze³aduj extends Komenda {

	public Prze³aduj() {
		super("prze³aduj");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupe³nijTabComplete(Func.listToString(args, 0), Prze³adowalny.prze³adowalne.keySet());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0)
			prze³aduj(sender);
		else if (Prze³adowalny.prze³adowalne.containsKey(args[0]))
			prze³aduj(sender, args[0]);
		else
			sender.sendMessage("§cNieporawna nazwa " + args[0]);
		return true;
	}
	
	private void prze³aduj(CommandSender sender) {
		Baza.prze³aduj();
		for (Prze³adowalny p : Prze³adowalny.prze³adowalne.values())
			prze³aduj(sender, p);
		sender.sendMessage("§aPrze³adowano wszystko");
	}
	private void prze³aduj(CommandSender sender, String co) {
		Baza.prze³aduj();
		prze³aduj(sender, Prze³adowalny.prze³adowalne.get(co));
		sender.sendMessage("§aPrze³adowano " + co);
	}
	private void prze³aduj(CommandSender sender, Prze³adowalny p) {
		p.prze³aduj();
		String r = p.raport();
		if (sender instanceof Player)
			sender.sendMessage(r);
		Main.log(r);
			
	}
}
