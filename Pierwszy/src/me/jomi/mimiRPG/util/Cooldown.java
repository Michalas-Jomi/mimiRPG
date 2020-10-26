package me.jomi.mimiRPG.util;

import java.util.HashMap;

public class Cooldown {
	private final HashMap<String, Long> mapa = new HashMap<>();
	
	int domyślny;
	
	public Cooldown(int sekundy) {
		this.domyślny = sekundy * 1000;
	}
	
	public void ustaw(String czemu) {
		mapa.put(czemu, System.currentTimeMillis());
	}
	public void wyczyść() {
		mapa.clear();
	}
		
	public boolean minął(String czemu) {
		return minąłMiliSek(czemu, domyślny);
	}
	public boolean minąłMiliSek(String czemu, int miliSekundy) {
		boolean w = ileJeszczeMiliSek(czemu) < 0;
		if (w) mapa.remove(czemu);
		return w;
	}
	public boolean minął(String czemu, int sekundy) {
		return minąłMiliSek(czemu, sekundy*1000);
	}

	public void anuluj(String czemu) {
		mapa.remove(czemu);
	}
	
	public int ileJeszcze(String czemu) {
		return ileJeszcze(czemu, domyślny / 1000);
	}
	public int ileJeszcze(String czemu, int sekundy) {
		return ileJeszczeMiliSek(czemu, sekundy) / 1000;
	}
	public int ileJeszczeMiliSek(String czemu) {
		return ileJeszczeMiliSek(czemu, domyślny);
	}
	public int ileJeszczeMiliSek(String czemu, int miliSekundy) {
		return (int) (mapa.getOrDefault(czemu, 0L) + miliSekundy - System.currentTimeMillis());
	}
	
	public String czas(String czemu) {
		return Func.czas(ileJeszcze(czemu));
	}

	public String toString() {
		return "Cooldown(" + (domyślny / 1000) + ")";
	}
}
