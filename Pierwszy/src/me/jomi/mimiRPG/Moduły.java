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

public class Modu�y implements Prze�adowalny {
	private static ConfigurationSection modu�y;
	int w��czone = 0;
	
	Class<?>[] klasy = {Antylog.class, AutoWiadomosci.class, Budownik.class, ChatGrupowy.class, 
CustomoweCraftingi.class, CustomoweItemy.class, CustomowyDrop.class, Czapka.class, DrabinaPlus.class, 
EdytorTabliczek.class, EdytujItem.class, Funkcje.class, G�owa.class, ItemLink.class, JednorekiBandyta.class,
KolorPisania.class, KomendyInfo.class, Koniki.class, Kosz.class, Lootbagi.class, LosowyDropGracza.class,
Menu.class, Menurpg.class, Mi.class, Miniony.class, Osi�gni�cia.class, Patrzeq.class, PiszJako.class,
Plecak.class, Poziom.class, Przyjaciele.class, RTP.class, Sklep.class, Spawnery.class, Targ.class,
Ujezdzaj.class, UstawAttr.class, WeryfikacjaPelnoletnosci.class, WykonajWszystkim.class, Wymienianie.class,
Wyplac.class, ZabezpieczGracza.class, ZamienEq.class};
	
	@Override
	public void prze�aduj() {
		ConfigurationSection sekcja = Main.ust.sekcja("Modu�y");
		w��cz(sekcja);
		if (modu�y == null) 
			modu�y = sekcja;
	}
	
	public void w��cz(ConfigurationSection sekcja) {
		boolean prze�adowa� = false;
		for (Class<?> klasa : klasy)
			try {
				String nazwa = klasa.getSimpleName();
				if (modu�y == null)
					if (sekcja.getBoolean(nazwa)) {
						Main.zarejestruj(klasa.newInstance());
						w��czone++;
					}
				else if (modu�y != null && !modu�y.getBoolean(nazwa) && sekcja.getBoolean(nazwa)) {
					Main.zarejestruj(klasa.newInstance());
					modu�y.set(nazwa, true);
					w��czone++;
					Main.log("�aW��czono Modu�: " + nazwa);
					prze�adowa� = true;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Main.log("�cProblem przy tworzeniu:", klasa.getSimpleName());
			}
		if (prze�adowa�)
			Bukkit.getServer().reloadData();
	}

	@Override
	public String raport() {
		return "�6W��czone Modu�y: �e" + w��czone + "�6/�e" + klasy.length;
	}

	public static boolean w��czony(String modu�) {
		Object obj = modu�y.get(modu�);
		return obj == null ? false : (boolean) obj;
	}
	
}
