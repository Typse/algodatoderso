package de.ostfalia.aud.s24ss.base;

import java.time.LocalDate;

/**
 * Interface IEmployee für das Labor zu Algorithmen und Datenstrukturen. Das
 * Interface darf nicht geändert werden.
 * 
 * @author M. Gründel, D. Dick
 * @since SS 2024
 *
 */
public interface IEmployee extends Comparable<IEmployee> {

    /**
     * Liefert den Schlüssel zurück. Der Schlüssel stellt die eindeutige
     * Identifikation des/der Mitarbeiter*in dar.
     * 
     * @return Den Schlüssel
     */
    public int getKey();

    /**
     * Liefert den Nachnamen des Mitarbeiters/der Mitarbeiterin zurück.
     * 
     * @return Der Nachname des Mitarbeiters/der Mitarbeiterin
     */
    public String getName();

    /**
     * Liefert den Vornamen des Mitarbeiters/der Mitarbeiterin zurück.
     * 
     * @return Der Vorname des Mitarbeiters/der Mitarbeiterin
     */
    public String getFirstName();

    /**
     * Liefert das Geschlecht des Mitarbeiters/der Mitarbeiterin zurück.
     * 
     * @return Geschlecht (Gender) des Mitarbeiters/der Mitarbeiterin
     */
    public Gender getGender();

    /**
     * Liefert das Geburtsdatum des Mitarbeiters/der Mitarbeiterin zurück
     * 
     * @return Geburtsdatum des Mitarbeiters/der Mitarbeiterin
     */
    public LocalDate getBirthdate();

    /**
     * Liefert das Einstellungsdatum des Mitarbeiters/der Mitarbeiterin zurück
     * 
     * @return Einstellungsdatum des Mitarbeiters/der Mitarbeiterin
     */
    public LocalDate getHiredate();

    /**
     * Liefert die Abteilung des Mitarbeiters/der Mitarbeiterin zurück
     * 
     * @return Abteilung (Department) des Mitarbeiters/der Mitarbeiterin
     */
    public Department getDepartment();

    /**
     * Liefert den Datensatz als String zurück. Die einzelnen Elemente sind dabei
     * durch ";" getrennt. Beispiel:
     * "10002;1964-06-02;Bezalel;Simmel;F;1985-11-21;Sales"
     * 
     * @return Ein String mit den einzelnen Informationen des/der
     *         Mitarbeiters/Mitarbeiterin. Die Werte sind dabei durch ";" getrennt.
     */
    public String toString();
}