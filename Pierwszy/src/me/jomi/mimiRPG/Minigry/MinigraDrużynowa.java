package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.NiepoprawneDemapowanieException;
import me.jomi.mimiRPG.util.Cooldown;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.ItemCreator;
import me.jomi.mimiRPG.util.KolorRGB;

public abstract class MinigraDrużynowa extends Minigra {
	public abstract static class Arena extends Minigra.Arena {
		private static final ItemStack itemWybórDrużyny = Func.stwórzItem(Material.COMPASS, "§9Wybierz Drużynę");
		
		private final Cooldown cooldownWyboruDrużyny = new Cooldown(5);

		private final HashMap<String, StringBuffer> mapaDrużynDlaMsgWin = new HashMap<>();
		
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();;
		
		
		// abstract
		abstract List<? extends Drużyna> getDrużyny();
		abstract int getMinDrużyny();
		
		abstract MinigraDrużynowa getInstMinigraDrużynowa();
		@Override Minigra getInstMinigra() { return getInstMinigraDrużynowa(); }

		@Override
		protected void Init() {
			try {
				for (Drużyna drużyna : getDrużyny()) {
					Team team = sb.registerNewTeam(drużyna.nazwa);
					team.setOption(Option.COLLISION_RULE, OptionStatus.FOR_OWN_TEAM);
					team.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
					team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
					team.setColor(drużyna.kolorRGB.zbliżony());
					team.setDisplayName(drużyna.toString());
					team.setCanSeeFriendlyInvisibles(true);
					team.setPrefix(drużyna.napisy);
					drużyna.team = team;
				}
			} catch (NullPointerException e) {
				throw new NiepoprawneDemapowanieException("Nie wszystkie wymagane parametry zostały ustawione");
			}
		}
		
		// obsłóga start / koniec
		@Override
		void start() {
			super.start();
			
			mapaDrużynDlaMsgWin.clear();
			for (Player p : gracze) {
				Drużyna drużyna = getInstMinigraDrużynowa().drużyna(p);
				if (drużyna == null)
					drużyna = autoDrużyna(p);
				drużyna.team.addEntry(p.getName());
				p.setScoreboard(sb);
				StringBuffer msg = mapaDrużynDlaMsgWin.getOrDefault(drużyna.nazwa, new StringBuffer());
				msg.append(" ").append(drużyna.napisy).append(p.getName());
				mapaDrużynDlaMsgWin.put(drużyna.nazwa, msg);
				ubierz(p);
			}
			
			cooldownWyboruDrużyny.wyczyść();
		}
		@Override
		boolean dołącz(Player p) {
			if (!super.dołącz(p))
				return false;
			
			p.getInventory().setItem(4, itemWybórDrużyny);
			
			return true;
		}
		@Override
		void opuść(Player p, int i, boolean info) {
			Drużyna drużyna = getInstMinigraDrużynowa().drużyna(p);
			if (drużyna != null)
				drużyna.gracze--;
			p.removeMetadata(getInstMinigraDrużynowa().getMetaDrużynaId(), Main.plugin);
			super.opuść(p, i, info);
		}
		<D extends Drużyna> boolean wygrana(D drużyna) {
			if (!grane) return true;
			for (Player p : gracze)
				if (drużyna.equals(getInstMinigraDrużynowa().drużyna(p)))
					getInstMinigraDrużynowa().staty(p).wygraneAreny++;
				else
					getInstMinigraDrużynowa().staty(p).przegraneAreny++;
			
			
			StringBuilder przegrani = new StringBuilder();
			for (Entry<String, StringBuffer> en : mapaDrużynDlaMsgWin.entrySet())
				if (!en.getKey().equals(drużyna.nazwa))
					przegrani.append(en.getValue());
			
			Bukkit.broadcastMessage(getInstMinigra().getPrefix() + Func.msg("Drużyna %s(%s) wygrała na arenie %s (z%s)",
					drużyna, mapaDrużynDlaMsgWin.get(drużyna.nazwa).substring(1), nazwa, przegrani));
			mapaDrużynDlaMsgWin.clear();
			koniec();
			return true;
		}
		
				
		// wybór Drużyny
		private static final ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l §e§o");
		private static final String nazwaInv = "§1§lWybierz drużynę";
		private Inventory inv;

		Inventory dajInv() {
			if (inv == null)
				stwórzInv();
			return inv;
		}
		void odświeżInv() {
			for (Drużyna drużyna : getDrużyny())
				odświeżInv(drużyna);
		}
		<D extends Drużyna> void odświeżInv(D drużyna) {	
			dajInv().getItem(drużyna.slotInv).setAmount(drużyna.gracze);
		}
		private void stwórzInv() {
			Set<Integer> sloty = dajSloty();
			inv = Bukkit.createInventory(null, Math.min((Func.max(sloty) / 9 + 2)*9, 6*9), nazwaInv);
			
			for (int i=0; i<inv.getSize(); i++)
				inv.setItem(i, pustySlot);
			
			int i=0;
			for (int slot : sloty) {
				Drużyna drużyna = getDrużyny().get(i++);
				ItemStack item = Func.stwórzItem(Material.LEATHER_CHESTPLATE, drużyna.toString(), "§bKliknij aby Dołączyć");
				inv.setItem(slot, Func.pokolorujZbroje(item, drużyna.kolor));
				drużyna.slotInv = slot;
			}
		}
		private Set<Integer> dajSloty() {
			Set<Integer> sloty = Sets.newConcurrentHashSet();
			int pozostałe = getDrużyny().size();
			int poziom = 1;
			if (pozostałe <= 3*3+4)
				while (pozostałe > 0) {
					int x = poziom++ * 9;
					if (pozostałe > 4) {
						Func.dodajWszystkie(sloty, 2+x, 4+x, 6+x);
						pozostałe -= 3;
						continue;
					}
					switch (pozostałe) {
					case 1: Func.dodajWszystkie(sloty, 4+x); break;
					case 2: Func.dodajWszystkie(sloty, 3+x, 5+x); break;
					case 3: Func.dodajWszystkie(sloty, 2+x, 4+x, 6+x); break;
					case 4: Func.dodajWszystkie(sloty, 1+x, 3+x, 5+x, 7+x); break;
					}
					break;
				}
			else if (pozostałe <= 5*7)
				for (int i=10; i<5*9; i++) {
					if ((i+1) % 9 == 0) continue;
					sloty.add(i);
					if (--pozostałe <= 0)
						break;
				}
			else
				while (pozostałe-->0)
					sloty.add(pozostałe);
			
			return sloty;
		}
		
		
		final void ubierz(Player p) {
			ubierz(p, getInstMinigraDrużynowa().drużyna(p));
		}
		<D extends Drużyna> void wybierzDrużyne(Player p, D drużyna) {
			if (drużyna.maxGracze >= 0 && drużyna.gracze >= drużyna.maxGracze) {
				p.sendMessage(getInstMinigra().getPrefix() + "Drużyna " + drużyna + " jest już pełna!");
				return;
			}
			if (!cooldownWyboruDrużyny.minął(p.getName())) {
				p.sendMessage(getInstMinigra().getPrefix() + "Poczekaj chwile zanim zmienisz drużyne");
				return;
			}
			
			Drużyna stara = getInstMinigraDrużynowa().drużyna(p);
			if (stara != null) {
				if (stara.equals(drużyna)) 
					return;
				stara.gracze--;
			}
			
			cooldownWyboruDrużyny.ustaw(p.getName());
			
			Func.ustawMetadate(p, getInstMinigraDrużynowa().getMetaDrużynaId(), drużyna);
			drużyna.gracze++;

			super.sprawdzStart();
			if (timer != -1 && sprawdzKoniec())
				timer = -1;
			
			ubierz(p, drużyna);
			
			napiszGraczom("%s dołącza do drużyny %s", p.getDisplayName(), drużyna);
		}
		<D extends Drużyna> void ubierz(Player p, D drużyna) {
			ubierz(p, drużyna, true, true, true, true);
		}
		<D extends Drużyna> void ubierz(Player p, D drużyna, boolean hełm, boolean spodnie, boolean klata, boolean buty) {
			Color kolor = drużyna.kolor;
			Function<Material, ItemStack> dajItem = mat -> 
				Func.pokolorujZbroje(ItemCreator.nowy(mat).nazwa(" ").unbreakable().stwórz(), kolor);
			PlayerInventory inv = p.getInventory();
			if (hełm)	inv.setHelmet(		dajItem.apply(Material.LEATHER_HELMET));
			if (klata)	inv.setChestplate(	dajItem.apply(Material.LEATHER_CHESTPLATE));
			if (spodnie)inv.setLeggings(	dajItem.apply(Material.LEATHER_LEGGINGS));
			if (buty)	inv.setBoots(		dajItem.apply(Material.LEATHER_BOOTS));
		}
		
		
		// util
		
		<D extends Drużyna> void powiadomDrużyne(D drużyna, String msg, Object... uzupełnienia) {
			for (Player p : gracze)
				if (getInstMinigraDrużynowa().drużyna(p).equals(drużyna))
					p.sendMessage(getInstMinigraDrużynowa().getPrefix() + Func.msg(msg, uzupełnienia));
		}
		
		
		// Override
		
		@Override
		boolean poprawna() {
			if (super.poprawna() && getDrużyny().size() >= getMinDrużyny()) {
				Set<String> nazwy = Sets.newConcurrentHashSet();
				for (Drużyna drużyna : getDrużyny())
					if (!nazwy.add(drużyna.nazwa))
						return false;
				return true;
			}
			return false;
		}

		@Override
		boolean sprawdzKoniec() {
			Drużyna grająca = null;
			for (Player p : gracze) {
				Drużyna drużyna = getInstMinigraDrużynowa().drużyna(p);
				if (grająca == null)
					grająca = drużyna;
				else if (!grająca.equals(drużyna))
					return false;
			}
			return wygrana(grająca);
		}

		@Override
		int policzGotowych() {
			int w = 0;
			int d = 0;
			for (Drużyna drużyna : getDrużyny()) {
				w += drużyna.gracze;
				if (drużyna.gracze > 0)
					d++;
			}
			if (getMinDrużyny() >= d)
				return w;
			return 0;
		}
		
		private Drużyna autoDrużyna(Player p) {
			Drużyna akt = null;
			int min = gracze.size() + 1;
			
			for (Drużyna drużyna : getDrużyny())
				if (min > drużyna.gracze) {
					min = drużyna.gracze;
					akt = drużyna;
				}
			
			wybierzDrużyne(p, akt);
			
			return akt;
		}
		
		@Override void sprawdzStart() {};
		
		@SuppressWarnings("unchecked")
		<D extends Drużyna> D znajdzDrużyne(String nazwa) {
			for (Drużyna drużyna : getDrużyny())
				if (drużyna.toString().equals(nazwa))
					return (D) drużyna;
			return null;
		}
	}
	public abstract static class Drużyna extends Mapowany {
		@Mapowane KolorRGB kolorRGB = new KolorRGB();
		@Mapowane int maxGracze = -1;
		@Mapowane String nazwa;

		Team team;
		
		int gracze;
		
		int slotInv = -1;
		
		Color kolor;
		String napisy;
		
		// Init
		@Override
		public void Init() {
			this.kolor 	= kolorRGB.kolor();
			this.napisy = kolorRGB.kolorChat();	
		}
	
		
		
		// util
		
		public void particle(Location loc, int ile, double dx, double dy, double dz) {
			loc.getWorld().spawnParticle(Particle.REDSTONE, loc, ile, dx, dy, dz, 0, new Particle.DustOptions(kolor, 1));
		}
		
		
		
		// Override
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Drużyna)
				return nazwa.equals(((Drużyna) obj).nazwa);
			return false;
		}

		@Override
		public String toString() {
			return napisy + nazwa;
		}
	}

	// abstract
	abstract String getMetaDrużynaId();
	
	
	// util
	String nick(Player p) {
		Drużyna d = drużyna(p);
		if (d != null)
			return d.napisy + p.getName();
		return p.getName();
	}

	<D extends Drużyna> D drużyna(Entity p) {
		return metadata(p, getMetaDrużynaId());
	}

	
	// EventHandler
	
	@EventHandler
	public void KlikanieInv(InventoryClickEvent ev) {
		Arena arena = arena(ev.getWhoClicked());
		int slot = ev.getRawSlot();
		if (arena != null && !arena.grane) {
			ev.setCancelled(true);
			if (Arena.nazwaInv.equals(ev.getView().getTitle()) && slot >= 0 && slot < ev.getInventory().getSize()) {
				ItemStack item = ev.getCurrentItem();
				if (item.getType().equals(Material.LEATHER_CHESTPLATE)) {
					Drużyna drużyna = arena.znajdzDrużyne(item.getItemMeta().getDisplayName());
					arena.wybierzDrużyne((Player) ev.getWhoClicked(), drużyna);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void użycie(PlayerInteractEvent ev) {
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		Arena arena = arena(ev.getPlayer());
		if (arena != null && !arena.grane && Arena.itemWybórDrużyny.equals(ev.getItem())) {
			ev.getPlayer().openInventory(arena.dajInv());
			ev.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void wyrzucanie(PlayerDropItemEvent ev) {
		Arena arena = arena(ev.getPlayer());
		if (arena != null && !arena.grane)
			ev.setCancelled(true);
	}
}
