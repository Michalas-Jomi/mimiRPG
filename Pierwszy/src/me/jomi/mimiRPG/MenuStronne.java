package me.jomi.mimiRPG;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class MenuStronne {
	private static ItemStack nic_dolne   = Func.stw髍zItem(Material.BLACK_STAINED_GLASS_PANE,	   1, "񘬎 ", null);
	private static ItemStack totalne_nic = Func.stw髍zItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "񘬎 ", null);
	private static ItemStack lewo  = Func.dajG丑wk�("�6Poprzednia strona", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=", null);
	private static ItemStack prawo = Func.dajG丑wk�("�6Nast阷na strona",   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19", null);
	
	public int wielko滄;
	public int strona = 0;
	public Inventory inv;
	public List<ItemStack> itemy = Lists.newArrayList();
	
	public MenuStronne(int rz阣y, String nazwa) {
		inv = Bukkit.createInventory(null, rz阣y*9, nazwa);
		wielko滄 = rz阣y*9-9;
		for (int i=wielko滄+1; i<rz阣y*9-1; i++)
			inv.setItem(i, nic_dolne);
		inv.setItem(wielko滄,   lewo);
		inv.setItem(wielko滄+8, prawo);
	}
	public void od渨ie�() {
		//int i = strona * wielko滄;
		int i = 0;
		while (i < (strona+1) * wielko滄 && i < itemy.size()) {
			inv.setItem(i, itemy.get(strona * wielko滄 + i));
			i++;
		}
		while (i < wielko滄) {
			inv.setItem(i, totalne_nic);
			i++;
		}
	}
	public void zmie馭tron�(int strona) {
		int mx = itemy.size() / wielko滄;
		strona		= Math.min(strona, mx);
		this.strona = Math.max(strona, 0);
		od渨ie�();
	}
	public void nast阷naStrona() {
		zmie馭tron�(strona + 1);
	}
	public void poprzedniaStrona() {
		zmie馭tron�(strona - 1);
	}
}
