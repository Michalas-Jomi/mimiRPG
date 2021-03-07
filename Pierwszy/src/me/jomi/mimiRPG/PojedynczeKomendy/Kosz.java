package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Kosz extends Komenda implements Przeładowalny, Zegar {
	private static Inventory inv = Bukkit.createInventory(null, 6*9, "§1§lKosz");
	private static List<ItemStack> kolejka = Lists.newArrayList();
	
	private static int maxTimer;
	private static int timer;
	
	private static int żywotność;
	
	private static String msgPoCzyszczeniu;
	private static String msgOstrzegawcze;
	
	private static List<Integer> ostrzerzenia;
	
	private static List<String> omijaneŚwiaty;

	public Kosz() {
		super("kosz");
	}
	
	@Override
	public int czas() {
		if (maxTimer <= -1) return 20;
		if (Bukkit.getOnlinePlayers().size() <= 0) return 20;
		timer -= 1;
        if (timer <= 0) {
        	timer = maxTimer;
        	zbierz();
        } else if (!msgOstrzegawcze.isEmpty() && ostrzerzenia.contains(timer))
    		Bukkit.broadcastMessage(msgOstrzegawcze.replace("{liczba}", "" + timer));
        return 20;
	}
	private static void zbierz() {
		inv.clear();
		kolejka.clear();
		for (World w : Bukkit.getWorlds())
			if (!omijaneŚwiaty.contains(w.getName()))
				for (Entity en : w.getEntitiesByClasses(Item.class))
					if (en.getTicksLived() > żywotność) {
						Item item = (Item) en;
						kolejka.add(item.getItemStack());
						item.remove();
					}
		if (!msgPoCzyszczeniu.isEmpty())
			Bukkit.broadcastMessage(msgPoCzyszczeniu.replace("{liczba}", "" + kolejka.size()));
		wrzućItemy();
	}
	
	private static void wrzućItemy() {
		while (inv.firstEmpty() != -1 && !kolejka.isEmpty()) {
			inv.addItem(kolejka.get(0));
			kolejka.remove(0);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		maxTimer = Main.ust.wczytajInt("Kosz.timer");
		timer = maxTimer;
		żywotność = Main.ust.wczytajInt("Kosz.zywotnosc") * 20;
		msgPoCzyszczeniu = Func.koloruj(Main.ust.wczytajStr("Kosz.msgPoCzyszczeniu"));
		msgOstrzegawcze  = Func.koloruj(Main.ust.wczytajStr("Kosz.msgOstrzegawcze"));
		ostrzerzenia = (List<Integer>) Main.ust.wczytaj("Kosz.ostrzerzenia");
		omijaneŚwiaty = Main.ust.wczytajListe("Kosz.omijane Światy");
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Kosz", (maxTimer != -1 ? (maxTimer + "s §6 między czyszczeniami") : "§cWyłaczony"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, "Jesteś zbyt ważny by grzebać się w śmieciach");
		Player p = (Player) sender;
		if (maxTimer <= -1) return Func.powiadom(p, "§cKosz jest wyłączony");
		wrzućItemy();
		p.openInventory(inv);
		return true;
	}
	
}
