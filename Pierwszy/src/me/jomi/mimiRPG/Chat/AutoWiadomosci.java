package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Prze³adowalny;
import me.jomi.mimiRPG.Zegar;

public class AutoWiadomosci implements Prze³adowalny, Zegar {
	private static List<Napis> wiadomoœci = Lists.newArrayList();
	private static List<Napis> doWys³ania = Lists.newArrayList();
	private static int timer = 0;
	private static Napis ostatnia = null;
	
	public int czas() {
		if (timer >= 0)
			wyœlij();
		return timer;
	}
	
	public void prze³aduj() { 
		wiadomoœci.clear();
		doWys³ania.clear();
		wiadomoœci = Main.ust.wczytajListeNapisów("AutoWiadomosci", "wiadomoœci");
		doWys³ania = Lists.newArrayList(wiadomoœci);
		timer = Main.ust.wczytajInt("AutoWiadomosci", "czas") * 20;
	}
	public String raport() {
		return "§6Atomatyczne Wiadomoœci: §e" + wiadomoœci.size();
	}
	
	public void wyœlij() {
		if (wiadomoœci.size() <= 0) return;
		if (Bukkit.getOnlinePlayers().size() <= 0) return;
		int i;
		do i = Func.losuj(0, doWys³ania.size()-1); 
			while(doWys³ania.size() > 1 && doWys³ania.get(i).equals(ostatnia));
		ostatnia = doWys³ania.get(i);
		ostatnia.wyœwietlWszystkim();
		doWys³ania.remove(i);
		if (doWys³ania.isEmpty())
			doWys³ania = Lists.newArrayList(wiadomoœci);
	}

}
