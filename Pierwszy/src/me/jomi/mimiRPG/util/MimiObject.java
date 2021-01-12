package me.jomi.mimiRPG.util;

import java.lang.reflect.Field;

public abstract class MimiObject {
	public abstract String[] dajSprawdzanePola();

	@Override
	public int hashCode() {
		String[] nazwy = dajSprawdzanePola();
		
		Class<?> clazz = this.getClass();
		
		int prime = 31;
		int w = 1;
		
		for (int i=0; i < nazwy.length; i++)
			try {
				Object pole = Func.dajField(clazz, nazwy[i]).get(this);
				w = prime * w + (pole == null ? 0 : pole.hashCode());
			} catch (Throwable e) {
				throw new Error("Niepoprawne pole " + nazwy[i] + " przy sprawdzaniu " + clazz.getName() + ".hashCode()");
			}
		
		return w;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		String[] sprawdzanePola = dajSprawdzanePola();
		
		if (this == obj)
			return true;
		if (this == null || obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		Class<?> clazz = this.getClass();
		
		for (String nazwaPola : sprawdzanePola)
			try {
				Field pole = Func.dajField(clazz, nazwaPola);
				
				if (pole.get(this) == null) {
					if (pole.get(obj) != null)
						return false;
				} else if (!pole.get(this).equals(pole.get(obj)))
					return false;
				
			} catch (Throwable e) {
				throw new Error("Niepoprawne pole " + nazwaPola + " przy sprawdzaniu " + clazz.getName() + ".equals(Object)");
			}
		
		return true;
	}
}
