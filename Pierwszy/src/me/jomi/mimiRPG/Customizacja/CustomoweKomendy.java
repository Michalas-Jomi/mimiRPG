package me.jomi.mimiRPG.Customizacja;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CustomoweKomendy extends Komenda implements Przeładowalny {
	public static final String prefix = Func.prefix("Customowe Komendy");
	public CustomoweKomendy() {
		super("customowekomendy");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Lists.newArrayList();
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("customowekomendy")) {
			sender.sendMessage(prefix + "Ta komenda nie ma ciała, jest bo jest nie wnikaj");
		} else {
			ConfigurationSection sekcja = Main.ust.sekcja("CustomoweKomendy.komendy." + cmd.getName());
			if (sekcja != null) {
				List<String> errory = Lists.newArrayList();
				for (String komenda : sekcja.getStringList("komendy"))
					try {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), 
								komenda
								.replace("%nick%", sender.getName())
								.replace("%displayname%", sender instanceof Player ? Func.getDisplayName(((Player) sender)) : sender.getName()));
					} catch (Throwable e) {
						Main.error(prefix + "Problem z customową komendą " + cmd.getName());
						errory.add(komenda);
						e.printStackTrace();
					}
				if (!errory.isEmpty())
					Main.warn(prefix + "Problemy z komendami customowej komendy " + cmd.getName() + ":");
				for (String komenda : errory)
					Main.warn(komenda);
			} else
				sender.sendMessage(prefix + "Ta komenda została wyłączona");
		}
		return true;
	}

	@Override
	public void przeładuj() {
		Krotka<Boolean, ?> k = new Krotka<>(false, null);
		Func.wykonajDlaNieNull(Main.ust.sekcja("CustomoweKomendy.komendy"), sekcja -> {
			for (String komenda : sekcja.getKeys(false))
				Func.wykonajDlaNieNull(sekcja.getConfigurationSection(komenda), sekcjaKomendy -> {
					if (!sekcjaKomendy.contains("komendy"))
						Main.warn("Brak komend customowej komendy " + komenda);
					for (PluginCommand cmd : dajKomendy())
						if (cmd.getName().equals(komenda))
							return;
					k.a = true;
					ustawKomende(komenda, null, sekcjaKomendy.getStringList("aliasy"));
				});
		});
		if (k.a)
			Main.reloadBukkitData();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Customowe Komendy", dajKomendy().size());
	}
	@SuppressWarnings("unchecked")
	private List<PluginCommand> dajKomendy() {
		try {
			return ((List<PluginCommand>) Func.dajZField(this, "_komendy"));
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * nazwa komendy:
	 *   aliasy: [alias1, alias2]
	 *   komendy:
	 *   - cmd1 %nick% %displayname%
	 *   - cmd2
	 * 
	 * 
	 * 
	 */
}
