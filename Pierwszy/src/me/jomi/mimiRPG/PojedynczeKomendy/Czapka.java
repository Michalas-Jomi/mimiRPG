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
			return Main.powiadom(sender, "Poka¿ mi swoj¹ g³owê to ci to za³o¿e");
		Player p = (Player) sender;
		String prefix = Func.prefix("Czapka");
		ItemStack helm = p.getInventory().getHelmet();
		ItemStack reka = p.getInventory().getItemInMainHand();
		if	 	(helm != null && reka == null) p.sendMessage(prefix + "Zdjêto czapkê");
		else if	(helm == null && reka != null) p.sendMessage(prefix + "Za³o¿on¹ czapkê");
		else if (helm != null && reka != null) p.sendMessage(prefix + "Za³o¿ono now¹ czapkê");
		else 								   p.sendMessage(prefix + "Nie trzymasz nic w rêce");
		p.getInventory().setItemInMainHand(helm);
		p.getInventory().setHelmet(reka);
		return true;
	}

}
