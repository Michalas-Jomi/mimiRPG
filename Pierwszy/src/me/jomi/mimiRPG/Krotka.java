package me.jomi.mimiRPG;

public class Krotka<T1, T2> {
	public T1 a;
	public T2 b;
	
	public static <V1, V2> Krotka<V1, V2> stwórz(V1 a, V2 b) {
		Krotka<V1, V2> krotka = new Krotka<V1, V2>();
		krotka.a = a;
		krotka.b = b;
		return krotka;
	}
}
