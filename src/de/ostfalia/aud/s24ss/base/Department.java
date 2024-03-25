package de.ostfalia.aud.s24ss.base;

/**
 * Aufzählungsklasse "Department"
 * 
 * @author M. Gründel
 * @since SS 2024
 *
 */
public enum Department {
    /** SERVICE: Dienstleistung */
    SERVICE("Service"),
    /** DEVELOPMENT: Entwicklung */
    DEVELOPMENT("Development"),
    /** SALES: Verkaufsabteilung */
    SALES("Sales"),
    /** PRODUCTION: Produktion */
    PRODUCTION("Production"),
    /** MANPOWER: Montagepersonal */
    MANPOWER("Manpower"),
    /** RESEARCH: Forschung */
    RESEARCH("Research"),
    /** MARKETING: Marketing */
    MARKETING("Marketing"),
    /** MANAGEMENT: Management */
    MANAGEMENT("Management"),
    /** FINANCE: Finanzwesen */
    FINANCE("Finance");

    private String value;

    Department(String string) {
	this.value = string;
    }

    public String toString() {
	return value;
    }

    public static Department getDepartment(String str) {
	return Department.valueOf(str.toUpperCase());
    }
}
