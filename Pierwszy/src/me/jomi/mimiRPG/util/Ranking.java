package me.jomi.mimiRPG.util;

import java.util.List;
import java.util.function.Supplier;

import me.jomi.mimiRPG.Mapowany;

public class Ranking extends Mapowany {
	public static class Rank extends Mapowany {
		@Mapowane String wyświetlane;
		@Mapowane String klucz;
		@Mapowane int wartość;
		
		public Rank() {}
		public Rank(int wartość, String klucz, String wyświetlane) {
			this.wyświetlane = wyświetlane;
			this.wartość = wartość;
			this.klucz = klucz;
		}
	}
	@Mapowane public int sloty = 10;
	@Mapowane List<Rank> topka;
	
	public boolean dodaj(int wartość, String klucz, String wyświetlane) {
		return dodaj(wartość, klucz, () -> wyświetlane);
	}
	public boolean dodaj(int wartość, String klucz, Supplier<String> wyświetlane) {
		int i=0;
		boolean res = false;
		for (; i < topka.size(); i++) {
			Rank rank = topka.get(i);
			if (rank.wartość < wartość) {
				topka.add(i, new Rank(wartość, klucz, wyświetlane.get()));
				return true;
			} else if (rank.klucz.equals(klucz)) {
				topka.remove(i--);
				res = true;
			}
		}
		if (topka.size() < sloty) {
			topka.add(new Rank(wartość, klucz, wyświetlane.get()));
			return true;
		}
		
		
		return res;
	}
}
