package me.jomi.mimiRPG.Miniony;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;

public class Kopacz extends Minion{

	private static ItemStack he�m 	 = Func.dajG��wk�("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjY0NGM4ZjM2ZTIxZDVlYTk1NjI0OTI4ZTNmMzVhMmI0OTQ5Y2U3NWIyZmE2OGNjZDJiYzg0ZDlhZGEwY2I3In19fQ==");
	private static ItemStack klata 	 = Func.stw�rzItem(Material.CHAINMAIL_CHESTPLATE);
	private static ItemStack spodnie = Func.stw�rzItem(Material.DIAMOND_LEGGINGS);
	private static ItemStack buty 	 = Func.stw�rzItem(Material.DIAMOND_BOOTS);

	private Location blok;
	
	public Kopacz(Config config) {
		super(config);
	}
	protected void init(Config config) {}
	public Kopacz(Location loc, String stworzyciel) {
		super(loc, stworzyciel, "�1Kopacz");
	}
	public Kopacz(Player p, ItemStack item) {
		super(p, item);
	}
	protected void init() {}
	protected void zrespMoba() {
		super.zrespMoba();
		Location wysoko��Oczu = loc.clone().add(new Vector(0, getHeight(), 0));
		blok = wysoko��Oczu.clone().add(wysoko��Oczu.getDirection());
		setNoAI(true);
	}
	
	protected void dajItem(Player p) {
		dajItem(p, Miniony.itemKopacz);
	}
	protected void _dajItem(List<String> lore) {}
	
	protected void ubierz() {
		ubierz(he�m, klata, spodnie, buty);
	}

	protected void mimiTick() {
		if(mimiTick(true)) {
			wykop(blok.getBlock());
			podnie�Itemy(blok);
		}
	}

}
