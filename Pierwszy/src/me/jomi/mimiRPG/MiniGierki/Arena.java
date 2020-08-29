package me.jomi.mimiRPG.MiniGierki;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;

public class Arena {
	public List<Player> gracze = Lists.newArrayList();
	public List<Boolean> głosy = Lists.newArrayList();
	public boolean sprawdzoneLokacje = false;
	public Location start;
	public Location koniec;
	public int zmienna;
	
	public boolean grane = false;
	
	public static Arena wczytaj(String sciezka, Config config) {
		sciezka += '.';
		Location start = (Location) config.wczytaj(sciezka + "start");
		Location koniec = (Location) config.wczytaj(sciezka + "koniec");
		if (start == null || koniec == null)
			return null;
		return new Arena(start, koniec);
	}
	public Arena(Location start, Location koniec) {
		this.start = start;
		this.koniec = koniec;
	}
	
	public String toString() {
		return "Arena(\n\tstart: " + start + ",\n\tkoniec: " + koniec + "\n)";
	}
	
	protected int znajdzGracza(Player p) {
		for (int i=0; i<gracze.size(); i++)
			if (gracze.get(i).getName().equals(p.getName())) 
				return i;
		return -1;
	}
	protected void policzGłosy(MiniGra gra) {
		if (grane) return;
		int g = 0;
		for (boolean b : głosy)
			if (b)
				g += 1;
		for (Player gracz : gracze)	{
			gracz.playSound(gracz.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, .4f, 1);
			gracz.sendMessage(gra.prefix + "Głosy na rozpoczęcie gry " + g + "/" + Math.max(głosy.size(), 2));
		}
		if (g == głosy.size() && g >= 2)
			gra.start(this);
	}
}
