package me.jomi.mimiRPG.util;

import java.util.HashMap;

// TODO sprawdzić

public class Cooldown {
	private final HashMap<String, Long> mapa = new HashMap<>();
	
	long domyślny;
	
	public Cooldown(int sekundy) {
		this.domyślny = sekundy * 1000;
	}
	
	public void ustawDomyślny(int sekundy) {
		domyślny = sekundy * 1000;
	}
	
	public boolean minął(String czemu) {
		return minąłMiliSek(czemu, domyślny);
	}
	public boolean minął(String czemu, int sekundy) {
		return minąłMiliSek(czemu, sekundy*1000);
	}
	public boolean minąłMiliSek(String czemu, long miliSekundy) {
		boolean w = ileJeszczeMiliSek(czemu) <= 0;
		if (w) mapa.remove(czemu);
		return w;
	}
	
	public boolean minąłToUstaw(String czemu) {
		return minąłMiliSekToUstaw(czemu.toString(), domyślny);
	}
	public boolean minąłToUstaw(String czemu, int sekundy) {
		return minąłMiliSekToUstaw(czemu, sekundy*1000);
	}
	public boolean minąłMiliSekToUstaw(String czemu, long miliSekundy) {
		boolean w = ileJeszczeMiliSek(czemu) <= 0;
		if (w) ustaw(czemu);
		return w;
	}
	
	

	
	public int ileJeszcze(String czemu) {
		return ileJeszczeMiliSek(czemu, domyślny) / 1000;
	}
	public int ileJeszcze(String czemu, int sekundy) {
		return ileJeszczeMiliSek(czemu, sekundy * 1000) / 1000;
	}
	
	public int ileJeszczeMiliSek(String czemu) {
		return ileJeszczeMiliSek(czemu, domyślny);
	}
	public int ileJeszczeMiliSek(String czemu, long miliSekundy) {
		if (!mapa.containsKey(czemu))
			return -1;
		return (int) (mapa.get(czemu) + miliSekundy - System.currentTimeMillis());
	}
	
	

	public void ustaw(String czemu) {
		mapa.put(czemu, System.currentTimeMillis());
	}
	public void anuluj(String czemu) {
		mapa.remove(czemu);
	}
	
	
	
	public String czas(String czemu) {
		return Func.czas(ileJeszcze(czemu));
	}
	@Override
	public String toString() {
		return "Cooldown(" + (domyślny / 1000) + ")";
	}
	public void wyczyść() {
		mapa.clear();
	}
}
