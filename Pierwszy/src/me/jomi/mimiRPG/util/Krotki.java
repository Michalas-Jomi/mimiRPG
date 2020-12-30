package me.jomi.mimiRPG.util;

import me.jomi.mimiRPG.Mapowane;

public class Krotki {
	public static class Box<T> {
		public T a;
		
		public Box(T a) {
			this.a = a;
		}
	}
	public static class TriKrotka<T1, T2, T3> extends Krotka<T1, Krotka<T2, T3>> {
		@Mapowane public T2 b;
		@Mapowane public T3 c;
		
		public TriKrotka() {}
		public TriKrotka(T1 a, T2 b, T3 c) {
			super.b = new Krotka<>(b, c);
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
	public static class QuadKrotka<T1, T2, T3, T4> extends Krotka<Krotka<T1, T2>, Krotka<T3, T4>> {
		@Mapowane public T1 a;
		@Mapowane public T2 b;
		@Mapowane public T3 c;
		@Mapowane public T4 d;
		
		public QuadKrotka() {}
		public QuadKrotka(T1 a, T2 b, T3 c, T4 d) {
			super.a = new Krotka<>(a, b);
			super.b = new Krotka<>(c, d);
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}
	}
	public static class PentKrotka<T1, T2, T3, T4, T5> extends Krotka<Krotka<T1, T2>, TriKrotka<T3, T4, T5>> {
		@Mapowane public T1 a;
		@Mapowane public T2 b;
		@Mapowane public T3 c;
		@Mapowane public T4 d;
		@Mapowane public T5 e;
		
		public PentKrotka() {}
		public PentKrotka(T1 a, T2 b, T3 c, T4 d, T5 e) {
			super.a = new Krotka<>(a, b);
			super.b = new TriKrotka<>(c, d, e);
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
		}
	}
	public static class HexKrotka<T1, T2, T3, T4, T5, T6> extends Krotka<TriKrotka<T1, T2, T3>, TriKrotka<T4, T5, T6>> {
		@Mapowane public T1 a;
		@Mapowane public T2 b;
		@Mapowane public T3 c;
		@Mapowane public T4 d;
		@Mapowane public T5 e;
		@Mapowane public T6 f;
		
		public HexKrotka() {}
		public HexKrotka(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f) {
			super.a = new TriKrotka<>(a, b, c);
			super.b = new TriKrotka<>(d, e, f);
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
			this.f = f;
		}
	}
	
	public static class MonoKrotka<T> extends Krotka<T, T> {
		public MonoKrotka() {}
		public MonoKrotka(T a, T b) {
			super(a, b);
		}
	}
	public static class MonoTriKrotka<T> extends TriKrotka<T, T, T> {
		public MonoTriKrotka() {}
		public MonoTriKrotka(T a, T b, T c) {
			super(a, b, c);
		}
	}
	public static class MonoQuadKrotka<T> extends QuadKrotka<T, T, T, T> {
		public MonoQuadKrotka() {}
		public MonoQuadKrotka(T a, T b, T c, T d) {
			super(a, b, c, d);
		}
	}
	public static class BiQuadKrotka<T1, T2> extends QuadKrotka<T1, T1, T2, T2> {
		public BiQuadKrotka() {}
		public BiQuadKrotka(T1 a, T1 b, T2 c, T2 d) {
			super(a, b, c, d);
		}
	}
	public static class MonoPentKrotka<T> extends PentKrotka<T, T, T, T, T> {
		public MonoPentKrotka() {}
		public MonoPentKrotka(T a, T b, T c, T d, T e) {
			super(a, b, c, d, e);
		}
	}
	public static class MonoHexKrotka<T> extends HexKrotka<T, T, T, T, T, T> {
		public MonoHexKrotka() {}
		public MonoHexKrotka(T a, T b, T c, T d, T e, T f) {
			super(a, b, c, d, e, f);
		}
	}
	public static class BiHexKrotka<T1, T2> extends HexKrotka<T1, T1, T1, T2, T2, T2> {
		public BiHexKrotka() {}
		public BiHexKrotka(T1 a, T1 b, T1 c, T2 d, T2 e, T2 f) {
			super(a, b, c, d, e, f);
		}
	}
	public static class TriHexKrotka<T1, T2, T3> extends HexKrotka<T1, T1, T2, T2, T3, T3> {
		public TriHexKrotka() {}
		public TriHexKrotka(T1 a, T1 b, T2 c, T2 d, T3 e, T3 f) {
			super(a, b, c, d, e, f);
		}
	}
}
