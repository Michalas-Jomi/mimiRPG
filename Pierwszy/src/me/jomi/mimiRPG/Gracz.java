package me.jomi.mimiRPG;

import java.io.File;
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

	@Mapowane public HashMap<String, List<String>> osiągnięciaUkończoneKryteria  = new HashMap<>();
	
	@Mapowane public int wyspa = -1;
	
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
		config = new Config(scieżkaConfigu(nick));
		if (Bukkit.getPlayer(nick) != null)
			mapa.put(nick, config);
		return config;
	}
 	private static String scieżkaConfigu(String nick) {
 		return "configi/gracze/" + nick + ".yml";
 	}
	
	public Gracz() {}
	private Gracz(String nick) {
		this.nick = nick;
	}
	public static Gracz wczytaj(String nick) {
		if (new File(scieżkaConfigu(nick)).exists())
			return config(nick).wczytajLubDomyślna("gracz", () -> new Gracz(nick));
		else
			return new Gracz(nick);
	}
	public static Gracz wczytaj(Player p) {
		return wczytaj(p.getName());
	}
}
