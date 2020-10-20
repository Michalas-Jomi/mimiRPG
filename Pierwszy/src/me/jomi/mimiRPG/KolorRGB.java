package me.jomi.mimiRPG;

import java.util.function.Function;

import org.bukkit.Color;

public class KolorRGB extends Mapowany {
	@Mapowane int red;
	@Mapowane int green;
	@Mapowane int blue;
	
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
}
