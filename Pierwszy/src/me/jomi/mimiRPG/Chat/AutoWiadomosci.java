package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Prze�adowalny;
import me.jomi.mimiRPG.Zegar;

public class AutoWiadomosci implements Prze�adowalny, Zegar {
	private static List<Napis> wiadomo�ci = Lists.newArrayList();
	private static List<Napis> doWys�ania = Lists.newArrayList();
	private static int timer = 0;
	private static Napis ostatnia = null;
	
	public int czas() {
		if (timer >= 0)
			wy�lij();
		return timer;
	}
	
	public void prze�aduj() { 
		wiadomo�ci.clear();
		doWys�ania.clear();
		wiadomo�ci = Main.ust.wczytajListeNapis�w("AutoWiadomosci", "wiadomo�ci");
		doWys�ania = Lists.newArrayList(wiadomo�ci);
		timer = Main.ust.wczytajInt("AutoWiadomosci", "czas") * 20;
	}
	public String raport() {
		return "�6Atomatyczne Wiadomo�ci: �e" + wiadomo�ci.size();
	}
	
	public void wy�lij() {
		if (wiadomo�ci.size() <= 0) return;
		if (Bukkit.getOnlinePlayers().size() <= 0) return;
		int i;
		do i = Func.losuj(0, doWys�ania.size()-1); 
			while(doWys�ania.size() > 1 && doWys�ania.get(i).equals(ostatnia));
		ostatnia = doWys�ania.get(i);
		ostatnia.wy�wietlWszystkim();
		doWys�ania.remove(i);
		if (doWys�ania.isEmpty())
			doWys�ania = Lists.newArrayList(wiadomo�ci);
	}

}
