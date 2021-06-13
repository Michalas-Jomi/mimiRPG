package me.jomi.mimiRPG.RPG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.RPG.Enchant.PE;
import me.jomi.mimiRPG.util.Func;

public class Enchant<T extends PE> {
	// Poziom Enchantu
	public static class PE {
		public final int cena; // exp

		public PE(int cena) {
			this.cena = cena;
		}
	
		public String opis(String baza) {
			return baza;
		}
	}
	public static class PES extends PE {
		public final double szansa;
		
		public PES(int cena, double szansa) {
			super(cena);
			this.szansa = szansa;
		}
		
		@Override
		public String opis(String baza) {
			return super.opis(baza).replace("{szansa}", Func.DoubleToString(szansa * 100));
		}
		
		public boolean losuj() {
			return Func.losuj(szansa);
		}
	}
	public static class PEW extends PE {
		public final int wartość;
		
		public PEW(int cena, int wartość) {
			super(cena);
			this.wartość = wartość;
		}
		
		@Override
		public String opis(String baza) {
			return super.opis(baza).replace("{wartość}", Func.IntToString(wartość));
		}
		
		public double wartość() {
			return wartość(wartość);
		}
		static double wartość(int wartość) {
			return wartość / 100d;
		}
	}
	public static class PESW extends PES {
		public final int wartość;
		
		public PESW(int cena, double szansa, int wartość) {
			super(cena, szansa);
			this.wartość = wartość;
		}
		
		@Override
		public String opis(String baza) {
			return super.opis(baza).replace("{wartość}", Func.IntToString(wartość));
		}
		
		public double wartość() {
			return PEW.wartość(wartość);
		}
	}
	
	
	static final Map<String, Enchant<?>> mapa = new HashMap<>();
	
	public final String nazwa;
	private final String opis;
	private final T[] poziomy;
	public final TypItemu typItemu;

	@SuppressWarnings("unchecked")
	public Enchant(String nazwa, TypItemu typItemu, String opis, T... poziomy) {
		this.typItemu = typItemu;
		this.opis = opis;
		this.nazwa = nazwa;
		this.poziomy = poziomy;
		
		mapa.put(nazwa, this);
	}
	
	public String opis(ItemStack item) {
		return getPoziom(item).opis(opis);
	}
	public String opis(NBTTagCompound tag) {
		return getPoziom(tag).opis(opis);
	}
	public String opis(int lvl) {
		return getPoziom(lvl).opis(opis);
	}
	public String opis(T poziom) {
		return poziom.opis(opis);
	}
	
	public boolean maDostęp(GraczRPG gracz) {
		return true;
		// TODO pamięć
		//return gracz.dataPamięć.getCompound("enchanty").hasKey(nazwa);
	}
	public void nadajDostęp(GraczRPG gracz) {
		RPG.dataDajUtwórz(gracz.dataPamięć, "enchanty").setBoolean(nazwa, true);
	}
	
	public int getLvl(ItemStack item) {
		return getLvl(enchanty(item));
	}
	public int getLvl(NBTTagCompound tag) {
		return tag.getByte(nazwa);
	}
	
	public int getPoziomy() {
		return poziomy.length;
	}
	
	public T getPoziom(ItemStack item) {
		return getPoziom(enchanty(item));
	}
	public T getPoziom(NBTTagCompound tag) {
		return getPoziom(getLvl(tag));
	}
	public T getPoziom(int lvl) {
		if (lvl <= 0)
			return null;
		if (lvl > poziomy.length)
			lvl = poziomy.length;
		return poziomy[lvl - 1];
	}
	
	
	public void zaaplikuj(ItemStack item, int lvl) {
		enchanty(item).setByte(nazwa, (byte) lvl);
		ZfaktoryzowaneItemy.przerób(item);
	}
	public void odaplikuj(ItemStack item) {
		enchanty(item).remove(nazwa);
		ZfaktoryzowaneItemy.przerób(item);
	}
	
	public ItemStack ikona() {
		return ikona(poziomy.length, false);
	}
	public ItemStack ikona(int lvl) {
		return ikona(lvl, true);
	}
	public ItemStack ikona(int lvl, boolean pokażKoszt) {
		T poziom = getPoziom(lvl);
		
		ItemStack item = Func.stwórzItem(Material.ENCHANTED_BOOK, "§c" + nazwa, Func.tnij(opis(poziom), "\n"));
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		
		if (pokażKoszt) {
			lore.add(" ");
			lore.add("§7Koszt§8: §e" + poziom.cena + "§7 lvl");
		}
		
		meta.setCustomModelData(lvl);
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	
	static NBTTagCompound enchanty(ItemStack item) {
		return RPG.dataDajUtwórz(ZfaktoryzowaneItemy.tag(item), "enchanty");
	}
	static NBTTagCompound enchantyUnsafe(ItemStack item) {
		return ZfaktoryzowaneItemy.tag(item).getCompound("enchanty");
	}
	
	private static final Enchant<?> pusty = new Enchant<>("", TypItemu.BRAK, "Pusty enchanty, poinformuj o nim admina");
	public static Enchant<?> getEnchant(String enchant) {
		return mapa.getOrDefault(enchant, pusty);
	}
	public static Iterable<String> getNazwyEnchantów() {
		return mapa.keySet();
	}
	public static Iterable<Enchant<?>> getEnchanty() {
		return mapa.values();
	}
	public static int getIlośćEnchantów() {
		return mapa.size();
	}
}
