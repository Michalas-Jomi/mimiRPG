package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.util.Func;

public enum Ranga {
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
	

	public static Ranga ranga(ItemStack item) {
		return ranga(ZfaktoryzowaneItemy.tag(item));
	}
	static Ranga ranga(NBTTagCompound tag) {
		try {
			return Func.StringToEnum(Ranga.class, tag.getString("ranga"));
		} catch (Throwable e) {
			return Ranga.ZWYCZAJNY;
		}
	}
	public static void ustawRangę(ItemStack item, Ranga ranga) {
		NBTTagCompound tag = ZfaktoryzowaneItemy.tag(item);
		ustawRangę(tag, ranga);
		ZfaktoryzowaneItemy.ustawTag(item, tag);
	}
	static void ustawRangę(NBTTagCompound tag, Ranga ranga) {
		tag.setString("ranga", ranga.name());
	}
}
