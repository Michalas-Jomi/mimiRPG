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
import me.jomi.mimiRPG.Prze�adowalny;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import me.jomi.mimiRPG.MiniGierki.MiniGra;

public class Osi�gni�cia implements Listener, Prze�adowalny{
	public static final String prefix = Func.prefix("Osi�gni�cia");
	private static Config config = new Config("osi�gni�cia");
	
	public static HashMap<String, List<Osi�gni�ciaInst>> mapa = new HashMap<>();
	
	public void prze�aduj() {
		config.prze�aduj();
		mapa.clear();
		
		List<String> wczytane = Lists.newArrayList();
		for (String klucz : config.klucze(false)) {
			Osi�gni�ciaInst inst = new Osi�gni�ciaInst(klucz, config.wczytajItemy(klucz, "nagrody"));
			wczytane.add(inst.nazwaPliku);
		}
		Iterator<Advancement> iterator = Bukkit.advancementIterator();
		while (iterator.hasNext()) {
			Advancement adv = iterator.next();
			String klucz = adv.getKey().getKey();
			if (adv.getKey().getNamespace().equals("skyblock"))
				if (!wczytane.contains(klucz))
					Main.plugin.getLogger().warning(prefix + "�cNie odnaleziono w osi�gni�cia.yml osi�gni�cia �4" + klucz);
		}
	}
	public String raport() {
		int ile = 0;
		for (List<Osi�gni�ciaInst> lista : mapa.values())
			ile += lista.size();
		return "�6Osi�gni�cia: �e" + ile;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void stawianieBlok�w(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		Player p = ev.getPlayer();
		p.incrementStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
		spe�nij(p, "USE_ITEM", "" + ev.getBlock().getType());
		spe�nij(p, "USE_ITEM", "" + ev.getItemInHand().getType());
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void niszczenieBlok�w(BlockBreakEvent ev) {
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
				spe�nij(p, "MINE_BLOCK", "CROPS");
				break;
			case NETHER_WART:
			case BEETROOTS:
				if (b.getBlockData().getAsString().contains("age=3"))
					p.incrementStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
				spe�nij(p, "MINE_BLOCK", "CROPS");
				break;
			default:
				p.incrementStatistic(Statistic.MINE_BLOCK, Material.BRICK);
				break;
			}
		spe�nij(p, "MINE_BLOCK", "" + ev.getBlock().getType(), ev.getBlock());

	}
	@EventHandler
	public void zabijanie(EntityDeathEvent ev) {
		Player p = ev.getEntity().getKiller();
		if (p == null) return;
		if (ev.getEntityType().equals(EntityType.PLAYER))
			spe�nij(p, "PLAYER_KILLS", "" + ev.getEntityType());
		else {
			spe�nij(p, "KILL_ENTITY", "" + ev.getEntityType());
			spe�nij(p, "MOB_KILLS", "" + ev.getEntityType());
		}
	}
	private void spe�nij(Player p, String czynno��, String warto��, Block b) {
		List<Osi�gni�ciaInst> lista = mapa.get(czynno��);
		if (lista != null)
			for (Osi�gni�ciaInst osi�gni�cie : lista)
				osi�gni�cie.spe�nij(p, warto��);
	}
	private void spe�nij(Player p, String czynno��, String warto��) {
		List<Osi�gni�ciaInst> lista = mapa.get(czynno��);
		if (lista != null)
			for (Osi�gni�ciaInst osi�gni�cie : lista)
				osi�gni�cie.spe�nij(p, warto��);
	}
	@EventHandler
	public void zdobycieOsi�gni�cia(PlayerAdvancementDoneEvent ev) {
		Advancement adv = ev.getAdvancement();
		if (!adv.getKey().getNamespace().equals("skyblock"))
			return;
		String nazwa = adv.getKey().getKey();
		String klucz = ((String) adv.getCriteria().toArray()[0]).split(" ")[0].replace("x", "_");
		if (klucz == null || !mapa.containsKey(klucz)) return;
		for (Osi�gni�ciaInst osi�gni�cie : mapa.get(klucz))
			if (osi�gni�cie.nazwaPliku.equals(nazwa)) {
				osi�gni�cie.nagr�dz(ev.getPlayer());
				break;
			}
		}
}

class Osi�gni�ciaInst {
	public static final String prefix = Osi�gni�cia.prefix;
	public String nazwaPliku;
	public String kryterium;
	public List<ItemStack> nagrody;
	
	public String czynno��;
	public String warto��;
	public int ilo��;
	
	public Advancement adv;
	
	public String toString() {
		return String.format("AdvInst(plik=%s, czynno��=%s, warto��=%s)", nazwaPliku, czynno��, warto��);
	}
	
	@SuppressWarnings("deprecation")
	public Osi�gni�ciaInst(String nazwaPliku, List<ItemStack> nagrody) {
		this.nagrody = nagrody;
		this.nazwaPliku = nazwaPliku;

		adv = Bukkit.getAdvancement(new NamespacedKey("skyblock", nazwaPliku));
		if (adv == null) {
			Main.plugin.getLogger().warning(prefix + "Niepoprawny plik osi�gni�cia.yml, osi�gni�cie " + nazwaPliku);
			return;
		}
		kryterium = ((String) adv.getCriteria().toArray()[0]);
		
		String[] t = kryterium.replace("x", "_").split(" ");
		if (t.length < 2) {
			Main.plugin.getLogger().warning("niepoprawne kryterium \"" + kryterium + "\" w osi�gni�cia.yml, osi�gni�cie " + nazwaPliku);
			return;
		}
		czynno�� = t[0];
		int i = 1;
		if (t.length >= 3)
			warto�� = t[i++];
		ilo�� = EdytujItem.sprawdz_liczbe(t[i], -1);
		if (ilo�� <= -1)
			Main.plugin.getLogger().warning("niepoprawny plik osi�gni�cia.yml, osi�gni�cie " + nazwaPliku);
		if (Osi�gni�cia.mapa.get(czynno��) == null)
			Osi�gni�cia.mapa.put(czynno��, Lists.newArrayList());
		Osi�gni�cia.mapa.get(czynno��).add(this);
	}

	public void spe�nij(Player p, String Warto��) {
		if (p.getAdvancementProgress(adv).isDone() || (warto�� != null && !warto��.equals(Warto��))) return;
		int ile;
		if (warto�� != null)
			switch(czynno��) {
			case "MINE_BLOCK":
				if (warto��.equals("CROPS")) {
					ile = p.getStatistic(Statistic.MINE_BLOCK, Material.WHEAT);
					break;
				}
			case "USE_ITEM":
				ile = p.getStatistic(Statistic.valueOf(czynno��), Material.valueOf(warto��));
				break;
			case "KILL_ENTITY":
				ile = p.getStatistic(Statistic.valueOf(czynno��), EntityType.valueOf(warto��));
				break;
			default:
				MiniGra.powiadomOp("Niepoprawna czynno�� w pliku osi�gni�cia.yml: " + czynno��);
				ile = 0;
				break;
			}
		else {
			if (czynno��.equals("MINE_BLOCK"))
				ile = p.getStatistic(Statistic.MINE_BLOCK, Material.BRICK);
			else if (czynno��.equals("USE_ITEM"))
				ile = p.getStatistic(Statistic.USE_ITEM, Material.MAGMA_CREAM);
			else
				ile = p.getStatistic(Statistic.valueOf(czynno��));
		}
		if (ile >= ilo��)
			p.getAdvancementProgress(adv).awardCriteria(kryterium);
		
	}
	public void nagr�dz(Player p) {
		for (ItemStack item : nagrody)
			Func.dajItem(p, item);
		Napis n = new Napis("�7Gracz �e" + p.getDisplayName() + "�7 uko�czy� �a");
		n.dodaj(Napis.osi�gni�cie("skyblock", nazwaPliku));
		for (Player gracz : Bukkit.getOnlinePlayers())
			n.wy�wietl(gracz);
		Bukkit.getConsoleSender().sendMessage("" + n);
	}
}
