package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

@Moduł
public class BedWarsAlaZiga extends MinigraDrużynowa {
	public static final String prefix = Func.prefix("Bedwars ala Ziga");
	public static class Arena extends MinigraDrużynowa.Arena {
		@Mapowane List<Drużyna> drużyny;
		@Mapowane List<Ruda> rudy;
		
		@Mapowane int życiaSerc = 3;
		@Mapowane int czasOchronySercaPoZniszczeniu = 15;
		@Mapowane double mnożnikCzekaniaRespawnu = 1; // 1 -> ilość minut gry = ilość sekund czekania
		
		final HashMap<String, Serce> oznaczeni = new HashMap<>();
		
		
		int sekundyStartu;
		
		
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
			sekundyStartu = (int) (System.currentTimeMillis() / 1000);
		}
		@Override
		void koniec() {
			super.koniec();
			oznaczeni.clear();
			for (Drużyna drużyna : drużyny)
				drużyna.koniec();
		}
		@Override
		Player opuść(int i, boolean info) {
			Player p = super.opuść(i, info);
			upuśćSerce(p);
			return p;
		}
		
		void upuśćSerce(Player p) {
			Serce serce = oznaczeni.remove(p.getName());
			if (serce == null) return;
			serce.powrót();
			napiszGraczom("%s upuścił serce drużyny %s", p.getName(), serce.drużyna);
		}
		
		
		// util
		
		void particleSerc() {
			for (Entry<String, Serce> en : oznaczeni.entrySet()) {
				Player p = Bukkit.getPlayer(en.getKey());
				en.getValue().drużyna.particle(p.getLocation().add(0, 5, 0), 100, 0, 5, 0);
			}
		}
		
		@Override
		<D extends MinigraDrużynowa.Drużyna> void ubierz(Player p, D drużyna, boolean hełm, boolean spodnie, boolean klata, boolean buty) {
			
		}
		
		
		//  Override
		
		BedWarsAlaZiga inst;
		@Override MinigraDrużynowa getInstMinigraDrużynowa() { return inst; }
		@Override <M extends Minigra> void setInst(M inst)	 { this.inst = (BedWarsAlaZiga) inst; }

		@Override List<? extends Drużyna> getDrużyny() { return drużyny; }
		@Override int getMinDrużyny() 				   { return 2; }

		@Override Supplier<? extends me.jomi.mimiRPG.Minigry.Minigra.Statystyki> noweStaty() { return Statystyki::new; }
	}
	public static class Drużyna extends MinigraDrużynowa.Drużyna {
		public static enum Slot {
			miecz,
			łuk,
			armor,
			narzędzie;
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

			int lvl;
			Slot slot;
			Enchantment ench;
			Enchant(Enchantment ench, Slot slot) {
				this.ench = ench;
				this.slot = slot;
			}
		}
		@Mapowane Location serceLoc;
		@Mapowane Location respawnLoc;
		Serce serce;

		static final Inventory guiGłówne;
		static {
			guiGłówne = Bukkit.createInventory(null, 3*9, Func.koloruj("&lUlepszenia"));
			Func.ustawPuste(guiGłówne);
			guiGłówne.setItem(12, Func.stwórzItem(Material.NETHERITE_SWORD, "&4Itemki",		"&bPrzedmioty jednorazowe"));
			guiGłówne.setItem(14, Func.stwórzItem(Material.ENCHANTED_BOOK,  "&aUlepszenia", "&bTrwałe wzmocnienia"));
		}

		Inventory ec;
		
		Inventory guiUlepszenia;
		Inventory guiItemki;
		
		void przygotujStart(Arena arena) {
			serce = new Serce(arena, this);
			for (Enchant ench : Enchant.values())
				ench.lvl = 0;
			
			ec = Bukkit.createInventory(null, 9, Func.koloruj(napisy + "Enderchest"));
			
			
			guiUlepszenia = Bukkit.createInventory(null, 5*9, Func.koloruj("&lUlepszenia Drużynowe"));
			Func.ustawPuste(guiUlepszenia);
			
			guiUlepszenia.setItem(12, Func.stwórzItem(Material.ENCHANTED_BOOK, "&aWydajność"));
			
			guiUlepszenia.setItem(14, Func.stwórzItem(Material.ENCHANTED_BOOK, "&cOstrość"));
			guiUlepszenia.setItem(15, Func.stwórzItem(Material.ENCHANTED_BOOK, "&cOdrzut"));
			guiUlepszenia.setItem(16, Func.stwórzItem(Material.ENCHANTED_BOOK, "&cZaklęty Ogień"));
			
			guiUlepszenia.setItem(21, Func.stwórzItem(Material.ENCHANTED_BOOK, "&bOchrona"));
			guiUlepszenia.setItem(22, Func.stwórzItem(Material.ENCHANTED_BOOK, "&bCiernie"));
			
			guiUlepszenia.setItem(30, Func.stwórzItem(Material.ENCHANTED_BOOK, "&eNieskończoność"));
			guiUlepszenia.setItem(31, Func.stwórzItem(Material.ENCHANTED_BOOK, "&eMoc"));
			guiUlepszenia.setItem(32, Func.stwórzItem(Material.ENCHANTED_BOOK, "&ePłomień"));
			guiUlepszenia.setItem(33, Func.stwórzItem(Material.ENCHANTED_BOOK, "&eUderzenie"));
		}
		void koniec() {
			Func.wykonajDlaNieNull(serce, serce -> serce.serce.remove());
			guiUlepszenia = null;
			guiItemki = null;
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
		
	}
	public static class Ruda extends Mapowany {
		@Mapowane Material blok = Material.IRON_ORE;
		@Mapowane int sekundyOdrespiania = 10;
		@Mapowane ItemStack drop;
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
		
		boolean chronione;
		
		public Serce(Arena arena, Drużyna drużyna) {
			this.arena = arena;
			this.drużyna = drużyna;
			życia = arena.życiaSerc;
		}
		
		void zresp() {
			serce = (EnderCrystal) drużyna.serceLoc.getWorld().spawnEntity(drużyna.serceLoc, EntityType.ENDER_CRYSTAL);
			Func.ustawMetadate(serce, metaSerce, this);
		}
		
		void oznacz(Player p) {
			arena.oznaczeni.put(p.getName(), this);
		}
		void powrót() {
			zresp();
		}
	}
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
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
			if (serce.drużyna.equals(drużyna(ev.getDamager()))) {
				ev.getDamager().sendMessage(prefix + "Nie możesz zniszczyć serca swojej drużyny!");
				return;
			}
			if (serce.chronione) {
				ev.getDamager().sendMessage(prefix + "Poczekaj chwile zanim uderzysz");
				return;
			}
			
			if (--serce.życia <= 0) {
				ev.getEntity().remove();
				serce.oznacz((Player) ev.getDamager());
				serce.arena.napiszGraczom("%s ukradł serce drużyny %s", drużyna(ev.getDamager()).napisy + ev.getDamager().getName(), serce.drużyna);
			} else {
				serce.chronione = true;
				serce.arena.opóznijTask(serce.arena.czasOchronySercaPoZniszczeniu * 20, () -> serce.chronione = false);
				serce.arena.napiszGraczom("Serce drużyny %s zostało uszkodzone! (%s/%s)", serce.drużyna.toString(), serce.życia, serce.arena.życiaSerc);
			}
		});
	}
	
	@EventHandler
	public void zdejmowanieZbroji(InventoryClickEvent ev) {
		if (ev.getSlotType().equals(SlotType.ARMOR))
			Func.wykonajDlaNieNull(arena(ev.getWhoClicked()), a -> ev.setCancelled(true));
	}
	
	@EventHandler
	public void otwieranieEc(InventoryOpenEvent ev) {
		if (ev.getInventory().getType().equals(InventoryType.ENDER_CHEST))
			Func.wykonajDlaNieNull(drużyna(ev.getPlayer()), drużyna -> {
				ev.setCancelled(true);
				ev.getPlayer().openInventory(drużyna.ec);
			});
	}
	
	
	@EventHandler
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(drużyna(ev.getPlayer()), drużyna -> {
			ev.setRespawnLocation(drużyna.respawnLoc);
			ev.getPlayer().setGameMode(GameMode.SPECTATOR);
			Arena arena = arena(ev.getPlayer());
			int czasGry = (int) ((System.currentTimeMillis() / 1000) - arena.czasStartu);
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
			
			Func.wykonajDlaNieNull(arena.oznaczeni.remove(ev.getEntity().getName()), serce -> {
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
			});
		});
	}
	
	
	
	
	
	// Override

	@Override
	public int czas() {
		int w = super.czas();
		
		for (Minigra.Arena arena : mapaAren.values())
			if (arena.grane)
				((Arena) arena).particleSerc();
		
		return w;
	}
	
	static final String metaSerce = "mimiBedWarsAlaZigaSerce";
	Serce serce(Entity p) {return metadata(p, metaSerce); }
	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Drużyna 	drużyna (Entity p) { return super.drużyna(p); }
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }
	

	private final Config configAreny = new Config("configi/BedWarsAlaZiga Areny");
	@Override Config getConfigAreny()	 { return configAreny; }

	@Override String getMetaStatystyki() { return "mimiBedWarsAlaZigaStaty"; }
	@Override String getMetaDrużynaId()	 { return "mimiBedWarsAlaZigaDrużyna"; }
	@Override String getMetaId()		 { return "mimiMinigraBedWarsAlaZiga"; }

	@Override String getPrefix() { return prefix; }
}
