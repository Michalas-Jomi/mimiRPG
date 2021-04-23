package me.jomi.mimiRPG.SkyBlock;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweOsiągnięcia;
import me.jomi.mimiRPG.Customizacja.CustomoweOsiągnięcia.Kryterium.Typ;
import me.jomi.mimiRPG.Customizacja.CustomoweOsiągnięcia.Osiągnięcie;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Zegar;

@Moduł(priorytet = Moduł.Priorytet.WYSOKI)
public class DailyAdv implements Zegar, Listener {
	public static final String prefix = Func.prefix("Daily");
	
	static NamespacedKey klucz = new NamespacedKey(Main.plugin, "daily");
	static String aktKod = aktKod();
	
	public static String aktKod() {
		ZonedDateTime teraz = ZonedDateTime.now();
		return teraz.getYear() + " " + teraz.getDayOfYear();
	}
	
	public DailyAdv() {
		if (Main.plugin.isEnabled())
			Main.reloadBukkitData();
	}
	
	
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		Gracz gracz = Gracz.wczytaj(ev.getPlayer());
		if (gracz.dailyAdv != null && !gracz.dailyAdv.equals(aktKod))
			wyczyśćAdv(ev.getPlayer());
	}
	@EventHandler
	public void adv(PlayerAdvancementDoneEvent ev) {
		if (ev.getAdvancement().getKey().equals(klucz)) {
			ev.getPlayer().sendMessage(prefix + "Ukończyłeś zadanie dzienne!");
			Main.log(prefix + "%s ukończył zadanie dzienne", ev.getPlayer().getName());
			Gracz gracz = Gracz.wczytaj(ev.getPlayer());
			gracz.dailyAdv = aktKod;
			gracz.zapisz();
		}
	}
	
	static void wyczyśćAdv(Player p) {
		try {
			AdvancementProgress progres = p.getAdvancementProgress(Bukkit.getAdvancement(klucz));
			Lists.newArrayList(progres.getRemainingCriteria()).forEach(progres::revokeCriteria);
			Gracz gracz = Gracz.wczytaj(p);
			gracz.dailyAdv = null;
			gracz.zapisz();
		} catch (IllegalArgumentException e) {
		}
	}
	
	
	@Override
	public int czas() {
		if (aktKod.equals(aktKod())) {
			if (!Main.plugin.isEnabled())
				Main.warn("DailyAdv niepotrzebnie próbował sie odnowić");
		} else {
			aktKod = aktKod();
			
			Bukkit.getOnlinePlayers().forEach(DailyAdv::wyczyśćAdv);

			Main.reloadBukkitData();
			
			Main.log("DailyAdv odnowiony");
		}
		
		ZonedDateTime teraz = ZonedDateTime.now();
		int czas = 0;
		
		czas += 24 - teraz.getHour();
		czas *= 60;
		
		czas += 60 - teraz.getMinute();
		czas *= 60;
		
		czas += 60 - teraz.getSecond();
		czas *= 20;
		
		return czas + 10;
	}



	
	
	public static class Daily extends Mapowany {
		@Mapowane ItemStack ikona;
		@Mapowane String opis;
		
		@Mapowane int ile_min = 1;
		@Mapowane int ile_max = 1;
		@Mapowane Typ typ = Typ.ZNISZCZONE_BLOKI;
		@Mapowane(nieTwórz = true) private List<String> konkrety = Lists.newArrayList();
		
		@Mapowane public List<ItemStack> nagroda;
		@Mapowane public double nagrodaKasa = 0d;
		@Mapowane public int nagrodaWalutaPremium = 0;
		@Mapowane public int exp;
	}
	
	@SuppressWarnings("unchecked")
	public static void przeładuj() {
		Config config = new Config("configi/Daily Questy");
		
		Osiągnięcie adv = new Osiągnięcie();
		Func.zdemapuj(adv, config.sekcja("podstawa").getValues(false));
		
		Map<String, Object> mapa = Func.zmapuj(Func.losuj((List<Daily>) config.wczytajPewny("kryteria")));
		mapa.put("ile", Func.losuj((int) mapa.remove("ile_min"), (int) mapa.remove("ile_max")));
		adv.ikona = Config.item(mapa.remove("ikona"));
		adv.opis  = (String) mapa.remove("opis");
		
		adv.namespacedKey = klucz.toString();
		
		adv.zapisywać = false;
		
		adv.exp 				 = (int) mapa.remove("exp");
		adv.nagrodaKasa 		 = (double) mapa.remove("nagrodaKasa");
		adv.nagroda 			 = (List<ItemStack>) mapa.remove("nagroda");
		adv.nagrodaWalutaPremium = (int) mapa.remove("nagrodaWalutaPremium");

		mapa.put("nazwa", "k_daily");
		adv.kryteria = new ArrayList<>();
		adv.kryteria.add(new CustomoweOsiągnięcia.Kryterium());
		Func.zdemapuj(adv.kryteria.get(0), mapa);
		
		adv = (Osiągnięcie) Mapowany.deserialize(adv.serialize());
		CustomoweOsiągnięcia.dodajAdv(adv);
	}
}
