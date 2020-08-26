package me.jomi.mimiRPG.Chat;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;
import me.jomi.mimiRPG.Edytory.EdytujItem;

public class Funkcje extends Komenda implements Prze³adowalny {
	public static final String prefix = Func.prefix("Funkcje");
	private static HashMap<String, Funkcja> mapa = new HashMap<>();
	
	public Funkcje() {
		super("funkcja", null, "func");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupe³nijTabComplete(args.length == 1 ? args[0] : "Ala ma kota", Lists.newArrayList(mapa.keySet()));
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 0) return Main.powiadom(sender, prefix + "/funkcja <nazwa> [parametry]");
		String nazwa = args[0];
		Funkcja func = mapa.get(nazwa);
		if (func != null) {
			if (args.length-1 == func.parametry)
				func.wykonaj(sender, args);
			else 
				sender.sendMessage(prefix + Func.msg("Funkcja %s przyjmuje %s parametry, podano %s", nazwa, func.parametry, args.length-1));
		}
		else
			sender.sendMessage(prefix + "Niepoprawna Funkcja §e" + nazwa);
		return true;
	}

	private static final List<Funkcja> funkcje = Lists.newArrayList();
	public void prze³aduj() {
		funkcje.clear();
		File folder = new File("plugins/"+Main.plugin.getName()+"/funkcje");
		if (!folder.exists())
			folder.mkdirs();
		skanuj(folder);
	}
	public String raport() {
		return "§6Funkcje: §e" + mapa.size();
	}
	private void skanuj(File folder) {
		for (File plik : folder.listFiles())
			if (plik.getName().endsWith(".txt"))
				dodaj(plik);
			else if (plik.isFile())
				Main.plugin.getLogger().warning("Niepoprawny plik Funkcji " + plik.getPath());
			else
				skanuj(plik);
	}
	private static int start = ("plugins/"+Main.plugin.getName()+"/funkcje/").length();
	private void dodaj(File plik) {
		if (plik.getName().contains(" ")) {
			Main.plugin.getLogger().warning("Niepoprawna nazwa Funkcji " + plik.getPath());
			return;
		}
		String path = plik.getPath();
		String nazwa = path.substring(start, path.length()-4);
		mapa.put(nazwa, new Funkcja(Func.czytajPlik(plik).split("\n")));
	}

}

class Funkcja {
	String[] komendy;
	public int parametry;
	public Funkcja(String[] komendy) {
		this.komendy = komendy;
		String raw = Func.listToString(komendy, 0);
		int i=0;
		while(raw.contains("%"+(++i)+"%"));
		parametry = i-1;
	}
	
	public void wykonaj(CommandSender p, String[] parametry) {
		nastêpna(p, 0, parametry);
	}
	private void nastêpna(CommandSender p, int nr, String[] parametry) {
		if (nr >= komendy.length)
			return;
		String komenda = komendy[nr];
		for (int i=1; i<=this.parametry; i++)
			komenda = komenda.replace("%"+i+"%", parametry[i]);
		if (komenda.startsWith(">czekaj")) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	            public void run() {
	                nastêpna(p, nr+1, parametry);
	            }
	        }, EdytujItem.sprawdz_liczbe(komenda.split(" ")[1], 0));
			return;
		}
		Bukkit.getServer().dispatchCommand(p, komenda);
		nastêpna(p, nr+1, parametry);
	}
}
