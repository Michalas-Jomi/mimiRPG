package me.jomi.mimiRPG;

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
}
