package com.razorfish.platforms.intellivault.filter;


/**
 *
 * An generic interface for filtering a given result set.  The intended use is for a filter, or Collection of filters,
 * to be passed to some method which uses or generating a result set.  Objects where allows() returns false are ignored \
 * from processing.
 *
 * @param <T> the Type of object to be filtered.
 */
public interface Filter<T> {
    /**
     * Determine if an object of Type T is allowed by this filter.
     *
     * @param object an object to be filtered.
     *
     * @return true if the object is allowed, false if it should be filtered out.
     */
    boolean allows(T object);
}
