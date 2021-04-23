package me.jomi.mimiRPG.util;

import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Mapowany;

public class KolorRGB extends Mapowany {
	@Mapowane int red;
	@Mapowane int green;
	@Mapowane int blue;
	
	static enum Domyślne {
		BLACK(0, 0, 0),
		DARK_BLUE(0, 0, 170),
		DARK_GREEN(0, 170, 0),
		DARK_AQUA(0, 170, 170),
		DARK_RED(170, 0, 0),
		DARK_PURPLE(170, 0, 170),
		GOLD(255, 170, 0),
		GRAY(170, 170, 170),
		
		DARK_GRAY(85, 85, 85),
		BLUE(85, 85, 255),
		GREEN(85, 255, 85),
		AQUA(85, 255, 255),
		RED(255, 85, 85),
		LIGHT_PURPLE(255, 85, 255),
		YELLOW(255, 255, 85),
		WHITE(255, 255, 255);

		KolorRGB kolor;
		Domyślne(int r, int g, int b) {
			kolor = new KolorRGB(r, g, b);
		}
	}
	
	
	public KolorRGB(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
	}
	public KolorRGB() {}
	public void Init() {
		red 	= Math.max(0, Math.min(255, red));
		green 	= Math.max(0, Math.min(255, green));
		blue 	= Math.max(0, Math.min(255, blue));
	}
	
	public Color kolor() {
		return Color.fromRGB(red, green, blue);
	}
	public String kolorChat() {
		Function<Integer, String> hex = i -> {
			String s = Integer.toHexString(i).toUpperCase() + "0";
			return "§" + s.charAt(0) + "§" + s.charAt(1);
		};
		return "§x" + hex.apply(red) + hex.apply(green) + hex.apply(blue);
	}

	public ChatColor zbliżony() {
		return ChatColor.valueOf(Func.max(Lists.newArrayList(Domyślne.values()), 
				k -> -(Math.abs(red - k.kolor.red) + Math.abs(green - k.kolor.green) + Math.abs(blue - k.kolor.blue))).toString());
	}


}
