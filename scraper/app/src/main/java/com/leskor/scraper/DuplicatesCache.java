package com.leskor.scraper;

import java.util.HashSet;
import java.util.Set;

/**
 * Duplicates Cache is a thread safe class for working with String hashes with the following constrains:
 * <p>
 * Can hold from 8640 to 17279 latest elements.
 * It clears the last half when it is full (17279 elements).
 * <p>
 * Used by {@link com.leskor.scraper.App}
 */
public class DuplicatesCache {
    private static final int CACHE_SIZE = 8640;

    private final Set<String> a = new HashSet<>();
    private final Set<String> b = new HashSet<>();
    private boolean isCurrentA = true;

    public boolean contains(String hash) {
        return a.contains(hash) || b.contains(hash);
    }

    synchronized public void add(String hash) {
        var currentSet = getCurrent();
        if (currentSet.size() >= CACHE_SIZE) {
            isCurrentA = !isCurrentA;
            currentSet = getCurrent();
            currentSet.clear();
        }
        currentSet.add(hash);
    }

    private Set<String> getCurrent() {
        return isCurrentA ? a : b;
    }
}
