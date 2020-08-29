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

public class Modu造 implements Prze豉dowalny {
	private static ConfigurationSection modu造;
	int w章czone = 0;
	
	Class<?>[] klasy = {Antylog.class, AutoWiadomosci.class, Budownik.class, ChatGrupowy.class, 
CustomoweCraftingi.class, CustomoweItemy.class, CustomowyDrop.class, Czapka.class, DrabinaPlus.class, 
EdytorTabliczek.class, EdytujItem.class, Funkcje.class, G這wa.class, ItemLink.class, JednorekiBandyta.class,
KolorPisania.class, KomendyInfo.class, Koniki.class, Kosz.class, Lootbagi.class, LosowyDropGracza.class,
Menu.class, Menurpg.class, Mi.class, Miniony.class, Osi鉚ni璚ia.class, Patrzeq.class, PiszJako.class,
Plecak.class, Poziom.class, Przyjaciele.class, RTP.class, Sklep.class, Spawnery.class, Targ.class,
Ujezdzaj.class, UstawAttr.class, WeryfikacjaPelnoletnosci.class, WykonajWszystkim.class, Wymienianie.class,
Wyplac.class, ZabezpieczGracza.class, ZamienEq.class};
	
	@Override
	public void prze豉duj() {
		ConfigurationSection sekcja = Main.ust.sekcja("Modu造");
		w章cz(sekcja);
		if (modu造 == null) 
			modu造 = sekcja;
	}
	
	public void w章cz(ConfigurationSection sekcja) {
		boolean prze豉dowa� = false;
		for (Class<?> klasa : klasy)
			try {
				String nazwa = klasa.getSimpleName();
				if (modu造 == null)
					if (sekcja.getBoolean(nazwa)) {
						Main.zarejestruj(klasa.newInstance());
						w章czone++;
					}
				else if (modu造 != null && !modu造.getBoolean(nazwa) && sekcja.getBoolean(nazwa)) {
					Main.zarejestruj(klasa.newInstance());
					modu造.set(nazwa, true);
					w章czone++;
					Main.log("吧W章czono Modu�: " + nazwa);
					prze豉dowa� = true;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Main.log("呃Problem przy tworzeniu:", klasa.getSimpleName());
			}
		if (prze豉dowa�)
			Bukkit.getServer().reloadData();
	}

	@Override
	public String raport() {
		return "�6W章czone Modu造: 呈" + w章czone + "�6/呈" + klasy.length;
	}

	public static boolean w章czony(String modu�) {
		Object obj = modu造.get(modu�);
		return obj == null ? false : (boolean) obj;
	}
	
}
