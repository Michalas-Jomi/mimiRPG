package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.chat.ClickEvent;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

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
			drop.dropnijNaRandSloty(blok.getSnapshotInventory());
		}
	}
	public static class Edytor {
		static final HashMap<String, Edytor> mapa = new HashMap<>();
		final CommandSender p;
		final Skrzynia skrzynia = Func.utwórz(Skrzynia.class);
		Edytor(CommandSender p) {
			skrzynia.drop.drop = Lists.newArrayList();
			skrzynia.drop.min_ilość = 10;
			skrzynia.drop.max_ilość = 20;
			
			this.p = p;
			mapa.put(p.getName(), this);
		}
		
		void wyświetl() {
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
							Func.koloruj("&6Ilość Itemów: " + skrzynia.drop.min_ilość + "-" + skrzynia.drop.max_ilość),
							Func.koloruj("&bKliknij aby zmienić&3format: &amin ilość&7-&amax ilość\n\n&8Oznacza ilość itemów w skrzyni"),
							"/edytujskarby ilośćItemów >> "
							)
					);
			n.dodajK("&6Dropy:\n");
			for (int i=0; i < skrzynia.drop.drop.size(); i++) {
				n.dodajK("&b&l- ");
				// item, szansa, min_ilość, max_ilość, rolle
				Drop drop = skrzynia.drop.drop.get(i);
				Napis item = Napis.item(drop.item);
				item.clickEvent(ClickEvent.Action.RUN_COMMAND, "/edytujskarby itemy " + i + "ustaw");
				n.dodaj(item);
				n.dodaj(" ");
				n.dodaj(new Napis(
						Func.koloruj("&e" + (drop.szansa / 100) + "%"),
						Func.koloruj("&bKliknij aby zmienić\n&3format: &a0.szansa\n&3format: &aszansa%\n\n&8Oznacza szanse na wypadnięcie itemu\\n&8dozwolone wartości z zakresu 0.0 < x <= 1.0\n&8dozwolone wartości z zakresu 0% < x <= 100%"),
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
			n.dodaj(new Napis(
					Func.koloruj("&a[dodaj]"),
					Func.koloruj("&bKliknij aby dodać itemek z ręki"),
					"/edytujskarby dodajItem "
					));
			n.dodajK("\n\n&5-----");
			n.dodaj(new Napis(
					Func.koloruj("&a[zatwierdz]"),
					Func.koloruj("&bKliknij aby zatwierdzić"),
					"/edytujskarby zatwierdz"
					));
			n.dodajK("&5-----\n\n");
			n.wyświetl(p);
		}
	}
	
	
	static final Config config = new Config("configi/SkrzynieSkarbów");
	static final HashMap<String, Skrzynia> mapaSkrzyń = new HashMap<>();
	static final HashMap<String, Krotka<Skrzynia, BlockData>> mapa = new HashMap<>();
	
	
	// TODO wczytywać
	int tickiUsuwaniaSkrzyń = 30*20;
	
	
	static String locToString(Location loc) {
		return locToString(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	static String locToString(World świat, int x, int y, int z) {
		return String.join(",", świat.getName(), String.valueOf(x), String.valueOf(y), String.valueOf(z));
	}
	static Location stringToLoc(String str) {
		String[] strs = str.split(",");
		return new Location(Bukkit.getWorld(strs[0]), Func.Int(strs[1]), Func.Int(strs[2]), Func.Int(strs[3]));
	}

	
	// TODO pzy włączaniu modułu zrespić wszystkie skrzynki
	/*
	 * skrzynie:
	 *   nazwa: Skrzynia
	 * 
	 * lokacje:// TODO syntax nazwy skrzyni bez ^
	 *   locToString: nazwaSkrzyni^BlockData
	 * 
	 */
	
	final Set<String> usuwane = Sets.newConcurrentHashSet();
	@EventHandler
	public void otwieranieEq(InventoryOpenEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Container.class, holder -> {
			String klucz = locToString(holder.getLocation());
			if (!usuwane.contains(klucz))
				Func.wykonajDlaNieNull(mapa.get(klucz), krotka -> {
					usuwane.add(klucz);
					Func.opóznij(tickiUsuwaniaSkrzyń, () -> {
						holder.getLocation().getBlock().setType(Material.AIR);
						Func.opóznij(krotka.a.czasOdrespiania * 20, () -> {
							krotka.a.zresp(holder.getLocation(), krotka.b);
							usuwane.remove(klucz);
						});
					});
				});
		});
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		mapaSkrzyń.clear();
		Func.wykonajDlaNieNull(config.sekcja("skrzynie"), sekcja ->
				sekcja.getValues(false).forEach((nazwa, skrzynia) ->
						mapaSkrzyń.put(nazwa, (Skrzynia) skrzynia)));
		
		mapa.clear();
		Func.wykonajDlaNieNull(config.sekcja("lokacje"), sekcja -> 
				sekcja.getValues(false).forEach((str, obj) -> {
					String[] strs = ((String) obj).split("^");
					mapa.put(str, new Krotka<>(mapaSkrzyń.get(strs[0]), Bukkit.createBlockData(strs[1])));
				}));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Skrzynie Skarbów", mapaSkrzyń.size() + "/" + mapa.size());
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Edytor edytor = Edytor.mapa.get(sender.getName());
		Player p = (Player) sender;
		String[] minmax;
		switch(args[0]) {
		case "-t":
			edytor = new Edytor(p);
			break;
		case "nazwa":
			edytor.skrzynia.nazwa = Func.koloruj(Func.listToString(args, 2));
			break;
		case "respawn":
			edytor.skrzynia.czasOdrespiania = Func.czas(Func.listToString(args, 2));
			break;
		case "ilośćItemów":
			minmax = args[2].split("-");
			edytor.skrzynia.drop.min_ilość = Func.Int(minmax[0]);
			edytor.skrzynia.drop.max_ilość = Func.Int(minmax[1]);
			break;
		case "dodajItem":
			edytor.skrzynia.drop.drop.add(new Drop(p.getInventory().getItemInMainHand()));
			break;
		case "zatwierdz":
			mapaSkrzyń.put(edytor.skrzynia.nazwa, edytor.skrzynia);
			if (mapaSkrzyń.containsKey(edytor.skrzynia.nazwa))
				return Func.powiadom(sender, prefix + "Ta nazwa jest już zajęta");
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
				drop.szansa = Func.Double(args[3]);
				break;
			case "ilość":
				minmax = args[3].split("-");
				drop.min_ilość = Func.Int(minmax[0]);
				drop.max_ilość = Func.Int(minmax[1]);
				break;
			case "rolle":
				if (args[3].startsWith("x"))
					args[3] = args[3].substring(1);
				drop.rolle = Func.Int(args[3]);
				break;
			case "usuń":
				edytor.skrzynia.drop.drop.remove(i);
				break;
			}
		}
		edytor.wyświetl();
		return true;
	}
}








