package me.jomi.mimiRPG.JednorekiBandyta;

import java.util.List;

import org.bukkit.Material;

import me.jomi.mimiRPG.Config;

public class Wygrana {
	@SuppressWarnings("unchecked")
	public static Wygrana wczytaj(Config config, String klucz) {
		List<Integer> lista = (List<Integer>) config.wczytaj(klucz);
		return new Wygrana(
				Material.valueOf(klucz.substring(klucz.indexOf('.')+1)),
				lista.get(0), // szansa
				lista.get(1)  // wygrana
				);
	}
	public int szansa;
	public int wygrana;
	public Material blok;
	public Wygrana(Material blok, int szansa, int wygrana) {
		this.blok = blok;
		this.szansa = szansa;
		this.wygrana = wygrana;
	}

	public String toString() {
		return blok.toString().toLowerCase() + " " + szansa + "% " + wygrana + "$";
	}

}
