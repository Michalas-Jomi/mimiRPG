package me.jomi.mimiRPG.SkyBlock;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.DołączanieDoWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.OpuszczanieWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.PrzeliczaniePunktówWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.UsuwanieWyspyEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class RangiWysp extends Komenda implements Przeładowalny, Listener {
	public RangiWysp() {
		super("rangiWysp");
	}
	public static boolean warunekModułu() {
		return Main.włączonyModół(SkyBlock.class) && Main.chat != null;
	}

	private final List<Krotka<String, Double>> rangi = Lists.newArrayList();

	// EventHandler
	
	/// api Modułu SkyBlock
	@EventHandler(priority = EventPriority.MONITOR)
	public void liczeniePkt(PrzeliczaniePunktówWyspyEvent ev) {
		new Thread(() -> {
			String jedenZCzłonków = Func.losuj(ev.wyspa.członkowie.keySet());
			String aktsuff = Main.chat.getPlayerSuffix(null, Func.graczOffline(jedenZCzłonków));
			
			String suff = ranga(ev.pktPo);
			if (suff.equals(aktsuff))
				return;
			
			for (String członek : ev.wyspa.członkowie.keySet()) {
				OfflinePlayer p = Func.graczOffline(członek);
				Main.chat.setPlayerSuffix(null, p, suff);
				
				ustawGrupe(p, suff);
			}
		}).start();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void usuwanieWyspy(UsuwanieWyspyEvent ev) {
		new Thread(() -> {
			for (String członek : ev.wyspa.członkowie.keySet()) {
				OfflinePlayer p = Func.graczOffline(członek);
				Main.chat.setPlayerSuffix(null, p, "");
				ustawGrupe(p, null);
			}
		}).start();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void opuszczanieWyspy(OpuszczanieWyspyEvent ev) {
		new Thread(() -> {
			OfflinePlayer p = Func.graczOffline(ev.nick);
			Main.chat.setPlayerSuffix(null, p, "");
			ustawGrupe(p, null);
		}).start();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void dołączanieDoWyspy(DołączanieDoWyspyEvent ev) {
		new Thread(() -> {
			OfflinePlayer randCzłonek = Func.graczOffline(Func.losuj(ev.wyspa.członkowie.keySet()));
			String suff = Main.chat.getPlayerSuffix(null, randCzłonek);
			Main.chat.setPlayerSuffix(null, ev.p, suff);
			ustawGrupe(ev.p, suff);
		}).start();
	}
	
	private void ustawGrupe(OfflinePlayer p, String ranga) {
		Func.forEach(Main.perms.getPlayerGroups(null, p), group -> {
			if (group.startsWith("sky-"))
				Main.perms.playerRemoveGroup(null, p, group);
		});
		if (ranga != null)
			Main.perms.playerAddGroup(null, p, "sky-" + ranga);
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
		String prefix = Func.nieNull(Main.ust.wczytajStr("RangiWysp.prefix"));
		rangi.clear();
		Double ost = null;
		boolean info = true;
		for (Entry<String, Object> en : Main.ust.sekcja("RangiWysp").getValues(false).entrySet())
			if (!en.getKey().equals("prefix")) {
				double w = Func.DoubleObj(en.getValue());
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
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		if (sekcja == null)
			return Func.powiadom(sender, "Nie ma żadnych rang wysp");
		String prefix = "§6" + sekcja.getString("prefix", "§a§l ");
		sender.sendMessage("§a§n|< §c-§l<>§c- §6Rangi wysp §c-§l<>§c- §a§n>|");
		int licz = 0;
		for (Entry<String, Object> entry : sekcja.getValues(false).entrySet())
			if (!entry.getKey().equals("prefix")) {
				if (licz != 0 && licz++ % 3 == 0)
					sender.sendMessage(" ");
				sender.sendMessage(prefix + entry.getKey() + "§8: §e" + Func.DoubleToString(Func.DoubleObj(entry.getValue())));
			}
		return true;
	}
}




