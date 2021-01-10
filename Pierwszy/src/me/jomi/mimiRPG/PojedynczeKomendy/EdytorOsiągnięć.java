package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.minecraft.server.v1_16_R2.Advancement;
import net.minecraft.server.v1_16_R2.AdvancementDisplay;
import net.minecraft.server.v1_16_R2.AdvancementFrameType;
import net.minecraft.server.v1_16_R2.AdvancementRewards;
import net.minecraft.server.v1_16_R2.Criterion;
import net.minecraft.server.v1_16_R2.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R2.CustomFunction;
import net.minecraft.server.v1_16_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R2.MinecraftKey;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.SelektorItemów;

@Moduł
public class EdytorOsiągnięć implements Listener, Przeładowalny {
	abstract static class Kryterium extends Mapowany {
		@Mapowane String nazwa;
		@Mapowane int ile;

		void Init() {
			if (nazwa == null)
				nazwa = Func.losujZnaki(10, 20, "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890");
			else if (nazwa.contains(":") || nazwa.contains(","))
				throw new Error("Nazwa kryterium osiągnięcia nie może zawierać znaków \":\" i \",\"");
		}
		
		abstract <T> T czego();
	}
	static class KryteriumWykop extends Kryterium {
		@SuppressWarnings("unchecked") @Override <T> T czego() { return (T) blok; }
		@Mapowane Material blok;
	}
	static class KryteriumZabij extends Kryterium {
		@SuppressWarnings("unchecked") @Override <T> T czego() { return (T) mob; }
		@Mapowane EntityType mob;
	}
	static class KryteriumZdobądz extends Kryterium {
		@SuppressWarnings("unchecked") @Override <T> T czego() { return (T) item; }
		@Mapowane SelektorItemów item;
		@Mapowane boolean zabierz;
	}
	
	static class Osiągnięcie extends Mapowany {
		final static HashMap<NamespacedKey, Osiągnięcie> mapa = new HashMap<>();
		@Mapowane String namespacedKey;
		@Mapowane List<ItemStack> nagroda;
		@Mapowane List<Kryterium> kryteria;// TODO zapewnić unikalne nazwy kryteriów w obrębie osiągnięcia

		@Mapowane ItemStack ikona;
		@Mapowane String nazwa;
		@Mapowane String opis;
		@Mapowane AdvancementFrameType ramka;
		@Mapowane boolean show_toast;
		@Mapowane boolean announce_to_chat;
		@Mapowane boolean hidden;
		
		@Mapowane String tło;
		@Mapowane String parent;
		
		NamespacedKey klucz;
		org.bukkit.advancement.Advancement adv;
		
		
		void Init() {
			klucz = CraftNamespacedKey.fromString(namespacedKey);
			nazwa = Func.koloruj(nazwa);
			opis = Func.koloruj(opis);
		}

		boolean stworzone = false;
		void stwórz() {
			if (stworzone)
				return;
			Advancement adv = null;
			if (this.parent != null) {
				NamespacedKey parent = CraftNamespacedKey.fromString(this.parent);
				mapa.get(parent).stwórz();
				adv = ((CraftAdvancement) Bukkit.getAdvancement(parent)).getHandle();
			}
			stworzone = true;
			this.adv = stwórzNowe(ikona, nazwa, opis, ramka, adv, tło, show_toast, announce_to_chat, hidden).bukkit;
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
		static final HashMap<String, Criterion> mapa = new HashMap<>();
		static {
			CustomFunction func = new CustomFunction(new MinecraftKey(Main.plugin.getName().toLowerCase(), "c"), new CustomFunction.c[0]);
			// new AdvancementRewards(exp, loot, recipes, function)
			reward = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], new CustomFunction.a(func));
			mapa.put("i", new Criterion(new CriterionTriggerImpossible.a()));
			strs[0][0] = "i";
		}
	}
	static Advancement stwórzNowe(ItemStack ikona, String nazwa, String opis, AdvancementFrameType ramka,
			boolean show_toast, boolean announce_to_chat, boolean hidden) {
		return stwórzNowe(ikona, nazwa, opis, ramka, null, null, show_toast, announce_to_chat, hidden);
	}
	@SuppressWarnings("resource")
	static Advancement stwórzNowe(ItemStack ikona, String nazwa, String opis, AdvancementFrameType ramka, Advancement parent, String tło,
			boolean show_toast, boolean announce_to_chat, boolean hidden) {
		AdvancementDisplay display = new AdvancementDisplay(
				CraftItemStack.asNMSCopy(ikona),
				ChatSerializer.a("{\"text\":\""+nazwa+"\"}"),
				ChatSerializer.a("{\"text\":\""+opis+"\"}"),
				new MinecraftKey("minecraft", tło == null ? "textures/block/light_blue_concrete.png" : tło),
				ramka,
				show_toast,
				announce_to_chat,
				hidden);
		
		MinecraftKey key = new MinecraftKey(Main.plugin.getName().toLowerCase(), parent == null ? "root" : nazwa);
		
		Advancement adv2 = new Advancement(key, parent, display, AdvStałe.reward, AdvStałe.mapa, AdvStałe.strs);
		
		((CraftServer) Bukkit.getServer()).getHandle().getServer().getAdvancementData().REGISTRY.advancements.put(key, adv2);
		
		return adv2;
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void osiągnięcia(PlayerAdvancementDoneEvent ev) {
		String klucz = ev.getAdvancement().getKey().getKey() + ":" + ev.getAdvancement().getKey().getNamespace();
		Func.wykonajDlaNieNull(Osiągnięcie.mapa.get(ev.getAdvancement().getKey()), adv -> {
			Gracz g = Gracz.wczytaj(ev.getPlayer());
			g.osiągnięciaUkończoneKryteria.remove(klucz);
			g.osiągnięciaPostęp.remove(klucz);
			g.zapisz();
			
			adv.nagroda.forEach(item -> Func.dajItem(ev.getPlayer(), item.clone()));
		});
	}
	
	
	// mob/blok: [(osiągnięcie, indexKryterium)]
	final HashMap<EntityType, List<Krotka<Osiągnięcie, Integer>>> mapaMobów = new HashMap<>(); 
	final HashMap<Material, List<Krotka<Osiągnięcie, Integer>>> mapaBloków = new HashMap<>(); 

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
	
	@Override
	public void przeładuj() {
		Config config = new Config("configi/Customowe Osiągnięcia");
		
		Osiągnięcie.mapa.clear();
		
		config.wartości(Osiągnięcie.class).forEach(adv -> Osiągnięcie.mapa.put(adv.klucz, adv));
		
		Osiągnięcie.mapa.forEach((klucz, adv) -> adv.stwórz());
		
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane osiągnięcia", Osiągnięcie.mapa.size());
	}
}
