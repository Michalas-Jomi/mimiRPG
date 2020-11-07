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

import com.google.common.collect.Lists;
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

	private final List<Krotka<String, Double>> rangi = Lists.newArrayList();

	// EventHandler

	@EventHandler(priority = EventPriority.MONITOR)
	public void liczeniePkt(IslandWorthCalculatedEvent ev) {
		new Thread(() -> {
			Island is = ev.getIsland();
			String owner = is.getOwner();
			String aktsuff = Main.chat.getPlayerSuffix(null, Func.graczOfflineUUID(owner));
			
			String suff = ranga(ev.getIslandWorth());
			if (suff.equals(aktsuff))
				return;
			
			Consumer<String> ustawRange = uuid ->
					Main.chat.setPlayerSuffix(null, Func.graczOfflineUUID(uuid), suff);
			
			ustawRange.accept(owner);
			for (String member : is.getMembers())
				ustawRange.accept(member);
		}).start();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void usuwanieWyspy(IslandDeleteEvent ev) {
		new Thread(() -> {
			Island is = ev.getIsland();
			String owner = is.getOwner();
			
			Consumer<String> ustawRange = nick ->
					Main.chat.setPlayerSuffix(null, Func.graczOffline(nick), null);
			
			ustawRange.accept(owner);
			for (String member : is.getMembers())
				ustawRange.accept(member);
		}).start();
	}
	
	String ranga(double pkt) {
		String ost = rangi.isEmpty() ? "" : rangi.get(0).a;
		for (Krotka<String, Double> krotka : rangi) {
			if (krotka.b > pkt)
				break;
			ost = krotka.a;
		}
		return ost;
	}
	
	// Override
	
	@Override
	public void przeładuj() {
		String prefix = Func.nieNullStr(Main.ust.wczytajStr("RangiWysp.prefix"));
		rangi.clear();
		Double ost = null;
		boolean info = true;
		for (Entry<String, Object> en : Main.ust.sekcja("RangiWysp").getValues(false).entrySet())
			if (!en.getKey().equals("prefix")) {
				double w = Func.Double(en.getValue());
				if (info) {
					if (ost != null && ost > w) {
						Main.warn("Nieposortowane RangiWysp w ustawienia.yml. Należy je posortować dla poprawnego funkcjonowania");
						info = false;
					}
					ost = w;
				}
				rangi.add(new Krotka<>(prefix + en.getKey(), w));
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Rangi wysp", rangi.size());
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




