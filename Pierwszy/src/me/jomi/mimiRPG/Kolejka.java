package me.jomi.mimiRPG;

import java.util.List;

import com.google.common.collect.Lists;

public class Kolejka<E> {

	public List<E> obiekty = Lists.newArrayList();
	
	public Kolejka() {
		
	}
	public Kolejka(List<E> lista) {
		obiekty = lista;
	}
	public Kolejka(Iterable<E> lista) {
		obiekty = Lists.newArrayList(lista);
	}
	
	public E nastêpny() {
		E e = obiekty.get(0);
		obiekty.remove(0);
		return e;
	}
	public void dodaj(E e) {
		obiekty.add(e);
	}
	public E nastêpny_wróæ() {
		E e = nastêpny();
		dodaj(e);
		return e;
	}
	
	
	public String toString() {
		String w = "";
		for (E e : obiekty)
			w += "\n" + e;
		return "Kolejka(" + w + ")";
	}
	
	
	
}
