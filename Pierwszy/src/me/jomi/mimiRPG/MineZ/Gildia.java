package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Gracze.Gracz;

public class Gildia extends Mapowany {
	public static final String prefix = Func.prefix("Gildia");
	static final Config config = new Config("configi/Gildie");
	@Mapowane List<String> gracze;
	@Mapowane String przywódca;
	@Mapowane String nazwa;
	
	
	void zapisz() {
		config.ustaw_zapisz(nazwa, this);
	}
	static Gildia wczytaj(String nazwa) {
		if (nazwa == null) return null;
		return (Gildia) config.wczytaj(nazwa);
	}
	static Gildia stwórz(String nazwa, String przywódca) {
		Gracz g = Gracz.wczytaj(przywódca);
		
		if (g.gildia != null) {
			Player p = Bukkit.getPlayer(przywódca);
			p.sendMessage(prefix + "Nie możesz utworzyć nowej gildi puki nie opuścisz aktualnej");
			return null;
		}
		
		Gildia gildia = new Gildia();
		gildia.przywódca = przywódca;
		gildia.nazwa = nazwa;
		gildia.zapisz();;
		
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
		
		wykonajNaRegionach(g, DefaultDomain::addPlayer);
		
		gracze.add(p.getName());
		zapisz();
		
		g.gildia = nazwa;
		g.zapisz();
		
	}
	void opuść(String nick) {
		Gracz g = Gracz.wczytaj(nick);
		
		gracze.remove(nick);
		zapisz();
		
		g.gildia = null;
		g.zapisz();
		
		wykonajNaRegionach(g, DefaultDomain::removePlayer);
		
		if (nick.equals(przywódca))
			if (gracze.size() == 0)
				config.ustaw_zapisz(nazwa, null);
			else
				przywódca = gracze.remove(0);
	}	
	private void wykonajNaRegionach(Gracz g, BiConsumer<DefaultDomain, String> bic) {
		
		Consumer<String> cons = członek -> {
			for (ProtectedRegion region : bazy(Gracz.wczytaj(członek))) {
				DefaultDomain members = region.getMembers();
				bic.accept(members, g.nick);
				region.setMembers(members);
			}
		};
		for (String członek : gracze)
			cons.accept(członek);
		cons.accept(przywódca);
		
		
		for (ProtectedRegion region : bazy(g)) {
			DefaultDomain members = region.getMembers();
			for (String członek : gracze)
				bic.accept(members, członek);
			bic.accept(members, przywódca);
			region.setMembers(members);
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

	
}
