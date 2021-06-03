package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.Map;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

public class Timming implements AutoCloseable {
	
	static final Map<String, Krotka<Double, Integer>> timmingi = new HashMap<>();
	
	
	public final String nazwa;
	final long start;
	int czas = -1;
	
	public Timming(String nazwa) {
		this.nazwa = nazwa;
		this.start = System.currentTimeMillis();
	}
	
	@Override
	public void close() {
		czas = (int) (System.currentTimeMillis() - start);
		
		Krotka<Double, Integer> akt = timmingi.get(nazwa);
		if (akt == null) {
			akt = new Krotka<>(0d, 0);
			timmingi.put(nazwa, akt);
		}
		akt.a = (akt.a * akt.b + czas) / ((double) akt.b + 1);
		akt.b++;
	}
	
	public static Krotka<String, Object> raport(String nazwa) {
		Krotka<Double, Integer> timming = timmingi.getOrDefault(nazwa, new Krotka<>(0d, 0));
		
		return Func.r("timming " + nazwa, Func.zaokrąglij(timming.a, 2) + "ms x" + timming.b);
	}
	public static Iterable<String> utworzone() {
		return timmingi.keySet();
	}
	
	
	public static void test(String nazwa, Runnable lambda) {
		try (Timming timming = new Timming(nazwa)) {
			lambda.run();
		}
	}
}
