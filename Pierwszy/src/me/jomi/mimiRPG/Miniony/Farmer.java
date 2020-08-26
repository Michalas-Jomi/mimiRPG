package me.jomi.mimiRPG.Miniony;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

public class Farmer extends Minion{

	private static ItemStack he�m 	 = Func.dajG��wk�("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjc3ZDQxNWY5YmFhNGZhNGI1ZTA1OGY1YjgxYmY3ZjAwM2IwYTJjOTBhNDgzMWU1M2E3ZGJjMDk4NDFjNTUxMSJ9fX0=");
	private static ItemStack klata 	 = Func.stw�rzItem(Material.IRON_CHESTPLATE);
	private static ItemStack spodnie = Func.stw�rzItem(Material.LEATHER_LEGGINGS);
	private static ItemStack buty 	 = Func.stw�rzItem(Material.LEATHER_BOOTS);
	
	protected boolean sadzenie = false;
	
	protected static int ulepszanieSadzenieCena = 20000;
	
	
	public Config zapisz() {
		Config config = super.zapisz();
		
		config.ustaw_zapisz("sadzenie", sadzenie);
		
		return config;
	}
	public Farmer(Config config) {
		super(config);
	}
	protected void init(Config config) {
		sadzenie = (boolean) config.wczytaj("sadzenie");
	}
	public Farmer(Location loc, String stworzyciel) {
		super(loc, stworzyciel, "�aFarmer");
	}
	public Farmer(Player p, ItemStack item) {
		super(p, item);
		sadzenie = item.getItemMeta().getLore().get(10).split(" ")[1].startsWith("�a");
	}
	protected void init() {}
	protected void zrespMoba() {
		super.zrespMoba();
		goalSelector 	= new PathfinderGoalSelector(getWorld().getMethodProfilerSupplier());
		goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
	}
	
	protected void dajItem(Player p) {
		dajItem(p, Miniony.itemFarmer);
	}
	protected void _dajItem(List<String> lore) {
		lore.add("sadzenie: " + zakup(sadzenie));
	}
	
	protected void ubierz() {
		ubierz(he�m, klata, spodnie, buty);
	}
	
	protected void idzDoNast�pnej() {
		Location blok = new Location(loc.getWorld(), locX(), locY(), locZ()).add(0, .2, 0);
		blok.add(-2, 0, -2);
		for (int z=0; z<5; z++) {
			for (int x=0; x<5; x++) {
				if (doWykopania(blok)) {
					idzDo(blok.getBlock().getLocation(), 1);
					return;
				}
				blok.add(1, 0, 0);
			}
			blok.add(-5, 0, 1);
		}
	}
	
	private boolean doWykopania(Location l) {
		Block b = l.getBlock();
		if (b == null) return false;
		switch (b.getType()) {
		case POTATOES:
		case CARROTS:
		case WHEAT:
			return b.getBlockData().getAsString().contains("age=7");
		case NETHER_WART:
		case BEETROOTS:
			return b.getBlockData().getAsString().contains("age=3");
		default:
			return false;
		}
	}
	
	protected void wykop(Location loc) {
		Location l = loc.clone();
		l.add(-1, 0, -1);
		for (int z=0; z<3; z++) {
			for (int x=0; x<3; x++) {
				wykop(l.getBlock());
				l.add(1, 0, 0);
			}
			l.add(-3, 0, 1);
		}
		podnie�Itemy(2.5, 2, 2.5);
	}
	protected void wykop(Block b) {
		Material mat = b.getType();
		switch (b.getType()) {
		case POTATOES:
		case CARROTS:
		case WHEAT:
			if (b.getBlockData().getAsString().contains("age=7")) {
				super.wykop(b);
				if (sadzenie)
					b.setType(mat);
			}
			break;
		case NETHER_WART:
		case BEETROOTS:
			if (b.getBlockData().getAsString().contains("age=3")) {
				super.wykop(b);
				if (sadzenie)
					b.setType(mat);
			}
			break;
		default:
			break;
		}
	}
	
	protected void mimiTick() {
		if (mimiTick(true)) {
			Location l = new Location(loc.getWorld(), locX(), locY(), locZ()).add(0, .2, 0);
			wykop(l);
			idzDoNast�pnej();
		}
	}
	
	protected void ulepszeniaOd�wie�(Inventory inv){
		super.ulepszeniaOd�wie�(inv);
		ustawItem(inv, 22, Material.WHEAT_SEEDS, "&2Sadzenie", Func.BooleanToString(sadzenie, Arrays.asList("&aZakupione"), Arrays.asList("&cNie Zakupione", "&3Cena: &e" + Func.IntToString(ulepszanieSadzenieCena) + "$")));
		}
	public boolean klikni�cie(Player p, InventoryClickEvent ev) {
		ItemStack item = ev.getCurrentItem();
		if (item == null || item.getType().equals(Material.AIR))
			return false;
		if (ev.getView().getTitle().equals("�4�lUlepszenia")) {
			if (ev.getCurrentItem().getItemMeta().getDisplayName().equals("�2Sadzenie")) {
				if (sadzenie) return true;
				if (Main.econ.getBalance(p) >= ulepszanieSadzenieCena) {
					Main.econ.withdrawPlayer(p, ulepszanieSadzenieCena);
					sadzenie = true;
					ulepszeniaOd�wie�(ev.getInventory());
				} else
					p.sendMessage(prefix + "Nie sta� ci� na to");
				return true;
			}
		}
		return super.klikni�cie(p, ev);
	}

}
