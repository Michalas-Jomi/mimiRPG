package me.jomi.mimiRPG.Chat;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class ItemLink implements Listener {
	@EventHandler(priority=EventPriority.LOWEST)
	private void itemLink(AsyncPlayerChatEvent ev) {
		String msg = ev.getMessage();
		
		if (Main.essentials != null && Main.essentials.getUser(ev.getPlayer()).isMuted())
			return;
		
		if (msg.contains("[i]")) {
			int i = msg.indexOf("[i]");
			String[] części = {msg.substring(0, i), msg.substring(i+3, msg.length())};
			ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
			if (item == null || item.getType().isAir()) {
				ev.getPlayer().sendMessage(Func.prefix("Item Link") + "§cnie możesz podlinkować powietrza");
				return;
			}
			Napis itemLink = Napis.item(item);
			String format = "§7" + ev.getPlayer().getDisplayName() + "§8:§r ";
			Napis n = new Napis(format);
			n.dodaj(części[0]);
			n.dodaj(itemLink);
			n.dodaj(znajdzKolor(format + części[0]) + Func.listToString(części, 1, "[i]"));
			for (Player p : ev.getRecipients())
				n.wyświetl(p);
			Bukkit.getConsoleSender().sendMessage(n.toString());
			ev.setCancelled(true);
		}
	}
	
	private static String znajdzKolor(String msg) {
		String w = "";
		List<Character> kolory = Arrays.asList('0','1','2','3','4','5','6','7','8','9','a','b','d','e','c','r','f');
		char[] array = msg.toCharArray();
		if (msg.length() >= 2)
			for(int i=array.length-2; i>0; i--)
				if (array[i] == '§') {
					char symbol = array[i+1];
					w = "§" + Character.toString(symbol) + w;
					if (kolory.contains(symbol))
						return w;
				}
		return "§r";
	}
}
