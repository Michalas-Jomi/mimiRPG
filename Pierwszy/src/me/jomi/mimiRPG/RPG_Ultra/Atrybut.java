package me.jomi.mimiRPG.RPG_Ultra;

// ➳࿄◯࿊࿋࿌⍾⎈╳☕❉☢☣☤☠☫☬☯☮〄〠⌇༗⎙⺸⳥⳦⺡⻱﷽
public enum Atrybut {
	DEF_NIEZALEŻNY		("🛡", "3", "Absorbowany dmg"),
	KRYT_SZANSA			("✧", "e", "Kryt szansa"),
	KRYT_DMG			("✦", "e", "Kryt dmg"),
	UNIK				("⎌", "f", "Unik"),
	PRĘDKOŚĆ_KOPANIA	("⛏", "6", "prd. Kopania"),
	PRĘDKOŚĆ_CHODZENIA	("⏭", "6", "Szybkość"),
	HP					("❤", "c", "Zdrowie"),
	DEF					("🛡", "9", "Obrona"),
	SIŁA				("🗡", "c", "Siła"),
	PRĘDKOŚĆ_ATAKU		("⚡", "c", "prd. ataku"),
	SZCZĘŚCIE			("☘", "a", "Szczęście"),
	INTELIGENCJA		("❂", "b", "Inteligencja");
	
	public final String ikona;
	public final String kolor;
	private final String str;
	Atrybut(String ikona, String kolor, String nazwa) {
		this.ikona = ikona;
		this.kolor = "§" + kolor;
		
		this.str = this.kolor + this.ikona + " " + nazwa;
	}
	
	@Override
	public String toString() {
		return this.str;
	}
}
