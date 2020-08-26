package me.jomi.mimiRPG;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class MenuStronne {
	private static ItemStack nic_dolne   = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE,	   1, "�6�2 ", null);
	private static ItemStack totalne_nic = Func.stw�rzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "�6�2 ", null);
	private static ItemStack lewo  = Func.dajG��wk�("�6Poprzednia strona", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=", null);
	private static ItemStack prawo = Func.dajG��wk�("�6Nast�pna strona",   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19", null);
	
	public int wielko��;
	public int strona = 0;
	public Inventory inv;
	public List<ItemStack> itemy = Lists.newArrayList();
	
	public MenuStronne(int rz�dy, String nazwa) {
		inv = Bukkit.createInventory(null, rz�dy*9, nazwa);
		wielko�� = rz�dy*9-9;
		for (int i=wielko��+1; i<rz�dy*9-1; i++)
			inv.setItem(i, nic_dolne);
		inv.setItem(wielko��,   lewo);
		inv.setItem(wielko��+8, prawo);
	}
	public void od�wie�() {
		//int i = strona * wielko��;
		int i = 0;
		while (i < (strona+1) * wielko�� && i < itemy.size()) {
			inv.setItem(i, itemy.get(strona * wielko�� + i));
			i++;
		}
		while (i < wielko��) {
			inv.setItem(i, totalne_nic);
			i++;
		}
	}
	public void zmie�Stron�(int strona) {
		int mx = itemy.size() / wielko��;
		strona		= Math.min(strona, mx);
		this.strona = Math.max(strona, 0);
		od�wie�();
	}
	public void nast�pnaStrona() {
		zmie�Stron�(strona + 1);
	}
	public void poprzedniaStrona() {
		zmie�Stron�(strona - 1);
	}
}
