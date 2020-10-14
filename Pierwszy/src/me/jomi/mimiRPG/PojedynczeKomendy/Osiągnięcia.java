package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Przeładowalny;

@Moduł
public class Osiągnięcia implements Listener, Przeładowalny{
	public static final String prefix = Func.prefix("Osiągnięcia");
	private static Config config = new Config("osiągnięcia");
	
	public static HashMap<String, List<OsiągnięciaInst>> mapa = new HashMap<>();
	
	public void przeładuj() {
		config.przeładuj();
		mapa.clear();
		
		List<String> wczytane = Lists.newArrayList();
		for (String klucz : config.klucze(false)) {
			OsiągnięciaInst inst = new OsiągnięciaInst(klucz, config.wczytajItemy(klucz, "nagrody"));
			wczytane.add(inst.nazwaPliku);
		}
		Iterator<Advancement> iterator = Bukkit.advancementIterator();
		while (iterator.hasNext()) {
			Advancement adv = iterator.next();
			String klucz = adv.getKey().getKey();
			if (adv.getKey().getNamespace().equals("skyblock"))
				if (!wczytane.contains(klucz))
					Main.plugin.getLogger().warning(prefix + "§cNie odnaleziono w osiągnięcia.yml osiągnięcia §4" + klucz);
		}
	}
	public String raport() {
		int ile = 0;
		for (List<OsiągnięciaInst> lista : mapa.values())
			ile += lista.size();
		return "§6Osiągnięcia: §e" + ile;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void stawianieBloków(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		Player p = ev.getPlayer();
		p.incrementStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
		spełnij(p, "USE_ITEM", "" + ev.getBlock().getType());
		spełnij(p, "USE_ITEM", "" + ev.getItemInHand().getType());
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void niszczenieBloków(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		Player p = ev.getPlayer();
		Block b = ev.getBlock();
		if  (b != null)
			switch (b.getType()) {
			case WHEAT:
			case POTATOES:
			case CARROTS:
				if (b.getBlockData().getAsString().contains("age=7"))
					p.incrementStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
				spełnij(p, "MINE_BLOCK", "CROPS");
				break;
			case NETHER_WART:
			case BEETROOTS:
				if (b.getBlockData().getAsString().contains("age=3"))
					p.incrementStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
				spełnij(p, "MINE_BLOCK", "CROPS");
				break;
			default:
				p.incrementStatistic(Statistic.MINE_BLOCK, Material.BRICK);
				break;
			}
		spełnij(p, "MINE_BLOCK", "" + ev.getBlock().getType(), ev.getBlock());

	}
	@EventHandler
	public void zabijanie(EntityDeathEvent ev) {
		Player p = ev.getEntity().getKiller();
		if (p == null) return;
		if (ev.getEntityType().equals(EntityType.PLAYER))
			spełnij(p, "PLAYER_KILLS", "" + ev.getEntityType());
		else {
			spełnij(p, "KILL_ENTITY", "" + ev.getEntityType());
			spełnij(p, "MOB_KILLS", "" + ev.getEntityType());
		}
	}
	private void spełnij(Player p, String czynność, String wartość, Block b) {
		List<OsiągnięciaInst> lista = mapa.get(czynność);
		if (lista != null)
			for (OsiągnięciaInst osiągnięcie : lista)
				osiągnięcie.spełnij(p, wartość);
	}
	private void spełnij(Player p, String czynność, String wartość) {
		List<OsiągnięciaInst> lista = mapa.get(czynność);
		if (lista != null)
			for (OsiągnięciaInst osiągnięcie : lista)
				osiągnięcie.spełnij(p, wartość);
	}
	@EventHandler
	public void zdobycieOsiągnięcia(PlayerAdvancementDoneEvent ev) {
		Advancement adv = ev.getAdvancement();
		if (!adv.getKey().getNamespace().equals("skyblock"))
			return;
		String nazwa = adv.getKey().getKey();
		String klucz = ((String) adv.getCriteria().toArray()[0]).split(" ")[0].replace("x", "_");
		if (klucz == null || !mapa.containsKey(klucz)) return;
		for (OsiągnięciaInst osiągnięcie : mapa.get(klucz))
			if (osiągnięcie.nazwaPliku.equals(nazwa)) {
				osiągnięcie.nagródz(ev.getPlayer());
				break;
			}
		}
}

class OsiągnięciaInst {
	public static final String prefix = Osiągnięcia.prefix;
	public String nazwaPliku;
	public String kryterium;
	public List<ItemStack> nagrody;
	
	public String czynność;
	public String wartość;
	public int ilość;
	
	public Advancement adv;
	
	public String toString() {
		return String.format("AdvInst(plik=%s, czynność=%s, wartość=%s)", nazwaPliku, czynność, wartość);
	}
	
	@SuppressWarnings("deprecation")
	public OsiągnięciaInst(String nazwaPliku, List<ItemStack> nagrody) {
		this.nagrody = nagrody;
		this.nazwaPliku = nazwaPliku;

		adv = Bukkit.getAdvancement(new NamespacedKey("skyblock", nazwaPliku));
		if (adv == null) {
			Main.plugin.getLogger().warning(prefix + "Niepoprawny plik osiągnięcia.yml, osiągnięcie " + nazwaPliku);
			return;
		}
		kryterium = ((String) adv.getCriteria().toArray()[0]);
		
		String[] t = kryterium.replace("x", "_").split(" ");
		if (t.length < 2) {
			Main.plugin.getLogger().warning("niepoprawne kryterium \"" + kryterium + "\" w osiągnięcia.yml, osiągnięcie " + nazwaPliku);
			return;
		}
		czynność = t[0];
		int i = 1;
		if (t.length >= 3)
			wartość = t[i++];
		ilość = Func.Int(t[i], -1);
		if (ilość <= -1)
			Main.plugin.getLogger().warning("niepoprawny plik osiągnięcia.yml, osiągnięcie " + nazwaPliku);
		if (Osiągnięcia.mapa.get(czynność) == null)
			Osiągnięcia.mapa.put(czynność, Lists.newArrayList());
		Osiągnięcia.mapa.get(czynność).add(this);
	}

	public void spełnij(Player p, String Wartość) {
		if (p.getAdvancementProgress(adv).isDone() || (wartość != null && !wartość.equals(Wartość))) return;
		int ile;
		if (wartość != null)
			switch(czynność) {
			case "MINE_BLOCK":
				if (wartość.equals("CROPS")) {
					ile = p.getStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
					break;
				}
			case "USE_ITEM":
				ile = p.getStatistic(Statistic.valueOf(czynność), Material.valueOf(wartość));
				break;
			case "KILL_ENTITY":
				ile = p.getStatistic(Statistic.valueOf(czynność), EntityType.valueOf(wartość));
				break;
			default:
				ile = 0;
				break;
			}
		else {
			if (czynność.equals("MINE_BLOCK"))
				ile = p.getStatistic(Statistic.MINE_BLOCK, Material.BRICK);
			else if (czynność.equals("USE_ITEM"))
				ile = p.getStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
			else
				ile = p.getStatistic(Statistic.valueOf(czynność));
		}
		if (ile >= ilość)
			p.getAdvancementProgress(adv).awardCriteria(kryterium);
		
	}
	public void nagródz(Player p) {
		for (ItemStack item : nagrody)
			Func.dajItem(p, item);
		Napis n = new Napis("§7Gracz §e" + p.getDisplayName() + "§7 ukończył §a");
		n.dodaj(Napis.osiągnięcie("skyblock", nazwaPliku));
		for (Player gracz : Bukkit.getOnlinePlayers())
			n.wyświetl(gracz);
		Bukkit.getConsoleSender().sendMessage("" + n);
	}
}
