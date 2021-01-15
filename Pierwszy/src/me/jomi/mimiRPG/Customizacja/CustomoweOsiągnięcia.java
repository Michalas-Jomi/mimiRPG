package me.jomi.mimiRPG.Customizacja;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.server.v1_16_R2.Advancement;
import net.minecraft.server.v1_16_R2.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R2.AdvancementDataWorld;
import net.minecraft.server.v1_16_R2.AdvancementDisplay;
import net.minecraft.server.v1_16_R2.AdvancementFrameType;
import net.minecraft.server.v1_16_R2.AdvancementProgress;
import net.minecraft.server.v1_16_R2.AdvancementRewards;
import net.minecraft.server.v1_16_R2.Criterion;
import net.minecraft.server.v1_16_R2.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R2.CustomFunction;
import net.minecraft.server.v1_16_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.PacketPlayOutAdvancements;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NiepoprawneDemapowanieException;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.SkyBlock.SkyBlock;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.SelektorItemów;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;



/**
 * Rezerwuje statystykę (Statistic.USE_ITEM, Material.MAGMA_CREAM) jako postawione bloki gracza 
 * 
 * Rezerwuje statystykę (Statistic.MINE_BLOCK, Material.WHEAT) jako zebrane plony (dorosła przenica, marchewki ziemniaki, netherowe brodawki, buraki)
 * 
 * Rezerwuje statystykę (Statistic.MINE_BLOCK, Material.BRICK) jako wykopane bloki pomijając te nietrwałe (np pochodnie, kwiatki, grzyby, trawa itp)
 *
 */
@Moduł
@Przeładowalny.WymagaReloadBukkitData
public class CustomoweOsiągnięcia extends Komenda implements Listener, Przeładowalny {
	public static class Kryterium extends Mapowany {
		public static enum Typ {
			POSTAWIONE_BLOKI_WSZYSTKIE(Rodzaj.WSKAZANE, Statistic.USE_ITEM,	  Material.MAGMA_CREAM),
			ZNISZCZONE_BLOKI_WSZYSTKIE(Rodzaj.WSKAZANE, Statistic.MINE_BLOCK, Material.BRICK),
			FARMER					  (Rodzaj.WSKAZANE, Statistic.MINE_BLOCK, Material.WHEAT),
			
			WYRZUCONE_DROP		(Rodzaj.ZAKRES, Statistic.DROP,			Material.class),
			PODNIESIONE_ITEMY	(Rodzaj.ZAKRES, Statistic.PICKUP,		Material.class),
			ZNISZCZONE_BLOKI	(Rodzaj.ZAKRES, Statistic.MINE_BLOCK,	Material.class),
			UŻYCIE				(Rodzaj.ZAKRES, Statistic.USE_ITEM,		Material.class),
			ZNISZCZONE_NARZĘDZIA(Rodzaj.ZAKRES, Statistic.BREAK_ITEM,	Material.class),
			WYCRAFTOWANE_ITEMY	(Rodzaj.ZAKRES, Statistic.CRAFT_ITEM,	Material.class),
			ZABITE_MOBY			(Rodzaj.ZAKRES, Statistic.KILL_ENTITY,		EntityType.class),
			ZABITY_PRZEZ_MOBY	(Rodzaj.ZAKRES, Statistic.ENTITY_KILLED_BY,	EntityType.class),

			SKYBLOCK_PUNKTY_WYSPY(Rodzaj.INNE, null, null, false), // api SkyBlock
			ZEBRANE_ITEMY(Rodzaj.INNE, null, null, SelektorItemów.class, true), // selektor itemów
			
			STATYSTYKA(Rodzaj.INNE, null, null, Statistic.class, true);

			public static enum Rodzaj {
				WSKAZANE,
				ZAKRES,
				INNE;
			}
			
			static {
				FARMER.mapaBloków = ZNISZCZONE_BLOKI.mapaBloków;
				ZNISZCZONE_BLOKI_WSZYSTKIE.mapaBloków = ZNISZCZONE_BLOKI.mapaBloków;
				POSTAWIONE_BLOKI_WSZYSTKIE.mapaBloków = UŻYCIE.mapaBloków;
			}
			
			private Map<Material, List<Krotka<Osiągnięcie, Integer>>> mapaBloków;
			private Map<EntityType, List<Krotka<Osiągnięcie, Integer>>> mapaMobów;
			public final Map<Statistic, List<Krotka<Osiągnięcie, Integer>>> mapaStatystyk;
			
			public final Rodzaj rodzaj;
			public final Statistic stat;
			public final Enum<?> konkret;
			public final boolean wymaganaLista;
			private final Class<?> klasaKonkretu;
			Typ(Rodzaj rodzaj, Statistic stat, Enum<?> konkret, Class<?> klasaKonkretu, boolean wymaganaLista) {
				this.wymaganaLista = wymaganaLista;
				this.klasaKonkretu = klasaKonkretu;
				this.konkret = konkret;
				this.rodzaj = rodzaj;
				this.stat = stat;
				
				if (rodzaj == Rodzaj.ZAKRES) {
					boolean bloki = klasaKonkretu.isAssignableFrom(Material.class);
					mapaBloków	= bloki ? new HashMap<>() : null;
					mapaMobów	= bloki ? null : new HashMap<>();
					mapaStatystyk = null;
				} else {
					mapaStatystyk = this.name().equals("STATYSTYKA") ? new HashMap<>() : null;
					mapaBloków = null;
					mapaMobów = null;
				}
			}
			Typ(Rodzaj rodzaj, Statistic stat, Enum<?> konkret, boolean wymaganaLista) {
				this(rodzaj, stat, konkret, null, wymaganaLista);
			}
			Typ(Rodzaj rodzaj, Statistic stat, Enum<?> konkret) {
				this(rodzaj, stat, konkret, false);
			}
			Typ(Rodzaj rodzaj, Statistic stat, Class<?> klasaKonkretu) {
				this(rodzaj, stat, null, klasaKonkretu, true);
			}
			
			@SuppressWarnings("unchecked")
			public <T> Map<T, List<Krotka<Osiągnięcie, Integer>>> mapa() {
				return mapaBloków == null ? (Map<T, List<Krotka<Osiągnięcie, Integer>>>) mapaMobów : (Map<T, List<Krotka<Osiągnięcie, Integer>>>) mapaBloków;
			} 
			public int ile(Player p, Kryterium kryterium) {
				if (rodzaj == Rodzaj.WSKAZANE)
					if (konkret.getClass().isAssignableFrom(Material.class))
						return p.getStatistic(this.stat, (Material) konkret);
					else
						return p.getStatistic(this.stat, (EntityType) konkret);

				if (this == SKYBLOCK_PUNKTY_WYSPY)
					try {
						return (int) SkyBlock.Wyspa.wczytaj(Gracz.wczytaj(p)).getPkt();
					} catch (NullPointerException e) {
						return 0;
					}
				
				int ile = 0;
				
				if (this == STATYSTYKA) {
					for (Object stat : kryterium.co)
						ile += p.getStatistic((Statistic) stat);
					return ile;
				}
				
				if (this == ZEBRANE_ITEMY) {
					for (Object selektor : kryterium.co)
						ile += ((SelektorItemów) selektor).zlicz(p.getInventory());
					return ile;
				}
				
				if (rodzaj == Rodzaj.ZAKRES) {
					if (klasaKonkretu.isAssignableFrom(Material.class))
						for (Object stat : kryterium.co)
							ile += p.getStatistic(this.stat, (Material) stat);
					else
						for (Object stat : kryterium.co)
							ile += p.getStatistic(this.stat, (EntityType) stat);
					return ile;
				}
				
				
				throw new Error("Nieobsługiwany CustomoweOsiągnięcia.Kryterium.Typ " + this);
			}
			public Class<?> klasaKonkretu() {
				return konkret != null ? konkret.getClass() : klasaKonkretu;
			}
		}
		@Mapowane String nazwa;
		@Mapowane int ile = 1;
		@Mapowane Typ typ = Typ.ZNISZCZONE_BLOKI;
		@Mapowane(nieTwórz = true) private List<String> konkrety = Lists.newArrayList();

		List<Object> co;
		@Override
		protected void Init() {
			if (nazwa == null)
				nazwa = Func.losujZnaki(10, 20, "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890");
			else if (nazwa.contains(":") || nazwa.contains("."))
				throw new Error("Nazwa kryterium osiągnięcia nie może zawierać znaków \":\" i \".\""); // TODO tylko \w
			
			if (typ.wymaganaLista) {
				co = Lists.newArrayList();
				konkrety.forEach(str -> {
					Class<?> clazz = typ.klasaKonkretu();
					if (clazz.isEnum())
						co.add(Func.StringToEnum(clazz, str));
					else  if (clazz.isAssignableFrom(SelektorItemów.class))
						co.add(Config.selektorItemów(str));
					else
						throw new Error("Nieprzewidzana Klasa konkretu w CustomoweOsiągnięcia.Typ: " + clazz);
				});
			}
			
			if (konkrety != null)
				konkrety.forEach(el -> 
					Func.multiTry(IllegalArgumentException.class,
							() -> co.add(Func.StringToEnum(EntityType.class, el)),
							() -> co.add(Func.StringToEnum(Material.class, el)),
							() -> co.add(Func.StringToEnum(Statistic.class, el)),
							() -> {
								SelektorItemów selektor = Config.selektorItemów(el);
								if (selektor == null)
									Main.warn("Customowe Osiągnięcia - Niepopwane kryterium  " + nazwa + " \"" + el + "\"");
								else
									co.add(selektor);
							}
							));
		}
	}
	public static class Osiągnięcie extends Mapowany {
		final static Map<NamespacedKey, Osiągnięcie> mapa = new HashMap<>();
		@Mapowane String tło;
		@Mapowane String parent;
		@Mapowane String namespacedKey;
		@Mapowane List<Kryterium> kryteria;

		@Mapowane AdvancementFrameType ramka = AdvancementFrameType.TASK;
		@Mapowane boolean show_toast = true;
		@Mapowane boolean announce_to_chat = true;
		@Mapowane boolean hidden = false;
		
		@Mapowane ItemStack ikona;
		@Mapowane String nazwa = "Osiągnięcie";
		@Mapowane String opis = "Zwyczajne osiągnięcie\nco więcej potrzeba?";
		
		@Mapowane List<ItemStack> nagroda;
		@Mapowane double nagrodaKasa = 0d;
		@Mapowane int nagrodaWalutaPremium = 0;
		@Mapowane int exp;
		
		@Mapowane float x;
		@Mapowane float y;

		
		NamespacedKey klucz;
		org.bukkit.advancement.Advancement adv;
		
		
		@Override
		protected void Init() {
			if (parent != null) {
				if (!parent.contains(":"))
					parent = Main.plugin.getName() + ":" + parent;
				parent = parent.toLowerCase();
			}
			if (namespacedKey != null) {
				if (!namespacedKey.contains(":"))
					namespacedKey = Main.plugin.getName() + ":" + namespacedKey;
				namespacedKey = namespacedKey.toLowerCase();
			} else
				throw new NiepoprawneDemapowanieException("Brak namespacedKey w Cusomowym osiągnięciu");
			
			klucz = CraftNamespacedKey.fromString(namespacedKey);
			
			nagrodaWalutaPremium = Math.max(0, nagrodaWalutaPremium);
			nagrodaKasa = Math.max(0, nagrodaKasa);
			
			
			Set<String> nazwy = Sets.newConcurrentHashSet();
			kryteria.forEach(k -> {
				if (!nazwy.add(k.nazwa))
					throw new NiepoprawneDemapowanieException("Nazwy kryteriów w osiągnięciach nie mogą sie powtarzać");
			});
			
/*
			/// XXX DEBUG
			if (namespacedKey.startsWith("mimirpg:budowniczy")) {
				Kryterium k = kryteria.get(0);
				k.typ = Kryterium.Typ.POSTAWIONE_BLOKI_WSZYSTKIE;
				opis = "Postaw " + Func.IntToString(k.ile) + " bloków";
				k.nazwa = "k1";
				k.konkrety = null;
				k.co = null;
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:farmer")) {
				Kryterium k = kryteria.get(0);
				opis = "Zbierz " + Func.IntToString(k.ile) + " plonów";
				k.typ = Kryterium.Typ.FARMER;
				k.nazwa = "k1";
				k.konkrety = null;
				k.co = null;
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:lowca")) {
				Kryterium k = kryteria.get(0);
				opis = "Zabij " + Func.IntToString(k.ile) + " potworów";
				k.typ = Kryterium.Typ.ZABITE_MOBY;
				k.nazwa = "k1";
				k.konkrety = Lists.newArrayList("Zombie", "Skeleton", "Spider", "Creeper", "Blaze", "Enderman", "Wither Skeleton", "Zoglin", "Witch", "Guardian", "Evoker", "Vindicator", "Husk", "Slime", "Magma Cube", "Phantom", "Pillager", "Shulker", "Drowned", "Stray", "Zombified Piglin");
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:miner")) {
				Kryterium k = kryteria.get(0);
				opis = "Wykop " + Func.IntToString(k.ile) + " Rud";
				k.typ = Kryterium.Typ.ZNISZCZONE_BLOKI;
				k.nazwa = "k1";
				k.konkrety = Lists.newArrayList("Coal Ore", "Iron Ore", "Gold Ore", "Diamond Ore", "Emerald Ore", "Redstone Ore", "Lapis Ore", "Nether Quartz Ore");
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:poziomwyspy")) {
				Kryterium k = kryteria.get(0);
				opis = "Zdobądz " + Func.IntToString(k.ile) + " punktów wyspy";
				k.typ = Kryterium.Typ.SKYBLOCK_PUNKTY_WYSPY;
				k.nazwa = "k1";
				k.konkrety = null;
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:rybak")) {
				Kryterium k = kryteria.get(0);
				opis = "Wyłów " + Func.IntToString(k.ile) + " ryb";
				k.typ = Kryterium.Typ.STATYSTYKA;
				k.nazwa = "k1";
				k.konkrety = Lists.newArrayList("FISH_CAUGHT");
				k.Init();
			} else if (namespacedKey.startsWith("mimirpg:zabojca")) {
				Kryterium k = kryteria.get(0);
				opis = "Zabij " + Func.IntToString(k.ile) + " graczy";
				k.typ = Kryterium.Typ.STATYSTYKA;
				k.nazwa = "k1";
				k.konkrety = Lists.newArrayList("PLAYER_KILLS");
				k.Init();
			} else
				Main.warn("Omijane osiągnięcie debugu: " + namespacedKey);
			/// XXX DEBUG
*/
			nazwa = Func.koloruj(nazwa);
			opis = Func.koloruj(opis);
		}

		private boolean stworzone = false;
		void stwórz() {
			if (stworzone)
				return;
			Advancement adv = null;
			if (this.parent != null) {
				NamespacedKey parent = CraftNamespacedKey.fromString(this.parent);
				try {
					mapa.get(parent).stwórz();
					adv = ((CraftAdvancement) Bukkit.getAdvancement(parent)).getHandle();
				} catch(Throwable e) {
					Main.warn("Nieprawidłowy parent Customowego Osiągnięcia " + klucz + " parent: " + this.parent);
				}
			}
			stworzone = true;
			this.adv = stwórzNowe(CraftNamespacedKey.toMinecraft(klucz), ikona, nazwa, opis, ramka, adv, tło, x, y, show_toast, announce_to_chat, hidden, kryteria).bukkit;
		}
		
		public void odznacz(Player p, int index) {
			p.getAdvancementProgress(adv).awardCriteria(kryteria.get(index).nazwa);
		}
	}
	
	static class AdvStałe {
		static final AdvancementRewards reward;
		static final String[][] strs = new String[1][1];
		static final Map<String, Criterion> mapa = new HashMap<>();
		static final Criterion niewykonalneKryterium = new Criterion(new CriterionTriggerImpossible.a());
		static {
			CustomFunction func = new CustomFunction(new MinecraftKey(Main.plugin.getName().toLowerCase(), "c"), new CustomFunction.c[0]);
			// new AdvancementRewards(exp, loot, recipes, function)
			reward = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], new CustomFunction.a(func));
			mapa.put("i", niewykonalneKryterium);
			strs[0][0] = "i";
		}
	}
	@SuppressWarnings("resource")
	static Advancement stwórzNowe(MinecraftKey key, ItemStack ikona, String nazwa, String opis, AdvancementFrameType ramka, Advancement parent, String tło,
			float x, float y, boolean show_toast, boolean announce_to_chat, boolean hidden, List<Kryterium> kryteria) {
		AdvancementDisplay display = new AdvancementDisplay(
				CraftItemStack.asNMSCopy(ikona),
				ChatSerializer.a("{\"text\":\""+nazwa+"\"}"),
				ChatSerializer.a("{\"text\":\""+opis+"\"}"),
				parent != null ? null : new MinecraftKey("minecraft", tło == null ? "textures/block/light_blue_concrete.png" : tło),
				ramka,
				show_toast,
				announce_to_chat,
				hidden);
		
		display.a(x, y);
		
		
		Map<String, Criterion> mapaKryteriów;
		String[][] wymaganeKryteria;
		if (!kryteria.isEmpty()) {
			wymaganeKryteria = new String[kryteria.size()][1];
			mapaKryteriów = new HashMap<>();
			int i = 0;
			for (Kryterium kryterium : kryteria) {
				wymaganeKryteria[i++][0] = kryterium.nazwa;
				mapaKryteriów.put(kryterium.nazwa, AdvStałe.niewykonalneKryterium);
			}
		} else {
			mapaKryteriów = AdvStałe.mapa;
			wymaganeKryteria = AdvStałe.strs;
		}
		
		Advancement adv = new Advancement(key, parent, display, AdvStałe.reward, mapaKryteriów, wymaganeKryteria);
		
		((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData().REGISTRY.advancements.put(key, adv);
		
		return adv;
	}

	
	final EdytorOgólny<Osiągnięcie> edytor = new EdytorOgólny<>("edytujosiągnięcia", Osiągnięcie.class);
	public CustomoweOsiągnięcia() {
		super("edytujosiągnięcia", "edytujosiągnięcia edytor (-t <nazwa>)");
		
		edytor.zarejestrujOnInit((adv, ścieżka) -> adv.namespacedKey = ścieżka);
		edytor.zarejestrójWyjątek("/edytujosiągnięcia edytor tło", (adv, ścieżka) -> {
			if (adv.parent != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
		edytor.zarejestrójWyjątek("/edytujosiągnięcia edytor parent", (adv, ścieżka) -> {
			if (adv.tło != null)
				return null;
			throw new EdytorOgólny.DomyślnyWyjątekException();
		});
		edytor.zarejestrujOnZatwierdzZEdytorem((adv, edytor) -> {
			if (!adv.namespacedKey.contains(":"))
				adv.namespacedKey = Main.plugin.getName() + ":" + adv.namespacedKey;
			adv.namespacedKey = adv.namespacedKey.toLowerCase();
			edytor.ścieżka = adv.namespacedKey;
			adv.Init();
		});
		edytor.zarejestrujPoZatwierdz((dawnyAdv, adv) -> {
			if (dawnyAdv.klucz != null)
				Osiągnięcie.mapa.remove(dawnyAdv.klucz);
			Osiągnięcie.mapa.put(adv.klucz, adv);
			zapisz();
		});
	}
	
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void osiągnięcia(PlayerAdvancementDoneEvent ev) {
		Func.wykonajDlaNieNull(Osiągnięcie.mapa.get(ev.getAdvancement().getKey()), adv -> {
			adv.nagroda.forEach(item -> Func.dajItem(ev.getPlayer(), item.clone()));
			Func.dajWPremium(ev.getPlayer(), adv.nagrodaWalutaPremium);
			ev.getPlayer().giveExp(adv.exp);
			if (adv.nagrodaKasa != 0)
				if (Main.ekonomia)
					Main.econ.depositPlayer(ev.getPlayer(), adv.nagrodaKasa);
				else
					Main.warn("Nie odnaleziono ekonomi na serwerze! " + ev.getPlayer().getName() + " nie otrzymał należnych mu " 
							+ adv.nagrodaKasa + "$ za osiągnięcie " + adv.namespacedKey);
			Main.log(ev.getPlayer().getName() + " ukończył osiągnięcie " + adv.namespacedKey);
		});
	}
	
	
	// mob/blok: [(osiągnięcie, indexKryterium)]
	static final List<Krotka<SelektorItemów, Krotka<Osiągnięcie, Integer>>> listaSelektorów = Lists.newArrayList();
	static final List<Krotka<Osiągnięcie, Integer>> listaPunktówWyspy = Lists.newArrayList();
	
	
	int staty(Player p, Statistic stat, Object konkret) {
		if (konkret instanceof Material)
			return p.getStatistic(stat, (Material) konkret);
		else
			return p.getStatistic(stat, (EntityType) konkret);
		
	}
	void sprawdzKryterium(Player p, Osiągnięcie adv, int index) {
		Kryterium kryterium = adv.kryteria.get(index);
		
		if (p.getAdvancementProgress(adv.adv).getAwardedCriteria().contains(kryterium.nazwa))
				return;
		
		int ile = 0;
		
		switch (kryterium.typ.rodzaj) {
		case INNE:
			if (kryterium.typ == Kryterium.Typ.SKYBLOCK_PUNKTY_WYSPY)
				try {
					ile += SkyBlock.Wyspa.wczytaj(Gracz.wczytaj(p)).getPkt();
				} catch (NullPointerException e) {}
			else if (kryterium.typ == Kryterium.Typ.ZEBRANE_ITEMY)
				for (Object co : kryterium.co)
					ile += ((SelektorItemów) co).zlicz(p.getInventory());
			else if (kryterium.typ == Kryterium.Typ.STATYSTYKA)
				for (Object co : kryterium.co)
					ile += p.getStatistic((Statistic) co);
			else
				throw new Error("Nieznany typ kryterium Customowego Osiągnięcia " + kryterium.typ);
			break;
		case WSKAZANE:
			ile += staty(p, kryterium.typ.stat, kryterium.typ.konkret);
			break;
		case ZAKRES:
			for (Object co : kryterium.co)
				ile += staty(p, kryterium.typ.stat, co);
			break;
		}
		
		if (ile >= kryterium.ile)
			adv.odznacz(p, index);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void podnoszenieStatystyk(PlayerStatisticIncrementEvent ev) {
		if (ev.isCancelled()) return;

		Player p = ev.getPlayer();
		Consumer<List<Krotka<Osiągnięcie, Integer>>> cons = lista -> {
			if (lista != null)
				Bukkit.getScheduler().runTask(Main.plugin, () -> lista.forEach(krotka -> sprawdzKryterium(p, krotka.a, krotka.b)));
		};
		
		
		Func.wykonajDlaNieNull(Kryterium.Typ.STATYSTYKA.mapaStatystyk.get(ev.getStatistic()), cons::accept);
				
		if (ev.getStatistic().isSubstatistic())
			for (Kryterium.Typ typ : Kryterium.Typ.values())
				if (ev.getStatistic() == typ.stat)
					switch (typ.rodzaj) {
					case WSKAZANE:
						if (typ.konkret == ev.getMaterial())
							cons.accept(typ.mapa().get(ev.getMaterial()));
						break;
					case ZAKRES:
						Enum<?> en = ev.getMaterial() == null ? ev.getEntityType() : ev.getMaterial();
						if (typ.klasaKonkretu().isAssignableFrom(en.getClass()))
							cons.accept(typ.mapa().get(en));
						break;
					case INNE:
						break;
					}
	}
	private void incrementStatistic(Player p, Statistic stat, Material mat) {
		int akt = p.getStatistic(stat, mat);
		PlayerStatisticIncrementEvent ev = new PlayerStatisticIncrementEvent(p, stat, akt, akt+1, mat);
		Bukkit.getPluginManager().callEvent(ev);
		if (!ev.isCancelled())
			p.setStatistic(stat, mat, ev.getNewValue());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (ev.getEntityType() != EntityType.PLAYER) return;
		if (ev.isCancelled()) return;
		
		Player p = (Player) ev.getEntity();
		
		Bukkit.getScheduler().runTask(Main.plugin, () ->
			listaSelektorów.forEach(krotka ->
				sprawdzKryterium(p, krotka.b.a, krotka.b.b)));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void stawianie(BlockPlaceEvent ev) {
		if (!ev.isCancelled())
			incrementStatistic(ev.getPlayer(), Statistic.USE_ITEM, Material.MAGMA_CREAM);
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void niszczenieBloków(BlockBreakEvent ev) {
		if (ev.isCancelled())
			return;
		
		Player p = ev.getPlayer();
		Block b = ev.getBlock();
		if  (b != null)
			switch (b.getType()) {
			case WHEAT:
			case POTATOES:
			case CARROTS:
				if (b.getBlockData().getAsString().contains("age=7"))
					incrementStatistic(p, Statistic.MINE_BLOCK, Material.WHEAT);
				break;
			case NETHER_WART:
			case BEETROOTS:
				if (b.getBlockData().getAsString().contains("age=3"))
					incrementStatistic(p, Statistic.MINE_BLOCK, Material.WHEAT);
				break;
			default:
				if (b.getType().getHardness() != 0)
					incrementStatistic(p, Statistic.MINE_BLOCK, Material.BRICK);
				break;
			}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void liczenieWartościWyspy(SkyBlock.API.PrzeliczaniePunktówWyspyEvent ev) {
		Func.wykonajDlaNieNull(ev.p, p -> 
			Bukkit.getScheduler().runTask(Main.plugin, () ->
				listaPunktówWyspy.forEach(krotka ->
					sprawdzKryterium(p, krotka.a, krotka.b))));
	}
	
	@SuppressWarnings("resource")
	void zapomnijUsunięte(Player p) {
		Map<MinecraftKey, Advancement> advs = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData().REGISTRY.advancements;
		Map<Advancement, AdvancementProgress> data = ((CraftPlayer) p).getHandle().getAdvancementData().data;
		Set<Advancement> doUsunięcia = Sets.newConcurrentHashSet();
		data.forEach((adv, nmsprog) -> {
			if (!advs.containsKey(adv.getName())) {
				org.bukkit.advancement.AdvancementProgress prog = p.getAdvancementProgress(adv.bukkit);
				prog.getAwardedCriteria().forEach(prog::revokeCriteria);
				doUsunięcia.add(adv);
			}
		});
		doUsunięcia.forEach(data::remove);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void dołączanie(PlayerJoinEvent ev) {
		zapomnijUsunięte(ev.getPlayer());
	}
	
	
	// Override
	
	final File configPlik = new File(Main.path + "configi/Customowe Osiągnięcia.yml");
	private final Map<String, Map<MinecraftKey, AdvancementProgress>> preReload = new HashMap<>();
	@Override
	public void preReloadBukkitData() {
		Bukkit.getOnlinePlayers().forEach(p -> {
			Map<MinecraftKey, AdvancementProgress> mapa = new HashMap<>();
			((CraftPlayer) p).getHandle().getAdvancementData().data.forEach((adv, prog) -> {
				if (Osiągnięcie.mapa.containsKey(CraftNamespacedKey.fromMinecraft(adv.getName())))
					mapa.put(adv.getName(), prog);
			});
			preReload.put(p.getName(), mapa);
		});
	}
	@Override
	public void przeładuj() {
		Config config = new Config(configPlik);
		
		Osiągnięcie.mapa.clear();
		
		config.wartości(Osiągnięcie.class).forEach(adv -> Osiągnięcie.mapa.put(adv.klucz, adv));

		Kryterium.Typ.STATYSTYKA.mapaStatystyk.clear();
		for (Kryterium.Typ typ : Kryterium.Typ.values())
			Func.wykonajDlaNieNull(typ.mapa(), Map::clear);
		
		listaSelektorów.clear();
		Osiągnięcie.mapa.forEach((klucz, adv) -> {
			adv.stwórz();
			for (int i=0; i < adv.kryteria.size(); i++) {
				Kryterium kryterium = adv.kryteria.get(i);
				Krotka<Osiągnięcie, Integer> krotka = new Krotka<>(adv, i);
				
				BiConsumer<Map<?, List<Krotka<Osiągnięcie, Integer>>>, Object> bic = (mapa, k) -> {
					if (mapa.containsKey(k))
						mapa.get(k).add(krotka);
					else
						mapa.put(Func.pewnyCast(k), Lists.newArrayList(krotka));
				};
				switch (kryterium.typ.rodzaj) {
				case WSKAZANE:
					bic.accept(kryterium.typ.mapa(), kryterium.typ.konkret);
					break;
				case ZAKRES:
					for (Object co : kryterium.co)
						bic.accept(kryterium.typ.mapa(), co);
					break;
				case INNE:
					switch (kryterium.typ) {
					case STATYSTYKA:
						for (Object co : kryterium.co)
							bic.accept(Kryterium.Typ.STATYSTYKA.mapaStatystyk, co);
						break;
					case ZEBRANE_ITEMY:
						for (Object co : kryterium.co)
							listaSelektorów.add(new Krotka<>((SelektorItemów) co, krotka));
						break;
					case SKYBLOCK_PUNKTY_WYSPY:
						listaPunktówWyspy.add(krotka);
						break;
					default:
						break;
					}
					break;
				}
				
			}
		});
		
		AdvancementDataWorld dataWorld = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData();
		Map<MinecraftKey, Advancement> advs = dataWorld.REGISTRY.advancements;
		
		preReload.forEach((nick, mapa) ->
				Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> {
					AdvancementDataPlayer data = ((CraftPlayer) p).getHandle().getAdvancementData();

					Set<Advancement> setAdvs = Sets.newConcurrentHashSet();
					Set<MinecraftKey> setKluczy = Sets.newConcurrentHashSet();
					
					mapa.forEach((klucz, prog) ->
					Func.wykonajDlaNieNull(advs.get(klucz), adv -> {
						try {
							Func.dajMetode(AdvancementDataPlayer.class, "a", Advancement.class, AdvancementProgress.class).invoke(data, adv, prog);
							
							setAdvs.add(adv);
							setKluczy.add(adv.getName());
							
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}));

					Map<MinecraftKey, AdvancementProgress> m = new HashMap<>();
					data.data.forEach((adv, prog) -> m.put(adv.getName(), prog));
					
					PacketPlayOutAdvancements packet = new PacketPlayOutAdvancements(
							true,
							setAdvs,
							setKluczy,
							m);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}));
		preReload.clear();

		
		
		Main.ust.wczytajListe("Zablokowane Osiągnięcia").forEach(pattern -> {
			Pattern pat = Pattern.compile(pattern);
			Set<MinecraftKey> doUsunięcia = Sets.newConcurrentHashSet();
			advs.forEach((klucz, adv) -> {
				if (pat.matcher(klucz.toString()).matches()) {
					doUsunięcia.add(klucz);
					try {
						Field requirements = Advancement.class.getDeclaredField("requirements");
						requirements.setAccessible(true);
						requirements.set(adv, AdvStałe.strs);
						Field criteria = Advancement.class.getDeclaredField("criteria");
						criteria.setAccessible(true);
						criteria.set(adv, AdvStałe.mapa);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					
				}
			});
			doUsunięcia.forEach(advs::remove);
		});
		Bukkit.getOnlinePlayers().forEach(this::zapomnijUsunięte);
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane osiągnięcia", Osiągnięcie.mapa.size());
	}
	
	public void zapisz() {
		Config config = new Config(configPlik);
		
		config.klucze(false).forEach(klucz -> config.ustaw(klucz, null));
		
		Osiągnięcie.mapa.values().forEach(adv -> config.ustaw(adv.namespacedKey, adv));
		
		config.zapisz();
		
		Main.reloadBukkitData();
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Supplier<List<String>> advs = () -> Func.wykonajWszystkim(Osiągnięcie.mapa.keySet(), NamespacedKey::toString);
		if (args.length <= 1)
			return utab(args, "edytor", "usuń");
		if (args.length == 2)
			if (args[0].equalsIgnoreCase("edytor"))
				return utab(args, "-t", "-u");
			else
				return utab(args, advs.get());
		if (args.length == 3 && args[1].equals("-t"))
			return utab(args, advs.get());
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		
		switch (args[0].toLowerCase()) {
		case "e":
		case "edytor":
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, "/edytujosiągnięcia edytor -t <nazwa osiągnięcia>");
			else if (args.length >= 2 && args[1].equals("-t")) {
				if (!args[2].contains(":"))
					args[2] = Main.plugin.getName() + ":" + args[2];
				args[2] = args[2].toLowerCase();
				args[2] = "configi/Customowe Osiągnięcia|" + args[2];
			}
			return edytor.onCommand(sender, "edytujosiągnięcia", args);
		case "delete":
		case "del":
		case "remove":
		case "rem":
		case "u":
		case "usun":
		case "usuń":
			if (args.length < 2)
				return Func.powiadom(sender, "/edytujosiągnięcia usuń <nazwa>");
			if (!args[1].contains(":"))
				args[1] = Main.plugin.getName() + ":" + args[1];
			args[1] = args[1].toLowerCase();
			Osiągnięcie adv = Osiągnięcie.mapa.remove(CraftNamespacedKey.fromString(args[1]));
			if (adv == null)
				return Func.powiadom(sender, "Nieprawidłowe osiągnięcie " + args[1]);
			zapisz();
			return Func.powiadom(sender, "Usunięto osiągnięcie " + args[1]);
		}
		
		return false;
	}
}
