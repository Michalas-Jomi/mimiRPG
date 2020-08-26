package me.jomi.mimiRPG.CustomowyDrop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;

public class Drop {
	List<Item> itemy = Lists.newArrayList();
	boolean wy章czPierwotny;
	ItemStack itemNaG這wie;
	String imie;
	
	public Drop(boolean wy章czPierwotny, ItemStack itemNaG這wie, String imie) {
		this.wy章czPierwotny = wy章czPierwotny;
		this.itemNaG這wie = itemNaG這wie;
		if (imie != null)
			imie = Func.koloruj(imie);
		this.imie = imie;
	}
	public boolean warunek(LivingEntity mob) {
		if (imie != null && !imie.equals(mob.getCustomName()))
			return false;
		ItemStack glowa = mob.getEquipment().getHelmet();
		if (glowa == null || glowa.getType().isAir())
			glowa = null;
		if (itemNaG這wie != null && !itemNaG這wie.equals(glowa))
			return false;
		return true;
	}
	public void dodajDrop(double szansa, double bonusEnch, ItemStack item) {
		itemy.add(new Item(szansa, bonusEnch, item));
	}
	public void dropnij(Location loc, int lvl) {
		for (Item item : itemy)
			item.upu��(loc, lvl);
	}

	public String toString() {
		return String.format("呀Drop(imie=%s)", Func.odkoloruj(imie));
	}
	
}
