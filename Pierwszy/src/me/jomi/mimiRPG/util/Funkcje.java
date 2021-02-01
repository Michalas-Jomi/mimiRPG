package me.jomi.mimiRPG.util;

public class Funkcje {
	public static interface TriFunction<T1, T2, T3, R> {
		R apply(T1 t1, T2 t2, T3 t3);
	}
	public static interface TriConsumer <T1, T2, T3> {
		void accept(T1 t1, T2 t2, T3 t3);
	};
	public static interface TriPredicate<T1, T2, T3> extends TriFunction<T1, T2, T3, Boolean> {};
}
