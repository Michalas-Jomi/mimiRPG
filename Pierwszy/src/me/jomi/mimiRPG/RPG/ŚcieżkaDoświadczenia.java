package me.jomi.mimiRPG.RPG;

import org.bukkit.Material;

public enum ŚcieżkaDoświadczenia {
	FARMER		("Farmer",		"Zbieraj plony i zabijaj zwięrzęta",15, Material.WHEAT,					20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	ŁOWCA		("Łowca",		"Zabijaj moby",						25, Material.BOW,					20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	KOPACZ		("Kopacz",		"Kop rudy i kamień",				20, Material.IRON_PICKAXE,			20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	DRWAL		("Drwal",		"Zcinaj drzewa",					11, Material.GOLDEN_AXE,			20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	RYBAK		("Rybak",		"Łów ryby",							24, Material.FISHING_ROD,			20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	MAG			("Mag",			"Enchantuj",						13, Material.ENCHANTING_TABLE,		20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	ALCHEMIK	("Alchemik",	"warz potki",						22, Material.BREWING_STAND,			20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000),
	BUDOWNICZY	("Budowniczy",	"stawiaj bloki",					19, Material.CHISELED_STONE_BRICKS,	20, 100, 250, 500, 1_000, 2_500, 5_000, 7_500, 12_000, 20_000, 30_000, 50_000, 75_000, 150_000, 500_000);
	

	// 0 - lvl 0 -> 1
	// 1 - lvl 1 -> 2
	public final int[] potrzebyExp;
	public final String nazwa;
	public final String opis;
	
	public final int slotWPanelu;
	public final Material ikona;
	
	ŚcieżkaDoświadczenia(String nazwa, String opis, int slotWPanelu, Material ikona, int... potrzebyExp) {
		this.slotWPanelu = slotWPanelu;
		this.potrzebyExp = potrzebyExp;
		this.ikona = ikona;
		this.nazwa = nazwa;
		this.opis = opis;
	}
}
