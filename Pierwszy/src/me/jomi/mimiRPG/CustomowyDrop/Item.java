package me.jomi.mimiRPG.CustomowyDrop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Item {
	private int pe³ne;
	private double szansa;
	private ItemStack item;
	private double bonusEnch;
	
	public Item(double szansa, double bonusEnch, ItemStack item) {
		this.item = item;
		pe³ne = (int) szansa;
		this.bonusEnch = bonusEnch;
		this.szansa =  szansa - pe³ne;
	}
	
	public void upuœæ(Location loc, int lvl) {
		double szansa = this.szansa + (lvl * bonusEnch);
		for (int i=0; i<pe³ne; i++)
			if (Math.random() <= szansa)
				loc.getWorld().dropItem(loc, item);
	}
	
}
