package me.jomi.mimiRPG.Gracze;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Mapowalne;
import me.jomi.mimiRPG.Mapowane;

public class Gracz extends Mapowalne {
	public Gracz(Map<String, Object> mapa) {
		super(mapa);
	}
	
	@Mapowane public String nick;
	@Mapowane public int dropPoŚmierci;
	@Mapowane public String kolorPisania = "";
	@Mapowane public List<String> przyjaciele = Lists.newArrayList();
	@Mapowane public List<ItemStack> plecak = Lists.newArrayList();
	// świat: lista id regionów
	@Mapowane public HashMap<String, List<String>> bazy = new HashMap<>();
	@Mapowane public String gildia;
	
	@Mapowane public Kon kon;	
	
	public void zapisz() {
		new Config("configi/gracze/" + nick).ustaw_zapisz("gracz", this);
	}

	private Gracz(String nick) {
		super(null);
		this.nick = nick;
	}
	public static Gracz wczytaj(String nick) {
		return new Config("configi/gracze/" + nick).wczytajLubDomyślna("gracz", new Gracz(nick));
	}

	public boolean posiadaGildie() {
		return gildia != null && !gildia.isEmpty();
	}
}
