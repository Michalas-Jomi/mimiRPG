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

public class Moduły implements Przeładowalny {
	private static ConfigurationSection moduły;
	int włączone = 0;
	
	Class<?>[] klasy = {Antylog.class, AutoWiadomosci.class, Budownik.class, ChatGrupowy.class, 
CustomoweCraftingi.class, CustomoweItemy.class, CustomowyDrop.class, Czapka.class, DrabinaPlus.class, 
EdytorTabliczek.class, EdytujItem.class, Funkcje.class, Głowa.class, ItemLink.class, JednorekiBandyta.class,
KolorPisania.class, KomendyInfo.class, Koniki.class, Kosz.class, Lootbagi.class, LosowyDropGracza.class,
Menu.class, Menurpg.class, Mi.class, Miniony.class, Osiągnięcia.class, Patrzeq.class, PiszJako.class,
Plecak.class, Poziom.class, Przyjaciele.class, RTP.class, Sklep.class, Spawnery.class, Targ.class,
Ujezdzaj.class, UstawAttr.class, WeryfikacjaPelnoletnosci.class, WykonajWszystkim.class, Wymienianie.class,
Wyplac.class, ZabezpieczGracza.class, ZamienEq.class};
	
	@Override
	public void przeładuj() {
		ConfigurationSection sekcja = Main.ust.sekcja("Moduły");
		włącz(sekcja);
		if (moduły == null) 
			moduły = sekcja;
	}
	
	public void włącz(ConfigurationSection sekcja) {
		boolean przeładować = false;
		for (Class<?> klasa : klasy)
			try {
				String nazwa = klasa.getSimpleName();
				if (moduły == null)
					if (sekcja.getBoolean(nazwa)) {
						Main.zarejestruj(klasa.newInstance());
						włączone++;
					}
				else if (moduły != null && !moduły.getBoolean(nazwa) && sekcja.getBoolean(nazwa)) {
					Main.zarejestruj(klasa.newInstance());
					moduły.set(nazwa, true);
					włączone++;
					Main.log("§aWłączono Moduł: " + nazwa);
					przeładować = true;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Main.log("§cProblem przy tworzeniu:", klasa.getSimpleName());
			}
		if (przeładować)
			Bukkit.getServer().reloadData();
	}

	@Override
	public String raport() {
		return "§6Włączone Moduły: §e" + włączone + "§6/§e" + klasy.length;
	}

	public static boolean włączony(String moduł) {
		Object obj = moduły.get(moduł);
		return obj == null ? false : (boolean) obj;
	}
	
}
