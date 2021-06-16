package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public enum KolekcjaKategoria {
	FARMA(		"Farma",		"Twoja Farmerska Kolekcja",	11,	Material.IRON_HOE),
	GÓRNICTWO(	"Górnictwo",	"Twoja Górnicza Kolekcja",	12,	Material.NETHERITE_PICKAXE),
	WALKA(		"Walka",		"Twoja Wojownicza Kolekcja",13,	Material.DIAMOND_SWORD),
	LEŚNICTWO(	"Leśnictwo",	"Twoja Leśna Kolekcja",		14,	Material.ACACIA_SAPLING),
	RYBOŁÓWSTWO("Rybołóstwo",	"Twoja Rybska Kolekcja",	15,	Material.FISHING_ROD);
	//BOSSY(		"Bossy",		"Twoja Bosowa Kolekcja",	22,	Material.WITHER_SKELETON_SKULL);

	public final String nazwa;
	public final String opis;
	
	public final int slotWPanelu;
	public final Material ikona;
	
	public final List<Kolekcja> kolekcje = new ArrayList<>();
	
	KolekcjaKategoria(String nazwa, String opis, int slotWPanelu, Material ikona) {
		this.slotWPanelu = slotWPanelu;
		this.ikona = ikona;
		this.nazwa = nazwa;
		this.opis = opis;
	}
}
