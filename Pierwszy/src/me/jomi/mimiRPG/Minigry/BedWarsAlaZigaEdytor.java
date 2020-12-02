package me.jomi.mimiRPG.Minigry;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

public class BedWarsAlaZigaEdytor extends Komenda implements Przeładowalny {
	public BedWarsAlaZigaEdytor() {
		super("edytujBedWarsAlaZiga");
	}

	static class HolderPanelu {
		//static List<HolderPanelu> holderzy
	}
	
	void otwórzPanelEdycjiItemów(Player p) {
		Inventory panel = Func.stwórzInv(6, "&1&lEdycja itemów");
		
		
		p.openInventory(panel);
	}
	
	
	@Override
	public void przeładuj() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Krotka<String, Object> raport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
}
