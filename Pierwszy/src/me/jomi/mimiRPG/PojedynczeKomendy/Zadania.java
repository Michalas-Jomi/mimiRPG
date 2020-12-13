package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

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
	}
	public static class AktywneZadanie extends Mapowany {
		@Mapowane String zadanie;
		@Mapowane List<Integer> postępKryteriów; // z góry zakładam poprawność tego
		private boolean ukończone = false;
		
		void zwiększ(Player p, int i, int ile) {
			postępKryteriów.set(i, postępKryteriów.get(i) + ile);
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
					p.sendMessage(prefix + "Ukończyłeś zadanie " + zadanie);
					return;
				}
		}
		
		boolean ukończone() {
			return ukończone ? ukończone : (ukończone = spełnione());
		}
		private boolean spełnione() {
			Zadanie zadanie = wczytaj(this.zadanie);
			for (int i=0; i < zadanie.kryteria.size(); i++)
				if (postępKryteriów.get(i) < zadanie.kryteria.get(i).ile)
					return false;
			return true;
		}
	}
	public static class ZadaniaGracza extends Mapowany {
		@Mapowane List<String> ukończone;
		@Mapowane List<AktywneZadanie> aktywne;
		
		void dostarcz(Player p) {
			Func.wykonajDlaNieNull(p.getInventory().getItemInMainHand(), item -> sprawdzZadania(p, Rodzaj.DOSTARCZ, item, item::isSimilar));
		}
	}
	
	
	static final Config config = new Config("configi/Zadania");
	
	static Zadanie wczytaj(String nazwa) {
		return (Zadanie) config.wczytaj(nazwa);
	}
	
	public Zadania() {
		super("zadania");
		ustawKomende("zadaniaadmin", null, null);
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
			if (!aktywneZadanie.ukończone())
				Func.wykonajDlaNieNull(wczytaj(aktywneZadanie.zadanie), zadanie -> {
					for (int i=0; i < zadanie.kryteria.size(); i++)
						if (zadanie.kryteria.get(i).rodzaj == rodzaj && pred.test(czego)) {
							aktywneZadanie.zwiększ(p, i, 1);
							g.zapisz();
						}
				});
	}

	
	
	// Override
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// /zadaniaadmin <nick> <nazwy zadań>... - otwiera panel z dostępnymi zadaniami z listy <nazwy zadań>
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane zadania", config.klucze(false));
	}
}
