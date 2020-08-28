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
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Prze³adowalny;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import me.jomi.mimiRPG.MiniGierki.MiniGra;

public class Osi¹gniêcia implements Listener, Prze³adowalny{
	public static final String prefix = Func.prefix("Osi¹gniêcia");
	private static Config config = new Config("osi¹gniêcia");
	
	public static HashMap<String, List<Osi¹gniêciaInst>> mapa = new HashMap<>();
	
	public void prze³aduj() {
		config.prze³aduj();
		mapa.clear();
		
		List<String> wczytane = Lists.newArrayList();
		for (String klucz : config.klucze(false)) {
			Osi¹gniêciaInst inst = new Osi¹gniêciaInst(klucz, config.wczytajItemy(klucz, "nagrody"));
			wczytane.add(inst.nazwaPliku);
		}
		Iterator<Advancement> iterator = Bukkit.advancementIterator();
		while (iterator.hasNext()) {
			Advancement adv = iterator.next();
			String klucz = adv.getKey().getKey();
			if (adv.getKey().getNamespace().equals("skyblock"))
				if (!wczytane.contains(klucz))
					Main.plugin.getLogger().warning(prefix + "§cNie odnaleziono w osi¹gniêcia.yml osi¹gniêcia §4" + klucz);
		}
	}
	public String raport() {
		int ile = 0;
		for (List<Osi¹gniêciaInst> lista : mapa.values())
			ile += lista.size();
		return "§6Osi¹gniêcia: §e" + ile;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void stawianieBloków(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		Player p = ev.getPlayer();
		p.incrementStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
		spe³nij(p, "USE_ITEM", "" + ev.getBlock().getType());
		spe³nij(p, "USE_ITEM", "" + ev.getItemInHand().getType());
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
				spe³nij(p, "MINE_BLOCK", "CROPS");
				break;
			case NETHER_WART:
			case BEETROOTS:
				if (b.getBlockData().getAsString().contains("age=3"))
					p.incrementStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
				spe³nij(p, "MINE_BLOCK", "CROPS");
				break;
			default:
				p.incrementStatistic(Statistic.MINE_BLOCK, Material.BRICK);
				break;
			}
		spe³nij(p, "MINE_BLOCK", "" + ev.getBlock().getType(), ev.getBlock());

	}
	@EventHandler
	public void zabijanie(EntityDeathEvent ev) {
		Player p = ev.getEntity().getKiller();
		if (p == null) return;
		if (ev.getEntityType().equals(EntityType.PLAYER))
			spe³nij(p, "PLAYER_KILLS", "" + ev.getEntityType());
		else {
			spe³nij(p, "KILL_ENTITY", "" + ev.getEntityType());
			spe³nij(p, "MOB_KILLS", "" + ev.getEntityType());
		}
	}
	private void spe³nij(Player p, String czynnoœæ, String wartoœæ, Block b) {
		List<Osi¹gniêciaInst> lista = mapa.get(czynnoœæ);
		if (lista != null)
			for (Osi¹gniêciaInst osi¹gniêcie : lista)
				osi¹gniêcie.spe³nij(p, wartoœæ);
	}
	private void spe³nij(Player p, String czynnoœæ, String wartoœæ) {
		List<Osi¹gniêciaInst> lista = mapa.get(czynnoœæ);
		if (lista != null)
			for (Osi¹gniêciaInst osi¹gniêcie : lista)
				osi¹gniêcie.spe³nij(p, wartoœæ);
	}
	@EventHandler
	public void zdobycieOsi¹gniêcia(PlayerAdvancementDoneEvent ev) {
		Advancement adv = ev.getAdvancement();
		if (!adv.getKey().getNamespace().equals("skyblock"))
			return;
		String nazwa = adv.getKey().getKey();
		String klucz = ((String) adv.getCriteria().toArray()[0]).split(" ")[0].replace("x", "_");
		if (klucz == null || !mapa.containsKey(klucz)) return;
		for (Osi¹gniêciaInst osi¹gniêcie : mapa.get(klucz))
			if (osi¹gniêcie.nazwaPliku.equals(nazwa)) {
				osi¹gniêcie.nagródz(ev.getPlayer());
				break;
			}
		}
}

class Osi¹gniêciaInst {
	public static final String prefix = Osi¹gniêcia.prefix;
	public String nazwaPliku;
	public String kryterium;
	public List<ItemStack> nagrody;
	
	public String czynnoœæ;
	public String wartoœæ;
	public int iloœæ;
	
	public Advancement adv;
	
	public String toString() {
		return String.format("AdvInst(plik=%s, czynnoœæ=%s, wartoœæ=%s)", nazwaPliku, czynnoœæ, wartoœæ);
	}
	
	@SuppressWarnings("deprecation")
	public Osi¹gniêciaInst(String nazwaPliku, List<ItemStack> nagrody) {
		this.nagrody = nagrody;
		this.nazwaPliku = nazwaPliku;

		adv = Bukkit.getAdvancement(new NamespacedKey("skyblock", nazwaPliku));
		if (adv == null) {
			Main.plugin.getLogger().warning(prefix + "Niepoprawny plik osi¹gniêcia.yml, osi¹gniêcie " + nazwaPliku);
			return;
		}
		kryterium = ((String) adv.getCriteria().toArray()[0]);
		
		String[] t = kryterium.replace("x", "_").split(" ");
		if (t.length < 2) {
			Main.plugin.getLogger().warning("niepoprawne kryterium \"" + kryterium + "\" w osi¹gniêcia.yml, osi¹gniêcie " + nazwaPliku);
			return;
		}
		czynnoœæ = t[0];
		int i = 1;
		if (t.length >= 3)
			wartoœæ = t[i++];
		iloœæ = EdytujItem.sprawdz_liczbe(t[i], -1);
		if (iloœæ <= -1)
			Main.plugin.getLogger().warning("niepoprawny plik osi¹gniêcia.yml, osi¹gniêcie " + nazwaPliku);
		if (Osi¹gniêcia.mapa.get(czynnoœæ) == null)
			Osi¹gniêcia.mapa.put(czynnoœæ, Lists.newArrayList());
		Osi¹gniêcia.mapa.get(czynnoœæ).add(this);
	}

	public void spe³nij(Player p, String Wartoœæ) {
		if (p.getAdvancementProgress(adv).isDone() || (wartoœæ != null && !wartoœæ.equals(Wartoœæ))) return;
		int ile;
		if (wartoœæ != null)
			switch(czynnoœæ) {
			case "MINE_BLOCK":
				if (wartoœæ.equals("CROPS")) {
					ile = p.getStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
					break;
				}
			case "USE_ITEM":
				ile = p.getStatistic(Statistic.valueOf(czynnoœæ), Material.valueOf(wartoœæ));
				break;
			case "KILL_ENTITY":
				ile = p.getStatistic(Statistic.valueOf(czynnoœæ), EntityType.valueOf(wartoœæ));
				break;
			default:
				MiniGra.powiadomOp("Niepoprawna czynnoœæ w pliku osi¹gniêcia.yml: " + czynnoœæ);
				ile = 0;
				break;
			}
		else {
			if (czynnoœæ.equals("MINE_BLOCK"))
				ile = p.getStatistic(Statistic.MINE_BLOCK, Material.BRICK);
			else if (czynnoœæ.equals("USE_ITEM"))
				ile = p.getStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
			else
				ile = p.getStatistic(Statistic.valueOf(czynnoœæ));
		}
		if (ile >= iloœæ)
			p.getAdvancementProgress(adv).awardCriteria(kryterium);
		
	}
	public void nagródz(Player p) {
		for (ItemStack item : nagrody)
			Func.dajItem(p, item);
		Napis n = new Napis("§7Gracz §e" + p.getDisplayName() + "§7 ukoñczy³ §a");
		n.dodaj(Napis.osi¹gniêcie("skyblock", nazwaPliku));
		for (Player gracz : Bukkit.getOnlinePlayers())
			n.wyœwietl(gracz);
		Bukkit.getConsoleSender().sendMessage("" + n);
	}
}
