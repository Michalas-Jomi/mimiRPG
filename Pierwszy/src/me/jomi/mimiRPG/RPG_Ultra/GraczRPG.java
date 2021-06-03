package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagIntArray;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

import lombok.Getter;

public class GraczRPG {
	private static final NamespacedKey kluczGraczRPG = new NamespacedKey(Main.plugin, "mimiGraczRPG");
	
	public static class Api {
		public static class ZmianaStatystykiGraczaEvent extends PlayerEvent {
			public final Statystyka statystyka;
			
			public ZmianaStatystykiGraczaEvent(Player p, Statystyka statystyka) {
				super(p);
				this.statystyka = statystyka;
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
	}
	public class Statystyka {
		private double baza = 0;
		private double mnożnik = 1d;
		private transient double max = -1;
		public transient final Atrybut atrybut;
		
		Statystyka(Atrybut atrybut, double baza) {
			this.atrybut = atrybut;
			this.baza = baza;
		}
		
		public double wartość() {
			double wartość = baza * mnożnik;
			return max == -1 ? wartość : Math.min(wartość, max); 
		}
		
		public double getMnożnik() {
			return mnożnik;
		}
		public void setMnożnik(double mnożnik) {
			if (this.mnożnik == mnożnik)
				return;
			
			this.mnożnik = mnożnik;
			
			Bukkit.getPluginManager().callEvent(new ZmianaStatystykiGraczaEvent(p, this));
		}
		public void zwiększMnożnik(double mnożnik) {
			if (mnożnik >= 0) setMnożnik(getMnożnik() * mnożnik);
			else			  setMnożnik(getMnożnik() / -mnożnik);
		}
		
		public double getBaza() {
			return baza;
		}
		public void setBaza(double baza) {
			if (this.baza == baza)
				return;
			
			this.baza = baza;
			
			Bukkit.getPluginManager().callEvent(new ZmianaStatystykiGraczaEvent(p, this));
		}
		public void zwiększBaza(double dodatek) {
			setBaza(getBaza() + dodatek);
		}
		
		@Override
		public String toString() {
			return new StringBuilder().append(this.atrybut).append((int) wartość()).toString();
		}
	}
	public class StatystykaProcentowa extends Statystyka {
		StatystykaProcentowa(Atrybut atrybut, double baza) {
			super(atrybut, baza);
		}
		
		public boolean losuj() {
			return Func.losuj(wartość());
		}
		
		/**
		 * @return rzeczywista wartość statystyki
		 */
		@Override
		public double wartość() {
			return super.wartość() / 100d; 
		}
		/**
		 * @return wartość od 0 do 100
		 */
		public int procent() {
			return Math.min(100, Math.max(0, (int) super.wartość()));
		}
	

		@Override
		public String toString() {
			return new StringBuilder().append(this.atrybut).append(procent()).toString();
		}
	}

	public static class ŚcieżkaDoświadczenia {
		
		public static final ŚcieżkaDoświadczenia farmer		= new ŚcieżkaDoświadczenia("Farmer",	"Zbieraj plony i zabijaj zwięrzęta", new int[]{20, 100,250,500});
		public static final ŚcieżkaDoświadczenia łowca		= new ŚcieżkaDoświadczenia("Łowca",		"Zabijaj moby", new int[]{20, 100, 250, 500, 1000, 2500, 5000});
		public static final ŚcieżkaDoświadczenia kopacz		= new ŚcieżkaDoświadczenia("Kopacz",	"Kop rudy i kamień", new int[]{20, 100, 250, 500, 1000, 2500});
		public static final ŚcieżkaDoświadczenia drwal		= new ŚcieżkaDoświadczenia("Drwal",		"Zcinaj drzewa", new int[]{20, 100, 250, 500, 1000, 2500,5000});
		public static final ŚcieżkaDoświadczenia rybak		= new ŚcieżkaDoświadczenia("Rybak",		"Łów ryby", new int[]{20, 100, 250, 500, 1000, 2500, 5000});
		public static final ŚcieżkaDoświadczenia mag		= new ŚcieżkaDoświadczenia("Mag",		"Enchantuj", new int[]{20, 100, 250, 500, 1000, 2500, 5000});
		public static final ŚcieżkaDoświadczenia alchemik	= new ŚcieżkaDoświadczenia("Alchemik",	"warz potki", new int[]{20, 100, 250, 500, 1000, 2500, 5000});
		public static final ŚcieżkaDoświadczenia budowniczy	= new ŚcieżkaDoświadczenia("Budowniczy","stawiaj bloki", new int[]{20, 100, 250, 500, 1000, 2500, 5000});
		

		// 0 - lvl 0 -> 1
		// 1 - lvl 1 -> 2
		public final int[] potrzebyExp;
		public final String nazwa;
		public final String opis;
		
		private ŚcieżkaDoświadczenia(String nazwa, String opis, int[] potrzebyExp) {
			this.potrzebyExp = potrzebyExp;
			this.nazwa = nazwa;
			this.opis = opis;
		}
	}
	public class ŚcieżkaDoświadczeniaGracz {
		@Getter private int exp;
		@Getter private int lvl;
		private final transient ŚcieżkaDoświadczenia ścieżka;
		
		ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia ścieżka) {
			this.ścieżka = ścieżka;
			wczytaj();
		}
		
		public void zwiększExp(int oIle) {
			if (exp == -1)
				return;
			exp += oIle;
			while (exp != -1 && exp >= ścieżka.potrzebyExp[lvl]) {
				exp -= ścieżka.potrzebyExp[lvl++];
				p.sendMessage(RPG.prefix + Func.msg("Awansowałeś w ścieżce %s %s -> %s §alvl§c!", ścieżka.nazwa, lvl - 1, lvl));
				if (lvl >= ścieżka.potrzebyExp.length)
					exp = -1;
			}
			zapisz();
		}
		
		public void zapisz() {
			NMS.set(dataŚcieżkiDoświadczenia, ścieżka.nazwa, PersistentDataType.INTEGER_ARRAY, new int[] {exp, lvl});
		}
		public void wczytaj() {
			int[] exp_lvl = NMS.get(
					dataŚcieżkiDoświadczenia,
					ścieżka.nazwa,
					PersistentDataType.INTEGER_ARRAY
					);
			
			if (exp_lvl != null) {
				exp = exp_lvl[0];
				lvl = exp_lvl[1];
			}
		}
	}
	
	public static final GraczRPG gracz(Player p) {
		if (!p.hasMetadata("mimiGraczRPG"))
			Func.ustawMetadate(p, "mimiGraczRPG", new GraczRPG(p));
		return (GraczRPG) p.getMetadata("mimiGraczRPG").get(0).value();
	}
	

	public final StatystykaProcentowa defNiezależny		= new StatystykaProcentowa(Atrybut.DEF_NIEZALEŻNY,		0);
	public final StatystykaProcentowa szansaKryta		= new StatystykaProcentowa(Atrybut.KRYT_SZANSA,			5);
	public final StatystykaProcentowa szczęście			= new StatystykaProcentowa(Atrybut.SZCZĘŚCIE,			0);
	public final StatystykaProcentowa unik				= new StatystykaProcentowa(Atrybut.UNIK,				0);
	public final Statystyka prędkośćChodzenia			= new StatystykaProcentowa(Atrybut.PRĘDKOŚĆ_CHODZENIA,	100);
	public final Statystyka prędkośćKopania		= new Statystyka(Atrybut.PRĘDKOŚĆ_KOPANIA,	100);
	public final Statystyka dmgKryt				= new Statystyka(Atrybut.KRYT_DMG,			100);
	public final Statystyka zdrowie				= new Statystyka(Atrybut.HP,				100);
	public final Statystyka def					= new Statystyka(Atrybut.DEF,				0);
	public final Statystyka dmg					= new Statystyka(Atrybut.SIŁA,				1);
	public final Statystyka prędkośćAtaku		= new Statystyka(Atrybut.PRĘDKOŚĆ_ATAKU,	4);
	public final Statystyka inteligencja		= new Statystyka(Atrybut.INTELIGENCJA,		100);
	public Statystyka statystyka(Atrybut attr) {
		switch (attr) {
		case DEF:				return def;
		case DEF_NIEZALEŻNY:	return defNiezależny;
		case HP:				return zdrowie;
		case INTELIGENCJA:		return inteligencja;
		case KRYT_DMG:			return dmgKryt;
		case KRYT_SZANSA:		return szansaKryta;
		case PRĘDKOŚĆ_ATAKU:	return prędkośćAtaku;
		case PRĘDKOŚĆ_CHODZENIA:return prędkośćChodzenia;
		case PRĘDKOŚĆ_KOPANIA:	return prędkośćKopania;
		case SIŁA:				return dmg;
		case SZCZĘŚCIE:			return szczęście;
		case UNIK:				return unik;
		}
		throw new IllegalArgumentException("Brak statystyki graczaRPG " + attr);
	}
	
	public final ŚcieżkaDoświadczeniaGracz ścieżka_farmer;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_łowca;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_kopacz;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_drwal;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_rybak;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_mag;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_alchemik;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_budowniczy;
	
	
	public final Player p;

	public final PersistentDataContainer dataRPG;
	public final PersistentDataContainer dataStatystyki;
	public final PersistentDataContainer dataŚcieżkiDoświadczenia;
	public final PersistentDataContainer dataTrwałeBuffy;
	
	GraczRPG(Player p) {
		this.p = p;

		if (!p.getPersistentDataContainer().getKeys().contains(kluczGraczRPG))
			p.getPersistentDataContainer().set(kluczGraczRPG, PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer());
		dataRPG = p.getPersistentDataContainer().get(kluczGraczRPG, PersistentDataType.TAG_CONTAINER);
		
		if (!NMS.getRaw(dataRPG).containsKey("statystyki"))
			NMS.set(dataRPG, "statystyki", PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer());
		dataStatystyki = NMS.get(dataRPG, "statystyki", PersistentDataType.TAG_CONTAINER);
		
		if (!NMS.getRaw(dataRPG).containsKey("ścieżkiDoświadczenia"))
			NMS.set(dataRPG, "ścieżkiDoświadczenia", PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer());
		dataŚcieżkiDoświadczenia = NMS.get(dataRPG, "ścieżkiDoświadczenia", PersistentDataType.TAG_CONTAINER);
		
		if (!NMS.getRaw(dataRPG).containsKey("trwałeBuffy"))
			NMS.set(dataRPG, "trwałeBuffy", PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer());
		dataTrwałeBuffy = NMS.get(dataRPG, "trwałeBuffy", PersistentDataType.TAG_CONTAINER);
		
		
		NMS.getRaw(dataTrwałeBuffy).forEach((attr, buff) -> {
			Statystyka statystyka = statystyka(Func.StringToEnum(Atrybut.class, attr));
			int[] baza_mnożnik = ((NBTTagIntArray) buff).getInts();
			
			statystyka.zwiększBaza	 (baza_mnożnik[0] / 1000d);
			statystyka.zwiększMnożnik(baza_mnożnik[1] / 1000d);
		});
		
		ścieżka_farmer		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.farmer);
		ścieżka_łowca		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.łowca);
		ścieżka_kopacz		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.kopacz);
		ścieżka_drwal		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.drwal);
		ścieżka_rybak		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.rybak);
		ścieżka_mag			= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.mag);
		ścieżka_alchemik	= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.alchemik);
		ścieżka_budowniczy	= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.budowniczy);
	}
	
	public void zwiększTrwałyBuff(Atrybut attr, double oIle, boolean baza) {
		Statystyka statystyka = statystyka(attr);
		int[] baza_mnożnik = new int[] {0, 1000};
		
		NBTBase buff;
		if ((buff = NMS.getRaw(dataTrwałeBuffy).get(attr.name())) != null)
			baza_mnożnik = ((NBTTagIntArray) buff).getInts();
		
		if (baza)
			baza_mnożnik[0] += oIle * 1000;
		else
			baza_mnożnik[1] *= oIle;
		
		NMS.set(dataTrwałeBuffy, attr.name(), PersistentDataType.INTEGER_ARRAY, baza_mnożnik);
		
		if (baza)
			statystyka.zwiększBaza(oIle);
		else
			statystyka.zwiększMnożnik(oIle);
	}
	
	
	public void zwiększStatystykę(String statystyka) {
		NMS.set(dataStatystyki, statystyka, PersistentDataType.INTEGER, NMS.get(dataStatystyki, statystyka, PersistentDataType.INTEGER));
	}
	public int dajStatystykę(String statystyka) {
		return NMS.get(dataStatystyki, statystyka, PersistentDataType.INTEGER);
	}
	
	
	public void dodajKase(double ile) {
		// TODO ekonomia rpg
	}

	
	
	public List<Statystyka> getStaty() {
		List<Statystyka> lista = new ArrayList<>();
		
		Func.forEach(Atrybut.values(), attr -> lista.add(statystyka(attr)));
		
		return lista;
	}
}
