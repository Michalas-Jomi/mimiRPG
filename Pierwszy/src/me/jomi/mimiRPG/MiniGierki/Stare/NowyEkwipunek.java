package me.jomi.mimiRPG.MiniGierki.Stare;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;


public class NowyEkwipunek {
	
	private static HashMap<String, List<ItemStack>> mapa = new HashMap<>();
	public  static HashMap<String, Integer> mapaExpa = new HashMap<>();
	
	
	public static void dajNowy(Player p) {
		Inventory inv = p.getInventory();
		String nick = p.getName();

		if (mapa.containsKey(nick)) 
			wczytajStary(p);
		
		
		mapa.put(nick, Lists.newArrayList());
		for (int i=0; i<9*4+5; i++) 
			mapa.get(nick).add(inv.getItem(i));
		
		mapaExpa.put(nick, Poziom.policzCałyExp(p));
		
		
		inv.clear();
		p.setExp(0);
		p.setLevel(0);
	}
	
	public static void wczytajStary(Player p) {
		Inventory inv = p.getInventory();
		String nick = p.getName();

		if (!mapa.containsKey(nick)) 
			return;
		
		
		inv.clear();
		List<ItemStack> lista = mapa.get(nick);
		for (int i=0; i<9*4+5; i++)
			inv.setItem(i, lista.get(i));
		
		p.giveExp(mapaExpa.get(nick));
		
		
		mapa.remove(nick);
		mapaExpa.remove(nick);
	}
}
