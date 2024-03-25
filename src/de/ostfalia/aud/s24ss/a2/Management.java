package de.ostfalia.aud.s24ss.a2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.ostfalia.aud.s24ss.a1.Employee;
import de.ostfalia.aud.s24ss.base.*;

public class Management implements IManagement{

    private AlgoArrayList<Employee> employees;
    private KeyComparator comparator;
    
    public Management() {
        this.employees = new AlgoArrayList<>();

    }

    public Management(String dataName) {
        this();
        try (BufferedReader br = new BufferedReader(new FileReader(dataName))) {
            String line;
            while ((line = br.readLine()) != null) {
                Employee person = new Employee(line);
                this.employees.add(person);
            }
            this.employees.mergeSort(new KeyComparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Management(String[] dataInput) {
        this();

        for(String s : dataInput) {
            Employee employee = new Employee(s);
            this.employees.add(employee);
        }

        this.employees.mergeSort((new KeyComparator()));

    }


    @Override
    public int size() {
        return employees.size();
    }

    @Override
    public void insert(IEmployee member) {
        employees.add((Employee) member);
        employees.mergeSort(new KeyComparator());
    }

    @Override
    public boolean delete(int key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public IEmployee search(int key) {
        return (IEmployee) employees.binarySearch(comparator, key);
    }

    @Override
    public IEmployee[] search(String name, String firstName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public int size(Department department) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    @Override
    public IEmployee[] members(Department department) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'members'");
    }

    @Override
    public IEmployee[] toArray() {
        return null;
    }



}
