package me.jomi.mimiRPG.Customizacja;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
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

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NiepoprawneDemapowanieException;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.SelektorItemów;

@Moduł
@Przeładowalny.WymagaReloadBukkitData
public class CustomoweOsiągnięcia extends Komenda implements Listener, Przeładowalny {
	public static class Kryterium extends Mapowany {
		@Mapowane String nazwa;
		@Mapowane int ile = 1;
		@Mapowane private List<String> czego; // Material | EntityType | SelektorItemów

		List<Object> co = Lists.newArrayList();
		@Override
		protected void Init() {
			if (nazwa == null)
				nazwa = Func.losujZnaki(10, 20, "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890");
			else if (nazwa.contains(":") || nazwa.contains(","))
				throw new Error("Nazwa kryterium osiągnięcia nie może zawierać znaków \":\" i \",\"");
			
			czego.forEach(el -> 
				Func.multiTry(IllegalArgumentException.class,
						() -> co.add(Func.StringToEnum(EntityType.class, el)),
						() -> co.add(Func.StringToEnum(Material.class, el)),
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
		@Mapowane List<Kryterium> kryteria;
		@Mapowane String namespacedKey;

		@Mapowane ItemStack ikona;
		@Mapowane String nazwa = "Osiągnięcie";
		@Mapowane String opis = "Zwyczajne osiągnięcie\nco więcej potrzeba?";
		@Mapowane AdvancementFrameType ramka = AdvancementFrameType.TASK;
		@Mapowane boolean show_toast = true;
		@Mapowane boolean announce_to_chat = true;
		@Mapowane boolean hidden = false;
		
		@Mapowane List<ItemStack> nagroda;
		@Mapowane double nagrodaKasa = 0d;
		@Mapowane int nagrodaWalutaPremium = 0;
		@Mapowane int exp;
		
		@Mapowane float x;
		@Mapowane float y;
		
		@Mapowane String parent;
		@Mapowane String tło;
		
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
			
			nazwa = Func.koloruj(nazwa);
			opis = Func.koloruj(opis);
			
			Set<String> nazwy = Sets.newConcurrentHashSet();
			kryteria.forEach(k -> {
				if (!nazwy.add(k.nazwa))
					throw new NiepoprawneDemapowanieException("Nazwy kryteriów w osiągnięciach nie mogą sie powtarzać");
			});
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
	static final Map<EntityType, List<Krotka<Osiągnięcie, Integer>>> mapaMobów	= new HashMap<>(); 
	static final Map<Material, List<Krotka<Osiągnięcie, Integer>>> mapaBloków	= new HashMap<>();
	static final List<Krotka<SelektorItemów, Krotka<Osiągnięcie, Integer>>> listaSelektorów = Lists.newArrayList(); 

	void sprawdzKryterium(Player p, Osiągnięcie adv, int index) {
		Kryterium kryterium = adv.kryteria.get(index);
		
		if (p.getAdvancementProgress(adv.adv).getAwardedCriteria().contains(kryterium.nazwa))
				return;
		
		int ile = 0;
		
		for (Object co : kryterium.co) {
			if (co instanceof Material)
				ile += p.getStatistic(Statistic.MINE_BLOCK, (Material) co);
			else if (co instanceof EntityType)
				ile += p.getStatistic(Statistic.KILL_ENTITY, (EntityType) co);
			else if (co instanceof SelektorItemów)
				ile += ((SelektorItemów) co).zlicz(p.getInventory());
			else
				throw new Error("Nieprawidłowe kryterium Customowych osiągnięć: " + co);
			
			if (ile >= kryterium.ile) {
				adv.odznacz(p, index);
				return;
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void podnoszenieStatystyk(PlayerStatisticIncrementEvent ev) {
		if (ev.isCancelled()) return;
		List<Krotka<Osiągnięcie, Integer>> lista;
		switch (ev.getStatistic()) {
		case KILL_ENTITY: lista = mapaMobów .get(ev.getEntityType()); break;
		case MINE_BLOCK:  lista = mapaBloków.get(ev.getMaterial());   break;
		default:
			return;
		}
		Player p = ev.getPlayer();
		if (lista != null)
			Bukkit.getScheduler().runTask(Main.plugin, () -> lista.forEach(krotka -> sprawdzKryterium(p, krotka.a, krotka.b)));
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
		
		mapaMobów.clear();
		mapaBloków.clear();
		listaSelektorów.clear();
		Osiągnięcie.mapa.forEach((klucz, adv) -> {
			adv.stwórz();
			for (int i=0; i < adv.kryteria.size(); i++) {
				Kryterium kryterium = adv.kryteria.get(i);
				Krotka<Osiągnięcie, Integer> krotka = new Krotka<>(adv, i);
				
				kryterium.co.forEach(kco -> {
					
					Map<? extends Enum<?>, List<Krotka<Osiągnięcie, Integer>>> mapa = null;
					
					if (kco instanceof Material)
						mapa = mapaBloków;
					else if (kco instanceof EntityType)
						mapa = mapaMobów;
					else if (kco instanceof SelektorItemów)
						listaSelektorów.add(new Krotka<>((SelektorItemów) kco, krotka));
					if (mapa != null)
						if (mapa.containsKey(kco))
							mapa.get(kco).add(krotka);
						else
							mapa.put(Func.pewnyCast(kco), Lists.newArrayList(krotka));
				});
				
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
