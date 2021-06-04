package me.jomi.mimiRPG.RPG_Ultra;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

public class Enchant {
	static final Map<String, Enchant> mapa = new HashMap<>();
	
	public final String nazwa;
	public final String opis;
	public final int[] ceny;
	
	public Enchant(String nazwa, String opis, int... ceny) {
		this.nazwa = nazwa;
		this.opis = opis;
		this.ceny = ceny;
		
		mapa.put(nazwa, this);
	}

	
	static NBTTagCompound enchanty(ItemStack item) {
		return ZfaktoryzowaneItemy.tag(item).getCompound("enchanty");
	}
	public static int getEnchantLvl(ItemStack item, String enchant) {
		return enchanty(item).getInt(enchant);
	}
	private static final Enchant pusty = new Enchant("", "");
	public static Enchant getEnchant(String enchant) {
		return mapa.getOrDefault(enchant, pusty);
	}
	public static Iterable<String> getEnchanty() {
		return mapa.keySet();
	}
}
