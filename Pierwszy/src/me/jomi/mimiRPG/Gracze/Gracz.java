package me.jomi.mimiRPG.Gracze;

import java.util.List;

import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Config;

public class Gracz {
	public Config config;
	public Player p;
	
	public int dropPoŒmierci;
	public String kolorPisania;
	public List<String> przyjaciele;
	
	public Kon koñ;
	
	public Gracz(Player p) {
		this.p = p;
		
		config = new Config("gracze/" + p.getName());
		
		dropPoŒmierci = (int) config.wczytajLubDomyœlna("dropPoŒmierci", 0);
		kolorPisania  = (String) config.wczytajLubDomyœlna("kolorPisania", "");
		przyjaciele   = config.wczytajListe("przyjaciele");
		
		koñ = new Kon(this,
				(boolean) config.wczytajLubDomyœlna("koñ.bezg³oœny", false),
				(boolean) config.wczytajLubDomyœlna("koñ.ma³y", false),
				(String) config.wczytajLubDomyœlna("koñ.kolor", "Bia³y"),
				(String) config.wczytajLubDomyœlna("koñ.styl", "Brak"),
				(int) config.wczytajLubDomyœlna("koñ.zapas", -1));
	}
}
