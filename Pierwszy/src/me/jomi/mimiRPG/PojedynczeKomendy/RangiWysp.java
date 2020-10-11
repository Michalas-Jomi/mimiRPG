package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.User;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class RangiWysp extends Komenda implements Przeładowalny, Listener {
	public RangiWysp() {
		super("przelicz");
	}

	public boolean warunekModułu() {
		return Main.iridiumSkyblock;
	}
	
	void odśwież(Player p) {
		Island wyspa = User.getUser(p).getIsland();
		ustawRange(p, wyspa == null? "" : ranga(wyspa.getValue()));
	}
	
	String sprawdz(Player p) {
		Island wyspa = User.getUser(p).getIsland();
		if (wyspa == null) return "";
		
		String tytuł = ranga(wyspa.getValue());
		if (p.getDisplayName().startsWith(tytuł)) return "";
		
		ustawRange(p, tytuł);
		return tytuł;
	}
	
	void ustawRange(Player p, String tytuł) {
		Main.chat.setPlayerSuffix(p, "§a§l " + Func.koloruj(tytuł));
	}
	String ranga(double pkt) {
		String w = " ";
		double ost = -1;
		for (Entry<String, Object> en : Main.ust.sekcja("RangiWysp").getValues(false).entrySet()) {
			double _pkt = Func.Double(en.getValue());
			if (_pkt <= pkt && _pkt > ost) {
				w = en.getKey();
				ost = _pkt;
			}
		}
		return w;
	}

	@Override
	public void przeładuj() {}
	@Override
	public String raport() {
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		return "§6Rangi wysp: §e" + (sekcja == null ? 0 : sekcja.getKeys(false).size());
	}
	
	@EventHandler
	public void dołączanie(PlayerJoinEvent ev) {
		odśwież(ev.getPlayer());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			String tytuł = sprawdz(p);
			Island is = User.getUser(p).getIsland();
			if (!tytuł.isEmpty())
				for (String nick : is.getMembers()) {
					if (p.getName() == nick) continue;
					Player p2 = Bukkit.getPlayer(nick);
					if (p2 == null) continue;
					ustawRange(p2, tytuł);
				}
			if (is == null)
				return Main.powiadom(p, "§6Nie masz wyspy");
			return Main.powiadom(p, "§6Ranga wyspy: §e" + tytuł + " §e" + ((int) is.getValue()) + "pkt");
		}
		return Main.powiadom(sender, "Tylko gracz może tego użyć");
	}
}




