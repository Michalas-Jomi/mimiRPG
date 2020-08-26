package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Czapka extends Komenda {

	public Czapka() {
		super("czapka");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Poka� mi swoj� g�ow� to ci to za�o�e");
		Player p = (Player) sender;
		String prefix = Func.prefix("Czapka");
		ItemStack helm = p.getInventory().getHelmet();
		ItemStack reka = p.getInventory().getItemInMainHand();
		if	 	(helm != null && reka == null) p.sendMessage(prefix + "Zdj�to czapk�");
		else if	(helm == null && reka != null) p.sendMessage(prefix + "Za�o�on� czapk�");
		else if (helm != null && reka != null) p.sendMessage(prefix + "Za�o�ono now� czapk�");
		else 								   p.sendMessage(prefix + "Nie trzymasz nic w r�ce");
		p.getInventory().setItemInMainHand(helm);
		p.getInventory().setHelmet(reka);
		return true;
	}

}
