package me.jomi.mimiRPG;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import me.jomi.mimiRPG.Chat.AutoWiadomosci;
import me.jomi.mimiRPG.Chat.ChatGrupowy;
import me.jomi.mimiRPG.Chat.Funkcje;
import me.jomi.mimiRPG.Chat.ItemLink;
import me.jomi.mimiRPG.Chat.KolorPisania;
import me.jomi.mimiRPG.Chat.KomendyInfo;
import me.jomi.mimiRPG.Chat.Mi;
import me.jomi.mimiRPG.Chat.PiszJako;
import me.jomi.mimiRPG.Chat.Przyjaciele;
import me.jomi.mimiRPG.Chat.WykonajWszystkim;
import me.jomi.mimiRPG.Edytory.EdytorTabliczek;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import me.jomi.mimiRPG.Maszyny.Budownik;
import me.jomi.mimiRPG.Maszyny.JednorekiBandyta;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.*;

public class Modu³y implements Prze³adowalny {
	private static ConfigurationSection modu³y;
	int w³¹czone = 0;
	
	Class<?>[] klasy = {Antylog.class, AutoWiadomosci.class, Budownik.class, ChatGrupowy.class, 
CustomoweCraftingi.class, CustomoweItemy.class, CustomowyDrop.class, Czapka.class, DrabinaPlus.class, 
EdytorTabliczek.class, EdytujItem.class, Funkcje.class, G³owa.class, ItemLink.class, JednorekiBandyta.class,
KolorPisania.class, KomendyInfo.class, Koniki.class, Kosz.class, Lootbagi.class, LosowyDropGracza.class,
Menu.class, Menurpg.class, Mi.class, Miniony.class, Osi¹gniêcia.class, Patrzeq.class, PiszJako.class,
Plecak.class, Poziom.class, Przyjaciele.class, RTP.class, Sklep.class, Spawnery.class, Targ.class,
Ujezdzaj.class, UstawAttr.class, WeryfikacjaPelnoletnosci.class, WykonajWszystkim.class, Wymienianie.class,
Wyplac.class, ZabezpieczGracza.class, ZamienEq.class};
	
	@Override
	public void prze³aduj() {
		ConfigurationSection sekcja = Main.ust.sekcja("Modu³y");
		w³¹cz(sekcja);
		if (modu³y == null) 
			modu³y = sekcja;
	}
	
	public void w³¹cz(ConfigurationSection sekcja) {
		boolean prze³adowaæ = false;
		for (Class<?> klasa : klasy)
			try {
				String nazwa = klasa.getSimpleName();
				if (modu³y == null)
					if (sekcja.getBoolean(nazwa)) {
						Main.zarejestruj(klasa.newInstance());
						w³¹czone++;
					}
				else if (modu³y != null && !modu³y.getBoolean(nazwa) && sekcja.getBoolean(nazwa)) {
					Main.zarejestruj(klasa.newInstance());
					modu³y.set(nazwa, true);
					w³¹czone++;
					Main.log("§aW³¹czono Modu³: " + nazwa);
					prze³adowaæ = true;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Main.log("§cProblem przy tworzeniu:", klasa.getSimpleName());
			}
		if (prze³adowaæ)
			Bukkit.getServer().reloadData();
	}

	@Override
	public String raport() {
		return "§6W³¹czone Modu³y: §e" + w³¹czone + "§6/§e" + klasy.length;
	}

	public static boolean w³¹czony(String modu³) {
		Object obj = modu³y.get(modu³);
		return obj == null ? false : (boolean) obj;
	}
	
}
