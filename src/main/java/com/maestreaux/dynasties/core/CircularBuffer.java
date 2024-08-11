package com.maestreaux.dynasties.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CircularBuffer<T> {
    private final List<T> buffer;  // The buffer to hold data
    private int head;  // Points to the next write position
    private int tail;  // Points to the next read position
    private int size;  // The current number of elements in the buffer
    private final int capacity;  // The fixed capacity of the buffer

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public void add(T element) {
        // Add element to the buffer at the head position
        buffer.set(head, element);
        head = (head + 1) % capacity;

        // If the buffer is full, move the tail forward to overwrite the oldest element
        if (size == capacity) {
            tail = (tail + 1) % capacity;
        } else {
            size++;
        }
    }

    public T get() {
        if (size == 0) {
            return null;
        }

        // Get the element at the tail position
        T element = buffer.get(tail);
        buffer.set(tail, null);  // Optional: clear the reference
        tail = (tail + 1) % capacity;
        size--;
        return element;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }
}