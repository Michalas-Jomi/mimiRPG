package me.jomi.mimiRPG.SkyBlock.Multi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class ZfaktoryzowaneItemy implements Listener {
	public static enum Ranga {
		ZWYCZAJNY("f", ChatColor.WHITE),
		NADZWYCZAJNY("a", ChatColor.GREEN),
		RZADKI("9", ChatColor.BLUE),
		EPICKI("5", ChatColor.DARK_PURPLE),
		LEGENDARNY("6", ChatColor.GOLD),
		MISTYCZNY("c", ChatColor.RED),
		
		EVENTOWY("e", ChatColor.YELLOW);
		
		public final Team team;
		public final String kolor;
		Ranga(String kolor, ChatColor chatColor) {
			this.kolor = (kolor.length() == 13 ? "&%" : "§") + kolor;
			try {
				Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
				Team team = sc.getTeam("rang" + name());
				if (team == null)
					team = sc.registerNewTeam("rang" + name());
				this.team = team;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			this.team.setColor(chatColor);
		}
		
		@Override
		public String toString() {
			return Func.koloruj(kolor + name());
		}
	}
	
	public static void przerób(Player p, ItemStack item) {
		if (item == null) return;
		przerób(p, GraczRPG.gracz(p), item, NMS.nms(item));
	}
	private static void przerób(Player p, GraczRPG gracz, org.bukkit.inventory.ItemStack bukkit, net.minecraft.server.v1_16_R3.ItemStack nms) {
		NBTTagCompound tag = tag(bukkit);
		ItemMeta meta = bukkit.getItemMeta();
		List<String> lore = new ArrayList<>();
		
		Ranga ranga = ranga(tag);
		lore.add(" ");
		lore.add(ranga.toString());
		
		meta.setLore(lore);
		
		bukkit.setItemMeta(meta);
	}
	
	private static NBTTagCompound tag(ItemStack item) {
		return NMS.nms(item).getOrCreateTag().getCompound("mimiFactor");
	}
	static void ustawTag(ItemStack item, NBTTagCompound tag) {
		net.minecraft.server.v1_16_R3.ItemStack nms = NMS.nms(item);
		
		boolean miał = nms.hasTag();
		
		NBTTagCompound nbt = nms.getOrCreateTag();
		nbt.set("mimiFactor", tag);
		
		if (!miał)
			nms.setTag(nbt);
	}
	
	public static Ranga ranga(ItemStack item) {
		return ranga(tag(item));
	}
	static Ranga ranga(NBTTagCompound tag) {
		try {
			return Func.StringToEnum(Ranga.class, tag.getString("ranga"));
		} catch (Throwable e) {
			return Ranga.ZWYCZAJNY;
		}
	}
	public static void ustawRangę(ItemStack item, Ranga ranga) {
		NBTTagCompound tag = tag(item);
		ustawRangę(tag, ranga);
		ustawTag(item, tag);
	}
	static void ustawRangę(NBTTagCompound tag, Ranga ranga) {
		tag.setString("ranga", ranga.name());
	}

	
	@EventHandler
	public void spawnItemu(ItemSpawnEvent ev) {
		Ranga ranga = ranga(ev.getEntity().getItemStack());
		
		ustawRangę(ev.getEntity().getItemStack(), ranga);
		
		ranga.team.addEntry(ev.getEntity().getUniqueId().toString());
		ev.getEntity().setGlowing(true);
	}
	
	
	@EventHandler
	public void otwieranieInv(InventoryOpenEvent ev) {
		((CraftInventory) ev.getInventory()).getContents();
		ev.getInventory().forEach(item -> przerób((Player) ev.getPlayer(), item));
	}
	@EventHandler
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (!(ev.getEntity() instanceof Player)) return;
		
		przerób((Player) ev.getEntity(), ev.getItem().getItemStack());
	}
}
