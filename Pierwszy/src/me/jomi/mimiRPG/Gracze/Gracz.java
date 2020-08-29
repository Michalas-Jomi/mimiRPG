package me.jomi.mimiRPG.Gracze;

import java.util.List;

import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Config;

public class Gracz {
	public Config config;
	public Player p;
	
	public int dropPoŚmierci;
	public String kolorPisania;
	public List<String> przyjaciele;
	
	public Kon koń;
	
	public Gracz(Player p) {
		this.p = p;
		
		config = new Config("gracze/" + p.getName());
		
		dropPoŚmierci = (int) config.wczytajLubDomyślna("dropPoŚmierci", 0);
		kolorPisania  = (String) config.wczytajLubDomyślna("kolorPisania", "");
		przyjaciele   = config.wczytajListe("przyjaciele");
		
		koń = new Kon(this,
				(boolean) config.wczytajLubDomyślna("koń.bezgłośny", false),
				(boolean) config.wczytajLubDomyślna("koń.mały", false),
				(String) config.wczytajLubDomyślna("koń.kolor", "Biały"),
				(String) config.wczytajLubDomyślna("koń.styl", "Brak"),
				(int) config.wczytajLubDomyślna("koń.zapas", -1));
	}
}
