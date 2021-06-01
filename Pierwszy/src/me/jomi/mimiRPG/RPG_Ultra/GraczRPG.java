package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.util.Func;

public class GraczRPG {
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
		public transient final double domyślnie;
		private double baza = 0;
		private double mnożnik = 1d;
		private transient double max = -1;
		public transient final Atrybut atrybut;
		public transient final NamespacedKey kluczBaza;
		public transient final NamespacedKey kluczMnożnik;
		
		Statystyka(Atrybut atrybut, double domyślnie, String id) {
			this.kluczBaza	  = new NamespacedKey(Main.plugin, "mimi_rpg_baza_"		+ id);
			this.kluczMnożnik = new NamespacedKey(Main.plugin, "mimi_rpg_mnoznik_"	+ id);
			this.domyślnie = domyślnie;
			this.atrybut = atrybut;
			this.baza = domyślnie;
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
			zapisz();
			
			Bukkit.getPluginManager().callEvent(new ZmianaStatystykiGraczaEvent(p, this));
		}
		public void zwiększMnożnik(double mnożnik) {
			if (mnożnik > 0) setMnożnik(getMnożnik() * mnożnik);
			else			 setMnożnik(getMnożnik() / mnożnik);
		}
		
		public double getBaza() {
			return baza;
		}
		public void setBaza(double baza) {
			if (this.baza == baza)
				return;
			
			this.baza = baza;
			zapisz();
			
			Bukkit.getPluginManager().callEvent(new ZmianaStatystykiGraczaEvent(p, this));
		}
		public void zwiększBaza(double dodatek) {
			setBaza(getBaza() + dodatek);
		}
		
		
		public void wczytaj() {
			/*PersistentDataContainer data = p.getPersistentDataContainer();
			baza	= data.getOrDefault(kluczBaza,		PersistentDataType.DOUBLE, domyślnie);
			mnożnik	= data.getOrDefault(kluczMnożnik,	PersistentDataType.DOUBLE, 1d);*/
			baza = domyślnie;
		}
		public void zapisz() {
			/*PersistentDataContainer data = p.getPersistentDataContainer();
			data.set(kluczBaza,		PersistentDataType.DOUBLE, baza);
			data.set(kluczMnożnik,	PersistentDataType.DOUBLE, mnożnik);*/
		}
		
		
		@Override
		public String toString() {
			return new StringBuilder().append(this.atrybut).append((int) wartość()).toString();
		}
	}
	public class StatystykaProcentowa extends Statystyka {
		StatystykaProcentowa(Atrybut atrybut, double domyślnie, String id) {
			super(atrybut, domyślnie, id);
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

	public final StatystykaProcentowa defNiezależny		= new StatystykaProcentowa(Atrybut.DEF_NIEZALEŻNY,	0,	"def_niezalezny");
	public final StatystykaProcentowa szansaKryta		= new StatystykaProcentowa(Atrybut.KRYT_SZANSA,		5,	"szansa_kryta");
	public final StatystykaProcentowa szczęście			= new StatystykaProcentowa(Atrybut.SZCZĘŚCIE,		0,	"szczescie");
	public final StatystykaProcentowa unik				= new StatystykaProcentowa(Atrybut.UNIK,			0,	"unik");
	public final Statystyka prędkośćKopania		= new Statystyka(Atrybut.PRĘDKOŚĆ_KOPANIA,	100,	"predkosc_kopania");
	public final Statystyka prędkośćChodzenia	= new Statystyka(Atrybut.PRĘDKOŚĆ_CHODZENIA,100,	"predkosc_chodzenia");
	public final Statystyka dmgKryt				= new Statystyka(Atrybut.KRYT_DMG,			100,	"dmg_kryt");
	public final Statystyka zdrowie				= new Statystyka(Atrybut.HP,				100,	"hp");
	public final Statystyka def					= new Statystyka(Atrybut.DEF,				0,		"def");
	public final Statystyka dmg					= new Statystyka(Atrybut.SIŁA,				1,		"dmg");
	public final Statystyka prędkośćAtaku		= new Statystyka(Atrybut.PRĘDKOŚĆ_ATAKU,	4,		"predkosc_ataku");
	public final Statystyka inteligencja		= new Statystyka(Atrybut.INTELIGENCJA,		100,	"inteligencja");
	
	
	public static final GraczRPG gracz(Player p) {
		if (!p.hasMetadata("mimiGraczRPG"))
			Func.ustawMetadate(p, "mimiGraczRPG", new GraczRPG(p));
		return (GraczRPG) p.getMetadata("mimiGraczRPG").get(0).value();
	}
	
	
	public final Player p;
	
	GraczRPG(Player p) {
		this.p = p;
		
		Func.dajFields(this.getClass()).forEach(field -> {
			if (field.getGenericType() instanceof Class<?> && ((Class<?>) field.getGenericType()).isAssignableFrom(Statystyka.class))
				try {
					((Statystyka) field.get(this)).wczytaj();
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
		});
		
	}


	public List<Statystyka> getStaty() {
		List<Statystyka> lista = new ArrayList<>();
		
		Func.dajFields(this.getClass()).forEach(field -> {
			if (field.getGenericType() instanceof Class<?> && Func.dziedziczy((Class<?>) field.getGenericType(), Statystyka.class))
				try {
					lista.add((Statystyka) field.get(this));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
		});
		
		return lista;
	}


}
