package me.jomi.mimiRPG.Edytory;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.SelektorItemów;

@Moduł
public class EdytorSelektorówItemów extends Komenda {
	public EdytorSelektorówItemów() {
		super("edytujselektoritemów");

		edytor.zarejestrójWyjątek("/edytujselektoritemów edytor czarnaLista", (selektor, ścieżka) -> {
			if (selektor.wymagane == null || selektor.kopia != null || selektor.akceptowalne != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
		edytor.zarejestrójWyjątek("/edytujselektoritemów edytor akceptowalne", (selektor, ścieżka) -> {
			if (selektor.wymagane == null || selektor.kopia != null || selektor.czarnaLista != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
		edytor.zarejestrójWyjątek("/edytujselektoritemów edytor wymagane", (selektor, ścieżka) -> {
			if (selektor.kopia != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
		
		edytor.zarejestrójWyjątek("/edytujselektoritemów edytor kopia", (selektor, ścieżka) -> {
			if (selektor.wymagane != null || selektor.czarnaLista != null || selektor.akceptowalne != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
	}
	

	final EdytorOgólny<SelektorItemów> edytor = new EdytorOgólny<>("edytujselektoritemów", SelektorItemów.class);
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "edytor");
		if (args.length == 2)
			return utab(args, "-t", "-u");
		if (args.length == 3 && args[1].equals("-t"))
			return utab(args, Baza.selektoryItemów.keySet());
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("edytor")) {
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, "/edytujselektoritemów edytor -t <nazwa selektora>");
			else if (args.length >= 2 && args[1].equals("-t"))
				args[2] = "configi/Selektory Itemów|" + args[2];
			return edytor.onCommand(sender, "edytujselektoritemów", args);
		}
		return false;
	}
}
