package me.jomi.mimiRPG.Minigry;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Mapowane;

public class Test implements ConfigurationSerializable {

	@Mapowane int zmienna1;
	@Mapowane Integer zmienna2;
	@Mapowane String zmiennaStr;
	@Mapowane char zmiennaChar;
	@Mapowane Location zmiennaLoc;
	@Mapowane List<TestB> testbLista;
	
	
	public static class TestB implements ConfigurationSerializable {
		
		@Mapowane int testBint;
		@Mapowane ItemStack testBItemStack;
		@Mapowane List<Integer> listaInteger;
		
		
		
		public String toString() {
			return Func.msg("TestB(%s,%s,%s)", testBint, testBItemStack);
		}

		public TestB(Map<String, Object> mapa){Func.zdemapuj(this, mapa);}
		public Map<String, Object> serialize() {return Func.zmapuj(this);}
	}
	
	public String toString() {
		return Func.msg("Test(%s,%s,%s,%s,%s,%s)", zmienna1, zmienna2, zmiennaStr, zmiennaChar, zmiennaLoc, testbLista);
	}
	
	public Test(Map<String, Object> mapa){Func.zdemapuj(this, mapa);}
	public Map<String, Object> serialize() {return Func.zmapuj(this);}
}
