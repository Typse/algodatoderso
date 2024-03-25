package de.ostfalia.aud.s24ss.a2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.ostfalia.aud.s24ss.a1.Employee;

public class AlgoArrayList<Employee> implements Iterable<Employee> {
    private Object[] array;
    private int size = 0;
    private static final int DEFAULT_CAPACITY = 8;

    public AlgoArrayList() {
        array = new Object[DEFAULT_CAPACITY];
    }

    public void add(Employee element) {
        if (size == array.length) {
            array = Arrays.copyOf(array, array.length * 2);
        }
        array[size++] = element;
    }

    public Employee get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (Employee) array[index];
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<Employee> iterator() {
        return new Iterator<Employee>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Employee next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (Employee) array[currentIndex++];
            }
        };
    }

    public void mergeSort(Comparator<Employee> comparator) {
        if (size > 1) {
            Employee[] aux = (Employee[]) new Object[size];
            mergeSortRec((Employee[]) array, aux, 0, size - 1, comparator);
        }
    }

    private void mergeSortRec(Employee[] array, Employee[] aux, int low, int high, Comparator<Employee> comparator) {
        if (low < high) {
            int middle = low + (high - low) / 2;
            mergeSortRec(array, aux, low, middle, comparator);
            mergeSortRec(array, aux, middle + 1, high, comparator);
            merge(array, aux, low, middle, high, comparator);
        }
    }

    private void merge(Employee[] array, Employee[] aux, int low, int middle, int high, Comparator<Employee> comparator) {
        for (int k = low; k <= high; k++) {
            aux[k] = array[k];
        }
        int i = low, j = middle + 1;
        for (int k = low; k <= high; k++) {
            if (i > middle) array[k] = (Employee) aux[j++];
            else if (j > high) array[k] = (Employee) aux[i++];
            else if (comparator.compare((Employee) aux[i], (Employee) aux[j]) <= 0) array[k] = (Employee) aux[i++];
            else array[k] = (Employee) aux[j++];
        }
    }

    public List<Employee> binarySearch(KeyComparator comparator, int key) {
        int low = 0;
        int high = size - 1;
        List<Employee> results = new ArrayList<>();

        while (low <= high) {
            int mid = (low + high) / 2;
            Employee midVal = (Employee) array[mid];
            int cmp = comparator.compare(midVal, key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                // Schlüssel gefunden, jetzt finde alle Duplikate dieses Schlüssels
                // Suche nach links von mid
                int left = mid - 1;
                while (left >= low && comparator.compare((Employee) array[left], key) == 0) {
                    results.add((Employee) array[left]);
                    left--;
                }
            }

            results.add(midVal);

            int right = mid + 1;
            while (right <= high && comparator.compare((Employee) array[right], key) == 0) {
                results.add((Employee) array[right]);
                right++;
            }

            break;
        }

        return (List<Employee>) results;
    }
}

class KeyComparator implements Comparator<Employee> {
    @Override
    public int compare(Employee e1, Employee e2) {
        return Integer.compare(e1.getKey(), e2.getKey());
    }

    public int compare(Employee e1, int key) {
        return Integer.compare(e1.getKey(), key);
    }
}


class NameComparator implements Comparator<Employee> {
    @Override
    public int compare(Employee e1, Employee e2) {
        int lastNameComparison = e1.getName().compareTo(e2.getName());
        if (lastNameComparison != 0) {
            return lastNameComparison;
        } else {
            return e1.getFirstName().compareTo(e2.getFirstName());
        }
    }
}
