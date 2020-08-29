package me.jomi.mimiRPG.Miniony;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import net.minecraft.server.v1_16_R1.EntityChicken;
import net.minecraft.server.v1_16_R1.EntityCow;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityPig;
import net.minecraft.server.v1_16_R1.EntityPolarBear;
import net.minecraft.server.v1_16_R1.EntityRabbit;
import net.minecraft.server.v1_16_R1.EntitySheep;
import net.minecraft.server.v1_16_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_16_R1.PathfinderGoalNearestAttackableTarget;

public class Rzeźnik extends Minion{

	private static ItemStack hełm 	 = Func.dajGłówkę("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIyMDA3OTgzMjY4NGE2NmJiMmRjY2I3Yzg5ZjNlMTQ5NzFmNjk0YWY0YTE3NzQ1YTBlZmZkOWI5ZGZiZDUwNCJ9fX0=");
	private static ItemStack klata 	 = Func.stwórzItem(Material.GOLDEN_CHESTPLATE);
	private static ItemStack spodnie = Func.stwórzItem(Material.LEATHER_LEGGINGS);
	private static ItemStack buty 	 = Func.stwórzItem(Material.LEATHER_BOOTS);

	private boolean atakuje = false;	
	
	public Rzeźnik(Config config) {
		super(config);
	}
	protected void init(Config config) {
		staty.add(new Statystyka(config, "dmg"));
	}
	public Rzeźnik(Location loc, String stworzyciel) {
		super(loc, stworzyciel, "§cRzeźnik");
	}
	public Rzeźnik(Player p, ItemStack item) {
		super(p, item);
		staty.add(new Statystyka(item.getItemMeta().getLore().get(10)));
	}
	protected void init() {
		staty.add(new Statystyka("dmg"));
	}
	protected void zrespMoba() {
		super.zrespMoba();
		goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityPig>(this, EntityPig.class, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityCow>(this, EntityCow.class, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntitySheep>(this, EntitySheep.class, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityRabbit>(this, EntityRabbit.class, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityChicken>(this, EntityChicken.class, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityPolarBear>(this, EntityPolarBear.class, true));
		ustawDmg();
	}
	
	protected void _dajItem(List<String> lore) {}
	protected void dajItem(Player p) {
		dajItem(p, Miniony.itemRzeznik);
	}
	public void zamknij(Player p, Inventory inv, boolean menu) {
		super.zamknij(p, inv, menu);
		if (mimiTick(false))
			włączAtakZwierząt();
	}
	public void otwórz(Player p) {
		super.otwórz(p);
		wyłączAtakZwierząt();
	}
	private void wyłączAtakZwierząt() {
		if (!atakuje) return;
		((LivingEntity) getBukkitEntity()).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(-20);
		((LivingEntity) getBukkitEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		atakuje = false;
	}
	private void włączAtakZwierząt() {
		if (atakuje) return;
		ustawDmg();
		((LivingEntity) getBukkitEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.23);
		atakuje = true;
	}
	
	protected void ubierz() {
		ubierz(hełm, klata, spodnie, buty);
	}
	protected void mimiTick() {
		if (mimiTick(true)) {
			podnieśItemy(1.5, 1, 1.5);
			włączAtakZwierząt();
		} else 
			wyłączAtakZwierząt();
	}

	protected void ustawDmg() {
		((LivingEntity) getBukkitEntity()).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(staty.get(3).akt);
	}
	
	protected void ulepszeniaOdśwież(Inventory inv){
		super.ulepszeniaOdśwież(inv);
		ustawItem(inv, 15, Material.IRON_SWORD, "&2Atak", Arrays.asList("&3Aktualny dmg: &e"   + staty.get(3).str(), "&3Następny poziom: &e" + staty.get(3).str2(), staty.get(3).strCena()));
	}
	public boolean kliknięcie(Player p, InventoryClickEvent ev) {
		ItemStack item = ev.getCurrentItem();
		if (item == null || item.getType().equals(Material.AIR))
			return false;
		if (ev.getView().getTitle().equals("§4§lUlepszenia"))
			if (ev.getCurrentItem().getItemMeta().getDisplayName().equals("§2Atak"))
				if (staty.get(3).ulepsz(p)) {
					ulepszeniaOdśwież(ev.getInventory());
					ustawDmg();
					return true;
				}
		return super.kliknięcie(p, ev);
	}

}
