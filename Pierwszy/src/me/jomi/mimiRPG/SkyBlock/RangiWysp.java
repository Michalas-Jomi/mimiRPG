package me.jomi.mimiRPG.SkyBlock;

import java.util.List;

import org.bukkit.Bukkit;
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
	public static class Ranga {
		final double pkt;
		final String nazwa;
		final List<String> cmd_stracone;
		final List<String> cmd_zdobyty;
		
		public Ranga(String nazwa, double pkt, List<String> cmd_stracone, List<String> cmd_zdobyty) {
			this.pkt = pkt;
			this.nazwa = nazwa;
			this.cmd_zdobyty = Func.nieNull(cmd_zdobyty);
			this.cmd_stracone = Func.nieNull(cmd_stracone);
		}
		
		public void strać(String gracz) {
			Bukkit.getScheduler().runTask(Main.plugin, () -> 
					cmd_stracone.forEach(cmd -> 
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%gracz%", gracz))));
		}
		public void zdobądz(String gracz) {
			Bukkit.getScheduler().runTask(Main.plugin, () -> 
					cmd_zdobyty.forEach(cmd -> 
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%gracz%", gracz))));
		}
	}
	
	
	public RangiWysp() {
		super("rangiWysp");
	}
	public static boolean warunekModułu() {
		return Main.włączonyModół(SkyBlock.class) && Main.chat != null;
	}

	private final List<Ranga> rangi = Lists.newArrayList();

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
			
			Ranga stara = ranga(aktsuff);
			Ranga nowa = ranga(suff);
			
			for (String członek : ev.wyspa.członkowie.keySet()) {
				OfflinePlayer p = Func.graczOffline(członek);
				Main.chat.setPlayerSuffix(null, p, suff);
				
				stara.strać(p.getName());
				nowa.zdobądz(p.getName());
				
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
			
			Ranga ranga = ranga(suff);
			int index = index(ranga);
			
			for (int i=0; i < index; i++) {
				rangi.get(i).zdobądz(ev.p.getName());
				rangi.get(i).strać(ev.p.getName());
			}
			rangi.get(index).zdobądz(ev.p.getName());
			
		}).start();
	}
	
	private void ustawGrupe(OfflinePlayer p, String ranga) {
		if (ranga == null)
			Func.wykonajDlaNieNull(ranga(p), r -> {
				int i = index(r);
				while (i >= 0)
					rangi.get(i--).strać(p.getName());
			});
		
		
		Func.forEach(Main.perms.getPlayerGroups(null, p), group -> {
			if (group.startsWith("sky-"))
				Main.perms.playerRemoveGroup(null, p, group);
		});
		if (ranga != null)
			Main.perms.playerAddGroup(null, p, "sky-" + ranga);
	}
	
	int index(Ranga ranga) {
		for (int i=0; i < rangi.size(); i++)
			if (rangi.get(i).nazwa.equals(ranga.nazwa))
				return i;
		return -1;
	}
	Ranga ranga(OfflinePlayer p) {
		return ranga(Main.chat.getPlayerSuffix(null, p));
	}
	Ranga ranga(String suffix) {
		for (Ranga ranga : rangi)
			if (ranga.nazwa.equals(suffix))
				return ranga;
		return null;
	}
	String ranga(double pkt) {
		String ost = rangi.isEmpty() ? "" : rangi.get(0).nazwa;
		for (Ranga ranga : rangi) {
			if (ranga.pkt > pkt)
				break;
			ost = ranga.nazwa;
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
		ConfigurationSection sekcja = Main.ust.sekcja("RangiWysp");
		for (String key : sekcja.getValues(false).keySet())
			if (!key.equals("prefix")) {
				double w = Func.DoubleObj(sekcja.get(key + ".pkt"));
				
				if (info) {
					if (ost != null && ost > w) {
						Main.warn("Nieposortowane RangiWysp w ustawienia.yml. Należy je posortować dla poprawnego funkcjonowania");
						info = false;
					}
					ost = w;
				}
				rangi.add(new Ranga(prefix + key, w, sekcja.getStringList(key + ".cmd_stracone"), sekcja.getStringList(key + ".cmd_zdobyty")));
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
		for (Ranga ranga : rangi) {
			if (licz != 0 && licz++ % 3 == 0)
				sender.sendMessage(" ");
			sender.sendMessage(prefix + ranga.nazwa + "§8: §e" + Func.DoubleToString(ranga.pkt));
		}
		return true;
	}
}




