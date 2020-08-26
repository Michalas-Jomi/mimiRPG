package me.jomi.mimiRPG.Gracze;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.jomi.mimiRPG.Prze³adowalny;

public class Gracze implements Listener, Prze³adowalny {
	public static final HashMap<String, Gracz> mapa = new HashMap<>();
	
	private void wczytajGracza(Player p) {
		Gracze.mapa.put(p.getName(), new Gracz(p));
	}
	public void prze³aduj() {
		mapa.clear();
		for (Player p : Bukkit.getOnlinePlayers())
			wczytajGracza(p);
	}
	public String raport() {
		return "§6Gracze online: §e" + mapa.size();
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void do³¹czaniedoGry(PlayerJoinEvent ev) {
		wczytajGracza(ev.getPlayer());
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void opuszczanieGry(PlayerQuitEvent ev) {
		mapa.remove(ev.getPlayer().getName());
	}

	public static Gracz gracz(String nick) {
		return mapa.get(nick);
	}
}


