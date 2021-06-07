package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.MultiConfig;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CraftingiRPG implements Przeładowalny, Listener {
	private static final Map<Integer, Recepta> mapaZId = new HashMap<>();
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
		}

		public boolean możeWycrafować(GraczRPG gracz) {
			return domyślna; // TODO pamięć receptór
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
	
	Panel panel = new Panel(false);
	public CraftingiRPG() {
		panel.ustawClick(ev -> {
			if (ev.getRawSlot() == 23) {
				// klikanie rezultu
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
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (Func.multiEquals(ev.getClick(), ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT) &&
				ev.getRawSlot() >= ev.getInventory().getSize() && panel.jestPanelem(ev.getInventory())) {
			odświeżRezult(ev);
		}
	}
	private static void odświeżRezult(InventoryClickEvent ev) {
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			String[][] matrix = new String[3][3];
			Inventory inv = ev.getInventory();
			for (int y=0; y < 3; y++)
				for (int x=0; x < 3; x++)
					matrix[y][x] = ZfaktoryzowaneItemy.id(inv.getItem((y + 1) * 9 + x + 1));
			// TODO usunąć puste lewe i górne paski
		});
	}
	
	
	@Override
	public void przeładuj() {
		MultiConfig config = new MultiConfig("craftingi");
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
