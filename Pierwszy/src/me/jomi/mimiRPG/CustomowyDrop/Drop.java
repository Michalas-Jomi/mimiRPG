package me.jomi.mimiRPG.CustomowyDrop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;

public class Drop {
	List<Item> itemy = Lists.newArrayList();
	boolean wy��czPierwotny;
	ItemStack itemNaG�owie;
	String imie;
	
	public Drop(boolean wy��czPierwotny, ItemStack itemNaG�owie, String imie) {
		this.wy��czPierwotny = wy��czPierwotny;
		this.itemNaG�owie = itemNaG�owie;
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
		if (itemNaG�owie != null && !itemNaG�owie.equals(glowa))
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
		return String.format("�rDrop(imie=%s)", Func.odkoloruj(imie));
	}
	
}
