package me.jomi.mimiRPG.CustomowyDrop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Item {
	private int pe�ne;
	private double szansa;
	private ItemStack item;
	private double bonusEnch;
	
	public Item(double szansa, double bonusEnch, ItemStack item) {
		this.item = item;
		pe�ne = (int) szansa;
		this.bonusEnch = bonusEnch;
		this.szansa =  szansa - pe�ne;
	}
	
	public void upu��(Location loc, int lvl) {
		double szansa = this.szansa + (lvl * bonusEnch);
		for (int i=0; i<pe�ne; i++)
			if (Math.random() <= szansa)
				loc.getWorld().dropItem(loc, item);
	}
	
}
