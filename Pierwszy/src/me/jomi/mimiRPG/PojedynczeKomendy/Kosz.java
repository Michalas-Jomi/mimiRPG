package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;
import me.jomi.mimiRPG.Zegar;

public class Kosz extends Komenda implements Prze³adowalny, Zegar {
	private static Inventory inv = Bukkit.createInventory(null, 6*9, "§1§lKosz");
	private static List<ItemStack> kolejka = Lists.newArrayList();
	
	private static int maxTimer;
	private static int timer;
	
	private static int ¿ywotnoœæ;
	
	private static String msgPoCzyszczeniu;
	private static String msgOstrzegawcze;
	
	private static List<Integer> ostrzerzenia;

	public Kosz() {
		super("kosz");
	}
	
	public int czas() {
		if (maxTimer <= -1) return 20;
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
			for (Entity en : w.getEntitiesByClasses(CraftItem.class))
				if (en.getTicksLived() > ¿ywotnoœæ) {
					CraftItem item = (CraftItem) en;
					kolejka.add(item.getItemStack());
					item.remove();
				}
		if (!msgPoCzyszczeniu.isEmpty())
			Bukkit.broadcastMessage(msgPoCzyszczeniu.replace("{liczba}", "" + kolejka.size()));
		wrzuæItemy();
	}
	
	private static void wrzuæItemy() {
		while (inv.firstEmpty() != -1 && !kolejka.isEmpty()) {
			inv.addItem(kolejka.get(0));
			kolejka.remove(0);
		}
	}

	@SuppressWarnings("unchecked")
	public void prze³aduj() {
		maxTimer = Main.ust.wczytajInt("Kosz", "timer");
		timer = maxTimer;
		¿ywotnoœæ = Main.ust.wczytajInt("Kosz", "zywotnosc") * 20;
		msgPoCzyszczeniu = Func.koloruj(Main.ust.wczytajStr("Kosz", "msgPoCzyszczeniu"));
		msgOstrzegawcze  = Func.koloruj(Main.ust.wczytajStr("Kosz", "msgOstrzegawcze"));
		ostrzerzenia = (List<Integer>) Main.ust.wczytaj("Kosz", "ostrzerzenia");
	}
	public String raport() {
		return "§6Kosz: §e" + (maxTimer != -1 ? (maxTimer + "s §6 miêdzy czyszczeniami") : "§cWy³aczony");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, "Jesteœ zbyt wa¿ny by grzebaæ siê w œmieciach");
		Player p = (Player) sender;
		if (maxTimer <= -1) return Main.powiadom(p, "§cKosz jest wy³¹czony");
		wrzuæItemy();
		p.openInventory(inv);
		return true;
	}
	
}
