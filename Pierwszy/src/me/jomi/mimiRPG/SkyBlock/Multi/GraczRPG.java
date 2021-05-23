package me.jomi.mimiRPG.SkyBlock.Multi;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.Func;

public class GraczRPG {
	public class Statystyka {
		private double baza = 0d;
		private double mnożnik = 1d;
		private transient double max = -1;
		public transient final NamespacedKey kluczBaza;
		public transient final NamespacedKey kluczMnożnik;
		
		Statystyka(double baza, String id) {
			this.baza = baza;
			this.kluczBaza	  = new NamespacedKey(Main.plugin, "mimi_rpg_baza_"		+ id);
			this.kluczMnożnik = new NamespacedKey(Main.plugin, "mimi_rpg_mnoznik_"	+ id);
		}

		public double wartość() {
			double wartość = baza * mnożnik;
			return max == -1 ? wartość : Math.min(wartość, max); 
		}

		public double getMnożnik() {
			return mnożnik;
		}
		public void setMnożnik(double mnożnik) {
			this.mnożnik = mnożnik;
			zapisz();
		}
		public void zwiększMnożnik(double mnożnik) {
			if (mnożnik > 0) setMnożnik(getMnożnik() * mnożnik);
			else			 setMnożnik(getMnożnik() / mnożnik);
		}
		
		public double getBaza() {
			return baza;
		}
		public void setBaza(double baza) {
			this.baza = baza;
			zapisz();
		}
		public void zwiększBaza(double dodatek) {
			setBaza(getBaza() + dodatek);
		}
	
		
		
		public void wczytaj() {
			PersistentDataContainer data = p.getPersistentDataContainer();
			baza	= data.getOrDefault(kluczBaza,		PersistentDataType.DOUBLE, 0d);
			mnożnik	= data.getOrDefault(kluczMnożnik,	PersistentDataType.DOUBLE, 1d);
		}
		public void zapisz() {
			PersistentDataContainer data = p.getPersistentDataContainer();
			data.set(kluczBaza,		PersistentDataType.DOUBLE, baza);
			data.set(kluczMnożnik,	PersistentDataType.DOUBLE, mnożnik);
		}
	}
	public class StatystykaProcentowa extends Statystyka {
		StatystykaProcentowa(double baza, String id) {
			super(baza, id);
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
	}
	

	public final StatystykaProcentowa defNiezależny		= new StatystykaProcentowa(0,	"def_niezalezny");
	public final StatystykaProcentowa szansaKryta		= new StatystykaProcentowa(5,	"szansa_kryta");
	public final StatystykaProcentowa dmgKryt			= new StatystykaProcentowa(100,	"dmg_kryt");
	public final StatystykaProcentowa unik				= new StatystykaProcentowa(0,	"unik");
	public final Statystyka prędkośćKopania		= new Statystyka(100,	"predkosc_kopania");
	public final Statystyka prędkośćChodzenia	= new Statystyka(100,	"predkosc_chodzenia");
	public final Statystyka zdrowie				= new Statystyka(100,	"hp");
	public final Statystyka def					= new Statystyka(0,		"def");
	public final Statystyka dmg					= new Statystyka(1,		"dmg");
	public final Statystyka prędkośćAtaku		= new Statystyka(4,		"predkosc_ataku");
	public final Statystyka inteligencja		= new Statystyka(100,	"inteligencja");
	
	
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
}
