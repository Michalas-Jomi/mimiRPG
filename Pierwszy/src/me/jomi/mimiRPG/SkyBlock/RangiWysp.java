package me.jomi.mimiRPG.SkyBlock;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.User;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

// TODO usunąć range przy usuwaniu wyspy

@Moduł
public class RangiWysp extends Komenda implements Przeładowalny, Listener {
	public RangiWysp() {
		super("przelicz");
		ustawKomende("rangiWysp", null, null);
	}

	public static boolean warunekModułu() {
		return Main.iridiumSkyblock && Main.chat != null;
	}
	
	void odśwież(Player p) {
		Island wyspa = User.getUser(p).getIsland();
		ustawRange(p, wyspa == null? "" : ranga(wyspa.getValue()));
	}
	
	String sprawdz(Player p) {
		Island wyspa = User.getUser(p).getIsland();
		if (wyspa == null) return "";
		
		String tytuł = ranga(wyspa.getValue());
		if (p.getDisplayName().endsWith(tytuł)) return "";
		
		ustawRange(p, tytuł);
		return tytuł;
	}
	
	void ustawRange(Player p, String tytuł) {
		String suff = Func.koloruj(Main.ust.wczytajLubDomyślna("RangiWysp.prefix", "§a§l ") + tytuł);
		for (World świat : Bukkit.getWorlds())
			Main.chat.setPlayerSuffix(świat.getName(), p, suff);
	}
	String ranga(double pkt) {
		String w = " ";
		double ost = -1;
		for (Entry<String, Object> en : Main.ust.sekcja("RangiWysp").getValues(false).entrySet()) {
			if (en.getKey().equals("prefix")) continue;
			try {
				double _pkt = Func.Double(en.getValue());
				if (_pkt <= pkt && _pkt > ost) {
					w = en.getKey();
					ost = _pkt;
				}
			} catch(Throwable e) {}
		}
		return w;
	}

	@Override
	public void przeładuj() {}
	@Override
	public Krotka<String, Object> raport() {
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		return Func.r("Rangi wysp", (sekcja == null ? 0 : (sekcja.getKeys(false).size() - (sekcja.contains("prefix") ? 1 : 0))));
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
		if (cmd.getName().equals("przelicz")) {
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
					return Func.powiadom(p, "§6Nie masz wyspy");
				return Func.powiadom(p, "§6Ranga wyspy: §e" + tytuł + " §e" + ((int) is.getValue()) + "pkt");
			}
			return Func.powiadom(sender, "Tylko gracz może tego użyć");
		} else {
			ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
			String prefix = "§6" + sekcja.getString("prefix", "§a§l ");
			sender.sendMessage("§a§n|< §c-§l<>§c- §6Rangi wysp §c-§l<>§c- §a§n>|");
			for (Entry<String, Object> entry : sekcja.getValues(false).entrySet())
				if (!entry.getKey().equals("prefix"))
					sender.sendMessage(prefix + entry.getKey() + "§8: §e" + Func.Double(entry.getValue()));
			return true;
		}
	}
}




