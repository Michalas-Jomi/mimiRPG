package me.jomi.mimiRPG.util;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;

public class Krotka<T1, T2> extends Mapowany {
	@Mapowane public T1 a;
	@Mapowane public T2 b;
	
	
	public Krotka() {}
	public Krotka(T1 a, T2 b) {
		this.a = a;
		this.b = b;
	}

	public String toString() {
		return "§r(" + a.toString() + "§r, " + b.toString() + "§r)";
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof Krotka))
			return false;
		Krotka<?, ?> k2 = (Krotka<?, ?>) obj;
		return a.equals(k2.a) && b.equals(k2.b);
	}
}
