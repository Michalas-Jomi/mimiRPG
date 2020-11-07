package me.jomi.mimiRPG.SkyBlock;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.api.IslandWorthCalculatedEvent;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;


import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class RangiWysp extends Komenda implements Przeładowalny, Listener {
	public RangiWysp() {
		super("rangiWysp");
	}

	public static boolean warunekModułu() {
		return Main.iridiumSkyblock && Main.chat != null;
	}
	
	String ranga(double pkt) {
		String w = "";
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
		return Func.koloruj(w);
	}

	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void liczeniePkt(IslandWorthCalculatedEvent ev) {
		Island is = ev.getIsland();
		String owner = is.getOwner();
		String aktsuff = Main.chat.getPlayerSuffix(null, Func.graczOffline(owner));
		
		String suff = ranga(ev.getIslandWorth());
		if (suff.equals(aktsuff))
			return;
		
		Consumer<String> ustawRange = nick ->
				Main.chat.setPlayerSuffix(null, Func.graczOffline(nick), suff);
		
		ustawRange.accept(owner);
		for (String member : is.getMembers())
			ustawRange.accept(member);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void usuwanieWyspy(IslandDeleteEvent ev) {
		Island is = ev.getIsland();
		String owner = is.getOwner();
		
		Consumer<String> ustawRange = nick ->
				Main.chat.setPlayerSuffix(null, Func.graczOffline(nick), null);
		
		ustawRange.accept(owner);
		for (String member : is.getMembers())
			ustawRange.accept(member);
	}
	
	
	@Override public void przeładuj() {}
	@Override public Krotka<String, Object> raport() {
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		return Func.r("Rangi wysp", (sekcja == null ? 0 : (sekcja.getKeys(false).size() - (sekcja.contains("prefix") ? 1 : 0))));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		if (sekcja == null)
			return Func.powiadom(sender, "Nie ma żadnych rang wysp");
		String prefix = "§6" + sekcja.getString("prefix", "§a§l ");
		sender.sendMessage("§a§n|< §c-§l<>§c- §6Rangi wysp §c-§l<>§c- §a§n>|");
		for (Entry<String, Object> entry : sekcja.getValues(false).entrySet())
			if (!entry.getKey().equals("prefix"))
				sender.sendMessage(prefix + entry.getKey() + "§8: §e" + Func.DoubleToString(Func.Double(entry.getValue())));
		return true;
	}
}




