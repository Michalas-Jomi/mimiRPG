package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.MineZ.Wilczek;
import me.jomi.mimiRPG.Minigry.Minigra;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.util.Config;

public class Gracz extends Mapowany {
	@Mapowane public String nick;
	@Mapowane public int dropPoŚmierci;
	@Mapowane public String kolorPisania = "";
	@Mapowane public List<String> przyjaciele = Lists.newArrayList();
	@Mapowane public List<ItemStack> plecak = Lists.newArrayList();
	
	@Mapowane public Koniki.Kon kon;
	
	@Mapowane public HashMap<String, Minigra.Statystyki> staty = new HashMap<>();
	
	@Mapowane public Location łóżkoBazowe;
	@Mapowane public Bazy.Baza baza;
	@Mapowane public String gildia;
	
	@Mapowane public HashMap<String, String> superItemy = new HashMap<>();
	
	public Wilczek.Wilk wilk;
	
 	public void zapisz() {
		config(nick).ustaw_zapisz("gracz", this);
	}
 	
 	static final HashMap<String, Config> mapa = new HashMap<>();
	private static Config config(String nick) {
		nick = nick.toLowerCase();
		Config config = mapa.get(nick);
		if (config != null)
			return config;
		config = new Config("configi/gracze/" + nick);
		if (Bukkit.getPlayer(nick) != null)
			mapa.put(nick, config);
		return config;
	}
 	
	public Gracz() {}
	private Gracz(String nick) {
		this.nick = nick;
	}
	public static Gracz wczytaj(String nick) {
		return config(nick).wczytajLubDomyślna("gracz", new Gracz(nick));
	}
	public static Gracz wczytaj(Player p) {
		return wczytaj(p.getName());
	}
}
