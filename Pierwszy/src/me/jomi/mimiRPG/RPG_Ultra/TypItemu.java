package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public enum TypItemu {
	BRAK,
	BLOK,
	NARZĘDZIE,
	ARMOR,
	BROŃ,
	
	BROŃ_BIAŁA(BROŃ),
	BROŃ_DYSTANSOWA(BROŃ),
	SPRZĘT(BROŃ, NARZĘDZIE),
	
	KILOF	(NARZĘDZIE),
	ŁOPATA	(NARZĘDZIE),
	MOTYKA	(NARZĘDZIE),
	NORZYCE	(NARZĘDZIE),
	WĘDKA	(NARZĘDZIE, BROŃ_BIAŁA),
	SIEKIERA(NARZĘDZIE, BROŃ_BIAŁA),
	MIECZ	(BROŃ_BIAŁA),
	ŁUK		(BROŃ_DYSTANSOWA),
	KUSZA	(BROŃ_DYSTANSOWA),
	
	HEŁM	(ARMOR),
	KLATA	(ARMOR),
	SPODNIE	(ARMOR),
	BUTY	(ARMOR);
	
	
	private final TypItemu[] sub;
	TypItemu(TypItemu... subtypy) {
		this.sub = subtypy;
	}
	
	public boolean pasuje(TypItemu typ) {
		if (typ == this)
			return true;
		for (TypItemu subtyp : sub)
			if (subtyp.pasuje(typ))
				return true;
		return false;
	}


	public static TypItemu typ(ItemStack item) {
		return typ(ZfaktoryzowaneItemy.tag(item));
	}
	static TypItemu typ(NBTTagCompound tag) {
		try {
			return Func.StringToEnum(TypItemu.class, tag.getString("typ_itemu"));
		} catch (Throwable e) {
			return TypItemu.BRAK;
		}
	}
	public static void ustawTyp(ItemStack item, TypItemu typ) {
		NBTTagCompound tag = ZfaktoryzowaneItemy.tag(item);
		ustawTyp(tag, typ);
		ZfaktoryzowaneItemy.ustawTag(item, tag);
	}
	static void ustawTyp(NBTTagCompound tag, TypItemu typ) {
		tag.setString("typ_itemu", typ.name());
	}
}
