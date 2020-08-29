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
			return Main.powiadom(sender, "Pokaż mi swoją głowę to ci to założe");
		Player p = (Player) sender;
		String prefix = Func.prefix("Czapka");
		ItemStack helm = p.getInventory().getHelmet();
		ItemStack reka = p.getInventory().getItemInMainHand();
		if	 	(helm != null && reka == null) p.sendMessage(prefix + "Zdjęto czapkę");
		else if	(helm == null && reka != null) p.sendMessage(prefix + "Założoną czapkę");
		else if (helm != null && reka != null) p.sendMessage(prefix + "Założono nową czapkę");
		else 								   p.sendMessage(prefix + "Nie trzymasz nic w ręce");
		p.getInventory().setItemInMainHand(helm);
		p.getInventory().setHelmet(reka);
		return true;
	}

}
