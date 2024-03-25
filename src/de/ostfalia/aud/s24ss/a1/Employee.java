package de.ostfalia.aud.s24ss.a1;

import de.ostfalia.aud.s24ss.base.IEmployee;
import de.ostfalia.aud.s24ss.base.Gender;
import de.ostfalia.aud.s24ss.base.Department;
import java.time.LocalDate;

public class Employee implements IEmployee {

    final int employeeKey;

    String firstNameString;
    String nameString;

    Gender gender;
    Department department;

    LocalDate birthDate;
    LocalDate hireDate;

    public Employee(String string) {

        String[] data = {"", "", "", "", "", "", ""};
        int s = 0;

        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != ';') {
                data[s] += string.charAt(i);
            } else {
                s++;
            }
        }

        this.employeeKey = Integer.parseInt(data[0]);
        this.birthDate = LocalDate.parse(data[1]);
        this.firstNameString = data[2];
        this.nameString = data[3];
        this.gender = Gender.valueOf(data[4]);
        this.hireDate = LocalDate.parse(data[5]);
        this.department = Department.getDepartment(data[6]);
    }

    public int getKey() {
        return this.employeeKey;
    }

    public String getName() {
        return this.nameString;
    }

    public String getFirstName() {
        return this.firstNameString;
    }

    public Gender getGender() {
        return this.gender;
    }

    public LocalDate getBirthdate() {
        return this.birthDate;
    }

    public LocalDate getHiredate() {
        return this.hireDate;
    }

    public Department getDepartment() {
        return this.department;
    }

    public String toString() {
        return this.employeeKey + ";" + this.birthDate + ";" + this.firstNameString + ";" + this.nameString + ";" + 
            this.gender + ";" + this.hireDate + ";" + this.department;
    }

    @Override
    public int compareTo(IEmployee o) {
        return Integer.compare(employeeKey, o.getKey());
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Employee)) {
            //Prüft ob object ein Employee ist
            return false;
        }

        Employee second = (Employee) obj;
        if (second.nameString == null || nameString == null) {
            // Vergleich nur den Key!
            return (second.getKey() == this.employeeKey);
        } else {
            return (getKey() == second.getKey() && 
                    getName().equals(second.getName()) && 
                    getFirstName().equals(second.getFirstName()));
        }
    }

    public int hashCode() {
        return 0;
    }
}