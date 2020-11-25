package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class BonusoweSkrzynki extends Komenda implements Listener, Przeładowalny {
	public static class Skrzynka extends Mapowany {
		@Mapowane List<ItemStack> itemy;
		@Mapowane int minItemy = 1;
		@Mapowane int maxItemy = 1;
		@Mapowane ItemStack klucz;
		@Mapowane String nazwa;
		@Mapowane boolean broadcast = true;
		@Mapowane Material blok = Material.LIGHT_BLUE_SHULKER_BOX;
		
		void otwórz(Player p) {
			Inventory inv = Bukkit.createInventory(p, 3*9, Func.koloruj("&4&lOtwierasz pakę " + nazwa));
			// 13 środek
			
			if (broadcast)
				Bukkit.broadcastMessage(prefix + Func.msg("%s Otworzył pakę %s", p.getDisplayName(), Func.koloruj(nazwa)));
			
			Consumer<Supplier<Material>> ustawTło = sup -> {
				for (int i=0; i<inv.getSize(); i++)
					if (!(i == 4 || i == 22 || (i <= 15 && i >= 11)))
						inv.getItem(i).setType(sup.get());
			};
			
			ItemStack środek = Func.stwórzItem(Material.CHAIN, "&a&lWygrywająca");
			ItemStack szybka = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "&1&l ");
			for (int i=0; i<inv.getSize();i++)	inv.setItem(i, szybka);
			for (int i=11; i<16; i++)			inv.setItem(i, Func.losuj(itemy));
			inv.setItem(4, środek);
			inv.setItem(22, środek);

			// ((przejścia, wygraneitemy), runnable)
			Krotka<Krotka<Integer, Integer>, Runnable> k = new Krotka<>(new Krotka<>(0, Func.losuj(minItemy, maxItemy)), null);
			Runnable ruch = () -> {
				for (int i=11; i<15; i++)
					inv.setItem(i, inv.getItem(i+1));
				ustawTło.accept(() -> Func.losuj(szybki));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, .6f, 1f);
				inv.setItem(15, Func.losuj(itemy));
				Func.opóznij(5, k.b);
			};
			
			
			k.b = () -> {
				if (k.a.a++ < 10)
					ruch.run();
				else {
					k.a.a = 0;
					Func.dajItem(p, inv.getItem(13));
					Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
		            FireworkMeta fwm = fw.getFireworkMeta();
		            Supplier<Integer> los = () -> Func.losujWZasięgu(256);
					fwm.addEffect(FireworkEffect.builder()
							.with(Func.losuj(Type.values()))
							.flicker(Func.losuj(.5))
							.trail(true)
							.withFade(Color.fromBGR(los.get(), los.get(), los.get()))
							.withColor(Color.fromBGR(los.get(), los.get(), los.get()))
							.build()
							);
					fwm.setPower(2);
					fw.setFireworkMeta(fwm);
					if (--k.a.b > 0)
						Func.opóznij(40, k.b);
					else {
						Material mat = Func.losuj(szybki);
						Func.opóznij(10, () -> ustawTło.accept(() -> mat));
						Func.opóznij(20, () -> ustawTło.accept(() -> Func.losuj(szybki)));
						Func.opóznij(30, () -> {
							otwarteOtwieranie.remove(p.getName());
							p.closeInventory();
						});
					}
				}
			};
			Func.opóznij(10, k.b);
			p.removeScoreboardTag(Main.tagBlokOtwarciaJednorazowy);
			p.openInventory(inv);
			otwarteOtwieranie.add(p.getName());
		}

		void podgląd(Player p) {
			Inventory inv = gui(p, ((itemy.size() - 1) / 9 + 1) * 9, Func.koloruj("&1&lPodglądasz Pakę " + nazwa));

			ItemStack pusty = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&1&l ");
			for (int i=itemy.size(); i<inv.getSize(); i++)
				inv.setItem(i, pusty);
			
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		void edytuj(Player p) {
			gui(p, 6*9, nazwa);
			edytujący.put(p.getName(), this);
		}
		private Inventory gui(Player p, int sloty, String nazwa) {
			Inventory inv = Bukkit.createInventory(p, sloty, nazwa);
			int i=0;
			for (ItemStack item : itemy)
				inv.setItem(i++, item);
			p.openInventory(inv);
			return inv;
		}
	}	
	public static final String prefix = Func.prefix("Paki");
	static final HashMap<Location, Skrzynka> mapaSkrzyń = new HashMap<>();
	static final HashMap<String, Skrzynka> edytujący = new HashMap<>();
	static final Set<String> otwarteOtwieranie = Sets.newConcurrentHashSet();
	static final Material[] szybki = new Material[16];
	static {
		int i=0;
		for (DyeColor kolor : DyeColor.values())
			szybki[i++] = Material.valueOf(kolor + "_STAINED_GLASS_PANE");
	}
	
	static final String permEdycja = "mimiBonusoweSkrzynkiEdycja";
	public BonusoweSkrzynki() {
		super("bonusoweSkrzynki", "/bonusoweSkrzynki <czynność> <skrzynka> (gracz)");
		Main.dodajPermisje(permEdycja);
	}
	
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (otwarteOtwieranie.contains(ev.getWhoClicked().getName()))
			ev.setCancelled(true);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (otwarteOtwieranie.contains(ev.getPlayer().getName()))
			Func.opóznij(1, () -> ev.getPlayer().openInventory(ev.getInventory()));
		else 
			Func.wykonajDlaNieNull(edytujący.remove(ev.getPlayer().getName()), skrzynka -> {
				List<ItemStack> itemy = Lists.newArrayList();
				for (ItemStack item : ev.getInventory())
					Func.wykonajDlaNieNull(item, itemy::add);
				if (itemy.isEmpty())
					ev.getPlayer().sendMessage(prefix + "Nie możesz ustawić pustego dropu");
				else {
					Box box = (Box) config.wczytaj(ev.getView().getTitle());
					box.skrzynka.itemy = itemy;
					config.ustaw_zapisz(ev.getView().getTitle(), box);
					przeładuj();
					ev.getPlayer().sendMessage(prefix + "Zapisano skrzynkę");
				}
			});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void stawianie(BlockPlaceEvent ev) {
		if (!ev.getPlayer().hasPermission(permEdycja)) return;
		if (!ev.getItemInHand().getItemMeta().hasItemFlag(ItemFlag.HIDE_PLACED_ON)) return;
		//Func.stwórzItem(box.skrzynka.blok, Func.koloruj("&b&l&oPaka " + box.skrzynka.nazwa));
		String nazwa = ev.getItemInHand().getItemMeta().getDisplayName();
		nazwa = nazwa.substring(nazwa.indexOf(' ') + 1);
		Func.wykonajDlaNieNull((Box) config.wczytaj(nazwa), box -> {
			box.lokacje.add(ev.getBlock().getLocation());
			mapaSkrzyń.put(ev.getBlock().getLocation(), box.skrzynka);
			config.ustaw_zapisz(box.skrzynka.nazwa, box);
		});
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void niszczenie(BlockBreakEvent ev) {
		if (ev.getPlayer().hasPermission(permEdycja)) {
				Func.wykonajDlaNieNull(mapaSkrzyń.get(ev.getBlock().getLocation()), skrzynka -> {
					ev.setCancelled(true);
					if (ev.getPlayer().isSneaking()) {
						Func.opóznij(1, () -> ev.getBlock().setType(Material.AIR));
						Box box = (Box) config.wczytaj(skrzynka.nazwa);
						box.lokacje.remove(ev.getBlock().getLocation());
						mapaSkrzyń.remove(ev.getBlock().getLocation());
						config.ustaw_zapisz(skrzynka.nazwa, box);
					}
				});
		}
	}
	@EventHandler
	public void klikanieSkrzyni(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();
		Krotka<Consumer<Skrzynka>, Runnable> k = new Krotka<>(null, null);
		switch (ev.getAction()) {
		case LEFT_CLICK_BLOCK:
			k.a = skrzynia ->  skrzynia.podgląd(p);
			break;
		case RIGHT_CLICK_BLOCK:
			k.b = () -> p.addScoreboardTag(Main.tagBlokOtwarciaJednorazowy);
			k.a = skrzynia -> {
				if (p.getInventory().getItemInMainHand().isSimilar(skrzynia.klucz)) {
					Func.zabierzItem(p.getInventory(), EquipmentSlot.HAND);
					skrzynia.otwórz(p);
				} else
					p.sendMessage(prefix + "Musisz trzymać odpowiedni klucz aby to otworzyć");
			};
			break;
		default:
			return;
		}
		Func.wykonajDlaNieNull(mapaSkrzyń.get(ev.getClickedBlock().getLocation()), skrzynia -> {
					ev.setCancelled(true);
					Func.opóznij(1, () -> k.a.accept(skrzynia));
					Func.wykonajDlaNieNull(k.b, Runnable::run);
				});
	}
	
	// Operacje na plikach
	

	
	public static class Box extends Mapowany {
		@Mapowane List<Location> lokacje;
		@Mapowane Skrzynka skrzynka;
	}
	
	Config config = new Config("configi/Bonusowe Skrzynki");
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		mapaSkrzyń.clear();
		for (Box box : config.wartości(Box.class))
			for (Location loc : box.lokacje) {
				mapaSkrzyń.put(loc, box.skrzynka);
				loc.getBlock().setType(box.skrzynka.blok);
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Skrzynki", config.klucze(false).size());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 0:
		case 1:
			return utab(args, "edytuj", "klucz", "skrzynka"); // TODO rozbudować
		case 2:
			return utab(args, config.klucze(false));
		}
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		if (args.length < 2)
			return false;
		Player p = (Player) sender;
		try {
			p = Bukkit.getPlayer(args[2]);
		} catch (Throwable e) {}
		Box box = (Box) config.wczytaj(args[1]);
		if (box == null)
			return Func.powiadom(sender, prefix + "Niepoprawna nazwa paki");
		switch (args[0]) {
		case "e":
		case "edytuj":
			box.skrzynka.edytuj((Player) sender);
			break;
		case "s":
		case "skrzynka":
			ItemStack item = Func.stwórzItem(box.skrzynka.blok, Func.koloruj("&b&l&oPaka " + box.skrzynka.nazwa));
			Func.ukryj(item, ItemFlag.HIDE_PLACED_ON);
			Func.dajItem(p, item);
			break;
		case "k":
		case "klucz":
			Func.dajItem(p, box.skrzynka.klucz);
			break;
		}
		return true;
	}
}

