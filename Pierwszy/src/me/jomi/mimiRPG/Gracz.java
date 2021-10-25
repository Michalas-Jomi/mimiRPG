package me.jomi.mimiRPG;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.MineZ.Wilczek;
import me.jomi.mimiRPG.Minigry.Minigra;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.PojedynczeKomendy.Zadania;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;

public class Gracz extends Mapowany {
	public static enum Język {
		POLSKI,
		ANGIELSKI;
	}
	@Mapowane public String nick;
	
	@Mapowane public Język język = Język.ANGIELSKI;
	
	@Mapowane public int dropPoŚmierci;
	@Mapowane public String kolorPisania = "";
	@Mapowane public List<String> przyjaciele = Lists.newArrayList();
	@Mapowane public List<ItemStack> plecak = Lists.newArrayList();
	
	@Mapowane public Koniki.Kon kon;
	
	@Mapowane public HashMap<String, Minigra.Statystyki> staty = new HashMap<>();
	
	@Mapowane public int BazaOstatnieStawianie;
	@Mapowane public Location łóżkoBazowe;
	@Mapowane public Bazy.Baza baza;
	@Mapowane public String gildia;
	
	@Mapowane public List<Integer> DzienneNagrodyodebrane = Lists.newArrayList();
	@Mapowane public int DzienneNagrodyOst;
	
	@Mapowane public int HitmanOstatnieZgłoszenie;
	
	@Mapowane public HashMap<String, String> superItemy = new HashMap<>();
	
	@Mapowane public Zadania.ZadaniaGracza zadania = Func.utwórz(Zadania.ZadaniaGracza.class);

	@Mapowane public String dailyAdv;
	
	@Mapowane public int wyspa = -1;
	@Mapowane public List<Integer> ulubioneWyspy;
	@Mapowane public List<Integer> polubioneWyspy;

	@Mapowane public HashMap<String, Long> bossyCooldown = new HashMap<>();
	@Mapowane public HashMap<String, Integer> bossyLicznik = new HashMap<>();
	
	@Mapowane public HashMap<String, Long> dungiCooldown = new HashMap<>();
	@Mapowane public HashMap<String, Integer> dungiLicznik = new HashMap<>();
	
	@Mapowane public int nasilanymute_licznik;
	
	@Mapowane public List<String> zbierankiZebrane = new ArrayList<>();
	
	@Mapowane public Wilczek.Wilk wilk;
	
	@Mapowane public boolean zarejestrowanyDiscord = false;
	
 	public void zapisz() {
		config(nick).ustaw_zapisz("gracz", this);
	}
 	
 	static final HashMap<String, Config> mapa = new HashMap<>();
	private static Config config(String nick) {
		nick = nick.toLowerCase();
		Config config = mapa.get(nick);
		if (config != null)
			return config;
		config = new Config(new File(scieżkaConfigu(nick)));
		if (Bukkit.getPlayer(nick) != null)
			mapa.put(nick, config);
		return config;
	}
 	private static String scieżkaConfigu(String nick) {
 		return Main.path + "configi/gracze/" + nick.toLowerCase() + ".yml";
 	}
	
	public Gracz() {}
	private Gracz(String nick) {
		this.nick = nick;
	}
	public static Gracz wczytaj(String nick) {
		if (new File(scieżkaConfigu(nick)).exists())
			return config(nick).wczytaj("gracz", () -> new Gracz(nick));
		else
			return new Gracz(nick);
	}
	public static Gracz wczytaj(OfflinePlayer p) {
		return wczytaj(p.getName());
	}
}
