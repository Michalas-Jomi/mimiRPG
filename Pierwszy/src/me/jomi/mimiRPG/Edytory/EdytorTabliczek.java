package me.jomi.mimiRPG.Edytory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class EdytorTabliczek extends Komenda implements Listener{
	public static String prefix = Func.prefix("Edytor Tabliczek");
	private static HashMap<String, Sign> mapa = new HashMap<>();

	public EdytorTabliczek() {
		super("edytujtabliczke");
	}
	
	private static void ustaw(Player p, Sign blok) {
		mapa.put(p.getName(), blok);
		
		Napis msg = new Napis("\n\n§6§l-----§6|§l> §1§lEdytor Tabliczek §6§l <§6|§l-----\n");
		for (int i=0; i<4; i++) {
			String linia = blok.getLine(i);
			Napis n = new Napis("§e§l-> §0" + dajLinie(linia) + "\n", Action.SUGGEST_COMMAND, "/edytujtabliczke " + i + " " + linia.replace("&", "&&").replace("§", "&"));
			msg.dodaj(n);
		}
		String typ = blok.getType().toString().toLowerCase();
		for (String _typ : Arrays.asList("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped")) {
			String kolor = "§6";
			if (typ.startsWith(_typ))
				kolor = "§e";
			Napis n = new Napis(kolor + "[" + _typ + "] ", Action.RUN_COMMAND, "/edytujtabliczke typ " + _typ);
			msg.dodaj(n);
		}
		msg.dodaj("\n");
		msg.wyświetl(p);
	}
	private static String dajLinie(String linia) {
		if (linia == null || linia.equals(""))
			return "§6brak lini";
		return linia;
	}
	
	@EventHandler
	public void kliknięcieBloku(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();
		if (!p.hasPermission("mimiRPG.edytujtabliczke") || !p.isSneaking()) return;
		
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		if (!blok.getType().toString().endsWith("_SIGN")) return;

		ustaw(p, (Sign) blok.getState());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Gracz w świecie, Konsole we wszechświecie, jesteś zbyt potężny SORY");
		Player p = (Player) sender;
		if (!mapa.containsKey(p.getName())) return Func.powiadom(p, prefix + "Nie wybrałeś żadnej tabliczki");
		if (args.length < 1) return Func.powiadom(p, prefix + "Nie podano numeru lini");
		if (args[0].equals("typ"))
			if (args.length < 2) return Func.powiadom(p, prefix + "Nieprawidłowy typ tabliczki");
			else {
				String typ = args[1];
				Sign blok = mapa.get(p.getName());
				String data = blok.getBlockData().getAsString().substring(10);
				if (data.startsWith("dark_"))
					data = data.substring(5);
				String[] _typ = data.split("_");
				_typ[0] = typ;
				data = Func.listToString(_typ, 0, "_");
				blok.setBlockData(Bukkit.createBlockData("minecraft:" + data));
				blok.update(true, false);
				ustaw(p, blok);
				return true;
			}
		int nr = Func.Int(args[0], -1);
		if (nr == -1 || nr >= 4) return Func.powiadom(p, prefix + "Niepoprawny nr lini");
		Sign blok = mapa.get(p.getName());
		if (blok == null) return Func.powiadom(p, prefix + "Wybrana tabliczka nie istnieje");
		
		String txt = "";
		if (args.length > 1)
			txt = Func.listToString(args, 1);
		blok.setLine(nr, Func.koloruj(txt));
		blok.update();
		ustaw(p, blok);
		return true;
	}
}
