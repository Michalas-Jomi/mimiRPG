package me.jomi.mimiRPG.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;

public class Cena extends Mapowany {
	@Mapowane public Integer walutaPremium;
	@Mapowane public Double kasa;
	@Mapowane public Integer exp;
	
	public Cena() {
		this(null, null, null);
	}
	public Cena(Double kasa) {
		this(kasa, null, null);
	}
	public Cena(Double kasa, Integer exp) {
		this(kasa, exp, null);
	}
	public Cena(Double kasa, Integer exp, Integer walutaPremium) {
		this.walutaPremium = walutaPremium;
		this.kasa = kasa;
		this.exp = exp;
	}
	
	
	public boolean staćGo(Player p) {
		return !((exp != null && 			Poziom.policzCałyExp(p) < exp) ||
			 	 (walutaPremium != null && !p.getInventory().containsAtLeast(Baza.walutaPremium, walutaPremium)) ||
			 	 (kasa != null && 			Main.econ.getBalance(p) < kasa));
	}
	
	public boolean zabierz(Player p) {
		return	zabierzExp(p) &&
				zabierzKase(p) &&
				zabierzPremium(p);
	}
	
	public boolean zabierzPremium(Player p) {
		if (walutaPremium != null) {
			ItemStack item;
			int ile = walutaPremium;
			PlayerInventory inv = p.getInventory();
			for (int i=0; i < inv.getSize(); i++)
				if ((item = inv.getItem(i)) != null && item.isSimilar(Baza.walutaPremium)) {
					ile -= item.getAmount();
					if (ile >= 0)
						inv.setItem(i, null);
					else {
						item.setAmount(-ile);
						inv.setItem(i, item);
					}
					if (ile <= 0)
						return true;
				}
			Func.dajWPremium(p, walutaPremium - ile);
			return false;
		}
		return true;
	}
	public boolean zabierzKase(Player p) {
		if (kasa != null) {
			if (Main.econ == null || Main.econ.getBalance(p) < kasa)
				return false;
			Main.econ.withdrawPlayer(p, kasa);
		}
		return true;
	}
	public boolean zabierzExp(Player p) {
		if (exp != null) {
			if (Poziom.policzCałyExp(p) < exp)
				return false;
			p.giveExp(-exp);
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		if (kasa != null)
			str.append(Func.DoubleToString(kasa)).append('$');
		if (exp != null)
			str.append(' ').append(Func.IntToString(exp)).append(" expa");
		if (walutaPremium != null)
			str.append(' ').append(Func.IntToString(walutaPremium)).append("x ").append(Func.nazwaItemku(Baza.walutaPremium));
		
		return str.toString();
	}
}
