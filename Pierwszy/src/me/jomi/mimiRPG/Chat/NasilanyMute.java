package me.jomi.mimiRPG.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class NasilanyMute extends Komenda implements Przeładowalny {
	public static boolean warunekModułu() {
		return Main.essentials != null;
	}
	
	public NasilanyMute() {
		super("nasilanymute", "/nasilanymute <nick> (powód)", "nmute");
	}
	
	final List<Integer> kary = new ArrayList<>();

	
	@Override
	public void przeładuj() {
		kary.clear();
		Main.ust.wczytajListe("nasilany mute.kary").forEach(czas -> kary.add(Func.czas(czas)));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("kary nasilanego mute", kary.size());
	}
	
	public int getCzasMuta(Gracz g) {
		if (kary.size() == 0)
			return 1000 * 60;
		return kary.get(Math.min(g.nasilanymute_licznik, kary.size() - 1)) * 1000;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		Consumer<OfflinePlayer> cons = p -> {
			User user = Main.essentials.getUser(p.getUniqueId());
			
			Gracz g = Gracz.wczytaj(p);
			if (!user.isMuted()) {
				g.nasilanymute_licznik++;
				g.zapisz();
			}
			
			int czas = getCzasMuta(g);
			
			String powód = args.length >= 2 ? null : Func.listToString(args, 1);
			
			user.setMuted(true);
			user.setMuteReason(Func.koloruj(powód));
			user.setMuteTimeout(System.currentTimeMillis() + czas);
			
			Object[] format = new Object[] {"mutujący", sender, "mutowany", p, "czas", Func.czas(czas), "powód", powód == null ? "Nie ustawiono powodu" : powód};
			if (p instanceof Player)
				((Player) p).sendMessage(preThrowMsg("NasilanyMute.zmutowano.dla zmutowanego", format));
			Func.broadcast(preThrowMsg("NasilanyMute.zmutowano.dla uprawnionych", format), Func.permisja("nasilanymute"));
			broadcastMsg("NasilanyMute.zmutowano.broadcast", format);
		};

		OfflinePlayer p = Bukkit.getPlayer(args[0]);
		
		if (p == null)
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				OfflinePlayer p2 = Func.graczOffline(args[0]);
				if (p2 == null)
					throwMsg("NasilanyMute.niepoprawnyGracz", "nick", args[0]);
				cons.accept(p2);
			});
		else
			cons.accept(p);
		
		return true;
	}
}
