package org.freeplane.core.util;

public enum User {
	rafalM("Rafal", "rafalmachalica@interia.pl", CountryCode.pl), // 
	anatolyY("Anatoly Y", "2tolik@gmail.com", CountryCode.ru), //
	pyb("Pierre-Yves Baumann", "pyb@sunrise.ch", CountryCode.fr), //
	rladstaetter("Robert Ladstaetter", "rladstaetter@gmail.com", CountryCode.de), 
	teppl("Tamás Eppl","peletomi@gmail.com", CountryCode.hu);
	private final CountryCode countryCode;
	private final String email;
	private final String name;

	private User(String name, String email, CountryCode c) {
		this.name = name;
		this.email = email;
		this.countryCode = c;
	}

	public CountryCode getCountryCode() {
		return countryCode;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	static User getUserFor(CountryCode c) {
		for (User u : values()) {
			if (u.getCountryCode().equals(c)) {
				return u;
			}
		}
		return null;
	}
}
