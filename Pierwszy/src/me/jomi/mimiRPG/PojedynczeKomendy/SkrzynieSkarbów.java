package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.chat.ClickEvent;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

// TODO przetestować te skrzynie

@Moduł
public class SkrzynieSkarbów extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Skrzynie Skarbów");
	
	public SkrzynieSkarbów() {
		super("edytujskarby");
	}
	public static class Skrzynia extends Mapowany {
		@Mapowane String nazwa = "&4Skrzynia Skarbów";
		@Mapowane Drop drop;
		@Mapowane int czasOdrespiania;
		
		void Init() {
			nazwa = Func.koloruj(nazwa);
		}
		
		public void zresp(Location loc, BlockData data) {
			loc.getBlock().setBlockData(data);
			Container blok = (Container) loc.getBlock().getState();
			blok.setCustomName(nazwa);
			drop.dropnijNaRandSloty(blok.getInventory());
		}
	}
	public static class Edytor {
		static final HashMap<String, Edytor> mapa = new HashMap<>();
		final CommandSender p;
		final Skrzynia skrzynia;
		Edytor(CommandSender p) {
			this(p, Func.utwórz(Skrzynia.class));
			skrzynia.drop = new Drop();
			skrzynia.drop.drop = Lists.newArrayList();
			skrzynia.drop.min_ilość = 10;
			skrzynia.drop.max_ilość = 20;
		}
		Edytor(CommandSender p, Skrzynia skrzynia) {
			this.skrzynia = skrzynia;
			this.p = p;
			
			mapa.put(p.getName(), this);
		}
		
		
		void wyświetl(int start) {
			Napis n = new Napis("\n\n\n");
			n.dodajK("&a~~ &2Edytor Skarbów &a~~\n\n");
			n.dodajEnd(
					new Napis(
							Func.koloruj("&6Nazwa: &e" + skrzynia.nazwa),
							Func.koloruj("&bKliknij aby zmienić"),
							"/edytujskarby nazwa >> "
							),
					new Napis(
							Func.koloruj("&6Respawn: &e" + Func.czas(skrzynia.czasOdrespiania)),
							Func.koloruj("&bKliknij aby zmienić\n&3format: &d(&egodziny&ag&d)(&eminuty&am&d)(&esekundy&as&d)\n&3format np.: &a1m30s &7 - 90 sekund (minuta i 30 sek)"),
							"/edytujskarby respawn >> "
							),
					new Napis(
							Func.koloruj("&6Ilość Itemów: &e" + skrzynia.drop.min_ilość + "-" + skrzynia.drop.max_ilość),
							Func.koloruj("&bKliknij aby zmienić\n&3format: &amin ilość&7-&amax ilość\n\n&8Oznacza ilość itemów w skrzyni"),
							"/edytujskarby ilośćItemów >> "
							)
					);
			n.dodajK("&6Dropy:\n");
			int i = Math.max(0, start);
			while (i < skrzynia.drop.drop.size() && i < start + mxLinie)
				dodajItem(n, i++);
			if (i >= skrzynia.drop.drop.size())
				n.dodaj(new Napis(
						Func.koloruj("&a[dodaj]"),
						Func.koloruj("&bKliknij aby dodać itemek z ręki"),
						"/edytujskarby dodajItem " + start
						));
			
			n.dodajK("\n&8-----");
			n.dodaj(new Napis(
					Func.koloruj("&6<<<"),
					Func.koloruj("&bPrzejdz na poprzednią stronę"),
					"/edytujskarby itemy " + Math.max(0, start - mxLinie) + " nic"
					));
			n.dodajK("&8---");
			n.dodaj(new Napis(
					Func.koloruj("&6>>>"),
					Func.koloruj("&bPrzejdz na następną stronę"),
					"/edytujskarby itemy " + Math.min((skrzynia.drop.drop.size() - 1) / mxLinie * mxLinie, start + mxLinie) + " nic"
					));
			n.dodajK("&8-----\n");
			
			
			n.dodajK("\n&5-----");
			n.dodaj(new Napis(
					Func.koloruj("&a[zatwierdz]"),
					Func.koloruj("&bKliknij aby zatwierdzić"),
					"/edytujskarby zatwierdz"
					));
			n.dodajK("&5-----");
			
			
			n.wyświetl(p);
		}
		void dodajItem(Napis n, int i) {
			n.dodajK("&b&l- ");
			// item, szansa, min_ilość, max_ilość, rolle
			Drop drop = skrzynia.drop.drop.get(i);
			Napis item = Napis.item(drop.item);
			item.clickEvent(ClickEvent.Action.RUN_COMMAND, "/edytujskarby itemy " + i + " ustaw");
			n.dodaj(item);
			n.dodaj(" ");
			n.dodaj(new Napis(
					Func.koloruj("&e" + (drop.szansa * 100) + "%"),
					Func.koloruj("&bKliknij aby zmienić\n&3format: &a0.szansa\n&3format: &aszansa%\n\n&8Oznacza szanse na wypadnięcie itemu\n&8dozwolone wartości z zakresu 0.0 < x <= 1.0\n&8dozwolone wartości z zakresu 0% < x <= 100%"),
					"/edytujskarby itemy " + i + " szansa >> "
					));
			n.dodaj(" ");
			n.dodaj(new Napis(
					Func.koloruj("&e" + drop.min_ilość + "-" + drop.max_ilość),
					Func.koloruj("&bKliknij aby zmienić\n&3format: &amin ilość&b-&amax ilość\n\n&8Oznacza ilość itemu w slocie\n&8dozwolone wartości z zakresu 1-64"),
					"/edytujskarby itemy " + i + " ilość >> "
					));
			n.dodaj(" ");
			n.dodaj(new Napis(
					Func.koloruj("&ex" + drop.rolle),
					Func.koloruj("&bKliknij aby zmienić\n&3format: &arolle\n&3format: &axrolle\n\n&8Oznacza ilość losowań itemka\n&8dozwolone wartości z zakresu 0 < x"),
					"/edytujskarby itemy " + i + " rolle >> "
					));
			n.dodaj("  ");
			n.dodaj(new Napis(
					Func.koloruj("&c{x}"),
					Func.koloruj("&cKliknij aby &4Usunąć &citemek z listy"),
					"/edytujskarby itemy " + i + " usuń >> potwierdzam usunięcie itemka z listy",
					ClickEvent.Action.SUGGEST_COMMAND
					));
			n.dodaj("\n");
		}
	}
	
	static final Config config = new Config("configi/SkrzynieSkarbów");
	static final HashMap<String, Skrzynia> mapaSkrzyń = new HashMap<>();
	static final HashMap<String, Krotka<Skrzynia, BlockData>> mapa = new HashMap<>();
	
	static int mxLinie;
	int tickiUsuwaniaSkrzyń;
	
	
	String locToString(Location loc) {
		return locToString(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	String locToString(World świat, int x, int y, int z) {
		return String.join(",", świat.getName(), String.valueOf(x), String.valueOf(y), String.valueOf(z));
	}
	Location stringToLoc(String str) {
		String[] strs = str.split(",");
		return new Location(Bukkit.getWorld(strs[0]), Func.Int(strs[1]), Func.Int(strs[2]), Func.Int(strs[3]));
	}
	
	
	private boolean zrespioneWszystkie = false;
	void zrespWszystkieSkrzynie() {
		zrespioneWszystkie = true;
		mapa.forEach((strLoc, krotka) -> {
			Location loc = stringToLoc(strLoc);
			if (loc.getBlock().getType().isAir())
				krotka.a.zresp(loc, krotka.b);
		});
	}
	
	
	/*
	 * skrzynie:
	 *   nazwa: Skrzynia
	 * 
	 * lokacje:
	 *   locToString: nazwaSkrzyni^BlockData
	 * 
	 */
	private final Set<String> usuwane = Sets.newConcurrentHashSet();
	@EventHandler
	public void otwieranieEq(InventoryOpenEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Container.class, holder -> {
			String klucz = locToString(holder.getLocation());
			if (!usuwane.contains(klucz))
				Func.wykonajDlaNieNull(mapa.get(klucz), krotka -> {
					usuwane.add(klucz);
					Func.opóznij(tickiUsuwaniaSkrzyń, () -> {
						holder.getInventory().clear();
						holder.getLocation().getBlock().setType(Material.AIR);
						Func.opóznij(krotka.a.czasOdrespiania * 20, () -> {
							if (mapa.containsKey(klucz)) 
								krotka.a.zresp(holder.getLocation(), krotka.b);
							usuwane.remove(klucz);
						});
					});
				});
		});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void niszczenie(BlockBreakEvent ev) {
		if (mapa.containsKey(locToString(ev.getBlock().getLocation())))
			ev.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void stawianie(BlockPlaceEvent ev) {
		if (ev.getItemInHand().hasItemMeta() && ev.getItemInHand().getItemMeta().hasCustomModelData() && ev.getItemInHand().getItemMeta().getCustomModelData() == 7345)
			Func.wykonajDlaNieNull(ev.getBlock().getState(),  Container.class, container -> {
				String nazwa = ev.getItemInHand().getItemMeta().getDisplayName();
				String klucz = locToString(container.getLocation());
				if (mapaSkrzyń.containsKey(nazwa) && !mapa.containsKey(klucz)) {
					BlockData data = ev.getBlock().getBlockData();
					config.ustaw_zapisz("lokacje." + klucz , nazwa + "^" + data.getAsString());
					mapa.put(klucz, new Krotka<>(mapaSkrzyń.get(nazwa), data));
					ev.getPlayer().sendMessage(prefix + Func.msg("Postawiono %s", nazwa));
				}
			});
	}
	
	
	@Override
	public void przeładuj() {
		config.przeładuj();

		mxLinie = Main.ust.wczytajLubDomyślna("SkrzynieSkarbów.mxLinie", 10);
		tickiUsuwaniaSkrzyń = Main.ust.wczytajLubDomyślna("SkrzynieSkarbów.czasUsuwaniaSkrzyń", 30)*20;
		
		mapaSkrzyń.clear();
		Func.wykonajDlaNieNull(config.sekcja("skrzynie"), sekcja ->
				sekcja.getValues(false).forEach((nazwa, skrzynia) ->
						mapaSkrzyń.put(nazwa, (Skrzynia) skrzynia)));
		
		mapa.clear();
		Func.wykonajDlaNieNull(config.sekcja("lokacje"), sekcja -> 
				sekcja.getValues(false).forEach((str, obj) -> {
					List<String> strs = Func.tnij((String) obj, "^");
					if (mapaSkrzyń.containsKey(strs.get(0)))
						mapa.put(str, new Krotka<>(mapaSkrzyń.get(strs.get(0)), Bukkit.createBlockData(strs.get(1))));
				}));
		
		if (!zrespioneWszystkie)
			zrespWszystkieSkrzynie();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Skrzynie Skarbów", mapaSkrzyń.size() + "/" + mapa.size());
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "daj", "usuń", "nowa", "modyfikuj");
		if (args.length >= 2 && Func.multiEquals(args[0].toLowerCase(), "daj", "modyfikuj"))
			return utab(args, Func.wykonajWszystkim(mapaSkrzyń.keySet(), Func::odkoloruj));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = sender instanceof Player ? (Player) sender : null;
		Edytor edytor = Edytor.mapa.get(sender.getName());
		String[] minmax;
		try {
			switch(args[0].toLowerCase()) {
			case "daj":
				Func.wykonajDlaNieNull(mapaSkrzyń.get(Func.koloruj(Func.listToString(args, 1))),
						skrzynia -> Func.dajItem(p, Func.customModelData(Func.stwórzItem(Material.CHEST, skrzynia.nazwa, "&bPostaw ją gdziekolwiek", "&bAby stała się skrzynią skarbów"), 7345)),
						() -> sender.sendMessage(prefix + "Skrzynia o tej nazwie nie istnieje")
						);
				return true;
			case "usuń":
				Block skrzynia = p.getTargetBlockExact(5, FluidCollisionMode.NEVER);
				String klucz = locToString(skrzynia.getLocation());
				Func.wykonajDlaNieNull(mapa.get(klucz), krotka -> {
					mapa.remove(klucz);
					config.ustaw_zapisz("lokacje." + klucz, null);
					sender.sendMessage(prefix + "Usunięto skrzynie skarbów");
				}, () -> sender.sendMessage(prefix + "Musisz patrzeć się na skrzynie skarbów aby ją usunąć"));
				return true;
			case "nowa":
				edytor = new Edytor(p);
				break;
			case "modyfikuj":
				Skrzynia skrzynka = mapaSkrzyń.get(Func.koloruj(Func.listToString(args, 1)));
				if (skrzynka == null)
					return Func.powiadom(sender, prefix + "Niepoprawna nazwa skrzyni skarbów");
				else
					edytor = new Edytor(p, skrzynka);
				break;
			// edytor
			case "nazwa":
				String nazwa = Func.koloruj(Func.listToString(args, 2));
				if (nazwa.contains("^"))
					return Func.powiadom(sender, prefix + "Nazwa nie może zawierać symbolu ^");
				edytor.skrzynia.nazwa = nazwa;
				break;
			case "respawn":
				//TODO sprawdzić bo nie działa
				edytor.skrzynia.czasOdrespiania = Func.czas(Func.listToString(args, 2));
				break;
			case "ilośćitemów":
				minmax = args[2].split("-");
				edytor.skrzynia.drop.min_ilość = Func.Int(minmax[0]);
				edytor.skrzynia.drop.max_ilość = Func.Int(minmax[1]);
				break;
			case "dodajitem":
				edytor.skrzynia.drop.drop.add(new Drop(p.getInventory().getItemInMainHand()));
				edytor.wyświetl(Func.Int(args[1], 0));
				return true;
			case "zatwierdz":
				mapaSkrzyń.put(edytor.skrzynia.nazwa, edytor.skrzynia);
				config.ustaw_zapisz("skrzynie." + edytor.skrzynia.nazwa, edytor.skrzynia);
				Edytor.mapa.remove(sender.getName());
				return Func.powiadom(sender, prefix + "Zapisano");
			case "itemy":
				int i = Func.Int(args[1]);
				Drop drop = edytor.skrzynia.drop.drop.get(i);
				switch (args[2]) {
				case "ustaw":
					drop.item = p.getInventory().getItemInMainHand();
					break;
				case "szansa":
					drop.szansa = Func.Double(args[4]);
					break;
				case "ilość":
					minmax = args[4].split("-");
					drop.min_ilość = Func.Int(minmax[0]);
					drop.max_ilość = Func.Int(minmax[1]);
					break;
				case "rolle":
					if (args[4].startsWith("x"))
						args[4] = args[4].substring(1);
					drop.rolle = Func.Int(args[4]);
					break;
				case "usuń":
					edytor.skrzynia.drop.drop.remove(i);
					break;
				}
				edytor.wyświetl(i / mxLinie * mxLinie);
				return true;
			}
			edytor.wyświetl(0);
		} catch (Throwable e) {
			sender.sendMessage(prefix + "Niepoprawne argumenty");
		}
		return true;
	}
}

