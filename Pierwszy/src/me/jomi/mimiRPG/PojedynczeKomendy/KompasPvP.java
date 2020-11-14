package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

@Moduł
public class KompasPvP implements Zegar {
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


	// TODO szablon w configu
	@Override
	public int czas() {
		List<Player> gracze = Lists.newArrayList(Bukkit.getOnlinePlayers());
		
		for (int i=0; i<Main.ust.wczytajLubDomyślna("KompasPvP.odświeżanie gracze na raz", 10); i++)
			ustaw(Func.losujPop(gracze));
		
		return Main.ust.wczytajLubDomyślna("KompasPvP.odświeżanie ticki", 20);
	}
}


