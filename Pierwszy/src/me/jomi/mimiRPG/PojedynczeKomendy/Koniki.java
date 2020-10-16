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
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Gracze.Kon;

@Moduł
public class Koniki extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Koniki");
	
	public Koniki() {
		super("konik", null, "kon", "horse");
		Main.dodajPermisje("konik.bypass");
		int ile = usuńWszystkie();
		if (ile != 0)
			Main.log("§eUsunięto §b" + ile + "§e Niechcianych Koników");
	}
	
	@Override
	public int czas() {
		for (World świat : Bukkit.getWorlds())
			for (Horse h : świat.getEntitiesByClass(Horse.class))
				if (h.hasMetadata("mimiKon")) {
					Gracz.wczytaj(h.getOwner().getName()).kon.sprawdz(h);
				}
		return 400;
	}
	
	private int maxCzas;
	private final HashMap<Material, Integer> mapa = new HashMap<>(); 

	@Override
	public void przeładuj() {
		mapa.clear();
		ConfigurationSection sekcja = Main.ust.sekcja("Koniki.jedzenie");
		if (sekcja != null)
			for (Entry<String, Object> en : sekcja.getValues(false).entrySet())
				mapa.put(Material.valueOf(en.getKey().toUpperCase()), (int) en.getValue()*60);
		maxCzas = (int) Main.ust.wczytajLubDomyślna("Koniki.maxCzas", 3) * 60;
		usuńWszystkie();
	}
	@Override
	public String raport() {
		return "§6Potrawy dla Koników: §e" + mapa.size();
	}
	
	public static int usuńWszystkie() {
		int ile = 0;
		for (World world : Bukkit.getWorlds())
		for (Entity en : world.getEntitiesByClasses(Horse.class)) {
			if (en.hasMetadata("mimiKon")) {
				for (Entity _en : en.getPassengers())
					_en.sendMessage(prefix + "Przeładowywanie Pluginu");
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
			przywołaj(Gracz.wczytaj(sender.getName()));
		else
			sender.sendMessage("Tylko gracz może tego użyć");
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
				p.sendMessage(prefix + "§4Ej! §cTo nie twój Konik ziom, nie siadaj.");
				ev.setCancelled(true);
				return;
			}
			if (!Gracz.wczytaj(kon.getMetadata("mimiKon").get(0).asString()).kon.nakarmiony()) {
				p.sendMessage(prefix + "Ten Konik jest zbyt głodny aby mógł cie wozić");
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
		otwórz(p, kon, ev);
	}
	@EventHandler
	public void klikanieKonika(PlayerInteractAtEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof Horse)) return;
		Horse kon = (Horse) ev.getRightClicked();
		Player p = ev.getPlayer();
		if (!kon.hasMetadata("mimiKon")) return;
		if (ev.getPlayer().isSneaking() && !kon.isAdult())
			otwórz(p, kon, ev);
		else {
			ItemStack item = p.getInventory().getItemInMainHand();
			if (item != null && mapa.containsKey(item.getType())) {
				int ile = mapa.get(item.getType());
				ev.setCancelled(true);
				Gracz gracz = Gracz.wczytaj(kon.getMetadata("mimiKon").get(0).asString());
				Player graczP = Bukkit.getPlayer(gracz.nick);
				if (gracz.kon.zapas >= System.currentTimeMillis() / 1000 + maxCzas) {
					p.sendMessage(prefix + "Ten Konik jest już najedzony");
					return;
				}
				gracz.kon.nakarm(ile, gracz);
				item.setAmount(item.getAmount() - 1);
				if (!gracz.kon.właściciel.equals(p.getName())) {
					p.sendMessage(prefix + Func.msg("Nakarmiłeś Konika gracza %s może on jezdzić na nim jeszcze %s bez karmienia", gracz.kon.właściciel, czas(gracz.kon.zapas)));
					graczP.sendMessage(prefix + Func.msg("%s Nakarmił twojego Konika, możesz jezdzić na nim jeszcze %s bez karmienia", p.getName(), czas(gracz.kon.zapas)));
				} else {
					p.sendMessage(prefix + Func.msg("Nakarmiłeś swojego Konika możesz jezdzić na nim jeszcze %s bez karmienia", czas(gracz.kon.zapas)));
				}
				return;
			}
			if (!kon.isAdult())
				kon.addPassenger(p);
		}
	}
	public static String czas(int zapas) {
		return Func.czas((int) (zapas - (System.currentTimeMillis() / 1000)));
	}
	private void otwórz(Player p, Horse kon, Cancellable ev) {
		if (kon.hasMetadata("mimiKon")) {
			ev.setCancelled(true);
			String nick = kon.getMetadata("mimiKon").get(0).asString();
			if (p.hasPermission("mimiRPG.konik.bypass") || 
					nick.equals(p.getName()))
				p.openInventory(Gracz.wczytaj(nick).kon.dajInv(kon));
			else
				p.sendMessage(prefix + "§4Ej! §cTo nie twój Konik ziom, nie dotykaj.");
		}
	}

	void przywołaj(Gracz g) {
		if (g.kon == null) {
			g.kon = new Kon(g);
			g.zapisz();
		}
		g.kon.przywołaj(Bukkit.getPlayer(g.nick));
	}
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getView().getTitle().equals("§1§lTwój Konik")) {
			Gracz g = Gracz.wczytaj(((Player) ev.getInventory().getHolder()).getName());
			g.kon.kliknięteEq(ev, g);
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getView().getTitle().equals("§1§lTwój Konik")) {
			Gracz g = Gracz.wczytaj(((Player) ev.getInventory().getHolder()).getName());
			g.kon.ustawItemy(ev.getInventory());
			g.zapisz();
		}
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Kon kon = Gracz.wczytaj(ev.getPlayer().getName()).kon;
		if (kon != null)
			kon.usuń();
	}
}

