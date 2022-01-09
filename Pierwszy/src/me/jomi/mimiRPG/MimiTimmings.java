package me.jomi.mimiRPG;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotki.TriKrotka;

@Moduł
public class MimiTimmings extends Komenda {
	public static final String prefix = Func.prefix(MimiTimmings.class);
	public MimiTimmings() {
		super("mimitimmings", "/mimitimmings [reset | <ilość top> [ilość | czas]]");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) return utab(args, "reset", "5", "10", "25", "50", "100");
		if (args.length == 2) return utab(args, "ilość", "czas");
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (args.length < 1)
			return false;
		
		if (args[0].equalsIgnoreCase("reset")) {
			Timming.timmingi.clear();
			throwFormatMsg("zresetowano mimiTimmingi");
		} else {
			if (args.length < 2)
				return false;
			
			int ile = Func.Int(args[0], 10);
			
			switch (args[1].toLowerCase()) {
			case "ilosc":
			case "ilosć":
			case "ilośc":
			case "ilość": fabric(sender, ile, k -> -(double) k.c); break;
			case "czas":  fabric(sender, ile, k -> -k.b); 		  break;
			default: return false;
			}
		}
		
		return true;
	}
	private void fabric(CommandSender sender, int ile, Function<TriKrotka<String, Double, Integer>, Double> func) {
		List<TriKrotka<String, Double, Integer>> top = new ArrayList<>();
		
		Timming.timmingi.forEach((nazwa, krotka) -> {
			TriKrotka<String, Double, Integer> trikrotka = new TriKrotka<>(nazwa, krotka.a, krotka.b);
			Func.insort(trikrotka, top, func);
		});
		

		sender.sendMessage(" ");
		sender.sendMessage(" ");
		sender.sendMessage(" §c~~ " + prefix + "§c~~");
		sender.sendMessage("§7lp) nazwa§8, §7średni czas w milisekundach §8x §7ile razy było wykonane");
		for (int i=0; i < ile && top.size() > 0; i++) {
			TriKrotka<String, Double, Integer> k = top.remove(0);
			sender.sendMessage("§9 " + (i+1) + ") §a" + k.a + " §e" + Func.zaokrąglij(k.b, 2) + "§6ms x§e" + k.c);
		}
		
		sender.sendMessage(" ");
	}

}
