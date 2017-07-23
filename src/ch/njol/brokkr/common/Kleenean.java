package ch.njol.brokkr.common;

import java.util.Locale;

public enum Kleenean {
	TRUE, FALSE, UNKNOWN;
	
	final String stringValue;
	
	private Kleenean() {
		stringValue = "" + name().toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public String toString() {
		return stringValue;
	}
}