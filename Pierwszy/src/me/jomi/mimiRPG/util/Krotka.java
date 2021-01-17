package me.jomi.mimiRPG.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

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

	@Override
	public final String toString() {
		StringBuilder strB = new StringBuilder("§r(");
		Consumer<Object> str = obj -> strB.append(obj == null ? "null" : obj.toString());
		Object[] wartości = dajWartości();
		
		for (int i=0; i < wartości.length - 1; i++) {
			str.accept(wartości[i]);
			strB.append("§r, ");
		}
		str.accept(wartości[wartości.length - 1]);
		return strB.append("§r)").toString();
	}
	@Override
	@SuppressWarnings("rawtypes")
	public final boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj.getClass().getName().equals(this.getClass().getName())))
			return false;
		
		Object[] w1 = dajWartości();
		Object[] w2 = ((Krotka) obj).dajWartości();
		
		for (int i=0; i < w1.length; i++)
			if (!(w1[i].equals(w2[i])))
				return false;
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}
	
	protected final Object[] dajWartości() {
		List<Field> pola = Func.głębokiSkanKlasy(this.getClass());
		Object[] wartości = new Object[pola.size()];
		
		try {
			for (int i=0; i < wartości.length; i++)
				wartości[i] = pola.get(i).get(this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return wartości;
	}
}