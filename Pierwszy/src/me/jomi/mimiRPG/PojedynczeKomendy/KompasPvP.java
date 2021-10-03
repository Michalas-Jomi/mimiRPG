package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class KompasPvP implements Zegar, Listener {
	public static final String prefix = Func.prefix("KompasPvP");
	void ustaw(Player p) {
		if (p == null)
			return;
		Player najbliższy = null;
		double dystans = p.getWorld().getWorldBorder().getSize()*2;
		
		Gracz g = Gracz.wczytaj(p);
		
		for (Player player : p.getWorld().getPlayers()) {
			if (Func.multiEquals(player.getGameMode(), GameMode.SPECTATOR, GameMode.CREATIVE))
				continue;
			if (g.gildia != null) {
				if (g.gildia.equals(Gracz.wczytaj(player).gildia))
					continue;
			} else
				if (player.getName().equals(p.getName()))
					continue;
			double _dystans = p.getLocation().distance(player.getLocation());
			if (_dystans < dystans) {
				dystans = _dystans;
				najbliższy = player;
			}
		}
		
		p.setCompassTarget(najbliższy == null ? p.getWorld().getSpawnLocation() : najbliższy.getLocation());
		Predicate<ItemStack> test = item -> item != null && item.getType().equals(Material.COMPASS);
		
		PlayerInventory inv = p.getInventory();
		if (test.test(inv.getItemInMainHand()) || test.test(inv.getItemInOffHand()))
			if (najbliższy == null)
				Func.sendActionBar(p, "§6Nie odlaneziono żadnego nieprzyjaznego gracza");
			else		
				Func.sendActionBar(p, "§6Najbliższy gracz: §a" + Func.getDisplayName(najbliższy) + " " + (int) dystans + "m");
	}

	int getOdświeżanie() {
		return Main.ust.wczytaj("KompasPvP.limit odświeżania", -1);
	}
	
	final Set<String> cooldown = Sets.newConcurrentHashSet();
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK))
			return;
		if (cooldown.contains(ev.getPlayer().getName()))
			return;
		Func.wykonajDlaNieNull(ev.getItem(), item -> {
			if (item.getType().equals(Material.COMPASS) && getOdświeżanie() != -1) {
				Matcher matcher = Pattern.compile("§6Kompas §aużyć:§e (\\d+)").matcher(Func.nieNull(Func.getDisplayName(item.getItemMeta())));
				int ile = matcher.find() ? Func.Int(matcher.group(1)) : getOdświeżanie();
				if (ile <= 0)
					ev.getPlayer().sendMessage(prefix + "Nie możesz już użyć tego kompasu");
				else {
					Func.nazwij(item, "§6Kompas §aużyć:§e " + (ile - 1));
					ustaw(ev.getPlayer());
					cooldown.add(ev.getPlayer().getName());
					Func.opóznij(20*5, () -> cooldown.remove(ev.getPlayer().getName()));
				}
			}
		});
	}
	
	@Override
	public int czas() {
		if (getOdświeżanie() != -1)
			return 100;
		List<Player> gracze = Lists.newArrayList(Bukkit.getOnlinePlayers());
		
		for (int i=0; i<Main.ust.wczytaj("KompasPvP.odświeżanie gracze na raz", 10); i++)
			ustaw(Func.losujPop(gracze));
		
		return Main.ust.wczytaj("KompasPvP.odświeżanie ticki", 20);
	}
}


