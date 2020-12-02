package me.jomi.mimiRPG.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class PanelStronny {
	private static final ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "&1&l ");
	private static final ItemStack poprzedniaStrona	= Func.stwórzItem(Material.WRITABLE_BOOK, "&aPoprzednia Strona");
	private static final ItemStack następnaStrona	= Func.stwórzItem(Material.WRITABLE_BOOK, "&aNastępna Strona");
	private final List<Inventory> strony = Lists.newArrayList();
	
	private final int rządki;
	private final String nazwa;
	
	private class Holder implements InventoryHolder {
		Inventory inv;
		int strona;
		@Override
		public Inventory getInventory() {
			return inv;
		}
	}
	
	
	/**
	 * Tworzy panel
	 * jeden rządek jest tworzony dodatkowo dla zmiany stron
	 * 
	 * @param rządki 1-5
	 */
	public PanelStronny(int rządki, String nazwa) {
		this.nazwa = Func.koloruj(nazwa);
		this.rządki = rządki;
	}
	
	
	Inventory stwórzInv() {
		Holder holder = new Holder();
		
		holder.inv = Func.stwórzInv(holder, rządki + 1, nazwa);
		
		holder.strona = strony.size();
		
		for (int i = rządki*9; i < rządki*9+9; i++)
			holder.inv.setItem(i, pustySlot);
		
		if (holder.strona != 0)
			holder.inv.setItem(rządki*9, poprzedniaStrona);
		
		return holder.inv;
}
	void dodajStrone() {
		Inventory inv = stwórzInv();
		
		Holder holder = (Holder) inv.getHolder();
		if (holder.strona != 0)
			strony.get(holder.strona - 1).setItem(rządki*9+8, następnaStrona);
		
		strony.add(inv);
	}
	
	
	/**
	 * ustawia item na panelu na odpowiedniej stronie
	 * 
	 * @param rząd 0 - rządki	
	 * @param kolumna 0 - n
	 * @param item
	 */
	public void ustawItem(int rząd, int kolumna, ItemStack item) {
		Krotka<Integer, Integer> k = slot(rząd, kolumna);
		
		while (strony.size() < k.a)
			dodajStrone();
		
		strony.get(k.a).setItem(k.b, item);
	}
	public ItemStack wezItem(int rząd, int kolumna) {
		Krotka<Integer, Integer> k = slot(rząd, kolumna);
		return strony.get(k.a).getItem(k.b);
	}
	public ItemStack usuńItem(int rząd, int kolumna) {
		ItemStack item = wezItem(rząd, kolumna);
		ustawItem(rząd, kolumna, null);
		return item;
	}
	private Krotka<Integer, Integer> slot(int rząd, int kolumna) {
		if (rząd > rządki || rząd < 0)	throw new Error("rząd może być tylko z zakresu 0 - rządki (" + rządki + "), podano: " + rząd);
		if (kolumna < 0)				throw new Error("kolumna nie może być niższa niż 0, podano: " + kolumna);
		
		int invid = kolumna / 6;
		kolumna = kolumna % 6;
		
		return new Krotka<>(invid, rząd*9 + kolumna);
	}
	
	public boolean jestPanelem(Inventory inv) {
		return inv.getHolder() != null && inv.getHolder() instanceof Holder;
	}

	/**
	 * obsługuje zmiane stron
	 * 
	 * @param ev obiekt Eventu
	 * @return true jeśli event został obsłużony
	 */
	public boolean clickEvent(InventoryClickEvent ev) {
		int slot = ev.getRawSlot();
		
		if (slot < 0 || slot >= ev.getInventory().getSize())
			return true;
		
		if (slot < rządki*9)
			return false;
		
		ev.setCancelled(true);
		
		if (ev.getCurrentItem().equals(pustySlot))
			return true;
		
		int i = 1;
		switch (slot % 9) {
		case 0: 
			i = -1;
		case 8:
			ev.getWhoClicked().openInventory(strony.get(((Holder) ev.getInventory().getHolder()).strona + i));
		}
		
		return true;
	}

}
