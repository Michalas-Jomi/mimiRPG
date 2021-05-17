package me.jomi.mimiRPG.SkyBlock.Multi;

import org.bukkit.entity.Player;

public class GraczRPG {
	public class Statystyka {
		private double baza;
		private double mnożnik;
		private transient double max = -1;
		
		Statystyka() {
			this(0);
		}
		Statystyka(double baza) {
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
			this.mnożnik = mnożnik;
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
		}
		public void zwiększBaza(double dodatek) {
			setBaza(getBaza() + dodatek);
		}
	
	
	}

	public final Statystyka zdrowie				= new Statystyka(100);
	public final Statystyka prędkośćKopania		= new Statystyka(100);
	public final Statystyka prędkośćChodzenia	= new Statystyka(100);
	public final Statystyka prędkośćAtaku		= new Statystyka(4);
	public final Statystyka unik				= new Statystyka(0);
	public final Statystyka szansaKryta			= new Statystyka(20);
	public final Statystyka dmgKryt				= new Statystyka(100);
	public final Statystyka dmg					= new Statystyka(1);
	public final Statystyka def					= new Statystyka(0);
	public final Statystyka defNiezależny		= new Statystyka(0);
	public final Statystyka inteligencja		= new Statystyka(100);
	
	
	public static final GraczRPG gracz(Player p) {
		return (GraczRPG) p.getMetadata("mimiGraczRPG").get(0).value();
	}
	
	
	public final Player p;
	
	GraczRPG(Player p) {
		this.p = p;
	}
}
