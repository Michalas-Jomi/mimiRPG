package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.util.Func;

public class Boost {
	public static List<Boost> getBoosty(ItemStack item) {
		return getBoosty(ZfaktoryzowaneItemy.tag(item));
	}
	static List<Boost> getBoosty(NBTTagCompound tag) {
		List<Boost> list = new ArrayList<>();
		
		NBTTagCompound boosty = tag.getCompound("boosty");
		
		boosty.getKeys().forEach(klucz -> list.add(new Boost(klucz, klucz.startsWith("baza_"), boosty.getDouble(klucz))));
		
		return list;
	}
	
	public static double getBoost(ItemStack item, Atrybut attr, boolean baza) {
		return getBoost(ZfaktoryzowaneItemy.tag(item), attr, baza);
	}
	static double getBoost(NBTTagCompound tag, Atrybut attr, boolean baza) {
		if (!tag.hasKey("boosty")) return 0;
		
		NBTTagCompound boosty = tag.getCompound("boosty");
		String klucz = (baza ? "baza" : "mn") + "_" + attr.name();
		
		return boosty.getDouble(klucz);
	}
	
	public static void dodajBoost(ItemStack item, Atrybut attr, double ile) {
		dodajBoost(item, attr, ile, true);
	}
	public static void dodajBoost(ItemStack item, Atrybut attr, double ile, boolean baza) {
		NBTTagCompound tag = ZfaktoryzowaneItemy.tag(item);
		dodajBoost(tag, attr, ile, baza);
		ZfaktoryzowaneItemy.ustawTag(item, tag);
	}
	static void dodajBoost(NBTTagCompound tag, Atrybut attr, double ile, boolean baza) {
		if (!tag.hasKey("boosty"))
			tag.set("boosty", new NBTTagCompound());
		
		NBTTagCompound boosty = tag.getCompound("boosty");
		String klucz = (baza ? "baza" : "mn") + "_" + attr.name();

		double akt = boosty.getDouble(klucz);
		boosty.setDouble(klucz, akt + ile);
	}

	
	public final Atrybut attr;
	public final boolean baza; // baza : mnożnik
	public final double wartość;
	
	public Boost(Atrybut attr, boolean baza, double wartość) {
		this.wartość = wartość;
		this.baza = baza;
		this.attr = attr;
	}
	Boost(String klucz, boolean baza, double wartość) {
		this(Func.StringToEnum(Atrybut.class, klucz.substring(baza ? 5 : 3)), baza, wartość);
	}
	
	
	private void __aplikuj(GraczRPG gracz, double wartość) {
		if (baza)
			gracz.statystyka(attr).zwiększBaza(wartość);
		else
			gracz.statystyka(attr).zwiększMnożnik(wartość);
		
	}
	public void zaaplikuj(GraczRPG gracz) {
		__aplikuj(gracz, wartość);
	}
	public void odaplikuj(GraczRPG gracz) {
		__aplikuj(gracz, -wartość);
	}
	
	
	@Override
		public String toString() {
		StringBuilder strB = new StringBuilder();
		
		strB.append(attr);
		strB.append(wartość < 0 ? " §c-" : " §a+");
		if (baza)
			strB.append((int) wartość);
		else
			strB.append(((int) ((wartość - 1) * 100)) + "%");
		
		return strB.toString();
	}
}