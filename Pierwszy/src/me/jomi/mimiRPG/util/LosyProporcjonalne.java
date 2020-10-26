package me.jomi.mimiRPG.util;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;

public class LosyProporcjonalne<T> extends Mapowany {
	@Mapowane List<Krotka<Integer, T>> lista;
	@Mapowane int suma = 0;
	
	static final Random random = new Random();
	
	public LosyProporcjonalne() {}
	public LosyProporcjonalne(List<Krotka<Integer, T>> lista) {
		this.lista = lista;
		przetwórz();
	}
	void Init() {
		if (suma == 0)
			przetwórz();
	}
	
	void przetwórz() {
		List<Krotka<Integer, T>> nowa = Lists.newArrayList();
		for (Krotka<Integer, T> krotka : lista) {
			nowa.add(new Krotka<>(krotka.a + suma, krotka.b));
			suma += krotka.a;
		}
		this.lista = nowa;
	}
	
	
	public T losuj() {
		int los = random.nextInt(suma) + 1;
		
		int l = 0;
		int r = lista.size() - 1;
		
		while (l < r) {
			int s = l + ((r - l) / 2);
			int w = lista.get(s).a;
			
			if (w < los)
				l = s + 1;
			else
				r = s;
		}
		return lista.get(l).b;
	}
}
