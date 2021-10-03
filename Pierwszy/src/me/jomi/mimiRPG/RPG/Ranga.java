package me.jomi.mimiRPG.RPG;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.minecraft.nbt.NBTTagCompound;

import me.jomi.mimiRPG.util.Func;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Ranga {
	ZWYCZAJNY("f", NamedTextColor.WHITE),
	NADZWYCZAJNY("a", NamedTextColor.GREEN),
	RZADKI("9", NamedTextColor.BLUE),
	EPICKI("5", NamedTextColor.DARK_PURPLE),
	LEGENDARNY("6", NamedTextColor.GOLD),
	MISTYCZNY("c", NamedTextColor.RED),
	
	EVENTOWY("e", NamedTextColor.YELLOW);
	
	public final Team team;
	public final String kolor;
	public final String str;
	Ranga(String kolor, NamedTextColor chatColor) {
		this.kolor = (kolor.length() == 13 ? "&%" : "§") + kolor;
		this.str = Func.koloruj(this.kolor + name());

		try {
			Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
			Team team = sc.getTeam("rang" + name());
			if (team == null)
				team = sc.registerNewTeam("rang" + name());
			this.team = team;
		} catch (Throwable e) {
			throw Func.throwEx(e);
		}
		this.team.color(chatColor);
	}
	
	@Override
	public String toString() {
		return str;
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
