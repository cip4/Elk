/*
 * Created on Aug 29, 2004
 */
package org.cip4.elk.util;

import java.util.Comparator;

import org.cip4.jdflib.util.JDFDate;

/**
 * A <code>Comparator</code> for
 * {@link com.heidelberg.JDFLib.util.JDFDate JDFDate} objects.
 * 
 * @see java.util.Comparator
 * @see com.heidelberg.JDFLib.util.JDFDate
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class JDFDateComparator implements Comparator {

    private static final int SORT_ASCENDING = 1;

    private static final int SORT_DESCENDING = -1;

    private int _sortOrder;

    /**
     * Creates a <code>JDFDate</code> comparator that sorts in ascending
     * order.
     */
    public JDFDateComparator() {
        this(true);
    }

    /**
     * Creates a <code>JDFDate</code> comparator with the specified sort order.
     * 
     * @param sortOrder
     *            <code>true</code> for descending order; <code>false</code>
     *            for ascending order
     */
    public JDFDateComparator(boolean sortAscending) {
        if (sortAscending)
            _sortOrder = SORT_ASCENDING;
        else
            _sortOrder = SORT_DESCENDING;
    }

    /**
     * Copied from java.util.Comparator: Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     * 
     * The implementor must ensure that sgn(compare(x, y)) == -sgn(compare(y,
     * x)) for all x and y. (This implies that compare(x, y) must throw an
     * exception if and only if compare(y, x) throws an exception.)
     * 
     * The implementor must also ensure that the relation is transitive:
     * ((compare(x, y)>0) && (compare(y, z)>0)) implies compare(x, z)>0.
     * 
     * Finally, the implementer must ensure that compare(x, y)==0 implies that
     * sgn(compare(x, z))==sgn(compare(y, z)) for all z.
     * 
     * It is generally the case, but not strictly required that (compare(x,
     * y)==0) == (x.equals(y)). Generally speaking, any comparator that violates
     * this condition should clearly indicate this fact. The recommended
     * language is "Note: this comparator imposes orderings that are
     * inconsistent with equals."
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        JDFDate d1 = (JDFDate) o1;
        JDFDate d2 = (JDFDate) o2;
        int result = 0;
        if (d1.isEarlier(d2)) {
            result = -1;
        } else if (d1.isLater(d2)) {
            result = 1;
        }
        return result * _sortOrder;
    }
}