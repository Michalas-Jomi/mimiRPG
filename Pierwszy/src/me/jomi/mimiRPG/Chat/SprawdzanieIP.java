package me.jomi.mimiRPG.Chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;

@Moduł
public class SprawdzanieIP extends Komenda implements Listener {
	public static final String prefix = Func.prefix("IP");
	static final Config config = new Config("configi/sprawdzanieIP");
	final String perm = Func.permisja("sprawdzip");
	
	public static SprawdzanieIP inst;
	public SprawdzanieIP() {
		super("sprawdzip", "/sprawdzip <nick>");
		Main.dodajPermisje(perm);
		inst = this;
	}
	
	@EventHandler
	public void dołączanie(PlayerJoinEvent ev) {
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			String ip = ev.getPlayer().getAddress().getAddress().getHostAddress();
			String nick = ev.getPlayer().getName();
			
			boolean zapis = false;
			List<String> lista;
			lista = getNicki(ip);  if (!lista.contains(nick))	{ zapis = true; lista.add(nick); ustawNicki(ip, lista); }
			lista = getIpki(nick); if (!lista.contains(ip))		{ zapis = true; lista.add(ip);	 ustawIpki(nick, lista); }
			if (zapis) config.zapisz();
			
			
			MonoKrotka<List<String>> krotka = sprawdz(nick, ip);
			
			if (krotka.a.size() >= 3) {
				String msg = Func.msg(prefix + "Wykryto u %s posiadanie %s kont %s (%s adresów ip)", ev.getPlayer().getName(), krotka.a.size(), krotka.a, krotka.b.size());
				Main.log(msg);
				Bukkit.getOnlinePlayers().forEach(p -> {
					if (p.hasPermission(perm))
						p.sendMessage(msg);
				});
			}
		});
	}
	
	// ([nicki], [ipki])
	public MonoKrotka<List<String>> sprawdz(String nick, String ip) {
		List<String> listaNicków = getNicki(ip);
		List<String> listaIp = getIpki(nick);
		
		boolean zapis = false;
		
		if (nick != null && !listaNicków.contains(nick)) {
			zapis = true;
			listaNicków.add(nick);
		}
		if (ip != null && !listaIp.contains(ip)) {
			zapis = true;
			listaIp.add(ip);
		}
		
		new ArrayList<>(listaIp).forEach(subip -> {
			if (subip != null)
				sprawdzIp(subip, listaNicków, listaIp);
		});
		
		if (zapis) config.zapisz();
		
		
		return new MonoKrotka<>(listaNicków, listaIp);
	}
	private void sprawdzIp(String ip, List<String> nicki, List<String> ipki) {
		getNicki(ip).forEach(nick -> {
			if (!nicki.contains(nick)) {
				nicki.add(nick);
				sprawdzNick(nick, nicki, ipki);
			}
		});
	}
	private void sprawdzNick(String nick, List<String> nicki, List<String> ipki) {
		getIpki(nick).forEach(ip -> {
			if (!ipki.contains(ip)) {
				ipki.add(ip);
				sprawdzIp(ip, nicki, ipki);
			}
		});
	}

	public List<String> getNicki(String ip)  { return ip != null   ? config.wczytajListe(ip) : new ArrayList<>(); }
	public List<String> getIpki(String nick) { return nick != null ? config.wczytajListe("nick." + nick) : new ArrayList<>(); }
	public void ustawNicki(String ip, List<String> nicki) { config.ustaw(ip, nicki); }
	public void ustawIpki(String nick, List<String> ipki) { config.ustaw("nick." + nick, ipki); }

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			MonoKrotka<List<String>> krotka = sprawdz(args[0], null);
			
			int[] licznik = new int[] {0};
			String[] msgs = new String[krotka.a.size()];
			
			krotka.a.forEach(nick ->
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					OfflinePlayer op = Func.graczOffline(nick);
					String msg;
					if (op == null)
						msg = "§c" + nick + "§8: §cbrak danych";
					else
						msg = (op.isOnline() ? "§a" : "§c") + nick + "§8: §6gra od §e" + Func.data(op.getFirstPlayed()) + "§6 ostatnio §e"
							+ Func.data(op.getLastPlayed()) + "§6 " + (op.isBanned() ? "§4Zbanowany " : "") +
							"§6czas online§8: §e" + Func.czas(op.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) +
							" §6craft/jump/deaths/mob kills/walk§8:§e " + String.join("§6/§e", Func.wykonajWszystkim(new Statistic[] {
									Statistic.CRAFTING_TABLE_INTERACTION, Statistic.JUMP, Statistic.DEATHS, Statistic.MOB_KILLS, Statistic.WALK_ONE_CM},
									stat -> String.valueOf(op.getStatistic(stat))));
					msgs[licznik[0]++] = msg;
					if (licznik[0] >= msgs.length) {
						StringBuilder strB = new StringBuilder();
						strB.append(prefix).append("Info gracza §e").append(nick).append("§8:\n");
						strB.append("§6Adresy ip§8: §e").append(krotka.b).append(" (").append(krotka.b.size()).append(")\n");
						for (String konto : msgs)
							strB.append("§e").append(konto).append('\n');
						sender.sendMessage(strB.toString());
					}
				})
			);
		});
		return true;
	}
}
