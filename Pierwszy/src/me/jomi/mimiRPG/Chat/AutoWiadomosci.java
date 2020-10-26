package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class AutoWiadomosci implements Przeładowalny, Zegar {
	private List<Napis> wiadomości = Lists.newArrayList();
	private List<Napis> doWysłania = Lists.newArrayList();
	private int timer = 0;
	private Napis ostatnia = null;
	
	@Override
	public int czas() {
		if (timer >= 0)
			wyślij();
		return timer;
	}
	
	@Override
	public void przeładuj() { 
		wiadomości.clear();
		doWysłania.clear();
		wiadomości = Main.ust.wczytajListeNapisów("AutoWiadomosci", "wiadomości");
		doWysłania = Lists.newArrayList(wiadomości);
		timer = Main.ust.wczytajInt("AutoWiadomosci.czas") * 20;
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Atomatyczne Wiadomości", wiadomości.size());
	}
	
	public void wyślij() {
		if (wiadomości.size() <= 0) return;
		if (Bukkit.getOnlinePlayers().size() <= 0) return;
		int i;
		do i = Func.losuj(0, doWysłania.size()-1); 
			while(doWysłania.size() > 1 && doWysłania.get(i).equals(ostatnia));
		ostatnia = doWysłania.get(i);
		ostatnia.wyświetlWszystkim();
		doWysłania.remove(i);
		if (doWysłania.isEmpty())
			doWysłania = Lists.newArrayList(wiadomości);
	}

}
