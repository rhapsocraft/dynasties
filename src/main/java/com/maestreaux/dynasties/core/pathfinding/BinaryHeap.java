package com.maestreaux.dynasties.core.pathfinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BinaryHeap<T> {
    private List<T> heap = new ArrayList<>();
    private Comparator<T> comparator;

    public BinaryHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }
}