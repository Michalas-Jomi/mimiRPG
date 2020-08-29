package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze�adowalny;

public class Prze�aduj extends Komenda {

	public Prze�aduj() {
		super("prze�aduj");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupe�nijTabComplete(Func.listToString(args, 0), Prze�adowalny.prze�adowalne.keySet());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0)
			prze�aduj(sender);
		else if (Prze�adowalny.prze�adowalne.containsKey(args[0]))
			prze�aduj(sender, args[0]);
		else
			sender.sendMessage("�cNieporawna nazwa " + args[0]);
		return true;
	}
	
	private void prze�aduj(CommandSender sender) {
		Baza.prze�aduj();
		for (Prze�adowalny p : Prze�adowalny.prze�adowalne.values())
			prze�aduj(sender, p);
		sender.sendMessage("�aPrze�adowano wszystko");
	}
	private void prze�aduj(CommandSender sender, String co) {
		Baza.prze�aduj();
		prze�aduj(sender, Prze�adowalny.prze�adowalne.get(co));
		sender.sendMessage("�aPrze�adowano " + co);
	}
	private void prze�aduj(CommandSender sender, Prze�adowalny p) {
		p.prze�aduj();
		String r = p.raport();
		if (sender instanceof Player)
			sender.sendMessage(r);
		Main.log(r);
			
	}
}
