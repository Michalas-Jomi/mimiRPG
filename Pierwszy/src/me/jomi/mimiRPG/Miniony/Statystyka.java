package me.jomi.mimiRPG.Miniony;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;

public class Statystyka {
	private static final HashMap<String, Statystyka> mapa = new HashMap<>();
	private Statystyka(String nazwa, double start, double krok, double max, double cena, double cenaKrok) {
		this.cenaKrok = cenaKrok;
		this.nazwa = nazwa;
		this.start = start;
		this.krok = krok;
		this.cena = cena;
		this.max = max;
		mapa.put(nazwa, this);
	}
	private static void przepisz(Statystyka co, Statystyka gdzie) {
		gdzie.cenaKrok = co.cenaKrok;
		gdzie.start = co.start;
		gdzie.krok = co.krok;
		gdzie.max = co.max;
	}
	
	public static void ZainicjujStatystyki() {
		new Statystyka("ZużycieJedzenia", 3, -.02, .2, 5000, 500);
		new Statystyka("Prędkość", 		100, -5, 20, 20000, 10000);
		new Statystyka("ZużycieWody", 	40, -1, 5, 2000, 1000);
		new Statystyka("dmg", .5, .2, 5, 5000, 20000);
	}
	
	public String nazwa;
	public double start;
	public double krok;
	public double max;
	
	public double cenaKrok;
	public double cena;
	
	public double akt;
	public double nst;
	
	public Statystyka(String str) {
		String[] parametry = str.split(": ");
		nazwa = parametry[0];
		przepisz(mapa.get(nazwa), this);
		akt = start;
		if (parametry.length > 1)
			akt = Func.Double(parametry[1], -1);
		cena = (akt - start) / krok;
		nst = akt + krok;
	}
	public Statystyka(Config config, String nazwa) {
		this.nazwa = nazwa;
		
		przepisz(mapa.get(nazwa), this);
		akt   = (double) config.wczytajLubDomyślna("stat." + nazwa, start);
		cena = (akt - start) / krok;
		nst = akt + krok;
	}
	public void zapisz(Config config) {
		config.ustaw("stat." + nazwa, akt);
	}
	
	public boolean ulepsz(Player p) {
		if ((akt >= max && nst < max) || (akt <= max && nst > max)) 
			return false;
		double kasa = Main.econ.getBalance(p);
		if (kasa >= cena) {
			Main.econ.withdrawPlayer(p, cena);
			cena += cenaKrok;
			akt += krok;
			nst = akt + krok;
			return true;
		}
		p.sendMessage(Minion.prefix + "Nie stać cię na to");
		return false;
	}

	public String strCena() {
		if ((akt >= max && nst < max) || (akt <= max && nst > max)) 
			return "§6Osiągnięto maksymalny poziom";
		return "§3Koszt ulepszenia: §e" + Func.DoubleToString(cena) + "$";
	}
	
	public String str() {
		return Func.DoubleToString(Func.zaokrąglij(akt, 2));
	}
	public String str2() {
		if ((akt >= max && nst < max) || (akt <= max && nst > max))
			return "§6§lMAX";
		return Func.DoubleToString(Func.zaokrąglij(nst, 2));
	}
	
	
}
