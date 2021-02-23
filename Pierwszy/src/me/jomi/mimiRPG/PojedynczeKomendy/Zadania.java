package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.Box;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Zadania extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Zadania");
	public static enum Rodzaj {
		ZABIJ,
		WYKOP,
		DOSTARCZ,
		ZDOBĄDZ;
	}
	public static enum Status {
		DO_ODEBRANIA(Material.LIME_STAINED_GLASS_PANE),
		DO_PRZYJĘCIA(Material.YELLOW_STAINED_GLASS_PANE),
		UKOŃCZONE(Material.GREEN_STAINED_GLASS_PANE),
		W_TRAKCIE(Material.PINK_STAINED_GLASS_PANE),
		NIE_DOSTĘPNE(Material.RED_STAINED_GLASS_PANE);
		
		Material ikona;
		Status(Material ikona) {
			this.ikona = ikona;
		}
	}
	public static class Kryterium extends Mapowany {
		@Mapowane Rodzaj rodzaj = Rodzaj.DOSTARCZ;
		@Mapowane Object czego;
		@Mapowane int ile = 1;
		
		private Object czegoObj;
		public <T> T getCzego() {
			return Func.pewnyCast(czegoObj);
		}
		
		@Override
		public void Init() {
			try {
				switch (rodzaj) {
				case ZDOBĄDZ:
				case DOSTARCZ:
					czegoObj = Config.item(czego);
					break;
				case WYKOP:
					czegoObj = Func.StringToEnum(Material.class, (String) czego);
					break;
				case ZABIJ:
					czegoObj = Func.StringToEnum(EntityType.class, (String) czego);
					break;
				}
			} catch (Throwable e) {
				Main.warn("Niepoprawny wskaźnik(" + czego + ") w jednym z kryteriów Zadań, kryterium wymaga: " + rodzaj + " " + ile + " " + czego);
			}
		}
	}
	public static class Zadanie extends Mapowany {
		@Mapowane String id;
		@Mapowane String nazwaWyświetlana;
		@Mapowane List<String> opis;
		@Mapowane List<String> wymagane;
		@Mapowane List<ItemStack> nagroda;
		@Mapowane List<String> cmdsStart; // placeholders - %nick% %displayname%
		@Mapowane List<String> cmdsKoniec; // ^
		@Mapowane List<Kryterium> kryteria;
		@Mapowane boolean autoObieranie = false;
		
		@Override
		public void Init() {
			opis = Func.koloruj(opis);
			nazwaWyświetlana = Func.koloruj(nazwaWyświetlana);
		}
		
		// nie zapisuje g.zapisz()
		int debugUkończ(Gracz g, Player p) {
			if (g.zadania.ukończone.contains(id))
				return 0;
			Box<Integer> box = new Box<>(1);
			
			wymagane.forEach(str -> Func.wykonajDlaNieNull(wczytaj(str), zadanie -> box.a += zadanie.debugUkończ(g, p)));
			
			for (AktywneZadanie zadanie : g.zadania.aktywne)
				if (zadanie.zadanie.equals(id)) {
					zadanie.odbierzNagrode(p);
					return box.a;
				}
			
			przyjmij(p).odbierzNagrode(p);
			return box.a;
		}
		// nie zapisuje g.zapisz()
		int debugZapomnij(Gracz g) {
			if (!g.zadania.ukończone.remove(id))
				return 0;
			Box<Integer> box = new Box<>(1);
			
			mapaZadań.values().forEach(zadanie -> {
				if (zadanie.wymagane.contains(id))
					box.a += zadanie.debugZapomnij(g);
			});
			
			return box.a;
		}
		
		
		boolean możePrzyjąć(Player p) {
			Gracz g = Gracz.wczytaj(p);
			
			if (g.zadania.ukończone.contains(id))
				return false;
			
			for (AktywneZadanie zadanie : g.zadania.aktywne)
				if (zadanie.zadanie.equals(id))
					return false;
			
			return g.zadania.ukończone.containsAll(wymagane);
		}
		
		AktywneZadanie przyjmij(Player p) {
			cmdsStart.forEach(cmd -> 
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						cmd.replace("%nick%", p.getName()).replace("%displayname%", p.getDisplayName())));
			
			Gracz g = Gracz.wczytaj(p);
			
			AktywneZadanie zadanie = Func.utwórz(AktywneZadanie.class);
			zadanie.zadanie = id;
			g.zadania.aktywne.add(zadanie);
			
			g.zapisz();
			
			p.sendMessage(prefix + Func.msg("Przyjołeś nowe zadanie: %s", nazwaWyświetlana));
			
			return zadanie;
		}
	}
	public static class AktywneZadanie extends Mapowany {
		@Mapowane String zadanie;
		@Mapowane List<Integer> postępKryteriow;
		private boolean ukończone = false;
		
		int getPostęp(int i) {
			Func.wykonajDlaNieNull(wczytaj(zadanie), zadanie -> {
				while (postępKryteriow.size() < zadanie.kryteria.size())
					postępKryteriow.add(0);
			});
			return postępKryteriow.get(i);
		}
		
		
		void zwiększ(Player p, int i, int ile) {
			postępKryteriow.set(i, getPostęp(i) + ile);
			if (ukończone())
				Func.wykonajDlaNieNull(wczytaj(zadanie), zadanie -> {
					if (zadanie.autoObieranie)
						odbierzNagrode(p);
				});
		}

		void odbierzNagrode(Player p) {
			Gracz g = Gracz.wczytaj(p);
			for (int i=0; i < g.zadania.aktywne.size(); i++)
				if (g.zadania.aktywne.get(i).zadanie.equals(zadanie)) {
					g.zadania.ukończone.add(zadanie);
					g.zadania.aktywne.remove(i);
					Func.wykonajDlaNieNull(wczytaj(zadanie), zadanie -> {
						zadanie.nagroda.forEach(item -> Func.dajItem(p, item));
						zadanie.cmdsKoniec.forEach(cmd -> 
								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
											cmd.replace("%nick%", p.getName()).replace("%displayname%", p.getDisplayName())));
					});
					g.zapisz();
					p.sendMessage(prefix + Func.msg("Ukończyłeś zadanie %s", zadanie));
					return;
				}
		}
		
		boolean doOdebrania() {
			Zadanie zadanie = wczytaj(this.zadanie);
			return zadanie != null && ukończone();
		}
		
		boolean dostarcz(Player p, Gracz g) {
			Krotka<Boolean, ?> krotka = new Krotka<>(false, null);
			ItemStack item = p.getInventory().getItemInMainHand();
			sprawdzZadanie(p, g, Rodzaj.DOSTARCZ, item, item::isSimilar, this, k -> {
				krotka.a = true;
				
				int ile = Math.min(item.getAmount(), k.ile);
				item.setAmount(item.getAmount() - ile);
				p.getInventory().setItemInMainHand(item.getAmount() <= 0 ? null : item);
				return ile;
			});
			sprawdzZadanie(p, g, Rodzaj.ZDOBĄDZ, item, item::isSimilar, this, k -> Math.min(item.getAmount(), k.ile));
			return krotka.a;
		}
		
		boolean ukończone() {
			return ukończone ? ukończone : (ukończone = spełnione());
		}
		private boolean spełnione() {
			Box<Boolean> box = new Box<>(true);
			Func.wykonajDlaNieNull(wczytaj(this.zadanie), zadanie -> {
				for (int i=0; i < zadanie.kryteria.size(); i++)
					if (getPostęp(i) < zadanie.kryteria.get(i).ile) {
						box.a = false;
						return;
					}
			});
			return box.a;
		}
	}
	public static class ZadaniaGracza extends Mapowany {
		@Mapowane List<String> ukończone;
		@Mapowane List<AktywneZadanie> aktywne;
	}
	
	public static class InvHolder extends Func.abstractHolder {
		//	(zadanie, args1 ? przyjmować : odbierać)
		List<Krotka<Zadanie, Status>> zadania;
		public InvHolder(List<Krotka<Zadanie, Status>> zadania, List<String> ukończone) {
			super(Func.potrzebneRzędy(zadania.size()), "&4&lZadania");
			this.zadania = zadania;
			Func.ustawPuste(inv);
			Krotka<Integer, ?> i = new Krotka<>(0, null);
			zadania.forEach(krotka ->  {
				ItemStack item = Func.stwórzItem(krotka.b.ikona,
						"&c" + krotka.a.nazwaWyświetlana,
						krotka.a.opis);
				switch (krotka.b) {
				case DO_ODEBRANIA:	Func.dodajLore(item, "&aDo odebrania");	break;
				case DO_PRZYJĘCIA:	Func.dodajLore(item, "&aDo przyjęcia");	break;
				case UKOŃCZONE:		Func.dodajLore(item, "&aUkończone");	break;
				case W_TRAKCIE:		Func.dodajLore(item, "&dW trakcie");	break;
				case NIE_DOSTĘPNE:
					if (!krotka.a.wymagane.isEmpty()) {
						Func.dodajLore(item, "&6Wymagane zadania:");
						krotka.a.wymagane.forEach(str -> Func.dodajLore(item, "&" + (ukończone.contains(str) ? "a" : "c") + "- " +
								Func.domyślnaTry(() -> wczytaj(str).nazwaWyświetlana, str)));
					} else {
						Func.dodajLore(item, "&cNiedostępne");
					}
					break;
				}
				inv.setItem(i.a++, item);
				});
		}
	}
	
	
	public static final String permInfo = Func.permisja("zadania.czyjeś");
	
	static Zadanie wczytaj(String nazwa) {
		return mapaZadań.get(nazwa);
	}
	
	public Zadania() {
		super("zadania");
		ustawKomende("zadaniaadmin", null, null);

		Main.dodajPermisje(permInfo);
		
		edytor.zarejestrójWyjątek("/zadaniaadmin edytor kryteria <int> czego", (zadanie, ścieżka) -> {
			int index = Func.Int(Func.tnij(ścieżka, " ").get(3));
			Kryterium k = zadanie.kryteria.get(index);
			switch (k.rodzaj) {
			case ZDOBĄDZ:
			case DOSTARCZ:
				return new Napis("§6item§8: ")
						.dodaj((k.getCzego() instanceof ItemStack ? Napis.item((ItemStack) k.getCzego()) : new Napis("§enull"))
								.clickEvent(Action.RUN_COMMAND, ścieżka + "() org.bukkit.inventory.ItemStack >>"));
			case WYKOP:
				return new Napis("§6blok§8: ").dodaj(new Napis(
						"§e" + (k.getCzego() instanceof Material ? k.getCzego() : "null"),
						"§bKliknij aby ustawić",
						ścieżka + "() org.bukkit.Material >> "
						));
			case ZABIJ:
				return new Napis("§6mob§8: ").dodaj(new Napis(
						"§e" + (k.getCzego() instanceof EntityType ? k.getCzego() : "null"),
						"§bKliknij aby ustawić",
						ścieżka + "() org.bukkit.entity.EntityType >> "
						));
			}
			return null;
		});
		
		edytor.zarejestrójWyjątek("/zadaniaadmin edytor id", (zadanie, ścieżka) -> null);
		edytor.zarejestrujOnZatwierdz((zadanie, ścieżka) -> zadanie.id = ścieżka);
		edytor.zarejestrujPoZatwierdz((dawneZadanie, zadanie) -> przeładuj());
	}
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void zabicie(EntityDeathEvent ev) {
		Func.wykonajDlaNieNull(ev.getEntity().getKiller(), p -> sprawdzZadania(p, Rodzaj.ZABIJ, ev.getEntityType(), ev.getEntityType()::equals));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void kopanie(BlockBreakEvent ev) {
		if (!ev.isCancelled())
			sprawdzZadania(ev.getPlayer(), Rodzaj.WYKOP, ev.getBlock().getType(), ev.getBlock().getType()::equals);
	}
	private static <E> void sprawdzZadania(Player p, Rodzaj rodzaj, E czego, Predicate<E> pred) {
		Gracz g = Gracz.wczytaj(p);
		for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
			sprawdzZadanie(p, g, rodzaj, czego, pred, aktywneZadanie, k -> 1);
	}
	@SuppressWarnings("unchecked")
	private static <E> void sprawdzZadanie(Player p, Gracz g, Rodzaj rodzaj, E czego, Predicate<E> pred, AktywneZadanie aktywneZadanie, Function<Kryterium, Integer> ile) {
		try {
			if (!aktywneZadanie.ukończone())
				Func.wykonajDlaNieNull(wczytaj(aktywneZadanie.zadanie), zadanie -> {
					Kryterium k;
					for (int i=0; i < zadanie.kryteria.size(); i++)
						if ((k = zadanie.kryteria.get(i)).rodzaj == rodzaj && k.ile > aktywneZadanie.getPostęp(i) && pred.test((E) k.getCzego())) {
							aktywneZadanie.zwiększ(p, i, ile.apply(k));
							g.zapisz();
							if (zadanie.autoObieranie && aktywneZadanie.spełnione())
								aktywneZadanie.odbierzNagrode(p);
						}
				});
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getInventory().getHolder() instanceof InvHolder)
			Func.wykonajDlaNieNull((InvHolder) ev.getInventory().getHolder(), holder -> {
				int slot = ev.getRawSlot();
				Player p = (Player) ev.getWhoClicked();
				if (slot < holder.zadania.size() && slot >= 0) {
					Krotka<Zadanie, Status> krotka = holder.zadania.get(slot);
					if (krotka.b == Status.DO_PRZYJĘCIA)
						krotka.a.przyjmij(p);
					else if (krotka.b == Status.DO_ODEBRANIA)
						for (AktywneZadanie aktywneZadanie : Gracz.wczytaj(p).zadania.aktywne)
							if (aktywneZadanie.zadanie.equals(krotka.a.id)) {
								aktywneZadanie.odbierzNagrode(p);
								break;
							}
					p.closeInventory();
				}
			});
	}
	
	
	
	// Override
	
	final EdytorOgólny<Zadanie> edytor = new EdytorOgólny<>("zadaniaadmin", Zadanie.class);
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> lista = Lists.newArrayList();
		switch (cmd.getName()) {
		case "zadania":
			switch (args.length) {
			case 0:
			case 1:
				Player p = null;
				if (sender instanceof Player)
					p = (Player) sender;
				Func.wykonajDlaNieNull(p, _p -> {
					Gracz g = Gracz.wczytaj(_p);
					g.zadania.ukończone.forEach(lista::add);
					g.zadania.aktywne.forEach(aktywneZadanie -> lista.add(aktywneZadanie.zadanie));
				});
				break;
			case 2:
				return null;
			}
			break;
		case "zadaniaadmin":
			if (args.length >= 1 && args[0].equals("edytor"))
				return utab(args, mapaZadań.keySet());
			if (args.length <= 1) {
				lista.add("edytor");
				lista.add("zapomnij");
				lista.add("ukończ");
				Bukkit.getOnlinePlayers().forEach(p -> lista.add(p.getName()));
				return utab(args, lista);
			}
			lista.addAll(mapaZadań.keySet());
			switch(args[0]) {
			case "ukończ":
			case "zapomnij":
				if (args.length == 2)
					return null;
			default:
				if (!lista.isEmpty()) {
					lista.add("-!przyjmij");
					lista.add("-!odbierz");
					lista.add("-!dostarcz");
				}
			}
		}
		return utab(args, lista);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("zadaniaadmin"))
			return komendaZadaniaadmin(sender, args);
		return komendaZadania(sender, args);
	}
	private boolean komendaZadania(CommandSender sender, String[] args) {
		// /zadania (zadanie) (nick)
		Player p = null;
		if (sender instanceof Player)
			p = (Player) sender;
		if (args.length >= 2 && sender.hasPermission(permInfo))
			p = Bukkit.getPlayer(args[1]);
		if (p == null)
			return Func.powiadom(sender, prefix + "/zadania (zadanie) (nick)");
		
		Gracz g = Gracz.wczytaj(p);
		
		
		if (args.length == 0 || (args.length >= 2 && sender.hasPermission(permInfo))) {
			Napis n = new Napis("\n\n\n\n\n§aUkończone Zadania: ");
			
			Iterator<String> it = g.zadania.ukończone.iterator();
			while (it.hasNext()) {
				String nazwa = it.next();
				Napis napis = new Napis("§2" + nazwa);
				Func.wykonajDlaNieNull(wczytaj(nazwa), zadanie -> napis.hover(String.join("\n", zadanie.opis)));
				n.dodaj(napis);
				if (it.hasNext())
					n.dodaj("§f, ");
			}
			
			n.dodaj("\n\n§dAktywne:");
			
			for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
				Func.wykonajDlaNieNull(wczytaj(aktywneZadanie.zadanie), zadanie -> {
					n.dodaj(new Napis(
							"\n§e- §b" + zadanie.nazwaWyświetlana + "\n",
							String.join("\n", zadanie.opis)
							));
					int i=0;
					for (Kryterium kryterium : zadanie.kryteria)
						n.dodaj("\n§6" + Func.enumToString(kryterium.rodzaj) + " ")
						 .dodaj(kryterium.getCzego() instanceof ItemStack ?
								 Napis.item((ItemStack) kryterium.getCzego()) : new Napis("§e" + Func.enumToString((Enum<?>) kryterium.getCzego())))
						 .dodaj(String.format(" §e%s§6/§e%s\n", aktywneZadanie.postępKryteriow.get(i++), kryterium.ile));
				});
	
			n.dodaj("\n");
			
			n.wyświetl(sender);
		} else {
			Zadanie zadanie = wczytaj(args[0]);
			if (zadanie == null)
				return Func.powiadom(prefix, sender, "Niepoprawne zadanie %s", args[0]);
			
			if (g.zadania.ukończone.contains(zadanie.id))
				sender.sendMessage(String.format("\n\n§dZadanie §b%s §a(ukończone)\n", zadanie.nazwaWyświetlana) + Func.koloruj(String.join("\n", zadanie.opis)));
			else {
				for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
					if (aktywneZadanie.zadanie.equals(zadanie.id)) {
						Napis n = new Napis(
								"\n§b" + zadanie.nazwaWyświetlana,
								String.join("\n", zadanie.opis)
								);
						int i=0;
						for (Kryterium kryterium : zadanie.kryteria)
							n.dodaj("§6" + Func.enumToString(kryterium.rodzaj) + " ")
							 .dodaj(kryterium.getCzego() instanceof ItemStack ?
									 Napis.item((ItemStack) kryterium.getCzego()) : new Napis("§e" + Func.enumToString((Enum<?>) kryterium.getCzego())))
							 .dodaj(String.format(" §e%s§6/§e%s\n", aktywneZadanie.postępKryteriow.get(i++), kryterium.ile));
						
						n.wyświetl(sender);
						return true;
					}
				return Func.powiadom(prefix, sender, "Niepoprawne zadanie %s", args[0]);
			}
		}
		return true;
	}
	private boolean komendaZadaniaadmin(CommandSender sender, String[] args) {
		if (args.length <= 0)
			return Func.powiadom(sender, prefix + "/zadaniaadmin [nick / edytor]");
		
		switch (args[0].toLowerCase()) {
		case "edytor":
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, prefix + "/zadaniaadmin edytor -t <nazwa zadania>");
			else if (args.length >= 2 && args[1].equals("-t"))
				args[2] = "configi/Zadania|" + args[2];
			return edytor.onCommand(sender, "zadaniaadmin", args);
		case "ukończ":
			Func.wykonajDlaNieNull(Func.gracz(sender, args[1]), p -> {
				Func.wykonajDlaNieNull(wczytaj(Func.listToString(args, 2)), zadanie -> {
					Gracz g = Gracz.wczytaj(p);
					sender.sendMessage(prefix + Func.msg("%s ukończył %s zadań", p.getDisplayName(), zadanie.debugUkończ(g, p)));
					g.zapisz();
				},
						() -> sender.sendMessage(prefix + "To zadanie nie istnieje"));
			});
			return true;
		case "zapomnij":
			Func.wykonajDlaNieNull(Func.gracz(sender, args[1]), p -> {
				Func.wykonajDlaNieNull(wczytaj(Func.listToString(args, 2)), zadanie -> {
					Gracz g = Gracz.wczytaj(p);
					sender.sendMessage(prefix + Func.msg("%s zapomniał %s zadań", p.getDisplayName(), zadanie.debugZapomnij(g)));
					g.zapisz();
				},
						() -> sender.sendMessage(prefix + "To zadanie nie istnieje"));
			});
			return true;
		}
		
		// /zadaniaadmin <nick> [(opcje per zadanie)... <nazwy zadań>...] - otwiera panel z dostępnymi zadaniami z listy <nazwy zadań>
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			for (Entity e : Bukkit.selectEntities(sender, args[0]))
				if (e instanceof Player) {
					p = (Player) e;
					break;
				}
			if (p == null)
				return Func.powiadom(sender, "Ten gracz nie jest aktualnie online!");
		}
		//	(zadanie, args1 ? przyjmować : odbierać)
		List<Krotka<Zadanie, Status>> zadania = Lists.newArrayList();
		MonoKrotka<Boolean> dostarczone = new MonoKrotka<>(false, null);
		
		boolean przyjmij = true;
		boolean odbierz = true;
		boolean dostarcz = true;
		
		Gracz g = Gracz.wczytaj(p);
		
		for (int i=1; i < args.length; i++)
			switch (args[i]) {
			case "-!p":
			case "-!przyjmij":
				przyjmij = false;
				break;
			case "-!o":
			case "-!odbierz":
				odbierz = false;
				break;
			case "-!d":
			case "-!dostarcz":
				dostarcz = false;
				break;
			default:
				Zadanie zadanie = wczytaj(args[i]);
				if (zadanie == null)
					sender.sendMessage(prefix + "Nieprawidłowe zadanie: " + args[i]);
				else
					if (!dodajItem(zadania, zadanie, przyjmij, odbierz, dostarcz, dostarczone, g, p))
						Main.error("error: " + Lists.newArrayList(args));
				przyjmij = true;
				odbierz = true;
				dostarcz = true;
				break;
			}
		
		if (dostarczone.a)
			return Func.powiadom(p, prefix + "Dostarczyłeś troche itemków");
		
		if (!zadania.isEmpty()) {
			p.openInventory(new InvHolder(zadania, g.zadania.ukończone).getInventory());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		return true;
	}
	private boolean dodajItem(List<Krotka<Zadanie, Status>> zadania, Zadanie zadanie, boolean przyjmij, boolean odbierz, boolean dostarcz, MonoKrotka<Boolean> dostarczone, Gracz g, Player p) {
		if (dostarczone.a)
			return true;
		
		if (g.zadania.ukończone.contains(zadanie.id))
			return zadania.add(new Krotka<>(zadanie, Status.UKOŃCZONE));

		for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
			if (aktywneZadanie.zadanie.equals(zadanie.id)) {
				if (dostarcz && (dostarczone.a = aktywneZadanie.dostarcz(p, g) || dostarczone.a))
					return true;
				
				if (odbierz && aktywneZadanie.doOdebrania())
					return zadania.add(new Krotka<>(zadanie, Status.DO_ODEBRANIA));
				return zadania.add(new Krotka<>(zadanie, Status.W_TRAKCIE));
			}

		
		if (przyjmij)
			return zadania.add(new Krotka<>(zadanie, zadanie.możePrzyjąć(p) ? Status.DO_PRZYJĘCIA : Status.NIE_DOSTĘPNE));
		
		if (!przyjmij && !odbierz && !dostarcz)
			return true;
		
		Main.error("Nieprzewidziana sytuacja w zadaniach error id:3", p.getName(), przyjmij, odbierz, dostarcz, zadania);
		return false;
	}
	
	
	static final HashMap<String, Zadanie> mapaZadań = new HashMap<>();
	
	@Override
	public void przeładuj() {
		Config config = new Config("configi/Zadania");	
		config.przeładuj();
		mapaZadań.clear();
		
		for (String klucz : config.klucze(false)) {
			Object obj = config.wczytaj(klucz);
			if (obj instanceof Zadanie)
				mapaZadań.put(klucz, (Zadanie) obj);
			else
				Main.warn("Niepoprawne zadanie w configi/Zadania.yml: " + klucz);
		}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane zadania", mapaZadań.size());
	}
}
