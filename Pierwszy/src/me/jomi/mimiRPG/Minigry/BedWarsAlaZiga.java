package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class BedWarsAlaZiga extends MinigraDrużynowa {
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	public static final String prefix = Func.prefix("Bedwars ala Ziga");
	public static class PustyHolder implements InventoryHolder {
		private Inventory inv;
		public PustyHolder(int rzędy, String nazwa) {
			inv = Bukkit.createInventory(this, rzędy*9, Func.koloruj(nazwa));
		}
		@Override
		public Inventory getInventory() {
			return inv;
		}
	}
	public static class Arena extends MinigraDrużynowa.Arena {
		@Mapowane List<Drużyna> drużyny;
		@Mapowane List<Ruda> rudy;
		
		@Mapowane int życiaSerc = 3;
		@Mapowane int czasOchronySercaPoZniszczeniu = 15;
		@Mapowane double mnożnikCzekaniaRespawnu = 1; // 1 -> ilość minut gry = ilość sekund czekania
		
		@Mapowane List<SklepItemStrona> itemyDoKupienia;
		@Mapowane List<Ulepszenie> ulepszeniaDoKupienia;
		
		@Mapowane String schemat;
		@Mapowane Location locSchemat;
		@Mapowane Location rógAreny1;
		@Mapowane Location rógAreny2;
		
		
		final HashMap<String, List<Serce>> oznaczeni = new HashMap<>();
		
		
		int czasStartuAreny;
		
		static final Inventory guiGłówne;
		static {
			guiGłówne = new PustyHolder(3, "&4&lUlepszenia").getInventory();
			Func.ustawPuste(guiGłówne);
			guiGłówne.setItem(12, Func.stwórzItem(Material.NETHERITE_SWORD, "&4Itemki",		"&bPrzedmioty jednorazowe"));
			guiGłówne.setItem(14, Func.stwórzItem(Material.ENCHANTED_BOOK,  "&aUlepszenia", "&bTrwałe wzmocnienia"));
		}
		
		
		// obsługa start/koniec
		
		@Override
		void start() {
			super.start();
			for (Drużyna drużyna : drużyny) {
				drużyna.przygotujStart(this);
				if (!drużyna.serceLoc.getWorld().getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN))
					drużyna.serceLoc.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
			}
			for (Player p : gracze) {
				p.teleport(inst.drużyna(p).respawnLoc);
				p.setGameMode(GameMode.SURVIVAL);
			}
			czasStartuAreny = (int) (System.currentTimeMillis() / 1000);
		}
		@Override
		void koniec() {
			super.koniec();
			oznaczeni.clear();
			for (Drużyna drużyna : drużyny)
				drużyna.koniec();
		}
		@Override
		void opuść(Player p, int i, boolean info) {
			upuśćSerce(p);
			super.opuść(p, i, info);
		}
		
		void upuśćSerce(Player p) {
			Func.wykonajDlaNieNull(oznaczeni.remove(p.getName()), lista -> lista.forEach(serce -> {
				serce.powrót();
				napiszGraczom("%s upuścił serce drużyny %s", p.getName(), serce.drużyna);
			}));
		}
		
		void regeneruj() {
			regeneruj(Func.blokiIterator(rógAreny1, rógAreny2));
		}
		void regeneruj(Iterator<Block> it) {
			int licz = 0;
			int mx = Main.ust.wczytajLubDomyślna("Minigry.Budowanie Aren.Max Bloki", 50_000);
			while (it.hasNext()) {
				Block blok = it.next();
				if (blok.getType().equals(Material.AIR))
					continue;
				blok.setType(Material.AIR, false);
				if (++licz >= mx) {
					Func.opóznij(Main.ust.wczytajLubDomyślna("Minigry.Budowanie Aren.Ticki Przerw", 1), () -> regeneruj(it));
					return;
				}
			}
			Func.wklejSchemat(schemat, locSchemat);
		}
		
		// util
		
		void sekunda() {
			for (Entry<String, List<Serce>> en : oznaczeni.entrySet()) {
				Player p = Bukkit.getPlayer(en.getKey());
				en.getValue().forEach(serce -> particleNiesionychSerc(serce, p, 4));
				Drużyna drużyna = inst.drużyna(p);
				if (drużyna.serce.serce != null && !drużyna.serce.serce.isDead() && p.getLocation().distance(drużyna.serceLoc) <= 5)
					for (Serce serce : en.getValue()) {
						napiszGraczom("%s wyeliminował drużynę %s donosząc ich serce do swojego!", drużyna.napisy + p.getName(), serce.drużyna);
						for (int i = gracze.size() - 1; i >= 0; i--)
							if (inst.drużyna(gracze.get(i)).equals(serce.drużyna))
								opuść(gracze.get(i), i, true);
					}
			}
		}
		private void particleNiesionychSerc(Serce serce, Player p, int n) {
			if (n <= 0) return;
			serce.drużyna.particle(p.getLocation().add(.5, .5, .5), 100, .1, 2, .1);
			Func.opóznij(5, () -> particleNiesionychSerc(serce, p, n - 1));
		}

		void otwórzSklep(Player p, InventoryHolder strona) {
			p.openInventory(strona.getInventory());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		void otwórzSklep(Player p, Inventory inv) {
			p.openInventory(inv);
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		
		
		//  Override
		
		BedWarsAlaZiga inst;
		@Override MinigraDrużynowa getInstMinigraDrużynowa() { return inst; }
		@Override <M extends Minigra> void setInst(M inst)	 { this.inst = (BedWarsAlaZiga) inst; }

		@Override List<? extends Drużyna> getDrużyny() { return drużyny; }
		@Override int getMinDrużyny() 				   { return 2; }

		@Override Supplier<? extends me.jomi.mimiRPG.Minigry.Minigra.Statystyki> noweStaty() { return Statystyki::new; }
	}
	public static class Drużyna extends MinigraDrużynowa.Drużyna implements InventoryHolder  {
		public static enum Slot {
			miecz,
			łuk,
			armor,
			narzędzie;
			
			boolean pasuje(Material mat) {
				String nazwa = mat.toString();
				switch (this) {
				case łuk:		return mat.equals(Material.BOW);
				case miecz:		return nazwa.contains("_SWORD") || nazwa.contains("_AXE");
				case narzędzie:	return nazwa.contains("_AXE") || nazwa.contains("_PICKAXE") || nazwa.contains("_SHOVEL");
				case armor:		return nazwa.contains("_CHESTPLATE") || nazwa.contains("_HELMET") || nazwa.contains("_BOOTS") || nazwa.contains("_LEGGINGS");
				}
				return false;
			}
		}
		public enum Enchant {
			Wydajność(Enchantment.DIG_SPEED, Slot.narzędzie),
			
			Ostrość(Enchantment.DAMAGE_ALL, Slot.miecz),
			Odrzut(Enchantment.KNOCKBACK, Slot.miecz),
			Zaklęty(Enchantment.FIRE_ASPECT, Slot.miecz),
			
			Ochrona(Enchantment.PROTECTION_ENVIRONMENTAL, Slot.armor),
			Ciernie(Enchantment.THORNS, Slot.armor),
			
			Nieskończoność(Enchantment.ARROW_INFINITE, Slot.łuk),
			Moc(Enchantment.ARROW_DAMAGE, Slot.łuk),
			Płomień(Enchantment.ARROW_FIRE, Slot.łuk),
			Uderzenie(Enchantment.ARROW_KNOCKBACK, Slot.łuk);

			Slot slot;
			Enchantment ench;
			Enchant(Enchantment ench, Slot slot) {
				this.ench = ench;
				this.slot = slot;
			}
		}
		@Mapowane Location serceLoc;
		@Mapowane Location respawnLoc;
		@Mapowane Location locMobaSklepu;
		Serce serce;

		Inventory ec;
		
		Inventory guiUlepszeń;
		
		Entity mobSklepu;
		
		void przygotujStart(Arena arena) {
			serce = new Serce(arena, this);
			
			ec = Bukkit.createInventory(null, 9, Func.koloruj(napisy + "Enderchest"));
			
			przygotujGuiUlepszenia();
			zrespMobaSklepu();
		}
		private void zrespMobaSklepu() {
			mobSklepu = locMobaSklepu.getWorld().spawnEntity(locMobaSklepu, EntityType.HUSK);
			mobSklepu.setInvulnerable(true);
			mobSklepu.setGravity(false);
			mobSklepu.setSilent(true);
			if (mobSklepu instanceof LivingEntity) {
				((LivingEntity) mobSklepu).setCollidable(false);
				((LivingEntity) mobSklepu).setAI(false);
				((LivingEntity) mobSklepu).setCanPickupItems(false);
			}
			Func.ustawMetadate(mobSklepu, metaMobSklepu, serce.arena);
		}
		private void przygotujGuiUlepszenia() {
			// Ulepszenia Enchanty
			guiUlepszeń = Bukkit.createInventory(this, 5*9, Func.koloruj("&lUlepszenia Drużynowe"));
			Func.ustawPuste(guiUlepszeń);
			
			for (Ulepszenie upgr : Lists.reverse(serce.arena.ulepszeniaDoKupienia))
				ustawItemUlepszenia(upgr, 1);
		}
		void ustawItemUlepszenia(Ulepszenie upgr, int lvl) {
			ItemStack item = Func.nazwij(upgr.ikona.clone(), "&c" + upgr.enchant);
			if (lvl >= upgr.ceny.size()) {
				item.setType(Material.BARRIER);
				item.setAmount(1);
			} else {
				for (ItemStack cena : upgr.ceny.get(lvl - 1).cena)
					Func.dodajLore(item, "&a" + cena.getType().toString().toLowerCase() + " &9x &b" + cena.getAmount());
				item.setAmount(lvl);
			}
			
			guiUlepszeń.setItem(upgr.slot, item);
		}
		void koniec() {
			Func.wykonajDlaNieNull(serce, serce -> Func.wykonajDlaNieNull(serce.serce, Entity::remove));
			Func.wykonajDlaNieNull(serce, serce -> serce.uderzone = null);
			Func.wykonajDlaNieNull(mobSklepu, Entity::remove);
			guiUlepszeń = null;
			mobSklepu = null;
			serce = null;
			ec = null;
		}
		
		void ulepszEc() {
			for (HumanEntity p : ec.getViewers())
				p.closeInventory();
			
			Inventory nowy = Bukkit.createInventory(null, ec.getSize() + 9, Func.koloruj(napisy + "Enderchest"));
			for (int i=0; i<ec.getSize(); i++)
				nowy.setItem(i, ec.getItem(i));
			
			ec = nowy;
		}

		void kliknięte(Player p, int slot) {
			ItemStack item = guiUlepszeń.getItem(slot);
			if (Baza.pustySlot.isSimilar(item))
				return;
			if (item.getType().equals(Material.BARRIER)) {
				p.sendMessage(prefix + "Wykupiono już maksymalny poziom ulepszenia");
				return;
			}
			PlayerInventory inv = p.getInventory();
			Enchant ench = Enchant.valueOf(item.getItemMeta().getDisplayName().substring(2));
			for (Ulepszenie upgr : serce.arena.ulepszeniaDoKupienia)
				if (ench.equals(upgr.enchant)) {
					List<ItemStack> cena = upgr.ceny.get(item.getAmount() - 1).cena;
					
					if (!Func.posiada(inv, cena)) {
						p.sendMessage(prefix + "Nie stać cię na to");
						break;
					}
					
					Func.zabierz(inv, cena);
					
					serce.arena.powiadomDrużyne(this, "%s zakupił %s %s", napisy + p.getName(), ench.toString(), item.getAmount());
					
					zenchantój(ench, item.getAmount());
					
					ustawItemUlepszenia(upgr, item.getAmount() + 1);
					
					break;
				}
		}

		
		void zenchantój(ItemStack item) {
			for (Ulepszenie upgr : serce.arena.ulepszeniaDoKupienia)
				if (upgr.enchant.slot.pasuje(item.getType())) {
					int lvl = guiUlepszeń.getItem(upgr.slot).getAmount() - 1;
					if (lvl > 0)
						item.addUnsafeEnchantment(upgr.enchant.ench, lvl);
				}
		}
		private void zenchantój(Enchant ench, int lvl) {
			for (Player p : serce.arena.gracze)
				if (serce.arena.inst.drużyna(p).equals(this)) {
					zenchantój(p.getInventory(), ench, lvl);
					zenchantój(p.getItemOnCursor(), ench, lvl);
				}
			zenchantój(ec, ench, lvl);
		}
		private void zenchantój(Inventory inv, Enchant ench, int lvl) {
			for (ItemStack item : inv) 
				zenchantój(item, ench, lvl);
		}
		private void zenchantój(ItemStack item, Enchant ench, int lvl) { 
			if (item != null && ench.slot.pasuje(item.getType()))
				item.addUnsafeEnchantment(ench.ench, lvl);
		}
		
		@Override
		public Inventory getInventory() {
			return guiUlepszeń;
		}
	}
	public static class Ruda extends Mapowany {
		@Mapowane Material blok = Material.IRON_ORE;
		@Mapowane int sekundyOdrespiania = 10;
		@Mapowane ItemStack drop;
	}
	public static class SklepItemStrona extends Mapowany implements InventoryHolder {
		@Mapowane String nazwa = "Itemki";
		@Mapowane List<SklepItem> itemy;
		@Mapowane int rzędy = 6;
		
		private Inventory inv;
		
		private void stwórzInv() {
			inv = Bukkit.createInventory(this, rzędy*9, Func.koloruj("&4&l" + nazwa));
			Func.ustawPuste(inv);
			for (SklepItem sitem : Lists.reverse(itemy)) {
				ItemStack item = sitem.item.clone();
				for (ItemStack cena : sitem.cena)
					Func.dodajLore(item, "&a" + cena.getType().toString().toLowerCase() + " &9x &b" + cena.getAmount());
				inv.setItem(sitem.slot, item);
			}
		}
		SklepItem znajdzItem(int slot) {
			for (SklepItem sitem : itemy)
				if (sitem.slot == slot)
					return sitem;
			return null;
		}
		
		@Override
		public Inventory getInventory() {
			if (inv == null)
				stwórzInv();
			return inv;
		}
	}
	public static class SklepItem extends Mapowany {
		@Mapowane int slot;
		@Mapowane ItemStack item;
		
		@Mapowane int zmieńStrone = -1;
		@Mapowane List<ItemStack> cena;
		
		void kliknięty(Player p, Arena arena) {
			if (zmieńStrone != -1) {
				arena.otwórzSklep(p, arena.itemyDoKupienia.get(zmieńStrone));
				return;
			}
			
			PlayerInventory inv = p.getInventory();
			if (!Func.posiada(inv, cena)) {
				p.sendMessage(prefix + "Nie stać cię na to");
				return;
			}

			Func.zabierz(inv, cena);
			
			ItemStack item = this.item.clone();
			arena.inst.drużyna(p).zenchantój(item);
			inv.addItem(item);
			
			new Napis(prefix + "Kupiłeś ").dodaj(Napis.item(item)).wyświetl(p);
		}
	}
	public static class Ulepszenie extends Mapowany {
		public static class UlepszenieCena extends Mapowany {
			@Mapowane List<ItemStack> cena;
		}
		@Mapowane int slot;
		@Mapowane ItemStack ikona;
		@Mapowane Drużyna.Enchant enchant;
		@Mapowane List<UlepszenieCena> ceny;
	}	
	public static class Statystyki extends Minigra.Statystyki {

		@Override
		void sprawdzTopke(Player p) {
			// TODO Auto-generated method stub
			
		}
	}
	public static class Serce {
		Arena arena;
		Drużyna drużyna;
		EnderCrystal serce;
		
		int życia;
		
		Long uderzone = null;
		
		public Serce(Arena arena, Drużyna drużyna) {
			this.arena = arena;
			this.drużyna = drużyna;
			życia = arena.życiaSerc;
			zresp();
		}
		
		void zresp() {
			serce = (EnderCrystal) drużyna.serceLoc.getWorld().spawnEntity(drużyna.serceLoc, EntityType.ENDER_CRYSTAL);
			Func.ustawMetadate(serce, metaSerce, this);
		}
		
		void oznacz(Player p) {
			List<Serce> lista = Func.nieNull(arena.oznaczeni.get(p.getName()));
			lista.add(this);
			arena.oznaczeni.put(p.getName(), lista);
		}
		void powrót() {
			zresp();
		}
	
		void ustawCooldown() {
			if (uderzone == null) return;
			long cały = arena.czasOchronySercaPoZniszczeniu * 1000;
			long teraz = uderzone - System.currentTimeMillis();
			
			int procent = (int) ((teraz / cały) * 50);
			
			StringBuilder nazwa = new StringBuilder("§a");
			int i = 0;
			while (i++ < procent) nazwa.append('|');
			nazwa.append("§c");
			while (i++ < 50) nazwa.append('|');
			
			Func.opóznij(2, this::ustawCooldown);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Serce && obj != null && this.drużyna.equals(((Serce) obj).drużyna);
		}
	}
	
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void niszczenie(BlockBreakEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getPlayer()), arena -> {
			for (Ruda ruda : arena.rudy)
				if (ruda.blok.equals(ev.getBlock().getType())) {
					Material mat = ev.getBlock().getType();
					ev.setCancelled(true);
					Func.opóznij(1, () -> {
						ev.getBlock().setType(Material.AIR);
						ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation().add(.5, .5, .5), ruda.drop);
						arena.opóznijTask(ruda.sekundyOdrespiania * 20, () -> ev.getBlock().setType(mat));
					});
					break;
				}
		});
	}
	
	@EventHandler
	public void niszczenieSerca(EntityDamageByEntityEvent ev) {
		if (!(ev.getEntity() instanceof EnderCrystal)) return;
		Func.wykonajDlaNieNull(serce(ev.getEntity()), serce -> {
			ev.setCancelled(true);
			if (!(ev.getDamager() instanceof Player)) {
				if (ev.getDamager() instanceof Projectile)
					Func.wykonajDlaNieNull(((Projectile) ev.getDamager()).getShooter(), s -> {
						if (s instanceof Player)
							((Player) s).sendMessage(prefix + "Serce można zniszczyć tylko ręcznie");
					});
				return;
			}
			Drużyna drużyna = drużyna(ev.getDamager());
			if (serce.drużyna.equals(drużyna)) {
				ev.getDamager().sendMessage(prefix + "Nie możesz zniszczyć serca swojej drużyny!");
				return;
			}
			if (serce.uderzone != null) {
				ev.getDamager().sendMessage(prefix + "Poczekaj chwile zanim uderzysz");
				return;
			}
			
			if (--serce.życia <= 0) {
				ev.getEntity().remove();
				serce.oznacz((Player) ev.getDamager());
				serce.arena.napiszGraczom("%s ukradł serce drużyny %s", drużyna.napisy + ev.getDamager().getName(), serce.drużyna);
			} else {
				serce.uderzone = System.currentTimeMillis();
				serce.arena.opóznijTask(serce.arena.czasOchronySercaPoZniszczeniu * 20, () -> {
					serce.uderzone = null;
					serce.serce.setCustomName("");
					serce.serce.setCustomNameVisible(false);
				});
				serce.arena.napiszGraczom("Serce drużyny %s zostało uszkodzone! (%s/%s)", serce.drużyna.toString(), serce.życia, serce.arena.życiaSerc);
				Location loc = serce.drużyna.serceLoc;
				loc.getWorld().createExplosion(5, 5, 5, 5, false, false);
				serce.serce.setCustomNameVisible(true);
				serce.ustawCooldown();
			}
		});
	}
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getWhoClicked()), arena -> {
			int slot = ev.getRawSlot();
			Inventory inv = ev.getInventory();
			if (slot < 0 || slot >= inv.getSize())
				return;
			
			if (inv.getHolder() != null && inv.getHolder() instanceof PustyHolder) {
				if (slot == 12)
					arena.otwórzSklep((Player) ev.getWhoClicked(), arena.itemyDoKupienia.get(0));
				else if (slot == 14)
					arena.otwórzSklep((Player) ev.getWhoClicked(), drużyna(ev.getWhoClicked()).guiUlepszeń);
				return;
			}
			
			// Zdejmowanie Zbroji
			if (ev.getInventory() instanceof PlayerInventory) {
				Main.log(ev.getRawSlot(), ev.getSlot(), ev.getSlotType());
				ev.setCancelled(true);
				return;
			}
			
			
			// Sklep Itemów
			if (inv.getHolder() != null && inv.getHolder() instanceof SklepItemStrona)
				Func.wykonajDlaNieNull(((SklepItemStrona) inv.getHolder()).znajdzItem(slot),
						sitem -> sitem.kliknięty((Player) ev.getWhoClicked(), arena));
			else if (inv.getHolder() != null && inv.getHolder() instanceof Drużyna)
				((Drużyna) inv.getHolder()).kliknięte((Player) ev.getWhoClicked(), slot);
		});
		
	}
	
	@EventHandler
	public void otwieranieEc(InventoryOpenEvent ev) {
		if (ev.getInventory().getType().equals(InventoryType.ENDER_CHEST))
			Func.wykonajDlaNieNull(drużyna(ev.getPlayer()), drużyna -> {
				ev.setCancelled(true);
				ev.getPlayer().openInventory(drużyna.ec);
			});
	}
	
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void śmierć(PlayerDeathEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getEntity()), arena -> {
			ev.setKeepInventory(false);
			PlayerInventory inv = ev.getEntity().getInventory();
			ev.getDrops().remove(inv.getItem(EquipmentSlot.HEAD));
			ev.getDrops().remove(inv.getItem(EquipmentSlot.CHEST));
			ev.getDrops().remove(inv.getItem(EquipmentSlot.LEGS));
			ev.getDrops().remove(inv.getItem(EquipmentSlot.FEET));
			
			Func.wykonajDlaNieNull(arena.oznaczeni.remove(ev.getEntity().getName()), lista -> lista.forEach(serce -> {
				Player kiler = ev.getEntity().getKiller();
				Drużyna dk = drużyna(kiler);
				if (kiler != null)
					if (dk != null && dk.equals(serce.drużyna)) {
						arena.napiszGraczom("Drużyna %s odzyskała swoje Serce!", dk);
						serce.powrót();
					} else {
						serce.oznacz(kiler);
						arena.napiszGraczom("%s przejął serce drużyny %s od gracza %s", 
								(dk == null ? "" : dk.napisy) + kiler.getName(), serce.drużyna, drużyna(ev.getEntity()).napisy + ev.getEntity().getName());
					}
				else {
					serce.powrót();
					arena.napiszGraczom("%s zginął! Serce drużyny %s powróciło na swoje miejsce i jest gotowe do ponownego przejęcia!",
							drużyna(ev.getEntity()).napisy + ev.getEntity().getName(), serce.drużyna);
				}
			}));
		});
	}
	@EventHandler
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(drużyna(ev.getPlayer()), drużyna -> {
			ev.setRespawnLocation(drużyna.respawnLoc);
			ev.getPlayer().setGameMode(GameMode.SPECTATOR);
			Arena arena = arena(ev.getPlayer());
			int czasGry = (int) ((System.currentTimeMillis() / 1000) - arena.czasStartuAreny);
			int czekanie = czasGry / 60;
			Krotka<Integer, Runnable> k = new Krotka<>(czekanie + 1, null);
			k.b = () -> {
				ev.getPlayer().sendTitle("§bZginołeś", "§aZa " + --k.a + " sekund zrespisz się", 0, 30, 20);
				if (k.a > 0)
					arena.opóznijTask(20, k.b);
				else {
					ev.getPlayer().teleport(drużyna.respawnLoc);
					ev.getPlayer().setGameMode(GameMode.SURVIVAL);
					ev.getPlayer().sendTitle("§eWskrzeszenie", "§aZ prochów powstałeś!", 0, 30, 20);
					arena.ubierz(ev.getPlayer(), drużyna);
				}
			};
			k.b.run();
		});
	}
	
	@EventHandler
	public void klikanieMoba(PlayerInteractAtEntityEvent ev) {
		Func.wykonajDlaNieNull(sklep(ev.getRightClicked()), arena1 -> {
			Func.wykonajDlaNieNull(arena(ev.getPlayer()), arena2 -> {
				if (arena1.equals(arena2))
					arena1.otwórzSklep(ev.getPlayer(), Arena.guiGłówne);
			});
		});
	}
	
	
	
	// Override

	@Override
	Minigra.Arena zaczynanaArena() {
		if (zaczynanaArena != null) 
			return zaczynanaArena;
		
		Arena arena = (Arena) super.zaczynanaArena();
		if (arena == null) 
			return null;
		
		arena.regeneruj();
		
		return arena;
	}
	
	@Override
	public int czas() {
		int w = super.czas();
		
		for (Minigra.Arena arena : mapaAren.values())
			if (arena.grane)
				((Arena) arena).sekunda();
		
		return w;
	}

	static final String metaMobSklepu = "mimiBedWarsAlaZigaMobSklepu";
	Arena sklep(Entity p) {return metadata(p, metaMobSklepu); }
	static final String metaSerce = "mimiBedWarsAlaZigaSerce";
	Serce serce(Entity p) {return metadata(p, metaSerce); }
	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Drużyna 	drużyna (Entity p) { return super.drużyna(p); }
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }
	

	static final Config configAreny = new Config("configi/BedWarsAlaZiga Areny");
	@Override Config getConfigAreny()	 { return configAreny; }

	@Override String getMetaStatystyki() { return "mimiBedWarsAlaZigaStaty"; }
	@Override String getMetaDrużynaId()	 { return "mimiBedWarsAlaZigaDrużyna"; }
	@Override String getMetaId()		 { return "mimiMinigraBedWarsAlaZiga"; }

	@Override String getPrefix() { return prefix; }
}
