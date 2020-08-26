package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze�adowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Gracze.Gracze;

public class Koniki extends Komenda implements Listener, Prze�adowalny, Zegar {
	public static final String prefix = Func.prefix("Koniki");
	
	public Koniki() {
		super("konik", null, "kon", "horse");
		Main.dodajPermisje("konik.bypass");
		int ile = usu�Wszystkie();
		if (ile != 0)
			Main.log("�eUsuni�to �b" + ile + "�e Niechcianych Konik�w");
	}
	
	public int czas() {
		for (Gracz gracz : Gracze.mapa.values())
			gracz.ko�.sprawdz();
		return 400;
	}
	
	private static int maxCzas;
	private static final HashMap<Material, Integer> mapa = new HashMap<>(); 
	public void prze�aduj() {
		mapa.clear();
		ConfigurationSection sekcja = Main.ust.sekcja("Koniki.jedzenie");
		if (sekcja != null)
			for (Entry<String, Object> en : sekcja.getValues(false).entrySet())
				mapa.put(Material.valueOf(en.getKey().toUpperCase()), (int) en.getValue()*60);
		maxCzas = (int) Main.ust.wczytajLubDomy�lna("Koniki.maxCzas", 3) * 60;
		usu�Wszystkie();
	}
	public String raport() {
		return "�6Potrawy dla Konik�w: �e" + mapa.size();
	}
	
	public static int usu�Wszystkie() {
		int ile = 0;
		for (World world : Bukkit.getWorlds())
		for (Entity en : world.getEntitiesByClasses(Horse.class)) {
			if (en.hasMetadata("mimiKon")) {
				for (Entity _en : en.getPassengers())
					_en.sendMessage(prefix + "Prze�adowywanie Pluginu");
				en.remove();
				ile++;
			}
		}
		return ile;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			Gracze.gracz(sender.getName()).ko�.przywo�aj();
		else
			sender.sendMessage("Tylko gracz mo�e tego u�y�");
		return true;
	}

	@EventHandler
	public void siadanie(VehicleEnterEvent ev) {
		if (!(ev.getEntered() instanceof Player)) return;
		if (!(ev.getVehicle() instanceof Horse)) return;
		Player p = (Player) ev.getEntered();
		Horse kon = (Horse) ev.getVehicle();
		if (kon.hasMetadata("mimiKon")) {
			if (!p.hasPermission("mimiRPG.konik.bypass") &&
					!kon.getMetadata("mimiKon").get(0).asString().equals(p.getName())) {
				p.sendMessage(prefix + "�4Ej! �cTo nie tw�j Konik ziom, nie siadaj.");
				ev.setCancelled(true);
				return;
			}
			if (!Gracze.gracz(kon.getMetadata("mimiKon").get(0).asString()).ko�.nakarmiony()) {
				p.sendMessage(prefix + "Ten Konik jest zbyt g�odny aby m�g� cie wozi�");
				ev.setCancelled(true);
				return;
			}
		}
	}
	@EventHandler
	public void otwieranieEqKonika(InventoryOpenEvent ev) {
		if (!(ev.getInventory() instanceof HorseInventory)) return;
		HorseInventory inv = (HorseInventory) ev.getInventory();
		Player p = (Player) ev.getPlayer();
		if (!p.hasPermission("mimiRPG.konik")) return;
		Horse kon = (Horse) inv.getHolder();
		otw�rz(p, kon, ev);
	}
	@EventHandler
	public void klikanieKonika(PlayerInteractAtEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof Horse)) return;
		Horse kon = (Horse) ev.getRightClicked();
		Player p = ev.getPlayer();
		if (ev.getPlayer().isSneaking() && !kon.isAdult())
			otw�rz(p, kon, ev);
		else {
			ItemStack item = p.getInventory().getItemInMainHand();
			if (item != null && mapa.containsKey(item.getType())) {
				int ile = mapa.get(item.getType());
				ev.setCancelled(true);
				Gracz gracz = Gracze.gracz(kon.getMetadata("mimiKon").get(0).asString());
				if (gracz.ko�.zapas >= System.currentTimeMillis() / 1000 + maxCzas) {
					p.sendMessage(prefix + "Ten Konik jest ju� najedzony");
					return;
				}
				gracz.ko�.nakarm(ile);
				item.setAmount(item.getAmount() - 1);
				if (!gracz.p.getName().equals(p.getName())) {
					p.sendMessage(prefix + Func.msg("Nakarmi�e� Konika gracza %s mo�e on jezdzi� na nim jeszcze %s bez karmienia", gracz.p.getName(), czas(gracz.ko�.zapas)));
					gracz.p.sendMessage(prefix + Func.msg("%s Nakarmi� twojego Konika, mo�esz jezdzi� na nim jeszcze %s bez karmienia", p.getName(), czas(gracz.ko�.zapas)));
				} else {
					p.sendMessage(prefix + Func.msg("Nakarmi�e� swojego Konika mo�esz jezdzi� na nim jeszcze %s bez karmienia", czas(gracz.ko�.zapas)));
				}
				return;
			}
			if (!kon.isAdult())
				kon.addPassenger(p);
		}
	}
	public static String czas(long zapas) {
		int czas = (int) (zapas - (System.currentTimeMillis() / 1000));
		if (czas <= 0)
			return "0 sekund";
		int minuty = czas / 60;
		int godziny = minuty / 60; minuty %= 60;
		int dni = godziny / 24; godziny %= 24;
		
		String w = "";
		if (dni != 0)	 	w += dni 	+ " dni ";
		if (godziny != 0) 	w += godziny+ " godzin ";
		if (minuty != 0) 	w += minuty + " minut ";
		return w.equals("") ? "0 sekund" : w;
	}
	private void otw�rz(Player p, Horse kon, Cancellable ev) {
		if (kon.hasMetadata("mimiKon")) {
			ev.setCancelled(true);
			String nick = kon.getMetadata("mimiKon").get(0).asString();
			if (p.hasPermission("mimiRPG.konik.bypass") || 
					nick.equals(p.getName()))
				p.openInventory(Gracze.gracz(nick).ko�.inv);
			else
				p.sendMessage(prefix + "�4Ej! �cTo nie tw�j Konik ziom, nie dotykaj.");
		}
	}
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getView().getTitle().equals("�1�lTw�j Konik"))
			Gracze.gracz(((Player) ev.getInventory().getHolder()).getName()).ko�.klikni�teEq(ev);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getView().getTitle().equals("�1�lTw�j Konik"))
			Gracze.gracz(((Player) ev.getInventory().getHolder()).getName()).ko�.zapisz();	
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Gracze.gracz(ev.getPlayer().getName()).ko�.usu�();
	}
}
