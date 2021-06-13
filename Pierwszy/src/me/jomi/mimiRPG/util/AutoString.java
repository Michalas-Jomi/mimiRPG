package me.jomi.mimiRPG.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.inventory.ItemStack;

public abstract class AutoString {
	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("§6");
		strB.append(this.getClass().getSimpleName());
		strB.append("§3(");
		
		AtomicBoolean ab = new AtomicBoolean(false);
		Func.dajFields(this.getClass()).forEach(field -> {
			if ((field.getModifiers() & 8) != 0) // static
				return;
			if (ab.get())
				strB.append("§c, ");
			else
				ab.set(true);
			strB.append("§a");
			strB.append(field.getName());
			strB.append("§d=§b");
			try {
				if (ItemStack.class.isAssignableFrom((Class<?>) field.getGenericType()))
					strB.append("item: ").append(Func.nazwaItemku((ItemStack) field.get(this)));
				else
					throw new Throwable();
			} catch (Throwable e) {
				try {
					strB.append(field.get(this));
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					throw Func.throwEx(e1);
				}
			}
		});
		
		strB.append("§3)§r");
		
		return strB.toString();
	}
}
