package me.jomi.mimiRPG.RPG;

import java.lang.reflect.Constructor;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagString;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG.Gemy.Gem;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KomendaZMapowanymiItemami;

@Moduł
public class Gemy extends KomendaZMapowanymiItemami<Gem> implements Listener {
	public static class Gem extends Bonusy {
		@Mapowane String id; // jednakowe co klucz w configu
		@Mapowane String nazwaGemu;
		@Mapowane List<Material> akceptowalneTypyItemów;
		
		@Mapowane ItemStack itemGemu;
		
		@Override
		public void dodajDoItemu(ItemStack item) {
			super.dodajDoItemu(item);
			Func.dodajLore(item, "§6Gem§8: §e" + Func.koloruj(nazwaGemu));
			
			try {
				net.minecraft.server.v1_16_R3.ItemStack nmsItem = ((net.minecraft.server.v1_16_R3.ItemStack) Func.dajField(CraftItemStack.class, "handle").get(item));
				NBTTagCompound tag = nmsItem.getOrCreateTag();
				NBTTagList gemy = (NBTTagList) tag.get("mimiAktywneGemy");
				if (gemy == null)
					tag.set("mimiAktywneGemy", gemy = new NBTTagList());
				gemy.add(nbtStr());
				nmsItem.setTag(tag);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		public boolean możnaNałożyć(ItemStack item) {
			if (akceptowalneTypyItemów.contains(item.getType()) && gemId(item) == null)
				try {
					net.minecraft.server.v1_16_R3.ItemStack nmsItem = ((net.minecraft.server.v1_16_R3.ItemStack) Func.dajField(CraftItemStack.class, "handle").get(item));
					NBTTagList gemy = (NBTTagList) nmsItem.getOrCreateTag().get("mimiAktywneGemy");
					if (gemy == null)
						return true;
					return !gemy.contains(nbtStr());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			return false;
		}
		
		private NBTTagString nbtStr() {
			try {
				Constructor<NBTTagString> constructor = NBTTagString.class.getDeclaredConstructor(String.class);
				constructor.setAccessible(true);
				return constructor.newInstance(id);
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public ItemStack dajGem() {
			net.minecraft.server.v1_16_R3.ItemStack item = CraftItemStack.asNMSCopy(itemGemu);
			
			NBTTagCompound tag = item.getOrCreateTag();
			tag.setString("mimiGem", id);
			item.setTag(tag);
			
			return CraftItemStack.asBukkitCopy(item);
		}
		
		public static String gemId(ItemStack item) {
			if (item == null || item.getType() == Material.AIR)
				return null;
			try {
				return ((net.minecraft.server.v1_16_R3.ItemStack) Func.dajField(CraftItemStack.class, "handle").get(item)).getTag().getString("mimiGem");
			} catch (NullPointerException e) {
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static final String prefix = Func.prefix("Gemy");
	
	public Gemy() {
		super("gemy", new Config("configi/gemy"),  Gem.class);
		
		edytor.zarejestrujOnZatwierdz((gem, ścieżka) -> gem.id = ścieżka);
		edytor.zarejestrójWyjątek("/gemy edytor id", (gem, ścieżka) -> null);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieEq(InventoryClickEvent ev) {
		if (!ev.isCancelled() && ev.getClick() == ClickType.RIGHT) {
			ItemStack cursor = ev.getWhoClicked().getItemOnCursor();
			Func.wykonajDlaNieNull(ev.getCurrentItem(), klikanyItem ->
				Func.wykonajDlaNieNull(Gem.gemId(cursor), gemId ->
					Func.wykonajDlaNieNull(config.wczytaj(gemId), Gem.class, gem -> {
						Main.log(gem);
						if (gem.możnaNałożyć(klikanyItem)) {
							gem.dodajDoItemu(klikanyItem);
							ev.setCurrentItem(klikanyItem);
							cursor.setAmount(cursor.getAmount() - 1);
							ev.getWhoClicked().setItemOnCursor(cursor.getAmount() <= 0 ? null : cursor);
							ev.setCancelled(true);
						}
					})));
		}
	}

	@Override public ItemStack	getItem(Gem gem) { return gem.dajGem(); }
	@Override public String		getPrefix()		 { return prefix; }

	@Override
	public void przeładuj() {
		super.przeładuj();
	}
}
