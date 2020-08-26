package me.jomi.mimiRPG.Gracze;

import java.util.List;

import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Config;

public class Gracz {
	public Config config;
	public Player p;
	
	public int dropPo�mierci;
	public String kolorPisania;
	public List<String> przyjaciele;
	
	public Kon ko�;
	
	public Gracz(Player p) {
		this.p = p;
		
		config = new Config("gracze/" + p.getName());
		
		dropPo�mierci = (int) config.wczytajLubDomy�lna("dropPo�mierci", 0);
		kolorPisania  = (String) config.wczytajLubDomy�lna("kolorPisania", "");
		przyjaciele   = config.wczytajListe("przyjaciele");
		
		ko� = new Kon(this,
				(boolean) config.wczytajLubDomy�lna("ko�.bezg�o�ny", false),
				(boolean) config.wczytajLubDomy�lna("ko�.ma�y", false),
				(String) config.wczytajLubDomy�lna("ko�.kolor", "Bia�y"),
				(String) config.wczytajLubDomy�lna("ko�.styl", "Brak"),
				(int) config.wczytajLubDomy�lna("ko�.zapas", -1));
	}
}
