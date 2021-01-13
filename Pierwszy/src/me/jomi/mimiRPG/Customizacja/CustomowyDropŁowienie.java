package me.jomi.mimiRPG.Customizacja;

import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CustomowyDropŁowienie implements Listener, Przeładowalny {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void łowienie(PlayerFishEvent ev) {
		if (ev.isCancelled())
			return;
		if (drop != null && ev.getState() == State.CAUGHT_FISH)
			Func.wykonajDlaNieNull(ev.getCaught(), Item.class, item -> {
				List<ItemStack> itemki = drop.dropnij(Drop.poziom(ev.getPlayer(), ev.getPlayer().getInventory().getItemInMainHand()));
				if (!itemki.isEmpty()) {
					item.setItemStack(itemki.remove(0));
					itemki.forEach(itemek -> Func.dajItem(ev.getPlayer(), itemek));
				} else
					ev.setCancelled(true);
			});
	}
	
	Drop drop;
	
	@Override
	public void przeładuj() {
		drop = Main.ust.wczytajDrop("Customowy Drop Łowienie.drop"); //TODO dodać szablon w configu
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Customowe Łowienie", drop != null);
	}

}
