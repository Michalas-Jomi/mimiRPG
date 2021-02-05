package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class SprawdzanieIP implements Listener {
	public static final String prefix = Func.prefix("IP");
	static final Config config = new Config("configi/sprawdzanieIP");
	final String perm = Func.permisja("sprawdzanieip");
	
	public SprawdzanieIP() {
		Main.dodajPermisje(perm);
	}
	
	@EventHandler
	public void dołączanie(PlayerJoinEvent ev) {
		String ip = ev.getPlayer().getAddress().getAddress().getHostAddress();
		
		List<String> lista = config.wczytajListe(ip);
		if (lista.contains(ev.getPlayer().getName()))
			return;
		else {
			lista.add(ev.getPlayer().getName());
			if (lista.size() > 1) {
				String msg = prefix + Func.msg("Wykryto u %s %s kont %s", ev.getPlayer().getName(), lista.size(), lista);
				Main.log(msg);
				Bukkit.getOnlinePlayers().forEach(p -> {
					if (p.hasPermission(perm))
						p.sendMessage(msg);
				});
			}
			config.ustaw_zapisz(ip, lista);
		}
	}
	
	
	
}
