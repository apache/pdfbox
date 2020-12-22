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
package org.apache.pdfbox.util;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * see http://de.wikipedia.org/wiki/Quicksort.
 * 
 * @author Uwe Pachler
 * @author Manuel Aristaran
 */
public final class QuickSort
{
    
    private QuickSort()
    {
    }
    
    private static final Comparator<? extends Comparable> OBJCOMP = Comparable::compareTo;

    /**
     * Sorts the given list using the given comparator.
     * 
     * @param <T> type of the objects to be sorted.
     * @param list list to be sorted
     * @param cmp comparator used to compare the objects within the list
     */
    public static <T> void sort(final List<T> list, final Comparator<T> cmp)
    {
        final int size = list.size();
        if (size < 2)
        {
            return;
        }
        quicksort(list, cmp);
    }

    /**
     * Sorts the given list using compareTo as comparator.
     * 
     * @param <T> type of the objects to be sorted.
     * @param list list to be sorted
     */
    public static <T extends Comparable> void sort(final List<T> list)
    {
        sort(list, (Comparator<T>) OBJCOMP);
    }

    private static <T> void quicksort(final List<T> list, final Comparator<T> cmp)
    {
        final Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        stack.push(list.size());
        while (!stack.isEmpty())
        {
            final int right = stack.pop();
            final int left = stack.pop();
            if (right - left < 2)
            {
                continue;
            }
            int p = left + ((right - left) / 2);
            p = partition(list, cmp, p, left, right);

            stack.push(p + 1);
            stack.push(right);

            stack.push(left);
            stack.push(p);
        }
    }

    private static <T> int partition(final List<T> list, final Comparator<T> cmp, final int p, final int start, final int end)
    {
        int l = start;
        int h = end - 2;
        final T piv = list.get(p);
        swap(list, p, end - 1);

        while (l < h)
        {
            if (cmp.compare(list.get(l), piv) <= 0)
            {
                l++;
            }
            else if (cmp.compare(piv, list.get(h)) <= 0)
            {
                h--;
            }
            else
            {
                swap(list, l, h);
            }
        }
        int idx = h;
        if (cmp.compare(list.get(h), piv) < 0)
        {
            idx++;
        }
        swap(list, end - 1, idx);
        return idx;
    }

    private static <T> void swap(final List<T> list, final int i, final int j)
    {
        final T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }
}
