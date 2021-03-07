package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class DomyślneItemyPoŚmieci extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Domyślne Itemy Po Śmieci");
	
	Config config = new Config("configi/itemy po śmierci");
	
	
	public DomyślneItemyPoŚmieci() {
		super("ustawitemypośmierci");
	}

	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Domyślne Itemy po śmierci", config.klucze(true).size() - 2);
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		Player p = (Player) sender;
		List<ItemStack> lista = Lists.newArrayList();
		for (int i=0; i < 4*9; i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item != null && !item.getType().isAir())
				lista.add(item);
		}
		config.ustaw("itemy", lista);
		
		Consumer<String> cons = slot -> {
			ItemStack item = p.getInventory().getItem(EquipmentSlot.valueOf(slot));
			if (item != null) {
				config.ustaw("zbroja." + slot , item);
			}
		};
		
		Func.multiTry(
				() -> cons.accept("HEAD"),
				() -> cons.accept("CHEST"),
				() -> cons.accept("LEGS"),
				() -> cons.accept("FEET")
				);
		
		config.zapisz();
		
		sender.sendMessage(prefix + "Zapisano domyślne itemy po śmierci");
		
		return true;
	}
	

	void dajItemy(Player p) {
		for (ItemStack item : config.wczytajItemy("itemy"))
			Func.dajItem(p, item);
		
		Consumer<String> cons = slot -> {
			if (p.getEquipment().getItem(EquipmentSlot.valueOf(slot)) == null)
				p.getInventory().setItem(EquipmentSlot.valueOf(slot), config.wczytajItem("zbroja." + slot));
		};
		
		Func.multiTry(
				() -> cons.accept("HEAD"),
				() -> cons.accept("CHEST"),
				() -> cons.accept("LEGS"),
				() -> cons.accept("FEET")
				);
	}
	
	
	@EventHandler
	public void śmierć(PlayerDeathEvent ev) {
		Func.opóznij(1, () -> dajItemy(ev.getEntity()));
	}
	
}



