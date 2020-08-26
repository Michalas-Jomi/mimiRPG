package me.jomi.mimiRPG.JednorekiBandyta;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;

public class Automat {
	public static final String prefix = JednorekiBandyta.prefix;
	
	private static int slotyG�ra = 1;
	private static int slotyD� = 1;
	private static int wczesneRolle = 20;
	private static int czekanieKrok = 2;
	private static int czekanieMin = 5;
	private static int czekanieMax = 20;
	private static int licznikMax = 5;
	public static void prze�aduj() {
		slotyG�ra = (int) Main.ust.wczytaj("JednorekiBandyta.slotyG�ra");
		slotyD� = (int) Main.ust.wczytaj("JednorekiBandyta.slotyD�");
		wczesneRolle = (int) Main.ust.wczytaj("JednorekiBandyta.wczesneRolle");
		czekanieKrok = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieKrok");
		czekanieMax = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieMax");
		czekanieMin = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieMin");
		licznikMax = (int) Main.ust.wczytaj("JednorekiBandyta.licznikMax");
	}
	
	public String nazwa;
	public Automat(String nazwa) {
		this.nazwa = nazwa;
	}
	
	private int koszt = 0;
	private Player gracz = null;
	private boolean broadcastWygrana = false;
	private boolean broadcastPrzegrana = false;
	public Location blokAktywacyjny;
	private List<Location> spiny;
	private List<Wygrana> wygrane = Lists.newArrayList();
	@SuppressWarnings("unchecked")
	public void wczytaj(Config config, String klucz) {
		if (klucz.contains("spiny"))
			spiny = (List<Location>) config.wczytaj(klucz);
		else if (klucz.contains("koszt"))
			this.koszt = (int) config.wczytaj(klucz);
		else if (klucz.contains("broadcastWygrana"))
			this.broadcastWygrana = (boolean) config.wczytaj(klucz);
		else if (klucz.contains("broadcastPrzegrana"))
			this.broadcastPrzegrana = (boolean) config.wczytaj(klucz);
		else if (klucz.contains("blokAktywacyjny"))
			this.blokAktywacyjny = (Location) config.wczytaj(klucz);
		else
			wygrane.add(Wygrana.wczytaj(config, klucz));
	}
	int mx = 0;
	public void wczytany() {
		wygrane = Lists.reverse(wygrane);
		for (Wygrana wygrana : wygrane) {
			wygrana.szansa += mx;
			mx = wygrana.szansa;
		}
	}
	
	public void graj(Player p) {
		if (!p.hasPermission("mimiRPG.automat.graj")) {
			p.sendMessage(prefix + "Nie masz uprawnie� do gry na automatach");
			return;
		}
		if (gracz != null) {
			if (!gracz.getName().equals(p.getName()))
				p.sendMessage(prefix + "Aktualnie gra tu �e" + gracz.getDisplayName());
			return;
		}
		double kasa = Main.econ.getBalance(p);
		if (kasa < koszt) {
			p.sendMessage(prefix + "Nie sta� ci� na ten automat");
			return;
		}
		Main.econ.withdrawPlayer(p, koszt);
		
		gracz = p;
		zakr��();
	}
	private void koniec() {
		gracz = null;
	}
	
	private boolean podlicz() {
		Material ost = spiny.get(0).getBlock().getType();
		
		for (Location spin: spiny)
			if (!spin.getBlock().getType().equals(ost))
				return przegrana();
		return wygrana(ost);
	}
	private boolean wygrana(Material mat) {
		gracz.sendMessage(prefix + "�aWygra�e�");
		
		for (Wygrana wygrana : wygrane)
			if (wygrana.blok.equals(mat)) {
				final String msg = gracz.getDisplayName() + "�a wygra� �6w kasynie �e" + Func.DoubleToString(koszt) + "$";
				if (broadcastWygrana)
					Bukkit.broadcastMessage(prefix + msg);
				else {
					Main.log("[Automat] " + msg);
					gracz.sendMessage(prefix + "Wygra�e� �e" + Func.DoubleToString(wygrana.wygrana) + "$");
				}
				gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1);
				Main.econ.depositPlayer(gracz, wygrana.wygrana).transactionSuccess();
				koniec();
				return true;
			}
		koniec();
		return false;
	}
	private boolean przegrana() {
		gracz.sendMessage(prefix + "�cPrzegra�e�");
		
		final String msg = gracz.getDisplayName() + "�c przegra� �6w kasynie �e" + Func.DoubleToString(koszt) + "$";
		if (broadcastPrzegrana)
			Bukkit.broadcastMessage(prefix + msg);
		else
			Main.log("[Automat] " + msg);
		
		gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 1);
		koniec();
		return false;
	}
	
	private Material losujBlok() {
		int liczba = Func.losuj(0, wygrane.get(wygrane.size()-1).szansa);
		
		for (int i=0; i < wygrane.size(); i++)
			if (liczba <= wygrane.get(i).szansa)
				return wygrane.get(i).blok;
		
		Main.log(prefix + "Jest b��d w dzia�aniach, Jednor�kiBandyta -> Automat -> losujBlok\n wylosowana: " + liczba);
		return null;
	}
	private void zakr��() {
		gracz.sendMessage(prefix + "Grasz o �e" + koszt + "$");
		zakr��Wszystkie(wczesneRolle);
	}
	// zakr�ca kilka razy na maksymalnej szybko�ci
	private void zakr��Wszystkie(int licznik) {
		zakr��Kilka(spiny.size());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (licznik <= 0)
		    		_zakr��Wszystkie(czekanieMin);
		    	else
		    		zakr��Wszystkie(licznik - 1);
		    }
		}, czekanieMin);
	}
	// zakr�canie coraz to wolniej
	private void _zakr��Wszystkie(int czekanie) {
		zakr��Kilka(spiny.size());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (czekanie >= czekanieMax)
		    		zakr��(spiny.size(), czekanie, licznikMax);
		    	else
		    		_zakr��Wszystkie(czekanie + czekanieKrok);
		    }
		}, czekanie);
		
	}
	// ostateczne losowanie
	private void zakr��(int ile, int czekanie, int licznik) {
		if (ile <= 0) {
			podlicz();
			return;
		}
		
		zakr��Kilka(ile);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (licznik <= 0) {
		    		gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1);
			    	zakr��(ile - 1, czekanie, licznikMax);
		    	}
		    	else
		    		zakr��(ile, czekanie, licznik - 1);
		    }
		}, czekanie);
	}
	private void zakr��Kilka(int ile) {
		for (int i=0; i<spiny.size(); i++) {
			if (i >= ile) break;
			zakr��Jeden(spiny.get(i));
			gracz.getWorld().playSound(blokAktywacyjny, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
		}
	}
	private void zakr��Jeden(Location spin) {
		spin = spin.clone();
		spin.add(0, -slotyD�, 0);
		
		for (int i=0; i < slotyD� + slotyG�ra; i++) {
			Block b1 = spin.getBlock();
			Block b2 = spin.add(0, 1, 0).getBlock();
			b1.setType(b2.getType());
		}
		
		spin.getBlock().setType(losujBlok());
	}
}
