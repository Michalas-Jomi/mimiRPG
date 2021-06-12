package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.QuadKrotka;
import me.jomi.mimiRPG.util.MultiConfig;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CraftingiRPG implements Przeładowalny, Listener {
	private static final Map<Integer, Recepta> mapaZId = new HashMap<>();
	private static final Map<String, Recepta> mapaZRezultu = new HashMap<>();
	private static final ItemStack itemBrakuRezultu = Func.stwórzItem(Material.STRUCTURE_VOID, "§cUłóż itemy po lewej");
	public static class Recepta {
		public final boolean domyślna; // false oznacza że musi być w pamięci gracza aby być wycratowaną
		
		public final String rezult;
		public final String[][] matrix;
		public final int[][] ilości;
		
		
		public Recepta(boolean domyślna, String rezult, String[][] matrix, int[][] ilości) {
			this.domyślna = domyślna;
			this.rezult = rezult;
			this.matrix = matrix;
			this.ilości = ilości;
			
			mapaZId.put(id(matrix), this);
			mapaZRezultu.put(rezult, this);
		}

		public boolean możeWycrafować(GraczRPG gracz) {
			return domyślna ? true : gracz.dataPamięć.getCompound("craftingi").hasKey(rezult);
		}
		public boolean możnaWycraftować(int[][] ilości) {
			if (ilości.length != this.ilości.length || ilości[0].length != this.ilości[0].length)
				throw new IllegalArgumentException("Niepoprawna tablica na wejściu ilości w craftingach RPG");
			
			for (int y=0; y < ilości.length; y++)
				for (int x=0; x < ilości[0].length; x++)
					if (ilości[y][x] < this.ilości[y][x])
						return false;
			return true;
		}
	
		public ItemStack rezult() {
			return ZfaktoryzowaneItemy.dajItem(rezult);
		}
	}
	
	public static int id(String[][] matrix) {
		StringBuilder strB = new StringBuilder();
		for (String[] Xmatrix : matrix) {
			for (String id : Xmatrix)
				strB.append(id == null ? "-" : id).append(" ");
			strB.append('\n');
		}
		return strB.toString().hashCode();
	}
	
	public static void odblokuj(GraczRPG gracz, String item) {
		RPG.dataDajUtwórz(gracz.dataPamięć, "craftingi").setBoolean(item, true);
	}
	
	static final Panel panel = new Panel(false);
	public CraftingiRPG() {
		panel.ustawClick(ev -> {
			if (ev.getRawSlot() == 23) {
				Inventory inv = ev.getInventory();
				QuadKrotka<Integer, Integer, int[][], Recepta> krotka = panel.dajDanePaneluPewny(inv);
				if (krotka == null)
					ev.setCancelled(true);
				else
					krotka.wykonaj((k1, k2) -> k1.wykonaj((X, Y) -> k2.wykonaj((ilości, recepta) -> {
						for (int y = Y; y < 3; y++)
							for (int x = X; x < 3; x++) {
								ItemStack item = inv.getItem((y + 1) * 9 + (x + 1));
								int potrzebne = ilości[y - Y][x - X];
								item.setAmount(item.getAmount() - potrzebne);
								
								if (item.getAmount() < 0)
									Main.error("Błąd w CraftingachRPG, id = 1");
								
								inv.setItem(y * 9 + x, item.getAmount() <= 0 ? null : item);
							}
						odświeżRezult(ev);
					})));
				return;
			} else {
				int r = ev.getRawSlot() / 9;
				int mod = ev.getRawSlot() % 9;
				if (r >= 1 && r <= 3 && mod >= 1 && mod <= 3) {
					odświeżRezult(ev);
					return;
				}
			}
			ev.setCancelled(true);
		});
		panel.ustawClose(ev -> {
			Player p = (Player) ev.getPlayer();
			for (int y = 1; y < 4; y++)
				for (int x = 1; x < 4; x++)
					Func.wykonajDlaNieNull(ev.getInventory().getItem(y * 9 + x), item -> Func.dajItem(p, item));
		});
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (Func.multiEquals(ev.getClick(), ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT) &&
				ev.getRawSlot() >= ev.getInventory().getSize() && panel.jestPanelem(ev.getInventory()))
			odświeżRezult(ev);
	}
	private static void odświeżRezult(InventoryInteractEvent ev) {
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			String[][] matrix = new String[3][3];
			Inventory inv = ev.getInventory();
			
			panel.ustawDanePanelu(inv, null);
			
			for (int y=0; y < 3; y++)
				for (int x=0; x < 3; x++)
					matrix[y][x] = ZfaktoryzowaneItemy.id(inv.getItem((y + 1) * 9 + x + 1));
			
			boolean x1 = matrix[0][0] == null && matrix[0][1] == null && matrix[0][2] == null;
			boolean x2 = matrix[1][0] == null && matrix[1][1] == null && matrix[1][2] == null;
			boolean x3 = matrix[2][0] == null && matrix[2][1] == null && matrix[2][2] == null;
			
			if (x1 && x2 && x3) {
				inv.setItem(23, itemBrakuRezultu);
				return;
			}
			
			boolean y1 = matrix[0][0] == null && matrix[1][0] == null && matrix[2][0] == null;
			boolean y2 = matrix[0][1] == null && matrix[1][1] == null && matrix[2][1] == null;
			boolean y3 = matrix[0][2] == null && matrix[1][2] == null && matrix[2][2] == null;
			
			int x = x1 ? (x2 ? 2 : 1) : 0;
			int y = y1 ? (y2 ? 2 : 1) : 0;
			int X = x3 ? (x2 ? 2 : 1) : 0;
			int Y = y3 ? (y2 ? 2 : 1) : 0;
			
			String[][] m = new String[3 - y - Y][3 - x - X];
			for (int my = 0; my < m.length; my++)
				for (int mx = 0; mx < m[0].length; mx++)
					m[my][mx] = matrix[my + y][mx + x];
			
			int id = id(m);
			Func.wykonajDlaNieNull(getRecepta(id), recepta -> {
				if (recepta.możeWycrafować(GraczRPG.gracz((Player) ev.getWhoClicked()))) {
					int[][] ilości = new int[m.length][m[0].length];
					for (int my = 0; my < m.length; my++)
						for (int mx = 0; mx < m[0].length; mx++)
							ilości[my][mx] = inv.getItem((my + y + 1) * 9 + (mx + x + 1)).getAmount();
					
					if (recepta.możnaWycraftować(ilości)) {
						inv.setItem(23, recepta.rezult());
						panel.ustawDanePanelu(inv, new QuadKrotka<>(x, y, ilości, recepta));
						return;
					}
				}
				inv.setItem(23, itemBrakuRezultu);
			}, () -> inv.setItem(23, itemBrakuRezultu));
		});
	}
	@EventHandler
	public void przeciągnie(InventoryDragEvent ev) {
		if (panel.jestPanelem(ev.getInventory()))
			odświeżRezult(ev);
	}
	
	@EventHandler
	public void otwieranieCraftingu(InventoryOpenEvent ev) {
		if (ev.getInventory().getType() == InventoryType.WORKBENCH) {
			ev.setCancelled(true);
			otwórzCrafting((Player) ev.getPlayer());
		}
	}
	
	public static void otwórzCrafting(Player p) {
		Inventory inv = panel.stwórz(null, 6, Func.koloruj("&#8c430e&lCrafting"));
		Func.ustawPuste(inv, Baza.pustySlotCzarny);

		for (int y=0; y < 3; y++)
			for (int x=0; x < 3; x++)
				inv.setItem((y + 1) * 9 + x + 1, null);
		
		inv.setItem(23, itemBrakuRezultu);
		
		p.openInventory(inv);
	}
	
	public static Recepta getRecepta(int id) {
		return mapaZId.get(id);
	}
	public static Recepta getRecepta(String rezult) {
		return mapaZRezultu.get(rezult);
	}
	
	
	@Override
	public void przeładuj() {
		MultiConfig config = new MultiConfig("craftingi");
		mapaZRezultu.clear();
		mapaZId.clear();
		
		config.klucze().forEach(klucz -> {
			try {
				ConfigurationSection sekcja = config.sekcja(klucz);
				
				String rezult = sekcja.getString("rezult");
				boolean domyślna = sekcja.getBoolean("domyślna", true);
				
				List<List<Krotka<String, Integer>>> matrixList = new ArrayList<>();
				sekcja.getStringList("matrix").forEach(linia -> {
					List<Krotka<String, Integer>> lista = new ArrayList<>();
					matrixList.add(lista);
					List<String> elementy = Func.tnij(linia, " ");
					for (int i=0; i < elementy.size(); i += 2) {
						int ilość = Func.Int(elementy.get(i + 1));
						String item = elementy.get(i);
						lista.add(new Krotka<>(item, ilość));
					}
				});
				
				String[][] matrix = new String[matrixList.size()][matrixList.get(0).size()];
				int[][]    ilości = new int[matrix.length][matrix[0].length];
				for (int y = 0; y < matrix.length; y++) {
					List<Krotka<String, Integer>> lista = matrixList.remove(0);
					for (int x = 0; x < matrix[0].length; x++) {
						Krotka<String, Integer> krotka = lista.remove(0);
						matrix[y][x] = krotka.a.equals("-") ? null : krotka.a;
						ilości[y][x] = krotka.a.equals("-") ? 0    : krotka.b;
					}
					if (!lista.isEmpty())
						throw new IllegalArgumentException();
				}
				if (!matrixList.isEmpty())
					throw new IllegalArgumentException();
				
				new Recepta(domyślna, rezult, matrix, ilości);
			} catch (Throwable e) {
				Main.warn("problem z craftingiem rpg: " + klucz);
			}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane craftingi RPG", mapaZId.size());
	}
}
