package me.jomi.mimiRPG.PojedynczeKomendy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
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
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.SelektorItemów;

@Moduł
@Przeładowalny.WymagaReloadBukkitData
public class CustomoweOsiągnięcia extends Komenda implements Listener, Przeładowalny {
	public static class Kryterium extends Mapowany {
		@Mapowane String nazwa;
		@Mapowane int ile;
		@Mapowane private String czego;// Material | EntityType | SelektorItemów

		Object co;
		void Init() {
			if (nazwa == null)
				nazwa = Func.losujZnaki(10, 20, "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890");
			else if (nazwa.contains(":") || nazwa.contains(","))
				throw new Error("Nazwa kryterium osiągnięcia nie może zawierać znaków \":\" i \",\"");
			
			if (co != null)
				Func.multiTry(IllegalArgumentException.class,
						() -> co = Func.StringToEnum(EntityType.class, czego),
						() -> co = Func.StringToEnum(Material.class, czego),
						() -> co = Config.selektorItemów(czego)
						);
			
			
		}
		
		@SuppressWarnings("unchecked")
		<T> T czego() {
			return (T) co;
		};
	}
	public static class Osiągnięcie extends Mapowany {
		final static Map<NamespacedKey, Osiągnięcie> mapa = new HashMap<>();
		@Mapowane List<ItemStack> nagroda;
		@Mapowane List<Kryterium> kryteria;// TODO zapewnić unikalne nazwy kryteriów w obrębie osiągnięcia
		@Mapowane String namespacedKey;
		@Mapowane int exp;

		@Mapowane ItemStack ikona;
		@Mapowane String nazwa;
		@Mapowane String opis;
		@Mapowane AdvancementFrameType ramka = AdvancementFrameType.TASK;
		@Mapowane boolean show_toast = true;
		@Mapowane boolean announce_to_chat = true;
		@Mapowane boolean hidden = false;
		
		@Mapowane float x;
		@Mapowane float y;
		
		@Mapowane String tło;
		@Mapowane String parent;
		
		NamespacedKey klucz;
		org.bukkit.advancement.Advancement adv;
		
		
		void Init() {
			klucz = CraftNamespacedKey.fromString(namespacedKey);
			nazwa = Func.koloruj(nazwa);
			opis = Func.koloruj(opis);
			if (parent != null) {
				if (!parent.contains(":"))
					parent = Main.plugin.getName() + ":" + parent;
				parent = parent.toLowerCase();
			}
			
			Set<String> nazwy = Sets.newConcurrentHashSet();
			kryteria.forEach(k -> {
				if (!nazwy.add(k.nazwa))
					throw new Error("Nazwy kryteriów w osiągnięciach nie mogą sie powtarzać");
			});
		}

		boolean stworzone = false;
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
			this.adv = stwórzNowe(CraftNamespacedKey.toMinecraft(klucz), ikona, nazwa, opis, ramka, adv, tło, x, y, show_toast, announce_to_chat, hidden).bukkit;
		}
	
		public void odznacz(Player p, int indexKryterium) {
			Gracz g = Gracz.wczytaj(p);
			Kryterium kryterium = kryteria.get(indexKryterium);
			List<String> lista = Func.domyślna(g.osiągnięciaUkończoneKryteria.get(namespacedKey), Lists::newArrayList);
			if (!lista.contains(kryterium.nazwa)) {
				lista.add(kryterium.nazwa);
				g.osiągnięciaUkończoneKryteria.put(namespacedKey, lista);
				g.zapisz();
				
				if (g.osiągnięciaUkończoneKryteria.size() >= kryteria.size()) {
					for (Kryterium k : kryteria)
						if (!lista.contains(k.nazwa))
							return;
					Bukkit.getPluginManager().callEvent(new PlayerAdvancementDoneEvent(p, adv));
				}
			}
		}
	}
	
	static class AdvStałe {
		static final AdvancementRewards reward;
		static final String[][] strs = new String[1][1];
		static final Map<String, Criterion> mapa = new HashMap<>();
		static {
			CustomFunction func = new CustomFunction(new MinecraftKey(Main.plugin.getName().toLowerCase(), "c"), new CustomFunction.c[0]);
			// new AdvancementRewards(exp, loot, recipes, function)
			reward = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], new CustomFunction.a(func));
			mapa.put("i", new Criterion(new CriterionTriggerImpossible.a()));
			strs[0][0] = "i";
		}
	}
	@SuppressWarnings("resource")
	static Advancement stwórzNowe(MinecraftKey key, ItemStack ikona, String nazwa, String opis, AdvancementFrameType ramka, Advancement parent, String tło,
			float x, float y, boolean show_toast, boolean announce_to_chat, boolean hidden) {
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
		
		Advancement adv = new Advancement(key, parent, display, AdvStałe.reward, AdvStałe.mapa, AdvStałe.strs);
		
		((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData().REGISTRY.advancements.put(key, adv);
		
		return adv;
	}

	
	final EdytorOgólny<Osiągnięcie> edytor = new EdytorOgólny<>("edytujosiągnięcia", Osiągnięcie.class);
	public CustomoweOsiągnięcia() {
		super("edytujosiągnięcia", "edytujosiągnięcia edytor (-t <nazwa>)");
		
		edytor.zarejestrujOnInit((adv, ścieżka) -> adv.namespacedKey = ścieżka);
		edytor.zarejestrujOnZatwierdzZEdytorem((adv, edytor) -> {
			if (!adv.namespacedKey.contains(":"))
				adv.namespacedKey = Main.plugin.getName() + ":" + adv.namespacedKey;
			adv.namespacedKey = adv.namespacedKey.toLowerCase();
			edytor.ścieżka = adv.namespacedKey;
		});
		edytor.zarejestrujPoZatwierdz(Main::reloadBukkitData);
	}
	
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void osiągnięcia(PlayerAdvancementDoneEvent ev) {
		String klucz = ev.getAdvancement().getKey().getKey() + ":" + ev.getAdvancement().getKey().getNamespace();
		Func.wykonajDlaNieNull(Osiągnięcie.mapa.get(ev.getAdvancement().getKey()), adv -> {
			Gracz g = Gracz.wczytaj(ev.getPlayer());
			g.osiągnięciaUkończoneKryteria.remove(klucz);
			g.zapisz();
			
			adv.nagroda.forEach(item -> Func.dajItem(ev.getPlayer(), item.clone()));
			ev.getPlayer().giveExp(adv.exp);
		});
	}
	
	
	// mob/blok: [(osiągnięcie, indexKryterium)]
	final Map<EntityType, List<Krotka<Osiągnięcie, Integer>>> mapaMobów	= new HashMap<>(); 
	final Map<Material, List<Krotka<Osiągnięcie, Integer>>> mapaBloków	= new HashMap<>();
	final List<Krotka<SelektorItemów, Krotka<Osiągnięcie, Integer>>> listaSelektorów = Lists.newArrayList(); 

	@EventHandler(priority = EventPriority.MONITOR)
	public void zabicieMoba(EntityDeathEvent ev) {
		Func.wykonajDlaNieNull(ev.getEntity().getKiller(), p ->
			Func.wykonajDlaNieNull(mapaMobów.get(ev.getEntity().getType()), lista ->
				lista.forEach(krotka -> {
					if (	!p.getAdvancementProgress(krotka.a.adv).isDone() &&
							p.getStatistic(Statistic.KILL_ENTITY, ev.getEntity().getType()) >= krotka.a.kryteria.get(krotka.b).ile)
						krotka.a.odznacz(p, krotka.b);
				})));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void wykopanieBloku(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		Player p = ev.getPlayer();
		Func.wykonajDlaNieNull(mapaBloków.get(ev.getBlock().getType()), lista ->
			lista.forEach(krotka -> {
				if (	!p.getAdvancementProgress(krotka.a.adv).isDone() &&
						p.getStatistic(Statistic.MINE_BLOCK, ev.getBlock().getType()) >= krotka.a.kryteria.get(krotka.b).ile)
					krotka.a.odznacz(p, krotka.b);
		}));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (ev.getEntityType() != EntityType.PLAYER) return;
		if (ev.isCancelled()) return;
		
		Player p = (Player) ev.getEntity();
		
		ItemStack item = ev.getItem().getItemStack();
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			listaSelektorów.forEach(krotka -> {
				if (	!p.getAdvancementProgress(krotka.b.a.adv).isDone() &&
						p.getInventory().containsAtLeast(item, krotka.b.a.kryteria.get(krotka.b.b).ile))
					krotka.b.a.odznacz(p, krotka.b.b);
			});
		});
	}
	
	
	
	// Override
	
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
		Config config = new Config("configi/Customowe Osiągnięcia");
		
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
				
				Map<?, List<Krotka<Osiągnięcie, Integer>>> mapa = null;
				
				if (kryterium.co instanceof Material)
					mapa = mapaBloków;
				else if (kryterium.co instanceof EntityType)
					mapa = mapaMobów;
				else if (kryterium.co instanceof SelektorItemów)
					listaSelektorów.add(new Krotka<>(kryterium.czego(), krotka));
				
				if (mapa != null)
					if (mapa.containsKey(kryterium.czego()))
						mapa.get(kryterium.czego()).add(krotka);
					else
						mapa.put(kryterium.czego(), Lists.newArrayList(krotka));
			}
		});
		
		AdvancementDataWorld dataWorld = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData();
		Map<MinecraftKey, Advancement> advs = dataWorld.REGISTRY.advancements;
		
		Main.ust.wczytajListe("Zablokowane Osiągnięcia").forEach(pattern -> {
			Pattern pat = Pattern.compile(pattern);
			Set<MinecraftKey> doUsunięcia = Sets.newConcurrentHashSet();
			advs.forEach((klucz, adv) -> {
				if (pat.matcher(klucz.toString()).matches())
					doUsunięcia.add(klucz);
			});
			doUsunięcia.forEach(advs::remove);
		});
		preReload.forEach((nick, mapa) ->
				Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> {
					AdvancementDataPlayer data = ((CraftPlayer) p).getHandle().getAdvancementData();
					mapa.forEach((klucz, prog) ->
					Func.wykonajDlaNieNull(advs.get(klucz), adv -> {
						try {
							Method met = data.getClass().getDeclaredMethod("a", Advancement.class, AdvancementProgress.class);
							met.setAccessible(true);
							met.invoke(data, adv, prog);
							
							met = data.getClass().getDeclaredMethod("e", Advancement.class);
							met.setAccessible(true);
							met.invoke(data, adv);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}));
				}));
		preReload.clear();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane osiągnięcia", Osiągnięcie.mapa.size());
	}


	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("edytor")) {
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, "/edytujosiągnięcia edytor -t <nazwa osiągnięcia>");
			else if (args.length >= 2 && args[1].equals("-t")) {
				if (!args[2].contains(":"))
					args[2] = Main.plugin.getName() + ":" + args[2];
				args[2] = args[2].toLowerCase();
				args[2] = "configi/Customowe Osiągnięcia|" + args[2];
			}
			return edytor.onCommand(sender, "edytujosiągnięcia", args);
		}
		return false;
	}
}
