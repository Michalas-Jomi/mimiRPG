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
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Nie odlaneziono żadnego nieprzyjaznego gracza"));
			else		
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Najbliższy gracz: §a" + najbliższy.getDisplayName() + " " + (int) dystans + "m"));
	}

	int getOdświeżanie() {
		return Main.ust.wczytajLubDomyślna("KompasPvP.limit odświeżania", -1);
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
				Matcher matcher = Pattern.compile("§6Kompas §aużyć:§e (\\d+)").matcher(Func.nieNull(item.getItemMeta().getDisplayName()));
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
	
	// TODO szablon w configu
	@Override
	public int czas() {
		if (getOdświeżanie() != -1)
			return 100;
		List<Player> gracze = Lists.newArrayList(Bukkit.getOnlinePlayers());
		
		for (int i=0; i<Main.ust.wczytajLubDomyślna("KompasPvP.odświeżanie gracze na raz", 10); i++)
			ustaw(Func.losujPop(gracze));
		
		return Main.ust.wczytajLubDomyślna("KompasPvP.odświeżanie ticki", 20);
	}
}


