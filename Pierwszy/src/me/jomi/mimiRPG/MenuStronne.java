package me.jomi.mimiRPG;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class MenuStronne {
	private static ItemStack nic_dolne   = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE,	   1, "§6§2 ", null);
	private static ItemStack totalne_nic = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "§6§2 ", null);
	private static ItemStack lewo  = Func.dajG³ówkê("§6Poprzednia strona", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=", null);
	private static ItemStack prawo = Func.dajG³ówkê("§6Nastêpna strona",   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19", null);
	
	public int wielkoœæ;
	public int strona = 0;
	public Inventory inv;
	public List<ItemStack> itemy = Lists.newArrayList();
	
	public MenuStronne(int rzêdy, String nazwa) {
		inv = Bukkit.createInventory(null, rzêdy*9, nazwa);
		wielkoœæ = rzêdy*9-9;
		for (int i=wielkoœæ+1; i<rzêdy*9-1; i++)
			inv.setItem(i, nic_dolne);
		inv.setItem(wielkoœæ,   lewo);
		inv.setItem(wielkoœæ+8, prawo);
	}
	public void odœwie¿() {
		//int i = strona * wielkoœæ;
		int i = 0;
		while (i < (strona+1) * wielkoœæ && i < itemy.size()) {
			inv.setItem(i, itemy.get(strona * wielkoœæ + i));
			i++;
		}
		while (i < wielkoœæ) {
			inv.setItem(i, totalne_nic);
			i++;
		}
	}
	public void zmieñStronê(int strona) {
		int mx = itemy.size() / wielkoœæ;
		strona		= Math.min(strona, mx);
		this.strona = Math.max(strona, 0);
		odœwie¿();
	}
	public void nastêpnaStrona() {
		zmieñStronê(strona + 1);
	}
	public void poprzedniaStrona() {
		zmieñStronê(strona - 1);
	}
}
