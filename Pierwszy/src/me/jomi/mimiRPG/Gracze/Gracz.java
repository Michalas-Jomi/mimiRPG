package me.jomi.mimiRPG.Gracze;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Mapowane;

public class Gracz implements ConfigurationSerializable {
	public Gracz(Map<String, Object> mapa) {
		Func.zdemapuj(this, mapa);
	}
	@Override
	public Map<String, Object> serialize() {
		return Func.zmapuj(this);
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
		this.nick = nick;
	}
	public static Gracz wczytaj(String nick) {
		return new Config("configi/gracze/" + nick.toLowerCase()).wczytajLubDomyślna("gracz", new Gracz(nick));
	}

	public boolean posiadaGildie() {
		return gildia != null && !gildia.isEmpty();
	}
}
