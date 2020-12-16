package me.jomi.mimiRPG.PojedynczeKomendy;

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
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import net.md_5.bungee.api.chat.ClickEvent.Action;

@Moduł
public class Zadania extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Zadania");
	public static enum Rodzaj {
		ZABIJ,
		WYKOP,
		DOSTARCZ;
	}
	public static class Kryterium extends Mapowany {
		@Mapowane Rodzaj rodzaj = Rodzaj.DOSTARCZ;
		@Mapowane Object czego;
		@Mapowane int ile = 1;
		
		public void Init() {
			switch (rodzaj) {
			case DOSTARCZ:
				czego = Config.item(czego);
				break;
			case WYKOP:
				czego = Material.valueOf((String) czego);
				break;
			case ZABIJ:
				czego = EntityType.valueOf((String) czego);
				break;
			}
			
			ile = Math.max(1, Math.min(ile, 64));
		}
	}
	public static class Zadanie extends Mapowany {
		@Mapowane String nazwa;
		@Mapowane List<String> opis;
		@Mapowane List<String> wymagane;
		@Mapowane List<ItemStack> nagroda;
		@Mapowane List<String> cmdsStart; // placeholders - %nick% %displayname%
		@Mapowane List<String> cmdsKoniec; // ^
		@Mapowane List<Kryterium> kryteria;
		@Mapowane boolean autoObieranie = false;
		
		public void Init() {
			opis = Func.koloruj(opis);
		}
		
		
		boolean możePrzyjąć(Player p) {
			Gracz g = Gracz.wczytaj(p);
			
			if (g.zadania.ukończone.contains(nazwa))
				return false;
			
			for (AktywneZadanie zadanie : g.zadania.aktywne)
				if (zadanie.zadanie.equals(nazwa))
					return false;
			
			return g.zadania.ukończone.containsAll(wymagane);
		}
		
		void przyjmij(Player p) {
			cmdsStart.forEach(cmd -> 
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						cmd.replace("%nick%", p.getName()).replace("%displayname%", p.getDisplayName())));
			
			Gracz g = Gracz.wczytaj(p);
			
			AktywneZadanie zadanie = Func.utwórz(AktywneZadanie.class);
			zadanie.zadanie = nazwa;
			g.zadania.aktywne.add(zadanie);
			
			g.zapisz();
			
			p.sendMessage(prefix + Func.msg("Przyjołeś nowe zadanie: %s", nazwa));
		}
	}
	public static class AktywneZadanie extends Mapowany {
		@Mapowane String zadanie;
		@Mapowane List<Integer> postępKryteriow; // TODO naprawić - wysypie sie przy usunięciu kryterium
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
			Func.wykonajDlaNieNull(p.getInventory().getItemInMainHand(), item -> {
				sprawdzZadanie(p, g, Rodzaj.DOSTARCZ, item, item::isSimilar, this, k -> {
					krotka.a = true;
					
					int ile = Math.min(item.getAmount(), k.ile);
					item.setAmount(item.getAmount() - ile);
					p.getInventory().setItemInMainHand(item.getAmount() <= 0 ? null : item);
					return ile;
				});
			});
			return krotka.a;
		}
		
		boolean ukończone() {
			return ukończone ? ukończone : (ukończone = spełnione());
		}
		private boolean spełnione() {
			Zadanie zadanie = wczytaj(this.zadanie);
			for (int i=0; i < zadanie.kryteria.size(); i++)
				if (getPostęp(i) < zadanie.kryteria.get(i).ile)
					return false;
			return true;
		}
	}
	public static class ZadaniaGracza extends Mapowany {
		@Mapowane List<String> ukończone;
		@Mapowane List<AktywneZadanie> aktywne;
	}
	
	public static class InvHolder extends Func.abstractHolder {
		//	(zadanie, args1 ? przyjmować : odbierać)
		List<Krotka<Zadanie, Boolean>> zadania;
		public InvHolder(int rzędy, List<Krotka<Zadanie, Boolean>> zadania) {
			super(rzędy, "&4&lZadania");
			this.zadania = zadania;
			Func.ustawPuste(inv);
			Krotka<Integer, ?> i = new Krotka<>(0, null);
			zadania.forEach(krotka -> 
					inv.setItem(i.a++,
							Func.stwórzItem(krotka.b ? Material.YELLOW_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE,
									"&c" + krotka.a.nazwa,
									krotka.a.opis)
							));
		}
	}
	
	static final Config config = new Config("configi/Zadania");	
	
	static Zadanie wczytaj(String nazwa) {
		return (Zadanie) config.wczytaj(nazwa);
	}
	
	public Zadania() {
		super("zadania");
		ustawKomende("zadaniaadmin", null, null);
		
		edytor.zarejestrójWyjątek("/zadaniaadmin edytor kryteria <int> czego", (zadanie, ścieżka) -> {
			int index = Func.Int(Func.tnij(ścieżka, " ").get(3));
			Kryterium k = zadanie.kryteria.get(index);
			switch (k.rodzaj) {
			case DOSTARCZ:
				return new Napis("§6item§8: ")
						.dodaj((k.czego instanceof ItemStack ? Napis.item((ItemStack) k.czego) : new Napis("§enull"))
								.clickEvent(Action.RUN_COMMAND, ścieżka + "() org.bukkit.inventory.ItemStack >>"));
			case WYKOP:
				return new Napis("§6blok§8: ").dodaj(new Napis(
						"§e" + (k.czego instanceof Material ? k.czego : "null"),
						"§bKliknij aby ustawić",
						ścieżka + "() org.bukkit.Material >> "
						));
			case ZABIJ:
				return new Napis("§6mob§8: ").dodaj(new Napis(
						"§e" + (k.czego instanceof EntityType ? k.czego : "null"),
						"§bKliknij aby ustawić",
						ścieżka + "() org.bukkit.entity.EntityType >> "
						));
			}
			return null;
		});
		
		edytor.zarejestrójWyjątek("/zadaniaadmin edytor nazwa", (zadanie, ścieżka) -> null);
		edytor.zarejestrujOnZatwierdz((zadanie, ścieżka) -> zadanie.nazwa = ścieżka);
		edytor.zarejestrujOnZatwierdz((zadanie, ścieżka) -> przeładuj());
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
	private static <E> void sprawdzZadanie(Player p, Gracz g, Rodzaj rodzaj, E czego, Predicate<E> pred, AktywneZadanie aktywneZadanie, Function<Kryterium, Integer> ile) {
		if (!aktywneZadanie.ukończone())
			Func.wykonajDlaNieNull(wczytaj(aktywneZadanie.zadanie), zadanie -> {
				Kryterium k;
				for (int i=0; i < zadanie.kryteria.size(); i++)
					if ((k = zadanie.kryteria.get(i)).rodzaj == rodzaj && pred.test(czego)) {
						aktywneZadanie.zwiększ(p, i, ile.apply(k));
						g.zapisz();
						if (zadanie.autoObieranie && aktywneZadanie.spełnione())
							aktywneZadanie.odbierzNagrode(p);
					}
			});
	}

	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getInventory().getHolder() instanceof InvHolder)
			Func.wykonajDlaNieNull((InvHolder) ev.getInventory().getHolder(), holder -> {
				int slot = ev.getRawSlot();
				Player p = (Player) ev.getWhoClicked();
				if (slot < holder.zadania.size() && slot >= 0) {
					Krotka<Zadanie, Boolean> krotka = holder.zadania.get(slot);
					if (krotka.b)
						krotka.a.przyjmij(p);
					else {
						for (AktywneZadanie aktywneZadanie : Gracz.wczytaj(p).zadania.aktywne)
							if (aktywneZadanie.zadanie.equals(krotka.a.nazwa)) {
								aktywneZadanie.odbierzNagrode(p);
								break;
							}
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
				return null;
			if (args.length <= 1) {
				lista.add("edytor");
				Bukkit.getOnlinePlayers().forEach(p -> lista.add(p.getName()));
				return lista;
			}
			lista.addAll(config.klucze(false));
			if (!lista.isEmpty()) {
				lista.add("-!przyjmij");
				lista.add("-!odbierz");
				lista.add("-!dostarcz");
			}
		}
		return lista;
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
		if (args.length >= 2 && sender.hasPermission("mimirpg.zadaniaadmin"))
			p = Bukkit.getPlayer(args[1]);
		if (p == null)
			return Func.powiadom(sender, prefix + "/zadania (zadanie) (nick)");
		
		Gracz g = Gracz.wczytaj(p);
		
		
		if (args.length == 0 || (args.length >= 2 && sender.hasPermission("mimirpg.zadaniaadmin"))) {
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
							"\n§e- §b" + zadanie.nazwa,
							String.join("\n", zadanie.opis)
							));
					int i=0;
					for (Kryterium kryterium : zadanie.kryteria)
						n.dodaj("§6" + Func.enumToString(kryterium.rodzaj) + " ")
						 .dodaj(kryterium.czego instanceof ItemStack ?
								 Napis.item((ItemStack) kryterium.czego) : new Napis("§e" + Func.enumToString((Enum<?>) kryterium.czego)))
						 .dodaj(String.format(" §e%s§6/§e%s\n", aktywneZadanie.postępKryteriow.get(i++), kryterium.ile));
				});
	
			n.dodaj("\n");
			
			n.wyświetl(sender);
		} else {
			Zadanie zadanie = wczytaj(args[0]);
			if (zadanie == null)
				return Func.powiadom(prefix, sender, "Niepoprawne zadanie %s", args[0]);
			
			if (g.zadania.ukończone.contains(zadanie.nazwa))
				sender.sendMessage(String.format("\n\n§dZadanie §b%s §a(ukończone)\n", zadanie.nazwa) + Func.koloruj(String.join("\n", zadanie.opis)));
			else {
				for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
					if (aktywneZadanie.zadanie.equals(zadanie.nazwa)) {
						Napis n = new Napis(
								"\n§b" + zadanie.nazwa,
								String.join("\n", zadanie.opis)
								);
						int i=0;
						for (Kryterium kryterium : zadanie.kryteria)
							n.dodaj("§6" + Func.enumToString(kryterium.rodzaj) + " ")
							 .dodaj(kryterium.czego instanceof ItemStack ?
									 Napis.item((ItemStack) kryterium.czego) : new Napis("§e" + Func.enumToString((Enum<?>) kryterium.czego)))
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
		
		if (args[0].equals("edytor")) {
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, prefix + "/zadaniaadmin edytor -t <nazwa zadania>");
			else if (args.length >= 2 && args[1].equals("-t"))
				args[2] = "configi/Zadania|" + args[2];
			return edytor.onCommand(sender, "zadaniaadmin", args);
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
		List<Krotka<Zadanie, Boolean>> zadania = Lists.newArrayList();
		boolean dostarczone = false;
		
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
				else {
					if (dostarcz)
						for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
							if (aktywneZadanie.zadanie.equals(zadanie.nazwa))
								dostarczone = aktywneZadanie.dostarcz(p, g) || dostarczone;
					
					if (odbierz && !dostarczone)
						for (AktywneZadanie aktywneZadanie : g.zadania.aktywne)
							if (aktywneZadanie.zadanie.equals(zadanie.nazwa)) {
								if (aktywneZadanie.doOdebrania())
									zadania.add(new Krotka<>(zadanie, false));
								break;
							}
					
					if (przyjmij && !dostarczone && zadanie.możePrzyjąć(p))
						zadania.add(new Krotka<>(zadanie, true));
				
				}
				przyjmij = true;
				odbierz = true;
				dostarcz = true;
				break;
			}
		
		if (dostarczone)
			return Func.powiadom(p, prefix + "Dostarczyłeś troche itemków");
		
		if (!zadania.isEmpty()) {
			p.openInventory(new InvHolder((zadania.size() - 1) / 9 + 1, zadania).getInventory());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		return true;
	}

	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane zadania", config.klucze(false).size());
	}
}
