package me.jomi.mimiRPG.Minigry;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.util.Cooldown;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;

public abstract class MinigraDrużynowa extends Minigra {
	public abstract static class Arena extends Minigra.Arena {
		private static final ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l §e§o");
		static final ItemStack itemWybórDrużyny = Func.stwórzItem(Material.COMPASS, "§9Wybierz Drużynę");
		
		private Inventory inv;
		static final String nazwaInv = "§1§lWybierz drużynę";
		private final Cooldown cooldownWyboruDrużyny = new Cooldown(5);
		
		abstract List<? extends Drużyna> getDrużyny();
		
		@Override
		void start() {
			super.start();
			for (Player p : gracze)
				ubierz(p);
			cooldownWyboruDrużyny.wyczyść();
		}
		@Override
		Player opuść(int i, boolean info) {
			Player p = super.opuść(i, info);
			Drużyna drużyna = drużyna(p);
			if (drużyna != null)
				drużyna.gracze--;
			p.removeMetadata(inst.getMetaDrużynaId(), Main.plugin);
			return p;
		}
		
		@Override
		boolean sprawdzKoniec() {
			Drużyna grająca = null;
			for (Player p : gracze) {
				Drużyna drużyna = drużyna(p);
				if (grająca == null)
					grająca = drużyna;
				else if (!grająca.equals(drużyna))
					return false;
			}
			return wygrana(grająca);
		}
		
		boolean wygrana(Drużyna drużyna) {
			if (!grane) return true;
			for (Player p : gracze)
				if (drużyna.equals(drużyna(p)))
					staty(p).wygraneAreny++;
				else
					staty(p).przegraneAreny++;
			
			
			StringBuilder wygrani 	= new StringBuilder();
			StringBuilder przegrani = new StringBuilder();
			for (Player p : gracze) {
				Drużyna dp = drużyna(p);
				StringBuilder s = drużyna.equals(dp) ? wygrani : przegrani;
				s.append(' ').append(dp.napisy).append(p.getName());
			}
			
			Bukkit.broadcastMessage(prefix + Func.msg("Drużyna %s(%s) wygrała na arenie %s (z%s)",
					drużyna, wygrani.substring(1), nazwa, przegrani));
			koniec();
			return true;
		}
		
		void wybierzDrużyne(Player p, Drużyna drużyna) {
			if (!cooldownWyboruDrużyny.minął(p.getName())) {
				p.sendMessage(prefix + "Poczekaj chwile zanim zmienisz drużyne");
				return;
			}
			
			Drużyna stara = drużyna(p);
			if (stara != null) {
				if (stara.equals(drużyna)) 
					return;
				stara.gracze--;
			}
			
			cooldownWyboruDrużyny.ustaw(p.getName());
			
			Func.ustawMetadate(p, inst.getMetaDrużynaId(), drużyna);
			drużyna.gracze++;

			if (timer == -1 && policzGotowych() >= min_gracze)
				timer = czasStartu;
			if (timer != -1 && sprawdzKoniec())
				timer = -1;
			
			ubierz(p, drużyna);
			
			napiszGraczom("%s dołącza do drużyny %s", p.getDisplayName(), drużyna);
		}
		void ubierz(Player p) {
			ubierz(p, drużyna(p));
		}
		void ubierz(Player p, Drużyna drużyna) {
			Color kolor = drużyna.kolor;
			Function<Material, ItemStack> dajItem = mat -> 
					Func.pokolorujZbroje(Func.stwórzItem(mat, " "), kolor);
			PlayerInventory inv = p.getInventory();
			inv.setHelmet(dajItem.apply(Material.LEATHER_HELMET));
			inv.setChestplate(dajItem.apply(Material.LEATHER_CHESTPLATE));
			inv.setLeggings(dajItem.apply(Material.LEATHER_LEGGINGS));
			inv.setBoots(dajItem.apply(Material.LEATHER_BOOTS));
		}

		Inventory dajInv() {
			if (inv == null)
				stwórzInv();
			return inv;
		}
		void odświeżInv() {
			for (Drużyna drużyna : getDrużyny())
				odświeżInv(drużyna);
		}
		void odświeżInv(Drużyna drużyna) {	
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

		
		@Override
		int policzGotowych() {
			int w = 0;
			for (Drużyna drużyna : getDrużyny())
				w += drużyna.gracze;
			return w;
		}
		
		Drużyna znajdzDrużyne(String nazwa) {
			for (Drużyna drużyna : getDrużyny()) {
				if (drużyna.toString().equals(nazwa))
					return drużyna;
			}
			return null;
		}
	}
	public abstract static class Drużyna extends Mapowany {
		@Mapowane KolorRGB kolorRGB = new KolorRGB();
		@Mapowane String nazwa;
		
		int gracze;
		
		int slotInv = -1;
		
		Color kolor;
		String napisy;
		
		public void Init() {
			this.kolor 	= kolorRGB.kolor();
			this.napisy = kolorRGB.kolorChat();	
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Drużyna)
				return nazwa.equals(((Drużyna) obj).nazwa);
			return false;
		}

		public String toString() {
			return napisy + nazwa;
		}
	}
	
	abstract String getMetaDrużynaId();
	
	static Drużyna drużyna(Player p) {
		return metadata(p, inst.getMetaDrużynaId());
	}

	static MinigraDrużynowa inst;
	public MinigraDrużynowa() {
		inst = this;
	}
	
	@EventHandler
	public void KlikanieInv(InventoryClickEvent ev) {
		Arena arena = arena(ev.getWhoClicked());
		int slot = ev.getRawSlot();
		if (arena != null && Arena.nazwaInv.equals(ev.getView().getTitle()) && slot >= 0 && slot < ev.getInventory().getSize()) {
			ev.setCancelled(true);
			ItemStack item = ev.getCurrentItem();
			if (item.getType().equals(Material.LEATHER_CHESTPLATE)) {
				Drużyna drużyna = arena.znajdzDrużyne(item.getItemMeta().getDisplayName());
				arena.wybierzDrużyne((Player) ev.getWhoClicked(), drużyna);
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
	
	static Arena arena(Entity p) {
		return metadata(p, inst.getMetaId());
	}
}












