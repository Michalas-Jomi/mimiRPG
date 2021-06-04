package me.jomi.mimiRPG.RPG_Ultra;

import me.jomi.mimiRPG.Moduły.Moduł;

@Moduł
public enum TypItemu {
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
}
