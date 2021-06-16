package me.jomi.mimiRPG.RPG;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

public enum Kolekcja implements IŚcieżka {
	KOBL		(KolekcjaKategoria.GÓRNICTWO, Material.COBBLESTONE, "Twoja Kolekcja Kobla",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	WĘGIEL		(KolekcjaKategoria.GÓRNICTWO, Material.COAL,		"Twoja Kolekcja Węgla",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ŻELAZO		(KolekcjaKategoria.GÓRNICTWO, Material.IRON_INGOT,	"Twoja Kolekcja Żelaza",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ZŁOTO		(KolekcjaKategoria.GÓRNICTWO, Material.GOLD_INGOT,	"Twoja Kolekcja Złota",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	DIAMENTY	(KolekcjaKategoria.GÓRNICTWO, Material.DIAMOND,		"Twoja Kolekcja Diamentów",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	LAPIS		(KolekcjaKategoria.GÓRNICTWO, Material.LAPIS_LAZULI,"Twoja Kolekcja Lapisu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SZMARAGDY	(KolekcjaKategoria.GÓRNICTWO, Material.EMERALD,		"Twoja Kolekcja Szmaragdów",50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	REDSTONE	(KolekcjaKategoria.GÓRNICTWO, Material.REDSTONE,	"Twoja Kolekcja Redstone",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	KWARC		(KolekcjaKategoria.GÓRNICTWO, Material.QUARTZ,		"Twoja Kolekcja Kwarcu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	OBSYDIAN	(KolekcjaKategoria.GÓRNICTWO, Material.OBSIDIAN,	"Twoja Kolekcja Obsydianu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	JASNOGŁAZ	(KolekcjaKategoria.GÓRNICTWO, Material.GLOWSTONE,	"Twoja Kolekcja Jasnogłazu",50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ŻWIR		(KolekcjaKategoria.GÓRNICTWO, Material.GRAVEL,		"Twoja Kolekcja Żwiru",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	LÓD			(KolekcjaKategoria.GÓRNICTWO, Material.ICE,			"Twoja Kolekcja Lodu",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	NETHERRACK	(KolekcjaKategoria.GÓRNICTWO, Material.NETHERRACK,	"Twoja Kolekcja Netherracku",50,100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	PIASEK		(KolekcjaKategoria.GÓRNICTWO, Material.SAND,		"Twoja Kolekcja Piasku",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	KAMIEŃ_ENDU	(KolekcjaKategoria.GÓRNICTWO, Material.END_STONE,	"Twoja Kolekcja Kamienia endu",50,100,250,500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	
	
	PSZENICA	(KolekcjaKategoria.FARMA, Material.WHEAT,		"Twoja Kolekcja Pszenicy",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	MARCHEWKI	(KolekcjaKategoria.FARMA, Material.CARROT,		"Twoja Kolekcja Marchwi",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ZIEMNIAKI	(KolekcjaKategoria.FARMA, Material.POTATO,		"Twoja Kolekcja Ziemniaków",50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	BURAKI		(KolekcjaKategoria.FARMA, Material.BEETROOT,	"Twoja Kolekcja Buraków",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	DYNIE		(KolekcjaKategoria.FARMA, Material.PUMPKIN,		"Twoja Kolekcja Dyń",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ARBUZY		(KolekcjaKategoria.FARMA, Material.MELON_SLICE,	"Twoja Kolekcja Arbuzów",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	GRZYBY		(KolekcjaKategoria.FARMA, Material.RED_MUSHROOM,"Twoja Kolekcja Grzybów",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	KAKO		(KolekcjaKategoria.FARMA, Material.COCOA_BEANS,	"Twoja Kolekcja Kakaa",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	KAKTUSY		(KolekcjaKategoria.FARMA, Material.CACTUS,		"Twoja Kolekcja Kaktusów",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	TRZCINA		(KolekcjaKategoria.FARMA, Material.SUGAR_CANE,	"Twoja Kolekcja Trzciny",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	PIÓRA		(KolekcjaKategoria.FARMA, Material.FEATHER,		"Twoja Kolekcja Piór",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SKÓRA		(KolekcjaKategoria.FARMA, Material.LEATHER,		"Twoja Kolekcja Skóry",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	WIEPRZOWINA	(KolekcjaKategoria.FARMA, Material.PORKCHOP,	"Twoja Kolekcja Wieprzowiny",50,100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	DRÓB		(KolekcjaKategoria.FARMA, Material.CHICKEN,		"Twoja Kolekcja Drobiu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	WOŁOWINA	(KolekcjaKategoria.FARMA, Material.BEEF,		"Twoja Kolekcja Wołowiu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	JAGNIĘCINA	(KolekcjaKategoria.FARMA, Material.MUTTON,		"Twoja Kolekcja Jagnięciny",50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	PASZTET		(KolekcjaKategoria.FARMA, Material.RABBIT,		"Twoja Kolekcja Pasztetu",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	BRODAWKI	(KolekcjaKategoria.FARMA, Material.NETHER_WART,	"Twoja Kolekcja Brodawek",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),

	
	ZOMBIE		(KolekcjaKategoria.WALKA, Material.ROTTEN_FLESH,"Twoja Kolekcja Zgniłego mięsa",50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SZKIELET	(KolekcjaKategoria.WALKA, Material.BONE,		"Twoja Kolekcja Kości",			50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	PAJĄKI		(KolekcjaKategoria.WALKA, Material.STRING,		"Twoja Kolekcja Pajęczyn",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	CREEPER		(KolekcjaKategoria.WALKA, Material.GUNPOWDER,	"Twoja Kolekcja Prochu",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ENDERMAN	(KolekcjaKategoria.WALKA, Material.ENDER_PEARL,	"Twoja Kolekcja Pereł",			50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	DUCH		(KolekcjaKategoria.WALKA, Material.GHAST_TEAR,	"Twoja Kolekcja Łez",			50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SZLAM		(KolekcjaKategoria.WALKA, Material.SLIME_BALL,	"Twoja Kolekcja Szlamu",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	PŁOMYK		(KolekcjaKategoria.WALKA, Material.BLAZE_ROD,	"Twoja Kolekcja Płomieni",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	MAGMA		(KolekcjaKategoria.WALKA, Material.MAGMA_CREAM,	"Twoja Kolekcja Magmy",			50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	
	
	DĄB			(KolekcjaKategoria.LEŚNICTWO, Material.OAK_LOG,			"Twoja Kolekcja Dębu",					50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ŚWIERK		(KolekcjaKategoria.LEŚNICTWO, Material.SPRUCE_LOG,		"Twoja Kolekcja Świeków",				50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	BRZOZA		(KolekcjaKategoria.LEŚNICTWO, Material.BIRCH_LOG,		"Twoja Kolekcja Brzóz",					50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	CIEMNY_DĄB	(KolekcjaKategoria.LEŚNICTWO, Material.DARK_OAK_LOG,	"Twoja Kolekcja Ciemnych Dębów",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	AKACJA		(KolekcjaKategoria.LEŚNICTWO, Material.ACACIA_LOG,		"Twoja Kolekcja Akacji",				50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	TROPIKALNE	(KolekcjaKategoria.LEŚNICTWO, Material.JUNGLE_LOG,		"Twoja Kolekcja Trocpikalnego Drewna",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SPACZONE	(KolekcjaKategoria.LEŚNICTWO, Material.WARPED_STEM,		"Twoja Kolekcja Spaczonego Drewna",		50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	SZKARŁATNE	(KolekcjaKategoria.LEŚNICTWO, Material.CRIMSON_STEM,	"Twoja Kolekcja Szkarłatnego Drewna",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),

		
	DORSZ		(KolekcjaKategoria.RYBOŁÓWSTWO, Material.COD,			"Twoja Kolekcja Dorszy",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ŁOSOŚ		(KolekcjaKategoria.RYBOŁÓWSTWO, Material.SALMON,		"Twoja Kolekcja Łososi",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	BŁAZENEK	(KolekcjaKategoria.RYBOŁÓWSTWO, Material.TROPICAL_FISH,	"Twoja Kolekcja Błazenków",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000),
	ROZDYMKA	(KolekcjaKategoria.RYBOŁÓWSTWO, Material.PUFFERFISH,	"Twoja Kolekcja Rozdymek",	50, 100, 250, 500, 1_000, 1_750, 3_000, 5_000, 10_000, 17_500, 30_000, 50_000, 75_000, 120_000, 200_000);
	
	public static final String prefix = Func.prefix(Kolekcja.class);
	
	public final KolekcjaKategoria kategoria;

	public final int[] potrzebnyExp;
	public final String nazwa;
	public final String opis;
	
	public final Material mat;

	Kolekcja(KolekcjaKategoria kategoria, Material mat, String opis, int... potrzebyExp) {
		this.nazwa = Func.enumToString(this);
		this.potrzebnyExp = potrzebyExp;
		this.kategoria = kategoria;
		this.opis = opis;
		this.mat = mat;
		
		kategoria.kolekcje.add(this);
	}
	
	@Override
	public int[] getPotrzebnyExp() {
		return potrzebnyExp;
	}
	@Override
	public String getNazwa() {
		return nazwa;
	}
	@Override
	public String getOpis() {
		return opis;
	}
	@Override
	public Material getIkona() {
		return mat;
	}


	private static final Map<Material, Kolekcja> mapaZItemu = new EnumMap<>(Material.class);
	static {
		Func.forEach(Kolekcja.values(), kolekcja -> mapaZItemu.put(kolekcja.mat, kolekcja));
	}
	public static Kolekcja zItemu(Material mat) {
		return mapaZItemu.get(mat);
	}


	public static ItemStack oznakujItem(ItemStack item) {
		NMS.nms(item).getOrCreateTag().setBoolean("kolekcyjny", true);
		return item;
	}
	public static boolean oznakowanyItem(ItemStack item) {
		NBTTagCompound tag = NMS.nms(item).getTag();
		return tag == null ? false : tag.getBoolean("kolekcyjny");
	}
	public static ItemStack oznakujItemUndo(ItemStack item) {
		Func.wykonajDlaNieNull(NMS.nms(item).getTag(), tag -> tag.remove("kolekcyjny"));
		return item;
	}
	
	public static void podniósł(Player p, ItemStack item) {
		if (oznakowanyItem(item)) {
			Func.wykonajDlaNieNull(GraczRPG.gracz(p).getKolekcja(Kolekcja.zItemu(item.getType())), kolekcja -> kolekcja.zwiększExp(item.getAmount()));
			oznakujItemUndo(item);
		}
	}
}
