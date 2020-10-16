package me.jomi.mimiRPG.Bazy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Mapowalne;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Gracze.Gracz;

public class Gildia extends Mapowalne {
	public static final String prefix = Func.prefix("Gildia");
	public Gildia(Map<String, Object> mapa) {
		super(mapa);
	}

	@Mapowane List<String> gracze = Lists.newArrayList();
	@Mapowane String przywódca;
	@Mapowane String nazwa;
	
	static final Config config = new Config("configi/Gildie");
	void zapisz() {
		config.ustaw_zapisz(nazwa, this);
	}
	static Gildia wczytaj(String nazwa) {
		if (nazwa == null) return null;
		return (Gildia) config.wczytaj(nazwa);
	}
	static Gildia stwórz(String nazwa, String przywódca) {
		Gildia gildia = new Gildia(null);
		gildia.przywódca = przywódca;
		gildia.nazwa = nazwa;
		gildia.zapisz();
		Gracz g = Gracz.wczytaj(przywódca);
		g.gildia = nazwa;
		g.zapisz();
		return gildia;
	}
		
	static boolean istnieje(String nazwa) {
		for (String klucz : config.klucze(false))
			if (klucz.equalsIgnoreCase(nazwa))
				return true;
		return false;
	}
	
	void dołącz(Player p) {
		Gracz g = Gracz.wczytaj(p.getName());
		
		// dodanie gracza do regionów gildi
		for (String członek : gracze)
			for (ProtectedRegion region : bazy(Gracz.wczytaj(członek)))
				region.getMembers().addPlayer(p.getName());
		
		// dodanie regionów gracza do regionów gildi
		for (ProtectedRegion region : bazy(g))
			for (String członek : gracze)
				region.getMembers().addPlayer(członek);
		
		// dodanie gracza do gildi
		gracze.add(p.getName());
		zapisz();
		g.gildia = nazwa;
		g.zapisz();
		
	}
	private List<ProtectedRegion> bazy(Gracz g) {
		List<ProtectedRegion> lista = Lists.newArrayList();
		for (Entry<String, List<String>> en : g.bazy.entrySet()) {
			World świat = Bukkit.getWorld(en.getKey()); if (świat == null) continue;
			RegionManager manager = Bazy.inst.regiony.get(BukkitAdapter.adapt(świat));
			for (String regionId : en.getValue()) {
				ProtectedRegion region = manager.getRegion(regionId);
				if (region != null)
					lista.add(region);
			}
		}
		return lista;
	}
	void opuść(String nick) {
		Gracz g = Gracz.wczytaj(nick);
		
		// opuszczenie regionów gildijnych
		for (String członek : gracze)
			for (ProtectedRegion region : bazy(Gracz.wczytaj(członek)))
				region.getMembers().removePlayer(nick);
		
		// oddzielenie regionów gracza od gildi
		for (ProtectedRegion region : bazy(g))
			for (String członek : gracze)
				region.getMembers().removePlayer(członek);
		
		// opuszczenie gilidi
		gracze.remove(nick);
		zapisz();
		g.gildia = null;
		g.zapisz();
		
		// usunięcie gildi, lub przekazanie przywódctwa
		if (nick.equals(przywódca)) {
			if (gracze.size() == 0) {
				// usunięcie gildi
				config.ustaw_zapisz(nazwa, null);
			} else {
				// przekazanie przywódctwa
				przywódca = gracze.remove(0);
			}
		}
	}
	
	void napiszDoCzłonków(Player kto, String msg) {
		Set<Player> set = Sets.newConcurrentHashSet();
		
		Consumer<String> dodaj = nick -> {
			Player p = Bukkit.getPlayer(nick);
			if (p != null) 
				set.add(p);
		};
		
		for (String nick : gracze) 
			dodaj.accept(nick);
		dodaj.accept(przywódca);
		
		// TODO sprawdzić czy sie wyświetli, czy trzeba samodzielnie
		Event event = new AsyncPlayerChatEvent(true, kto, msg, set);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
	void wyświetlCzłonkom(String msg) {
		Consumer<String> wyświetl = nick -> {
			Player p = Bukkit.getPlayer(nick);
			if (p != null) 
				p.sendMessage(msg);
		};
		
		for (String nick : gracze) 
			wyświetl.accept(nick);
		wyświetl.accept(przywódca);
		
	}

}
