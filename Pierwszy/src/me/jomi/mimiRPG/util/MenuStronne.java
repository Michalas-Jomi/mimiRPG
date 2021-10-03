package me.jomi.mimiRPG.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class MenuStronne {
	private static ItemStack nic_dolne   = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE,	   1, "§6§2 ", null);
	private static ItemStack totalne_nic = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, "§6§2 ", null);
	private static ItemStack lewo  = Func.dajGłówkę("§6Poprzednia strona", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=");
	private static ItemStack prawo = Func.dajGłówkę("§6Następna strona",   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19");
	
	public int wielkość;
	public int strona = 0;
	public Inventory inv;
	public List<ItemStack> itemy = Lists.newArrayList();
	
	public MenuStronne(int rzędy, String nazwa) {
		inv = Func.createInventory(null, rzędy*9, Func.koloruj(nazwa));
		wielkość = rzędy*9-9;
		for (int i=wielkość+1; i<rzędy*9-1; i++)
			inv.setItem(i, nic_dolne);
		inv.setItem(wielkość,   lewo);
		inv.setItem(wielkość+8, prawo);
	}
	public void odśwież() {
		//int i = strona * wielkość;
		int i = 0;
		while (i < (strona+1) * wielkość && i < itemy.size()) {
			inv.setItem(i, itemy.get(strona * wielkość + i));
			i++;
		}
		while (i < wielkość) {
			inv.setItem(i, totalne_nic);
			i++;
		}
	}
	public void zmieńStronę(int strona) {
		int mx = itemy.size() / wielkość;
		strona		= Math.min(strona, mx);
		this.strona = Math.max(strona, 0);
		odśwież();
	}
	public void następnaStrona() {
		zmieńStronę(strona + 1);
	}
	public void poprzedniaStrona() {
		zmieńStronę(strona - 1);
	}
}
