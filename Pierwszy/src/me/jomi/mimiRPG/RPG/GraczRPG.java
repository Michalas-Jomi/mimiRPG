package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.persistence.PersistentDataType;

import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagIntArray;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.RPG.Bestie.Bestia;
import me.jomi.mimiRPG.RPG.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

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
			
			wyślijEvent();
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
			
			wyślijEvent();
		}
		public void zwiększBaza(double dodatek) {
			setBaza(getBaza() + dodatek);
		}
		
		void wyślijEvent() {
			Bukkit.getPluginManager().callEvent(new ZmianaStatystykiGraczaEvent(p, this));
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

	private abstract class AbstractŚcieżkaGracz<T extends IŚcieżka> {
		int exp;
		int lvl;
		public final T ścieżka;
		
		AbstractŚcieżkaGracz(T ścieżka) {
			this.ścieżka = ścieżka;
			wczytaj();
		}
		
		public int getExp() {
			return exp;
		}
		public int getLvl() {
			return lvl + 1;
		}
		
		public void zwiększExp(int oIle) {
			if (oIle == 0 || exp == -1)
				return;
			exp += oIle;
			while (exp != -1 && exp >= ścieżka.getPotrzebnyExp()[lvl]) {
				exp -= ścieżka.getPotrzebnyExp()[lvl++];
				p.sendMessage(RPG.prefix + Func.msg("Awansowałeś w ścieżce %s %s -> %s §alvl§c!", ścieżka.getNazwa(), lvl, lvl + 1));
				Main.log(RPG.prefix + "%s awansował w ścieżce rozwoju %s %s -> %s lvl", p.getName(), ścieżka.getNazwa(), lvl, lvl + 1);
				if (lvl >= ścieżka.getPotrzebnyExp().length)
					exp = -1;
			}
			
			zapisz();
		}
		
		public void zapisz() {
			getData().setIntArray(ścieżka.getNazwa(), new int[] {exp, lvl});
		}
		public void wczytaj() {
			int[] exp_lvl = getData().getIntArray(ścieżka.getNazwa());
			
			if (exp_lvl.length == 2) {
				exp = exp_lvl[0];
				lvl = exp_lvl[1];
			}
		}
		
		public abstract NBTTagCompound getData();

		public int getPotrzebnyExp() {
			if (exp == -1)
				return 0;
			return ścieżka.getPotrzebnyExp()[lvl];
		}
	}
	public class KolekcjaGracz extends AbstractŚcieżkaGracz<Kolekcja> {
		KolekcjaGracz(Kolekcja kolekcja) {
			super(kolekcja);
		}

		@Override
		public NBTTagCompound getData() {
			return dataKolekcje;
		}
	
		public boolean odblokowana() {
			return exp > 0 || lvl > 0;
		}
		
		@Override
		public void zwiększExp(int oIle) {
			boolean odblokowana = odblokowana();
			super.zwiększExp(oIle);
			if (!odblokowana) {
				Func.powiadom(Kolekcja.prefix, p, "Odblokowałeś kolekcję %s!", ścieżka.nazwa);
				Main.log(Kolekcja.prefix + "%s odblokował kolekcję %s", p.getName(), ścieżka.nazwa);
			}
		}
	}
	public class ŚcieżkaDoświadczeniaGracz extends AbstractŚcieżkaGracz<ŚcieżkaDoświadczenia> {
		ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia ścieżka) {
			super(ścieżka);
		}
		
		@Override
		public void zwiększExp(int oIle) {
			super.zwiększExp(oIle);
			
			if (exp != -1 && oIle != 0)
				RPG.actionBar(GraczRPG.this, strB -> {
					strB.append("§3");
					strB.append(ścieżka.nazwa);
					strB.append(' ');
					strB.append(exp);
					strB.append(" / ");
					strB.append(ścieżka.potrzebnyExp[lvl]);
					strB.append(" +");
					strB.append(oIle);
					strB.append(" (");
					strB.append(Func.zaokrąglij(exp / (double) ścieżka.potrzebnyExp[lvl] * 100, 1));
					strB.append("%)");
				});
		}

		@Override
		public NBTTagCompound getData() {
			return dataŚcieżkiDoświadczenia;
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
	public final Statystyka siła				= new Statystyka(Atrybut.SIŁA,				1);
	public final Statystyka prędkośćAtaku		= new Statystyka(Atrybut.PRĘDKOŚĆ_ATAKU,	4);
	public final Statystyka inteligencja		= new Statystyka(Atrybut.INTELIGENCJA,		100);
	
	protected long ostActionBar; // zmienna dla actionBaru
	public final ŚcieżkaDoświadczeniaGracz ścieżka_farmer;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_łowca; // w bestie
	public final ŚcieżkaDoświadczeniaGracz ścieżka_kopacz; // w kopanieRPG
	public final ŚcieżkaDoświadczeniaGracz ścieżka_drwal; // w kopanieRPG
	public final ŚcieżkaDoświadczeniaGracz ścieżka_rybak;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_mag;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_alchemik;
	public final ŚcieżkaDoświadczeniaGracz ścieżka_budowniczy;
	
	
	public final Player p;

	public final NBTTagCompound dataRPG;
	public final NBTTagCompound dataBestie;
	public final NBTTagCompound dataŚcieżkiDoświadczenia;
	public final NBTTagCompound dataKolekcje;
	public final NBTTagCompound dataTrwałeBuffy;
	public final NBTTagCompound dataPamięć;
	
	GraczRPG(Player p) {
		this.p = p;

		if (!p.getPersistentDataContainer().getKeys().contains(kluczGraczRPG))
			p.getPersistentDataContainer().set(kluczGraczRPG, PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer());
		dataRPG = NMS.tag(p.getPersistentDataContainer().get(kluczGraczRPG, PersistentDataType.TAG_CONTAINER));
		
		dataŚcieżkiDoświadczenia= RPG.dataDajUtwórz(dataRPG, "ścieżkiDoświadczenia");
		dataTrwałeBuffy			= RPG.dataDajUtwórz(dataRPG, "trwałeBuffy");
		dataKolekcje			= RPG.dataDajUtwórz(dataRPG, "kolekcje");
		dataBestie				= RPG.dataDajUtwórz(dataRPG, "bestie");
		dataPamięć				= RPG.dataDajUtwórz(dataRPG, "pamięć");
		
		ścieżka_farmer		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.FARMER);
		ścieżka_łowca		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.ŁOWCA);
		ścieżka_kopacz		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.KOPACZ);
		ścieżka_drwal		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.DRWAL);
		ścieżka_rybak		= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.RYBAK);
		ścieżka_mag			= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.MAG);
		ścieżka_alchemik	= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.ALCHEMIK);
		ścieżka_budowniczy	= new ŚcieżkaDoświadczeniaGracz(ŚcieżkaDoświadczenia.BUDOWNICZY);
		
		
		Bukkit.getScheduler().runTask(Main.plugin, () -> getStaty().forEach(Statystyka::wyślijEvent));
		
		dataTrwałeBuffy.getKeys().forEach(attr -> {
			Statystyka statystyka = statystyka(Func.StringToEnum(Atrybut.class, attr));
			int[] baza_mnożnik = dataTrwałeBuffy.getIntArray(attr);
			
			statystyka.zwiększBaza	 (baza_mnożnik[0] / 1000d);
			statystyka.zwiększMnożnik(baza_mnożnik[1] / 1000d);
		});
	}
	
	public void zwiększTrwałyBuff(Atrybut attr, double oIle, boolean baza) {
		Statystyka statystyka = statystyka(attr);
		int[] baza_mnożnik = new int[] {0, 1000};
		
		NBTBase buff;
		if ((buff = dataTrwałeBuffy.get(attr.name())) != null)
			baza_mnożnik = ((NBTTagIntArray) buff).getInts();
		
		if (baza)
			baza_mnożnik[0] += oIle * 1000;
		else
			baza_mnożnik[1] *= oIle;
		
		dataTrwałeBuffy.setIntArray(attr.name(), baza_mnożnik);
		
		if (baza)
			statystyka.zwiększBaza(oIle);
		else
			statystyka.zwiększMnożnik(oIle);
	}
	
	public NBTTagCompound getBestie(Bestia bestia) {
		return getBestie(bestia.kategoria, bestia.grupa, bestia.nazwa);
	}
	public NBTTagCompound getBestie(String kategoria, String grupa, String nazwa) {
		return RPG.dataDajUtwórz(getBestieGrupa(kategoria, grupa), nazwa);
	}
	public NBTTagCompound getBestieGrupa(String kategoria, String grupa) {
		return RPG.dataDajUtwórz(getBestieKategoria(kategoria), grupa);
	}
	public NBTTagCompound getBestieKategoria(String kategoria) {
		return RPG.dataDajUtwórz(dataBestie, kategoria);
	}
	
	
	private Map<Kolekcja, KolekcjaGracz> mapaKolekcji = new EnumMap<>(Kolekcja.class);
	public KolekcjaGracz getKolekcja(Kolekcja kolekcja) {
		if (kolekcja == null)
			return null;
		KolekcjaGracz kolekcjaGracza = mapaKolekcji.get(kolekcja);
		if (kolekcjaGracza == null)
			mapaKolekcji.put(kolekcja, kolekcjaGracza = new KolekcjaGracz(kolekcja));
		return kolekcjaGracza;
	}
	
	
	public void dodajKase(double ile) {
		if (Main.ekonomia)
			if (ile >= 0)
				Main.econ.depositPlayer(p, ile);
			else
				Main.econ.withdrawPlayer(p, -ile);
	}

	public List<Statystyka> getStaty() {
		List<Statystyka> lista = new ArrayList<>();
		
		Func.forEach(Atrybut.values(), attr -> lista.add(statystyka(attr)));
		
		return lista;
	}
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
		case SIŁA:				return siła;
		case SZCZĘŚCIE:			return szczęście;
		case UNIK:				return unik;
		}
		throw new IllegalArgumentException("Brak statystyki graczaRPG " + attr);
	}

	public ŚcieżkaDoświadczeniaGracz ścieżka(ŚcieżkaDoświadczenia ścieżka) {
		switch (ścieżka) {
		case ALCHEMIK:	return ścieżka_alchemik;
		case BUDOWNICZY:return ścieżka_budowniczy;
		case DRWAL:		return ścieżka_drwal;
		case FARMER:	return ścieżka_farmer;
		case KOPACZ:	return ścieżka_kopacz;
		case MAG:		return ścieżka_mag;
		case RYBAK:		return ścieżka_rybak;
		case ŁOWCA:		return ścieżka_łowca;
		}
		throw new IllegalArgumentException("Brak ścieżki graczaRPG " + ścieżka);
	}
	
	
	public void zapisz() {
		p.getPersistentDataContainer().set(kluczGraczRPG, PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer(dataRPG));
	}
	
}
