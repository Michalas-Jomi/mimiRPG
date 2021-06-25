package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.AutoString;
import me.jomi.mimiRPG.util.Func;

public class DropRPG extends AutoString {
	public static final DropRPG pusty = new DropRPG(Func.stwórzItem(Material.AIR), 1, 1, 1);
	
	public final List<DropRPG> dropy;
	
	public final ItemStack item;
	public final double szansa;
	public final int min_ilość;
	public final int max_ilość;

	public DropRPG(ItemStack item, double szansa, int min_ilość, int max_ilość) {
		dropy = null;
		this.max_ilość = max_ilość;
		this.min_ilość = min_ilość;
		this.szansa = szansa;
		this.item = Objects.requireNonNull(item, "item dropu rpg nie może być niczym");
	}
	private DropRPG() {
		item = null;
		szansa = 1;
		min_ilość = 1;
		max_ilość = 1;
		dropy = new ArrayList<>();
	}
	public static DropRPG parse(String drop) {
		if (drop == null)
			return pusty;
		List<String> części = Func.tnij(drop, " ");
		if (części.size() > 3) {
			DropRPG wynik = new DropRPG();
			for (int i=0; i < części.size(); i += 3) {
				StringBuilder strB = new StringBuilder();
				for (int j = 0; j < 3; j++)
					strB.append(części.get(i + j)).append(j == 2 ? '-' : ' ');
				wynik.dropy.add(parse(strB.toString()));
			}
			return wynik;
		}
		List<String> min_max = Func.tnij(części.get(2), "-");
		try {
			return new DropRPG(
					ZfaktoryzowaneItemy.dajItem(części.get(0)),
					Func.Double(części.get(1)),
					Func.Int(min_max.get(0)),
					Func.Int(min_max.get(1))
					);
		} catch (Throwable e) {
			Main.warn(Func.prefix("Drop RPG") + "Niepoprawny dropRPG " + e.getMessage() + ": " + drop);
			return pusty;
		}
	}
	
	public List<ItemStack> dropnij() {
		List<ItemStack> itemy = new ArrayList<>();
		if (dropy != null)
			dropy.forEach(drop -> drop.dropnij(itemy));
		else if (Func.losuj(szansa))
			itemy.add(Kolekcja.oznakujItem(Func.ilość(item.clone(), Func.losuj(min_ilość, max_ilość))));
		return itemy;
	}
	public boolean dropnij(List<ItemStack> dropy) {
		List<ItemStack> itemy = dropnij();
		dropy.addAll(itemy);
		return !itemy.isEmpty();
	}
	public boolean dropnij(Location loc) {
		List<ItemStack> itemy = dropnij();
		itemy.forEach(item -> loc.getWorld().dropItem(loc, item));
		return !itemy.isEmpty();
	}

	private String str;
	@Override
	public String toString() {
		if (str == null) {
			StringBuilder strB = new StringBuilder();
			
			strB.append(Ranga.ranga(item).kolor);
			strB.append(Func.nazwaItemku(item));
			strB.append("§a");
			boolean nierówne = min_ilość != max_ilość;
			if (min_ilość != 1 || nierówne)
				strB.append(" x").append(min_ilość);
			if (nierówne)
				strB.append('-').append(max_ilość);
			
			if (szansa < 1)
				strB.append(' ').append(Func.DoubleToString(Func.zaokrąglij(szansa * 100, 2))).append('%');
			
			str = strB.toString();
			if (str.contains("&%"))
				str = Func.koloruj(str);
			
		}
		return str;
	}
}
