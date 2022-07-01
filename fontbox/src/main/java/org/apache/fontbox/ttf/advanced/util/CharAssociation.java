/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fontbox.ttf.advanced.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A structure class encapsulating an interval of characters expressed as an offset and count of
 * Unicode scalar values (in an IntBuffer). A <code>CharAssociation</code> is used to maintain a
 * backpointer from a glyph to one or more character intervals from which the glyph was derived.
 *
 * Each glyph in a glyph sequence is associated with a single <code>CharAssociation</code> instance.
 *
 * A <code>CharAssociation</code> instance is additionally (and optionally) used to record
 * predication information about the glyph, such as whether the glyph was produced by the
 * application of a specific substitution table or whether its position was adjusted by a specific
 * poisitioning table.
 *
 * @author Glenn Adams
 */
public class CharAssociation implements Cloneable {

    // instance state
    private final int offset;
    private final int count;
    private final int[] subIntervals;
    private Map<String, Object> predications;

    // class state
    private static volatile Map<String, PredicationMerger> predicationMergers;

    interface PredicationMerger {
        Object merge(String key, Object v1, Object v2);
    }

    /**
     * Instantiate a character association.
     * @param offset into array of Unicode scalar values (in associated IntBuffer)
     * @param count of Unicode scalar values (in associated IntBuffer)
     * @param subIntervals if disjoint, then array of sub-intervals, otherwise null; even
     * members of array are sub-interval starts, and odd members are sub-interval
     * ends (exclusive)
     */
    public CharAssociation(int offset, int count, int[] subIntervals) {
        this.offset = offset;
        this.count = count;
        this.subIntervals = ((subIntervals != null) && (subIntervals.length > 2)) ? subIntervals : null;
    }

    /**
     * Instantiate a non-disjoint character association.
     * @param offset into array of UTF-16 code elements (in associated CharSequence)
     * @param count of UTF-16 character code elements (in associated CharSequence)
     */
    public CharAssociation(int offset, int count) {
        this (offset, count, null);
    }

    /**
     * Instantiate a non-disjoint character association.
     * @param subIntervals if disjoint, then array of sub-intervals, otherwise null; even
     * members of array are sub-interval starts, and odd members are sub-interval
     * ends (exclusive)
     */
    public CharAssociation(int[] subIntervals) {
        this (getSubIntervalsStart(subIntervals), getSubIntervalsLength(subIntervals), subIntervals);
    }

    /** @return offset (start of association interval) */
    public int getOffset() {
        return offset;
    }

    /** @return count (number of characer codes in association) */
    public int getCount() {
        return count;
    }

    /** @return start of association interval */
    public int getStart() {
        return getOffset();
    }

    /** @return end of association interval */
    public int getEnd() {
        return getOffset() + getCount();
    }

    /** @return true if association is disjoint */
    public boolean isDisjoint() {
        return subIntervals != null;
    }

    /** @return subintervals of disjoint association */
    public int[] getSubIntervals() {
        return subIntervals;
    }

    /** @return count of subintervals of disjoint association */
    public int getSubIntervalCount() {
        return (subIntervals != null) ? (subIntervals.length / 2) : 0;
    }

    /**
     * @param offset of interval in sequence
     * @param count length of interval
     * @return true if this association is contained within [offset,offset+count)
     */
    public boolean contained(int offset, int count) {
        int s = offset;
        int e = offset + count;
        if (!isDisjoint()) {
            int s0 = getStart();
            int e0 = getEnd();
            return (s0 >= s) && (e0 <= e);
        } else {
            int ns = getSubIntervalCount();
            for (int i = 0; i < ns; i++) {
                int s0 = subIntervals [ 2 * i + 0 ];
                int e0 = subIntervals [ 2 * i + 1 ];
                if ((s0 >= s) && (e0 <= e)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Set predication &lt;KEY,VALUE&gt;.
     * @param key predication key
     * @param value predication value
     */
    public void setPredication(String key, Object value) {
        if (predications == null) {
            predications = new HashMap<String, Object>();
        }
        if (predications != null) {
            predications.put(key, value);
        }
    }

    /**
     * Get predication KEY.
     * @param key predication key
     * @return predication KEY at OFFSET or null if none exists
     */
    public Object getPredication(String key) {
        if (predications != null) {
            return predications.get(key);
        } else {
            return null;
        }
    }

    /**
     * Merge predication &lt;KEY,VALUE&gt;.
     * @param key predication key
     * @param value predication value
     */
    public void mergePredication(String key, Object value) {
        if (predications == null) {
            predications = new HashMap<String, Object>();
        }
        if (predications != null) {
            if (predications.containsKey(key)) {
                Object v1 = predications.get(key);
                Object v2 = value;
                predications.put(key, mergePredicationValues(key, v1, v2));
            } else {
                predications.put(key, value);
            }
        }
    }

    /**
     * Merge predication values V1 and V2 on KEY. Uses registered <code>PredicationMerger</code>
     * if one exists, otherwise uses V2 if non-null, otherwise uses V1.
     * @param key predication key
     * @param v1 first (original) predication value
     * @param v2 second (to be merged) predication value
     * @return merged value
     */
    public static Object mergePredicationValues(String key, Object v1, Object v2) {
        PredicationMerger pm = getPredicationMerger(key);
        if (pm != null) {
            return pm.merge(key, v1, v2);
        } else if (v2 != null) {
            return v2;
        } else {
            return v1;
        }
    }

    /**
     * Merge predications from another CA.
     * @param ca from which to merge
     */
    public void mergePredications(CharAssociation ca) {
        if (ca.predications != null) {
            for (Map.Entry<String, Object> e : ca.predications.entrySet()) {
                mergePredication(e.getKey(), e.getValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        try {
            CharAssociation ca = (CharAssociation) super.clone();
            if (predications != null) {
                ca.predications = new HashMap<String, Object>(predications);
            }
            return ca;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Register predication merger PM for KEY.
     * @param key for predication merger
     * @param pm predication merger
     */
    public static void setPredicationMerger(String key, PredicationMerger pm) {
        if (predicationMergers == null) {
            predicationMergers = new HashMap<String, PredicationMerger>();
        }
        if (predicationMergers != null) {
            predicationMergers.put(key, pm);
        }
    }

    /**
     * Obtain predication merger for KEY.
     * @param key for predication merger
     * @return predication merger or null if none exists
     */
    public static PredicationMerger getPredicationMerger(String key) {
        if (predicationMergers != null) {
            return predicationMergers.get(key);
        } else {
            return null;
        }
    }

    /**
     * Replicate association to form <code>repeat</code> new associations.
     * @param a association to replicate
     * @param repeat count
     * @return array of replicated associations
     */
    public static CharAssociation[] replicate(CharAssociation a, int repeat) {
        CharAssociation[] aa = new CharAssociation [ repeat ];
        for (int i = 0, n = aa.length; i < n; i++) {
            aa [ i ] = (CharAssociation) a.clone();
        }
        return aa;
    }

    /**
     * Join (merge) multiple associations into a single, potentially disjoint
     * association.
     * @param aa array of associations to join
     * @return (possibly disjoint) association containing joined associations
     */
    public static CharAssociation join(CharAssociation[] aa) {
        CharAssociation ca;
        // extract sorted intervals
        int[] ia = extractIntervals(aa);
        if ((ia == null) || (ia.length == 0)) {
            ca = new CharAssociation(0, 0);
        } else if (ia.length == 2) {
            int s = ia[0];
            int e = ia[1];
            ca = new CharAssociation(s, e - s);
        } else {
            ca = new CharAssociation(mergeIntervals(ia));
        }
        return mergePredicates(ca, aa);
    }

    private static CharAssociation mergePredicates(CharAssociation ca, CharAssociation[] aa) {
        for (CharAssociation a : aa) {
            ca.mergePredications(a);
        }
        return ca;
    }

    private static int getSubIntervalsStart(int[] ia) {
        int us = Integer.MAX_VALUE;
        int ue = Integer.MIN_VALUE;
        if (ia != null) {
            for (int i = 0, n = ia.length; i < n; i += 2) {
                int s = ia [ i + 0 ];
                int e = ia [ i + 1 ];
                if (s < us) {
                    us = s;
                }
                if (e > ue) {
                    ue = e;
                }
            }
            if (ue < 0) {
                ue = 0;
            }
            if (us > ue) {
                us = ue;
            }
        }
        return us;
    }

    private static int getSubIntervalsLength(int[] ia) {
        int us = Integer.MAX_VALUE;
        int ue = Integer.MIN_VALUE;
        if (ia != null) {
            for (int i = 0, n = ia.length; i < n; i += 2) {
                int s = ia [ i + 0 ];
                int e = ia [ i + 1 ];
                if (s < us) {
                    us = s;
                }
                if (e > ue) {
                    ue = e;
                }
            }
            if (ue < 0) {
                ue = 0;
            }
            if (us > ue) {
                us = ue;
            }
        }
        return ue - us;
    }

    /**
     * Extract sorted sub-intervals.
     */
    private static int[] extractIntervals(CharAssociation[] aa) {
        int ni = 0;
        for (int i = 0, n = aa.length; i < n; i++) {
            CharAssociation a = aa [ i ];
            if (a.isDisjoint()) {
                ni += a.getSubIntervalCount();
            } else {
                ni += 1;
            }
        }
        int[] sa = new int [ ni ];
        int[] ea = new int [ ni ];
        for (int i = 0, k = 0; i < aa.length; i++) {
            CharAssociation a = aa [ i ];
            if (a.isDisjoint()) {
                int[] da = a.getSubIntervals();
                for (int j = 0; j < da.length; j += 2) {
                    sa [ k ] = da [ j + 0 ];
                    ea [ k ] = da [ j + 1 ];
                    k++;
                }
            } else {
                sa [ k ] = a.getStart();
                ea [ k ] = a.getEnd();
                k++;
            }
        }
        return sortIntervals(sa, ea);
    }

    private static final int[] SORT_INCREMENTS_16
        = { 1391376, 463792, 198768, 86961, 33936, 13776, 4592, 1968, 861, 336, 112, 48, 21, 7, 3, 1 };

    private static final int[] SORT_INCREMENTS_03
        = { 7, 3, 1 };

    /**
     * Sort sub-intervals using modified Shell Sort.
     */
    private static int[] sortIntervals(int[] sa, int[] ea) {
        assert sa != null;
        assert ea != null;
        assert sa.length == ea.length;
        int ni = sa.length;
        int[] incr = (ni < 21) ? SORT_INCREMENTS_03 : SORT_INCREMENTS_16;
        for (int k = 0; k < incr.length; k++) {
            for (int h = incr [ k ], i = h, n = ni, j; i < n; i++) {
                int s1 = sa [ i ];
                int e1 = ea [ i ];
                for (j = i; j >= h; j -= h) {
                    int s2 = sa [ j - h ];
                    int e2 = ea [ j - h ];
                    if (s2 > s1) {
                        sa [ j ] = s2;
                        ea [ j ] = e2;
                    } else if ((s2 == s1) && (e2 > e1)) {
                        sa [ j ] = s2;
                        ea [ j ] = e2;
                    } else {
                        break;
                    }
                }
                sa [ j ] = s1;
                ea [ j ] = e1;
            }
        }
        int[] ia = new int [ ni * 2 ];
        for (int i = 0; i < ni; i++) {
            ia [ (i * 2) + 0 ] = sa [ i ];
            ia [ (i * 2) + 1 ] = ea [ i ];
        }
        return ia;
    }

    /**
     * Merge overlapping and abutting sub-intervals.
     */
    private static int[] mergeIntervals(int[] ia) {
        int ni = ia.length;
        int i;
        int n;
        int nm;
        int is;
        int ie;
        // count merged sub-intervals
        for (i = 0, n = ni, nm = 0, is = ie = -1; i < n; i += 2) {
            int s = ia [ i + 0 ];
            int e = ia [ i + 1 ];
            if ((ie < 0) || (s > ie)) {
                is = s;
                ie = e;
                nm++;
            } else if (s >= is) {
                if (e > ie) {
                    ie = e;
                }
            }
        }
        int[] mi = new int [ nm * 2 ];
        // populate merged sub-intervals
        for (i = 0, n = ni, nm = 0, is = ie = -1; i < n; i += 2) {
            int s = ia [ i + 0 ];
            int e = ia [ i + 1 ];
            int k = nm * 2;
            if ((ie < 0) || (s > ie)) {
                is = s;
                ie = e;
                mi [ k + 0 ] = is;
                mi [ k + 1 ] = ie;
                nm++;
            } else if (s >= is) {
                if (e > ie) {
                    ie = e;
                }
                mi [ k - 1 ] = ie;
            }
        }
        return mi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(offset);
        sb.append(',');
        sb.append(count);
        sb.append(']');
        return sb.toString();
    }

}
