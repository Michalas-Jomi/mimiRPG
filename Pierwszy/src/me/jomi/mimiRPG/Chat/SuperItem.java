package me.jomi.mimiRPG.Chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class SuperItem extends Komenda implements Listener {
	public static final String prefix = Func.prefix("Super Item");
	public SuperItem() {
		super("superitem", null, "si");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command __cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "To tylko dla graczy jest");
		Player p = (Player) sender;
		
		if (args.length < 1)
			return edytor(p);
		
		Function<Integer, String> getCmd = i -> {
			if (args.length < i + 1) {
				p.sendMessage(prefix + "po znaku >> napisz swoją komende");
				return "";
			}
			String cmd = Func.listToString(args, i);
			if (cmd.charAt(0) == '/')
				cmd = cmd.substring(1);
			return cmd.trim();
		};
		Supplier<String> getMat = () -> {
			ItemStack item = p.getInventory().getItemInMainHand();
			if (item == null)
				item = p.getInventory().getItemInOffHand();
			if (item != null && !item.getType().isAir())
				return item.getType().toString();
			p.sendMessage(prefix + "Nie możesz przypisać komendy do powietrza!");
			return null;
		};

		Gracz g = Gracz.wczytaj(p);
		try {
			switch (args[0]) {
			case "usuń":
				g.superItemy.remove(args[1]);
				break;
			case "zmieńmateriał":
				String mat = getMat.get();
				if (mat == null) return true;
				g.superItemy.put(mat, g.superItemy.remove(args[1]));
				g.zapisz();
				break;
			case "zmień":
				Material.valueOf(args[1]);
				String cmd = getCmd.apply(3);
				if (cmd.isEmpty())
					throw new Throwable();
				g.superItemy.put(args[1], cmd);
				g.zapisz();
				break;
			case "ustaw":
				String _mat = getMat.get();
				if (_mat == null) return true;
				
				String _cmd = getCmd.apply(2);
				if (_cmd.isEmpty())
					throw new Throwable();
				
				g.superItemy.put(_mat, _cmd);
				break;
			}
		} catch (Throwable e) {
			return Func.powiadom(p, prefix + "Po znaku >> napisz swoją komendę");
		}
		
		return edytor(p, g);
	}
	
	boolean edytor(Player p) {
		return edytor(p, Gracz.wczytaj(p));
	}
	boolean edytor(Player p, Gracz g) {
		Napis n = new Napis("\n\n\n§a§lS§auper Item\n\n");
		
		int i = 0;
		// Material : cmd
		for (Entry<String, String> en : g.superItemy.entrySet()) {
			n.dodaj("§e- ");
			n.dodaj(new Napis("§cX", "§bKliknij aby §cUsunąć", "/superitem usuń " + en.getKey()));
			n.dodaj(" ");
			n.dodaj(new Napis("§d" + en.getKey(), "§bKliknij aby zmienić", "/superitem zmieńmateriał " + en.getKey()));
			n.dodaj("§8: ");
			n.dodaj(new Napis("§e/" + en.getValue(), "§bKliknij aby zmienić", "/superitem zmień " + en.getKey() + " >> "));
			n.dodaj("\n");
			i++;
		}
		
		int limit = limit(p);
		while (i++ < limit)
			n.dodajEnd(new Napis("§8Brak", "§bKliknij aby ustawić", "/superitem ustaw >> "));
		
		n.dodaj("\n");
		
		n.wyświetl(p);
		
		return true;
	}
	
	int limit(Player p) {
		if (Main.perms != null) {
			int limit = -1;
			for (String grupa : Main.perms.getPlayerGroups(p))
				limit = Math.max(limit, Main.ust.wczytajLubDomyślna("SuperItem.grupy." + grupa, -1));
			return limit == -1 ? 5 : limit;
		}
		return 5;
	}

	// nick : (ost czas, (ilość, task))
	HashMap<String, Krotka<Integer, Integer>> mapaUżyć = new HashMap<>();
	@EventHandler(priority = EventPriority.HIGHEST)
	public void użycie(PlayerInteractEvent ev) {
		if (!ev.getPlayer().hasPermission("mimirpg.superitem")) return;
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		
		ItemStack item = ev.getItem();
		if (item == null) return;
		
		String mat = item.getType().toString();
		HashMap<String, String> mapaCmd = Gracz.wczytaj(ev.getPlayer()).superItemy;
		for (Entry<String, String> en : mapaCmd.entrySet())
			if (en.getKey().equals(mat)) {
				Krotka<Integer, Integer> multiKrotka = 
						Func.wezUstaw(mapaUżyć, ev.getPlayer().getName(), () -> new Krotka<>(0, -1));
				if (multiKrotka.a >= 5) {
					ev.getPlayer().sendMessage(prefix + "Nie tak szybko!");
					return;
				}
				multiKrotka.a++;
				Func.wykonajDlaNieNull(multiKrotka.b, Bukkit.getScheduler()::cancelTask);
				multiKrotka.b = Func.opóznij(20, () -> mapaUżyć.remove(ev.getPlayer().getName()));
				ev.getPlayer().chat("/" + en.getValue());
				ev.setCancelled(true);
				return;
			}
	}
}

