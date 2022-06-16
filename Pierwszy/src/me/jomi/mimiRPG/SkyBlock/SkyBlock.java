package me.jomi.mimiRPG.SkyBlock;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicDouble;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.world.level.border.WorldBorder;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.DołączanieDoWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.OpuszczanieWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.PrzeliczaniePunktówWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.TworzenieWyspyEvent;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.API.UsuwanieWyspyEvent;
import me.jomi.mimiRPG.util.Cena;
import me.jomi.mimiRPG.util.Ciąg;
import me.jomi.mimiRPG.util.CmdChecker;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Cooldown;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

//TODO /is tempban
//TODO przycisk back w menu wyspy

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class SkyBlock extends Komenda implements Przeładowalny, Listener {
	static final String permBypass = Func.permisja("skyblock.admin");
	public static final String prefix = Func.prefix("Skyblock");
	static CmdChecker checker;

	// API
	public static class API {
		public static abstract class WyspyEvent extends Event {
			public final Wyspa wyspa;
			
			public WyspyEvent(Wyspa wyspa) {
				this.wyspa = wyspa;
			}
			
			protected void wywołaj() {
				Bukkit.getPluginManager().callEvent(this);
			}

			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
		public static abstract class WyspyCancellableEvent extends WyspyEvent implements Cancellable {
			public WyspyCancellableEvent(Wyspa wyspa) { super(wyspa); }
			
			private boolean cancelled = false;
			@Override public boolean isCancelled() { return cancelled; }
			@Override public void setCancelled(boolean stan) { cancelled = stan; }
		}
		
		public static class PrzeliczaniePunktówWyspyEvent extends WyspyEvent {
			public final double pktPrzed;
			public final Player p;
			public double pktPo;

			public PrzeliczaniePunktówWyspyEvent(Wyspa wyspa, Player p, double pktPrzed, double pktPo) {
				super(wyspa);
				this.p = p;
				this.pktPo = pktPo;
				this.pktPrzed = pktPrzed;
				wywołaj();
			}
		}
		
		public static class UsuwanieWyspyEvent extends WyspyCancellableEvent {
			public UsuwanieWyspyEvent(Wyspa wyspa) {
				super(wyspa);
				wywołaj();
			}
		}
		public static class TworzenieWyspyEvent extends WyspyCancellableEvent {
			public final Player p;

			public TworzenieWyspyEvent(Wyspa wyspa, Player p) {
				super(wyspa);
				this.p = p;;
				wywołaj();
			}
		}
		public static class OpuszczanieWyspyEvent extends WyspyCancellableEvent {
			public final String nick;

			public OpuszczanieWyspyEvent(Wyspa wyspa, String nick) {
				super(wyspa);
				this.nick = nick;
				wywołaj();
			}
		}
		public static class DołączanieDoWyspyEvent extends WyspyCancellableEvent {
			public final Player p;

			public DołączanieDoWyspyEvent(Wyspa wyspa, Player p) {
				super(wyspa);
				this.p = p;;
				wywołaj();
			}
		}
	}
	
	static class Biom {
		final Biome biom;
		final String permisja;
		final ItemStack ikona;
		private static int _id = 0;
		private final int id;

		public Biom(Biome biom, Material ikona, String permisja, List<String> opis) {
			this.ikona = Func.stwórzItem(ikona, (permisja == null ? "&9" : "&6") + Func.enumToString(biom), opis);
			this.permisja = permisja;
			this.biom = biom;
			this.id = _id++;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof Biom && ((Biom) obj).id == id;
		}
	}
	static class TypWyspy {
		// typ: TypWyspy
		private static final HashMap<String, TypWyspy> mapa = new HashMap<>();
		public final String schematOverworld;
		public final String schematNether;
		public final Biome biomOverworld;
		public final Biome biomNether;
		public final Material ikona;
		public final List<String> opis;
		public final double pkt;
		public final double dx; // przesunięcie wyspa.locHome od wyspa.locŚrodek
		public final double dy;
		public final double dz;
		private static int _id = 0;
		private final int id;

		public TypWyspy(String schematOverworld, String schematNether, double pkt, double dx, double dy, double dz,
				Biome biomOverworld, Biome biomNether, Material ikona, List<String> opis) {
			this.schematOverworld = schematOverworld;
			this.schematNether = schematNether;
			this.biomOverworld = biomOverworld;
			this.biomNether = biomNether;
			this.ikona = ikona;
			this.opis = opis;
			this.pkt = pkt;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.id = _id++;
		}

		public void wklejSchematy(int x, int y, int z) {
			try {
				wklejSchemat(TypWyspy.class.getDeclaredField("schematOverworld"), "zwyczajna", new Location(Światy.overworld, x, y, z));
				wklejSchemat(TypWyspy.class.getDeclaredField("schematNether"), "netherowa", new Location(Światy.nether, x, y, z));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private void wklejSchemat(Field schemat, String nazwaPodstawowa, Location loc) throws Throwable {
			if (Baza.we != null) {
				if (Func.wklejSchemat((String) schemat.get(this), loc))
					return;
				for (TypWyspy typ : mapa.values())
					if (Func.wklejSchemat((String) schemat.get(typ), loc))
						return;
				wrzućDomyślneSchematicki();
				if (Func.wklejSchemat((String) schemat.get(mapa.get(nazwaPodstawowa)), loc))
					return;
			}
			Func.wykonajNaBlokach(loc.clone().add(3, -1, 3), loc.clone().add(-3, -1, -3), blok -> {
				blok.setType(Material.GRASS_BLOCK, false);
				return true;
			});
		}

		static void wrzućDomyślneSchematicki() {
			Func.wyjmijPlik("Configi/wyspaZwyczajna.schem", Main.path + "wyspaZwyczajna.schem");
			Func.wyjmijPlik("Configi/wyspaNether.schem", Main.path + "wyspaNether.schem");
			mapa.put("zwyczajna",
					new TypWyspy(Main.path + "wyspaZwyczajna.schem", Main.path + "wyspaNether.schem", 0, -2, 0, 0,
							Biome.PLAINS, Biome.NETHER_WASTES, Material.GRASS_BLOCK,
							Func.koloruj(Lists.newArrayList(new String[] { "&8Zwyczajna wyspa" }))));
		}

		public static TypWyspy zNazwy(String nazwa) {
			TypWyspy typ = mapa.get(nazwa);
			if (typ == null || !typ.istniejąSchematy())
				typ = mapa.get("zwyczajna");
			if (typ == null || !typ.istniejąSchematy()) {
				for (TypWyspy _typ : mapa.values())
					if (_typ.istniejąSchematy())
						return _typ;
				wrzućDomyślneSchematicki();
				return mapa.get("zwyczajna");
			}
			return typ;
		}

		public boolean istniejąSchematy() {
			return new File(schematOverworld).exists() && new File(schematNether).exists();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof TypWyspy))
				return false;
			return id == ((TypWyspy) obj).id;
		}
	}
	static class Światy {
		static World overworld;
		static World nether;
		static String nazwaOverworld;
		static String nazwaNether;
		static boolean dozwolonyNether;

		static boolean należy(World świat) {
			return należy(świat.getName());
		}
		static boolean należy(String nazwaŚwiata) {
			return nazwaŚwiata.equals(nazwaOverworld) || (dozwolonyNether && nazwaŚwiata.equals(nazwaNether));
		}
	}
	public static class TopInfo extends Mapowany {
		@Mapowane double pkt;
		@Mapowane List<String> opis;
		@Mapowane String nick;
		@Mapowane String nazwa;
		@Mapowane int idWyspy;
	}
	public static class Holder implements InventoryHolder {
		private Inventory inv;
		TypInv typ;
		Wyspa wyspa;

		public Holder(Wyspa wyspa, TypInv typ, int rzędy) {
			this(wyspa, typ, rzędy, Func.enumToString(typ));
		}
		public Holder(Wyspa wyspa, TypInv typ, int rzędy, String nazwa) {
			inv = Func.stwórzInv(this, rzędy, nazwa);
			Func.ustawPuste(inv, Baza.pustySlotCzarny);
			this.typ = typ;
			this.wyspa = wyspa;
		}
		@Override
		public Inventory getInventory() {
			return inv;
		}
	}
	static class Ulepszenia {
		public static class Ulepszenie {
			public Ulepszenie(int wartość, Cena cena) {
				this.wartość = wartość;
				this.cena = cena;
			}

			public final int wartość;
			public final Cena cena;
		}

		public static Ulepszenie[] limityBloków;
		public static Ulepszenie[] członkowie;
		public static Ulepszenie[] generator;
		public static Ulepszenie[] wielkość;
		public static Ulepszenie[] magazyn;
		public static Ulepszenie[] warpy;

		private static void sprawdzSyntax(String nazwaPola, int max, int min, String prefixInfo) throws Throwable {
			Ulepszenie[] upgrs = (Ulepszenie[]) Ulepszenia.class.getDeclaredField(nazwaPola).get(null);
			for (int i = 0; i < upgrs.length; i++) {
				Ulepszenie upgr = upgrs[i];
				if (upgr.wartość > max) {
					Main.warn(
							prefixInfo + " nie może przekroczyć " + max + ", w Skyblock.yml w ulepszeniach ulepszenia."
									+ nazwaPola + " zostało podane " + upgr.wartość);
					upgrs[i] = new Ulepszenie(max, upgr.cena);
				} else if (upgr.wartość < min) {
					Main.warn(prefixInfo + "nie może być mniejsza niż " + min
							+ ", w Skyblock.yml w ulepszeniach ulepszenia." + nazwaPola + " zostało podane "
							+ upgr.wartość);
					upgrs[i] = new Ulepszenie(min, upgr.cena);
				}
			}
		}

		static void sprawdzSyntax() {
			try {
				for (Field field : Ulepszenia.class.getDeclaredFields())
					if (((Ulepszenie[]) field.get(null)).length <= 0)
						Main.error("Skyblock Brak ulepszeń typu " + field.getName());

				int mxWarpy = 6 * 9 - (Wyspa.Permisje.class.getDeclaredFields().length - 2);
				int gen = SkyBlock.generator.size();
				sprawdzSyntax("wielkość", odstęp, 1, "Wielkość wsypy");
				sprawdzSyntax("warpy", mxWarpy, 0, "Ilość warpów wyspy");
				sprawdzSyntax("magazyn", 6, 1, "Wielkość magazynu wsypy");
				sprawdzSyntax("generator", gen, 1, "Poziom generatora wyspy");
				sprawdzSyntax("członkowie", 6 * 9, 1, "Liczba członków na wsypie");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	public static class Wyspa extends Mapowany {
		public Wyspa() {
		}// Konstruktor dla mapowanego
	
		public static class Permisje {
			@Target(value = ElementType.FIELD)
			@Retention(value = RetentionPolicy.RUNTIME)
			public static @interface ObejmujeOdwiedzających {
			}

			public Permisje() {
			}

			public Permisje(String grupa) {
				this.grupa = grupa;
			}

			String grupa;

			@ObejmujeOdwiedzających
			public boolean niszczenie; // v
			@ObejmujeOdwiedzających
			public boolean stawianie; // v
			@ObejmujeOdwiedzających
			public boolean dostęp_do_spawnerów_i_maszyn; // v

			@ObejmujeOdwiedzających
			public boolean otwieranie_drzwi_i_furtek; // v
			@ObejmujeOdwiedzających
			public boolean otwieranie_skrzyń; // v
			@ObejmujeOdwiedzających
			public boolean używanie_przycisków_dzwigni; // v
			@ObejmujeOdwiedzających
			public boolean używanie_armorstandów_itemframów; // v

			public boolean wyrzucanie_członków_i_uncoop; // v
			public boolean zapraszanie_członków_i_coop; // v

			public boolean wyrzucanie_banowanie_odwiedzających; // v

			public boolean zmiana_prywatności; // v

			@ObejmujeOdwiedzających
			public boolean używanie_portalu; // v

			public boolean liczenie_wartości_wyspy; // v

			public boolean kupowianie_ulepszeń; // v

			public boolean ustawianie_home_wyspy; // v

			public boolean dodawanie_i_usuwanie_warpów;

			public boolean dostęp_do_kasy_banku; // v
			public boolean dostęp_do_expa_banku; // v
			public boolean dostęp_do_magazynu; // v

			@ObejmujeOdwiedzających
			public boolean bicie_mobów; // v

			@ObejmujeOdwiedzających
			public boolean podnoszenie_itemów; // v

			public boolean zmiana_koloru_bordera;

			public boolean usuwanie_grup_permisji; // v
			public boolean tworzenie_grup_permisji; // v
			public boolean edytowanie_hierarchii_grup_permisji; // v
			public boolean edytowanie_permisji; // v
			public boolean awansowanie_i_degradowanie_członków; // v

			public boolean zmiana_dropu; // v

			public boolean zmiana_nazwy_wyspy; // v

			public boolean zmiana_biomu; // v

			List<Boolean> warpy = Lists.newArrayList(); // v
		}
		public static class Warp extends Mapowany {
			@Mapowane String nazwa;
			@Mapowane Location loc;
		}
		public static class Poziomy extends Mapowany {
			@Mapowane int limityBloków; // v // limityBloków = limit hoperów | limityBloków / 2 = limit spawnerów
			@Mapowane int członkowie; // v // wartość - maksymalna ilość osób na wyspe
			@Mapowane int generator; // v // - SkyBlock.generator.get(this.generator - 1)
			@Mapowane int wielkość; // v // wartość - długość boku wyspy
			@Mapowane int magazyn; // v // wartość - ilość rządków magazynu
			@Mapowane int warpy; // v // wartość - maksymalna ilość warpów
			/*
			 * # Ulepszenia <nazwa>: #- wartość cena - 0 100 - 2 200 - 5 300
			 * 
			 * 
			 */
		}

		public Wyspa(Player p, TypWyspy typ) {
			Func.zdemapuj(this, new HashMap<>());

			id = configData.wczytajInt("id następnej wyspy");

			nazwa = p.getName();

			członkowie.put(p.getName(), "właściciel");

			dataUtworzenia = Func.data();

			Krotka<Integer, Integer> xz = następnaPozycja();
			locŚrodek = new Location(Światy.overworld, xz.a * odstęp, yWysp, xz.b * odstęp);

			locHome = locŚrodek.clone().add(typ.dx, typ.dy, typ.dz);

			dodatkowe_pkt = typ.pkt;
			
			for (Entry<String, TypWyspy> en : TypWyspy.mapa.entrySet())
				if (en.getValue().equals(typ)) {
					this.typ = en.getKey();
					break;
				}

			permsKody = Lists.newArrayList("właściciel 111111111111111111111111111111",
					"współwłaściciel 111111111111111111111111111111", "moderator 111111111111111111111111011101",
					"członek 111111100111110011111100000101", "odwiedzający 000101000001000000001000000000");
			wczytajPermisjeZKodów();

			permsNietykalne.forEach(perm -> {
				if (!perms.containsKey(perm))
					Main.error("Pzreoczona domyślna permisja: " + perm);
			});

			if (new TworzenieWyspyEvent(this, p).isCancelled())
				return;

			typ.wklejSchematy(locŚrodek.getBlockX(), locŚrodek.getBlockY(), locŚrodek.getBlockZ());
			
			sprawdzTop();

			zapiszNatychmiast();

			zmieńBiom(Światy.overworld, typ.biomOverworld);
			if (Światy.dozwolonyNether)
				zmieńBiom(Światy.nether, typ.biomNether);

			configData.ustaw("wyspy loc." + dolnyRóg(locŚrodek), id);

			configData.ustaw("id następnej wyspy", id + 1);

			configData.zapisz();

			Gracz g = Gracz.wczytaj(p);
			g.wyspa = id;
			g.zapisz();

			p.teleport(locHome);

			Main.log(prefix + p.getName() + " utworzył wyspę typu " + this.typ);
		}
		static String dolnyRóg(Location loc) {
			int x = (int) ((loc.getX() + odstęp / 2.0) / odstęp);
			int z = (int) ((loc.getZ() + odstęp / 2.0) / odstęp);

			return x + "_" + z;
		}
		public static Wyspa wczytaj(Location loc) {
			if (!Światy.należy(loc.getWorld()))
				return null;
			Wyspa wyspa = wczytaj(configData.wczytaj("wyspy loc." + dolnyRóg(loc), -1));
			return wyspa == null ? null : wyspa.zawieraIgnorujŚwiat(loc) ? wyspa : null;
		}
		public static Wyspa wczytaj(Player p) {
			return wczytaj(Gracz.wczytaj(p));
		}
		public static Wyspa wczytaj(Gracz g) {
			return g.wyspa == -1 ? null : wczytaj(g.wyspa);
		}
		// id: wyspa
		static final HashMap<Integer, WeakReference<Wyspa>> mapaWysp = new HashMap<>();
		public static Wyspa wczytaj(int id) {
			if (id == -1)
				return null;
			WeakReference<Wyspa> wyspa = mapaWysp.get(id);
			if (wyspa == null || wyspa.get() == null)
				mapaWysp.put(id, wyspa = new WeakReference<>((Wyspa) getConfig(id).wczytaj("wyspa")));
			return wyspa.get();
		}
		static Wyspa wczytajzid(int id) {
			return wczytaj(id);
		}
		static Config getConfig(int id) {
			return new Config("configi/Wyspy/" + id);
		}
		@Mapowane Poziomy poziomy = Func.utwórz(Poziomy.class);
		@Mapowane HashMap<String, String> członkowie; // nick: nazwaGrupyPermisji
		@Mapowane String dataUtworzenia;
		@Mapowane Location locŚrodek;
		@Mapowane String typ;
		@Mapowane int id;

		private void usuń() {
			if (new UsuwanieWyspyEvent(this).isCancelled())
				return;

			configData.ustaw("wyspy loc." + dolnyRóg(locŚrodek), null);

			członkowie.keySet().forEach(nick -> {
				Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), Func::tpSpawn);
				Gracz g = Gracz.wczytaj(nick);
				g.wyspa = -1;
				g.zapisz();
			});

			doZapisania = false;
			usuńZTop();
			getConfig(id).usuń();

			powiadomCzłonków("Wyspa została usunięta");

			Krotka<Location, Location> rogi = rogi();
			Func.wykonajNaBlokach(rogi.a, rogi.b, blok -> {
				if (blok.getType() == Material.AIR)
					return false;
				blok.setType(Material.AIR, false);
				return true;
			});
			if (Światy.dozwolonyNether) {
				rogi = new Krotka<>(rogi.a.clone(), rogi.b.clone());
				rogi.a.setWorld(Światy.nether);
				rogi.b.setWorld(Światy.nether);
				Func.wykonajNaBlokach(rogi.a, rogi.b, blok -> {
					if (blok.getType() == Material.AIR)
						return false;
					blok.setType(Material.AIR, false);
					return true;
				});
			}

			rogi = rogi();
			for (int x=rogi.a.getBlockX(); x <= rogi.b.getBlockX(); x += 16)
				for (int z=rogi.a.getBlockX(); z <= rogi.b.getBlockX(); z += 16) {
					_usuń(Światy.overworld.getChunkAt(x / 16, z / 16));
					if (Światy.dozwolonyNether)
						_usuń(Światy.nether.getChunkAt(x / 16, z / 16));
				}
		}
		private void _usuń(Chunk chunk) {
			for (Entity entity : chunk.getEntities())
				if (!(entity instanceof Player) && this.zawiera(entity.getLocation()))
					entity.remove();
		}

		public boolean zawiera(Location loc) {
			if (!Światy.należy(loc.getWorld()))
				return false;
			return zawieraIgnorujŚwiat(loc);
		}

		public boolean zawieraIgnorujŚwiat(Location loc) {
			Krotka<Location, Location> rogi = rogi();
			return Func.zawiera(true, false, true, loc, rogi.a, rogi.b);
		}

		// Generator

		public void generujRude(BlockFormEvent ev, boolean cobl) {
			MonoKrotka<Ciąg<Material>> krotka = ev.getBlock().getWorld().getName().equals(Światy.nazwaOverworld)
					? generator().a
					: generator().b;
			Ciąg<Material> gen = cobl ? krotka.a : krotka.b;

			ev.getNewState().setType(gen.losuj());
		}

		// Overworld Nether
		// ((cobl, stone), (cobl, stone))
		MonoKrotka<MonoKrotka<Ciąg<Material>>> generator() {
			return generator.get(Ulepszenia.generator[poziomy.generator].wartość - 1);
		}

		// Limity Bloków

		@Mapowane int hopery;
		@Mapowane int spawnery;
		public boolean postawiony(Player p, Material blok) {
			if (blok == Material.HOPPER) {
				if (hopery + 1 > Ulepszenia.limityBloków[poziomy.limityBloków].wartość)
					return Func.powiadom(p, prefix + "Nie możesz postawić więcej leji");
				hopery++;
			} else if (blok == Material.SPAWNER) {
				if (spawnery + 1 > Ulepszenia.limityBloków[poziomy.limityBloków].wartość / 4)
					return Func.powiadom(p, prefix + "Nie możesz postawić więcej spawnerów");
				spawnery++;
			} else
				return false;
			zapisz();
			return false;
		}
		public void zniszczony(Material blok) {
			if (blok == Material.HOPPER)
				hopery = Math.max(--hopery, 0);
			else if (blok == Material.SPAWNER)
				spawnery = Math.max(--spawnery, 0);
			else
				return;
			zapisz();
		}

		// Permisje

		static final List<String> permsNietykalne = Arrays.asList("właściciel", "członek", "odwiedzający");
		@Mapowane List<String> permsKody; // grupa: Permisje // posortowane według priorytetów
		final HashMap<String, Permisje> perms = new HashMap<>();

		@Override
		protected void Init() {
			wczytajPermisjeZKodów();
		}
		void wczytajPermisjeZKodów() {
			perms.clear();
			for (String kod : permsKody) {
				Permisje perm = permZKodu(kod);
				perms.put(perm.grupa, perm);
			}
		}
		Permisje permZKodu(String nazwaKod) {
			List<String> nazwaIKod = Func.tnij(nazwaKod, " ");

			Permisje perm = new Permisje(nazwaIKod.get(0));
			String kod = nazwaIKod.get(1);

			Field[] fields = Permisje.class.getDeclaredFields();
			try {
				for (int i = 1; i < fields.length - 1; i++)
					fields[i].set(perm, kod.charAt(i - 1) == '1');
				for (int i = 0; i < warpy.size(); i++) {
					try {
						perm.warpy.add(kod.charAt(fields.length + i - 2) == '1');
					} catch (IndexOutOfBoundsException e) {
						perm.warpy.add(true);
						Func.opóznij(1, () -> odświeżKodPermisji(perm, nazwaKod + "1"));
						Main.warn("Brak permisji dla warpa na wyspie o id:" + id + " \"" + nazwaKod + "\"(" + kod.length() + " znaków kodu) warpy: " + perm.warpy.size() + "/" + warpy.size() + ", przyznawanie dostępu");
					}
				}
			} catch (Throwable e) {
				Main.warn("coś nie tak z kodem na wyspie o id:" + id + " \"" + nazwaKod + "\"(" + kod.length() + " znaków kodu) warpy: " + perm.warpy.size() + "/" + warpy.size() + " permisje: " + (fields.length - 2));
				odświeżKodPermisji(perm);
				wczytajPermisjeZKodów();
			}
			return perm;
		}
		void odświeżKodPermisji(Permisje perm) {
			StringBuilder strB = new StringBuilder();
			strB.append(perm.grupa).append(' ');

			Field[] fields = Permisje.class.getDeclaredFields();
			for (int i = 1; i < fields.length - 1; i++)
				try {
					strB.append(fields[i].getBoolean(perm) ? '1' : '0');
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			for (int i=0; i < warpy.size(); i++)
				strB.append((perm.warpy.size() >= i || perm.warpy.get(i)) ? '1' : '0');

			odświeżKodPermisji(perm, strB.toString());

			zapisz();
		}
		private void odświeżKodPermisji(Permisje perm, String kod) {
			for (int i = 0; i < permsKody.size(); i++)
				if (permsKody.get(i).startsWith(perm.grupa)) {
					permsKody.set(i, kod);
					return;
				}
			permsKody.add(kod);
		}
		void odświeżKodyWszystkichPermisji() {
			Lists.newArrayList(perms.values()).forEach(this::odświeżKodPermisji);
		}

		public Permisje permisje(Player p) {
			if (maBypass(p))
				return perms.get("właściciel");
			return permisje(p.getName());
		}
		public Permisje permisje(String nick) {
			return perms.get(Func.domyślna(członkowie.get(nick), () -> 
				Func.domyślnaTry(() -> coop.contains(Wyspa.wczytaj(Gracz.wczytaj(nick)).id), false) ? "członek" : "odwiedzający"));
		}
		
		
		// /is permissions

		public void permisjeInv(Player p) {
			if (!permisje(p).edytowanie_permisji) {
				p.sendMessage(prefix + "Nie możesz tego zrobić");
				return;
			}
			p.openInventory(getInvPermisje(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvPermisje(Player p) {
			Inventory invPermisje = new Holder(this, TypInv.PERMISJE_MAIN, (permsKody.size() - 1) / 9 + 1)
					.getInventory();

			int i = -1;
			int ip = indexPerm(permisje(p));
			for (String perm : permsKody) {
				String grp = Func.tnij(perm, " ").get(0);
				invPermisje.setItem(++i, Func.stwórzItem(
						ip < i ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "&a&l" + grp));
			}
			return invPermisje;
		}
		private void klikanyInvPermisje(Player p, int slot) {
			if (slot >= permsKody.size())
				return;
			int i = -1;
			boolean może = false;
			Permisje perm = permisje(p);
			for (String str : permsKody) {
				if (++i == slot) {
					if (może)
						permisjeEdytuj(p, perms.get(Func.tnij(permsKody.get(i), " ").get(0)));
					else
						p.sendMessage(prefix + "Nie możesz edytować tych uprawnień");
					return;
				}
				if (str.startsWith(perm.grupa))
					może = true;
			}
		}
		private void permisjeEdytuj(Player p, Permisje perm) {
			p.openInventory(permisjeEdytujInv(permisje(p), perm));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory permisjeEdytujInv(Permisje p, Permisje perm) {
			Inventory inv = new Holder(this, TypInv.PERMISJE,
					(permsKody.get(0).length() - "właściciel ".length() - 1) / 9 + 1,
					"&4Edytuj Uprawnienia &9" + perm.grupa).getInventory();

			Field[] pola = Permisje.class.getDeclaredFields();
			try {
				for (int i = 1; i < pola.length - 1; i++) {
					boolean możeP = pola[i].getBoolean(p);
					boolean możePerm = pola[i].getBoolean(perm);

					ItemStack item = Func
							.stwórzItem(
									!możeP ? Material.BLUE_STAINED_GLASS_PANE
											: (możePerm ? Material.LIME_STAINED_GLASS_PANE
													: Material.RED_STAINED_GLASS_PANE),
									"&9" + pola[i].getName().replace("_", " "));
					if (perm.grupa.equals("odwiedzający")
							&& !pola[i].isAnnotationPresent(Permisje.ObejmujeOdwiedzających.class))
						item = Func.dodajLore(Func.typ(item, Material.WHITE_STAINED_GLASS_PANE), "&4Nie dotyczy");

					inv.setItem(i - 1, item);
				}
				if (!perm.grupa.equals("odwiedzający"))
					for (int i = -1; i < warpy.size() - 1; i++) {
						boolean możeP = p.warpy.get(i + 1);
						boolean możePerm = perm.warpy.get(i + 1);

						inv.setItem(pola.length + i,
								Func.stwórzItem(
										!możeP ? Material.BLUE_STAINED_GLASS_PANE
												: (możePerm ? Material.LIME_STAINED_GLASS_PANE
														: Material.RED_STAINED_GLASS_PANE),
										"&ewarp " + warpy.get(i + 1).nazwa));
					}
			} catch (Throwable e) {
				e.printStackTrace();
			}

			return inv;
		}
		void klikanyPermisjeEdytujInv(Player p, int slot, String invName, ItemStack item) {
			boolean red = false;
			switch (item.getType()) {
			case RED_STAINED_GLASS_PANE:
				red = true;
			case LIME_STAINED_GLASS_PANE:
				break;
			case BLUE_STAINED_GLASS_PANE:
				p.sendMessage(prefix + "Nie masz uprawnień do tych uprawnień");
			default:
				return;
			}
			Permisje perm = perms.get(invName.substring("&4Edytuj Uprawnienia &9".length()));
			try {
				Field[] pola = Permisje.class.getDeclaredFields();
				if (slot < pola.length - 2) {
					String permisja = Func.getDisplayName(item.getItemMeta()).substring(2).replace(" ", "_");
					Permisje.class.getDeclaredField(permisja).set(perm, red);
					if (!red)
						Func.wykonajDlaNieNull(zamknijPanele(permisja), typ -> zamknijPanele(typ, permisja));
				} else {
					perm.warpy.set(slot - pola.length + 2, red);
					odświeżInvWarpy();
				}
				odświeżKodPermisji(perm);
				odświeżEdytoryPermisji();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		private TypInv zamknijPanele(String permisja) {
			switch (permisja) {
			case "awansowanie_i_degradowanie_członków":
				return TypInv.CZŁONKOWIE;
			case "kupowianie_ulepszeń":
				return TypInv.ULEPSZENIA;
			case "dostęp_do_magazynu":
				return TypInv.MAGAZYN;
			case "zmiana_koloru_bordera":
				return TypInv.BORDER;
			case "zmiana_biomu":
				return TypInv.BIOM;
			case "edytowanie_permisji":
				zamknijPanele(TypInv.PERMISJE_MAIN, permisja);
				return TypInv.PERMISJE;
			case "zmiana_dropu":
				zamknijPanele(TypInv.DROP_OVERWORLD, permisja);
				zamknijPanele(TypInv.DROP_NETHER, permisja);
				return TypInv.DROP;
			}
			
			return null;
		}
		private void zamknijPanele(TypInv typ, String permisja) {
			Bukkit.getOnlinePlayers().forEach(p -> Func
					.wykonajDlaNieNull(p.getOpenInventory().getTopInventory().getHolder(), Holder.class, holder -> {
						try {
							if (holder.typ == typ && holder.wyspa.equals(this)
									&& !Permisje.class.getDeclaredField(permisja).getBoolean(permisje(p)))
								p.closeInventory();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}));
		}
		void odświeżEdytoryPermisji() {
			for (Player p : Bukkit.getOnlinePlayers()) {
				Inventory inv = p.getOpenInventory().getTopInventory();
				Func.wykonajDlaNieNull(inv.getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this))
						if (holder.typ == TypInv.PERMISJE_MAIN)
							permisjeInv(p);
						else if (holder.typ == TypInv.PERMISJE)
							Func.wykonajDlaNieNull(
									perms.get(Func.getTitle(p.getOpenInventory())
											.substring("&4Edytuj Uprawnienia &9".length())),
									perm -> permisjeEdytuj(p, perm));
				});
			}
		}

		// /is permsedit

		public void edytorPermisji(Player p, String[] args) throws MsgCmdError {
			Permisje permP = permisje(p);
			checker.checkFormat(permP.edytowanie_hierarchii_grup_permisji || permP.tworzenie_grup_permisji || permP.usuwanie_grup_permisji,
					"Nie masz wystarczających uprawnień aby zarządzać uprawnieniami");
			try {
				Permisje perm = perms.get(args[2]);
				Permisje perm2;
				int mnZwiększ = 1;
				switch (args[1]) {
				case "zwiększ":
					mnZwiększ = -1;
				case "zmniejsz":
					checker.checkFormat(permP.edytowanie_hierarchii_grup_permisji, "Nie masz wystarczających uprawnień aby edytować hierarchie grup permisji");
					if (permsNietykalne.contains(perm.grupa))
						break;
					int ipermP = indexPerm(permP);
					checker.checkFormat(!(indexPerm(perm) <= ipermP), "Nie możesz edytować tak ważnych permisji");
					for (int i = 0; i < permsKody.size(); i++)
						if (permsKody.get(i).startsWith(perm.grupa)) {
							perm2 = kodToPerm(i + mnZwiększ);

							if (permsNietykalne.contains(perm2.grupa))
								break;
							checker.checkFormat(!(indexPerm(perm2) <= ipermP), "Nie możesz edytować tak ważnych permisji");
							String s1 = permsKody.get(i);
							String s2 = permsKody.get(i + mnZwiększ);
							permsKody.set(i, s2);
							permsKody.set(i + mnZwiększ, s1);
							odświeżEdytoryPermisji();
							zapisz();
							break;
						}
					break;
				case "usuń":
					checker.checkFormat(permP.usuwanie_grup_permisji, "Nie masz wystarczających uprawnień aby usuwać grupy permisji");
					
					Matcher mat = Pattern.compile("Tak, chcę usunąć grupe permisji (\\w+)")
							.matcher(Func.listToString(args, 3));
					if (mat.find())
						Func.wykonajDlaNieNull(perms.get(mat.group(1)), permisja -> {
							checker.checkFormat(!permsNietykalne.contains(permisja.grupa), "Nie można usunąć tej grupy permisji");
							for (String permisjaCzłonka : członkowie.values())
								checker.checkFormat(!permisjaCzłonka.equals(permisja.grupa), "Nie można usunąć grupy w której są jacyś gracze!");
							checker.checkFormat(!(indexPerm(permisja) <= indexPerm(permP)), "Nie możesz usunąć tak ważnych permisji");
							for (int i = 0; i < permsKody.size(); i++)
								if (permsKody.get(i).startsWith(permisja.grupa)) {
									permsKody.remove(i);
									break;
								}
							perms.remove(permisja.grupa);
							odświeżEdytoryPermisji();
							zapisz();
						});
					break;
				case "dodaj":
					checker.checkFormat(permP.tworzenie_grup_permisji, "Nie masz wystarczających uprawnień aby dodawać grupy permisji");
					checker.checkFormat(args.length > 3, "po znaku >> wpisz nazwe grupy");
					checker.checkFormat(args.length < 5, "Nazwa grupy musi być pojedyńczym słowem");
					String nazwa = args[3];
					checker.checkFormat(!perms.containsKey(nazwa), "Grupa permisji o tej nazwie już istnieje");
					
					StringBuilder strB = new StringBuilder(nazwa).append(' ');
					try {
						for (int i = 0; i < Permisje.class.getDeclaredFields().length - 2; i++)
							strB.append('0');
						for (int i = 0; i < warpy.size(); i++)
							strB.append('0');

					} catch (Throwable e) {
						e.printStackTrace();
					}
					permsKody.add(permsKody.size() - 1, strB.toString());
					wczytajPermisjeZKodów();
					odświeżEdytoryPermisji();
					zapisz();
					break;
				}
			} catch (Throwable e) {
			}
			edytorPermisjiWyświetl(p);
		}
		void edytorPermisjiWyświetl(Player p) {
			Napis n = new Napis("\n\n§9Permisje wyspy: \n\n");
			for (String grp : permsKody) {
				grp = grp.substring(0, grp.indexOf(' '));
				n.dodaj("§e- §d" + grp + " ");
				if (!permsNietykalne.contains(grp)) {
					n.dodaj(new Napis("§6↑", "§bKliknij aby zwiększyć priorytet", "/is permsedit zwiększ " + grp));
					n.dodaj(" ");
					n.dodaj(new Napis("§6↓", "§bKliknij aby zmniejszyć priorytet", "/is permsedit zmniejsz " + grp));
					n.dodaj("   ");
					n.dodaj(new Napis("§c[x]", "§cKliknij aby usunąć\ngrupe §4" + grp, Action.SUGGEST_COMMAND,
							"/is permsedit usuń >> Tak, chcę usunąć grupe permisji " + grp));
				}
				n.dodaj("\n");
			}
			n.dodaj(new Napis("§a[nowa]", "§bKliknij aby dodać nową grupe", "/is permsedit dodaj >> "));
			n.dodaj("\n\n");
			n.wyświetl(p);
		}

		// /is warps

		@Mapowane List<Warp> warpy;
		public void otwórzWarpy(Player p) {
			p.openInventory(getInvWarpy(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvWarpy(Player p) {
			Inventory invWarpy = new Holder(this, TypInv.WARPY, (warpy.size() - 1) / 9 + 1).getInventory();

			Permisje perms = permisje(p);

			int i = -1;
			for (Warp warp : warpy)
				invWarpy.setItem(++i,
						Func.stwórzItem(
								perms.warpy.get(i) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
								"&9&l" + warp.nazwa));

			return invWarpy;
		}
		private void klikanyInvWarp(Player p, int slot, ItemStack item) {
			if (slot >= warpy.size() || slot < 0)
				return;
			if (permisje(p).warpy.get(slot))
				tpToWarp(p, warpy.get(slot));
			else
				p.sendMessage(prefix + "Nie masz uprawnień na korzystanie z tego warpa");
		}
		public void tpToWarp(Player p, Warp warp) {
			p.teleport(warp.loc);
			p.sendMessage(prefix + Func.msg("Zostałeś przeteleportowany na warp %s", warp.nazwa));
		}
		void odświeżInvWarpy() {
			for (Player p : Bukkit.getOnlinePlayers())
				Func.wykonajDlaNieNull(p.getOpenInventory().getTopInventory().getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this))
						if (holder.typ == TypInv.WARPY)
							otwórzWarpy(p);
						else if (holder.typ == TypInv.DEL_WARP)
							otwórzInvDelWarp(p);

				});
		}

		// /is addwarp

		public void dodajWarp(Player p, String nazwa) throws MsgCmdError {
			checker.checkFormat(warpy.size() < Ulepszenia.warpy[poziomy.warpy].wartość, "Wyspa osiągneła już limit warpów");
			checker.checkFormat(permisje(p).dodawanie_i_usuwanie_warpów, "Nie masz uprawnień na dodawanie warpów");
			checker.checkFormat(zawiera(p.getLocation()), "Nie możesz tu ustawić warpa wyspy");
			
			Warp warp = new Warp();
			warp.loc = p.getLocation();
			warp.nazwa = Func.koloruj(nazwa);
			warpy.add(warp);

			perms.values().forEach(perm -> perm.warpy.add(true));

			odświeżInvWarpy();

			odświeżKodyWszystkichPermisji();
			odświeżEdytoryPermisji();

			zapisz();
			inst.throwFormatMsg("Dodano nowy warp");
		}

		// /is delwarp

		public void otwórzInvDelWarp(Player p) {
			if (!permisje(p).dodawanie_i_usuwanie_warpów) {
				p.sendMessage(prefix + "Nie masz uprawnień aby usuwać warpy");
				return;
			}
			p.openInventory(dajInvDelWarp(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		Inventory dajInvDelWarp(Player p) {
			Inventory inv = new Holder(this, TypInv.DEL_WARP, Func.potrzebneRzędy(warpy.size())).getInventory();

			Permisje perm = permisje(p);

			for (int i = 0; i < warpy.size(); i++)
				inv.setItem(i,
						Func.stwórzItem(
								perm.warpy.get(i) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
								"&b&l" + warpy.get(i).nazwa));

			return inv;
		}
		void klikaniyInvDelWarp(Player p, int slot, Material mat) {
			if (mat == Material.RED_STAINED_GLASS_PANE)
				p.sendMessage(prefix + "Nie masz dostępu do tego warpu");
			else if (mat == Material.LIME_STAINED_GLASS_PANE)
				usuńWarp(slot);
		}
		void usuńWarp(int index) {
			warpy.remove(index);

			perms.values().forEach(perm -> perm.warpy.remove(index));

			odświeżInvWarpy();

			odświeżKodyWszystkichPermisji();
			odświeżEdytoryPermisji();

			zapisz();
		}
		
		
		// /is coop /is uncoop
		
		static Set<String> coopZaproszenia = Sets.newConcurrentHashSet();
		
		@Mapowane List<Integer> coop;
		public void coop(Player p, String zKim) throws MsgCmdError {
			checker.checkFormat(permisje(p).zapraszanie_członków_i_coop, "Nie masz permisji do zakładania coopów");
			
			Player kogoP = checker.nieNullFormat(Bukkit.getPlayer(zKim),	"%s nie jest online", zKim);
			Wyspa wyspa  = checker.nieNullFormat(Wyspa.wczytaj(kogoP),	"%s nie ma Wyspy", Func.getDisplayName(kogoP));

			checker.checkFormat(!coop.contains(wyspa.id), "Kooperacja z tą wyspą jest już nawiązana");

			String kodJaTy = kodcoop(this, wyspa);
			String kodTyJa = kodcoop(wyspa, this);
			
			checker.checkFormat(!coopZaproszenia.contains(kodJaTy), "Zaproszenie do coopu do tej wyspy zostało już wysłane");
			if (coopZaproszenia.remove(kodTyJa)) {
				// Przyjmowanie coopu
				BiConsumer<Wyspa, Wyspa> przyjmij = (w1, w2) -> {
					w1.coop.add(w2.id);
					w1.zapisz();
					w1.powiadomCzłonków("Nawiązano kooperacja z wyspą graczy " + w2.infoCzłonkowie());
				};
				przyjmij.accept(this, wyspa);
				przyjmij.accept(wyspa, this);
			} else {
				// zapraszanie do coopu
				coopZaproszenia.add(kodJaTy);
				powiadomCzłonków("%s wysłał zaproszenie do cooperacji z wyspą graczy %s, które wygaśnie za 5 minut", Func.getDisplayName(p), wyspa.infoCzłonkowie());
				powiadomCzłonków("Otrzymano zaproszenie do cooperacji z wyspą graczy %s od %s, które wygaśnie za 5 minut", this.infoCzłonkowie(), Func.getDisplayName(p));
				Func.opóznij(5*60*20, () -> {
					if (coopZaproszenia.remove(kodJaTy)) {
						wyspa.powiadomCzłonków("Zaproszenie do cooperacji z wyspą graczy %s od %s wygasło", this .infoCzłonkowie(), Func.getDisplayName(p));
						this .powiadomCzłonków("Zaproszenie do cooperacji z wyspą graczy %s od %s wygasło", wyspa.infoCzłonkowie(), Func.getDisplayName(p));
					}
				});
			}
		}
		public boolean uncoop(Player p, String zKim) throws MsgCmdError {
			checker.checkFormat(permisje(p).wyrzucanie_członków_i_uncoop, "Nie masz permisji do zakładania coopów");
			
			Player kogoP = checker.nieNullFormat(Bukkit.getPlayer(zKim),	"%s nie jest online", zKim);
			Wyspa wyspa  = checker.nieNullFormat(Wyspa.wczytaj(kogoP),	"%s nie ma Wyspy", Func.getDisplayName(kogoP));
			
			boolean f1 = this .coop.remove((Integer) wyspa.id);
			boolean f2 = wyspa.coop.remove((Integer) this .id);
			if (f1 || f2) {
				this .powiadomCzłonków("kooperacja z wyspą graczy %s została zerwana przez %s", wyspa.infoCzłonkowie(), Func.getDisplayName(p));
				wyspa.powiadomCzłonków("kooperacja z wyspą graczy %s została zerwana przez %s", this .infoCzłonkowie(), Func.getDisplayName(p));
				
				this.zapisz();
				wyspa.zapisz();
				return false;
			} else
				inst.throwFormatMsg("Między twoją wyspą a wyspą %s nie jest nawiązana kooperacja", Func.getDisplayName(kogoP));
			return true;
		}
		private static String kodcoop(Wyspa wyspa1, Wyspa wyspa2) {
			return wyspa1.id + "-" + wyspa2.id;
		}
		
		
		// /is drop

		@Mapowane List<String> wyłączoneDropyOverworld;
		@Mapowane List<String> wyłączoneDropyNether;
		public void otwórzDrop(Player p) {
			if (permisje(p).zmiana_dropu) {
				p.openInventory(dajInvDrop());
				p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			} else
				p.sendMessage(prefix + "Nie masz uprawnień do zmiany dropu");
		}
		Inventory dajInvDrop() {
			Inventory inv = new Holder(this, TypInv.DROP, 3).getInventory();

			if (!Światy.dozwolonyNether)
				inv.setItem(13, Func.stwórzItem(Material.GRASS_BLOCK, "&aOverworld"));
			else {
				inv.setItem(12, Func.stwórzItem(Material.GRASS_BLOCK, "&aOverworld"));
				inv.setItem(14, Func.stwórzItem(Material.CRIMSON_NYLIUM, "&4Nether"));
			}

			return inv;
		}
		void klikanyInvDrop(Player p, Material mat) {
			switch (mat) {
			case GRASS_BLOCK:
				p.openInventory(dajInvDropOverworld());
				break;
			case CRIMSON_NYLIUM:
				p.openInventory(dajInvDropNether());
				break;
			default:
				return;
			}
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		Inventory invDropOverworld;
		Inventory dajInvDropOverworld() {
			if (invDropOverworld == null)
				invDropOverworld = dajInvDrop(TypInv.DROP_OVERWORLD, generator().a, wyłączoneDropyOverworld);
			return invDropOverworld;
		}
		Inventory invDropNether;
		Inventory dajInvDropNether() {
			if (invDropNether == null)
				invDropNether = dajInvDrop(TypInv.DROP_NETHER, generator().b, wyłączoneDropyNether);
			return invDropNether;
		}
		void klikanyInvDropOverworld(Player p, ItemStack item) {
			klikanyInvDrop(p, item, wyłączoneDropyOverworld);
		}
		void klikanyInvDropNether(Player p, ItemStack item) {
			klikanyInvDrop(p, item, wyłączoneDropyNether);
		}
		void klikanyInvDrop(Player p, ItemStack item, List<String> wyłączoneDropy) {
			if (item.isSimilar(Baza.pustySlotCzarny))
				return;

			String mat = item.getType().toString();

			if (!wyłączoneDropy.remove(mat))
				wyłączoneDropy.add(mat);

			Func.ustawLore(item, wyłączoneDropy.contains(mat) ? "&cWyłączony" : "&aWłączony",
					Func.getLore(item.getItemMeta()).size() - 2);

			zapisz();
		}
		Inventory dajInvDrop(TypInv typ, MonoKrotka<Ciąg<Material>> krotka, List<String> wyłączoneDropy) {
			odświeżWyłączonyDropGeneratora(wyłączoneDropy, krotka);
			HashMap<Material, MonoKrotka<Double>> mapa = new HashMap<>();

			krotka.a.szanse().forEach((mat, szansa) -> mapa.put(mat, new MonoKrotka<>(szansa, 0.0)));
			krotka.a.szanse().forEach((mat, szansa) -> {
				MonoKrotka<Double> k = mapa.getOrDefault(mat, new MonoKrotka<>());
				k.b = szansa;
				mapa.put(mat, k);
			});

			Inventory inv = new Holder(this, typ, Func.potrzebneRzędy(mapa.size())).getInventory();

			int i = 0;
			for (Map.Entry<Material, MonoKrotka<Double>> en : mapa.entrySet()) {
				ItemStack item = Func.stwórzItem(en.getKey(), "&c" + Func.enumToString(en.getKey()));
				if (en.getValue().a != 0)
					Func.dodajLore(item, "&7Cobl:  &e" + Func.zaokrąglij(en.getValue().a * 100, 3) + "%");
				if (en.getValue().b != 0)
					Func.dodajLore(item, "&7Stone: &e" + Func.zaokrąglij(en.getValue().b * 100, 3) + "%");
				Func.dodajLore(item, "");
				Func.dodajLore(item, wyłączoneDropy.contains(en.getKey().toString()) ? "&cWyłączony" : "&aWłączony");
				Func.dodajLore(item, "");
				inv.setItem(i++, item);
			}

			return inv;
		}
		public void odświeżGenerator() {
			Consumer<Inventory> zamknij = inv -> {
				if (inv != null)
					Lists.newArrayList(inv.getViewers()).forEach(p -> Func.wykonajDlaNieNull(p, HumanEntity::closeInventory));
			};

			zamknij.accept(invDropOverworld);
			zamknij.accept(invDropNether);

			invDropOverworld = null;
			invDropNether = null;
		}
		void odświeżWyłączonyDropGeneratora(List<String> lista, MonoKrotka<Ciąg<Material>> krotka) {
			boolean zapisywać = false;

			Set<String> akt = Sets.newConcurrentHashSet();
			akt.addAll(Func.wykonajWszystkim(krotka.a.klucze(), Material::toString));
			akt.addAll(Func.wykonajWszystkim(krotka.b.klucze(), Material::toString));

			for (String mat : Lists.newArrayList(lista))
				if (!akt.contains(mat)) {
					zapisywać = true;
					lista.remove(mat);
				}

			if (zapisywać)
				zapisz();
		}
		public void generator(Material typ, BlockBreakEvent ev) {
			if (!ev.isDropItems())
				return;

			List<String> wyłączoneDropy = ev.getBlock().getWorld().getName().equals(Światy.nazwaOverworld) ? wyłączoneDropyOverworld : wyłączoneDropyNether;

			if (wyłączoneDropy.contains(typ.toString()))
				ev.setDropItems(false);
		}

		
		// /is transfer

		public void transfer(Player p, String komu) throws MsgCmdError {
			checker.checkFormat(członkowie.containsKey(p.getName()), "Tylko prawowity właściciel wyspy może przekazać włąściciela");
			checker.checkFormat(członkowie.get(p.getName()).equals("właściciel"), "Tylko właściciel wyspy może przekazać swoją range");
			checker.checkFormat(członkowie.containsKey(komu), "%s nie należy do twojej wyspy", komu);
			Main.panelTakNie(p, "&4Oddać range właściciela dla &c" + komu + "&4?",
					"&aTak, nie chce jej dłużej nosić",
					"&cNiee! ona jest moja!",
					() -> przekażWłaściciela(p, komu), null);
		}
		private boolean przekażWłaściciela(Player p, String komu) {
			if (!członkowie.containsKey(komu))
				return Func.powiadom(p, prefix + Func.msg("%s nie należy do twojej wyspy", komu));
			członkowie.remove(p.getName());
			członkowie.put(p.getName(), członkowie.remove(komu));
			członkowie.put(komu, "właściciel");
			zapisz();

			odświeżTopJeśliZawiera();

			powiadomCzłonków("%s przekazał range właściciela wyspy dla %s", p.getName(), komu);

			return false;
		}

		
		// /is info

		public void info(CommandSender p) {
			Napis n = new Napis("\n\n");

			n.dodajK("&aWyspa &4" + nazwa);
			n.dodaj("\n");

			n.dodaj(infoCzłonkowie());
			n.dodaj("\n");
			n.dodajEndK("&aData stworzenia: &e" + dataUtworzenia,
					"&aGenerator: &e" + Ulepszenia.generator[poziomy.generator].wartość + " lvl",
					"&aPunkty: &e" + Func.DoubleToString(pkt), "&aTyp: &e" + typ);

			n.dodaj(new Napis("§aKooperacje: §e"));
			if (coop.isEmpty())
				n.dodajK("&cBrak");
			else
				coop.forEach(id -> {
					Wyspa wyspa = Wyspa.wczytaj(id);
					if (wyspa != null)
						n.dodaj(new Napis("\n§e§l- §e" + wyspa.nazwa, "§d(" + wyspa.id + ") §b" + wyspa.infoCzłonkowie().toString()));
					else
						n.dodaj("\n§e§l- §4Wyspa usunięta");
				});
			
			n.dodaj("\n\n");

			n.wyświetl(p);
		}
		Napis infoCzłonkowie() {
			Napis n = new Napis();
			
			
			n.dodajK("&aCzłonkowie: ");
			
			int dł = "Członkowie: ".length();

			Iterator<Entry<String, String>> it = członkowie.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> en = it.next();
				n.dodaj(new Napis(Func.koloruj("&e" + en.getKey()), Func.koloruj("&b" + en.getValue())));
				if (it.hasNext()) {
					dł += en.getKey().length();
					if (dł <= Main.ust.wczytaj("opt.lore.znaki na linie", 50)) {
						n.dodaj("\n");
						dł = 0;
					}
					n.dodajK("&f, ");
				}
			}

			return n;
		}

		
		// /is bank

		@Mapowane int exp;
		@Mapowane int kasa;
		public void otwórzBank(Player p) {
			Permisje perm = permisje(p);
			if (!(perm.dostęp_do_expa_banku || perm.dostęp_do_kasy_banku || perm.dostęp_do_magazynu)) {
				p.sendMessage(prefix + "Nie masz uprawnień do banku wyspy");
				return;
			}
			p.openInventory(getInvBank());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		static int slotBankKasa = 11;
		static int slotBankExp = 13;
		static int slotBankMagazyn = 15;
		private Inventory invBank = null;
		private Inventory getInvBank() {
			if (invBank == null) {
				invBank = new Holder(this, TypInv.BANK, 3).getInventory();
				invBank.setItem(slotBankKasa, Func.stwórzItem(Material.PAPER, "&6Pieniądze", ""));
				invBank.setItem(slotBankExp, Func.stwórzItem(Material.EXPERIENCE_BOTTLE, "&6Exp", ""));
				invBank.setItem(slotBankMagazyn, Func.stwórzItem(Material.CHEST, "&6Magazyn"));
				odświeżInvBank();
			}

			return invBank;
		}
		private void odświeżInvBank() {
			Func.ustawLore(invBank.getItem(slotBankKasa), "&7Dostępne środki: &a" + kasa + "$", 0);
			Func.ustawLore(invBank.getItem(slotBankExp), "&7Zgromadzony Exp: &a" + exp, 0);
		}
		public void wpłaćExp(Player p, int ile) {
			if (ile == 0 || (ile > 0 && Poziom.policzCałyExp(p) < ile))
				return;
			p.giveExp(-ile);
			exp += ile;
			odświeżInvBank();
			zapisz();
			Main.log(prefix + p.getName() + " wpłacił " + ile + " expa do banku, całkowita ilość: " + exp);
		}
		public void wpłaćPieniądze(Player p, int ile) {
			if (ile == 0 || (ile > 0 && Main.econ.getBalance(p) < ile))
				return;

			if (ile > 0)
				Main.econ.withdrawPlayer(p, ile);
			else
				Main.econ.depositPlayer(p, -ile);

			kasa += ile;

			odświeżInvBank();
			zapisz();

			Main.log(prefix + p.getName() + " wpłacił " + ile + "$ do banku, całkowita ilość: " + kasa);
		}
		private boolean klikanyBank(Player p, int slot, ClickType clickType) {
			int min;
			int prawy;
			Supplier<Integer> lewy;

			BiConsumer<Player, Integer> bic = null;
			if (slot == slotBankKasa) {
				if (!permisje(p).dostęp_do_kasy_banku)
					return Func.powiadom(p, prefix + "Nie masz uprawnień do środków wyspy");
				lewy = () -> Math.abs((int) Main.econ.getBalance(p));
				bic = this::wpłaćPieniądze;
				min = 1000;
				prawy = kasa;
			} else if (slot == slotBankExp) {
				if (!permisje(p).dostęp_do_expa_banku)
					return Func.powiadom(p, prefix + "Nie masz uprawnień do expa wyspy");
				lewy = () -> Poziom.policzCałyExp(p);
				bic = this::wpłaćExp;
				min = 1725;
				prawy = exp;
			} else if (slot == slotBankMagazyn) {
				otwórzMagazyn(p);
				return true;
			} else
				return true;

			int ile;
			switch (clickType) {
			// wpłacanie
			case LEFT:
				ile = Math.min(min, lewy.get());
				break;
			case SHIFT_LEFT:
				ile = lewy.get();
				break;
			// wypłacanie
			case RIGHT:
				ile = -Math.min(min, prawy);
				break;
			case SHIFT_RIGHT:
				ile = -prawy;
				break;
			default:
				return true;
			}

			bic.accept(p, ile);
			return false;
		}

		
		// /is storage

		@Mapowane List<ItemStack> magazynItemów;
		public void otwórzMagazyn(Player p) {
			if (permisje(p).dostęp_do_magazynu)
				p.openInventory(getInvMagazyn());
			else
				p.sendMessage(prefix + "Nie masz uprawnień do magazynu wyspy");
		}
		private Inventory invMagazyn = null;
		private Inventory getInvMagazyn() {
			if (invMagazyn == null) {
				invMagazyn = new Holder(this, TypInv.MAGAZYN, Ulepszenia.magazyn[poziomy.magazyn].wartość).getInventory();
				invMagazyn.clear();
				int i = 0;
				for (ItemStack item : magazynItemów)
					invMagazyn.setItem(i++, item);
			}
			return invMagazyn;
		}
		private void zamknięcieMagazynu() {
			magazynItemów.clear();
			invMagazyn.forEach(magazynItemów::add);
			zapisz();
		}
		void odświeżMagazyn() {
			Func.wykonajDlaNieNull(invMagazyn, inv -> {
				Lists.newArrayList(inv.getViewers()).forEach(HumanEntity::closeInventory);
				invMagazyn = null;
			});
		}

		// /is sethome

		@Mapowane Location locHome;
		public void ustawHome(Player p) throws MsgCmdError {
			checker.checkFormat(permisje(p).ustawianie_home_wyspy, "Nie masz uprawnień na ustawnie home wyspy");
			checker.checkFormat(zawiera(p.getLocation()), "Nie możesz ustawić home wyspy poza swoją wyspą");
			locHome = p.getLocation();
			zapisz();
			inst.throwFormatMsg("Ustawiono nowy home wyspy");
		}

		// /is home

		public void tpHome(Player p) {
			p.teleport(locHome);
			p.sendMessage(prefix + "Zostałeś przeteleportowany na swoją wyspę");
		}

		
		// /is public /is private

		@Mapowane boolean prywatna = true;
		public void ustawPubliczna(Player p) {
			ustawPubliczność(p, true);
		}
		public void ustawPrywatna(Player p) {
			if (ustawPubliczność(p, false))
				for (Player gracz : Bukkit.getOnlinePlayers())
					if (!członkowie.containsKey(gracz.getName()) && !maBypass(gracz) && zawiera(gracz.getLocation()))
						Func.tpSpawn(gracz);
		}
		private boolean ustawPubliczność(Player p, boolean publiczna) throws MsgCmdError {
			checker.checkFormat(permisje(p).zmiana_prywatności, "Nie masz uprawnień do zmiany prywatności wyspy");
			String msg = publiczna ? "publiczn" : "prywatn";
			if (prywatna == publiczna) {
				prywatna = !prywatna;
				zapisz();
				p.sendMessage(prefix + "Ustawiono wyspę na " + msg + "ą");
				return true;
			} else
				p.sendMessage(prefix + "Twoja wyspa jest już " + msg + "a");
			return false;
		}

		
		// /is value
		
		private static final Panel panel_isValue = new Panel(true);
		static {
			panel_isValue.ustawClick(ev -> {
				if (Baza.pustySlotCzarny.isSimilar(ev.getCurrentItem())) return;
				
				Wyspa wyspa = (Wyspa) panel_isValue.dajDanePanelu(ev.getInventory());
				Material mat = ev.getCurrentItem().getType();
				Inventory inv = ev.getWhoClicked().getInventory();
				double pkt = punktacja.getOrDefault(mat.name(), 0d);
				int posiadane = wyspa.punkty.getOrDefault(mat.name(), 0);
				
				int ile = 0;
				for (int i=0; i < inv.getSize(); i++) {
					ItemStack item = inv.getItem(i);
					if (item != null && item.getType() == mat && new ItemStack(mat).isSimilar(item)) {
						ile += item.getAmount();
						inv.setItem(i, null);
					}
				}
				
				if (ile == 0)
					ev.getWhoClicked().sendMessage(prefix + Func.msg("Nie posiadzasz żadnego %s w swoim eq", Func.enumToString(mat)));
				else {
					wyspa.punkty.put(mat.name(), posiadane + ile);
					posiadane += ile;
					wyspa.policzWartość((Player) ev.getWhoClicked());
					wyspa.powiadomCzłonków("%s wrzucił %sx %s (%spkt) do /is value aktualna wartość wyspy: %spkt",
							ev.getWhoClicked().getName(), ile, Func.enumToString(mat), ile * pkt, wyspa.pkt);
					
					ItemStack item = ev.getCurrentItem();
					List<String> lore = Func.getLore(item.getItemMeta());
					lore.set(1, "§6Posiadane§7: §e" + posiadane);
					lore.set(2, "§6Razem§7: §e" + Func.DoubleToString(posiadane * pkt));
					ev.getInventory().setItem(ev.getRawSlot(), Func.ustawLore(item, lore));
				}
			});
		}
		private Inventory inv_isValue = null;
		
		@Mapowane double dodatkowe_pkt;
		@Mapowane double pkt;
		@Mapowane boolean przeliczył = false;
		boolean blokadaLiczenia = false;
		@Mapowane HashMap<String, Integer> punkty = new HashMap<>();
		
		public boolean wartość(Player p) throws MsgCmdError {
			checker.checkFormat(permisje(p).liczenie_wartości_wyspy, "Nie masz uprawnień do przeliczania wartości wyspy");

			if (!przeliczył && !blokadaLiczenia)
				zbierzStareBloki(p);
			
			p.openInventory(dajInv_isValue());
			
			return false;
		}
		private void zbierzStareBloki(Player p) {
			blokadaLiczenia = true;
			
			Runnable koniec = () -> {
				przeliczył = true;
				blokadaLiczenia = false;
				
				if (inv_isValue != null)
					while (!inv_isValue.getViewers().isEmpty())
						inv_isValue.getViewers().get(0).closeInventory();
				
				inv_isValue = null;
				
				policzWartość(p);
				
				powiadomCzłonków("Zebrano drogocenne bloki, aktualna wartość wyspy: %s", pkt);
			};
			
			Krotka<Location, Location> rogi = rogi();
			Predicate<Block> work = blok -> {
				Func.wykonajDlaNieNull(podmianaBloków.get(blok.getType()), mat -> {
					punkty.put(blok.getType().name(), punkty.getOrDefault(blok.getType().name(), 0) + 1);
					blok.setType(mat, false);
				});
				return true;
			};
			Func.wykonajNaBlokach(rogi.a, rogi.b, work, () -> {
				if (Światy.dozwolonyNether) {
					rogi.a.setWorld(Światy.nether);
					rogi.b.setWorld(Światy.nether);
					Func.wykonajNaBlokach(rogi.a, rogi.b, work, koniec);
				} else
					koniec.run();
			}); 
		}
		private Inventory dajInv_isValue() {
			if (inv_isValue == null || (punktacja.size() > 6*9 && inv_isValue.getViewers().isEmpty())) {
				int pr = Func.potrzebneRzędy(punktacja.size());
				inv_isValue = panel_isValue.stwórz(this, pr <= 4 ? pr + 2 : pr, "§e§lWartość", Baza.pustySlotCzarny);
				
				HashSet<String> set = new HashSet<>();
				while (set.size() < 6*9 && set.size() < punktacja.size())
					set.add(Func.losuj(punktacja.keySet()));
				
				List<ItemStack> itemy = new ArrayList<>();
				for (String mat : set) {
					int posiadane = punkty.getOrDefault(mat, 0);
					double pkt = punktacja.get(mat);
					ItemStack ikona = Func.stwórzItem(Func.StringToEnum(Material.class, mat), "§b" + mat,
							"§6Wartość§7: §e" + Func.DoubleToString(pkt) + "§6pkt",
							"§6Posiadane§7: §e" + posiadane,
							"§6Razem§7: §e" + Func.DoubleToString(posiadane * pkt)
							);
					Func.insort(ikona, itemy, item -> Func.stringToDouble(item.getType().name()));
				}
				
				for (int slot : Func.sloty(itemy.size(), pr))
					inv_isValue.setItem(pr <= 4 ? slot + 9 : slot, itemy.remove(0));
			}
			return inv_isValue;
		}
		void policzWartość(Player p) {
			double pkt = dodatkowe_pkt;
			for (Entry<String, Integer> entry : punkty.entrySet())
				pkt += punktacja.getOrDefault(entry.getKey(), 0d) * entry.getValue();


			double nowe = new PrzeliczaniePunktówWyspyEvent(this, p, this.pkt, pkt).pktPo;
			if (nowe != this.pkt) {
				this.pkt = nowe;
				sprawdzTop();
			}
			
			this.pkt = nowe;
			zapisz();
		}
		public double getPkt() {
			return pkt;
		}
		
		// stare /is value
/*
		private Runnable pkt_taskNaKoniec;
		private Player pkt_p;
		private long pkt_start;
		private volatile int oczekujące;
		private volatile double pkt_policzone;
		public boolean wartość(Player p) throws MsgCmdError {
			checker.checkFormat(permisje(p).liczenie_wartości_wyspy, "Nie masz uprawnień do przeliczania wartości wyspy");
			checker.checkFormat(oczekujące <= 0, "Wartość jest aktualnie liczona");

			String strId = String.valueOf(id);

			if (maBypass(p) || ostatnieLiczenie.minąłToUstaw(strId)) {
				p.sendMessage(prefix + "Liczenie wartości wyspy");
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> policzWartość(p, () -> {
					Main.log(prefix + Func.msg("Wartość wyspy przeliczanej przez %s to %s", Func.getDisplayName(p), Func.IntToString((int) pkt)));
					if (Gracz.wczytaj(p).wyspa != id)
						p.sendMessage(prefix + Func.msg("Aktualna wartość ich wyspy: %s", pkt));
					członkowie.keySet().forEach(
							nick -> Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), gracz -> 
							gracz.sendMessage(prefix + Func.msg("Aktualna wartość twojej wyspy: %s", Func.IntToString((int) pkt)))));
				}));
			} else
				p.sendMessage(prefix + Func.msg("musisz jeszcze poczekać %s żeby ponownie przeliczyć wartość wyspy", ostatnieLiczenie.czas(strId)));
			return false;

		}
		public void policzWartość(Player p, Runnable taskNaKoniec) {
			int dostępne = Main.ust.wczytajInt("SkyBlock.isvalue.maxWątki");
			if (dostępne <= 0)
				dostępne = Runtime.getRuntime().availableProcessors();
			
			final int overworld, nether;
			
			if (Światy.dozwolonyNether) {
				int pół = dostępne / 2;
				nether = Math.max(1, pół);
				overworld = dostępne - pół;
			} else {
				nether = 0;
				overworld = dostępne;
			}
			
			pkt_p = p;
			pkt_policzone = 0;
			pkt_taskNaKoniec = taskNaKoniec;
			oczekujące = nether + overworld;
			
			pkt_start = System.currentTimeMillis();
			Main.log(prefix + "Liczenie wartości wyspy (id: " + id + ") przeliczanej przez " + Func.getDisplayName(p) + " na " + dostępne + " wątkach");

			policz(Światy.nether, nether);
			policz(Światy.overworld, overworld);
		}
		private void policz(World świat, int wątki) {
			if (wątki <= 0)
				return;
			
			Krotka<Location, Location> rogi = rogi();
			Location min = rogi.a;
			Location max = rogi.b;

			min.setWorld(świat);
			max.setWorld(świat);
			
			int y = rogi.b.getBlockY() - rogi.a.getBlockY();
			
			int sy = y / wątki;
			
			while (wątki-- > 1) {
				Location smin = min.clone();
				Location smax = max.clone();
				
				smax.setY(smin.getY() + sy);
				min.setY(smax.getY() + 1);
				
				policz(smin, smax);
			}
			policz(min, max);
		}

		private void policz(Location róg1, Location róg2) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				double pkt = 0;
				for (Block blok : Func.bloki(róg1, róg2))
					pkt += blok.getType() == Material.AIR ? punktacja_AIR : punktacja.getOrDefault(blok.getType(), 0d);
				pkt_policzone += pkt;
				skończoneLiczenieWątku();
			});
		}
		// Async
		private void skończoneLiczenieWątku() {
			if (--oczekujące <= 0)
				Bukkit.getScheduler().runTask(Main.plugin, () -> {
					double nowe = new PrzeliczaniePunktówWyspyEvent(this, pkt_p, this.pkt, pkt_policzone + dodatkowe_pkt).pktPo;
					if (nowe != this.pkt) {
						this.pkt = nowe;
						sprawdzTop();
						zapisz();
					}
					Main.log(prefix + "Policzono wartość wyspy id: " + id + " w czasie " + ((System.currentTimeMillis() - pkt_start) / 1000d) + " sekund");
					Func.wykonajDlaNieNull(pkt_taskNaKoniec, Runnable::run);
					pkt_taskNaKoniec = null;
					pkt_p = null;
				});
		}
		
*/

		
		// /is invite /is join

		static final Cooldown cooldownZaproszeń = new Cooldown(45);
		private static final String metaZaproszenie = "mimiSkyblockZaproszenie";
		public void zaproś(Player p, Player kogo) throws MsgCmdError {
			checker.checkFormat(członkowie.size() < Ulepszenia.członkowie[poziomy.członkowie].wartość, "Wyspa osiągneła już limit członków");
			checker.checkFormat(permisje(p).zapraszanie_członków_i_coop, "Nie masz uprawnień na zapraszanie ludzi na wyspe");
			checker.checkFormat(cooldownZaproszeń.minął(p.getName() + kogo.getName()), "Musisz poczekać jeszcze %s zanim ponownie go zaprosisz",
					cooldownZaproszeń.czas(p.getName() + kogo.getName()));
			checker.checkFormat(Gracz.wczytaj(kogo).wyspa == -1, "%s ma już wyspę", Func.getDisplayName(kogo));

			Func.ustawMetadate(kogo, metaZaproszenie, p);

			Main.panelTakNie(kogo, "&4Zaproszenie na wyspe " + Func.getDisplayName(p),
					"&aDołącz do wyspy &7" + Func.getDisplayName(p),
					"&cOdrzuć zaproszenie do wyspy &7" + Func.getDisplayName(p), () -> przyjmijZaproszenie(kogo),
					() -> odrzućZaproszenie(kogo));

			inst.throwFormatMsg("Wysłano zaproszenie na wyspy do " + Func.getDisplayName(kogo));
		}
		boolean przyjmijZaproszenie(Player p) {
			if (członkowie.size() >= Ulepszenia.członkowie[poziomy.członkowie].wartość)
				return Func.powiadom(p, prefix + "Wyspa osiągneła już limit członków");

			if (new DołączanieDoWyspyEvent(this, p).isCancelled())
				return true;

			członkowie.put(p.getName(), "członek");
			zapisz();

			Gracz g = Gracz.wczytaj(p);
			g.wyspa = id;
			g.zapisz();

			powiadomCzłonków("%s dołączył wyspy", p.getName());

			p.removeMetadata(metaZaproszenie, Main.plugin);

			odświeżInvMembers();
			return false;
		}
		void odrzućZaproszenie(Player p) {
			Player zapraszający = (Player) p.getMetadata(metaZaproszenie).get(0).value();

			Func.powiadom(prefix, p, "Odrzuciłeś zaproszenie do wyspy %s", Func.getDisplayName(zapraszający));
			Func.powiadom(prefix, p, "%s odrzucił twoje zaproszenie do wyspy", Func.getDisplayName(zapraszający));

			cooldownZaproszeń.ustaw(zapraszający.getName() + p.getName());

			p.removeMetadata(metaZaproszenie, Main.plugin);
		}

		
		// /is leave

		public void opuść(Player p) throws MsgCmdError {
			checker.checkFormat(!"właściciel".equals(członkowie.get(p.getName())), "Nie możesz opuścić własnej wyspy");

			if (new OpuszczanieWyspyEvent(this, p.getName()).isCancelled())
				return;

			powiadomCzłonków("%s opuścił wyspę", Func.getDisplayName(p));

			członkowie.remove(p.getName());
			zapisz();

			Gracz g = Gracz.wczytaj(p);
			g.wyspa = -1;
			g.zapisz();

			odświeżInvMembers();

			Func.tpSpawn(p);
		}

		
		// /is fav
		
		public void uwielb(Player p) {
			Gracz gracz = Gracz.wczytaj(p);
			
			String czynność;
			if (gracz.ulubioneWyspy.remove((Integer) id)) {
				czynność = "Usunięto";
			} else {
				czynność = "Dodano";
				gracz.ulubioneWyspy.add(id);
			}
			Func.powiadom(p, prefix + czynność + " wyspę %s do ulubionych", nazwa);
			
			gracz.zapisz();
		}
		
		// /is like
		
		@Mapowane List<String> lubiący;
		public void polub(Player p) {
			Gracz gracz = Gracz.wczytaj(p);
			
			if (lubiący.remove(p.getName())) {
				gracz.polubioneWyspy.remove(id);
				Func.powiadom(p, prefix + "Już nie lubisz wyspy %s", nazwa);
				powiadomCzłonków("%s przestał lubić twoją wyspę! masz teraz %s polubień", Func.getDisplayName(p), lubiący.size());
			} else {
				lubiący.add(p.getName());
				gracz.polubioneWyspy.add(id);
			}
			
			Func.powiadom(p, prefix + "Polubiłeś wyspę %s", nazwa);
			this.powiadomCzłonków("%s polubił twoją wyspę! Masz już %s polubień!", Func.getDisplayName(p), lubiący.size());

			gracz.zapisz();
			zapisz();
		}
		
		
		// /is visit

		public void odwiedz(Player p) {
			if (zbanowani.contains(p.getName()) && !maBypass(p)) {
				p.sendMessage(prefix + "Nie możesz odwiedzać tej wyspy");
				return;
			}
			if (członkowie.containsKey(p.getName()))
				tpHome(p);
			else if (maBypass(p))
				p.teleport(locHome);
			else if (prywatna)
				p.sendMessage(prefix + "Ta wyspa jest prywatna");
			else {
				p.teleport(locHome);
				powiadomCzłonków("%s Odwiedza twoją wyspę", Func.getDisplayName(p));
				p.sendMessage(prefix + Func.msg("Odwiedzasz wyspę %s", nazwa));
			}
		}

		
		// /is delete

		public void usuń(Player p) throws MsgCmdError {
			checker.checkFormat(permisje(p).grupa.equals("właściciel"), "Tylko właściciel może usunąć wyspe");

			Main.panelTakNie(p, "&4Usunąć wyspe?", "&aTak, usuń wyspę", "&4Nie, nie usuwał wyspy", this::usuń, null);
		}

		
		// /is kick

		public void wyrzuć(Player p, String kogo) throws MsgCmdError {
			checker.checkFormat(!p.getName().equalsIgnoreCase(kogo), "zamiast tego użyć /is opuść");
			if (!członkowie.containsKey(kogo)) {
				for (String nick : członkowie.keySet())
					if (nick.equalsIgnoreCase(kogo)) {
						kogo = nick;
						break;
					}
				if (!członkowie.containsKey(kogo)) {
					checker.checkFormat(permisje(p).wyrzucanie_banowanie_odwiedzających, "Nie masz uprawnień aby wyrzucać odwiedzających z wyspy");
					Player kogoP = checker.nieNullFormat(Bukkit.getPlayer(kogo), "%s nie jest online i nie należy do twojej wyspy", kogo);
					if (zawiera(kogoP.getLocation())) {
						checker.checkFormat(!maBypass(kogoP), "Nie możesz wyprosić tego gracza");
						Func.tpSpawn(kogoP);
						p.sendMessage(prefix + Func.msg("Wyprosiłeś %s ze swojej wyspy", Func.getDisplayName(kogoP)));
						kogoP.sendMessage(prefix + Func.msg("%s wyprosił cie ze swojej wyspy", Func.getDisplayName(p)));
					}
					return;
				}
			}
			checker.checkFormat(permisje(p).wyrzucanie_członków_i_uncoop, "Nie masz uprawnień aby wyrzucać członków z wyspy");
			checker.checkFormat(!członkowie.get(kogo).equals("właściciel"), "Nie możesz wyrzucić właściciela wyspy");
			
			if (new OpuszczanieWyspyEvent(this, kogo).isCancelled())
				return;

			powiadomCzłonków("%s wyrzucił %s z wyspy!", p.getName(), kogo);

			członkowie.remove(kogo);

			zapisz();

			Gracz g = Gracz.wczytaj(kogo);
			g.wyspa = -1;
			g.zapisz();

			odświeżInvMembers();

			Func.wykonajDlaNieNull(Bukkit.getPlayer(kogo), Func::tpSpawn);
		}

		
		// /is name

		@Mapowane String nazwa;
		public void zmieńNazwe(Player p, String nazwa) throws MsgCmdError {
			checker.checkFormat(permisje(p).zmiana_nazwy_wyspy, "Nie masz uprawnień do zmiany nazwy wyspy");
			this.nazwa = Func.koloruj(nazwa);
			p.sendMessage(prefix + Func.msg("Zmieniono nazwę wyspy na %s", this.nazwa));
			odświeżTopJeśliZawiera();
			zapisz();
		}
		
		
		// /is members

		public void otwórzMembers(Player p) {
			p.openInventory(getInvMembers(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvMembers(Player p) {
			Inventory inv = new Holder(this, TypInv.CZŁONKOWIE, Func.potrzebneRzędy(członkowie.size())).getInventory();

			Permisje perm = permisje(p);

			int i = 0;
			int ip = indexPerm(perm);
			String[] nicki = new String[członkowie.size()];
			for (String grupa : permsKody) {
				grupa = kodToStrPerm(grupa);
				int ig = indexPerm(grupa);
				for (Entry<String, String> en : członkowie.entrySet())
					if (grupa.equals(en.getValue())) {
						nicki[i] = en.getKey();
						ItemStack item = Func.stwórzItem(Material.PLAYER_HEAD, "&9&l" + en.getKey(),
								"&6Ranga: &a" + grupa);
						if (perm.awansowanie_i_degradowanie_członków && ig > ip && !grupa.equals("członek"))
							Func.dodajLore(item, "&8PPM aby degradować");
						if (perm.awansowanie_i_degradowanie_członków && ig - 1 > ip)
							Func.dodajLore(item, "&8LPM aby awansować");
						inv.setItem(i++, item);
					}
			}

			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				for (int j = 0; j < nicki.length; j++)
					if (inv.getViewers().isEmpty())
						return;
					else
						Func.ustawGłowe(Func.graczOffline(nicki[j]), inv.getItem(j));
			});

			return inv;
		}
		void klikanyInvMembers(Player p, ItemStack item, ClickType typ) {
			if (!item.getType().equals(Material.PLAYER_HEAD) || !Func.multiEquals(typ, ClickType.RIGHT, ClickType.LEFT)
					|| !permisje(p).awansowanie_i_degradowanie_członków)
				return;

			String nick2 = Func.getDisplayName(item.getItemMeta()).substring(4);
			if (p.getName().equals(nick2))
				return;

			Permisje permP = permisje(p);
			Permisje perm2 = permisje(nick2);

			int mn = typ == ClickType.LEFT ? -1 : 1;
			int indexPerm2 = indexPerm(perm2);
			if (indexPerm(permP) >= indexPerm2 + mn)
				return;

			String nowaRanga = Func.tnij(permsKody.get(indexPerm2 + mn), " ").get(0);
			if (nowaRanga.equals("odwiedzający"))
				return;

			członkowie.put(nick2, nowaRanga);
			odświeżInvMembers();
			zapisz();

			Func.wykonajDlaNieNull(Bukkit.getPlayer(nick2),
					p2 -> p2.sendMessage(prefix + Func.msg("%s %s cię do rangi %s!", Func.getDisplayName(p),
							mn == -1 ? "Awansował" : "Zdegradował", nowaRanga)));
			p.sendMessage(prefix + Func.msg("%s gracza %s do rangi %s!", mn == -1 ? "Awansowałeś" : "Zdegradowałeś",
					nick2, nowaRanga));
		}
		void odświeżInvMembers() {
			odświeżTopJeśliZawiera();
			for (Player p : Bukkit.getOnlinePlayers()) {
				Inventory inv = p.getOpenInventory().getTopInventory();
				Func.wykonajDlaNieNull(inv.getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this) && holder.typ == TypInv.CZŁONKOWIE)
						otwórzMembers(p);
				});
			}
		}

		
		// /is biome

		public boolean otwórzInvBiom(Player p) {
			if (!permisje(p).zmiana_biomu)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do zmiany biomu");
			if (!zawiera(p.getLocation()))
				return Func.powiadom(p, prefix + "Musisz być na wyspie aby tego użyć");
			p.openInventory(dajInvBiom(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			return false;
		}
		private Inventory dajInvBiom(Player p) {
			List<Biom> dostępne = Func.przefiltruj(biomy,
					biom -> biom.permisja == null || p.hasPermission(biom.permisja));
			Inventory inv = new Holder(this, TypInv.BIOM, Func.potrzebneRzędy(dostępne.size())).getInventory();

			int i = -1;
			for (Biom biom : dostępne)
				inv.setItem(++i, Func.customModelData(biom.ikona.clone(), i));

			return inv;
		}
		static Cooldown biomCooldown = new Cooldown(2);
		void klikanyInvBiom(Player p, ItemStack item) {
			if (item.isSimilar(Baza.pustySlotCzarny))
				return;
			try {
				Biom biom = biomy.get(item.getItemMeta().getCustomModelData());
				if ((biom.permisja == null || p.hasPermission(biom.permisja)) && Światy.należy(p.getWorld())) {
					if (biomCooldown.minąłToUstaw(p.getName()))
						zmieńBiom(p.getWorld(), biom.biom);
				}
			} catch (IndexOutOfBoundsException e) {
			}
		}
		public void zmieńBiom(World świat, Biome biom) {
			Krotka<Location, Location> rogi = rogi();
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin,
					() -> Func.wykonajNaBlokach(rogi.a, rogi.b, (x, y, z) -> {
						świat.setBiome(x, y, z, biom);
						return true;
					}));
		}

		
		// /is upgrade

		public void otwórzUlepszenia(Player p) {
			if (permisje(p).kupowianie_ulepszeń) {
				p.openInventory(dajInvUlepszenia());
				p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			} else
				p.sendMessage(prefix + "Nie masz uprawnień do kupowania ulepszeń wyspy");
		}
		Inventory dajInvUlepszenia() {
			Inventory inv = new Holder(this, TypInv.ULEPSZENIA, 4).getInventory();

			inv.setItem(12,
					dajItemekInvUlepszenia("członkowie", Material.PLAYER_HEAD, "Członkowie",
							"&aZwiększa limit członków wyspy", ile -> "&aAktualnie dostępne &3" + ile + " &aczłonków",
							ile -> "&aNastępny poziom: &3" + ile));

			inv.setItem(13,
					dajItemekInvUlepszenia("wielkość", Material.BARRIER, "Wielkość", "&aRozszerza granice wyspy",
							w -> "&aAktualna wielkość: &3" + w + "m", w -> "&aNastępny poziom: &3" + w + "m"));

			inv.setItem(14,
					dajItemekInvUlepszenia("generator", Material.DIAMOND_ORE, "Generator",
							"&aZwiększa drop z cobla i stone", lvl -> "&aAktualny poziom: &3" + lvl + "lvl",
							lvl -> "&aNastępny poziom: &3" + lvl + "lvl"));

			inv.setItem(21, dajItemekInvUlepszenia("limityBloków", Material.HOPPER, "Limity Bloków",
					"&aZwiększa limity bloków na wyspie",
					limit -> String.format("Aktualny limit: &3%s &aleji &3%s &aspawnerów", limit, limit / 4),
					limit -> String.format("Następny limit: &3%s &aleji &3%s &aspawnerów", limit, limit / 4)));

			inv.setItem(22,
					dajItemekInvUlepszenia("magazyn", Material.CHEST, "Magazyn", "&aZwiększa pojemność magazynu wyspy",
							rzędy -> "&aAktualna pojemność: &3" + (rzędy * 9) + "&a slotów",
							rzędy -> "&aNastępny poziom: &3" + (rzędy * 9) + "&a slotów"));

			inv.setItem(23,
					dajItemekInvUlepszenia("warpy", Material.END_PORTAL_FRAME, "Warpy", "&aZwiększa limit warpów wyspy",
							warpy -> "&aAktualna ilość warpów: &e" + warpy,
							warpy -> "&aNastępna ilość warpów: &3" + warpy));

			return inv;
		}
		private ItemStack dajItemekInvUlepszenia(String nazwaPola, Material mat, String nazwa, String krótkiOpis,
				Function<Integer, String> akt, Function<Integer, String> następny) {
			int poziom;
			try {
				poziom = Poziomy.class.getDeclaredField(nazwaPola).getInt(poziomy);
				Ulepszenia.Ulepszenie[] ulepszenia = (Ulepszenia.Ulepszenie[]) Ulepszenia.class
						.getDeclaredField(nazwaPola).get(null);
				ItemStack item = Func.stwórzItem(mat, "&9&l" + nazwa, "", "&a" + krótkiOpis,
						"&a" + akt.apply(ulepszenia[poziom].wartość));
				if (ulepszenia.length > poziom + 1) {
					Func.dodajLore(Func.dodajLore(item, "&a" + następny.apply(ulepszenia[poziom + 1].wartość)),
							"&aCena ulepszenia: &e" + Func.DoubleToString(ulepszenia[poziom + 1].cena.kasa) + "$");
					if (ulepszenia[poziom + 1].cena.walutaPremium != null && ulepszenia[poziom + 1].cena.walutaPremium > 0)
						Func.dodajLore(item, "§aPotrzebne " + Func.nazwaItemku(Baza.walutaPremium) + "§a: §e" + ulepszenia[poziom + 1].cena.walutaPremium);
				}
				return item;
			} catch (Throwable e) {
				e.printStackTrace();
				return new ItemStack(Material.CREEPER_HEAD);
			}
		}
		boolean klikanyInvUlepszenia(Player p, int slot) {
			if (!permisje(p).kupowianie_ulepszeń)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do kupowania ulepszeń wyspy");

			Func.wykonajDlaNieNull(((Supplier<String>) () -> {
				if (slot == 12) 		return "członkowie";
				else if (slot == 13) 	return "wielkość";
				else if (slot == 14) 	return "generator";
				else if (slot == 21) 	return "limityBloków";
				else if (slot == 22) 	return "magazyn";
				else if (slot == 23) 	return "warpy";
				return null;
			}).get(), nazwaPola -> {
				try {
					Ulepszenia.Ulepszenie[] ulepszenia = (Ulepszenia.Ulepszenie[]) Ulepszenia.class.getDeclaredField(nazwaPola).get(null);
					Field pole = Poziomy.class.getDeclaredField(nazwaPola);

					if (pole.getInt(poziomy) + 1 >= ulepszenia.length)
						return;
					double cena = ulepszenia[pole.getInt(poziomy) + 1].cena.kasa;

					double fundusz = Math.max(0, kasa);
					if (Main.econ != null) {
						double bal = Main.econ.getBalance(p);
						if (bal > 0)
							fundusz += bal;
					}

					if (cena > fundusz) {
						p.sendMessage(prefix + "Nie posiadasz wystarczająco dużo pieniędzy");
						return;
					}
					
					if (!ulepszenia[pole.getInt(poziomy) + 1].cena.zabierzPremium(p)) {
						p.sendMessage(prefix + "Nie posiadasz wystarczająco dużo " + 
								(Baza.walutaPremium.getItemMeta().hasDisplayName() ?
										Func.getDisplayName(Baza.walutaPremium.getItemMeta()) : Func.enumToString(Baza.walutaPremium.getType())));
						return;
					}
					
					

					kasa -= cena;
					if (kasa < 0) {
						Main.econ.withdrawPlayer(p, -kasa);
						kasa = 0;
					}

					pole.setInt(poziomy, pole.getInt(poziomy) + 1);

					zapisz();

					if (slot == 13)
						odświeżBorder();
					else if (slot == 14)
						odświeżGenerator();
					else if (slot == 22)
						odświeżMagazyn();

					powiadomCzłonków("%s zakupił ulepszenie %s", Func.getDisplayName(p), nazwaPola);
					
					Bukkit.getOnlinePlayers().forEach(gracz ->
						Func.wykonajDlaNieNull(gracz.getOpenInventory().getTopInventory().getHolder(),  Holder.class, holder -> {
							if (holder.typ == TypInv.ULEPSZENIA && holder.wyspa.equals(this))
								otwórzUlepszenia(gracz);
						}));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});

			return false;
		}

		
		// /is border

		@Mapowane Border border = Border.NIEBIESKI;
		public static enum Border {
			CZERWONY(Material.RED_STAINED_GLASS_PANE,
					(wb, w) -> wb.transitionSizeBetween(w, w - .1, Integer.MAX_VALUE)),
			ZIELONY(Material.LIME_STAINED_GLASS_PANE,
					(wb, w) -> wb.transitionSizeBetween(w - .1, w, Integer.MAX_VALUE)),
			BRAK(Material.WHITE_STAINED_GLASS_PANE, (wb, w) -> wb.setSize(Integer.MAX_VALUE)),
			NIEBIESKI(Material.LIGHT_BLUE_STAINED_GLASS_PANE, (wb, w) -> wb.setSize(w));

			public boolean dozwolony;
			final Material ikona;
			final BiConsumer<WorldBorder, Integer> ustawWielkość;

			Border(Material ikona, BiConsumer<WorldBorder, Integer> ustawWielkość) {
				this.ikona = ikona;
				this.ustawWielkość = ustawWielkość;
			}
		}
		public void otwórzInvBorder(Player p) {
			if (!permisje(p).zmiana_koloru_bordera)
				p.sendMessage(prefix + "Nie masz uprawnień na zmiane koloru bordera");
			else {
				p.openInventory(dajInvBorder());
				p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			}
		}
		private Inventory dajInvBorder() {
			Inventory inv = new Holder(this, TypInv.BORDER, 3).getInventory();

			List<Border> lista = Func.przefiltruj(Border.values(), kolor -> kolor.dozwolony);

			for (int i : Func.sloty(lista.size(), 1)) {
				Border kolor = lista.remove(0);
				inv.setItem(9 + i, Func.stwórzItem(kolor.ikona, "&9&l" + kolor.toString().toLowerCase()));
			}

			return inv;
		}
		void klikanyInvBorder(Player p, ItemStack item) {
			if (item.isSimilar(Baza.pustySlotCzarny))
				return;
			Border kolor = Border.valueOf(Func.getDisplayName(item.getItemMeta()).substring(4).toUpperCase());
			if (!kolor.dozwolony)
				return;
			border = kolor;
			zapisz();
			odświeżBorder();
		}
		public void ustawBorder(Player p) {
			Func.opóznij(1, () -> _ustawBorder(p));
		}
		private void _ustawBorder(Player p) {
			int wielkość = Ulepszenia.wielkość[poziomy.wielkość].wartość;

			double mn = wielkość % 2 == 0 ? 0 : .5;

			WorldBorder wb = new WorldBorder();
			wb.world = ((CraftWorld) p.getWorld()).getHandle();
			wb.setCenter(locŚrodek.getX() + mn, locŚrodek.getZ() + mn);

			border.ustawWielkość.accept(wb, wielkość);

			wb.setWarningDistance(0);
			wb.setWarningTime(0);

			ClientboundInitializeBorderPacket p1 = new ClientboundInitializeBorderPacket(wb);
			ClientboundSetBorderCenterPacket p2 = new ClientboundSetBorderCenterPacket(wb);
			ClientboundSetBorderSizePacket p3 = new ClientboundSetBorderSizePacket(wb);
			ClientboundSetBorderLerpSizePacket p4 = new ClientboundSetBorderLerpSizePacket(wb);
			
			NMS.wyślij(p, p1);
			NMS.wyślij(p, p2);
			NMS.wyślij(p, p3);
			NMS.wyślij(p, p4);
		}
		public void odświeżBorder() {
			Bukkit.getOnlinePlayers().forEach(p -> {
				if (zawiera(p.getLocation()))
					ustawBorder(p);
			});
		}

		
		// /is ban /is unban
		
		@Mapowane List<String> zbanowani;
		public void zbanuj(Player p, String kogo) throws MsgCmdError {
			checker.checkFormat(permisje(p).wyrzucanie_banowanie_odwiedzających, "Nie masz permisji na banowanie graczy");
			checker.checkFormat(!członkowie.containsKey(kogo), "Nie możesz zbanować członka wyspy");
			checker.checkFormat(!zbanowani.contains(kogo), "Ten gracz jest już zbanowany");
			Player kogoP = checker.nieNullFormat(Bukkit.getPlayer(kogo), "Ten gracz nie jest online");
			
			zbanowani.add(kogoP.getName());
			
			if (!maBypass(kogoP) && zawiera(kogoP.getLocation()))
				Func.tpSpawn(kogoP);
			
			kogoP.sendMessage(prefix + Func.msg("%s zbanował cię na swojej wyspie", Func.getDisplayName(p)));
			powiadomCzłonków("%s zbanował %s na wsypie", Func.getDisplayName(p), Func.getDisplayName(kogoP));
			
			zapisz();
		}
		public void odbanuj(Player p, String kogo) throws MsgCmdError {
			checker.checkFormat(permisje(p).wyrzucanie_banowanie_odwiedzających, "Nie masz permisji na banowanie graczy");
			checker.checkFormat(zbanowani.contains(kogo), "Ten gracz jest nie jest zbanowany");
			Player kogoP = checker.nieNullFormat(Bukkit.getPlayer(kogo), "Ten gracz nie jest online");
			
			zbanowani.remove(kogoP.getName());
			
			kogoP.sendMessage(prefix + Func.msg("%s odbanował cię na swojej wyspie", Func.getDisplayName(p)));
			powiadomCzłonków("%s odbanował %s na wsypie", Func.getDisplayName(p), Func.getDisplayName(kogoP));
			
			zapisz();
		}
		
		
		
		// /is top

		public static void otwórzTopke(Player p) {
			p.openInventory(dajInvTop());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private static Inventory invTop;
		private static Inventory dajInvTop() {
			if (invTop == null) {
				invTop = new Holder(null, TypInv.TOP, rzędyTopki).getInventory();
				odświeżInvTop();
			}
			return invTop;
		}
		private static BukkitTask taskInvTop;
		static void odświeżInvTop() {
			if (invTop == null)
				return;
			if (taskInvTop != null) {
				taskInvTop.cancel();
				taskInvTop = null;
			}
			for (int i = 0; i < slotyTopki.size() && i < topInfo.size(); i++) {
				TopInfo top = topInfo.get(i);
				invTop.setItem(slotyTopki.get(i), Func.stwórzItem(Material.PLAYER_HEAD, "&1&l" + top.nazwa, top.opis));
			}
			configData.ustaw_zapisz("topka.gracze", topInfo);
			taskInvTop = Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				for (int i = 0; i < slotyTopki.size() && i < topInfo.size(); i++) {
					TopInfo top = topInfo.get(i);
					int slot = slotyTopki.get(i);
					try {
						invTop.setItem(slot, Func.ustawGłowe(Func.graczOffline(top.nick), invTop.getItem(slot)));
					} catch (Throwable e) {}
				}
				taskInvTop = null;
			});
		}
		static void klikanyInvTop(Player p, int slot) {
			for (int i = 0; i < slotyTopki.size() && i < topInfo.size(); i++)
				if (slotyTopki.get(i) == slot) {
					Wyspa.wczytaj(topInfo.get(i).idWyspy).odwiedz(p);
					break;
				}
		}
		TopInfo stwórzTopInfo() {
			TopInfo top = Func.utwórz(TopInfo.class);

			top.pkt = pkt;
			top.nazwa = nazwa;
			for (Entry<String, String> en : członkowie.entrySet())
				if (en.getValue().equals("właściciel")) {
					top.nick = en.getKey();
					break;
				}
			top.idWyspy = id;
			List<String> opis = new ArrayList<>();
			opis.add("&aPunkty: &e" + Func.IntToString((int) pkt) + "pkt");
			opis.add("");
			String info = infoCzłonkowie().toString();
			Func.tnij(info, "\n").forEach(opis::add);
			top.opis = Func.koloruj(opis);
			return top;
		}
		void odświeżTopJeśliZawiera() {
			for (int i = 0; i < slotyTopki.size() && i < topInfo.size(); i++)
				if (topInfo.get(i).idWyspy == id) {
					odświeżInvTop();
					break;
				}
		}
		void sprawdzTop() {
			int i = -1;
			boolean dodawać = true;
			while (++i < topInfo.size()) {
				TopInfo top = topInfo.get(i);
				if (dodawać && top.pkt < pkt) {
					topInfo.add(i++, stwórzTopInfo());
					dodawać = false;
				}
				if (top.idWyspy == id)
					topInfo.remove(i--);
			}
			while (topInfo.size() > (int) (slotyTopki.size() * 1.5))
				topInfo.remove(topInfo.size() - 1);
			if (!dodawać)
				odświeżTopJeśliZawiera();
			else if (topInfo.size() < (int) (slotyTopki.size() * 1.5)) {
				topInfo.add(stwórzTopInfo());
				odświeżTopJeśliZawiera();
			}
		}
		void usuńZTop() {
			for (int i = 0; i < slotyTopki.size() && i < topInfo.size(); i++)
				if (topInfo.get(i).idWyspy == id) {
					topInfo.remove(i);
					odświeżInvTop();
					break;
				}

		}

		
		// /is create

		private static final Cooldown cooldownTworzenia = new Cooldown(60 * 60);
		public static void podejmijDecyzjeTworzeniaWyspy(Player p) throws MsgCmdError {
			checker.checkFormat(Gracz.wczytaj(p).wyspa == -1, "Masz już wyspę");
			checker.checkFormat(!(!maBypass(p) && !cooldownTworzenia.minął(p.getName())), "Musisz poczekać jeszcze " + cooldownTworzenia.czas(p.getName()) + " zanim stworzysz kojeną wyspę");
			Func.wykonajDlaNieNull(dajPanelTworzeniaWyspy(p), inv -> {
				p.openInventory(inv);
				p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			});
		}
		static Inventory dajPanelTworzeniaWyspy(Player p) {
			List<Entry<String, TypWyspy>> lista = Lists.newArrayList();
			for (Entry<String, TypWyspy> en : TypWyspy.mapa.entrySet())
				if (en.getValue().istniejąSchematy())
					lista.add(en);
			if (lista.isEmpty()) {
				TypWyspy.wrzućDomyślneSchematicki();
				lista = Lists.newArrayList(TypWyspy.mapa.entrySet());
			}
			if (lista.size() == 1) {
				utwórzWyspę(p, lista.get(0).getValue());
				return null;
			}

			int potrzebne = Func.potrzebneRzędy(lista.size());
			Inventory inv = new Holder(null, TypInv.TWORZENIE_WYSPY, potrzebne <= 4 ? potrzebne + 2 : potrzebne).getInventory();

			for (int i : Func.sloty(TypWyspy.mapa.size(), potrzebne)) {
				Entry<String, TypWyspy> en = lista.remove(0);
				inv.setItem((potrzebne <= 4 ? 9 : 0) + i,
						Func.stwórzItem(en.getValue().ikona, "&9&l" + en.getKey(), en.getValue().opis));
			}

			return inv;
		}
		static void klikanyPanelTworzeniaWyspy(Player p, ItemStack item) {
			if (item.isSimilar(Baza.pustySlotCzarny))
				return;
			utwórzWyspę(p, TypWyspy.zNazwy(Func.getDisplayName(item.getItemMeta()).substring(4)));
		}
		static void utwórzWyspę(Player p, TypWyspy typ) {
			if (!maBypass(p) && !cooldownTworzenia.minąłToUstaw(p.getName())) {
				Func.powiadom(p, prefix + "Musisz poczekać jeszcze %s zanim stworzysz kojeną wyspę", cooldownTworzenia.czas(p.getName()));
				return;
			}
			p.closeInventory();
			new Wyspa(p, typ);
		}
		
		
		// /is

		private static enum PanelIsItemy {
			TOP(
					null,
					Func.stwórzItem(Material.NETHERITE_INGOT, "&aNajlepsi Gracze"),
					(w, p) -> Wyspa.otwórzTopke(p)
					),
			INFO(
					null,
					Func.stwórzItem(Material.CONDUIT, "&2Informacje o wyspie"),
					Wyspa::info
					),
			/*VISIT(
					null,
					Func.stwórzItem(Material.ENDER_PEARL, "&2Odwiedz wyspe gracza"),
					Wyspa::odwiedz
					),*/
			LEAVE(
					(p, w) -> w != null,
					Func.stwórzItem(Material.OAK_DOOR, "&2Opuść wyspe"),
					Wyspa::opuść
					),
			HOME(
					(p, w) -> w != null,
					Func.stwórzItem(Material.RED_BED, "&2Teleport na wyspe"),
					Wyspa::tpHome
					),
			UPGRADE(
					(p, w) -> p.kupowianie_ulepszeń,
					Func.stwórzItem(Material.DIAMOND, "&2Ulepszenia wyspy"),
					Wyspa::otwórzUlepszenia
					),
			DROP(
					(p, w) -> p.zmiana_dropu,
					Func.stwórzItem(Material.DIAMOND_ORE, "&2Edytuj drop na wyspie"),
					Wyspa::otwórzDrop
					),
			BANK(
					(p, w) -> p.dostęp_do_expa_banku || p.dostęp_do_kasy_banku || p.dostęp_do_magazynu,
					Func.stwórzItem(Material.GOLD_INGOT, "&2Bank wyspy"),
					Wyspa::otwórzBank
					),
			BIOM(
					(p, w) -> p.zmiana_biomu,
					Func.stwórzItem(Material.SPONGE, "&2Zmień biom wyspy"),
					Wyspa::otwórzInvBiom
					),
			/*KICK(
					(p, w) -> p.wyrzucanie_członków || p.wyrzucanie_odwiedzających,
					Func.stwórzItem(Material.IRON_BOOTS, "&2Wyrzuć/wyproś gracza z wyspy")
					),*/
			BORDER(
					(p, w) -> p.zmiana_koloru_bordera,
					Func.stwórzItem(Material.LIGHT_BLUE_DYE, "&2Ustaw kolor bordera"),
					Wyspa::otwórzInvBorder
					),
			VALUE(
					(p, w) -> p.liczenie_wartości_wyspy,
					Func.stwórzItem(Material.BEACON, "&2Policz wartość wyspy"),
					Wyspa::wartość
					),
			MEMBERS(
					(p, w) -> p.awansowanie_i_degradowanie_członków,
					Func.stwórzItem(Material.PLAYER_HEAD, "&2Zarządzaj członkami"),
					Wyspa::otwórzMembers
					),
			SETHOME(
					(p, w) -> p.ustawianie_home_wyspy,
					Func.stwórzItem(Material.LIME_BED, "&2Ustaw home wyspy"),
					Wyspa::ustawHome
					),
			/*INVITE(
					(p, w) -> p.zapraszanie_członków,
					Func.stwórzItem(Material.DIAMOND_PICKAXE, "&2Zaproś gracza na wyspe")
					),*/
			PERMISSIONS(
					(p, w) -> p.edytowanie_permisji,
					Func.stwórzItem(Material.BARRIER, "&2Zarządzaj uprawnieniami"),
					Wyspa::permisjeInv
					),
			PERMEDIT(
					(p, w) -> p.edytowanie_hierarchii_grup_permisji,
					Func.stwórzItem(Material.BARRIER, "&2Zarządzaj hierarchią uprawnieniwń"),
					(w, p) -> w.edytorPermisji(p, new String[]{})
					),
			PRIVATE(
					(p, w) -> p.zmiana_prywatności && !w.prywatna,
					Func.stwórzItem(Material.ACACIA_FENCE_GATE, "&2Ustaw wyspę publiczną", "&bUmożliwia innym graczom odwiedzanie wyspy"),
					Wyspa::ustawPrywatna
					),
			PUBLIC(
					(p, w) -> p.zmiana_prywatności && w.prywatna,
					Func.stwórzItem(Material.IRON_DOOR, "&2Ustaw wyspę prywatną", "&bUniemożliwia innym graczom odwiedzanie twojej wyspy"),
					Wyspa::ustawPubliczna
					),
			WARPS((p, w) -> {
				for (boolean może : p.warpy)
					if (może)
						return true;
				return false;
			}, Func.stwórzItem(Material.ENDER_EYE, "&2Warpy wyspy", "&bAby dodać warp", "&bużyj &9/is addwarp <nazwa>"),
				Wyspa::otwórzWarpy
				);
			
			
			ItemStack item;
			private final BiPredicate<Permisje, Wyspa> warunek;
			private final BiConsumer<Wyspa, Player> wykonanie;
			PanelIsItemy(BiPredicate<Permisje, Wyspa> warunek, ItemStack domyślnyItem, BiConsumer<Wyspa, Player> wykonanie) {
				this.wykonanie = wykonanie;
				this.item = domyślnyItem;
				this.warunek = warunek;
			}
			
			boolean spełniony(Permisje perm, Wyspa wyspa) {
				try {
					return warunek == null || warunek.test(perm, wyspa);
				} catch (NullPointerException e) {
					return false;
				}
			}
			
			void wykonaj(Wyspa wyspa, Player p) {
				wykonanie.accept(wyspa, p);
			}
			
			@Override
			public String toString() {
				return name().toLowerCase();
			}
		}	
		public void otwórzPanelIs(Player p) {
			p.openInventory(dajPanelIs(permisje(p)));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory dajPanelIs(Permisje perm) {
			Inventory inv = new Holder(this, TypInv.GŁÓWNY_PANEL, 6).getInventory();
			
			List<ItemStack> lista = Lists.newArrayList();
			for (PanelIsItemy item : PanelIsItemy.values())
				if (item.spełniony(perm, this))
					lista.add(item.item);
			
			int[] sloty = Func.sloty(lista.size(), 4);
			
			int i = 0;
			while (!lista.isEmpty())
				inv.setItem(9 + sloty[i++], lista.remove(0));
			
			
			return inv;
		}
		void klikanyPanelIs(Player p, ItemStack item) {
			if (item.isSimilar(Baza.pustySlotCzarny))
				return;
			for (PanelIsItemy isItem : PanelIsItemy.values())
				if (item.isSimilar(isItem.item)) {
					isItem.wykonaj(this, p);
					break;
				}
		}

		
		
		// ogólne odniesienia

		public void zniszczony(BlockBreakEvent ev) {
			Material typ = ev.getBlock().getType();
			zniszczony(typ);
			generator(typ, ev);
		}

		
		// zapis

		private boolean zapisano = false;
		private boolean doZapisania = false;
		public void zapisz() {
			doZapisania = true;
			if (!zapisano) {
				zapiszNatychmiast();
				zapisano = true;
				doZapisania = false;
				Func.opóznij(20 * 60, () -> {
					zapisano = false;
					if (doZapisania)
						zapisz();
				});
			}
		}
		public void zapiszNatychmiast() {
			getConfig(id).ustaw_zapisz("wyspa", this);
		}

		
		// util
		
		void powiadomCzłonków(String msg, Object... uzupełnienia) {
			msg = Func.msg(msg, uzupełnienia);
			if (!msg.startsWith(prefix))
				msg = prefix + msg;
			String _msg = msg;
			członkowie.keySet()
					.forEach(nick -> Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(_msg)));
		}

		Permisje kodToPerm(int i) {
			return kodToPerm(permsKody.get(i));
		}
		Permisje kodToPerm(String kod) {
			return perms.get(kodToStrPerm(kod));
		}
		String kodToStrPerm(int i) {
			return kodToStrPerm(permsKody.get(i));
		}
		String kodToStrPerm(String kod) {
			return kod.substring(0, kod.indexOf(' '));
		}
		
		// im niższy index tym ważniejsza ranga
		int indexPerm(Permisje perm) {
			return indexPerm(perm.grupa);
		}
		int indexPerm(String perm) {
			for (int i = 0; i < permsKody.size(); i++)
				if (kodToStrPerm(i).equals(perm))
					return i;
			throw new Error("Permisja " + perm + " nie została odnaleziona");
		}

		// (min, max)
		public Krotka<Location, Location> rogi() {
			double a1 = Ulepszenia.wielkość[poziomy.wielkość].wartość / 2.0;
			// double a2 = odstęp % 2 != 0 ? a1 - 1 : a1;
			double a2 = a1 + (15d / 16d);
			return new Krotka<>(locŚrodek.clone().add(-a1, -locŚrodek.getY(),	  -a1),
								locŚrodek.clone().add(a2,  256 - locŚrodek.getY(), a2));
		}

		
		// Override

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;

			return obj instanceof Wyspa && ((Wyspa) obj).id == this.id;
		}
	}
	static enum TypInv {
		MAGAZYN((wyspa, p, slot, item, ev) -> {}),
		GŁÓWNY_PANEL((wyspa, p, sllot, item, ev) -> wyspa.klikanyPanelIs(p, item)),
		TOP((wyspa, p, slot, item, ev) -> Wyspa.klikanyInvTop(p, slot)),
		BIOM((wyspa, p, slot, item, ev) -> wyspa.klikanyInvBiom(p, item)),
		BORDER((wyspa, p, slot, item, ev) -> wyspa.klikanyInvBorder(p, item)),
		PERMISJE_MAIN((wyspa, p, slot, item, ev) -> wyspa.klikanyInvPermisje(p, slot)),
		ULEPSZENIA((wyspa, p, slot, item, ev) -> wyspa.klikanyInvUlepszenia(p, slot)),
		DROP_NETHER((wyspa, p, slot, item, ev) -> wyspa.klikanyInvDropNether(p, item)),
		WARPY((wyspa, p, slot, item, ev) -> wyspa.klikanyInvWarp(p, slot, item)),
		DROP_OVERWORLD((wyspa, p, slot, item, ev) -> wyspa.klikanyInvDropOverworld(p, item)),
		DROP((wyspa, p, slot, item, ev) -> wyspa.klikanyInvDrop(p, item.getType())),
		BANK((wyspa, p, slot, item, ev) -> wyspa.klikanyBank(p, slot, ev.getClick())),
		TWORZENIE_WYSPY((wyspa, p, slot, item, ev) -> Wyspa.klikanyPanelTworzeniaWyspy(p, item)),
		CZŁONKOWIE((wyspa, p, slot, item, ev) -> wyspa.klikanyInvMembers(p, item, ev.getClick())),
		DEL_WARP((wyspa, p, slot, item, ev) -> wyspa.klikaniyInvDelWarp(p, slot, item.getType())),
		PERMISJE((wyspa, p, slot, item, ev) -> wyspa.klikanyPermisjeEdytujInv(p, slot, Func.getTitle(ev.getView()), item));

		private static interface TypInvConsumer {
			void wykonaj(Wyspa wyspa, Player p, int slot, ItemStack item, InventoryClickEvent ev);
		}

		final TypInvConsumer cons;

		TypInv(TypInvConsumer cons) {
			this.cons = cons;
		}
	}
	
	static SkyBlock inst;
	public SkyBlock() {
		super("is");
		inst = this;
		checker = new CmdChecker(this);
		Main.dodajPermisje(permBypass);
	}

	
	// Generowanie pustych Światów

	private static final GeneratorChunków generatorChunków = new GeneratorChunków();
	public static GeneratorChunków worldGenerator(String worldName) {
		return Światy.należy(worldName) ? generatorChunków : null;
	}
	public static class GeneratorChunków extends ChunkGenerator {
		@Override
		public List<BlockPopulator> getDefaultPopulators(World world) {
			return Lists.newArrayList();
		}

		@Override
		public boolean canSpawn(World world, int x, int z) {
			return true;
		}

		@Override
		public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biomeGrid) {
			ChunkData chunkData = createChunkData(world);
			
			Biome biom;
			if (world.getName().equals(Światy.nazwaOverworld))
				biom = Biome.PLAINS;
			else if (Światy.dozwolonyNether && world.getName().equals(Światy.nazwaNether))
				biom = Biome.NETHER_WASTES;
			else
				return chunkData;

			for (int x = 0; x < 16; x++)
				for (int z = 0; z < 16; z++)
					for (int y = 0; y < world.getMaxHeight(); y++)
						biomeGrid.setBiome(x, y, z, biom);

			return chunkData;
		}

		public byte[][] blockSections;

		public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
			if (blockSections == null)
				blockSections = new byte[world.getMaxHeight() / 16][];
			return blockSections;
		}
	}
	private World stwórzŚwiat(Environment env, String nazwa) {
		WorldCreator wc = new WorldCreator(nazwa);
		wc.generator(Main.plugin.getName() + ":skyblock");
		wc.generator(generatorChunków);
		wc.generateStructures(false);
		wc.type(WorldType.NORMAL);
		wc.environment(env);
		World w = wc.createWorld();

		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null)
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + nazwa + " " + env + " -g " + Main.plugin.getName() + ":skyblock");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set generator " + Main.plugin.getName() + ":skyblock " + nazwa);
			});

		return w;
	}
	private void stwórzŚwiaty() {
		Światy.overworld = stwórzŚwiat(Environment.NORMAL, Światy.nazwaOverworld);
		if (Światy.dozwolonyNether)
			Światy.nether = stwórzŚwiat(Environment.NETHER, Światy.nazwaNether);
	}

	
	// bypass

	private static final Set<String> zBypassem = Sets.newConcurrentHashSet();
	public static boolean maBypass(Player p) {
		return zBypassem.contains(p.getName());
	}
	public static void ustawBypass(Player p, boolean stan) {
		Consumer<String> cons = (stan ? zBypassem::add : zBypassem::remove);
		cons.accept(p.getName());
	}

	
	// Event Handler

	/// obsługa inv
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			if (ev.getRawSlot() >= 0 && ev.getRawSlot() < ev.getInventory().getSize())
				holder.typ.cons.wykonaj(holder.wyspa, (Player) ev.getWhoClicked(), ev.getRawSlot(), ev.getCurrentItem(),
						ev);
		});
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			if (holder.typ == TypInv.MAGAZYN)
				holder.wyspa.zamknięcieMagazynu();
		});
	}

	/// permisje
	@EventHandler
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (ev.getEntity() instanceof Player)
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getItem().getLocation()), wyspa -> {
				if (!wyspa.permisje((Player) ev.getEntity()).podnoszenie_itemów)
					ev.setCancelled(true);
			});
	}
	@EventHandler
	public void interakjca(PlayerInteractEvent ev) {
		Func.wykonajDlaNieNull(ev.getClickedBlock(),
				blok -> Func.wykonajDlaNieNull(Wyspa.wczytaj(blok.getLocation()), wyspa -> {
					if (blok.getState() instanceof Container) {
						if (!wyspa.permisje(ev.getPlayer()).otwieranie_skrzyń)
							ev.setCancelled(true);
					} else if (blok.getType().isInteractable())
						if (blok.getType().equals(Material.LEVER) || blok.getType().toString().contains("_BUTTON")) {
							if (!wyspa.permisje(ev.getPlayer()).używanie_przycisków_dzwigni)
								ev.setCancelled(true);
						} else if (!wyspa.permisje(ev.getPlayer()).otwieranie_drzwi_i_furtek)
							ev.setCancelled(true);
				}));
	}
	@EventHandler
	public void bicieMobów(EntityDamageByEntityEvent ev) {
		Predicate<Entity> gracz = e -> e instanceof Player ||
				(e instanceof Projectile && ((Projectile) e).getShooter() != null && ((Projectile) e).getShooter() instanceof Player);
		
		boolean dp = gracz.test(ev.getDamager());
		boolean ep = gracz.test(ev.getEntity());
		
		if ((dp || ep) && !(dp && ep))
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getDamager().getLocation()), wyspa -> {
				if (!wyspa.permisje(
						dp ?
						((Player) (ev.getDamager() instanceof Player ? ev.getDamager() : ((Projectile) ev.getDamager()).getShooter()))
						:
						(Player) ev.getEntity()).bicie_mobów
						)
					ev.setCancelled(true);
			});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieItemFramów(PlayerInteractEntityEvent ev) {
		if (ev.getRightClicked().getType() == EntityType.ITEM_FRAME)
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getRightClicked().getLocation()), wyspa -> {
				if (!wyspa.permisje(ev.getPlayer()).używanie_armorstandów_itemframów)
					ev.setCancelled(true);
			});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieItemArmorStandów(PlayerArmorStandManipulateEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getRightClicked().getLocation()), wyspa -> {
			if (!wyspa.permisje(ev.getPlayer()).używanie_armorstandów_itemframów)
				ev.setCancelled(true);
		});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void deptanieUpraw(EntityChangeBlockEvent ev) {
		if (ev.getEntity() instanceof Player) {
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
				if (!wyspa.permisje((Player) ev.getEntity()).niszczenie)
					ev.setCancelled(true);
			});
		}
	}
	@EventHandler
	public void napełnianieWiadra(PlayerBucketFillEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
			if (!wyspa.permisje(ev.getPlayer()).niszczenie)
				ev.setCancelled(true);
		});
	}
	@EventHandler
	public void opróżnianieWiadra(PlayerBucketEmptyEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
			if (!wyspa.permisje(ev.getPlayer()).stawianie)
				ev.setCancelled(true);
		});
	}
	/// limityBloków / permisje
	@EventHandler(priority = EventPriority.HIGH)
	public void stawianieBloków(BlockPlaceEvent ev) {
		if (ev.isCancelled())
			return;
		if (Światy.należy(ev.getBlock().getWorld()))
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
				if (!wyspa.permisje(ev.getPlayer()).stawianie || wyspa.postawiony(ev.getPlayer(), ev.getBlock().getType()))
					ev.setCancelled(true);
			},
					() -> ev.setCancelled(!maBypass(ev.getPlayer())));
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void niszczenieBloków(BlockBreakEvent ev) {
		if (ev.isCancelled())
			return;
		if (Światy.należy(ev.getBlock().getWorld()))
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
				if (!wyspa.permisje(ev.getPlayer()).niszczenie)
					ev.setCancelled(true);
				else
					wyspa.zniszczony(ev);
			},
					() -> ev.setCancelled(!maBypass(ev.getPlayer())));
	}

	/// border
	@EventHandler(priority = EventPriority.MONITOR)
	public void dołączanieDoGry(PlayerJoinEvent ev) {
		Player p = ev.getPlayer();
		Func.wykonajDlaNieNull(Wyspa.wczytaj(p.getLocation()), wyspa -> wyspa.ustawBorder(p));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void tepanie(PlayerTeleportEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getTo()), wyspa -> wyspa.ustawBorder(ev.getPlayer()));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getRespawnLocation()), wyspa -> wyspa.ustawBorder(ev.getPlayer()));
	}
	@EventHandler
	public void wodaZaBorderem(BlockFromToEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> 
				ev.setCancelled(!wyspa.zawieraIgnorujŚwiat(ev.getToBlock().getLocation())));
	}
	@EventHandler
	public void pistony(BlockPistonExtendEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
			Krotka<Location, Location> rogi = wyspa.rogi();
			int mx = rogi.a.getBlockX() + 2;
			int mz = rogi.a.getBlockZ() + 2;
			int xx = rogi.b.getBlockX() - 2;
			int xz = rogi.b.getBlockZ() - 2;
			
			BiPredicate<Integer, Integer> bip = (x, z) -> x <= mx || x >= xx || z <= mz || z >= xz;

			Block tłok = ev.getBlock().getRelative(ev.getDirection());
			if (bip.test(tłok.getX(), tłok.getZ())) {
				ev.setCancelled(true);
				return;
			}
			
			for (Block blok : ev.getBlocks()) {
				if (bip.test(blok.getX(), blok.getZ())) {
					ev.setCancelled(true);
					return;
				}
			}
		});
	}

	
	/// spadanie do voida
	@EventHandler
	public void spadanieDoVoida(EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player && ev.getCause() == DamageCause.VOID && Światy.należy(ev.getEntity().getWorld())) {
			Player p = (Player) ev.getEntity();
			ev.setCancelled(true);

			Wyspa _wyspa = Wyspa.wczytaj(p.getLocation());
			if (_wyspa == null)
				_wyspa = Wyspa.wczytaj(p);
			
			Wyspa wyspa = _wyspa;
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				Main.chwilowyGodMode(p, 1);
				if (wyspa == null)
					Func.tpSpawn(p);
				else if (maBypass(p) || wyspa.członkowie.containsKey(p.getName()))
					wyspa.tpHome(p);
				else if (wyspa.prywatna)
					Func.tpSpawn(p);
				else
					wyspa.odwiedz(p);
			});
		}
	}
	
	/// generator / obsydnia w lawe
	Set<Location> obsydiany = Sets.newConcurrentHashSet();
	@EventHandler(priority = EventPriority.HIGH)
	public void generatorCoblaIStone(BlockFormEvent ev) {
		if (generator.isEmpty())
			return;
		
		Material przed = ev.getBlock().getType();
		Material po = ev.getNewState().getType();
		
		if (przed == Material.LAVA && po == Material.OBSIDIAN) {
			Location loc = ev.getBlock().getLocation();
			obsydiany.add(loc);
			Func.opóznij(20*60*20, () -> obsydiany.remove(loc));
			return;
		}
		
		boolean cobl;
		if (przed == Material.LAVA && po == Material.COBBLESTONE)
			cobl = true;
		else if (przed == Material.WATER && po == Material.STONE)
			cobl = false;
		else
			return;

		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> wyspa.generujRude(ev, cobl));
	}
	@EventHandler
	public void klikanieObsydianu(PlayerInteractEvent ev) {
		if (ev.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
			Func.wykonajDlaNieNull(ev.getClickedBlock(), blok -> {
				if (!(blok.getType() != Material.OBSIDIAN || !Światy.należy(blok.getLocation().getWorld())))
					Func.wykonajDlaNieNull(ev.getPlayer().getInventory().getItemInMainHand(), item -> {
						if (item.getType() == Material.BUCKET && obsydiany.remove(blok.getLocation())) {
							blok.setType(Material.AIR);
							item.setAmount(item.getAmount() - 1);
							ev.getPlayer().getInventory().setItemInMainHand(item);
							Func.dajItem(ev.getPlayer(), new ItemStack(Material.LAVA_BUCKET));
							ev.setCancelled(true);
						}
					});
			});
	}

	/// woda w netherze
	@EventHandler
	public void stwianieWody(PlayerBucketEmptyEvent ev) {
		if (Światy.dozwolonyNether && ev.getBlock().getWorld().getName().equals(Światy.nazwaNether)) {
			Block blok = ev.getBlock();
			Material mat = blok.getType();

			Consumer<Block> wykonaj = b -> b.setType(Material.WATER);
			Consumer<Block> undo = b -> b.setType(mat);

			if (!ev.getPlayer().isSneaking() && blok.getBlockData().getAsString().contains("waterlogged")) {
				String data = blok.getBlockData().getAsString();
				wykonaj = b -> b
						.setBlockData(Bukkit.createBlockData(data.replace("waterlogged=false", "waterlogged=true")));
				undo = b -> b.setBlockData(Bukkit.createBlockData(data));
			} else if (blok.getType().isSolid())
				blok = blok.getRelative(ev.getBlockFace());

			wykonaj.accept(blok);
			ItemStack item = ev.getItemStack();
			ItemStack itemWRęce = ev.getPlayer().getInventory().getItemInMainHand();
			BlockPlaceEvent nowyEvent = new BlockPlaceEvent(blok, blok.getState(),
					blok.getRelative(ev.getBlockFace().getOppositeFace()), item, ev.getPlayer(), false,
					itemWRęce.equals(item) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
			Bukkit.getPluginManager().callEvent(nowyEvent);
			if (nowyEvent.isCancelled())
				undo.accept(blok);
		}
	}

	/// nether portal
	@EventHandler(ignoreCancelled = true)
	public void netherPortal(PlayerPortalEvent ev) {
		if (ev.getCause() != TeleportCause.NETHER_PORTAL || !Światy.należy(ev.getFrom().getWorld()))
			return;

		if (!Światy.dozwolonyNether)
			ev.setCancelled(true);
		else
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getFrom()), wyspa -> {
				if (!wyspa.permisje(ev.getPlayer()).używanie_portalu) {
					ev.setCancelled(true);
					return;
				}
				ev.setSearchRadius(Ulepszenia.wielkość[wyspa.poziomy.wielkość].wartość / 2);
				ev.setCreationRadius(Ulepszenia.wielkość[wyspa.poziomy.wielkość].wartość / 3);
				ev.setCanCreatePortal(true);

				Location loc = wyspa.locŚrodek.clone();
				loc.setWorld(Światy.nazwaOverworld.equals(ev.getFrom().getWorld().getName()) ? Światy.nether
						: Światy.overworld);
				ev.setTo(loc);
				ev.setCancelled(false);
			}, () -> ev.setCancelled(true));
	}
	@EventHandler
	public void teleportowanie(PlayerTeleportEvent ev) {
		if (!maBypass(ev.getPlayer()))
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getTo()), wyspa -> {
				if (wyspa.zbanowani.contains(ev.getPlayer().getName()) && !wyspa.członkowie.containsKey(ev.getPlayer().getName()))
					ev.setCancelled(true);
			});
	}
	
	
	
	// onDisable

	public static void onDisable() {
		Wyspa.mapaWysp.values().forEach(w -> Func.wykonajDlaNieNull(w.get(), wyspa -> {
			if (wyspa.doZapisania)
				wyspa.zapiszNatychmiast();
		}));
	}

	
	// I/O

	static final Config configData = new Config("configi/SkyBlock Data");

	// [ ((overworld cobl, overworld stone), (nether cobl, nether stone)), ][lvl -1]
	static final List<MonoKrotka<MonoKrotka<Ciąg<Material>>>> generator = Lists.newArrayList();
	static final Map<String, Double> punktacja = new HashMap<>();
	static final Map<Material, Material> podmianaBloków = new EnumMap<>(Material.class);
	static double punktacja_AIR;
	static final List<Biom> biomy = Lists.newArrayList();
	static List<Integer> slotyTopki;
	static List<TopInfo> topInfo;
	static int rzędyTopki;
	static int odstęp;
	static int yWysp;

	
	// Następna pozycja wyspy

	public static class NastępnaPozycja extends Mapowany {
		@Mapowane int faza;
		@Mapowane int mx;
		@Mapowane int x;
		@Mapowane int y;
	}
	static Krotka<Integer, Integer> następnaPozycja() {
		NastępnaPozycja next = configData.wczytaj("następna", () -> Func.utwórz(NastępnaPozycja.class));
		Krotka<Integer, Integer> w = new Krotka<>(next.x, next.y);

		boolean r = false;

		switch (next.faza) {
		case 0: if (r = (--next.y < -next.mx)) next.y++; break;
		case 1: if (r = (--next.x < -next.mx)) next.x++; break;
		case 2: if (r = (++next.y >  next.mx)) next.y--; break;
		case 3:
			if (r = (++next.x > next.mx)) {
				next.x = next.y = ++next.mx;
				next.faza = -1;
			}
			break;
		}

		if (r)
			next.faza++;
		configData.ustaw_zapisz("następna", next);
		return r ? następnaPozycja() : w;
	}

	
	// Override

	private boolean poprawnieWczytany;
	
	@Override
	@SuppressWarnings("unchecked")
 	public void przeładuj() {
		poprawnieWczytany = false;
		final Config config = new Config("Skyblock");
		configData.przeładuj();

		// Dla bezpieczeństwa zamykanie wszystkich inv wysp, poza magazaynem
		Bukkit.getOnlinePlayers().forEach(p -> Func
				.wykonajDlaNieNull(p.getOpenInventory().getTopInventory().getHolder(), Holder.class, holder -> {
					if (holder.typ != TypInv.MAGAZYN)
						p.closeInventory();
				}));
		Wyspa.invTop = null;

		odstęp = config.wczytaj("odstęp między wyspami", 150);
		rzędyTopki = Math.max(1, Math.min(6, config.wczytaj("topka.rzędy", 4)));
		slotyTopki = Func.nieNull((List<Integer>) config.wczytaj("topka.sloty"));
		topInfo = Func.nieNull((List<TopInfo>) configData.wczytaj("topka.gracze"));
		yWysp = config.wczytaj("y wysp", 100);

		
		// PodmianaBloków
		podmianaBloków.clear();
		Func.wykonajDlaNieNull(config.sekcja("podmianaBloków"), sekcja -> sekcja.getValues(false).forEach((klucz, wartość) -> 
				podmianaBloków.put(Func.StringToEnum(Material.class, klucz), Func.StringToEnum(Material.class, (String) wartość))));
		
		
		// Punktacja
		punktacja.clear();
		Func.wykonajDlaNieNull(config.sekcja("punktacja"), sekcja -> sekcja.getValues(false).forEach((klucz, obj) -> {
			Material mat = Func.StringToEnum(Material.class, klucz);
			double pkt = Func.DoubleObj(obj);
			if (pkt != 0)
				punktacja.put(mat.name(), pkt);
		}));
		punktacja_AIR = punktacja.getOrDefault(Material.AIR.name(), 0d);

		// Generator
		generator.clear();
		Func.wykonajDlaNieNull((List<Map<String, Map<String, Integer>>>) config.wczytaj("generator"),
				lista -> lista.forEach(głównaMapa -> {
					Function<String, Ciąg<Material>> cons = klucz -> {
						Map<String, Integer> mapa = głównaMapa.get(klucz);

						Ciąg<Material> ciąg = new Ciąg<>();
						if (mapa != null)
							mapa.forEach((typ, szansa) -> {
								try {
									ciąg.dodaj(Func.StringToEnum(Material.class, typ), szansa);
								} catch (IllegalArgumentException e) {
									Main.warn("Niepoprawny blok w Skyblock.yml w generatorze: " + typ);
								}
							});

						return ciąg;
					};

					generator.add(new MonoKrotka<>(
							new MonoKrotka<>(cons.apply("overworld cobl"), cons.apply("overworld stone")),
							new MonoKrotka<>(cons.apply("nether cobl"), cons.apply("nether stone"))));
				}));

		// Biomy
		biomy.clear();
		// Biome: ikona permisja opis
		Func.wykonajDlaNieNull(config.sekcja("biom"), sekcja -> sekcja.getValues(false).forEach((nazwaBiomu, obj) -> {
			Biome biom = Func.StringToEnum(Biome.class, nazwaBiomu);
			Material ikona = Material.GRASS_BLOCK;
			List<String> opis = null;
			String perm = ".";

			String[] części = ((String) obj).split(" ");

			switch (części.length) {
			default:
				opis = Func.tnij(Func.listToString(części, 2), "\\n");
			case 2:
				perm = części[1];
			case 1:
				ikona = Func.StringToEnum(Material.class, części[0]);
			case 0:
			}
			perm = perm.equals(".") ? null : Func.permisja(perm);
			Func.wykonajDlaNieNull(perm, Main::dodajPermisje);

			if (biom == null)
				Main.warn("Skyblock Niepoprawny Biom " + nazwaBiomu);
			else if (ikona == null)
				Main.warn("Skyblock Niepoprawny typItemu " + części[0]);
			else
				biomy.add(new Biom(biom, ikona, perm, Func.koloruj(opis)));
		}));

		// Typy wysp
		TypWyspy.mapa.clear();
		// <nazwa typu>: scieżkaDoSchematickaOverworld scieżkaDoSchematickaNether dx dy +pkt
		// dz biomOverworld biomNether ikona opis
		Func.wykonajDlaNieNull(config.sekcja("typy wysp"), sekcja -> sekcja.getValues(false).forEach((typ, obj) -> {
			String części[] = ((String) obj).split(" ");
			Biome biomOverworld = Biome.PLAINS;
			Biome biomNether = Biome.NETHER_WASTES;
			Material ikona = Material.GRASS_BLOCK;
			List<String> opis = Lists.newArrayList(new String[] { "&8Zwyczajna wyspa" });
			double x = 0, y = 0, z = 0;
			double pkt = 0;
			switch (części.length) {
			default:
				opis = Func.tnij(Func.listToString(części, 9), "\\n");
			case 9:
				pkt = Func.Double(części[8]);
			case 8:
				ikona = Material.valueOf(części[7].toUpperCase());
			case 7:
				biomNether = Biome.valueOf(części[6].toUpperCase());
			case 6:
				biomOverworld = Biome.valueOf(części[5].toUpperCase());
			case 5:
				z = Func.Double(części[4]);
			case 4:
				y = Func.Double(części[3]);
			case 3:
				x = Func.Double(części[2]);
			case 2:
			}
			TypWyspy.mapa.put(typ,
					new TypWyspy(części[0], części[1], pkt, x, y, z, biomOverworld, biomNether, ikona, Func.koloruj(opis)));
		}));
		if (TypWyspy.mapa.isEmpty())
			TypWyspy.wrzućDomyślneSchematicki();

		// Światy
		Światy.dozwolonyNether = config.wczytaj("świat.dozwolony nether", true);
		Światy.nazwaOverworld = config.wczytaj("świat.zwykły", "mimiSkyblock");
		Światy.nazwaNether = config.wczytaj("świat.nether", "mimiSkyblockNether");
		stwórzŚwiaty();

		// Border
		for (Wyspa.Border kolor : Wyspa.Border.values())
			kolor.dozwolony = config.wczytaj("border." + kolor.toString().toLowerCase(), true);

		// Ulepszenia
		for (Field field : Ulepszenia.class.getDeclaredFields())
			try {
				List<String> linie = config.wczytajListe("ulepszenia." + field.getName());
				Ulepszenia.Ulepszenie[] tab = new Ulepszenia.Ulepszenie[linie.size()];

				for (int i = 0; i < tab.length; i++) {
					List<String> części = Func.tnij(linie.get(i), " ");
					tab[i] = new Ulepszenia.Ulepszenie(Func.Int(części.get(0)), new Cena(Func.Double(części.get(1)), null, części.size() >= 3 ? Func.Int(części.get(2)) : null));
				}

				field.set(null, tab);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				Main.warn("Nieprawidłowe Skyblock.yml \"ulepszenia." + field.getName() + "\"");
			}
		Ulepszenia.sprawdzSyntax();
		
		poprawnieWczytany = true;
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Skyblock", Func.koloruj(poprawnieWczytany ? "&aPoprawny" : "&cNiepoprawny"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 1) {
			if (args.length == 2)
				switch (args[0]) {
				case "uncoop":
					return Func.domyślnaTry(() -> {
						Wyspa wyspa = Wyspa.wczytaj((Player) sender);
						return utab(args, Func.wykonajWszystkim(wyspa.coop, x -> x.toString()));
					}, null);
				}
			return null;
		}
		if (!(sender instanceof Player))
			return utab(args, "info");

		List<String> lista = Lists.newArrayList();

		BiConsumer<Boolean, String> bic = (warunek, komenda) -> {
			if (warunek)
				lista.add(komenda);
		};

		Player p = (Player) sender;
		Wyspa wyspa = Wyspa.wczytaj(p);

		boolean pl = Gracz.wczytaj(p).język == Gracz.Język.POLSKI;

		lista.add("info");
		lista.add("top");
		lista.add("like");
		lista.add("fav");
		lista.add(pl ? "odwiedz" : "visit");
		if (p.hasPermission(permBypass)) {
			lista.add("bypass");
			lista.add("admin");
			lista.add("-p");
		}

		if (wyspa == null)
			lista.add(pl ? "stwórz" : "create");
		else {
			lista.add(pl ? "dom" : "home");
			lista.add(pl ? "opuść" : "leave");
			lista.add(pl ? "warpy" : "warps");
			Wyspa.Permisje perm = wyspa.permisje(p);
			bic.accept(perm.dostęp_do_expa_banku || perm.dostęp_do_kasy_banku || perm.dostęp_do_magazynu, "bank");
			bic.accept(perm.wyrzucanie_członków_i_uncoop || perm.wyrzucanie_banowanie_odwiedzających, pl ? "wyrzuć"  : "kick");
			bic.accept(perm.wyrzucanie_banowanie_odwiedzających, pl ? "zbanuj"  : "ban");
			bic.accept(perm.wyrzucanie_banowanie_odwiedzających, pl ? "odbanuj" : "unban");
			bic.accept(perm.edytowanie_permisji, pl ? "uprawnienia" : "permissions");
			bic.accept(perm.edytowanie_hierarchii_grup_permisji, pl ? "edytujpermisje" : "permsedit");
			bic.accept(perm.awansowanie_i_degradowanie_członków, pl ? "członkowie" : "members");
			bic.accept(perm.dodawanie_i_usuwanie_warpów, pl ? "usuńwarp" : "delwarp");
			bic.accept(perm.dodawanie_i_usuwanie_warpów, pl ? "dodajwarp" : "addwarp");
			bic.accept(perm.ustawianie_home_wyspy, pl ? "ustawdom" : "sethome");
			bic.accept(perm.kupowianie_ulepszeń, pl ? "ulepsz" : "upgrade");
			bic.accept(perm.dostęp_do_magazynu, pl ? "magazyn" : "storage");
			bic.accept(perm.zmiana_prywatności, pl ? "prywatna" : "private");
			bic.accept(perm.zmiana_prywatności, pl ? "publiczna" : "public");
			bic.accept(perm.zapraszanie_członków_i_coop, pl ? "zaproś" : "invite");
			bic.accept(perm.liczenie_wartości_wyspy, pl ? "wartość" : "value");
			bic.accept(perm.zmiana_biomu, pl ? "biom" : "biome");
			bic.accept(perm.zmiana_nazwy_wyspy, pl ? "nazwa" : "name");
			bic.accept(perm.zmiana_koloru_bordera, "border");
			bic.accept(perm.zapraszanie_członków_i_coop, "coop");
			bic.accept(perm.wyrzucanie_członków_i_uncoop, "uncoop");
			bic.accept(perm.zmiana_dropu, "drop");
			if (perm.grupa.equals("właściciel")) {
				lista.add(pl ? "przekaż" : "transfer");
				lista.add(pl ? "usuń" : "delete");
			}
		}

		return utab(args, lista);
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		Gracz g2;

		Wyspa wyspa;
		Player p = sender instanceof Player ? (Player) sender : null;

		// gracz nie musi mieć wyspy
		try {
			if (args.length > 0)
				switch (args[0].toLowerCase()) {
				case "fav":
				case "like":
					checker.checkFormat(args.length >= 2, "/is " + args[0].toLowerCase() + " <nick>");
					g2 = Gracz.wczytaj(args[1]);
					checker.checkFormat(g2.wyspa != -1, "%s nie posiada wyspy", g2.nick);
					
					if (args[0].equalsIgnoreCase("fav"))
						Wyspa.wczytaj(g2).uwielb(p);
					else
						Wyspa.wczytaj(g2).polub(p);
					
					return true;
				case "visit":
				case "odwiedz":
					checker.checkFormat(args.length >= 2, "/is visit <nick>");
					g2 = Gracz.wczytaj(args[1]);
					checker.checkFormat(g2.wyspa != -1, "%s nie posiada wyspy", g2.nick);
					Wyspa.wczytaj(g2).odwiedz(p);
					return true;
				case "admin":
					checker.checkFormat(sender.hasPermission(permBypass), "Nie masz uprawnień do tego");
					checker.checkFormat(args.length >= 2, "/is admin <nick>");
					checker.nieNullFormat(Wyspa.wczytaj(Gracz.wczytaj(args[1])), "%s nie ma wyspy", args[1]).otwórzPanelIs(p);
					return true;
				case "bypass":
					checker.checkFormat(sender.hasPermission(permBypass), "Nie masz uprawnień do tego");
					ustawBypass(p, !maBypass(p));
					return Func.powiadom(sender, prefix + (maBypass(p) ? "w" : "wy") + "łączono bypass");
				case "info":
				case "informacje":
					String sprawdzany = sender instanceof Player ? sender.getName() : null;
					if (args.length >= 2)
						sprawdzany = args[1];
					checker.checkFormat(sprawdzany != null, "/is info <nick>");
					checker.nieNullFormat(Wyspa.wczytaj(Gracz.wczytaj(sprawdzany)), "%s nie ma wyspy", sprawdzany).info(sender);
					return true;
				case "top":
					Wyspa.otwórzTopke(p);
					return true;
				case "create":
				case "stwórz":
					Wyspa.podejmijDecyzjeTworzeniaWyspy(p);
					return true;
				}

			Gracz g = null;
			if (sender.hasPermission(permBypass) && args.length >= 2 && args[0].equalsIgnoreCase("-p")) {
				g = Gracz.wczytaj(args[1]);
				String[] _args = new String[args.length - 2];
				for (int i=2; i < args.length; i++)
					_args[i-2] = args[i];
				args = _args;
			} else if (p != null)
				g = Gracz.wczytaj(p.getName());
			wyspa = g == null ? null : Wyspa.wczytaj(g.wyspa);
			
			if (g != null && g.wyspa == -1) {
				Wyspa.podejmijDecyzjeTworzeniaWyspy(p);
				return true;
			}
			
			checker.checkFormat(p != null, "Tylko gracz może zarządzać wyspą");
			checker.checkFormat(g.wyspa != -1, "Problem jest następujący: Brak wyspy");
			
			if (args.length == 0) {
				wyspa.otwórzPanelIs(p);
				return true;
			}
			
			
			switch (args[0].toLowerCase()) {
			case "bank":
				wyspa.otwórzBank(p);
				break;
			case "storage":
			case "magazyn":
				wyspa.otwórzMagazyn(p);
				break;
			case "sethome":
			case "ustawdom":
				wyspa.ustawHome(p);
				break;
			case "home":
			case "dom":
				wyspa.tpHome(p);
				break;
			case "public":
			case "publiczna":
				wyspa.ustawPubliczna(p);
				break;
			case "private":
			case "prywatna":
				wyspa.ustawPrywatna(p);
				break;
			case "warp":
				if (args.length > 1) {
					String nazwa = Func.listToString(args, 1);
					for (Wyspa.Warp warp : wyspa.warpy)
						if (warp.nazwa.equalsIgnoreCase(nazwa)) {
							wyspa.tpToWarp(p, warp);
							return true;
						}
				}
			case "warps":
			case "warpy":
				wyspa.otwórzWarpy(p);
				break;
			case "value":
			case "wartość":
				wyspa.wartość(p);
				break;
			case "invite":
			case "zaproś":
				checker.checkFormat(args.length >= 2, "/is zaproś <nick>");
				wyspa.zaproś(p, checker.nieNullFormat(Bukkit.getPlayer(args[1]), "%s nie jest online", args[1]));
				break;
			case "leave":
			case "opuść":
				wyspa.opuść(p);
				break;
			case "delete":
			case "usuń":
				wyspa.usuń(p);
				break;
			case "permissions":
			case "permisje":
			case "uprawnienia":
				wyspa.permisjeInv(p);
				break;
			case "permsedit":
			case "edytujpermisje":
				wyspa.edytorPermisji(p, args);
				break;
			case "members":
			case "członkowie":
				wyspa.otwórzMembers(p);
				break;
			case "kick":
			case "wyrzuć":
				checker.checkFormat(args.length >= 2, "/is wyrzuć <nick>");
				wyspa.wyrzuć(p, args[1]);
				break;
			case "biom":
			case "biome":
				wyspa.otwórzInvBiom(p);
				break;
			case "addwarp":
			case "dodajwarp":
				checker.checkFormat(args.length >= 2, "/is dodajwarp <nazwa>");
				wyspa.dodajWarp(p, args[1]);
				break;
			case "delwarp":
			case "remwarp":
			case "deletewarp":
			case "removewarp":
			case "usuńwarp":
				wyspa.otwórzInvDelWarp(p);
				break;
			case "border":
				wyspa.otwórzInvBorder(p);
				break;
			case "name":
			case "nazwa":
				checker.checkFormat(args.length >= 2, "/is nazwa <nazwa>");
				wyspa.zmieńNazwe(p, args[1]);
				break;
			case "transfer":
			case "przekaż":
				checker.checkFormat(args.length >= 2, "/is przekaż <nick>");
				wyspa.transfer(p, args[1]);
				break;
			case "drop":
				wyspa.otwórzDrop(p);
				break;
			case "upgrade":
			case "ulepsz":
				wyspa.otwórzUlepszenia(p);
				break;
			case "zbanuj":
			case "ban":
				checker.checkFormat(args.length >= 2, "/is ban <nick>");
				wyspa.zbanuj(p, args[1]);
				break;
			case "odbanuj":
			case "unban":
				checker.checkFormat(args.length >= 2, "/is unban <nick>");
				wyspa.odbanuj(p, args[1]);
				break;
			case "coop":
				checker.checkFormat(args.length >= 2, "/is coop <nick>");
				wyspa.coop(p, args[1]);
				break;
			case "uncoop":
				checker.checkFormat(args.length >= 2, "/is uncoop <nick>");
				wyspa.uncoop(p, args[1]);
				break;
			default:
				sender.sendMessage(prefix + "Niepoprawny argument " + args[0]);
			}
		} catch (IndexOutOfBoundsException e) {
			sender.sendMessage(prefix + "coś nie tego chyba nie?");
		}
		return true;
	}
}
